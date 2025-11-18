package com.example.demo.adapter.in.grpc.notification;

import com.example.demo.application.service.NotificationService;
import com.example.demo.config.security.SecurityUtil;
import com.example.demo.domain.User;
import com.example.demo.domain.notification.Notification;
import com.example.demo.grpc.common.Empty;
import com.example.demo.grpc.common.IdRequest;
import com.example.demo.grpc.common.PaginationRequest;
import com.example.demo.grpc.notification.*;
import com.google.protobuf.Timestamp;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * gRPC service adapter for notification operations
 * Includes server-side streaming for real-time notifications (replaces SSE)
 * Follows hexagonal architecture pattern - this is an input adapter
 */
@GrpcService
public class NotificationGrpcService extends NotificationServiceGrpc.NotificationServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(NotificationGrpcService.class);

    private final NotificationService notificationService;

    // Store active notification streams per user
    private final Map<UUID, List<StreamObserver<NotificationStreamResponse>>> activeStreams = new ConcurrentHashMap<>();

    public NotificationGrpcService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public void createNotification(CreateNotificationRequest request,
                                   StreamObserver<CreateNotificationResponse> responseObserver) {
        try {
            User currentUser = SecurityUtil.getCurrentUser();
            logger.info("gRPC CreateNotification request by user: {}", currentUser.getId());

            // Determine target user (defaults to current user if not specified)
            UUID targetUserId = request.getUserId().isEmpty()
                    ? currentUser.getId()
                    : UUID.fromString(request.getUserId());

            Notification notification = notificationService.createNotification(
                    targetUserId,
                    Notification.NotificationType.valueOf(request.getType()),
                    Notification.NotificationChannel.valueOf(request.getChannel()),
                    request.getTitle(),
                    request.getMessage(),
                    Notification.NotificationPriority.valueOf(request.getPriority())
            );

            CreateNotificationResponse response = CreateNotificationResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Notification created successfully")
                    .setNotification(mapToNotificationResponse(notification))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            // Notify active streams
            notifyStreams(targetUserId, notification, "NEW");

        } catch (IllegalArgumentException e) {
            logger.error("CreateNotification validation error: {}", e.getMessage());
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            logger.error("CreateNotification error", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Failed to create notification: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void getNotification(IdRequest request, StreamObserver<GetNotificationResponse> responseObserver) {
        try {
            User currentUser = SecurityUtil.getCurrentUser();
            logger.info("gRPC GetNotification request for id: {}", request.getId());

            Notification notification = notificationService.getNotification(
                    UUID.fromString(request.getId()), currentUser.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Notification not found"));

            GetNotificationResponse response = GetNotificationResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Notification retrieved successfully")
                    .setNotification(mapToNotificationResponse(notification))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (IllegalArgumentException e) {
            logger.error("GetNotification error: {}", e.getMessage());
            responseObserver.onError(io.grpc.Status.NOT_FOUND
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            logger.error("GetNotification error", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Failed to get notification: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void getAllNotifications(GetAllNotificationsRequest request,
                                    StreamObserver<GetAllNotificationsResponse> responseObserver) {
        try {
            User currentUser = SecurityUtil.getCurrentUser();
            logger.info("gRPC GetAllNotifications request for user: {}", currentUser.getId());

            Pageable pageable = createPageable(request.getPagination());
            Page<Notification> notificationPage = notificationService.getUserNotifications(currentUser.getId(), pageable);

            GetAllNotificationsResponse response = GetAllNotificationsResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Notifications retrieved successfully")
                    .addAllNotifications(notificationPage.getContent().stream()
                            .map(this::mapToNotificationResponse)
                            .collect(Collectors.toList()))
                    .setPagination(mapToPaginationMetadata(notificationPage))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            logger.error("GetAllNotifications error", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Failed to get notifications: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void getUnreadNotifications(PaginationRequest request,
                                       StreamObserver<GetUnreadNotificationsResponse> responseObserver) {
        try {
            User currentUser = SecurityUtil.getCurrentUser();
            logger.info("gRPC GetUnreadNotifications request for user: {}", currentUser.getId());

            Pageable pageable = createPageable(request);
            Page<Notification> notificationPage = notificationService.getUnreadNotifications(currentUser.getId(), pageable);

            GetUnreadNotificationsResponse response = GetUnreadNotificationsResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Unread notifications retrieved successfully")
                    .addAllNotifications(notificationPage.getContent().stream()
                            .map(this::mapToNotificationResponse)
                            .collect(Collectors.toList()))
                    .setPagination(mapToPaginationMetadata(notificationPage))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            logger.error("GetUnreadNotifications error", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Failed to get unread notifications: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void getUnreadCount(Empty request, StreamObserver<GetUnreadCountResponse> responseObserver) {
        try {
            User currentUser = SecurityUtil.getCurrentUser();
            logger.info("gRPC GetUnreadCount request for user: {}", currentUser.getId());

            long count = notificationService.getUnreadCount(currentUser.getId());

            GetUnreadCountResponse response = GetUnreadCountResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Unread count retrieved successfully")
                    .setCount(count)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            logger.error("GetUnreadCount error", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Failed to get unread count: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void markAsRead(IdRequest request, StreamObserver<MarkAsReadResponse> responseObserver) {
        try {
            User currentUser = SecurityUtil.getCurrentUser();
            logger.info("gRPC MarkAsRead request for notification: {}", request.getId());

            Notification notification = notificationService.markAsRead(
                    UUID.fromString(request.getId()), currentUser.getId());

            MarkAsReadResponse response = MarkAsReadResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Notification marked as read")
                    .setNotification(mapToNotificationResponse(notification))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            // Notify active streams of the update
            notifyStreams(currentUser.getId(), notification, "UPDATED");

        } catch (Exception e) {
            logger.error("MarkAsRead error", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Failed to mark notification as read: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void markAllAsRead(Empty request, StreamObserver<MarkAllAsReadResponse> responseObserver) {
        try {
            User currentUser = SecurityUtil.getCurrentUser();
            logger.info("gRPC MarkAllAsRead request for user: {}", currentUser.getId());

            int updatedCount = notificationService.markAllAsRead(currentUser.getId());

            MarkAllAsReadResponse response = MarkAllAsReadResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("All notifications marked as read")
                    .setUpdatedCount(updatedCount)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            logger.error("MarkAllAsRead error", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Failed to mark all notifications as read: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void deleteNotification(IdRequest request, StreamObserver<DeleteNotificationResponse> responseObserver) {
        try {
            User currentUser = SecurityUtil.getCurrentUser();
            logger.info("gRPC DeleteNotification request for id: {}", request.getId());

            notificationService.deleteNotification(
                    UUID.fromString(request.getId()), currentUser.getId());

            DeleteNotificationResponse response = DeleteNotificationResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Notification deleted successfully")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            logger.error("DeleteNotification error", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Failed to delete notification: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    /**
     * Server-side streaming for real-time notifications
     * Replaces SSE functionality
     */
    @Override
    public void streamNotifications(Empty request, StreamObserver<NotificationStreamResponse> responseObserver) {
        try {
            User currentUser = SecurityUtil.getCurrentUser();
            logger.info("gRPC StreamNotifications started for user: {}", currentUser.getId());

            // Add this stream to active streams
            activeStreams.computeIfAbsent(currentUser.getId(), k -> new java.util.ArrayList<>())
                    .add(responseObserver);

            // Send initial heartbeat or welcome message
            logger.info("Notification stream established for user: {}", currentUser.getId());

            // The stream will remain open until the client disconnects or an error occurs
            // New notifications will be sent via notifyStreams method

        } catch (Exception e) {
            logger.error("StreamNotifications error", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Failed to establish notification stream: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    /**
     * Notify all active streams for a user about a new/updated notification
     */
    private void notifyStreams(UUID userId, Notification notification, String eventType) {
        List<StreamObserver<NotificationStreamResponse>> streams = activeStreams.get(userId);
        if (streams != null && !streams.isEmpty()) {
            NotificationStreamResponse streamResponse = NotificationStreamResponse.newBuilder()
                    .setNotification(mapToNotificationResponse(notification))
                    .setEventType(eventType)
                    .build();

            streams.forEach(stream -> {
                try {
                    stream.onNext(streamResponse);
                } catch (Exception e) {
                    logger.error("Error sending notification to stream", e);
                    streams.remove(stream);
                }
            });
        }
    }

    /**
     * Maps domain Notification to gRPC NotificationResponse
     */
    private com.example.demo.grpc.notification.NotificationResponse mapToNotificationResponse(Notification notification) {
        var builder = com.example.demo.grpc.notification.NotificationResponse.newBuilder()
                .setId(notification.getId().toString())
                .setUserId(notification.getUserId().toString())
                .setType(notification.getType().name())
                .setChannel(notification.getChannel().name())
                .setTitle(notification.getTitle())
                .setMessage(notification.getMessage())
                .setPriority(notification.getPriority().name())
                .setRead(notification.isRead())
                .setCreatedAt(Timestamp.newBuilder()
                        .setSeconds(notification.getCreatedAt().toEpochSecond(ZoneOffset.UTC))
                        .setNanos(notification.getCreatedAt().getNano())
                        .build());

        if (notification.getReadAt() != null) {
            builder.setReadAt(Timestamp.newBuilder()
                    .setSeconds(notification.getReadAt().toEpochSecond(ZoneOffset.UTC))
                    .setNanos(notification.getReadAt().getNano())
                    .build());
        }

        return builder.build();
    }

    /**
     * Creates Spring Pageable from gRPC PaginationRequest
     */
    private Pageable createPageable(PaginationRequest request) {
        if (request == null) {
            return PageRequest.of(0, 20);
        }

        int page = request.getPage();
        int size = request.getSize() > 0 ? request.getSize() : 20;

        if (request.getSortBy().isEmpty()) {
            return PageRequest.of(page, size);
        }

        Sort.Direction direction = "DESC".equalsIgnoreCase(request.getSortDirection())
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        return PageRequest.of(page, size, Sort.by(direction, request.getSortBy()));
    }

    /**
     * Maps Spring Page to gRPC PaginationMetadata
     */
    private com.example.demo.grpc.common.PaginationMetadata mapToPaginationMetadata(Page<?> page) {
        return com.example.demo.grpc.common.PaginationMetadata.newBuilder()
                .setCurrentPage(page.getNumber())
                .setPageSize(page.getSize())
                .setTotalElements(page.getTotalElements())
                .setTotalPages(page.getTotalPages())
                .setHasNext(page.hasNext())
                .setHasPrevious(page.hasPrevious())
                .build();
    }
}
