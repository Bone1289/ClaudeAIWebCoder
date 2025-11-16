package com.example.demo.adapter.in.web.banking.dto;

import com.example.demo.domain.Account;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AccountResponse {
    private Long id;
    private String accountNumber;
    private Long customerId;
    private String accountType;
    private BigDecimal balance;
    private String status;
    private LocalDateTime createdAt;

    public static AccountResponse fromDomain(Account account) {
        AccountResponse response = new AccountResponse();
        response.id = account.getId();
        response.accountNumber = account.getAccountNumber();
        response.customerId = account.getCustomerId();
        response.accountType = account.getAccountType();
        response.balance = account.getBalance();
        response.status = account.getStatus().name();
        response.createdAt = account.getCreatedAt();
        return response;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
