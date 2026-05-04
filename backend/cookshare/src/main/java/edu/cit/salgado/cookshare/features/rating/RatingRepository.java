package edu.cit.salgado.cookshare.features.rating;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    boolean existsByUserEmailAndRecipeId(String userEmail, String recipeId);

    Optional<Rating> findByUserEmailAndRecipeId(String userEmail, String recipeId);
}