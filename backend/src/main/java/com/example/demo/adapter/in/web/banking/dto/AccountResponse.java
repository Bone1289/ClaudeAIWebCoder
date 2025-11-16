package com.example.demo.adapter.in.web.banking.dto;

import com.example.demo.domain.Account;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AccountResponse {
    private Long id;
    private String accountNumber;
    private String firstName;
    private String lastName;
    private String nationality;
    private String accountType;
    private BigDecimal balance;
    private String status;
    private LocalDateTime createdAt;

    public static AccountResponse fromDomain(Account account) {
        AccountResponse response = new AccountResponse();
        response.id = account.getId();
        response.accountNumber = account.getAccountNumber();
        response.firstName = account.getFirstName();
        response.lastName = account.getLastName();
        response.nationality = account.getNationality();
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
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }
    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
