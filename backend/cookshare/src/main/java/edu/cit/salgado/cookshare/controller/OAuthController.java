package edu.cit.salgado.cookshare.controller;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.cit.salgado.cookshare.entity.User;
import edu.cit.salgado.cookshare.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;

@RestController
public class OAuthController {

    private final UserRepository userRepository;

    public OAuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/login-success")
    public void loginSuccess(Authentication authentication, HttpServletResponse response) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String firstName = oAuth2User.getAttribute("given_name");
        String lastName = oAuth2User.getAttribute("family_name");

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

        // Pass user data + role to frontend via URL params
        String redirectUrl = String.format(
            "http://localhost:5173/oauth-success?firstName=%s&lastName=%s&email=%s&role=%s",
            firstName != null ? firstName : "",
            lastName != null ? lastName : "",
            email != null ? email : "",
            role
        );

        response.sendRedirect(redirectUrl);
    }
}