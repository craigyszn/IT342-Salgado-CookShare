package edu.cit.salgado.cookshare.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.cit.salgado.cookshare.entity.NutritionData;

public interface NutritionRepository extends JpaRepository<NutritionData, Long> {
    Optional<NutritionData> findByRecipeId(String recipeId);
}