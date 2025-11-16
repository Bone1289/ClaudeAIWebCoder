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
    private final String firstName;
    private final String lastName;
    private final String nationality;
    private final String accountType;
    private final BigDecimal balance;
    private final AccountStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public enum AccountStatus {
        ACTIVE, SUSPENDED, CLOSED
    }

    private Account(Long id, String accountNumber, String firstName, String lastName, String nationality,
                   String accountType, BigDecimal balance, AccountStatus status,
                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.nationality = nationality;
        this.accountType = accountType;
        this.balance = balance;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Create a new account
     */
    public static Account create(String accountNumber, String firstName, String lastName,
                                String nationality, String accountType) {
        validateAccountType(accountType);
        validateName(firstName, "First name");
        validateName(lastName, "Last name");
        validateNationality(nationality);

        return new Account(
            null,
            accountNumber,
            firstName,
            lastName,
            nationality,
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
    public static Account of(Long id, String accountNumber, String firstName, String lastName,
                            String nationality, String accountType, BigDecimal balance,
                            AccountStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        if (id == null) {
            throw new IllegalArgumentException("Account ID cannot be null");
        }
        validateAccountNumber(accountNumber);
        validateName(firstName, "First name");
        validateName(lastName, "Last name");
        validateNationality(nationality);
        validateAccountType(accountType);
        validateBalance(balance);

        return new Account(id, accountNumber, firstName, lastName, nationality, accountType, balance, status, createdAt, updatedAt);
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
        return new Account(id, accountNumber, firstName, lastName, nationality, accountType, newBalance, status, createdAt, LocalDateTime.now());
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

        return new Account(id, accountNumber, firstName, lastName, nationality, accountType, newBalance, status, createdAt, LocalDateTime.now());
    }

    /**
     * Suspend account
     */
    public Account suspend() {
        if (status == AccountStatus.CLOSED) {
            throw new IllegalStateException("Cannot suspend a closed account");
        }
        return new Account(id, accountNumber, firstName, lastName, nationality, accountType, balance, AccountStatus.SUSPENDED, createdAt, LocalDateTime.now());
    }

    /**
     * Close account
     */
    public Account close() {
        if (balance.compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalStateException("Cannot close account with non-zero balance");
        }
        return new Account(id, accountNumber, firstName, lastName, nationality, accountType, balance, AccountStatus.CLOSED, createdAt, LocalDateTime.now());
    }

    /**
     * Update account details (account type only - number and customer cannot be changed)
     */
    public Account updateAccountType(String newAccountType) {
        validateAccountType(newAccountType);
        if (status == AccountStatus.CLOSED) {
            throw new IllegalStateException("Cannot update a closed account");
        }
        return new Account(id, accountNumber, firstName, lastName, nationality, newAccountType, balance, status, createdAt, LocalDateTime.now());
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
        if (!accountType.equals("CHECKING") && !accountType.equals("SAVINGS") && !accountType.equals("CREDIT")) {
            throw new IllegalArgumentException("Account type must be CHECKING, SAVINGS, or CREDIT");
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

    private static void validateName(String name, String fieldName) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or empty");
        }
    }

    private static void validateNationality(String nationality) {
        if (nationality == null || nationality.trim().isEmpty()) {
            throw new IllegalArgumentException("Nationality cannot be null or empty");
        }
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getNationality() {
        return nationality;
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
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", nationality='" + nationality + '\'' +
                ", accountType='" + accountType + '\'' +
                ", balance=" + balance +
                ", status=" + status +
                '}';
    }
}
