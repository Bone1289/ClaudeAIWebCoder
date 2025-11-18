package com.example.demo.adapter.in.graphql.dto;

import com.example.demo.domain.CategoryReport;
import com.example.demo.domain.TransactionCategory;

import java.math.BigDecimal;
import java.util.UUID;

public record CategoryReportDTO(
        UUID categoryId,
        String categoryName,
        TransactionCategory.CategoryType categoryType,
        BigDecimal totalAmount,
        int transactionCount,
        double percentage
) {
    public static CategoryReportDTO fromDomain(CategoryReport.CategorySummary summary) {
        return new CategoryReportDTO(
                summary.category().getId(),
                summary.category().getName(),
                summary.category().getType(),
                summary.amount(),
                summary.count(),
                summary.percentage().doubleValue()
        );
    }
}
