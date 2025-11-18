package com.example.demo.application.service;

import com.example.demo.application.ports.out.UserRepository;
import com.example.demo.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Admin service for user management operations
 * Only accessible by users with ADMIN role
 */
@Service
@Transactional
public class AdminService {

    private final UserRepository userRepository;

    public AdminService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Get all users in the system
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Get user by ID
     */
    public Optional<User> getUserById(UUID userId) {
        return userRepository.findById(userId);
    }

    /**
     * Suspend a user
     */
    public User suspendUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        User suspendedUser = user.suspend();
        return userRepository.update(suspendedUser);
    }

    /**
     * Activate a user
     */
    public User activateUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        User activatedUser = user.activate();
        return userRepository.update(activatedUser);
    }

    /**
     * Lock a user
     */
    public User lockUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        User lockedUser = user.lock();
        return userRepository.update(lockedUser);
    }

    /**
     * Delete a user
     */
    public void deleteUser(UUID userId) {
        if (!userRepository.deleteById(userId)) {
            throw new IllegalArgumentException("User not found");
        }
    }
}
