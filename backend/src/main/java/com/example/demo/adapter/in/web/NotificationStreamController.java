package com.example.demo.adapter.in.web;

import com.example.demo.application.ports.in.GetNotificationsUseCase;
import com.example.demo.application.service.NotificationService;
import com.example.demo.domain.notification.Notification;
import com.example.demo.config.security.SecurityUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * REST Controller for Server-Sent Events (SSE) notification streaming
 * Provides real-time notification updates to connected clients
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationStreamController {

    private static final Logger log = LoggerFactory.getLogger(NotificationStreamController.class);
    private static final long SSE_TIMEOUT = 30 * 60 * 1000; // 30 minutes
    private static final long HEARTBEAT_INTERVAL = 15 * 1000; // 15 seconds

    private final GetNotificationsUseCase getNotificationsUseCase;
    private final ObjectMapper objectMapper;
    private final Map<UUID, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final ScheduledExecutorService heartbeatScheduler = Executors.newScheduledThreadPool(1);

    public NotificationStreamController(
            NotificationService notificationService,
            ObjectMapper objectMapper) {
        this.getNotificationsUseCase = notificationService;
        this.objectMapper = objectMapper;

        // Start heartbeat scheduler
        heartbeatScheduler.scheduleAtFixedRate(this::sendHeartbeats, HEARTBEAT_INTERVAL, HEARTBEAT_INTERVAL, TimeUnit.MILLISECONDS);
    }

    /**
     * SSE endpoint for real-time notifications
     * Client connects and receives notifications as they arrive
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("isAuthenticated()")
    public SseEmitter streamNotifications(@RequestParam(required = false) String token) {
        UUID userId = SecurityUtil.getCurrentUserId();
        log.info("SSE connection request from user: {}", userId);

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        // Store emitter for this user
        emitters.put(userId, emitter);

        // Handle connection events
        emitter.onCompletion(() -> {
            log.info("SSE connection completed for user: {}", userId);
            emitters.remove(userId);
        });

        emitter.onTimeout(() -> {
            log.info("SSE connection timeout for user: {}", userId);
            emitters.remove(userId);
        });

        emitter.onError((ex) -> {
            log.error("SSE connection error for user: {}", userId, ex);
            emitters.remove(userId);
        });

        // Send initial connected event
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("Connected to notification stream"));

            // Send initial unread count
            sendUnreadCount(userId, emitter);

        } catch (IOException e) {
            log.error("Error sending initial SSE events to user: {}", userId, e);
            emitter.completeWithError(e);
        }

        return emitter;
    }

    /**
     * Send unread notification count to a specific emitter
     */
    private void sendUnreadCount(UUID userId, SseEmitter emitter) {
        try {
            Page<Notification> unreadPage = getNotificationsUseCase.getUnreadNotifications(userId, PageRequest.of(0, 1));
            long unreadCount = unreadPage.getTotalElements();

            Map<String, Object> data = Map.of("unreadCount", unreadCount);
            String json = objectMapper.writeValueAsString(data);

            emitter.send(SseEmitter.event()
                    .name("unread-count")
                    .data(json));

        } catch (IOException e) {
            log.error("Error sending unread count to user: {}", userId, e);
        }
    }

    /**
     * Send heartbeat to all connected clients to keep connection alive
     */
    private void sendHeartbeats() {
        emitters.forEach((userId, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("heartbeat")
                        .data("ping"));
            } catch (Exception e) {
                log.warn("Failed to send heartbeat to user: {}, removing emitter", userId);
                emitters.remove(userId);
            }
        });
    }

    /**
     * Broadcast notification to a specific user
     * This method can be called by other services when a new notification is created
     */
    public void broadcastNotification(UUID userId, Notification notification) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            try {
                String json = objectMapper.writeValueAsString(notification);
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(json));

                // Also send updated unread count
                sendUnreadCount(userId, emitter);

                log.info("Broadcasted notification to user: {}, notificationId: {}", userId, notification.getId());
            } catch (IOException e) {
                log.error("Error broadcasting notification to user: {}", userId, e);
                emitters.remove(userId);
            }
        }
    }
}
