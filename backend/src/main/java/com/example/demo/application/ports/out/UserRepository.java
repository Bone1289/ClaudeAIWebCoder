package com.example.demo.application.ports.out;

import com.example.demo.domain.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Output port for user persistence
 */
public interface UserRepository {
    User save(User user);
    Optional<User> findById(UUID id);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    List<User> findAll();
    User update(User user);
    boolean deleteById(UUID id);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}
