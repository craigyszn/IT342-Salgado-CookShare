package edu.cit.salgado.cookshare.service;

import java.util.List;

import org.springframework.stereotype.Service;

import edu.cit.salgado.cookshare.entity.Recipe;
import edu.cit.salgado.cookshare.repository.RecipeRepository;

@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;

    public RecipeService(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    public List<Recipe> getAllRecipes() {
        return recipeRepository.findAll();
    }

    public Recipe createRecipe(Recipe recipe) {
        return recipeRepository.save(recipe);
    }
}
