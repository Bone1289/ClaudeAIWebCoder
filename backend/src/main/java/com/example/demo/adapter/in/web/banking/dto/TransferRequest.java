package com.example.demo.adapter.in.web.banking.dto;

import java.math.BigDecimal;

public class TransferRequest {
    private Long toAccountId;
    private BigDecimal amount;
    private String description;

    public Long getToAccountId() { return toAccountId; }
    public void setToAccountId(Long toAccountId) { this.toAccountId = toAccountId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
