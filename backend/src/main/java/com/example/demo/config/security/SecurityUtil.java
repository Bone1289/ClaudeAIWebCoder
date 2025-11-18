package com.example.demo.config.security;

import com.example.demo.domain.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

/**
 * Security utility class for extracting current user information from SecurityContext
 * Supports both REST (Spring Security) and gRPC (ThreadLocal) authentication
 */
public class SecurityUtil {

    // ThreadLocal storage for gRPC user context
    private static final ThreadLocal<User> currentUser = new ThreadLocal<>();

    /**
     * Get the current authenticated user's ID from the SecurityContext
     * @return UUID of the current user
     * @throws IllegalStateException if user is not authenticated
     */
    public static UUID getCurrentUserId() {
        // Check ThreadLocal first (for gRPC)
        User user = currentUser.get();
        if (user != null) {
            return user.getId();
        }

        // Fall back to Spring Security context (for REST)
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
     * Get the current authenticated user (for gRPC)
     * @return User object
     * @throws IllegalStateException if user is not authenticated
     */
    public static User getCurrentUser() {
        User user = currentUser.get();
        if (user == null) {
            throw new IllegalStateException("No authenticated user found in gRPC context");
        }
        return user;
    }

    /**
     * Set the current user in ThreadLocal (for gRPC interceptor)
     * @param user User object to set
     */
    public static void setCurrentUser(User user) {
        currentUser.set(user);
    }

    /**
     * Clear the current user from ThreadLocal (for gRPC interceptor cleanup)
     */
    public static void clearCurrentUser() {
        currentUser.remove();
    }

    /**
     * Check if there is a currently authenticated user
     * @return true if user is authenticated, false otherwise
     */
    public static boolean isAuthenticated() {
        // Check ThreadLocal first
        if (currentUser.get() != null) {
            return true;
        }

        // Fall back to Spring Security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() != null;
    }
}
