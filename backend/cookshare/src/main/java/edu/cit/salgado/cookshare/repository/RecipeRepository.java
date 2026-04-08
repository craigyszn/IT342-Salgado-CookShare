package edu.cit.salgado.cookshare.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.cit.salgado.cookshare.entity.Recipe;

public interface RecipeRepository extends JpaRepository<Recipe, String> {

    // Count recipes by user email
    long countByUserEmail(String userEmail);

    // Get all recipes by user email
    List<Recipe> findByUserEmail(String userEmail);
}