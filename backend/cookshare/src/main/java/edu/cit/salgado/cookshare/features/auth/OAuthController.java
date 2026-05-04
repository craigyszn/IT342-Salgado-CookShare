package edu.cit.salgado.cookshare.features.auth;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.cit.salgado.cookshare.features.user.User;
import edu.cit.salgado.cookshare.features.user.UserRepository;
import edu.cit.salgado.cookshare.shared.security.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;

@RestController
public class OAuthController {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public OAuthController(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/login-success")
    public void loginSuccess(Authentication authentication, HttpServletResponse response) throws IOException {

        String frontendUrl = System.getenv("FRONTEND_URL") != null
            ? System.getenv("FRONTEND_URL")
            : "http://localhost:5173";

        // Guard: if authentication is null or not OAuth2, redirect to login
        if (authentication == null || !(authentication.getPrincipal() instanceof OAuth2User)) {
            response.sendRedirect(frontendUrl + "/login");
            return;
        }

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email     = oAuth2User.getAttribute("email");
        String firstName = oAuth2User.getAttribute("given_name");
        String lastName  = oAuth2User.getAttribute("family_name");

        // Save to DB if first time logging in with Google
        if (!userRepository.existsByEmail(email)) {
            User user = new User();
            user.setEmail(email);
            user.setFirstname(firstName);
            user.setLastname(lastName);
            user.setPasswordHash("");
            user.setCreatedAt(LocalDateTime.now());
            user.setRole("USER");
            userRepository.save(user);
        }

        // Get role from DB
        User user = userRepository.findByEmail(email).orElse(null);
        String role = (user != null) ? user.getRole() : "USER";

        // Generate JWT access token for OAuth user
        String accessToken = jwtUtil.generateAccessToken(email, role);

        // Pass user data + role + token to frontend via URL params
        String redirectUrl = String.format(
            "%s/oauth-success?firstName=%s&lastName=%s&email=%s&role=%s&accessToken=%s",
            frontendUrl,
            firstName != null ? firstName : "",
            lastName != null ? lastName : "",
            email != null ? email : "",
            role,
            accessToken
        );

        response.sendRedirect(redirectUrl);
    }
}