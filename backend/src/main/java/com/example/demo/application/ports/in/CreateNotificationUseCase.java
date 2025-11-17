package com.example.demo.application.ports.in;

import com.example.demo.domain.notification.Notification;
import com.example.demo.domain.notification.Notification.*;

import java.util.UUID;

/**
 * Input port for creating notifications
 */
public interface CreateNotificationUseCase {

    /**
     * Create a new notification for a user
     */
    Notification createNotification(
            UUID userId,
            NotificationType type,
            NotificationChannel channel,
            String title,
            String message,
            NotificationPriority priority
    );

    /**
     * Create and send notification asynchronously via Kafka
     */
    void createAndSendAsync(
            UUID userId,
            NotificationType type,
            NotificationChannel channel,
            String title,
            String message,
            NotificationPriority priority
    );
}
