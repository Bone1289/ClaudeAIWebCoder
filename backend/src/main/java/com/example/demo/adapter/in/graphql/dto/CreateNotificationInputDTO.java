package com.example.demo.adapter.in.graphql.dto;

import com.example.demo.domain.notification.Notification;

import java.util.UUID;

public record CreateNotificationInputDTO(
        UUID userId,
        Notification.NotificationType type,
        Notification.NotificationChannel channel,
        String title,
        String message,
        Notification.NotificationPriority priority
) {
}
