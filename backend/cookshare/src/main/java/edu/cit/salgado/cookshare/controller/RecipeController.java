package edu.cit.salgado.cookshare.controller;

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

import edu.cit.salgado.cookshare.entity.Recipe;
import edu.cit.salgado.cookshare.service.RecipeService;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
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

    // ── Rate endpoint — now requires userEmail to prevent duplicates ───────────
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
            // Could be duplicate or recipe not found
            return ResponseEntity.status(409).body("You have already rated this recipe");
        }

        return ResponseEntity.ok(updated);
    }
}