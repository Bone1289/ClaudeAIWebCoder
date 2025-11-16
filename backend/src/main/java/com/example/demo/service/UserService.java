package com.example.demo.service;

import com.example.demo.model.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class UserService {

    private final List<User> users = new ArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    public UserService() {
        // Initialize with some sample data
        users.add(new User(idCounter.getAndIncrement(), "John Doe", "john@example.com", "USER"));
        users.add(new User(idCounter.getAndIncrement(), "Jane Smith", "jane@example.com", "ADMIN"));
        users.add(new User(idCounter.getAndIncrement(), "Bob Johnson", "bob@example.com", "USER"));
    }

    /**
     * Get all users
     */
    public List<User> getAllUsers() {
        return new ArrayList<>(users);
    }

    /**
     * Get user by ID
     */
    public Optional<User> getUserById(Long id) {
        return users.stream()
                .filter(user -> user.getId().equals(id))
                .findFirst();
    }

    /**
     * Create a new user
     */
    public User createUser(User user) {
        user.setId(idCounter.getAndIncrement());
        users.add(user);
        return user;
    }

    /**
     * Update an existing user
     */
    public Optional<User> updateUser(Long id, User updatedUser) {
        return users.stream()
                .filter(user -> user.getId().equals(id))
                .findFirst()
                .map(user -> {
                    user.setName(updatedUser.getName());
                    user.setEmail(updatedUser.getEmail());
                    user.setRole(updatedUser.getRole());
                    return user;
                });
    }

    /**
     * Delete a user
     */
    public boolean deleteUser(Long id) {
        return users.removeIf(user -> user.getId().equals(id));
    }
}
