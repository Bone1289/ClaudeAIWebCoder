package com.example.demo.adapter.in.graphql.dto;

import com.example.demo.domain.notification.Notification;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationDTO(
        UUID id,
        UUID userId,
        Notification.NotificationType type,
        Notification.NotificationChannel channel,
        String title,
        String message,
        Notification.NotificationPriority priority,
        boolean read,
        LocalDateTime createdAt,
        LocalDateTime readAt
) {
    public static NotificationDTO fromDomain(Notification notification) {
        return new NotificationDTO(
                notification.getId(),
                notification.getUserId(),
                notification.getType(),
                notification.getChannel(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getPriority(),
                notification.isRead(),
                notification.getCreatedAt(),
                notification.getReadAt()
        );
    }
}
