package edu.cit.salgado.cookshare.features.favorite;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    List<Favorite> findByUserEmail(String userEmail);

    Optional<Favorite> findByUserEmailAndRecipeId(String userEmail, String recipeId);

    boolean existsByUserEmailAndRecipeId(String userEmail, String recipeId);

    long countByUserEmail(String userEmail);

    void deleteByUserEmailAndRecipeId(String userEmail, String recipeId);
}