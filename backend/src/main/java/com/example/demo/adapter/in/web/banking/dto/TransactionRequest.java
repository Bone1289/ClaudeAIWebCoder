package com.example.demo.adapter.in.web.banking.dto;

import java.math.BigDecimal;

public class TransactionRequest {
    private BigDecimal amount;
    private String description;
    private String category;  // Optional: SALARY, GROCERIES, UTILITIES, etc.

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
