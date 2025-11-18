package com.example.demo.adapter.in.graphql.dto;

import com.example.demo.domain.User;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserDTO(
        UUID id,
        String email,
        String username,
        String firstName,
        String lastName,
        User.UserRole role,
        User.UserStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static UserDTO fromDomain(User user) {
        return new UserDTO(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole(),
                user.getStatus(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
