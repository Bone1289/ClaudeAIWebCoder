package com.example.demo.application.ports.out;

import com.example.demo.domain.Account;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Output port for account persistence
 */
public interface AccountRepository {
    Account save(Account account);
    Optional<Account> findById(UUID id);
    Optional<Account> findByAccountNumber(String accountNumber);
    List<Account> findAll();
    List<Account> findByUserId(UUID userId);
    Account update(Account account);
    boolean deleteById(UUID id);
    String generateAccountNumber();
}
