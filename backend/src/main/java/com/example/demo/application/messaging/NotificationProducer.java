package com.example.demo.application.messaging;

import com.example.demo.config.KafkaConfig;
import com.example.demo.domain.notification.Notification;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka producer for publishing notification events asynchronously
 * Publishes notifications to Kafka topic for async processing
 */
@Service
public class NotificationProducer {

    private static final Logger log = LoggerFactory.getLogger(NotificationProducer.class);

    private final KafkaTemplate<String, Notification> kafkaTemplate;
    private final Counter notificationsSent;
    private final Counter notificationsFailed;

    public NotificationProducer(
            KafkaTemplate<String, Notification> kafkaTemplate,
            MeterRegistry meterRegistry) {
        this.kafkaTemplate = kafkaTemplate;
        this.notificationsSent = Counter.builder("kafka.notifications.sent")
                .description("Total notifications sent to Kafka")
                .register(meterRegistry);
        this.notificationsFailed = Counter.builder("kafka.notifications.failed")
                .description("Total notifications failed to send to Kafka")
                .register(meterRegistry);
    }

    /**
     * Send notification to Kafka topic asynchronously
     *
     * @param notification The notification to send
     * @return CompletableFuture with the send result
     */
    public CompletableFuture<SendResult<String, Notification>> sendNotification(Notification notification) {
        String key = notification.getUserId().toString(); // Use userId as partition key for ordering

        log.debug("Sending notification to Kafka: id={}, userId={}, type={}",
                notification.getId(), notification.getUserId(), notification.getType());

        CompletableFuture<SendResult<String, Notification>> future =
                kafkaTemplate.send(KafkaConfig.NOTIFICATION_TOPIC, key, notification);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                notificationsSent.increment();
                log.info("Notification sent successfully to Kafka: id={}, partition={}, offset={}",
                        notification.getId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                notificationsFailed.increment();
                log.error("Failed to send notification to Kafka: id={}, error={}",
                        notification.getId(), ex.getMessage(), ex);
            }
        });

        return future;
    }

    /**
     * Send notification and wait for confirmation (blocking)
     *
     * @param notification The notification to send
     */
    public void sendNotificationSync(Notification notification) {
        try {
            sendNotification(notification).get();
            log.info("Notification sent synchronously: id={}", notification.getId());
        } catch (Exception e) {
            log.error("Failed to send notification synchronously: id={}, error={}",
                    notification.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to send notification", e);
        }
    }

    /**
     * Send email notification event to Kafka
     * Used to trigger email sending asynchronously
     *
     * @param notificationId The notification ID
     * @param email Recipient email
     * @param subject Email subject
     * @param content Email content
     */
    public void sendEmailEvent(String notificationId, String email, String subject, String content) {
        String message = String.format("%s|%s|%s|%s", notificationId, email, subject, content);
        String key = email; // Use email as partition key

        log.debug("Sending email event to Kafka: email={}, subject={}", email, subject);

        kafkaTemplate.send(KafkaConfig.EMAIL_TOPIC, key, message)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Email event sent successfully to Kafka: email={}, partition={}, offset={}",
                                email,
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    } else {
                        log.error("Failed to send email event to Kafka: email={}, error={}",
                                email, ex.getMessage(), ex);
                    }
                });
    }
}
