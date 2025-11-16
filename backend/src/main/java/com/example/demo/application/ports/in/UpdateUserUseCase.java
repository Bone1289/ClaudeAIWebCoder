package com.example.demo.application.ports.in;

import com.example.demo.domain.User;

import java.util.Optional;

/**
 * Input port for updating a user
 * This defines the contract for the use case
 */
public interface UpdateUserUseCase {
    Optional<User> updateUser(Long id, String name, String email, String role);
}
