package com.example.demo.application.ports.in;

import java.math.BigDecimal;

/**
 * Input port for transferring money between accounts
 */
public interface TransferUseCase {
    void transfer(Long fromAccountId, Long toAccountId, BigDecimal amount, String description);
}
