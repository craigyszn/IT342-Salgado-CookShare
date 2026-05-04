package edu.cit.salgado.cookshare.features.recipe;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import edu.cit.salgado.cookshare.features.rating.RatingRepository;
import edu.cit.salgado.cookshare.features.user.SupabaseStorageService;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private final RecipeService recipeService;
    private final SupabaseStorageService supabaseStorageService;
    private final RatingRepository ratingRepository;

    public RecipeController(
            RecipeService recipeService,
            SupabaseStorageService supabaseStorageService,
            RatingRepository ratingRepository) {
        this.recipeService = recipeService;
        this.supabaseStorageService = supabaseStorageService;
        this.ratingRepository = ratingRepository;
    }

    @GetMapping
    public List<Recipe> getRecipes() {
        return recipeService.getAllRecipes();
    }

    @PostMapping
    public Recipe createRecipe(@RequestBody Recipe recipe) {
        return recipeService.createRecipe(recipe);
    }

    @GetMapping("/user")
    public List<Recipe> getRecipesByUser(@RequestParam String email) {
        return recipeService.getRecipesByUser(email);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteRecipe(@PathVariable String id) {
        recipeService.deleteRecipe(id);
        return ResponseEntity.ok("Recipe deleted");
    }

    // ── Upload recipe image to Supabase ───────────────────────────────────────
    @PostMapping("/upload-image")
    public ResponseEntity<?> uploadRecipeImage(@RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = supabaseStorageService.uploadRecipeImage(file);
            return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Image upload failed: " + e.getMessage());
        }
    }

    // ── Check if user already rated ───────────────────────────────────────────
    @GetMapping("/{id}/my-rating")
    public ResponseEntity<?> getMyRating(
            @PathVariable String id,
            @RequestParam String email) {
        return ratingRepository.findByUserEmailAndRecipeId(email, id)
            .map(r -> ResponseEntity.ok(Map.of("rated", true, "stars", r.getRatingValue())))
            .orElse(ResponseEntity.ok(Map.of("rated", false, "stars", 0)));
    }

    // ── Rate endpoint ─────────────────────────────────────────────────────────
    @PostMapping("/{id}/rate")
    public ResponseEntity<?> rateRecipe(
            @PathVariable String id,
            @RequestBody Map<String, Object> body) {

        int stars = 0;
        String userEmail = "";

        try {
            stars = (Integer) body.get("stars");
            userEmail = (String) body.getOrDefault("userEmail", "");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid request body");
        }

        if (stars < 1 || stars > 5) {
            return ResponseEntity.badRequest().body("Rating must be between 1 and 5");
        }

        if (userEmail.isEmpty()) {
            return ResponseEntity.badRequest().body("userEmail is required");
        }

        Recipe updated = recipeService.rateRecipe(id, stars, userEmail);

        if (updated == null) {
            return ResponseEntity.status(409).body("You have already rated this recipe");
        }

        return ResponseEntity.ok(updated);
    }
}