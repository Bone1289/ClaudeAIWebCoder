package com.example.demo.adapter.in.web.banking.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class TransferRequest {
    private UUID toAccountId;
    private BigDecimal amount;
    private String description;

    public UUID getToAccountId() { return toAccountId; }
    public void setToAccountId(UUID toAccountId) { this.toAccountId = toAccountId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
