package edu.cit.salgado.cookshare.features.auth;

import java.time.LocalDateTime;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import edu.cit.salgado.cookshare.features.user.User;
import edu.cit.salgado.cookshare.features.user.UserRepository;
import edu.cit.salgado.cookshare.shared.security.JwtUtil;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtUtil = jwtUtil;
    }

    // ── Register ──────────────────────────────────────────────────────────────

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
        user.setRole("USER");

        userRepository.save(user);
        return "User registered successfully";
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            return new LoginResponse("Invalid credentials", null, null, null, null, null, null);
        }

        return buildLoginResponse(user);
    }

    // ── Refresh Token ─────────────────────────────────────────────────────────

    public LoginResponse refresh(String refreshTokenStr) {
        RefreshToken stored = refreshTokenRepository.findByToken(refreshTokenStr).orElse(null);

        if (stored == null || stored.isRevoked() || stored.isExpired()) {
            return new LoginResponse("Invalid or expired refresh token", null, null, null, null, null, null);
        }

        User user = stored.getUser();

        // Revoke old refresh token and issue new one (rotation)
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        return buildLoginResponse(user);
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    public String logout(String refreshTokenStr) {
        RefreshToken stored = refreshTokenRepository.findByToken(refreshTokenStr).orElse(null);
        if (stored != null) {
            stored.setRevoked(true);
            refreshTokenRepository.save(stored);
        }
        return "Logged out successfully";
    }

    // ── Helper: build full LoginResponse with tokens ──────────────────────────

    public LoginResponse buildLoginResponse(User user) {
        String accessToken  = jwtUtil.generateAccessToken(user.getEmail(), user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        // Save refresh token to DB
        RefreshToken rt = new RefreshToken(
            user,
            refreshToken,
            LocalDateTime.now().plusDays(7)
        );
        refreshTokenRepository.save(rt);

        return new LoginResponse(
            "Login successful",
            user.getFirstname(),
            user.getLastname(),
            user.getEmail(),
            user.getRole(),
            accessToken,
            refreshToken
        );
    }
}