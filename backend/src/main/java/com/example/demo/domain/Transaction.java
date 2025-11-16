package com.example.demo.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Transaction domain entity
 * Represents a financial transaction (deposit, withdrawal, transfer)
 */
public class Transaction {
    private final Long id;
    private final Long accountId;
    private final TransactionType type;
    private final TransactionCategory category;
    private final BigDecimal amount;
    private final BigDecimal balanceAfter;
    private final String description;
    private final Long relatedAccountId; // For transfers
    private final LocalDateTime createdAt;

    public enum TransactionType {
        DEPOSIT, WITHDRAWAL, TRANSFER_IN, TRANSFER_OUT
    }

    public enum TransactionCategory {
        SALARY,           // Income
        INVESTMENT,       // Income
        REFUND,          // Income
        GROCERIES,       // Expense
        UTILITIES,       // Expense
        RENT,            // Expense
        ENTERTAINMENT,   // Expense
        HEALTHCARE,      // Expense
        TRANSPORTATION,  // Expense
        SHOPPING,        // Expense
        DINING,          // Expense
        TRANSFER,        // Transfer between accounts
        OTHER            // Uncategorized
    }

    private Transaction(Long id, Long accountId, TransactionType type, TransactionCategory category,
                       BigDecimal amount, BigDecimal balanceAfter, String description,
                       Long relatedAccountId, LocalDateTime createdAt) {
        this.id = id;
        this.accountId = accountId;
        this.type = type;
        this.category = category != null ? category : TransactionCategory.OTHER;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.description = description;
        this.relatedAccountId = relatedAccountId;
        this.createdAt = createdAt;
    }

    /**
     * Create a deposit transaction
     */
    public static Transaction createDeposit(Long accountId, BigDecimal amount, BigDecimal balanceAfter,
                                           String description, TransactionCategory category) {
        validateAmount(amount);
        validateAccountId(accountId);

        return new Transaction(
            null,
            accountId,
            TransactionType.DEPOSIT,
            category != null ? category : TransactionCategory.OTHER,
            amount,
            balanceAfter,
            description != null ? description : "Deposit",
            null,
            LocalDateTime.now()
        );
    }

    // Convenience method without category
    public static Transaction createDeposit(Long accountId, BigDecimal amount, BigDecimal balanceAfter, String description) {
        return createDeposit(accountId, amount, balanceAfter, description, TransactionCategory.OTHER);
    }

    /**
     * Create a withdrawal transaction
     */
    public static Transaction createWithdrawal(Long accountId, BigDecimal amount, BigDecimal balanceAfter,
                                              String description, TransactionCategory category) {
        validateAmount(amount);
        validateAccountId(accountId);

        return new Transaction(
            null,
            accountId,
            TransactionType.WITHDRAWAL,
            category != null ? category : TransactionCategory.OTHER,
            amount,
            balanceAfter,
            description != null ? description : "Withdrawal",
            null,
            LocalDateTime.now()
        );
    }

    // Convenience method without category
    public static Transaction createWithdrawal(Long accountId, BigDecimal amount, BigDecimal balanceAfter, String description) {
        return createWithdrawal(accountId, amount, balanceAfter, description, TransactionCategory.OTHER);
    }

    /**
     * Create a transfer-out transaction
     */
    public static Transaction createTransferOut(Long accountId, BigDecimal amount, BigDecimal balanceAfter,
                                                Long toAccountId, String description) {
        validateAmount(amount);
        validateAccountId(accountId);
        validateAccountId(toAccountId);

        return new Transaction(
            null,
            accountId,
            TransactionType.TRANSFER_OUT,
            TransactionCategory.TRANSFER,  // Transfers always use TRANSFER category
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
    public static Transaction createTransferIn(Long accountId, BigDecimal amount, BigDecimal balanceAfter,
                                               Long fromAccountId, String description) {
        validateAmount(amount);
        validateAccountId(accountId);
        validateAccountId(fromAccountId);

        return new Transaction(
            null,
            accountId,
            TransactionType.TRANSFER_IN,
            TransactionCategory.TRANSFER,  // Transfers always use TRANSFER category
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
    public static Transaction of(Long id, Long accountId, TransactionType type, TransactionCategory category,
                                 BigDecimal amount, BigDecimal balanceAfter, String description,
                                 Long relatedAccountId, LocalDateTime createdAt) {
        if (id == null) {
            throw new IllegalArgumentException("Transaction ID cannot be null");
        }
        validateAccountId(accountId);
        validateAmount(amount);

        return new Transaction(id, accountId, type, category, amount, balanceAfter, description, relatedAccountId, createdAt);
    }

    private static void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transaction amount must be positive");
        }
    }

    private static void validateAccountId(Long accountId) {
        if (accountId == null) {
            throw new IllegalArgumentException("Account ID cannot be null");
        }
    }

    // Getters
    public Long getId() {
        return id;
    }

    public Long getAccountId() {
        return accountId;
    }

    public TransactionType getType() {
        return type;
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

    public Long getRelatedAccountId() {
        return relatedAccountId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public TransactionCategory getCategory() {
        return category;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", accountId=" + accountId +
                ", type=" + type +
                ", amount=" + amount +
                ", balanceAfter=" + balanceAfter +
                ", description='" + description + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
