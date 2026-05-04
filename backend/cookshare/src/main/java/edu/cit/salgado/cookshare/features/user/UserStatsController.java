package edu.cit.salgado.cookshare.features.user;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import edu.cit.salgado.cookshare.features.comment.CommentRepository;
import edu.cit.salgado.cookshare.features.favorite.FavoriteRepository;
import edu.cit.salgado.cookshare.features.recipe.RecipeRepository;

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

    // ── Get full profile ──────────────────────────────────────────────────────
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestParam String email) {
        try {
            User user = userService.getUserByEmail(email);
            if (user == null) return ResponseEntity.status(404).body("User not found");
            Map<String, String> profile = new HashMap<>();
            profile.put("email", user.getEmail() != null ? user.getEmail() : "");
            profile.put("firstName", user.getFirstname() != null ? user.getFirstname() : "");
            profile.put("lastName", user.getLastname() != null ? user.getLastname() : "");
            profile.put("bio", user.getBio() != null ? user.getBio() : "");
            profile.put("location", user.getLocation() != null ? user.getLocation() : "");
            profile.put("favoriteFood", user.getFavoriteFood() != null ? user.getFavoriteFood() : "");
            profile.put("profilePhotoUrl", user.getProfilePhotoUrl() != null ? user.getProfilePhotoUrl() : "");
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to get profile");
        }
    }

    // ── Update profile fields ─────────────────────────────────────────────────
    @PutMapping("/update-profile")
    public ResponseEntity<?> updateProfile(@RequestBody UpdateProfileRequest request) {
        try {
            User updated = userService.updateProfile(request);
            Map<String, String> response = new HashMap<>();
            response.put("email", updated.getEmail());
            response.put("bio", updated.getBio() != null ? updated.getBio() : "");
            response.put("location", updated.getLocation() != null ? updated.getLocation() : "");
            response.put("favoriteFood", updated.getFavoriteFood() != null ? updated.getFavoriteFood() : "");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to update profile: " + e.getMessage());
        }
    }
}