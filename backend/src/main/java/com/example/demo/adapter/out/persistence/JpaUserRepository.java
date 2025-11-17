package com.example.demo.adapter.out.persistence;

import com.example.demo.adapter.out.persistence.entity.UserJpaEntity;
import com.example.demo.adapter.out.persistence.mapper.UserMapper;
import com.example.demo.adapter.out.persistence.repository.UserJpaRepository;
import com.example.demo.application.ports.out.UserRepository;
import com.example.demo.domain.User;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * JPA implementation of UserRepository (Output Port)
 * This is a persistence adapter in the hexagonal architecture
 * Uses MapStruct for domain ↔ entity conversion
 * Database-agnostic using standard JPA
 */
@Repository
@Primary
public class JpaUserRepository implements UserRepository {

    private final UserJpaRepository jpaRepository;
    private final UserMapper mapper;

    public JpaUserRepository(UserJpaRepository jpaRepository, UserMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public User save(User user) {
        UserJpaEntity entity = mapper.toEntity(user);
        UserJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpaRepository.findByUsername(username)
                .map(mapper::toDomain);
    }

    @Override
    public List<User> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public User update(User user) {
        if (user.getId() == null || !jpaRepository.existsById(user.getId())) {
            throw new IllegalArgumentException("Cannot update user: user not found");
        }
        UserJpaEntity entity = mapper.toEntity(user);
        UserJpaEntity updated = jpaRepository.save(entity);
        return mapper.toDomain(updated);
    }

    @Override
    public boolean deleteById(UUID id) {
        if (jpaRepository.existsById(id)) {
            jpaRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpaRepository.existsByUsername(username);
    }
}
