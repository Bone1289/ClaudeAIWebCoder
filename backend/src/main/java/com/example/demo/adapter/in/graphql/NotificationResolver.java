package com.example.demo.adapter.in.graphql;

import com.example.demo.adapter.in.graphql.dto.CreateNotificationInputDTO;
import com.example.demo.adapter.in.graphql.dto.NotificationDTO;
import com.example.demo.adapter.in.graphql.dto.NotificationPageDTO;
import com.example.demo.application.ports.in.CreateNotificationUseCase;
import com.example.demo.application.ports.in.DeleteNotificationUseCase;
import com.example.demo.application.ports.in.GetNotificationsUseCase;
import com.example.demo.application.ports.in.MarkNotificationAsReadUseCase;
import com.example.demo.config.security.SecurityUtil;
import com.example.demo.domain.notification.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.UUID;
import java.util.stream.Collectors;

/**
 * GraphQL Resolver for Notification operations
 */
@Controller
public class NotificationResolver {

    private final CreateNotificationUseCase createNotificationUseCase;
    private final GetNotificationsUseCase getNotificationsUseCase;
    private final MarkNotificationAsReadUseCase markNotificationAsReadUseCase;
    private final DeleteNotificationUseCase deleteNotificationUseCase;

    public NotificationResolver(CreateNotificationUseCase createNotificationUseCase,
                               GetNotificationsUseCase getNotificationsUseCase,
                               MarkNotificationAsReadUseCase markNotificationAsReadUseCase,
                               DeleteNotificationUseCase deleteNotificationUseCase) {
        this.createNotificationUseCase = createNotificationUseCase;
        this.getNotificationsUseCase = getNotificationsUseCase;
        this.markNotificationAsReadUseCase = markNotificationAsReadUseCase;
        this.deleteNotificationUseCase = deleteNotificationUseCase;
    }

    // ==================== Queries ====================

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public NotificationPageDTO notifications(@Argument Integer page, @Argument Integer size) {
        UUID userId = SecurityUtil.getCurrentUserId();
        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 20;

        Pageable pageable = PageRequest.of(pageNum, pageSize);
        Page<Notification> notificationPage = getNotificationsUseCase.getUserNotifications(userId, pageable);

        return NotificationPageDTO.fromPage(notificationPage);
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public NotificationPageDTO unreadNotifications(@Argument Integer page, @Argument Integer size) {
        UUID userId = SecurityUtil.getCurrentUserId();
        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 20;

        Pageable pageable = PageRequest.of(pageNum, pageSize);
        Page<Notification> notificationPage = getNotificationsUseCase.getUnreadNotifications(userId, pageable);

        return NotificationPageDTO.fromPage(notificationPage);
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public NotificationPageDTO readNotifications(@Argument Integer page, @Argument Integer size) {
        UUID userId = SecurityUtil.getCurrentUserId();
        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 20;

        Pageable pageable = PageRequest.of(pageNum, pageSize);
        Page<Notification> notificationPage = getNotificationsUseCase.getReadNotifications(userId, pageable);

        return NotificationPageDTO.fromPage(notificationPage);
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public NotificationPageDTO recentNotifications(@Argument Integer days,
                                                   @Argument Integer page,
                                                   @Argument Integer size) {
        UUID userId = SecurityUtil.getCurrentUserId();
        int daysToCheck = days != null ? days : 7;
        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 20;

        Pageable pageable = PageRequest.of(pageNum, pageSize);
        Page<Notification> notificationPage = getNotificationsUseCase.getRecentNotifications(userId, daysToCheck, pageable);

        return NotificationPageDTO.fromPage(notificationPage);
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public NotificationDTO notification(@Argument UUID id) {
        UUID userId = SecurityUtil.getCurrentUserId();
        return getNotificationsUseCase.getNotification(id, userId)
                .map(NotificationDTO::fromDomain)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public Integer unreadCount() {
        UUID userId = SecurityUtil.getCurrentUserId();
        return (int) getNotificationsUseCase.getUnreadCount(userId);
    }

    // ==================== Mutations ====================

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public NotificationDTO createNotification(@Argument CreateNotificationInputDTO input) {
        Notification notification = createNotificationUseCase.createNotification(
                input.userId(),
                input.type(),
                input.channel(),
                input.title(),
                input.message(),
                input.priority()
        );
        return NotificationDTO.fromDomain(notification);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public NotificationDTO markNotificationAsRead(@Argument UUID id) {
        UUID userId = SecurityUtil.getCurrentUserId();
        Notification notification = markNotificationAsReadUseCase.markAsRead(id, userId);
        return NotificationDTO.fromDomain(notification);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public NotificationDTO markNotificationAsUnread(@Argument UUID id) {
        UUID userId = SecurityUtil.getCurrentUserId();
        Notification notification = markNotificationAsReadUseCase.markAsUnread(id, userId);
        return NotificationDTO.fromDomain(notification);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public Boolean markAllNotificationsAsRead() {
        UUID userId = SecurityUtil.getCurrentUserId();
        markNotificationAsReadUseCase.markAllAsRead(userId);
        return true;
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public Boolean deleteNotification(@Argument UUID id) {
        UUID userId = SecurityUtil.getCurrentUserId();
        return deleteNotificationUseCase.deleteNotification(id, userId);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public Boolean deleteAllNotifications() {
        UUID userId = SecurityUtil.getCurrentUserId();
        deleteNotificationUseCase.deleteAllNotifications(userId);
        return true;
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public Integer cleanupOldNotifications(@Argument Integer daysOld) {
        UUID userId = SecurityUtil.getCurrentUserId();
        int days = daysOld != null ? daysOld : 30;
        return deleteNotificationUseCase.deleteOldReadNotifications(userId, days);
    }
}
