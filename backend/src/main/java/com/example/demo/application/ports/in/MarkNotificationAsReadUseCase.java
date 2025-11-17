package com.example.demo.application.ports.in;

import com.example.demo.domain.notification.Notification;

import java.util.UUID;

/**
 * Input port for marking notifications as read/unread
 */
public interface MarkNotificationAsReadUseCase {

    /**
     * Mark a specific notification as read
     */
    Notification markAsRead(UUID notificationId, UUID userId);

    /**
     * Mark a specific notification as unread
     */
    Notification markAsUnread(UUID notificationId, UUID userId);

    /**
     * Mark all notifications as read for a user
     */
    int markAllAsRead(UUID userId);
}
