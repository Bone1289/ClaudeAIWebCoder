package com.example.demo.adapter.in.web.auth.dto;

import com.example.demo.domain.User;

import java.time.LocalDateTime;

/**
 * DTO for user profile response
 * IMPORTANT: Password is never included in the response
 */
public class UserResponse {
    private String id;
    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private String role;
    private String status;
    private LocalDateTime createdAt;

    public static UserResponse fromDomain(User user) {
        UserResponse response = new UserResponse();
        response.id = user.getId().toString();
        response.email = user.getEmail();
        response.username = user.getUsername();
        response.firstName = user.getFirstName();
        response.lastName = user.getLastName();
        response.role = user.getRole().name();
        response.status = user.getStatus().name();
        response.createdAt = user.getCreatedAt();
        return response;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
