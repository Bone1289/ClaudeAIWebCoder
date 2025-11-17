package com.example.demo.application.ports.in;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Input port for deleting notifications
 */
public interface DeleteNotificationUseCase {

    /**
     * Delete a specific notification for a user
     */
    boolean deleteNotification(UUID notificationId, UUID userId);

    /**
     * Delete old read notifications for a user (cleanup)
     *
     * @param userId User ID
     * @param daysOld Delete notifications older than this many days
     * @return Number of notifications deleted
     */
    int deleteOldReadNotifications(UUID userId, int daysOld);

    /**
     * Delete all notifications for a user
     */
    void deleteAllNotifications(UUID userId);
}
