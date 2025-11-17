package com.example.demo.adapter.out.persistence.entity;

import com.example.demo.domain.AuditLog;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_action", columnList = "action"),
    @Index(name = "idx_timestamp", columnList = "timestamp"),
    @Index(name = "idx_entity", columnList = "entity_type, entity_id")
})
public class AuditLogJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username", length = 100)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 50)
    private AuditLog.AuditAction action;

    @Column(name = "entity_type", length = 100)
    private String entityType;

    @Column(name = "entity_id", length = 100)
    private String entityId;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AuditLog.AuditStatus status;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @CreationTimestamp
    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp;

    // Constructors
    public AuditLogJpaEntity() {
    }

    public AuditLogJpaEntity(Long userId, String username, AuditLog.AuditAction action, String entityType,
                             String entityId, String details, String ipAddress, String userAgent,
                             AuditLog.AuditStatus status, String failureReason) {
        this.userId = userId;
        this.username = username;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.details = details;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.status = status;
        this.failureReason = failureReason;
    }

    // Convert from domain to JPA entity
    public static AuditLogJpaEntity fromDomain(AuditLog auditLog) {
        AuditLogJpaEntity entity = new AuditLogJpaEntity();
        entity.setId(auditLog.getId());
        entity.setUserId(auditLog.getUserId());
        entity.setUsername(auditLog.getUsername());
        entity.setAction(auditLog.getAction());
        entity.setEntityType(auditLog.getEntityType());
        entity.setEntityId(auditLog.getEntityId());
        entity.setDetails(auditLog.getDetails());
        entity.setIpAddress(auditLog.getIpAddress());
        entity.setUserAgent(auditLog.getUserAgent());
        entity.setStatus(auditLog.getStatus());
        entity.setFailureReason(auditLog.getFailureReason());
        if (auditLog.getTimestamp() != null) {
            entity.setTimestamp(auditLog.getTimestamp());
        }
        return entity;
    }

    // Convert from JPA entity to domain
    public AuditLog toDomain() {
        return AuditLog.builder()
                .id(this.id)
                .userId(this.userId)
                .username(this.username)
                .action(this.action)
                .entityType(this.entityType)
                .entityId(this.entityId)
                .details(this.details)
                .ipAddress(this.ipAddress)
                .userAgent(this.userAgent)
                .status(this.status)
                .failureReason(this.failureReason)
                .timestamp(this.timestamp)
                .build();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public AuditLog.AuditAction getAction() {
        return action;
    }

    public void setAction(AuditLog.AuditAction action) {
        this.action = action;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public AuditLog.AuditStatus getStatus() {
        return status;
    }

    public void setStatus(AuditLog.AuditStatus status) {
        this.status = status;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
