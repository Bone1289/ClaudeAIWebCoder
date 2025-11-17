package com.example.demo.adapter.in.web.auth;

import com.example.demo.adapter.in.web.auth.dto.LoginRequest;
import com.example.demo.adapter.in.web.auth.dto.LoginResponse;
import com.example.demo.adapter.in.web.auth.dto.SignUpRequest;
import com.example.demo.adapter.in.web.auth.dto.UserResponse;
import com.example.demo.adapter.in.web.dto.ApiResponse;
import com.example.demo.application.ports.in.GetCurrentUserUseCase;
import com.example.demo.application.ports.in.LoginUseCase;
import com.example.demo.application.ports.in.RegisterUserUseCase;
import com.example.demo.application.service.AuditService;
import com.example.demo.config.security.JwtUtil;
import com.example.demo.domain.AuditLog;
import com.example.demo.domain.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication REST Controller
 * Handles user registration, login, and profile operations
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUseCase loginUseCase;
    private final GetCurrentUserUseCase getCurrentUserUseCase;
    private final JwtUtil jwtUtil;
    private final AuditService auditService;

    public AuthController(RegisterUserUseCase registerUserUseCase,
                         LoginUseCase loginUseCase,
                         GetCurrentUserUseCase getCurrentUserUseCase,
                         JwtUtil jwtUtil,
                         AuditService auditService) {
        this.registerUserUseCase = registerUserUseCase;
        this.loginUseCase = loginUseCase;
        this.getCurrentUserUseCase = getCurrentUserUseCase;
        this.jwtUtil = jwtUtil;
        this.auditService = auditService;
    }

    /**
     * POST /api/auth/signup - Register a new user
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserResponse>> signUp(@Valid @RequestBody SignUpRequest request,
                                                             HttpServletRequest httpRequest) {
        try {
            User user = registerUserUseCase.registerUser(
                    request.getEmail(),
                    request.getUsername(),
                    request.getPassword(),
                    request.getFirstName(),
                    request.getLastName()
            );

            // Audit log successful registration
            auditService.logSuccess(
                    AuditLog.AuditAction.USER_REGISTERED,
                    user.getId().getMostSignificantBits(),
                    user.getUsername(),
                    "User",
                    user.getId().toString(),
                    String.format("User registered: email=%s, username=%s", user.getEmail(), user.getUsername()),
                    httpRequest
            );

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("User registered successfully", UserResponse.fromDomain(user)));
        } catch (IllegalArgumentException e) {
            // Audit log failed registration
            auditService.logFailure(
                    AuditLog.AuditAction.USER_REGISTERED,
                    null,
                    request.getUsername(),
                    "User",
                    null,
                    String.format("Registration attempt: email=%s, username=%s", request.getEmail(), request.getUsername()),
                    e.getMessage(),
                    httpRequest
            );

            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * POST /api/auth/login - Authenticate user and return JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request,
                                                             HttpServletRequest httpRequest) {
        try {
            User user = loginUseCase.login(
                    request.getEmailOrUsername(),
                    request.getPassword()
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
                    String.format("User logged in: username=%s, role=%s", user.getUsername(), user.getRole()),
                    httpRequest
            );

            // Create response
            LoginResponse loginResponse = new LoginResponse(token, UserResponse.fromDomain(user));

            return ResponseEntity.ok(ApiResponse.success("Login successful", loginResponse));
        } catch (IllegalArgumentException | IllegalStateException e) {
            // Audit log failed login
            auditService.logFailure(
                    AuditLog.AuditAction.LOGIN,
                    null,
                    request.getEmailOrUsername(),
                    "User",
                    null,
                    String.format("Login attempt: emailOrUsername=%s", request.getEmailOrUsername()),
                    e.getMessage(),
                    httpRequest
            );

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * GET /api/auth/me - Get current authenticated user
     * Note: This will require authentication after SecurityConfig is set up
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("No authentication token provided"));
            }

            String token = authHeader.substring(7);
            if (!jwtUtil.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Invalid or expired token"));
            }

            var userId = jwtUtil.getUserIdFromToken(token);
            User user = getCurrentUserUseCase.getCurrentUser(userId);

            return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", UserResponse.fromDomain(user)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid authentication token"));
        }
    }

    /**
     * POST /api/auth/logout - Logout current user
     * Note: Since we use stateless JWT, this is primarily for audit logging
     * Actual token invalidation should be handled client-side by discarding the token
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                                     HttpServletRequest httpRequest) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("No authentication token provided"));
            }

            String token = authHeader.substring(7);
            if (!jwtUtil.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Invalid or expired token"));
            }

            var userId = jwtUtil.getUserIdFromToken(token);
            User user = getCurrentUserUseCase.getCurrentUser(userId);

            // Audit log successful logout
            auditService.logSuccess(
                    AuditLog.AuditAction.LOGOUT,
                    user.getId().getMostSignificantBits(),
                    user.getUsername(),
                    "User",
                    user.getId().toString(),
                    String.format("User logged out: username=%s", user.getUsername()),
                    httpRequest
            );

            return ResponseEntity.ok(ApiResponse.success("Logout successful", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid authentication token"));
        }
    }
}
