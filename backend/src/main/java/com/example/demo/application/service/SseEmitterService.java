package com.example.demo.application.service;

import com.example.demo.domain.notification.Notification;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Service for managing Server-Sent Events (SSE) connections
 * Enables real-time push notifications to connected clients
 */
@Service
public class SseEmitterService {

    private static final Logger log = LoggerFactory.getLogger(SseEmitterService.class);
    private static final long SSE_TIMEOUT = 30 * 60 * 1000L; // 30 minutes
    private static final long HEARTBEAT_INTERVAL = 30000L; // 30 seconds

    // Map of userId -> List of SseEmitters (multiple connections per user)
    private final Map<UUID, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper;
    private final Counter connectionsCreated;
    private final Counter connectionsRemoved;
    private final Counter notificationsSent;
    private final Counter notificationsFailed;

    public SseEmitterService(ObjectMapper objectMapper, MeterRegistry meterRegistry) {
        this.objectMapper = objectMapper;

        // Register metrics
        this.connectionsCreated = Counter.builder("sse.connections.created")
                .description("Total SSE connections created")
                .register(meterRegistry);
        this.connectionsRemoved = Counter.builder("sse.connections.removed")
                .description("Total SSE connections removed")
                .register(meterRegistry);
        this.notificationsSent = Counter.builder("sse.notifications.sent")
                .description("Total notifications sent via SSE")
                .register(meterRegistry);
        this.notificationsFailed = Counter.builder("sse.notifications.failed")
                .description("Total SSE notification send failures")
                .register(meterRegistry);

        // Register gauge for active connections
        Gauge.builder("sse.connections.active", this, SseEmitterService::getTotalConnectionCount)
                .description("Current number of active SSE connections")
                .register(meterRegistry);
    }

    /**
     * Create a new SSE emitter for a user
     */
    public SseEmitter createEmitter(UUID userId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        // Add emitter to user's list
        emitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);
        connectionsCreated.increment();

        log.info("SSE connection created for user: {}, total connections: {}",
                userId, emitters.get(userId).size());

        // Setup callbacks
        emitter.onCompletion(() -> {
            removeEmitter(userId, emitter);
            log.debug("SSE connection completed for user: {}", userId);
        });

        emitter.onTimeout(() -> {
            removeEmitter(userId, emitter);
            log.debug("SSE connection timed out for user: {}", userId);
        });

        emitter.onError(throwable -> {
            removeEmitter(userId, emitter);
            log.error("SSE connection error for user: {}, error: {}",
                    userId, throwable.getMessage());
        });

