package com.example.demo.application.ports.in;

import com.example.demo.domain.Account;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Input port for withdrawing money from an account
 * Now uses categoryId instead of enum
 */
public interface WithdrawUseCase {
    Account withdraw(UUID accountId, BigDecimal amount, String description);

    Account withdraw(UUID accountId, BigDecimal amount, String description, UUID categoryId);
}
