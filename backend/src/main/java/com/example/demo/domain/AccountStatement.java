package com.example.demo.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Account Statement domain object
 * Represents a statement of account activity over a time period
 */
public record AccountStatement(
    Account account,
    LocalDateTime startDate,
    LocalDateTime endDate,
    BigDecimal openingBalance,
    BigDecimal closingBalance,
    BigDecimal totalDeposits,
    BigDecimal totalWithdrawals,
    int transactionCount,
    List<Transaction> transactions
) {
    public AccountStatement {
        if (account == null) {
            throw new IllegalArgumentException("Account cannot be null");
        }
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Date range cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }
        if (transactions == null) {
            transactions = List.of();
        }
    }

    /**
     * Calculate net change in balance
     */
    public BigDecimal netChange() {
        return closingBalance.subtract(openingBalance);
    }
}
