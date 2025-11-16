package com.example.demo.adapter.in.web.banking.dto;

import com.example.demo.domain.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionResponse {
    private Long id;
    private Long accountId;
    private String type;
    private String category;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private String description;
    private Long relatedAccountId;
    private LocalDateTime createdAt;

    public static TransactionResponse fromDomain(Transaction transaction) {
        TransactionResponse response = new TransactionResponse();
        response.id = transaction.getId();
        response.accountId = transaction.getAccountId();
        response.type = transaction.getType().name();
        response.category = transaction.getCategory() != null ? transaction.getCategory().name() : null;
        response.amount = transaction.getAmount();
        response.balanceAfter = transaction.getBalanceAfter();
        response.description = transaction.getDescription();
        response.relatedAccountId = transaction.getRelatedAccountId();
        response.createdAt = transaction.getCreatedAt();
        return response;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(BigDecimal balanceAfter) { this.balanceAfter = balanceAfter; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getRelatedAccountId() { return relatedAccountId; }
    public void setRelatedAccountId(Long relatedAccountId) { this.relatedAccountId = relatedAccountId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
