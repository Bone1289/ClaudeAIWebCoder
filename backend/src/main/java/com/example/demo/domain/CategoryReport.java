package com.example.demo.domain;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Category Report domain object
 * Aggregates transaction data by category for analysis
 * Now uses TransactionCategory entity instead of enum
 */
public record CategoryReport(
    UUID accountId,
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
     * Now includes full category entity information
     */
    public record CategorySummary(
        TransactionCategory category,
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
     * Get summary for a specific category by ID
     */
    public CategorySummary getSummaryForCategoryId(UUID categoryId) {
        return categories.stream()
                .filter(s -> s.category().getId().equals(categoryId))
                .findFirst()
                .orElse(null);
    }
}
