package com.example.demo.domain.notification;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Notification domain entity
 * Represents a notification that can be sent to users via different channels
 */
public class Notification {
    private final UUID id;
    private final UUID userId;
    private final NotificationType type;
    private final NotificationChannel channel;
    private final String title;
    private final String message;
    private final NotificationPriority priority;
    private final boolean read;
    private final LocalDateTime createdAt;
    private final LocalDateTime readAt;

    public enum NotificationType {
        ACCOUNT_CREATED,
        TRANSACTION_COMPLETED,
        TRANSACTION_FAILED,
        SECURITY_ALERT,
        SYSTEM_ANNOUNCEMENT,
        ACCOUNT_SUSPENDED,
        ACCOUNT_ACTIVATED
    }

    public enum NotificationChannel {
        IN_APP,
        EMAIL,
        BOTH
    }

    public enum NotificationPriority {
        LOW,
        MEDIUM,
        HIGH,
        URGENT
    }

    private Notification(UUID id, UUID userId, NotificationType type, NotificationChannel channel,
                        String title, String message, NotificationPriority priority,
                        boolean read, LocalDateTime createdAt, LocalDateTime readAt) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.channel = channel;
        this.title = title;
        this.message = message;
        this.priority = priority;
        this.read = read;
        this.createdAt = createdAt;
        this.readAt = readAt;
    }

    /**
     * Create a new notification
     */
    public static Notification create(UUID userId, NotificationType type, NotificationChannel channel,
                                     String title, String message, NotificationPriority priority) {
        validateUserId(userId);
        validateTitle(title);
        validateMessage(message);

        return new Notification(
            null,
            userId,
            type,
            channel,
            title,
            message,
            priority,
            false,
            LocalDateTime.now(),
            null
        );
    }

    /**
     * Reconstitute notification from persistence
     */
    public static Notification of(UUID id, UUID userId, NotificationType type, NotificationChannel channel,
                                 String title, String message, NotificationPriority priority,
                                 boolean read, LocalDateTime createdAt, LocalDateTime readAt) {
        if (id == null) {
            throw new IllegalArgumentException("Notification ID cannot be null");
        }
        validateUserId(userId);
        validateTitle(title);
        validateMessage(message);

        return new Notification(id, userId, type, channel, title, message, priority,
                              read, createdAt, readAt);
    }

    /**
     * Mark notification as read
     */
    public Notification markAsRead() {
        if (this.read) {
            return this;
        }
        return new Notification(id, userId, type, channel, title, message, priority,
                              true, createdAt, LocalDateTime.now());
    }

    /**
     * Mark notification as unread
     */
    public Notification markAsUnread() {
        if (!this.read) {
            return this;
        }
        return new Notification(id, userId, type, channel, title, message, priority,
                              false, createdAt, null);
    }

    // Validation methods
    private static void validateUserId(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
    }

    private static void validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Notification title cannot be null or empty");
        }
        if (title.length() > 200) {
            throw new IllegalArgumentException("Notification title must be at most 200 characters");
        }
    }

    private static void validateMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Notification message cannot be null or empty");
        }
        if (message.length() > 1000) {
            throw new IllegalArgumentException("Notification message must be at most 1000 characters");
        }
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public NotificationType getType() {
        return type;
    }

    public NotificationChannel getChannel() {
        return channel;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public NotificationPriority getPriority() {
        return priority;
    }

    public boolean isRead() {
        return read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Notification that = (Notification) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", userId=" + userId +
                ", type=" + type +
                ", channel=" + channel +
                ", title='" + title + '\'' +
                ", priority=" + priority +
                ", read=" + read +
                ", createdAt=" + createdAt +
                '}';
    }
}
