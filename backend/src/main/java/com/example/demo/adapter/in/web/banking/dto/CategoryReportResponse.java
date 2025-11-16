package com.example.demo.adapter.in.web.banking.dto;

import com.example.demo.adapter.in.web.category.dto.CategoryResponse;
import com.example.demo.domain.CategoryReport;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class CategoryReportResponse {
    private Long accountId;
    private String transactionType;
    private List<CategorySummaryResponse> categories;
    private BigDecimal totalAmount;
    private int totalTransactions;

    public static CategoryReportResponse fromDomain(CategoryReport report) {
        CategoryReportResponse response = new CategoryReportResponse();
        response.accountId = report.accountId();
        response.transactionType = report.transactionType().name();
        response.categories = report.categories().stream()
                .map(CategorySummaryResponse::fromDomain)
                .collect(Collectors.toList());
        response.totalAmount = report.totalAmount();
        response.totalTransactions = report.totalTransactions();
        return response;
    }

    public static class CategorySummaryResponse {
        private CategoryResponse category;  // Full category details
        private BigDecimal amount;
        private int count;
        private BigDecimal percentage;

        public static CategorySummaryResponse fromDomain(CategoryReport.CategorySummary summary) {
            CategorySummaryResponse response = new CategorySummaryResponse();
            response.category = CategoryResponse.fromDomain(summary.category());
            response.amount = summary.amount();
            response.count = summary.count();
            response.percentage = summary.percentage();
            return response;
        }

        // Getters and Setters
        public CategoryResponse getCategory() { return category; }
        public void setCategory(CategoryResponse category) { this.category = category; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }
        public BigDecimal getPercentage() { return percentage; }
        public void setPercentage(BigDecimal percentage) { this.percentage = percentage; }
    }

    // Getters and Setters
    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }
    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
    public List<CategorySummaryResponse> getCategories() { return categories; }
    public void setCategories(List<CategorySummaryResponse> categories) { this.categories = categories; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public int getTotalTransactions() { return totalTransactions; }
    public void setTotalTransactions(int totalTransactions) { this.totalTransactions = totalTransactions; }
}
