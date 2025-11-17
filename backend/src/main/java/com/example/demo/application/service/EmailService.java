package com.example.demo.application.service;

import com.example.demo.domain.notification.Notification;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

/**
 * Email service for sending notification emails asynchronously
 * Supports both plain text and HTML emails with Thymeleaf templates
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    private final Counter emailsSent;
    private final Counter emailsFailed;

    @Value("${spring.mail.from:noreply@virtualbank.com}")
    private String fromEmail;

    public EmailService(
            JavaMailSender mailSender,
            SpringTemplateEngine templateEngine,
            MeterRegistry meterRegistry) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.emailsSent = Counter.builder("emails.sent")
                .description("Total emails sent successfully")
                .register(meterRegistry);
        this.emailsFailed = Counter.builder("emails.failed")
                .description("Total emails failed to send")
                .register(meterRegistry);
    }

    /**
     * Send a plain text email asynchronously
     */
    @Async
    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            log.info("Sending plain text email to: {}, subject: {}", to, subject);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);

            emailsSent.increment();
            log.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            emailsFailed.increment();
            log.error("Failed to send email to: {}, error: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    /**
     * Send an HTML email using Thymeleaf template
     */
    @Async
    public void sendHtmlEmail(String to, String subject, String templateName, Context context) {
        try {
            log.info("Sending HTML email to: {}, subject: {}, template: {}", to, subject, templateName);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);

            String htmlContent = templateEngine.process(templateName, context);
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);

            emailsSent.increment();
            log.info("HTML email sent successfully to: {}", to);
        } catch (MessagingException e) {
            emailsFailed.increment();
            log.error("Failed to send HTML email to: {}, error: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send HTML email", e);
        }
    }

    /**
     * Send notification email based on notification type
     */
    @Async
    public void sendNotificationEmail(String to, Notification notification) {
        try {
            log.info("Sending notification email: type={}, to={}", notification.getType(), to);

            Context context = new Context();
            context.setVariable("notification", notification);
            context.setVariable("title", notification.getTitle());
            context.setVariable("message", notification.getMessage());
            context.setVariable("type", notification.getType().toString());
            context.setVariable("priority", notification.getPriority().toString());

            // Use template based on notification type or generic template
            String templateName = getTemplateForType(notification.getType());

            sendHtmlEmail(to, notification.getTitle(), templateName, context);

            log.info("Notification email sent: type={}, to={}", notification.getType(), to);
        } catch (Exception e) {
            emailsFailed.increment();
            log.error("Failed to send notification email: type={}, to={}, error={}",
                    notification.getType(), to, e.getMessage(), e);

            // Fallback to simple text email
            sendSimpleEmail(to, notification.getTitle(), notification.getMessage());
        }
    }

    /**
     * Get email template name based on notification type
     */
    private String getTemplateForType(Notification.NotificationType type) {
        return switch (type) {
            case ACCOUNT_CREATED -> "email/account-created";
            case TRANSACTION_COMPLETED -> "email/transaction-completed";
            case TRANSACTION_FAILED -> "email/transaction-failed";
            case SECURITY_ALERT -> "email/security-alert";
            case SYSTEM_ANNOUNCEMENT -> "email/system-announcement";
            case ACCOUNT_SUSPENDED -> "email/account-suspended";
            case ACCOUNT_ACTIVATED -> "email/account-activated";
            default -> "email/notification-generic";
        };
    }

    /**
     * Send welcome email to new users
     */
    @Async
    public void sendWelcomeEmail(String to, String username) {
        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("bankName", "VirtualBank");

        sendHtmlEmail(to, "Welcome to VirtualBank!", "email/welcome", context);
    }

    /**
     * Send password reset email
     */
    @Async
    public void sendPasswordResetEmail(String to, String resetLink) {
        Context context = new Context();
        context.setVariable("resetLink", resetLink);

        sendHtmlEmail(to, "Password Reset Request", "email/password-reset", context);
    }
}
