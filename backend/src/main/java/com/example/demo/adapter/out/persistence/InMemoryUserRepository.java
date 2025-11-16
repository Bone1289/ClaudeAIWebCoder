package com.example.demo.adapter.out.persistence;

import com.example.demo.application.ports.out.UserRepository;
import com.example.demo.domain.User;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-Memory persistence adapter implementing UserRepository port
 * This is an output adapter that implements the persistence logic
 * It can be easily replaced with a database implementation without changing the core logic
 */
@Repository
public class InMemoryUserRepository implements UserRepository {

    private final Map<Long, User> users = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    public InMemoryUserRepository() {
        // Initialize with sample data
        User user1 = User.of(idCounter.getAndIncrement(), "John Doe", "john@example.com", "USER");
        User user2 = User.of(idCounter.getAndIncrement(), "Jane Smith", "jane@example.com", "ADMIN");
        User user3 = User.of(idCounter.getAndIncrement(), "Bob Johnson", "bob@example.com", "USER");

        users.put(user1.getId(), user1);
        users.put(user2.getId(), user2);
        users.put(user3.getId(), user3);
    }

    @Override
    public User save(User user) {
        Long id = idCounter.getAndIncrement();
        User savedUser = User.of(id, user.getName(), user.getEmail(), user.getRole());
        users.put(id, savedUser);
        return savedUser;
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public User update(User user) {
        if (user.getId() == null || !users.containsKey(user.getId())) {
            throw new IllegalArgumentException("Cannot update user: user not found");
        }
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public boolean deleteById(Long id) {
        return users.remove(id) != null;
    }

    @Override
    public boolean existsById(Long id) {
        return users.containsKey(id);
    }
}
