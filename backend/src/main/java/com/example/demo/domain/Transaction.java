package com.example.demo.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Transaction domain entity
 * Represents a financial transaction (deposit, withdrawal, transfer)
 * Now uses TransactionCategory entity reference instead of enum
 */
public class Transaction {
    private final UUID id;
    private final UUID accountId;
    private final TransactionType type;
    private final UUID categoryId;  // Reference to TransactionCategory entity
    private final BigDecimal amount;
    private final BigDecimal balanceAfter;
    private final String description;
    private final UUID relatedAccountId; // For transfers
    private final LocalDateTime createdAt;

    public enum TransactionType {
        DEPOSIT, WITHDRAWAL, TRANSFER_IN, TRANSFER_OUT
    }

    private Transaction(UUID id, UUID accountId, TransactionType type, UUID categoryId,
                       BigDecimal amount, BigDecimal balanceAfter, String description,
                       UUID relatedAccountId, LocalDateTime createdAt) {
        this.id = id;
        this.accountId = accountId;
        this.type = type;
        this.categoryId = categoryId;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.description = description;
        this.relatedAccountId = relatedAccountId;
        this.createdAt = createdAt;
    }

    /**
     * Create a deposit transaction
     */
    public static Transaction createDeposit(UUID accountId, BigDecimal amount, BigDecimal balanceAfter,
                                           String description, UUID categoryId) {
        validateAmount(amount);
        validateAccountId(accountId);

        return new Transaction(
            null,
            accountId,
            TransactionType.DEPOSIT,
            categoryId,
            amount,
            balanceAfter,
            description != null ? description : "Deposit",
            null,
            LocalDateTime.now()
        );
    }

    /**
     * Create a withdrawal transaction
     */
    public static Transaction createWithdrawal(UUID accountId, BigDecimal amount, BigDecimal balanceAfter,
                                              String description, UUID categoryId) {
        validateAmount(amount);
        validateAccountId(accountId);

        return new Transaction(
            null,
            accountId,
            TransactionType.WITHDRAWAL,
            categoryId,
            amount,
            balanceAfter,
            description != null ? description : "Withdrawal",
            null,
            LocalDateTime.now()
        );
    }

    /**
     * Create a transfer-out transaction
     */
    public static Transaction createTransferOut(UUID accountId, BigDecimal amount, BigDecimal balanceAfter,
                                                UUID toAccountId, String description, UUID transferCategoryId) {
        validateAmount(amount);
        validateAccountId(accountId);
        validateAccountId(toAccountId);

        return new Transaction(
            null,
            accountId,
            TransactionType.TRANSFER_OUT,
            transferCategoryId,  // TRANSFER category ID
            amount,
            balanceAfter,
            description != null ? description : "Transfer to account " + toAccountId,
            toAccountId,
            LocalDateTime.now()
        );
    }

    /**
     * Create a transfer-in transaction
     */
    public static Transaction createTransferIn(UUID accountId, BigDecimal amount, BigDecimal balanceAfter,
                                               UUID fromAccountId, String description, UUID transferCategoryId) {
        validateAmount(amount);
        validateAccountId(accountId);
        validateAccountId(fromAccountId);

        return new Transaction(
            null,
            accountId,
            TransactionType.TRANSFER_IN,
            transferCategoryId,  // TRANSFER category ID
            amount,
            balanceAfter,
            description != null ? description : "Transfer from account " + fromAccountId,
            fromAccountId,
            LocalDateTime.now()
        );
    }

    /**
     * Reconstitute transaction from persistence
     */
    public static Transaction of(UUID id, UUID accountId, TransactionType type, UUID categoryId,
                                 BigDecimal amount, BigDecimal balanceAfter, String description,
                                 UUID relatedAccountId, LocalDateTime createdAt) {
        if (id == null) {
            throw new IllegalArgumentException("Transaction ID cannot be null");
        }
        validateAccountId(accountId);
        validateAmount(amount);

        return new Transaction(id, accountId, type, categoryId, amount, balanceAfter, description, relatedAccountId, createdAt);
    }

    private static void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transaction amount must be positive");
        }
    }

    private static void validateAccountId(UUID accountId) {
        if (accountId == null) {
            throw new IllegalArgumentException("Account ID cannot be null");
        }
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public TransactionType getType() {
        return type;
    }

    public UUID getCategoryId() {
        return categoryId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public String getDescription() {
        return description;
    }

    public UUID getRelatedAccountId() {
        return relatedAccountId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", accountId=" + accountId +
                ", type=" + type +
                ", categoryId=" + categoryId +
                ", amount=" + amount +
                ", balanceAfter=" + balanceAfter +
                ", description='" + description + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
