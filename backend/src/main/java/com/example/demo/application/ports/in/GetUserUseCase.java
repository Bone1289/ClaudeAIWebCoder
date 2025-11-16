package com.example.demo.application.ports.in;

import com.example.demo.domain.User;

import java.util.List;
import java.util.Optional;

/**
 * Input port for retrieving users
 * This defines the contract for querying users
 */
public interface GetUserUseCase {
    List<User> getAllUsers();
    Optional<User> getUserById(Long id);
}
