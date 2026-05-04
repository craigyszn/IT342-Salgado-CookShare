package edu.cit.salgado.cookshare.features.recipe;

import java.util.List;

import org.springframework.stereotype.Service;

import edu.cit.salgado.cookshare.features.rating.Rating;
import edu.cit.salgado.cookshare.features.rating.RatingRepository;

@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final RatingRepository ratingRepository;

    public RecipeService(
            RecipeRepository recipeRepository,
            RatingRepository ratingRepository) {
        this.recipeRepository = recipeRepository;
        this.ratingRepository = ratingRepository;
    }

    public List<Recipe> getAllRecipes() {
        return recipeRepository.findAll();
    }

    public Recipe createRecipe(Recipe recipe) {
        return recipeRepository.save(recipe);
    }

    public List<Recipe> getRecipesByUser(String email) {
        return recipeRepository.findByUserEmail(email);
    }

    public void deleteRecipe(String id) {
        recipeRepository.deleteById(id);
    }

    // ── Rate recipe — returns null if already rated or not found ──────────────

    public Recipe rateRecipe(String id, int stars, String userEmail) {
        // Block duplicate ratings from the same user
        if (ratingRepository.existsByUserEmailAndRecipeId(userEmail, id)) {
            return null; // caller will return 409 Conflict
        }

        Recipe recipe = recipeRepository.findById(id).orElse(null);
        if (recipe == null) return null;

        // Save individual rating record for tracking
        ratingRepository.save(new Rating(userEmail, id, stars));

        // Recalculate running average
        int newTotal  = recipe.getTotalRatingScore() + stars;
        int newCount  = recipe.getReviewCount() + 1;
        double newAvg = Math.round((double) newTotal / newCount * 10.0) / 10.0;

        recipe.setTotalRatingScore(newTotal);
        recipe.setReviewCount(newCount);
        recipe.setRating(newAvg);

        return recipeRepository.save(recipe);
    }
}