package com.example.demo.application.ports.in;

import com.example.demo.domain.Account;

import java.math.BigDecimal;

/**
 * Input port for withdrawing money from an account
 */
public interface WithdrawUseCase {
    Account withdraw(Long accountId, BigDecimal amount, String description);
}
