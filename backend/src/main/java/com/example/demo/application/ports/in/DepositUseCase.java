package com.example.demo.application.ports.in;

import com.example.demo.domain.Account;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Input port for depositing money into an account
 * Now uses categoryId instead of enum
 */
public interface DepositUseCase {
    Account deposit(UUID accountId, BigDecimal amount, String description);

    Account deposit(UUID accountId, BigDecimal amount, String description, UUID categoryId);
}
