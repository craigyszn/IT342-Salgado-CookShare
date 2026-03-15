package edu.cit.salgado.cookshare.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.cit.salgado.cookshare.entity.Recipe;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {
}
