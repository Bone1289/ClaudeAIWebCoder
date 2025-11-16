package com.example.demo.adapter.in.web.banking.dto;

import com.example.demo.domain.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionResponse {
    private String id;
    private String accountId;
    private String type;
    private String categoryId;  // Now just the ID, frontend will fetch full details
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private String description;
    private String relatedAccountId;
    private LocalDateTime createdAt;

    public static TransactionResponse fromDomain(Transaction transaction) {
        TransactionResponse response = new TransactionResponse();
        response.id = transaction.getId().toString();
        response.accountId = transaction.getAccountId().toString();
        response.type = transaction.getType().name();
        response.categoryId = transaction.getCategoryId() != null ? transaction.getCategoryId().toString() : null;
        response.amount = transaction.getAmount();
        response.balanceAfter = transaction.getBalanceAfter();
        response.description = transaction.getDescription();
        response.relatedAccountId = transaction.getRelatedAccountId() != null ? transaction.getRelatedAccountId().toString() : null;
        response.createdAt = transaction.getCreatedAt();
        return response;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(BigDecimal balanceAfter) { this.balanceAfter = balanceAfter; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getRelatedAccountId() { return relatedAccountId; }
    public void setRelatedAccountId(String relatedAccountId) { this.relatedAccountId = relatedAccountId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
