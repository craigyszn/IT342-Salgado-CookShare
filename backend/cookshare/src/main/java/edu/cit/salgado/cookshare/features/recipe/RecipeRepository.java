package edu.cit.salgado.cookshare.features.recipe;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeRepository extends JpaRepository<Recipe, String> {

    // Count recipes by user email
    long countByUserEmail(String userEmail);

    // Get all recipes by user email
    List<Recipe> findByUserEmail(String userEmail);
}