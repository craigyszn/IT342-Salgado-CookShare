package edu.cit.salgado.cookshare.service;

import edu.cit.salgado.cookshare.dto.LoginRequest;
import edu.cit.salgado.cookshare.dto.RegisterRequest;
import edu.cit.salgado.cookshare.entity.User;
import edu.cit.salgado.cookshare.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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

        user.setFirstname(request.getFirstname());
        user.setLastname(request.getLastname());
        user.setEmail(request.getEmail());

        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        user.setCreatedAt(LocalDateTime.now());

        userRepository.save(user);

        return "User registered successfully";
    }

    public String login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail()).orElse(null);

        if (user == null) {
            return "Invalid credentials";
        }

        if (passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            return "Login successful";
        }

        return "Invalid credentials";
    }
}