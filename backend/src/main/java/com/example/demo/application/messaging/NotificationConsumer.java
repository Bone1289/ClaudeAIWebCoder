package com.example.demo.application.messaging;

import com.example.demo.application.ports.out.NotificationRepository;
import com.example.demo.application.ports.out.UserRepository;
import com.example.demo.application.service.EmailService;
import com.example.demo.application.service.SseEmitterService;
import com.example.demo.config.KafkaConfig;
import com.example.demo.domain.User;
import com.example.demo.domain.notification.Notification;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Kafka consumer for processing notification events asynchronously
 * Consumes notifications from Kafka, persists them, and sends emails
 */
@Service
public class NotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final SseEmitterService sseEmitterService;
    private final Counter notificationsProcessed;
    private final Counter notificationsFailed;

    public NotificationConsumer(
            NotificationRepository notificationRepository,
            UserRepository userRepository,
            EmailService emailService,
            SseEmitterService sseEmitterService,
            MeterRegistry meterRegistry) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.sseEmitterService = sseEmitterService;
        this.notificationsProcessed = Counter.builder("kafka.notifications.processed")
                .description("Total notifications processed from Kafka")
                .register(meterRegistry);
        this.notificationsFailed = Counter.builder("kafka.notifications.processing.failed")
                .description("Total notifications failed to process")
                .register(meterRegistry);
    }

    /**
     * Consume notification events from Kafka and process them
     */
    @KafkaListener(
            topics = KafkaConfig.NOTIFICATION_TOPIC,
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "notificationKafkaListenerContainerFactory"
    )
    public void consumeNotification(Notification notification, Acknowledgment acknowledgment) {
        try {
            log.info("Consuming notification from Kafka: id={}, userId={}, type={}",
                    notification.getId(), notification.getUserId(), notification.getType());

            // 1. Save notification to database
            Notification savedNotification = notificationRepository.save(notification);
            log.debug("Notification saved to database: id={}", savedNotification.getId());

            // 2. Push notification via SSE to connected clients (real-time)
            sseEmitterService.sendNotificationToUser(savedNotification.getUserId(), savedNotification);
            log.debug("Notification pushed via SSE: id={}, userId={}",
                    savedNotification.getId(), savedNotification.getUserId());

            // 3. Update unread count via SSE
            long unreadCount = notificationRepository.countUnreadByUserId(savedNotification.getUserId());
            sseEmitterService.sendUnreadCountToUser(savedNotification.getUserId(), unreadCount);

            // 4. Send email if channel is EMAIL or BOTH
            if (notification.getChannel() == Notification.NotificationChannel.EMAIL ||
                    notification.getChannel() == Notification.NotificationChannel.BOTH) {

                sendEmailNotification(savedNotification);
            }

            // 5. Acknowledge the message
            notificationsProcessed.increment();
            acknowledgment.acknowledge();

            log.info("Notification processed successfully: id={}", savedNotification.getId());

        } catch (Exception e) {
            notificationsFailed.increment();
            log.error("Failed to process notification: userId={}, type={}, error={}",
                    notification.getUserId(), notification.getType(), e.getMessage(), e);

            // Don't acknowledge - message will be redelivered
            // You might want to implement a dead letter queue here for permanently failed messages
        }
    }

    /**
     * Send email notification to user
     */
    private void sendEmailNotification(Notification notification) {
        try {
            // Get user email from user repository
            Optional<User> userOpt = userRepository.findById(notification.getUserId());

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                String userEmail = user.getEmail();

                log.debug("Sending email notification: to={}, type={}", userEmail, notification.getType());
                emailService.sendNotificationEmail(userEmail, notification);

            } else {
                log.warn("User not found for notification email: userId={}", notification.getUserId());
            }

        } catch (Exception e) {
            log.error("Failed to send email notification: notificationId={}, error={}",
                    notification.getId(), e.getMessage(), e);
            // Don't throw - notification is already saved, email failure shouldn't fail the whole process
        }
    }

    /**
     * Consume email events from Kafka
     * Format: "notificationId|email|subject|content"
     */
    @KafkaListener(
            topics = KafkaConfig.EMAIL_TOPIC,
            groupId = "${spring.kafka.consumer.group-id}-email",
            containerFactory = "emailKafkaListenerContainerFactory"
    )
    public void consumeEmailEvent(String emailMessage, Acknowledgment acknowledgment) {
        try {
            log.info("Consuming email event from Kafka: {}", emailMessage);

            String[] parts = emailMessage.split("\\|", 4);
            if (parts.length == 4) {
                String notificationId = parts[0];
                String email = parts[1];
                String subject = parts[2];
                String content = parts[3];

                emailService.sendSimpleEmail(email, subject, content);

                acknowledgment.acknowledge();
                log.info("Email event processed: notificationId={}, to={}", notificationId, email);
            } else {
                log.warn("Invalid email message format: {}", emailMessage);
                acknowledgment.acknowledge(); // Acknowledge to prevent redelivery of bad messages
            }

        } catch (Exception e) {
            log.error("Failed to process email event: error={}", e.getMessage(), e);
            // Don't acknowledge - message will be redelivered
        }
    }
}
