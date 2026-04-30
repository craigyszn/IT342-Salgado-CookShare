package edu.cit.salgado.cookshare.features.nutrition;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NutritionRepository extends JpaRepository<NutritionData, Long> {
    Optional<NutritionData> findByRecipeId(String recipeId);
}