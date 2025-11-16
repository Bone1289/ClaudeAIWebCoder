package com.example.demo.adapter.in.web.banking.dto;

public class CreateAccountRequest {
    private Long customerId;
    private String accountType;

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }
}
