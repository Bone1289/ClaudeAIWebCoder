package com.example.demo.adapter.out.persistence;

import com.example.demo.adapter.out.persistence.entity.NotificationJpaEntity;
import com.example.demo.adapter.out.persistence.mapper.NotificationMapper;
import com.example.demo.adapter.out.persistence.repository.NotificationJpaRepository;
import com.example.demo.application.ports.out.NotificationRepository;
import com.example.demo.domain.notification.Notification;
import com.example.demo.domain.notification.Notification.NotificationType;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * JPA implementation of NotificationRepository (Output Port)
 * Persistence adapter in hexagonal architecture
 * Uses MapStruct for domain ↔ entity conversion
 */
@Repository
@Primary
@Transactional
public class JpaNotificationRepository implements NotificationRepository {

    private final NotificationJpaRepository jpaRepository;
    private final NotificationMapper mapper;

    public JpaNotificationRepository(NotificationJpaRepository jpaRepository, NotificationMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Notification save(Notification notification) {
        NotificationJpaEntity entity = mapper.toEntity(notification);
        NotificationJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Notification update(Notification notification) {
        if (notification.getId() == null || !jpaRepository.existsById(notification.getId())) {
            throw new IllegalArgumentException("Cannot update notification: notification not found");
        }
        NotificationJpaEntity entity = mapper.toEntity(notification);
        NotificationJpaEntity updated = jpaRepository.save(entity);
        return mapper.toDomain(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Notification> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Notification> findByIdAndUserId(UUID id, UUID userId) {
        return jpaRepository.findById(id)
                .filter(entity -> entity.getUserId().equals(userId))
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Notification> findByUserId(UUID userId, Pageable pageable) {
        Page<NotificationJpaEntity> entities = jpaRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return entities.map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Notification> findUnreadByUserId(UUID userId, Pageable pageable) {
        Page<NotificationJpaEntity> entities = jpaRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId, pageable);
        return entities.map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Notification> findReadByUserId(UUID userId, Pageable pageable) {
        Page<NotificationJpaEntity> entities = jpaRepository.findByUserIdAndReadTrueOrderByCreatedAtDesc(userId, pageable);
        return entities.map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Notification> findByUserIdAndType(UUID userId, NotificationType type, Pageable pageable) {
        Page<NotificationJpaEntity> entities = jpaRepository.findByUserIdAndTypeOrderByCreatedAtDesc(userId, type, pageable);
        return entities.map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Notification> findRecentNotifications(UUID userId, LocalDateTime since, Pageable pageable) {
        Page<NotificationJpaEntity> entities = jpaRepository.findRecentNotifications(userId, since, pageable);
        return entities.map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnreadByUserId(UUID userId) {
        return jpaRepository.countByUserIdAndReadFalse(userId);
    }

    @Override
    public int markAllAsRead(UUID userId) {
        return jpaRepository.markAllAsRead(userId, LocalDateTime.now());
    }

    @Override
    public boolean deleteById(UUID id) {
        if (jpaRepository.existsById(id)) {
            jpaRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteByIdAndUserId(UUID id, UUID userId) {
        Optional<NotificationJpaEntity> notification = jpaRepository.findById(id);
        if (notification.isPresent() && notification.get().getUserId().equals(userId)) {
            jpaRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public int deleteOldReadNotifications(UUID userId, LocalDateTime before) {
        return jpaRepository.deleteOldReadNotifications(userId, before);
    }

    @Override
    public void deleteByUserId(UUID userId) {
        jpaRepository.deleteByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByIdAndUserId(UUID id, UUID userId) {
        return jpaRepository.existsByIdAndUserId(id, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
