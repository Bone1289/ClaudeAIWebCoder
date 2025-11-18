package com.example.demo.config.grpc;

import com.example.demo.config.security.JwtUtil;
import com.example.demo.config.security.SecurityUtil;
import com.example.demo.domain.User;
import io.grpc.*;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * gRPC interceptor for JWT authentication
 * Intercepts all gRPC calls and validates JWT tokens
 * Replaces the REST JwtAuthenticationFilter functionality
 */
@Component
@GrpcGlobalServerInterceptor
public class JwtGrpcInterceptor implements ServerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(JwtGrpcInterceptor.class);

    private static final Metadata.Key<String> AUTHORIZATION_METADATA_KEY =
            Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER);

    private static final String BEARER_PREFIX = "Bearer ";

    // Methods that don't require authentication
    private static final String[] PUBLIC_METHODS = {
            "AuthService/SignUp",
            "AuthService/Login"
    };

    private final JwtUtil jwtUtil;

    public JwtGrpcInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        String methodName = call.getMethodDescriptor().getFullMethodName();
        logger.debug("gRPC method called: {}", methodName);

        // Check if method is public (doesn't require authentication)
        if (isPublicMethod(methodName)) {
            logger.debug("Public method, skipping authentication: {}", methodName);
            return next.startCall(call, headers);
        }

        // Extract authorization header
        String authHeader = headers.get(AUTHORIZATION_METADATA_KEY);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            logger.warn("Missing or invalid authorization header for method: {}", methodName);
            call.close(
                    Status.UNAUTHENTICATED.withDescription("Missing or invalid authorization header"),
                    new Metadata()
            );
            return new ServerCall.Listener<ReqT>() {};
        }

        // Extract and validate JWT token
        String token = authHeader.substring(BEARER_PREFIX.length());

        try {
            // Validate token and extract user
            if (!jwtUtil.validateToken(token)) {
                logger.warn("Invalid JWT token for method: {}", methodName);
                call.close(
                        Status.UNAUTHENTICATED.withDescription("Invalid or expired token"),
                        new Metadata()
                );
                return new ServerCall.Listener<ReqT>() {};
            }

            // Extract user from token
            User user = jwtUtil.getUserFromToken(token);

            // Set user in security context
            SecurityUtil.setCurrentUser(user);

            logger.debug("Authenticated user {} for method: {}", user.getUsername(), methodName);

            // Continue with the call
            return new AuthenticatedListener<>(next.startCall(call, headers), user);

        } catch (Exception e) {
            logger.error("Error validating token for method: {}", methodName, e);
            call.close(
                    Status.UNAUTHENTICATED.withDescription("Token validation failed: " + e.getMessage()),
                    new Metadata()
            );
            return new ServerCall.Listener<ReqT>() {};
        }
    }

    /**
     * Check if the method is public and doesn't require authentication
     */
    private boolean isPublicMethod(String methodName) {
        for (String publicMethod : PUBLIC_METHODS) {
            if (methodName.endsWith(publicMethod)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Custom listener that clears security context after call completes
     */
    private static class AuthenticatedListener<ReqT> extends ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT> {
        private final User user;

        protected AuthenticatedListener(ServerCall.Listener<ReqT> delegate, User user) {
            super(delegate);
            this.user = user;
        }

        @Override
        public void onComplete() {
            try {
                super.onComplete();
            } finally {
                // Clear security context after request completes
                SecurityUtil.clearCurrentUser();
            }
        }

        @Override
        public void onCancel() {
            try {
                super.onCancel();
            } finally {
                // Clear security context if request is cancelled
                SecurityUtil.clearCurrentUser();
            }
        }
    }
}
