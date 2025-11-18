package com.example.demo.adapter.in.grpc.auth;

import com.example.demo.application.service.AuditService;
import com.example.demo.application.service.AuthService;
import com.example.demo.config.security.JwtUtil;
import com.example.demo.config.security.SecurityUtil;
import com.example.demo.domain.AuditLog;
import com.example.demo.domain.User;
import com.example.demo.grpc.auth.*;
import com.example.demo.grpc.common.Empty;
import com.example.demo.grpc.common.UserResponse;
import com.google.protobuf.Timestamp;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * gRPC service adapter for authentication operations
 * Follows hexagonal architecture pattern - this is an input adapter
 */
@GrpcService
public class AuthGrpcService extends AuthServiceGrpc.AuthServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(AuthGrpcService.class);

    private final AuthService authService;
    private final AuditService auditService;
    private final JwtUtil jwtUtil;

    public AuthGrpcService(AuthService authService, AuditService auditService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.auditService = auditService;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void signUp(SignUpRequest request, StreamObserver<SignUpResponse> responseObserver) {
        try {
            logger.info("gRPC SignUp request for email: {}", request.getEmail());

            // Call the use case
            User user = authService.registerUser(
                    request.getEmail(),
                    request.getUsername(),
                    request.getPassword(),
                    request.getFirstName(),
                    request.getLastName()
            );

            // Log the registration
            AuditLog auditLog = AuditLog.builder()
                    .action(AuditLog.AuditAction.USER_REGISTERED)
                    .userId(user.getId().getMostSignificantBits())
                    .username(user.getUsername())
                    .entityType("User")
                    .entityId(user.getId().toString())
                    .details("User registered via gRPC: " + user.getEmail())
                    .status(AuditLog.AuditStatus.SUCCESS)
                    .build();
            auditService.logAsync(auditLog);

            // Build response
            SignUpResponse response = SignUpResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("User registered successfully")
                    .setUser(mapToUserResponse(user))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (IllegalArgumentException e) {
            logger.error("SignUp validation error: {}", e.getMessage());
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            logger.error("SignUp error", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Failed to register user: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void login(LoginRequest request, StreamObserver<LoginResponse> responseObserver) {
        try {
            logger.info("gRPC Login request for: {}", request.getEmailOrUsername());

            // Call the use case
            User user = authService.login(request.getEmailOrUsername(), request.getPassword());

            // Generate JWT token
            String token = jwtUtil.generateToken(user);

            // Log the login
            AuditLog loginAuditLog = AuditLog.builder()
                    .action(AuditLog.AuditAction.LOGIN)
                    .userId(user.getId().getMostSignificantBits())
                    .username(user.getUsername())
                    .entityType("User")
                    .entityId(user.getId().toString())
                    .details("User logged in via gRPC: " + user.getEmail())
                    .status(AuditLog.AuditStatus.SUCCESS)
                    .build();
            auditService.logAsync(loginAuditLog);

            // Build response
            LoginResponse response = LoginResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Login successful")
                    .setToken(token)
                    .setUser(mapToUserResponse(user))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.error("Login validation error: {}", e.getMessage());
            responseObserver.onError(io.grpc.Status.UNAUTHENTICATED
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            logger.error("Login error", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Failed to login: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void getMe(Empty request, StreamObserver<GetMeResponse> responseObserver) {
        try {
            // Get current user from security context
            User user = SecurityUtil.getCurrentUser();
            logger.info("gRPC GetMe request for user: {}", user.getId());

            // Build response
            GetMeResponse response = GetMeResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("User retrieved successfully")
                    .setUser(mapToUserResponse(user))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (IllegalStateException e) {
            logger.error("GetMe authentication error: {}", e.getMessage());
            responseObserver.onError(io.grpc.Status.UNAUTHENTICATED
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            logger.error("GetMe error", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Failed to get user: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void logout(Empty request, StreamObserver<LogoutResponse> responseObserver) {
        try {
            // Get current user from security context
            User user = SecurityUtil.getCurrentUser();
            logger.info("gRPC Logout request for user: {}", user.getId());

            // Log the logout
            AuditLog logoutAuditLog = AuditLog.builder()
                    .action(AuditLog.AuditAction.LOGOUT)
                    .userId(user.getId().getMostSignificantBits())
                    .username(user.getUsername())
                    .entityType("User")
                    .entityId(user.getId().toString())
                    .details("User logged out via gRPC: " + user.getEmail())
                    .status(AuditLog.AuditStatus.SUCCESS)
                    .build();
            auditService.logAsync(logoutAuditLog);

            // Build response
            LogoutResponse response = LogoutResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Logout successful")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (IllegalStateException e) {
            logger.error("Logout authentication error: {}", e.getMessage());
            responseObserver.onError(io.grpc.Status.UNAUTHENTICATED
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            logger.error("Logout error", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Failed to logout: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    /**
     * Maps domain User to gRPC UserResponse
     */
    private UserResponse mapToUserResponse(User user) {
        return UserResponse.newBuilder()
                .setId(user.getId().toString())
                .setEmail(user.getEmail())
                .setUsername(user.getUsername())
                .setFirstName(user.getFirstName())
                .setLastName(user.getLastName())
                .setRole(user.getRole().name())
                .setStatus(user.getStatus().name())
                .setCreatedAt(Timestamp.newBuilder()
                        .setSeconds(user.getCreatedAt().toEpochSecond(java.time.ZoneOffset.UTC))
                        .setNanos(user.getCreatedAt().getNano())
                        .build())
                .build();
    }
}
