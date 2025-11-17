package com.example.demo.application.ports.in;

import com.example.demo.domain.Account;

import java.util.UUID;

/**
 * Input port for creating a new bank account
 */
public interface CreateAccountUseCase {
    Account createAccount(UUID userId, String firstName, String lastName, String nationality, String accountType);
}
