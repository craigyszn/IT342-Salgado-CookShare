package edu.cit.salgado.cookshare.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.cit.salgado.cookshare.entity.NutritionData;
import edu.cit.salgado.cookshare.entity.Recipe;
import edu.cit.salgado.cookshare.repository.NutritionRepository;
import edu.cit.salgado.cookshare.repository.RecipeRepository;

@Service
public class NutritionService {

    @Value("${spoonacular.api-key}")
    private String apiKey;

    private final NutritionRepository nutritionRepository;
    private final RecipeRepository recipeRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String SPOONACULAR_BASE = "https://api.spoonacular.com";

    public NutritionService(NutritionRepository nutritionRepository,
                            RecipeRepository recipeRepository) {
        this.nutritionRepository = nutritionRepository;
        this.recipeRepository    = recipeRepository;
    }

    // ── Get nutrition for any recipe (DB or Spoonacular) ─────────────────────
    public NutritionData getNutrition(String recipeId) {
        // Check if already cached in DB
        Optional<NutritionData> cached = nutritionRepository.findByRecipeId(recipeId);
        if (cached.isPresent()) return cached.get();

        // Check if it's a user-created recipe (DB recipe)
        Optional<Recipe> dbRecipe = recipeRepository.findById(recipeId);
        if (dbRecipe.isPresent()) {
            return fetchNutritionFromIngredients(recipeId, dbRecipe.get().getIngredients());
        }

        // Otherwise treat as Spoonacular recipe ID
        return fetchNutritionFromSpoonacular(recipeId);
    }

    // ── Fetch nutrition for Spoonacular recipes ───────────────────────────────
    private NutritionData fetchNutritionFromSpoonacular(String recipeId) {
        try {
            String url = SPOONACULAR_BASE + "/recipes/" + recipeId
                    + "/nutritionWidget.json?apiKey=" + apiKey;

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            if (!response.getStatusCode().is2xxSuccessful()) return defaultNutrition(recipeId);

            JsonNode root = objectMapper.readTree(response.getBody());

            double calories = parseNutrient(root, "calories");
            double carbs    = parseNutrientFromBadList(root, "Carbohydrates");
            double protein  = parseNutrientFromBadList(root, "Protein");
            double fat      = parseNutrientFromBadList(root, "Fat");
            double fiber    = parseNutrientFromBadList(root, "Fiber");

            NutritionData data = new NutritionData(recipeId, calories, protein, carbs, fat, fiber);
            return nutritionRepository.save(data);

        } catch (Exception e) {
            return defaultNutrition(recipeId);
        }
    }

    // ── Fetch nutrition for user-created recipes using ingredients ────────────
    private NutritionData fetchNutritionFromIngredients(String recipeId, List<String> ingredients) {
        try {
            String ingredientText = String.join("\n", ingredients);
            String url = SPOONACULAR_BASE + "/recipes/analyze?apiKey=" + apiKey
                    + "&includeNutrition=true";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String body = "{\"title\":\"Recipe\",\"servings\":1,\"ingredients\":"
                    + objectMapper.writeValueAsString(ingredients)
                    + ",\"instructions\":\"\"}";

            HttpEntity<String> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) return defaultNutrition(recipeId);

            JsonNode root     = objectMapper.readTree(response.getBody());
            JsonNode nutrition = root.path("nutrition").path("nutrients");

            double calories = 0, protein = 0, carbs = 0, fat = 0, fiber = 0;

            if (nutrition.isArray()) {
                for (JsonNode n : nutrition) {
                    String name = n.path("name").asText();
                    double amount = n.path("amount").asDouble();
                    switch (name) {
                        case "Calories"        -> calories = amount;
                        case "Protein"         -> protein  = amount;
                        case "Carbohydrates"   -> carbs    = amount;
                        case "Fat"             -> fat      = amount;
                        case "Fiber"           -> fiber    = amount;
                    }
                }
            }

            NutritionData data = new NutritionData(recipeId, calories, protein, carbs, fat, fiber);
            return nutritionRepository.save(data);

        } catch (Exception e) {
            return defaultNutrition(recipeId);
        }
    }

    // ── Parse helpers ─────────────────────────────────────────────────────────
    private double parseNutrient(JsonNode root, String field) {
        try {
            String raw = root.path(field).asText("0")
                    .replaceAll("[^0-9.]", "");
            return Double.parseDouble(raw);
        } catch (Exception e) { return 0; }
    }

    private double parseNutrientFromBadList(JsonNode root, String name) {
        try {
            JsonNode bad = root.path("bad");
            JsonNode good = root.path("good");
            for (JsonNode n : bad) {
                if (n.path("title").asText().equalsIgnoreCase(name))
                    return Double.parseDouble(
                        n.path("amount").asText("0").replaceAll("[^0-9.]", ""));
            }
            for (JsonNode n : good) {
                if (n.path("title").asText().equalsIgnoreCase(name))
                    return Double.parseDouble(
                        n.path("amount").asText("0").replaceAll("[^0-9.]", ""));
            }
        } catch (Exception e) { /* ignore */ }
        return 0;
    }

    private NutritionData defaultNutrition(String recipeId) {
        return new NutritionData(recipeId, 0, 0, 0, 0, 0);
    }
}