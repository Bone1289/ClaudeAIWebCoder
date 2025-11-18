package com.example.demo.adapter.in.graphql;

import com.example.demo.adapter.in.graphql.dto.*;
import com.example.demo.application.ports.in.GetCurrentUserUseCase;
import com.example.demo.application.ports.in.LoginUseCase;
import com.example.demo.application.ports.in.RegisterUserUseCase;
import com.example.demo.application.service.AuditService;
import com.example.demo.application.service.NotificationService;
import com.example.demo.config.security.JwtUtil;
import com.example.demo.config.security.SecurityUtil;
import com.example.demo.domain.AuditLog;
import com.example.demo.domain.User;
import com.example.demo.domain.notification.Notification;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.UUID;

/**
 * GraphQL Resolver for Authentication operations
 */
@Controller
public class AuthResolver {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUseCase loginUseCase;
    private final GetCurrentUserUseCase getCurrentUserUseCase;
    private final JwtUtil jwtUtil;
    private final AuditService auditService;
    private final NotificationService notificationService;

    public AuthResolver(RegisterUserUseCase registerUserUseCase,
                       LoginUseCase loginUseCase,
                       GetCurrentUserUseCase getCurrentUserUseCase,
                       JwtUtil jwtUtil,
                       AuditService auditService,
                       NotificationService notificationService) {
        this.registerUserUseCase = registerUserUseCase;
        this.loginUseCase = loginUseCase;
        this.getCurrentUserUseCase = getCurrentUserUseCase;
        this.jwtUtil = jwtUtil;
        this.auditService = auditService;
        this.notificationService = notificationService;
    }

    /**
     * Query: me
     * Get the currently authenticated user
     */
    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public UserDTO me() {
        UUID userId = SecurityUtil.getCurrentUserId();
        User user = getCurrentUserUseCase.getCurrentUser(userId);
        return UserDTO.fromDomain(user);
    }

    /**
     * Mutation: signUp
     * Register a new user
     */
    @MutationMapping
    public AuthResponseDTO signUp(@Argument SignUpInputDTO input) {
        try {
            User user = registerUserUseCase.registerUser(
                    input.email(),
                    input.username(),
                    input.password(),
                    input.firstName(),
                    input.lastName()
            );

            // Generate JWT token
            String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name());

            // Audit log successful registration
            auditService.logSuccess(
                    AuditLog.AuditAction.USER_REGISTERED,
                    user.getId().getMostSignificantBits(),
                    user.getUsername(),
                    "User",
                    user.getId().toString(),
                    String.format("User registered via GraphQL: email=%s, username=%s", user.getEmail(), user.getUsername()),
                    null
            );

            return new AuthResponseDTO(token, UserDTO.fromDomain(user));
        } catch (IllegalArgumentException e) {
            // Audit log failed registration
            auditService.logFailure(
                    AuditLog.AuditAction.USER_REGISTERED,
                    null,
                    input.username(),
                    "User",
                    null,
                    String.format("Registration attempt via GraphQL: email=%s, username=%s", input.email(), input.username()),
                    e.getMessage(),
                    null
            );
            throw new RuntimeException("Registration failed: " + e.getMessage(), e);
        }
    }

    /**
     * Mutation: login
     * Authenticate user and return JWT token
     */
    @MutationMapping
    public AuthResponseDTO login(@Argument LoginInputDTO input) {
        try {
            User user = loginUseCase.login(
                    input.username(),
                    input.password()
            );

            // Generate JWT token
            String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name());

            // Audit log successful login
            auditService.logSuccess(
                    AuditLog.AuditAction.LOGIN,
                    user.getId().getMostSignificantBits(),
                    user.getUsername(),
                    "User",
                    user.getId().toString(),
                    String.format("User logged in via GraphQL: username=%s, role=%s", user.getUsername(), user.getRole()),
                    null
            );

            // Send notification for successful login
            try {
                notificationService.createAndSendAsync(
                        user.getId(),
                        Notification.NotificationType.SECURITY_ALERT,
                        Notification.NotificationChannel.IN_APP,
                        "New Login Detected",
                        "Your account was accessed via GraphQL API",
                        Notification.NotificationPriority.MEDIUM
                );
            } catch (Exception e) {
                // Don't fail login if notification fails
                e.printStackTrace();
            }

            return new AuthResponseDTO(token, UserDTO.fromDomain(user));
        } catch (IllegalArgumentException | IllegalStateException e) {
            // Audit log failed login
            auditService.logFailure(
                    AuditLog.AuditAction.LOGIN,
                    null,
                    input.username(),
                    "User",
                    null,
                    String.format("Login attempt via GraphQL: username=%s", input.username()),
                    e.getMessage(),
                    null
            );
            throw new RuntimeException("Login failed: " + e.getMessage(), e);
        }
    }

    /**
     * Mutation: logout
     * Logout current user (for audit logging purposes)
     */
    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public Boolean logout() {
        try {
            UUID userId = SecurityUtil.getCurrentUserId();
            User user = getCurrentUserUseCase.getCurrentUser(userId);

            // Audit log successful logout
            auditService.logSuccess(
                    AuditLog.AuditAction.LOGOUT,
                    user.getId().getMostSignificantBits(),
                    user.getUsername(),
                    "User",
                    user.getId().toString(),
                    String.format("User logged out via GraphQL: username=%s", user.getUsername()),
                    null
            );

            return true;
        } catch (Exception e) {
            throw new RuntimeException("Logout failed: " + e.getMessage(), e);
        }
    }

    /**
     * Query: hello
     * Simple hello endpoint for testing
     */
    @QueryMapping
    public String hello(@Argument String name) {
        return "Hello " + (name != null ? name : "World") + "!";
    }

    /**
     * Query: health
     * Health check endpoint
     */
    @QueryMapping
    public HealthCheckDTO health() {
        return new HealthCheckDTO("UP", java.time.LocalDateTime.now());
    }
}
