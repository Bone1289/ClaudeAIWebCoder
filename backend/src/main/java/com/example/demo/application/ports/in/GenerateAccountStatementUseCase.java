package com.example.demo.application.ports.in;

import com.example.demo.domain.AccountStatement;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Input port for generating account statements
 */
public interface GenerateAccountStatementUseCase {
    /**
     * Generate account statement for a date range
     * @param accountId Account ID
     * @param startDate Start of period
     * @param endDate End of period
     * @return Account statement with transactions
     */
    AccountStatement generateStatement(UUID accountId, LocalDateTime startDate, LocalDateTime endDate);
}
