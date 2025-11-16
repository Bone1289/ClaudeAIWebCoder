package com.example.demo.adapter.out.persistence;

import com.example.demo.adapter.out.persistence.entity.TransactionJpaEntity;
import com.example.demo.adapter.out.persistence.mapper.TransactionMapper;
import com.example.demo.adapter.out.persistence.repository.TransactionJpaRepository;
import com.example.demo.application.ports.out.TransactionRepository;
import com.example.demo.domain.Transaction;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * JPA implementation of TransactionRepository (Output Port)
 * This is a persistence adapter in the hexagonal architecture
 * Uses MapStruct for domain ↔ entity conversion
 * Database-agnostic using standard JPA
 */
@Repository
@Primary  // This makes it the default implementation
public class JpaTransactionRepository implements TransactionRepository {

    private final TransactionJpaRepository jpaRepository;
    private final TransactionMapper mapper;

    public JpaTransactionRepository(TransactionJpaRepository jpaRepository, TransactionMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Transaction save(Transaction transaction) {
        TransactionJpaEntity entity = mapper.toEntity(transaction);
        TransactionJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public List<Transaction> findByAccountId(Long accountId) {
        return jpaRepository.findByAccountIdOrderByCreatedAtDesc(accountId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Transaction> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .sorted((t1, t2) -> t2.getCreatedAt().compareTo(t1.getCreatedAt()))
                .collect(Collectors.toList());
    }
}
