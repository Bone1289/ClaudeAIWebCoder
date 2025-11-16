package com.example.demo.adapter.out.persistence;

import com.example.demo.adapter.out.persistence.entity.AccountJpaEntity;
import com.example.demo.adapter.out.persistence.mapper.AccountMapper;
import com.example.demo.adapter.out.persistence.repository.AccountJpaRepository;
import com.example.demo.application.ports.out.AccountRepository;
import com.example.demo.domain.Account;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * JPA implementation of AccountRepository (Output Port)
 * This is a persistence adapter in the hexagonal architecture
 * Uses MapStruct for domain ↔ entity conversion
 * Database-agnostic using standard JPA
 */
@Repository
@Primary  // This makes it the default implementation
public class JpaAccountRepository implements AccountRepository {

    private final AccountJpaRepository jpaRepository;
    private final AccountMapper mapper;
    private final Random random = new Random();

    public JpaAccountRepository(AccountJpaRepository jpaRepository, AccountMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Account save(Account account) {
        AccountJpaEntity entity = mapper.toEntity(account);
        AccountJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Account> findById(Long id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Account> findByAccountNumber(String accountNumber) {
        return jpaRepository.findByAccountNumber(accountNumber)
                .map(mapper::toDomain);
    }

    @Override
    public List<Account> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Account> findByCustomerId(Long customerId) {
        return jpaRepository.findByCustomerId(customerId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Account update(Account account) {
        if (account.getId() == null || !jpaRepository.existsById(account.getId())) {
            throw new IllegalArgumentException("Cannot update account: account not found");
        }
        AccountJpaEntity entity = mapper.toEntity(account);
        AccountJpaEntity updated = jpaRepository.save(entity);
        return mapper.toDomain(updated);
    }

    @Override
    public boolean deleteById(Long id) {
        if (jpaRepository.existsById(id)) {
            jpaRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public String generateAccountNumber() {
        String accountNumber;
        do {
            // Generate account number: ACC + 9 random digits
            accountNumber = "ACC" + String.format("%09d", 100000000 + random.nextInt(900000000));
        } while (jpaRepository.existsByAccountNumber(accountNumber));
        return accountNumber;
    }
}
