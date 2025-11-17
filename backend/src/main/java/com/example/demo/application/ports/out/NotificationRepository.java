package com.example.demo.application.ports.out;

import com.example.demo.domain.notification.Notification;
import com.example.demo.domain.notification.Notification.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Output port for notification persistence
 * Defines operations for storing and retrieving notifications
 */
public interface NotificationRepository {

    /**
     * Save a new notification
     */
    Notification save(Notification notification);

    /**
     * Update an existing notification
     */
    Notification update(Notification notification);

    /**
     * Find notification by ID
     */
    Optional<Notification> findById(UUID id);

    /**
     * Find notification by ID and user ID (for authorization)
     */
    Optional<Notification> findByIdAndUserId(UUID id, UUID userId);

    /**
     * Find all notifications for a user (paginated)
     */
    Page<Notification> findByUserId(UUID userId, Pageable pageable);

    /**
     * Find unread notifications for a user (paginated)
     */
    Page<Notification> findUnreadByUserId(UUID userId, Pageable pageable);

    /**
     * Find read notifications for a user (paginated)
     */
    Page<Notification> findReadByUserId(UUID userId, Pageable pageable);

    /**
     * Find notifications by type for a user (paginated)
     */
    Page<Notification> findByUserIdAndType(UUID userId, NotificationType type, Pageable pageable);

    /**
     * Find recent notifications (last N days)
     */
    Page<Notification> findRecentNotifications(UUID userId, LocalDateTime since, Pageable pageable);

    /**
     * Count unread notifications for a user
     */
    long countUnreadByUserId(UUID userId);

    /**
     * Mark all unread notifications as read for a user
     */
    int markAllAsRead(UUID userId);

    /**
     * Delete notification by ID
     */
    boolean deleteById(UUID id);

    /**
     * Delete notification by ID and user ID (for authorization)
     */
    boolean deleteByIdAndUserId(UUID id, UUID userId);

    /**
     * Delete old read notifications (cleanup)
     */
    int deleteOldReadNotifications(UUID userId, LocalDateTime before);

    /**
     * Delete all notifications for a user
     */
    void deleteByUserId(UUID userId);

    /**
     * Check if notification exists for user
     */
    boolean existsByIdAndUserId(UUID id, UUID userId);

    /**
     * Find all notifications (for admin purposes)
     */
    List<Notification> findAll();
}
