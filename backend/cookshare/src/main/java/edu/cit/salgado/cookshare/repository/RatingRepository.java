package edu.cit.salgado.cookshare.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.cit.salgado.cookshare.entity.Rating;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    boolean existsByUserEmailAndRecipeId(String userEmail, String recipeId);
}