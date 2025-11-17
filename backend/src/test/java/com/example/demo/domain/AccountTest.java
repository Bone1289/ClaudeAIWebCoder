package com.example.demo.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Account domain entity
 */
@DisplayName("Account Domain Tests")
class AccountTest {

    @Nested
    @DisplayName("Account Creation Tests")
    class AccountCreationTests {

        @Test
        @DisplayName("Should create new account with valid data")
        void shouldCreateNewAccountWithValidData() {
            // When
            Account account = Account.create(
                "ACC001",
                "John",
                "Doe",
                "United States",
                "CHECKING"
            );

            // Then
            assertNotNull(account);
            assertEquals("ACC001", account.getAccountNumber());
            assertEquals("John", account.getFirstName());
            assertEquals("Doe", account.getLastName());
            assertEquals("United States", account.getNationality());
            assertEquals("CHECKING", account.getAccountType());
            assertEquals(BigDecimal.ZERO, account.getBalance());
            assertEquals(Account.AccountStatus.ACTIVE, account.getStatus());
            assertNull(account.getId()); // ID is null before persistence
        }

        @Test
        @DisplayName("Should throw exception when first name is null")
        void shouldThrowExceptionWhenFirstNameIsNull() {
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Account.create("ACC001", null, "Doe", "USA", "CHECKING")
            );
            assertEquals("First name cannot be null or empty", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when last name is empty")
        void shouldThrowExceptionWhenLastNameIsEmpty() {
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Account.create("ACC001", "John", "  ", "USA", "CHECKING")
            );
            assertEquals("Last name cannot be null or empty", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when nationality is null")
        void shouldThrowExceptionWhenNationalityIsNull() {
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Account.create("ACC001", "John", "Doe", null, "CHECKING")
            );
            assertEquals("Nationality cannot be null or empty", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for invalid account type")
        void shouldThrowExceptionForInvalidAccountType() {
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Account.create("ACC001", "John", "Doe", "USA", "INVALID")
            );
            assertEquals("Account type must be CHECKING, SAVINGS, or CREDIT", exception.getMessage());
        }

        @Test
        @DisplayName("Should accept all valid account types")
        void shouldAcceptAllValidAccountTypes() {
            // When & Then
            assertDoesNotThrow(() -> Account.create("ACC001", "John", "Doe", "USA", "CHECKING"));
            assertDoesNotThrow(() -> Account.create("ACC002", "Jane", "Smith", "UK", "SAVINGS"));
            assertDoesNotThrow(() -> Account.create("ACC003", "Bob", "Wilson", "Canada", "CREDIT"));
        }
    }

    @Nested
    @DisplayName("Deposit Tests")
    class DepositTests {

        @Test
        @DisplayName("Should deposit money to active account")
        void shouldDepositMoneyToActiveAccount() {
            // Given
            Account account = createTestAccount();

            // When
            Account updatedAccount = account.deposit(new BigDecimal("1000.00"));

            // Then
            assertEquals(new BigDecimal("1000.00"), updatedAccount.getBalance());
        }

        @Test
        @DisplayName("Should accumulate multiple deposits")
        void shouldAccumulateMultipleDeposits() {
            // Given
            Account account = createTestAccount();

            // When
            Account updated1 = account.deposit(new BigDecimal("500.00"));
            Account updated2 = updated1.deposit(new BigDecimal("300.50"));

            // Then
            assertEquals(new BigDecimal("800.50"), updated2.getBalance());
        }

        @Test
        @DisplayName("Should throw exception for negative deposit")
        void shouldThrowExceptionForNegativeDeposit() {
            // Given
            Account account = createTestAccount();

            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> account.deposit(new BigDecimal("-100.00"))
            );
            assertEquals("Amount must be positive", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for zero deposit")
        void shouldThrowExceptionForZeroDeposit() {
            // Given
            Account account = createTestAccount();

            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> account.deposit(BigDecimal.ZERO)
            );
            assertEquals("Amount must be positive", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when depositing to suspended account")
        void shouldThrowExceptionWhenDepositingToSuspendedAccount() {
            // Given
            Account account = createTestAccount().suspend();

            // When & Then
            IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> account.deposit(new BigDecimal("100.00"))
            );
            assertEquals("Cannot deposit to inactive account", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Withdrawal Tests")
    class WithdrawalTests {

        @Test
        @DisplayName("Should withdraw money from account with sufficient balance")
        void shouldWithdrawMoneyWithSufficientBalance() {
            // Given
            Account account = createTestAccount()
                .deposit(new BigDecimal("1000.00"));

            // When
            Account updatedAccount = account.withdraw(new BigDecimal("300.00"));

            // Then
            assertEquals(new BigDecimal("700.00"), updatedAccount.getBalance());
        }

        @Test
        @DisplayName("Should throw exception for insufficient funds")
        void shouldThrowExceptionForInsufficientFunds() {
            // Given
            Account account = createTestAccount()
                .deposit(new BigDecimal("100.00"));

            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> account.withdraw(new BigDecimal("200.00"))
            );
            assertTrue(exception.getMessage().contains("Insufficient funds"));
        }

        @Test
        @DisplayName("Should throw exception for negative withdrawal")
        void shouldThrowExceptionForNegativeWithdrawal() {
            // Given
            Account account = createTestAccount();

            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> account.withdraw(new BigDecimal("-50.00"))
            );
            assertEquals("Amount must be positive", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when withdrawing from suspended account")
        void shouldThrowExceptionWhenWithdrawingFromSuspendedAccount() {
            // Given
            Account account = createTestAccount()
                .deposit(new BigDecimal("1000.00"))
                .suspend();

            // When & Then
            IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> account.withdraw(new BigDecimal("100.00"))
            );
            assertEquals("Cannot withdraw from inactive account", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Account Status Tests")
    class AccountStatusTests {

        @Test
        @DisplayName("Should suspend active account")
        void shouldSuspendActiveAccount() {
            // Given
            Account account = createTestAccount();

            // When
            Account suspendedAccount = account.suspend();

            // Then
            assertEquals(Account.AccountStatus.SUSPENDED, suspendedAccount.getStatus());
        }

        @Test
        @DisplayName("Should throw exception when suspending closed account")
        void shouldThrowExceptionWhenSuspendingClosedAccount() {
            // Given
            Account account = createTestAccount().close();

            // When & Then
            IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                account::suspend
            );
            assertEquals("Cannot suspend a closed account", exception.getMessage());
        }

        @Test
        @DisplayName("Should close account with zero balance")
        void shouldCloseAccountWithZeroBalance() {
            // Given
            Account account = createTestAccount();

            // When
            Account closedAccount = account.close();

            // Then
            assertEquals(Account.AccountStatus.CLOSED, closedAccount.getStatus());
        }

        @Test
        @DisplayName("Should throw exception when closing account with non-zero balance")
        void shouldThrowExceptionWhenClosingAccountWithNonZeroBalance() {
            // Given
            Account account = createTestAccount()
                .deposit(new BigDecimal("100.00"));

            // When & Then
            IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                account::close
            );
            assertEquals("Cannot close account with non-zero balance", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Account Update Tests")
    class AccountUpdateTests {

        @Test
        @DisplayName("Should update account type")
        void shouldUpdateAccountType() {
            // Given
            Account account = createTestAccount();

            // When
            Account updatedAccount = account.updateAccountType("SAVINGS");

            // Then
            assertEquals("SAVINGS", updatedAccount.getAccountType());
        }

        @Test
        @DisplayName("Should throw exception when updating closed account")
        void shouldThrowExceptionWhenUpdatingClosedAccount() {
            // Given
            Account account = createTestAccount().close();

            // When & Then
            IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> account.updateAccountType("SAVINGS")
            );
            assertEquals("Cannot update a closed account", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for invalid account type update")
        void shouldThrowExceptionForInvalidAccountTypeUpdate() {
            // Given
            Account account = createTestAccount();

            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> account.updateAccountType("INVALID")
            );
            assertEquals("Account type must be CHECKING, SAVINGS, or CREDIT", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Account Reconstitution Tests")
    class AccountReconstitutionTests {

        @Test
        @DisplayName("Should reconstitute account from persistence")
        void shouldReconstituteAccountFromPersistence() {
            // Given
            UUID id = UUID.randomUUID();
            LocalDateTime now = LocalDateTime.now();

            // When
            Account account = Account.of(
                id,
                "ACC001",
                "John",
                "Doe",
                "United States",
                "CHECKING",
                new BigDecimal("5000.00"),
                Account.AccountStatus.ACTIVE,
                now,
                now
            );

            // Then
            assertNotNull(account);
            assertEquals(id, account.getId());
            assertEquals("ACC001", account.getAccountNumber());
            assertEquals(new BigDecimal("5000.00"), account.getBalance());
            assertEquals(Account.AccountStatus.ACTIVE, account.getStatus());
        }

        @Test
        @DisplayName("Should throw exception when reconstituting with null ID")
        void shouldThrowExceptionWhenReconstitutingWithNullId() {
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Account.of(
                    null,
                    "ACC001",
                    "John",
                    "Doe",
                    "USA",
                    "CHECKING",
                    BigDecimal.ZERO,
                    Account.AccountStatus.ACTIVE,
                    LocalDateTime.now(),
                    LocalDateTime.now()
                )
            );
            assertEquals("Account ID cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when reconstituting with negative balance")
        void shouldThrowExceptionWhenReconstitutingWithNegativeBalance() {
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Account.of(
                    UUID.randomUUID(),
                    "ACC001",
                    "John",
                    "Doe",
                    "USA",
                    "CHECKING",
                    new BigDecimal("-100.00"),
                    Account.AccountStatus.ACTIVE,
                    LocalDateTime.now(),
                    LocalDateTime.now()
                )
            );
            assertEquals("Balance cannot be negative", exception.getMessage());
        }
    }

    // Helper method
    private Account createTestAccount() {
        return Account.create(
            "ACC" + System.currentTimeMillis(),
            "John",
            "Doe",
            "United States",
            "CHECKING"
        );
    }
}
