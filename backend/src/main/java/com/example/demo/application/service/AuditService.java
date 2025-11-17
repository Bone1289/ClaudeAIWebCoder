package com.example.demo.application.service;

import com.example.demo.adapter.out.persistence.JpaAuditLogRepository;
import com.example.demo.adapter.out.persistence.entity.AuditLogJpaEntity;
import com.example.demo.domain.AuditLog;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class AuditService {

    private static final Logger logger = LoggerFactory.getLogger(AuditService.class);

    private final JpaAuditLogRepository auditLogRepository;
    private final Map<String, Counter> auditCounters;

    public AuditService(JpaAuditLogRepository auditLogRepository, MeterRegistry meterRegistry) {
        this.auditLogRepository = auditLogRepository;
        this.auditCounters = new HashMap<>();

        // Initialize counters for each audit action
        for (AuditLog.AuditAction action : AuditLog.AuditAction.values()) {
            Counter successCounter = Counter.builder("audit.action")
                    .tag("action", action.name())
                    .tag("status", "success")
                    .description("Count of successful audit actions")
                    .register(meterRegistry);
            auditCounters.put(action.name() + "_SUCCESS", successCounter);

            Counter failureCounter = Counter.builder("audit.action")
                    .tag("action", action.name())
                    .tag("status", "failure")
                    .description("Count of failed audit actions")
                    .register(meterRegistry);
            auditCounters.put(action.name() + "_FAILURE", failureCounter);
        }
    }

    /**
     * Log an audit event asynchronously
     */
    @Async
    public void logAsync(AuditLog auditLog) {
        try {
            log(auditLog);
        } catch (Exception e) {
            logger.error("Failed to log audit event asynchronously: {}", e.getMessage(), e);
        }
    }

    /**
     * Log an audit event synchronously
     */
    public AuditLog log(AuditLog auditLog) {
        try {
            AuditLogJpaEntity entity = AuditLogJpaEntity.fromDomain(auditLog);
            AuditLogJpaEntity saved = auditLogRepository.save(entity);

            // Update metrics
            String counterKey = auditLog.getAction().name() + "_" + auditLog.getStatus().name();
            Counter counter = auditCounters.get(counterKey);
            if (counter != null) {
                counter.increment();
            }

            logger.info("Audit log recorded: action={}, userId={}, status={}, entityType={}, entityId={}",
                    auditLog.getAction(),
                    auditLog.getUserId(),
                    auditLog.getStatus(),
                    auditLog.getEntityType(),
                    auditLog.getEntityId());

            return saved.toDomain();
        } catch (Exception e) {
            logger.error("Failed to save audit log: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Log a successful action
     */
    public void logSuccess(AuditLog.AuditAction action, Long userId, String username,
                          String entityType, String entityId, String details,
                          HttpServletRequest request) {
        AuditLog auditLog = AuditLog.builder()
                .action(action)
                .userId(userId)
                .username(username)
                .entityType(entityType)
                .entityId(entityId)
                .details(details)
                .ipAddress(getClientIpAddress(request))
                .userAgent(request != null ? request.getHeader("User-Agent") : null)
                .status(AuditLog.AuditStatus.SUCCESS)
                .build();

        logAsync(auditLog);
    }

    /**
     * Log a failed action
     */
    public void logFailure(AuditLog.AuditAction action, Long userId, String username,
                          String entityType, String entityId, String details,
                          String failureReason, HttpServletRequest request) {
        AuditLog auditLog = AuditLog.builder()
                .action(action)
                .userId(userId)
                .username(username)
                .entityType(entityType)
                .entityId(entityId)
                .details(details)
                .failureReason(failureReason)
                .ipAddress(getClientIpAddress(request))
                .userAgent(request != null ? request.getHeader("User-Agent") : null)
                .status(AuditLog.AuditStatus.FAILURE)
                .build();

        logAsync(auditLog);
    }

    /**
     * Get audit logs for a specific user
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogsForUser(Long userId, Pageable pageable) {
        return auditLogRepository.findByUserId(userId, pageable)
                .map(AuditLogJpaEntity::toDomain);
    }

    /**
     * Get audit logs for a specific action
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogsByAction(AuditLog.AuditAction action, Pageable pageable) {
        return auditLogRepository.findByAction(action, pageable)
                .map(AuditLogJpaEntity::toDomain);
    }

    /**
     * Get audit logs for a date range
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getAuditLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return auditLogRepository.findByTimestampBetween(startDate, endDate)
                .stream()
                .map(AuditLogJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * Get audit logs for a specific entity
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getAuditLogsForEntity(String entityType, String entityId) {
        return auditLogRepository.findByEntity(entityType, entityId)
                .stream()
                .map(AuditLogJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * Extract client IP address from request
     */
    private String getClientIpAddress(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        String[] headers = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For can contain multiple IPs, get the first one
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }

        return request.getRemoteAddr();
    }
}
