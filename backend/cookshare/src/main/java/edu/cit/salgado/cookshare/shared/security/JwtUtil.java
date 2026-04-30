package edu.cit.salgado.cookshare.shared.security;

import java.security.Key;
import java.util.Date;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    // ── Secret key — in production move this to application.properties ────────
    private static final String SECRET = "cookshare-super-secret-jwt-key-2024-must-be-long-enough";
    private static final Key KEY = Keys.hmacShaKeyFor(SECRET.getBytes());

    // Access token: 15 minutes
    private static final long ACCESS_TOKEN_EXPIRY = 15 * 60 * 1000L;

    // Refresh token: 7 days
    private static final long REFRESH_TOKEN_EXPIRY = 7 * 24 * 60 * 60 * 1000L;

    // ── Generate Access Token ─────────────────────────────────────────────────
    public String generateAccessToken(String email, String role) {
        return Jwts.builder()
            .setSubject(email)
            .claim("role", role)
            .claim("type", "access")
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRY))
            .signWith(KEY, SignatureAlgorithm.HS256)
            .compact();
    }

    // ── Generate Refresh Token ────────────────────────────────────────────────
    public String generateRefreshToken(String email) {
        return Jwts.builder()
            .setSubject(email)
            .claim("type", "refresh")
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRY))
            .signWith(KEY, SignatureAlgorithm.HS256)
            .compact();
    }

    // ── Extract email from token ──────────────────────────────────────────────
    public String extractEmail(String token) {
        return getClaims(token).getSubject();
    }

    // ── Extract role from token ───────────────────────────────────────────────
    public String extractRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    // ── Validate token ────────────────────────────────────────────────────────
    public boolean isValid(String token) {
        try {
            Claims claims = getClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    // ── Check if token is an access token ────────────────────────────────────
    public boolean isAccessToken(String token) {
        try {
            return "access".equals(getClaims(token).get("type", String.class));
        } catch (Exception e) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(KEY)
            .build()
            .parseClaimsJws(token)
            .getBody();
    }
}