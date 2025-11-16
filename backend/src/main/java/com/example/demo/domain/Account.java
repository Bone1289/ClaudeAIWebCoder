package com.example.demo.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Account domain entity for the virtual bank
 * Represents a bank account with balance and transaction capabilities
 */
public class Account {
    private final Long id;
    private final String accountNumber;
    private final Long customerId;
    private final String accountType;
    private final BigDecimal balance;
    private final AccountStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public enum AccountStatus {
        ACTIVE, SUSPENDED, CLOSED
    }

    private Account(Long id, String accountNumber, Long customerId, String accountType,
                   BigDecimal balance, AccountStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.customerId = customerId;
        this.accountType = accountType;
        this.balance = balance;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Create a new account
     */
    public static Account create(String accountNumber, Long customerId, String accountType) {
        validateAccountType(accountType);

        return new Account(
            null,
            accountNumber,
            customerId,
            accountType,
            BigDecimal.ZERO,
            AccountStatus.ACTIVE,
            LocalDateTime.now(),
            LocalDateTime.now()
        );
    }

    /**
     * Reconstitute account from persistence
     */
    public static Account of(Long id, String accountNumber, Long customerId, String accountType,
                            BigDecimal balance, AccountStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        if (id == null) {
            throw new IllegalArgumentException("Account ID cannot be null");
        }
        validateAccountNumber(accountNumber);
        validateAccountType(accountType);
        validateBalance(balance);

        return new Account(id, accountNumber, customerId, accountType, balance, status, createdAt, updatedAt);
    }

    /**
     * Deposit money into account
     */
    public Account deposit(BigDecimal amount) {
        if (status != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Cannot deposit to inactive account");
        }
        validatePositiveAmount(amount);

        BigDecimal newBalance = this.balance.add(amount);
        return new Account(id, accountNumber, customerId, accountType, newBalance, status, createdAt, LocalDateTime.now());
    }

    /**
     * Withdraw money from account
     */
    public Account withdraw(BigDecimal amount) {
        if (status != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Cannot withdraw from inactive account");
        }
        validatePositiveAmount(amount);

        BigDecimal newBalance = this.balance.subtract(amount);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Insufficient funds. Current balance: " + this.balance);
        }

        return new Account(id, accountNumber, customerId, accountType, newBalance, status, createdAt, LocalDateTime.now());
    }

    /**
     * Suspend account
     */
    public Account suspend() {
        if (status == AccountStatus.CLOSED) {
            throw new IllegalStateException("Cannot suspend a closed account");
        }
        return new Account(id, accountNumber, customerId, accountType, balance, AccountStatus.SUSPENDED, createdAt, LocalDateTime.now());
    }

    /**
     * Close account
     */
    public Account close() {
        if (balance.compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalStateException("Cannot close account with non-zero balance");
        }
        return new Account(id, accountNumber, customerId, accountType, balance, AccountStatus.CLOSED, createdAt, LocalDateTime.now());
    }

    // Validation methods
    private static void validateAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Account number cannot be null or empty");
        }
    }

    private static void validateAccountType(String accountType) {
        if (accountType == null || accountType.trim().isEmpty()) {
            throw new IllegalArgumentException("Account type cannot be null or empty");
        }
        if (!accountType.equals("CHECKING") && !accountType.equals("SAVINGS")) {
            throw new IllegalArgumentException("Account type must be CHECKING or SAVINGS");
        }
    }

    private static void validateBalance(BigDecimal balance) {
        if (balance == null) {
            throw new IllegalArgumentException("Balance cannot be null");
        }
        if (balance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Balance cannot be negative");
        }
    }

    private void validatePositiveAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public String getAccountType() {
        return accountType;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return id != null && id.equals(account.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", accountNumber='" + accountNumber + '\'' +
                ", customerId=" + customerId +
                ", accountType='" + accountType + '\'' +
                ", balance=" + balance +
                ", status=" + status +
                '}';
    }
}
