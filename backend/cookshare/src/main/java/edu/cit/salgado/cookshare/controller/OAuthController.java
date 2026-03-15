package edu.cit.salgado.cookshare.controller;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;

@RestController
public class OAuthController {

    @GetMapping("/login-success")
    public void loginSuccess(Authentication authentication, HttpServletResponse response) throws IOException {

        OAuth2User user = (OAuth2User) authentication.getPrincipal();
        String email = user.getAttribute("email");

        System.out.println("Google login successful: " + email);

        // redirect to React dashboard
        response.sendRedirect("http://localhost:5173/dashboard");
    }
}