package com.example.demo.adapter.out.persistence;

import com.example.demo.adapter.out.persistence.entity.TransactionJpaEntity;
import com.example.demo.adapter.out.persistence.mapper.TransactionMapper;
import com.example.demo.adapter.out.persistence.repository.TransactionJpaRepository;
import com.example.demo.application.ports.out.TransactionRepository;
import com.example.demo.domain.CategoryReport;
import com.example.demo.domain.Transaction;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JPA implementation of TransactionRepository (Output Port)
 * This is a persistence adapter in the hexagonal architecture
 * Uses MapStruct for domain ↔ entity conversion
 * Database-agnostic using standard JPA
 */
@Repository
@Primary  // This makes it the default implementation
public class JpaTransactionRepository implements TransactionRepository {

    private final TransactionJpaRepository jpaRepository;
    private final TransactionMapper mapper;

    public JpaTransactionRepository(TransactionJpaRepository jpaRepository, TransactionMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Transaction save(Transaction transaction) {
        TransactionJpaEntity entity = mapper.toEntity(transaction);
        TransactionJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public List<Transaction> findByAccountId(Long accountId) {
        return jpaRepository.findByAccountIdOrderByCreatedAtDesc(accountId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Transaction> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .sorted((t1, t2) -> t2.getCreatedAt().compareTo(t1.getCreatedAt()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Transaction> findByAccountIdAndDateRange(Long accountId, LocalDateTime startDate, LocalDateTime endDate) {
        return jpaRepository.findByAccountIdAndDateRange(accountId, startDate, endDate).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Transaction> findByAccountIdAndCategory(Long accountId, Transaction.TransactionCategory category) {
        TransactionJpaEntity.TransactionCategory entityCategory =
                TransactionJpaEntity.TransactionCategory.valueOf(category.name());
        return jpaRepository.findByAccountIdAndCategoryOrderByCreatedAtDesc(accountId, entityCategory).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryReport.CategorySummary> getCategorySummary(Long accountId, Transaction.TransactionType type) {
        TransactionJpaEntity.TransactionType entityType =
                TransactionJpaEntity.TransactionType.valueOf(type.name());

        List<Object[]> results = jpaRepository.findCategorySummary(accountId, entityType);

        // Calculate total for percentages
        BigDecimal total = results.stream()
                .map(row -> (BigDecimal) row[2])
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Convert to CategorySummary
        List<CategoryReport.CategorySummary> summaries = new ArrayList<>();
        for (Object[] row : results) {
            TransactionJpaEntity.TransactionCategory entityCategory = (TransactionJpaEntity.TransactionCategory) row[0];
            Long count = (Long) row[1];
            BigDecimal amount = (BigDecimal) row[2];

            BigDecimal percentage = total.compareTo(BigDecimal.ZERO) > 0
                    ? amount.multiply(BigDecimal.valueOf(100)).divide(total, 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            Transaction.TransactionCategory domainCategory =
                    Transaction.TransactionCategory.valueOf(entityCategory.name());

            summaries.add(new CategoryReport.CategorySummary(
                    domainCategory,
                    amount,
                    count.intValue(),
                    percentage
            ));
        }

        return summaries;
    }
}
