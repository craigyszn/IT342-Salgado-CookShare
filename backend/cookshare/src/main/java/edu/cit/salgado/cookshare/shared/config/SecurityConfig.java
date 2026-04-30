package edu.cit.salgado.cookshare.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import edu.cit.salgado.cookshare.shared.security.JwtFilter;

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

            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(auth -> auth
                // ── Allow ALL OPTIONS preflight requests ──────────────────
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // ── Public endpoints ──────────────────────────────────────
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
                    "/login/**",
                    "/error"
                ).permitAll()

                // ── Admin only ────────────────────────────────────────────
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                // ── Everything else needs authentication ──────────────────
                .anyRequest().authenticated()
            )

            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)

            // ── Disable default form login & http basic ───────────────────
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())

            .oauth2Login(oauth2 -> oauth2
                .defaultSuccessUrl("/login-success", true)
                // ── Only trigger OAuth for non-API requests ───────────────
                .authorizationEndpoint(endpoint -> endpoint
                    .baseUri("/oauth2/authorization")
                )
            )

            // ── Disable OAuth2 redirect for API calls ─────────────────────
            .exceptionHandling(ex -> ex
                .defaultAuthenticationEntryPointFor(
                    (request, response, authException) -> {
                        response.setStatus(401);
                        response.getWriter().write("{\"error\": \"Unauthorized\"}");
                    },
                    new AntPathRequestMatcher("/api/**")
                )
            );

        return http.build();
    }
}