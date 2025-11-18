package com.example.demo.config.grpc;

import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;

/**
 * Global exception handler for gRPC services
 * Converts exceptions to appropriate gRPC status codes
 */
@GrpcAdvice
public class GrpcExceptionAdvice {

    private static final Logger logger = LoggerFactory.getLogger(GrpcExceptionAdvice.class);

    /**
     * Handle IllegalArgumentException - maps to INVALID_ARGUMENT
     */
    @GrpcExceptionHandler(IllegalArgumentException.class)
    public StatusRuntimeException handleIllegalArgument(IllegalArgumentException e) {
        logger.error("Invalid argument: {}", e.getMessage());
        return Status.INVALID_ARGUMENT
                .withDescription(e.getMessage())
                .withCause(e)
                .asRuntimeException();
    }

    /**
     * Handle IllegalStateException - maps to FAILED_PRECONDITION
     */
    @GrpcExceptionHandler(IllegalStateException.class)
    public StatusRuntimeException handleIllegalState(IllegalStateException e) {
        logger.error("Illegal state: {}", e.getMessage());
        return Status.FAILED_PRECONDITION
                .withDescription(e.getMessage())
                .withCause(e)
                .asRuntimeException();
    }

    /**
     * Handle AccessDeniedException - maps to PERMISSION_DENIED
     */
    @GrpcExceptionHandler(AccessDeniedException.class)
    public StatusRuntimeException handleAccessDenied(AccessDeniedException e) {
        logger.error("Access denied: {}", e.getMessage());
        return Status.PERMISSION_DENIED
                .withDescription(e.getMessage())
                .withCause(e)
                .asRuntimeException();
    }

    /**
     * Handle SecurityException - maps to UNAUTHENTICATED
     */
    @GrpcExceptionHandler(SecurityException.class)
    public StatusRuntimeException handleSecurityException(SecurityException e) {
        logger.error("Security error: {}", e.getMessage());
        return Status.UNAUTHENTICATED
                .withDescription(e.getMessage())
                .withCause(e)
                .asRuntimeException();
    }

    /**
     * Handle NullPointerException - maps to INTERNAL
     */
    @GrpcExceptionHandler(NullPointerException.class)
    public StatusRuntimeException handleNullPointer(NullPointerException e) {
        logger.error("Null pointer exception", e);
        return Status.INTERNAL
                .withDescription("An internal error occurred")
                .withCause(e)
                .asRuntimeException();
    }

    /**
     * Handle generic exceptions - maps to INTERNAL
     */
    @GrpcExceptionHandler(Exception.class)
    public StatusRuntimeException handleGenericException(Exception e) {
        logger.error("Unhandled exception", e);
        return Status.INTERNAL
                .withDescription("An unexpected error occurred: " + e.getMessage())
                .withCause(e)
                .asRuntimeException();
    }

    /**
     * Handle StatusException - pass through
     */
    @GrpcExceptionHandler(StatusException.class)
    public StatusRuntimeException handleStatusException(StatusException e) {
        logger.error("gRPC status exception: {}", e.getMessage());
        return e.getStatus()
                .withDescription(e.getMessage())
                .asRuntimeException(e.getTrailers());
    }

    /**
     * Handle StatusRuntimeException - pass through
     */
    @GrpcExceptionHandler(StatusRuntimeException.class)
    public StatusRuntimeException handleStatusRuntimeException(StatusRuntimeException e) {
        logger.error("gRPC status runtime exception: {}", e.getMessage());
        return e;
    }
}
