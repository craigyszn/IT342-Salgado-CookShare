package edu.cit.salgado.cookshare.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.cit.salgado.cookshare.repository.CommentRepository;
import edu.cit.salgado.cookshare.repository.FavoriteRepository;
import edu.cit.salgado.cookshare.repository.RecipeRepository;
import edu.cit.salgado.cookshare.repository.UserRepository;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/users")
public class UserStatsController {

    private final RecipeRepository recipeRepository;
    private final FavoriteRepository favoriteRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    public UserStatsController(
            RecipeRepository recipeRepository,
            FavoriteRepository favoriteRepository,
            CommentRepository commentRepository,
            UserRepository userRepository) {
        this.recipeRepository = recipeRepository;
        this.favoriteRepository = favoriteRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getUserStats(@RequestParam String email) {
        Map<String, Long> stats = new HashMap<>();
        stats.put("recipesShared", recipeRepository.countByUserEmail(email));
        stats.put("favorites", favoriteRepository.countByUserEmail(email));
        stats.put("comments", commentRepository.countByUserEmail(email));
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getUserCount() {
        Map<String, Long> result = new HashMap<>();
        result.put("count", userRepository.count());
        return ResponseEntity.ok(result);
    }
}