package com.example.demo.adapter.out.persistence.repository;

import com.example.demo.adapter.out.persistence.entity.TransactionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Spring Data JPA Repository for Transaction persistence
 * Uses standard JPA repository - database agnostic
 */
@Repository
public interface TransactionJpaRepository extends JpaRepository<TransactionJpaEntity, Long> {

    List<TransactionJpaEntity> findByAccountIdOrderByCreatedAtDesc(Long accountId);

    List<TransactionJpaEntity> findByAccountIdAndCategoryOrderByCreatedAtDesc(
            Long accountId,
            TransactionJpaEntity.TransactionCategory category
    );

    List<TransactionJpaEntity> findByAccountIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long accountId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    @Query("SELECT t FROM TransactionJpaEntity t WHERE t.accountId = :accountId " +
           "AND t.createdAt >= :startDate AND t.createdAt <= :endDate " +
           "ORDER BY t.createdAt DESC")
    List<TransactionJpaEntity> findByAccountIdAndDateRange(
            @Param("accountId") Long accountId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT t.category, COUNT(t), SUM(t.amount) FROM TransactionJpaEntity t " +
           "WHERE t.accountId = :accountId " +
           "AND t.type = :type " +
           "GROUP BY t.category")
    List<Object[]> findCategorySummary(
            @Param("accountId") Long accountId,
            @Param("type") TransactionJpaEntity.TransactionType type
    );
}
