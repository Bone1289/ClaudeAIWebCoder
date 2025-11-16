package com.example.demo.application.ports.out;

import com.example.demo.domain.CategoryReport;
import com.example.demo.domain.Transaction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Output port for transaction persistence
 */
public interface TransactionRepository {
    Transaction save(Transaction transaction);
    List<Transaction> findByAccountId(Long accountId);
    List<Transaction> findAll();

    /**
     * Find transactions for an account within a date range
     */
    List<Transaction> findByAccountIdAndDateRange(Long accountId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find transactions by account and category
     */
    List<Transaction> findByAccountIdAndCategory(Long accountId, Transaction.TransactionCategory category);

    /**
     * Get category summary (category -> total amount, count)
     */
    List<CategoryReport.CategorySummary> getCategorySummary(Long accountId, Transaction.TransactionType type);
}
