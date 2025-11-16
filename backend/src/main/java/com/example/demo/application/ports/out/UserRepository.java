package com.example.demo.application.ports.out;

import com.example.demo.domain.User;

import java.util.List;
import java.util.Optional;

/**
 * Output port for user persistence
 * This defines the contract for storing and retrieving users
 * The implementation details (in-memory, database, etc.) are hidden
 */
public interface UserRepository {
    /**
     * Save a new user
     * @param user User to save
     * @return Saved user with generated ID
     */
    User save(User user);

    /**
     * Find all users
     * @return List of all users
     */
    List<User> findAll();

    /**
     * Find user by ID
     * @param id User ID
     * @return Optional containing user if found
     */
    Optional<User> findById(Long id);

    /**
     * Update an existing user
     * @param user User to update
     * @return Updated user
     */
    User update(User user);

    /**
     * Delete user by ID
     * @param id User ID
     * @return true if user was deleted, false otherwise
     */
    boolean deleteById(Long id);

    /**
     * Check if user exists by ID
     * @param id User ID
     * @return true if user exists, false otherwise
     */
    boolean existsById(Long id);
}
