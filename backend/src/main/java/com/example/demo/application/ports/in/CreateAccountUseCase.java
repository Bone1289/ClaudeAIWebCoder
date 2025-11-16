package com.example.demo.application.ports.in;

import com.example.demo.domain.Account;

/**
 * Input port for creating a new bank account
 */
public interface CreateAccountUseCase {
    Account createAccount(String firstName, String lastName, String nationality, String accountType);
}
