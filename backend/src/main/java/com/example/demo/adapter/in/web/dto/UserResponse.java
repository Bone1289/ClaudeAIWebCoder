package com.example.demo.adapter.in.web.dto;

import com.example.demo.domain.User;

/**
 * DTO for user response
 * This is part of the web adapter layer
 * Converts domain User to API response
 */
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private String role;

    public UserResponse() {
    }

    public UserResponse(Long id, String name, String email, String role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    // Factory method to convert domain User to UserResponse
    public static UserResponse fromDomain(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
