package com.example.demo.application.ports.in;

import com.example.demo.domain.Account;

import java.math.BigDecimal;

/**
 * Input port for depositing money into an account
 */
public interface DepositUseCase {
    Account deposit(Long accountId, BigDecimal amount, String description);
}
