package com.example.demo.config.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

/**
 * Security utility class for extracting current user information from SecurityContext
 */
public class SecurityUtil {

    /**
     * Get the current authenticated user's ID from the SecurityContext
     * @return UUID of the current user
     * @throws IllegalStateException if user is not authenticated
     */
    public static UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == null) {
            throw new IllegalStateException("No authenticated user found");
        }

        // The principal is the user ID as a string (set in JwtAuthenticationFilter)
        String userIdString = authentication.getPrincipal().toString();

        try {
            return UUID.fromString(userIdString);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Invalid user ID format in authentication", e);
        }
    }

    /**
     * Check if there is a currently authenticated user
     * @return true if user is authenticated, false otherwise
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() != null;
    }
}
