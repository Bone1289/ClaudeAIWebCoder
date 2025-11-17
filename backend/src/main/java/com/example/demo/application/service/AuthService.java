package com.example.demo.application.service;

import com.example.demo.application.ports.in.GetCurrentUserUseCase;
import com.example.demo.application.ports.in.LoginUseCase;
import com.example.demo.application.ports.in.RegisterUserUseCase;
import com.example.demo.application.ports.out.UserRepository;
import com.example.demo.domain.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Authentication service implementing authentication use cases
 * This is the core application service for user registration and login
 */
@Service
@Transactional
public class AuthService implements RegisterUserUseCase, LoginUseCase, GetCurrentUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User registerUser(String email, String username, String password, String firstName, String lastName) {
        // Check if email already exists
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already registered");
        }

        // Check if username already exists
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already taken");
        }

        // Hash password
        String hashedPassword = passwordEncoder.encode(password);

        // Create and save user
        User user = User.create(email, username, hashedPassword, firstName, lastName);
        return userRepository.save(user);
    }

    @Override
    public User login(String emailOrUsername, String password) {
        // Try to find user by email or username
        User user = userRepository.findByEmail(emailOrUsername)
                .or(() -> userRepository.findByUsername(emailOrUsername))
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        // Verify password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        // Check if user can login
        if (!user.canLogin()) {
            throw new IllegalStateException("Account is " + user.getStatus().name().toLowerCase() + ". Please contact support.");
        }

        return user;
    }

    @Override
    public User getCurrentUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
