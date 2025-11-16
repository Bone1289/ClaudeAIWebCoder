package com.example.demo.application.ports.in;

import com.example.demo.domain.Account;

import java.util.Optional;

/**
 * Input port for updating an account
 * This defines the contract for the use case
 */
public interface UpdateAccountUseCase {
    Optional<Account> updateAccount(Long id, String accountType);
}
