package com.example.demo.adapter.in.web.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for user login request
 */
public class LoginRequest {

    @NotBlank(message = "Email or username is required")
    private String emailOrUsername;

    @NotBlank(message = "Password is required")
    private String password;

    // Getters and Setters
    public String getEmailOrUsername() {
        return emailOrUsername;
    }

    public void setEmailOrUsername(String emailOrUsername) {
        this.emailOrUsername = emailOrUsername;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
