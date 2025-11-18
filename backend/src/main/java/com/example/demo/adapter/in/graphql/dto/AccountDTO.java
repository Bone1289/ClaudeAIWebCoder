package com.example.demo.adapter.in.graphql.dto;

import com.example.demo.domain.Account;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record AccountDTO(
        UUID id,
        UUID userId,
        String accountNumber,
        String firstName,
        String lastName,
        String nationality,
        String accountType,
        BigDecimal balance,
        Account.AccountStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static AccountDTO fromDomain(Account account) {
        return new AccountDTO(
                account.getId(),
                account.getUserId(),
                account.getAccountNumber(),
                account.getFirstName(),
                account.getLastName(),
                account.getNationality(),
                account.getAccountType(),
                account.getBalance(),
                account.getStatus(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }
}
