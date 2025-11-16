package com.example.demo.adapter.in.web.banking.dto;

import java.math.BigDecimal;

public class TransactionRequest {
    private BigDecimal amount;
    private String description;
    private Long categoryId;  // Optional: ID of the category entity

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
}
