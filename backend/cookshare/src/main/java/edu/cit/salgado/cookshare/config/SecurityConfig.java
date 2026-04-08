package edu.cit.salgado.cookshare.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import edu.cit.salgado.cookshare.security.JwtFilter;

@Configuration
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())

            // Stateless — no sessions, JWT handles auth
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(auth -> auth
                // Public endpoints — no token needed
                .requestMatchers(
                    "/api/auth/**",
                    "/api/recipes",
                    "/api/recipes/**",
                    "/api/comments",
                    "/api/comments/**",
                    "/api/favorites",
                    "/api/favorites/**",
                    "/api/users/**",
                    "/login-success",
                    "/oauth2/**",
                    "/login/**"
                ).permitAll()

                // Admin only
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                // Everything else needs authentication
                .anyRequest().authenticated()
            )

            // Add JWT filter before Spring's username/password filter
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)

            .oauth2Login(oauth2 -> oauth2
                .defaultSuccessUrl("/login-success", true)
            );

        return http.build();
    }
}