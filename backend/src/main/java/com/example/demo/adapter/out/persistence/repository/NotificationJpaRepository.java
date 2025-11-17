package com.example.demo.adapter.out.persistence.repository;

import com.example.demo.adapter.out.persistence.entity.NotificationJpaEntity;
import com.example.demo.domain.notification.Notification.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for Notification persistence
 */
@Repository
public interface NotificationJpaRepository extends JpaRepository<NotificationJpaEntity, UUID> {

    /**
     * Find all notifications for a specific user
     */
    Page<NotificationJpaEntity> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    /**
     * Find unread notifications for a user
     */
    Page<NotificationJpaEntity> findByUserIdAndReadFalseOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    /**
     * Find read notifications for a user
     */
    Page<NotificationJpaEntity> findByUserIdAndReadTrueOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    /**
     * Find notifications by type for a user
     */
    Page<NotificationJpaEntity> findByUserIdAndTypeOrderByCreatedAtDesc(
            UUID userId,
            NotificationType type,
            Pageable pageable
    );

    /**
     * Count unread notifications for a user
     */
    long countByUserIdAndReadFalse(UUID userId);

    /**
     * Find recent notifications (last N days)
     */
    @Query("SELECT n FROM NotificationJpaEntity n WHERE n.userId = :userId " +
           "AND n.createdAt >= :since ORDER BY n.createdAt DESC")
    Page<NotificationJpaEntity> findRecentNotifications(
            @Param("userId") UUID userId,
            @Param("since") LocalDateTime since,
            Pageable pageable
    );

    /**
     * Mark all unread notifications as read for a user
     */
    @Modifying
    @Query("UPDATE NotificationJpaEntity n SET n.read = true, n.readAt = :readAt " +
           "WHERE n.userId = :userId AND n.read = false")
    int markAllAsRead(@Param("userId") UUID userId, @Param("readAt") LocalDateTime readAt);

    /**
     * Delete old read notifications (cleanup)
     */
    @Modifying
    @Query("DELETE FROM NotificationJpaEntity n WHERE n.userId = :userId " +
           "AND n.read = true AND n.readAt < :before")
    int deleteOldReadNotifications(@Param("userId") UUID userId, @Param("before") LocalDateTime before);

    /**
     * Delete all notifications for a user
     */
    void deleteByUserId(UUID userId);

    /**
     * Check if notification exists for user
     */
    boolean existsByIdAndUserId(UUID id, UUID userId);
}
