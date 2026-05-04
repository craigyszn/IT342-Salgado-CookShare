package edu.cit.salgado.cookshare.features.admin;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.cit.salgado.cookshare.features.recipe.RecipeRepository;
import edu.cit.salgado.cookshare.features.user.User;
import edu.cit.salgado.cookshare.features.user.UserRepository;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final RecipeRepository recipeRepository;

    public AdminController(UserRepository userRepository, RecipeRepository recipeRepository) {
        this.userRepository = userRepository;
        this.recipeRepository = recipeRepository;
    }

    private boolean isAdmin(String email) {
        return userRepository.findByEmail(email)
            .map(u -> "ADMIN".equals(u.getRole()))
            .orElse(false);
    }

    // ── Users ─────────────────────────────────────────────────────────────────

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(@RequestHeader("X-User-Email") String email) {
        if (!isAdmin(email)) return ResponseEntity.status(403).body("Access denied");
        return ResponseEntity.ok(userRepository.findAll());
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(
            @RequestHeader("X-User-Email") String email,
            @PathVariable Long id,
            @RequestBody Map<String, String> updates) {

        if (!isAdmin(email)) return ResponseEntity.status(403).body("Access denied");
        User user = userRepository.findById(id).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();

        if (updates.containsKey("firstname")) user.setFirstname(updates.get("firstname"));
        if (updates.containsKey("lastname"))  user.setLastname(updates.get("lastname"));
        if (updates.containsKey("email"))     user.setEmail(updates.get("email"));
        if (updates.containsKey("role"))      user.setRole(updates.get("role"));

        userRepository.save(user);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(
            @RequestHeader("X-User-Email") String email,
            @PathVariable Long id) {

        if (!isAdmin(email)) return ResponseEntity.status(403).body("Access denied");
        if (!userRepository.existsById(id)) return ResponseEntity.notFound().build();
        userRepository.deleteById(id);
        return ResponseEntity.ok("User deleted");
    }

    @PostMapping("/users/{id}/promote")
    public ResponseEntity<?> promoteUser(
            @RequestHeader("X-User-Email") String email,
            @PathVariable Long id) {

        if (!isAdmin(email)) return ResponseEntity.status(403).body("Access denied");
        User user = userRepository.findById(id).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();
        user.setRole("ADMIN");
        userRepository.save(user);
        return ResponseEntity.ok("User promoted to ADMIN");
    }

    @PostMapping("/users/{id}/demote")
    public ResponseEntity<?> demoteUser(
            @RequestHeader("X-User-Email") String email,
            @PathVariable Long id) {

        if (!isAdmin(email)) return ResponseEntity.status(403).body("Access denied");
        User user = userRepository.findById(id).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();
        user.setRole("USER");
        userRepository.save(user);
        return ResponseEntity.ok("User demoted to USER");
    }

    // ── Recipes — ID is String ────────────────────────────────────────────────

    @GetMapping("/recipes")
    public ResponseEntity<?> getAllRecipes(@RequestHeader("X-User-Email") String email) {
        if (!isAdmin(email)) return ResponseEntity.status(403).body("Access denied");
        return ResponseEntity.ok(recipeRepository.findAll());
    }

    @DeleteMapping("/recipes/{id}")
    public ResponseEntity<?> deleteRecipe(
            @RequestHeader("X-User-Email") String email,
            @PathVariable String id) {

        if (!isAdmin(email)) return ResponseEntity.status(403).body("Access denied");
        if (!recipeRepository.existsById(id)) return ResponseEntity.notFound().build();
        recipeRepository.deleteById(id);
        return ResponseEntity.ok("Recipe deleted");
    }
}