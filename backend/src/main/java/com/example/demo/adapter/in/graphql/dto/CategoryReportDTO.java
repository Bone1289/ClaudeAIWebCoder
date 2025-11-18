package com.example.demo.adapter.in.graphql.dto;

import com.example.demo.domain.CategoryReport;
import com.example.demo.domain.TransactionCategory;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public record CategoryReportDTO(
        UUID categoryId,
        String categoryName,
        TransactionCategory.CategoryType categoryType,
        BigDecimal totalAmount,
        int transactionCount,
        double percentage,
        List<TransactionDTO> transactions
) {
    public static CategoryReportDTO fromDomain(CategoryReport.CategoryItem item) {
        return new CategoryReportDTO(
                item.getCategoryId(),
                item.getCategoryName(),
                item.getCategoryType(),
                item.getTotalAmount(),
                item.getTransactionCount(),
                item.getPercentage(),
                item.getTransactions().stream()
                        .map(TransactionDTO::fromDomain)
                        .collect(Collectors.toList())
        );
    }
}
