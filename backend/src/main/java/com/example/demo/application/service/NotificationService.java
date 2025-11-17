package com.example.demo.application.service;

import com.example.demo.application.messaging.NotificationProducer;
import com.example.demo.application.ports.in.*;
import com.example.demo.application.ports.out.NotificationRepository;
import com.example.demo.domain.notification.Notification;
import com.example.demo.domain.notification.Notification.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Notification service implementing all notification use cases
 * Central orchestrator for notification operations
 */
@Service
@Transactional
public class NotificationService implements
        CreateNotificationUseCase,
        GetNotificationsUseCase,
        MarkNotificationAsReadUseCase,
        DeleteNotificationUseCase {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final NotificationProducer notificationProducer;

    public NotificationService(
            NotificationRepository notificationRepository,
            NotificationProducer notificationProducer) {
        this.notificationRepository = notificationRepository;
        this.notificationProducer = notificationProducer;
    }

    // ==================== CreateNotificationUseCase ====================

    @Override
    public Notification createNotification(
            UUID userId,
            NotificationType type,
            NotificationChannel channel,
            String title,
            String message,
            NotificationPriority priority) {

        log.info("Creating notification: userId={}, type={}, channel={}", userId, type, channel);

        Notification notification = Notification.create(userId, type, channel, title, message, priority);
        Notification saved = notificationRepository.save(notification);

        log.info("Notification created: id={}, userId={}", saved.getId(), userId);
        return saved;
    }

    @Override
    public void createAndSendAsync(
            UUID userId,
            NotificationType type,
            NotificationChannel channel,
            String title,
            String message,
            NotificationPriority priority) {

        log.info("Creating and sending notification async: userId={}, type={}, channel={}", userId, type, channel);

        Notification notification = Notification.create(userId, type, channel, title, message, priority);

        // Send to Kafka for async processing
        notificationProducer.sendNotification(notification);

        log.info("Notification sent to Kafka: userId={}, type={}", userId, type);
    }

    // ==================== GetNotificationsUseCase ====================

    @Override
    @Transactional(readOnly = true)
    public Optional<Notification> getNotification(UUID notificationId, UUID userId) {
        log.debug("Getting notification: id={}, userId={}", notificationId, userId);
        return notificationRepository.findByIdAndUserId(notificationId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Notification> getUserNotifications(UUID userId, Pageable pageable) {
        log.debug("Getting user notifications: userId={}, page={}", userId, pageable.getPageNumber());
        return notificationRepository.findByUserId(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Notification> getUnreadNotifications(UUID userId, Pageable pageable) {
        log.debug("Getting unread notifications: userId={}, page={}", userId, pageable.getPageNumber());
        return notificationRepository.findUnreadByUserId(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Notification> getReadNotifications(UUID userId, Pageable pageable) {
        log.debug("Getting read notifications: userId={}, page={}", userId, pageable.getPageNumber());
        return notificationRepository.findReadByUserId(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Notification> getNotificationsByType(UUID userId, NotificationType type, Pageable pageable) {
        log.debug("Getting notifications by type: userId={}, type={}, page={}", userId, type, pageable.getPageNumber());
        return notificationRepository.findByUserIdAndType(userId, type, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Notification> getRecentNotifications(UUID userId, int days, Pageable pageable) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        log.debug("Getting recent notifications: userId={}, days={}, page={}", userId, days, pageable.getPageNumber());
        return notificationRepository.findRecentNotifications(userId, since, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(UUID userId) {
        log.debug("Getting unread count: userId={}", userId);
        return notificationRepository.countUnreadByUserId(userId);
    }

    // ==================== MarkNotificationAsReadUseCase ====================

    @Override
    public Notification markAsRead(UUID notificationId, UUID userId) {
        log.info("Marking notification as read: id={}, userId={}", notificationId, userId);

        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found or access denied"));

        Notification markedAsRead = notification.markAsRead();
        Notification updated = notificationRepository.update(markedAsRead);

        log.info("Notification marked as read: id={}", notificationId);
        return updated;
    }

    @Override
    public Notification markAsUnread(UUID notificationId, UUID userId) {
        log.info("Marking notification as unread: id={}, userId={}", notificationId, userId);

        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found or access denied"));

        Notification markedAsUnread = notification.markAsUnread();
        Notification updated = notificationRepository.update(markedAsUnread);

        log.info("Notification marked as unread: id={}", notificationId);
        return updated;
    }

    @Override
    public int markAllAsRead(UUID userId) {
        log.info("Marking all notifications as read: userId={}", userId);
        int count = notificationRepository.markAllAsRead(userId);
        log.info("Marked {} notifications as read for user: {}", count, userId);
        return count;
    }

    // ==================== DeleteNotificationUseCase ====================

    @Override
    public boolean deleteNotification(UUID notificationId, UUID userId) {
        log.info("Deleting notification: id={}, userId={}", notificationId, userId);
        boolean deleted = notificationRepository.deleteByIdAndUserId(notificationId, userId);
        log.info("Notification deleted: id={}, success={}", notificationId, deleted);
        return deleted;
    }

    @Override
    public int deleteOldReadNotifications(UUID userId, int daysOld) {
        LocalDateTime before = LocalDateTime.now().minusDays(daysOld);
        log.info("Deleting old read notifications: userId={}, daysOld={}", userId, daysOld);
        int count = notificationRepository.deleteOldReadNotifications(userId, before);
        log.info("Deleted {} old notifications for user: {}", count, userId);
        return count;
    }

    @Override
    public void deleteAllNotifications(UUID userId) {
        log.info("Deleting all notifications: userId={}", userId);
        notificationRepository.deleteByUserId(userId);
        log.info("All notifications deleted for user: {}", userId);
    }

    // ==================== Additional Helper Methods ====================

    /**
     * Create welcome notification for new users
     */
    public void createWelcomeNotification(UUID userId, String username) {
        log.info("Creating welcome notification for new user: userId={}, username={}", userId, username);

        createAndSendAsync(
                userId,
                NotificationType.ACCOUNT_CREATED,
                NotificationChannel.BOTH,
                "Welcome to VirtualBank!",
                String.format("Hello %s! Welcome to VirtualBank. Your account has been created successfully.", username),
                NotificationPriority.MEDIUM
        );
    }

    /**
     * Create transaction notification
     */
    public void createTransactionNotification(
            UUID userId,
            boolean success,
            String transactionDetails) {

        NotificationType type = success ?
                NotificationType.TRANSACTION_COMPLETED :
                NotificationType.TRANSACTION_FAILED;

        String title = success ?
                "Transaction Completed" :
                "Transaction Failed";

        NotificationPriority priority = success ?
                NotificationPriority.LOW :
                NotificationPriority.HIGH;

        createAndSendAsync(
                userId,
                type,
                NotificationChannel.IN_APP,
                title,
                transactionDetails,
                priority
        );
    }

    /**
     * Create security alert notification
     */
    public void createSecurityAlert(UUID userId, String alertMessage) {
        log.warn("Creating security alert: userId={}, message={}", userId, alertMessage);

        createAndSendAsync(
                userId,
                NotificationType.SECURITY_ALERT,
                NotificationChannel.BOTH,
                "Security Alert",
                alertMessage,
                NotificationPriority.URGENT
        );
    }
}
