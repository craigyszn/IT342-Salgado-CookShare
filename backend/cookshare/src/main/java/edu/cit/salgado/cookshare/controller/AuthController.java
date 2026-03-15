package edu.cit.salgado.cookshare.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.cit.salgado.cookshare.dto.LoginRequest;
import edu.cit.salgado.cookshare.dto.LoginResponse;
import edu.cit.salgado.cookshare.dto.RegisterRequest;
import edu.cit.salgado.cookshare.service.AuthService;

@CrossOrigin(origins = "http://localhost:5173")   // ADD THIS LINE
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public String register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }

        @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }
}


