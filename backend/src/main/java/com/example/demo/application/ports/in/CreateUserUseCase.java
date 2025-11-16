package com.example.demo.application.ports.in;

import com.example.demo.domain.User;

/**
 * Input port for creating a new user
 * This defines the contract for the use case
 */
public interface CreateUserUseCase {
    User createUser(String name, String email, String role);
}
