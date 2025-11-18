package com.example.demo.adapter.in.graphql.dto;

import com.example.demo.domain.AccountStatement;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public record AccountStatementDTO(
        AccountDTO account,
        LocalDate startDate,
        LocalDate endDate,
        List<TransactionDTO> transactions,
        BigDecimal totalDeposits,
        BigDecimal totalWithdrawals,
        BigDecimal netChange,
        BigDecimal startingBalance,
        BigDecimal endingBalance
) {
    public static AccountStatementDTO fromDomain(AccountStatement statement) {
        return new AccountStatementDTO(
                AccountDTO.fromDomain(statement.account()),
                statement.startDate().toLocalDate(),
                statement.endDate().toLocalDate(),
                statement.transactions().stream()
                        .map(TransactionDTO::fromDomain)
                        .collect(Collectors.toList()),
                statement.totalDeposits(),
                statement.totalWithdrawals(),
                statement.netChange(),
                statement.openingBalance(),
                statement.closingBalance()
        );
    }
}
