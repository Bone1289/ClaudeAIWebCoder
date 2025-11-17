package com.example.demo.application.ports.in;

import com.example.demo.domain.User;

import java.util.UUID;

/**
 * Input port for getting current authenticated user
 */
public interface GetCurrentUserUseCase {
    User getCurrentUser(UUID userId);
}
