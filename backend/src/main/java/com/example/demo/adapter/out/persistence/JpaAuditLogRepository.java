package com.example.demo.adapter.out.persistence;

import com.example.demo.adapter.out.persistence.entity.AuditLogJpaEntity;
import com.example.demo.domain.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JpaAuditLogRepository extends JpaRepository<AuditLogJpaEntity, Long> {

    Page<AuditLogJpaEntity> findByUserId(Long userId, Pageable pageable);

    Page<AuditLogJpaEntity> findByAction(AuditLog.AuditAction action, Pageable pageable);

    Page<AuditLogJpaEntity> findByUserIdAndAction(Long userId, AuditLog.AuditAction action, Pageable pageable);

    @Query("SELECT a FROM AuditLogJpaEntity a WHERE a.timestamp BETWEEN :startDate AND :endDate ORDER BY a.timestamp DESC")
    List<AuditLogJpaEntity> findByTimestampBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT a FROM AuditLogJpaEntity a WHERE a.userId = :userId AND a.timestamp BETWEEN :startDate AND :endDate ORDER BY a.timestamp DESC")
    List<AuditLogJpaEntity> findByUserIdAndTimestampBetween(
        @Param("userId") Long userId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT a FROM AuditLogJpaEntity a WHERE a.entityType = :entityType AND a.entityId = :entityId ORDER BY a.timestamp DESC")
    List<AuditLogJpaEntity> findByEntity(
        @Param("entityType") String entityType,
        @Param("entityId") String entityId
    );

    List<AuditLogJpaEntity> findByStatus(AuditLog.AuditStatus status, Pageable pageable);
}
