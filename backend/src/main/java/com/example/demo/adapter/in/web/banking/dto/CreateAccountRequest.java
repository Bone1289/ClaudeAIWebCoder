package com.example.demo.adapter.in.web.banking.dto;

import java.util.UUID;

public class CreateAccountRequest {
    private String userId; // UUID as string
    private String firstName;
    private String lastName;
    private String nationality;
    private String accountType;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }
    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }
}
