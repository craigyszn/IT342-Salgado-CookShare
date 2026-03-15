package edu.cit.salgado.cookshare.service;

import java.time.LocalDateTime;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import edu.cit.salgado.cookshare.dto.LoginRequest;
import edu.cit.salgado.cookshare.dto.LoginResponse;
import edu.cit.salgado.cookshare.dto.RegisterRequest;
import edu.cit.salgado.cookshare.entity.User;
import edu.cit.salgado.cookshare.repository.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String register(RegisterRequest request) {

    if (userRepository.existsByEmail(request.getEmail())) {
        return "Email already registered";
    }

        User user = new User();

        user.setFirstname(request.getFirstName());
        user.setLastname(request.getLastName());
        user.setEmail(request.getEmail());

        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        user.setCreatedAt(LocalDateTime.now());

        userRepository.save(user);

        return "User registered successfully";
    }

    
    public LoginResponse login(LoginRequest request) {
    User user = userRepository.findByEmail(request.getEmail()).orElse(null);

    if (user == null) {
        return new LoginResponse("Invalid credentials", null, null, null);
    }

    if (passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
        return new LoginResponse("Login successful", user.getFirstname(), user.getLastname(), user.getEmail());
    }

    return new LoginResponse("Invalid credentials", null, null, null);
    }
}