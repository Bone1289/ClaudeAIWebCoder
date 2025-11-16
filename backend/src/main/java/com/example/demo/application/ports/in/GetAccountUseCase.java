package com.example.demo.application.ports.in;

import com.example.demo.domain.Account;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Input port for retrieving account information
 */
public interface GetAccountUseCase {
    List<Account> getAllAccounts();
    Optional<Account> getAccountById(UUID id);
    Optional<Account> getAccountByAccountNumber(String accountNumber);
}
