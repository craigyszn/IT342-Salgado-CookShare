package edu.cit.salgado.cookshare.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import edu.cit.salgado.cookshare.repository.CommentRepository;
import edu.cit.salgado.cookshare.repository.FavoriteRepository;
import edu.cit.salgado.cookshare.repository.RecipeRepository;
import edu.cit.salgado.cookshare.repository.UserRepository;
import edu.cit.salgado.cookshare.service.SupabaseStorageService;
import edu.cit.salgado.cookshare.service.UserService;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/users")
public class UserStatsController {

    private final RecipeRepository recipeRepository;
    private final FavoriteRepository favoriteRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final SupabaseStorageService supabaseStorageService;
    private final UserService userService;

    public UserStatsController(
            RecipeRepository recipeRepository,
            FavoriteRepository favoriteRepository,
            CommentRepository commentRepository,
            UserRepository userRepository,
            SupabaseStorageService supabaseStorageService,
            UserService userService) {
        this.recipeRepository = recipeRepository;
        this.favoriteRepository = favoriteRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.supabaseStorageService = supabaseStorageService;
        this.userService = userService;
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

    // ── Upload profile photo ──────────────────────────────────────────────────
    @PostMapping("/upload-profile-photo")
    public ResponseEntity<?> uploadProfilePhoto(
            @RequestParam("file") MultipartFile file,
            @RequestParam("email") String email) {
        try {
            String photoUrl = supabaseStorageService.uploadProfilePhoto(file);
            userService.updateProfilePhoto(email, photoUrl);
            return ResponseEntity.ok(Map.of("profilePhotoUrl", photoUrl));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Photo upload failed: " + e.getMessage());
        }
    }

    // ── Get profile photo ─────────────────────────────────────────────────────
    @GetMapping("/profile-photo")
    public ResponseEntity<?> getProfilePhoto(@RequestParam String email) {
        try {
            String photoUrl = userService.getUserByEmail(email).getProfilePhotoUrl();
            return ResponseEntity.ok(Map.of("profilePhotoUrl", photoUrl != null ? photoUrl : ""));
        } catch (Exception e) {
            return ResponseEntity.status(404).body("User not found");
        }
    }
}