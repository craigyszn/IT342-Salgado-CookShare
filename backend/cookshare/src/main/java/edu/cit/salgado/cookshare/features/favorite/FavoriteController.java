package edu.cit.salgado.cookshare.features.favorite;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final FavoriteRepository favoriteRepository;

    public FavoriteController(FavoriteRepository favoriteRepository) {
        this.favoriteRepository = favoriteRepository;
    }

    // Get all favorites for a user
    @GetMapping
    public List<Favorite> getFavorites(@RequestParam String email) {
        return favoriteRepository.findByUserEmail(email);
    }

    // Check if a recipe is favorited by user
    @GetMapping("/check")
    public ResponseEntity<Map<String, Boolean>> checkFavorite(
            @RequestParam String email,
            @RequestParam String recipeId) {
        boolean isFavorited = favoriteRepository.existsByUserEmailAndRecipeId(email, recipeId);
        return ResponseEntity.ok(Map.of("favorited", isFavorited));
    }

    // Add a favorite
    @PostMapping
    public ResponseEntity<Favorite> addFavorite(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String recipeId = body.get("recipeId");
        String recipeTitle = body.get("recipeTitle");
        String recipeImage = body.get("recipeImage");

        if (favoriteRepository.existsByUserEmailAndRecipeId(email, recipeId)) {
            return ResponseEntity.ok(favoriteRepository.findByUserEmailAndRecipeId(email, recipeId).get());
        }

        Favorite favorite = new Favorite();
        favorite.setUserEmail(email);
        favorite.setRecipeId(recipeId);
        favorite.setRecipeTitle(recipeTitle);
        favorite.setRecipeImage(recipeImage);

        return ResponseEntity.ok(favoriteRepository.save(favorite));
    }

    // Remove a favorite
    @DeleteMapping
    @Transactional
    public ResponseEntity<String> removeFavorite(
            @RequestParam String email,
            @RequestParam String recipeId) {
        favoriteRepository.deleteByUserEmailAndRecipeId(email, recipeId);
        return ResponseEntity.ok("Removed from favorites");
    }
}