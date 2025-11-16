package com.example.demo.application.ports.out;

import com.example.demo.domain.Account;

import java.util.List;
import java.util.Optional;

/**
 * Output port for account persistence
 */
public interface AccountRepository {
    Account save(Account account);
    Optional<Account> findById(Long id);
    Optional<Account> findByAccountNumber(String accountNumber);
    List<Account> findAll();
    Account update(Account account);
    boolean deleteById(Long id);
    String generateAccountNumber();
}
