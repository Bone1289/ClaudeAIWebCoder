package com.example.demo.adapter.in.graphql.dto;

import com.example.demo.domain.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionDTO(
        UUID id,
        UUID accountId,
        Transaction.TransactionType type,
        UUID categoryId,
        BigDecimal amount,
        BigDecimal balanceAfter,
        String description,
        UUID relatedAccountId,
        LocalDateTime createdAt
) {
    public static TransactionDTO fromDomain(Transaction transaction) {
        return new TransactionDTO(
                transaction.getId(),
                transaction.getAccountId(),
                transaction.getType(),
                transaction.getCategoryId(),
                transaction.getAmount(),
                transaction.getBalanceAfter(),
                transaction.getDescription(),
                transaction.getRelatedAccountId(),
                transaction.getCreatedAt()
        );
    }
}
