package com.example.demo.application.ports.out;

import com.example.demo.domain.CategoryReport;
import com.example.demo.domain.Transaction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Output port for transaction persistence
 * Updated to use category entity references
 */
public interface TransactionRepository {
    Transaction save(Transaction transaction);
    List<Transaction> findByAccountId(UUID accountId);
    List<Transaction> findAll();

    /**
     * Find transactions for an account within a date range
     */
    List<Transaction> findByAccountIdAndDateRange(UUID accountId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find transactions by account and category ID
     */
    List<Transaction> findByAccountIdAndCategoryId(UUID accountId, UUID categoryId);

    /**
     * Get category summary (category -> total amount, count)
     * Returns full category entities with aggregated data
     */
    List<CategoryReport.CategorySummary> getCategorySummary(UUID accountId, Transaction.TransactionType type);
}
