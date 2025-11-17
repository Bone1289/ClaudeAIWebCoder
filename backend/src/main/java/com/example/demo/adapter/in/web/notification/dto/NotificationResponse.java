package com.example.demo.adapter.in.web.notification.dto;

import com.example.demo.domain.notification.Notification;
import com.example.demo.domain.notification.Notification.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for notification responses
 */
public class NotificationResponse {

    private UUID id;
    private UUID userId;
    private String type;
    private String channel;
    private String title;
    private String message;
    private String priority;
    private boolean read;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;

    public NotificationResponse() {
    }

    public NotificationResponse(UUID id, UUID userId, String type, String channel,
                               String title, String message, String priority,
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
     * Convert domain notification to response DTO
     */
    public static NotificationResponse fromDomain(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getUserId(),
                notification.getType().name(),
                notification.getChannel().name(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getPriority().name(),
                notification.isRead(),
                notification.getCreatedAt(),
                notification.getReadAt()
        );
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }
}
