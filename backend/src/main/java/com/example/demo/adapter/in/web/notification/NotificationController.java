package com.example.demo.adapter.in.web.notification;

import com.example.demo.adapter.in.web.dto.ApiResponse;
import com.example.demo.adapter.in.web.notification.dto.CreateNotificationRequest;
import com.example.demo.adapter.in.web.notification.dto.NotificationResponse;
import com.example.demo.application.ports.in.CreateNotificationUseCase;
import com.example.demo.application.ports.in.DeleteNotificationUseCase;
import com.example.demo.application.ports.in.GetNotificationsUseCase;
import com.example.demo.application.ports.in.MarkNotificationAsReadUseCase;
import com.example.demo.config.security.SecurityUtil;
import com.example.demo.domain.notification.Notification;
import com.example.demo.domain.notification.Notification.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

/**
 * REST Controller for notification operations
 * Provides endpoints for creating, retrieving, updating, and deleting notifications
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final CreateNotificationUseCase createNotificationUseCase;
    private final GetNotificationsUseCase getNotificationsUseCase;
    private final MarkNotificationAsReadUseCase markNotificationAsReadUseCase;
    private final DeleteNotificationUseCase deleteNotificationUseCase;

    public NotificationController(
            CreateNotificationUseCase createNotificationUseCase,
            GetNotificationsUseCase getNotificationsUseCase,
            MarkNotificationAsReadUseCase markNotificationAsReadUseCase,
            DeleteNotificationUseCase deleteNotificationUseCase) {
        this.createNotificationUseCase = createNotificationUseCase;
        this.getNotificationsUseCase = getNotificationsUseCase;
        this.markNotificationAsReadUseCase = markNotificationAsReadUseCase;
        this.deleteNotificationUseCase = deleteNotificationUseCase;
    }

    /**
     * Create a new notification
     * POST /api/notifications
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')") // Only admins can create notifications directly
    public ResponseEntity<ApiResponse<NotificationResponse>> createNotification(
            @Valid @RequestBody CreateNotificationRequest request) {
        try {
            UUID targetUserId = request.getUserId() != null ?
                    request.getUserId() : SecurityUtil.getCurrentUserId();

            Notification notification = createNotificationUseCase.createNotification(
                    targetUserId,
                    NotificationType.valueOf(request.getType()),
                    NotificationChannel.valueOf(request.getChannel()),
                    request.getTitle(),
                    request.getMessage(),
                    NotificationPriority.valueOf(request.getPriority())
            );

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Notification created successfully",
                            NotificationResponse.fromDomain(notification)));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get all notifications for current user
     * GET /api/notifications?page=0&size=20
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getUserNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        UUID userId = SecurityUtil.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size);

        Page<NotificationResponse> notifications = getNotificationsUseCase
                .getUserNotifications(userId, pageable)
                .map(NotificationResponse::fromDomain);

        return ResponseEntity.ok(ApiResponse.success("Notifications retrieved successfully", notifications));
    }

    /**
     * Get unread notifications for current user
     * GET /api/notifications/unread?page=0&size=20
     */
    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getUnreadNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        UUID userId = SecurityUtil.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size);

        Page<NotificationResponse> notifications = getNotificationsUseCase
                .getUnreadNotifications(userId, pageable)
                .map(NotificationResponse::fromDomain);

        return ResponseEntity.ok(ApiResponse.success("Unread notifications retrieved successfully", notifications));
    }

    /**
     * Get read notifications for current user
     * GET /api/notifications/read?page=0&size=20
     */
    @GetMapping("/read")
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getReadNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        UUID userId = SecurityUtil.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size);

        Page<NotificationResponse> notifications = getNotificationsUseCase
                .getReadNotifications(userId, pageable)
                .map(NotificationResponse::fromDomain);

        return ResponseEntity.ok(ApiResponse.success("Read notifications retrieved successfully", notifications));
    }

    /**
     * Get recent notifications (last N days)
     * GET /api/notifications/recent?days=7
     */
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getRecentNotifications(
            @RequestParam(defaultValue = "7") int days,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        UUID userId = SecurityUtil.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size);

        Page<NotificationResponse> notifications = getNotificationsUseCase
                .getRecentNotifications(userId, days, pageable)
                .map(NotificationResponse::fromDomain);

        return ResponseEntity.ok(ApiResponse.success("Recent notifications retrieved successfully", notifications));
    }

    /**
     * Get unread notification count
     * GET /api/notifications/unread-count
     */
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount() {
        UUID userId = SecurityUtil.getCurrentUserId();
        long count = getNotificationsUseCase.getUnreadCount(userId);

        return ResponseEntity.ok(ApiResponse.success("Unread count retrieved successfully", count));
    }

    /**
     * Get a specific notification by ID
     * GET /api/notifications/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NotificationResponse>> getNotification(@PathVariable UUID id) {
        UUID userId = SecurityUtil.getCurrentUserId();

        Optional<Notification> notification = getNotificationsUseCase.getNotification(id, userId);

        return notification
                .map(n -> ResponseEntity.ok(ApiResponse.success("Notification retrieved successfully",
                        NotificationResponse.fromDomain(n))))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Notification not found")));
    }

    /**
     * Mark notification as read
     * PUT /api/notifications/{id}/read
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(@PathVariable UUID id) {
        try {
            UUID userId = SecurityUtil.getCurrentUserId();
            Notification notification = markNotificationAsReadUseCase.markAsRead(id, userId);

            return ResponseEntity.ok(ApiResponse.success("Notification marked as read",
                    NotificationResponse.fromDomain(notification)));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Mark notification as unread
     * PUT /api/notifications/{id}/unread
     */
    @PutMapping("/{id}/unread")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsUnread(@PathVariable UUID id) {
        try {
            UUID userId = SecurityUtil.getCurrentUserId();
            Notification notification = markNotificationAsReadUseCase.markAsUnread(id, userId);

            return ResponseEntity.ok(ApiResponse.success("Notification marked as unread",
                    NotificationResponse.fromDomain(notification)));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Mark all notifications as read
     * PUT /api/notifications/mark-all-read
     */
    @PutMapping("/mark-all-read")
    public ResponseEntity<ApiResponse<Integer>> markAllAsRead() {
        UUID userId = SecurityUtil.getCurrentUserId();
        int count = markNotificationAsReadUseCase.markAllAsRead(userId);

        return ResponseEntity.ok(ApiResponse.success(
                String.format("Marked %d notifications as read", count), count));
    }

    /**
     * Delete a notification
     * DELETE /api/notifications/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(@PathVariable UUID id) {
        UUID userId = SecurityUtil.getCurrentUserId();
        boolean deleted = deleteNotificationUseCase.deleteNotification(id, userId);

        if (deleted) {
            return ResponseEntity.ok(ApiResponse.success("Notification deleted successfully", null));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Notification not found"));
        }
    }

    /**
     * Delete old read notifications (cleanup)
     * DELETE /api/notifications/cleanup?daysOld=30
     */
    @DeleteMapping("/cleanup")
    public ResponseEntity<ApiResponse<Integer>> cleanupOldNotifications(
            @RequestParam(defaultValue = "30") int daysOld) {

        UUID userId = SecurityUtil.getCurrentUserId();
        int count = deleteNotificationUseCase.deleteOldReadNotifications(userId, daysOld);

        return ResponseEntity.ok(ApiResponse.success(
                String.format("Deleted %d old notifications", count), count));
    }

    /**
     * Delete all notifications for current user
     * DELETE /api/notifications/all
     */
    @DeleteMapping("/all")
    public ResponseEntity<ApiResponse<Void>> deleteAllNotifications() {
        UUID userId = SecurityUtil.getCurrentUserId();
        deleteNotificationUseCase.deleteAllNotifications(userId);

        return ResponseEntity.ok(ApiResponse.success("All notifications deleted successfully", null));
    }
}
