package com.example.demo.application.service;

import com.example.demo.application.ports.in.CreateUserUseCase;
import com.example.demo.application.ports.in.DeleteUserUseCase;
import com.example.demo.application.ports.in.GetUserUseCase;
import com.example.demo.application.ports.in.UpdateUserUseCase;
import com.example.demo.application.ports.out.UserRepository;
import com.example.demo.domain.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Application service implementing all user use cases
 * This is the core of the hexagonal architecture - contains business logic
 * It depends on ports (interfaces) not on concrete implementations
 */
@Service
public class UserManagementService implements
        CreateUserUseCase,
        GetUserUseCase,
        UpdateUserUseCase,
        DeleteUserUseCase {

    private final UserRepository userRepository;

    public UserManagementService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User createUser(String name, String email, String role) {
        // Create domain object using factory method (includes validation)
        User user = User.create(name, email, role);

        // Persist using output port
        return userRepository.save(user);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> updateUser(Long id, String name, String email, String role) {
        return userRepository.findById(id)
                .map(existingUser -> {
                    // Use domain method to create updated user (includes validation)
                    User updatedUser = existingUser.update(name, email, role);
                    return userRepository.update(updatedUser);
                });
    }

    @Override
    public boolean deleteUser(Long id) {
        return userRepository.deleteById(id);
    }
}
