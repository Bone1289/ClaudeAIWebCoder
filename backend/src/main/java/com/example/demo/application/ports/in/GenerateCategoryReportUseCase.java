package com.example.demo.application.ports.in;

import com.example.demo.domain.CategoryReport;
import com.example.demo.domain.Transaction;

import java.util.UUID;

/**
 * Input port for generating category-based transaction reports
 */
public interface GenerateCategoryReportUseCase {
    /**
     * Generate category report for an account
     * @param accountId Account ID
     * @param transactionType Type of transactions to analyze (DEPOSIT or WITHDRAWAL)
     * @return Category report with aggregations
     */
    CategoryReport generateCategoryReport(UUID accountId, Transaction.TransactionType transactionType);
}
