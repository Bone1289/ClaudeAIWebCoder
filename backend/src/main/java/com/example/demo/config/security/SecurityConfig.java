package com.example.demo.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security configuration
 * Configures JWT-based authentication and authorization
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Password encoder bean for hashing passwords
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Security filter chain configuration
     * For now, we allow all requests to support initial testing
     * In production, you should protect endpoints appropriately
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF for stateless JWT authentication
                .csrf(AbstractHttpConfigurer::disable)

                // Configure authorization
                .authorizeHttpRequests(auth -> auth
                        // Allow public access to auth endpoints
                        .requestMatchers("/api/auth/**").permitAll()

                        // Allow public access to actuator endpoints
                        .requestMatchers("/actuator/**").permitAll()

                        // For initial testing, allow all banking endpoints
                        // TODO: In production, require authentication for these
                        .requestMatchers("/api/**").permitAll()

                        // All other requests require authentication
                        .anyRequest().authenticated()
                )

                // Stateless session management (JWT doesn't need sessions)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        return http.build();
    }
}
