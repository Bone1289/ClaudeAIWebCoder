package com.example.demo.application.ports.in;

import com.example.demo.domain.notification.Notification;
import com.example.demo.domain.notification.Notification.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Input port for retrieving notifications
 */
public interface GetNotificationsUseCase {

    /**
     * Get a specific notification by ID for a user
     */
    Optional<Notification> getNotification(UUID notificationId, UUID userId);

    /**
     * Get all notifications for a user (paginated)
     */
    Page<Notification> getUserNotifications(UUID userId, Pageable pageable);

    /**
     * Get unread notifications for a user (paginated)
     */
    Page<Notification> getUnreadNotifications(UUID userId, Pageable pageable);

    /**
     * Get read notifications for a user (paginated)
     */
    Page<Notification> getReadNotifications(UUID userId, Pageable pageable);

    /**
     * Get notifications by type for a user (paginated)
     */
    Page<Notification> getNotificationsByType(UUID userId, NotificationType type, Pageable pageable);

    /**
     * Get recent notifications (last N days)
     */
    Page<Notification> getRecentNotifications(UUID userId, int days, Pageable pageable);

    /**
     * Get count of unread notifications for a user
     */
    long getUnreadCount(UUID userId);
}
