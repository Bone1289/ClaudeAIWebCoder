package com.example.demo.adapter.out.persistence;

import com.example.demo.adapter.out.persistence.entity.TransactionCategoryJpaEntity;
import com.example.demo.adapter.out.persistence.entity.TransactionJpaEntity;
import com.example.demo.adapter.out.persistence.mapper.CategoryMapper;
import com.example.demo.adapter.out.persistence.mapper.TransactionMapper;
import com.example.demo.adapter.out.persistence.repository.TransactionCategoryJpaRepository;
import com.example.demo.adapter.out.persistence.repository.TransactionJpaRepository;
import com.example.demo.application.ports.out.TransactionRepository;
import com.example.demo.domain.CategoryReport;
import com.example.demo.domain.Transaction;
import com.example.demo.domain.TransactionCategory;
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
 * Now handles category entity relationships
 */
@Repository
@Primary  // This makes it the default implementation
public class JpaTransactionRepository implements TransactionRepository {

    private final TransactionJpaRepository jpaRepository;
    private final TransactionCategoryJpaRepository categoryJpaRepository;
    private final TransactionMapper mapper;
    private final CategoryMapper categoryMapper;

    public JpaTransactionRepository(TransactionJpaRepository jpaRepository,
                                   TransactionCategoryJpaRepository categoryJpaRepository,
                                   TransactionMapper mapper,
                                   CategoryMapper categoryMapper) {
        this.jpaRepository = jpaRepository;
        this.categoryJpaRepository = categoryJpaRepository;
        this.mapper = mapper;
        this.categoryMapper = categoryMapper;
    }

    @Override
    public Transaction save(Transaction transaction) {
        TransactionJpaEntity entity = mapper.toEntity(transaction);

        // Set category relationship if categoryId is present
        if (transaction.getCategoryId() != null) {
            TransactionCategoryJpaEntity category = categoryJpaRepository.findById(transaction.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + transaction.getCategoryId()));
            entity.setCategory(category);
        }

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
    public List<Transaction> findByAccountIdAndCategoryId(Long accountId, Long categoryId) {
        return jpaRepository.findByAccountIdAndCategoryId(accountId, categoryId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryReport.CategorySummary> getCategorySummary(Long accountId, Transaction.TransactionType type) {
        TransactionJpaEntity.TransactionType entityType =
                TransactionJpaEntity.TransactionType.valueOf(type.name());

        // Query returns: [category_id, count, sum]
        List<Object[]> results = jpaRepository.findCategorySummaryByType(accountId, entityType);

        // Calculate total for percentages
        BigDecimal total = results.stream()
                .map(row -> (BigDecimal) row[2])
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Convert to CategorySummary
        List<CategoryReport.CategorySummary> summaries = new ArrayList<>();
        for (Object[] row : results) {
            Long categoryId = ((Number) row[0]).longValue();
            Long count = ((Number) row[1]).longValue();
            BigDecimal amount = (BigDecimal) row[2];

            BigDecimal percentage = total.compareTo(BigDecimal.ZERO) > 0
                    ? amount.multiply(BigDecimal.valueOf(100)).divide(total, 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            // Fetch the category entity
            TransactionCategoryJpaEntity categoryEntity = categoryJpaRepository.findById(categoryId)
                .orElse(null);

            if (categoryEntity != null) {
                TransactionCategory category = categoryMapper.toDomain(categoryEntity);
                summaries.add(new CategoryReport.CategorySummary(
                        category,
                        amount,
                        count.intValue(),
                        percentage
                ));
            }
        }

        return summaries;
    }
}