        // Send initial connection established event
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("{\"message\":\"SSE connection established\"}"));
            log.debug("Sent connection confirmation to user: {}", userId);
        } catch (IOException e) {
            log.error("Failed to send connection confirmation: {}", e.getMessage());
            removeEmitter(userId, emitter);
        }

        return emitter;
    }

    /**
     * Send notification to a specific user via SSE
     */
    public void sendNotificationToUser(UUID userId, Notification notification) {
        List<SseEmitter> userEmitters = emitters.get(userId);

        if (userEmitters == null || userEmitters.isEmpty()) {
            log.debug("No active SSE connections for user: {}", userId);
            return;
        }

        log.info("Sending notification via SSE to user: {}, connections: {}",
                userId, userEmitters.size());

        try {
            String notificationJson = objectMapper.writeValueAsString(notification);

            List<SseEmitter> deadEmitters = new ArrayList<>();

            for (SseEmitter emitter : userEmitters) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("notification")
                            .data(notificationJson));

                    notificationsSent.increment();
                    log.debug("Notification sent successfully via SSE: notificationId={}, userId={}",
                            notification.getId(), userId);

                } catch (IOException e) {
                    log.warn("Failed to send notification via SSE: userId={}, error={}",
                            userId, e.getMessage());
                    deadEmitters.add(emitter);
                    notificationsFailed.increment();
                }
            }

            // Clean up dead emitters
            deadEmitters.forEach(emitter -> removeEmitter(userId, emitter));

        } catch (Exception e) {
            log.error("Error serializing notification for SSE: notificationId={}, error={}",
                    notification.getId(), e.getMessage(), e);
            notificationsFailed.increment();
        }
    }

    /**
     * Send unread count update to a specific user
     */
    public void sendUnreadCountToUser(UUID userId, long unreadCount) {
        List<SseEmitter> userEmitters = emitters.get(userId);

        if (userEmitters == null || userEmitters.isEmpty()) {
            return;
        }

        log.debug("Sending unread count via SSE to user: {}, count: {}", userId, unreadCount);

        String data = String.format("{\"unreadCount\":%d}", unreadCount);
        List<SseEmitter> deadEmitters = new ArrayList<>();

        for (SseEmitter emitter : userEmitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("unread-count")
                        .data(data));
            } catch (IOException e) {
                log.warn("Failed to send unread count via SSE: userId={}, error={}",
                        userId, e.getMessage());
                deadEmitters.add(emitter);
            }
        }

        deadEmitters.forEach(emitter -> removeEmitter(userId, emitter));
    }

    /**
     * Send heartbeat to all active connections to keep them alive
     */
    @Scheduled(fixedRate = HEARTBEAT_INTERVAL)
    public void sendHeartbeat() {
        if (emitters.isEmpty()) {
            return;
        }

        log.debug("Sending heartbeat to {} users with {} total connections",
                emitters.size(), getTotalConnectionCount());

        String heartbeatData = String.format("{\"timestamp\":%d}", System.currentTimeMillis());

        emitters.forEach((userId, userEmitters) -> {
            List<SseEmitter> deadEmitters = new ArrayList<>();

            for (SseEmitter emitter : userEmitters) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("heartbeat")
                            .data(heartbeatData));
                } catch (IOException e) {
                    log.debug("Heartbeat failed for user: {}, removing connection", userId);
                    deadEmitters.add(emitter);
                }
            }

            deadEmitters.forEach(emitter -> removeEmitter(userId, emitter));
        });
    }

    /**
     * Remove a specific emitter for a user
     */
    private void removeEmitter(UUID userId, SseEmitter emitter) {
        List<SseEmitter> userEmitters = emitters.get(userId);

        if (userEmitters != null) {
            userEmitters.remove(emitter);
            connectionsRemoved.increment();

            if (userEmitters.isEmpty()) {
                emitters.remove(userId);
                log.info("All SSE connections removed for user: {}", userId);
            } else {
                log.debug("SSE connection removed for user: {}, remaining: {}",
                        userId, userEmitters.size());
            }
        }

        try {
            emitter.complete();
        } catch (Exception e) {
            log.debug("Error completing emitter: {}", e.getMessage());
        }
    }

    /**
     * Remove all emitters for a user
     */
    public void removeAllEmittersForUser(UUID userId) {
        List<SseEmitter> userEmitters = emitters.remove(userId);

        if (userEmitters != null) {
            userEmitters.forEach(emitter -> {
                try {
                    emitter.complete();
                    connectionsRemoved.increment();
                } catch (Exception e) {
                    log.debug("Error completing emitter: {}", e.getMessage());
                }
            });
            log.info("Removed {} SSE connections for user: {}", userEmitters.size(), userId);
        }
    }

    /**
     * Get total number of active connections
     */
    public int getTotalConnectionCount() {
        return emitters.values().stream()
                .mapToInt(List::size)
                .sum();
    }

    /**
     * Get number of connected users
     */
    public int getConnectedUserCount() {
        return emitters.size();
    }

    /**
     * Check if user has any active connections
     */
    public boolean hasActiveConnection(UUID userId) {
        List<SseEmitter> userEmitters = emitters.get(userId);
        return userEmitters != null && !userEmitters.isEmpty();
    }
}
