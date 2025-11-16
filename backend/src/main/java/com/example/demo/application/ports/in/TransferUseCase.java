package com.example.demo.application.ports.in;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Input port for transferring money between accounts
 */
public interface TransferUseCase {
    void transfer(UUID fromAccountId, UUID toAccountId, BigDecimal amount, String description);
}
