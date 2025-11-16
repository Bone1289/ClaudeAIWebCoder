package com.example.demo.domain;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Category Report domain object
 * Aggregates transaction data by category for analysis
 */
public record CategoryReport(
    Long accountId,
    Transaction.TransactionType transactionType,
    List<CategorySummary> categories,
    BigDecimal totalAmount,
    int totalTransactions
) {
    public CategoryReport {
        if (accountId == null) {
            throw new IllegalArgumentException("Account ID cannot be null");
        }
        if (categories == null) {
            categories = List.of();
        }
    }

    /**
     * Summary for a single category
     */
    public record CategorySummary(
        Transaction.TransactionCategory category,
        BigDecimal amount,
        int count,
        BigDecimal percentage
    ) {
        public CategorySummary {
            if (category == null) {
                throw new IllegalArgumentException("Category cannot be null");
            }
            if (amount == null) {
                amount = BigDecimal.ZERO;
            }
            if (percentage == null) {
                percentage = BigDecimal.ZERO;
            }
        }
    }

    /**
     * Get summary for a specific category
     */
    public CategorySummary getSummaryFor(Transaction.TransactionCategory category) {
        return categories.stream()
                .filter(s -> s.category() == category)
                .findFirst()
                .orElse(new CategorySummary(category, BigDecimal.ZERO, 0, BigDecimal.ZERO));
    }
}
