package com.example.demo.domain;

import java.time.LocalDateTime;

public class AuditLog {
    private final Long id;
    private final Long userId;
    private final String username;
    private final AuditAction action;
    private final String entityType;
    private final String entityId;
    private final String details;
    private final String ipAddress;
    private final String userAgent;
    private final AuditStatus status;
    private final String failureReason;
    private final LocalDateTime timestamp;

    public enum AuditAction {
        LOGIN,
        LOGOUT,
        ACCOUNT_CREATED,
        ACCOUNT_CLOSED,
        TRANSACTION_DEPOSIT,
        TRANSACTION_WITHDRAWAL,
        TRANSACTION_TRANSFER,
        USER_REGISTERED,
        PASSWORD_CHANGED,
        PROFILE_UPDATED
    }

    public enum AuditStatus {
        SUCCESS,
        FAILURE
    }

    private AuditLog(Builder builder) {
        this.id = builder.id;
        this.userId = builder.userId;
        this.username = builder.username;
        this.action = builder.action;
        this.entityType = builder.entityType;
        this.entityId = builder.entityId;
        this.details = builder.details;
        this.ipAddress = builder.ipAddress;
        this.userAgent = builder.userAgent;
        this.status = builder.status;
        this.failureReason = builder.failureReason;
        this.timestamp = builder.timestamp != null ? builder.timestamp : LocalDateTime.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Long userId;
        private String username;
        private AuditAction action;
        private String entityType;
        private String entityId;
        private String details;
        private String ipAddress;
        private String userAgent;
        private AuditStatus status = AuditStatus.SUCCESS;
        private String failureReason;
        private LocalDateTime timestamp;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder action(AuditAction action) {
            this.action = action;
            return this;
        }

        public Builder entityType(String entityType) {
            this.entityType = entityType;
            return this;
        }

        public Builder entityId(String entityId) {
            this.entityId = entityId;
            return this;
        }

        public Builder details(String details) {
            this.details = details;
            return this;
        }

        public Builder ipAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        public Builder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public Builder status(AuditStatus status) {
            this.status = status;
            return this;
        }

        public Builder failureReason(String failureReason) {
            this.failureReason = failureReason;
            return this;
        }

        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public AuditLog build() {
            if (action == null) {
                throw new IllegalArgumentException("Action is required for audit log");
            }
            return new AuditLog(this);
        }
    }

    // Getters
    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public AuditAction getAction() {
        return action;
    }

    public String getEntityType() {
        return entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public String getDetails() {
        return details;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public AuditStatus getStatus() {
        return status;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
