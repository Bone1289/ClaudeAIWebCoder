package com.example.demo.application.ports.in;

import com.example.demo.domain.Account;

/**
 * Input port for creating a new bank account
 */
public interface CreateAccountUseCase {
    Account createAccount(Long customerId, String accountType);
}
