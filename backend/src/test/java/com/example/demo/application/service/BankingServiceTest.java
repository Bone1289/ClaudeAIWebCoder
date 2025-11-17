package com.example.demo.application.service;

import com.example.demo.application.ports.out.AccountRepository;
import com.example.demo.application.ports.out.CategoryRepository;
import com.example.demo.application.ports.out.TransactionRepository;
import com.example.demo.domain.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BankingService
 * Uses Mockito to mock dependencies
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BankingService Tests")
class BankingServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    private BankingService bankingService;

    @BeforeEach
    void setUp() {
        bankingService = new BankingService(
            accountRepository,
            transactionRepository,
            categoryRepository
        );
    }

    @Nested
    @DisplayName("Create Account Tests")
    class CreateAccountTests {

        @Test
        @DisplayName("Should create account successfully")
        void shouldCreateAccountSuccessfully() {
            // Given
            String accountNumber = "ACC001";
            when(accountRepository.generateAccountNumber()).thenReturn(accountNumber);

            Account expectedAccount = Account.create(
                accountNumber,
                "John",
                "Doe",
                "United States",
                "CHECKING"
            );
            when(accountRepository.save(any(Account.class))).thenReturn(expectedAccount);

            // When
            Account result = bankingService.createAccount(
                "John",
                "Doe",
                "United States",
                "CHECKING"
            );

            // Then
            assertNotNull(result);
            assertEquals("John", result.getFirstName());
            assertEquals("Doe", result.getLastName());
            assertEquals("United States", result.getNationality());
            assertEquals("CHECKING", result.getAccountType());

            verify(accountRepository).generateAccountNumber();
            verify(accountRepository).save(any(Account.class));
        }

        @Test
        @DisplayName("Should throw exception when creating account with invalid account type")
        void shouldThrowExceptionWhenCreatingAccountWithInvalidAccountType() {
            // Given
            when(accountRepository.generateAccountNumber()).thenReturn("ACC001");

            // When & Then
            assertThrows(
                IllegalArgumentException.class,
                () -> bankingService.createAccount(
                    "John",
                    "Doe",
                    "United States",
                    "INVALID"
                )
            );

            verify(accountRepository).generateAccountNumber();
            verify(accountRepository, never()).save(any(Account.class));
        }

        @Test
        @DisplayName("Should throw exception when creating account with null name")
        void shouldThrowExceptionWhenCreatingAccountWithNullName() {
            // Given
            when(accountRepository.generateAccountNumber()).thenReturn("ACC001");

            // When & Then
            assertThrows(
                IllegalArgumentException.class,
                () -> bankingService.createAccount(
                    null,
                    "Doe",
                    "United States",
                    "CHECKING"
                )
            );

            verify(accountRepository, never()).save(any(Account.class));
        }
    }

    @Nested
    @DisplayName("Get Account Tests")
    class GetAccountTests {

        @Test
        @DisplayName("Should get all accounts")
        void shouldGetAllAccounts() {
            // Given
            Account account1 = createTestAccount("ACC001", "John", "Doe");
            Account account2 = createTestAccount("ACC002", "Jane", "Smith");
            when(accountRepository.findAll()).thenReturn(Arrays.asList(account1, account2));

            // When
            List<Account> result = bankingService.getAllAccounts();

            // Then
            assertEquals(2, result.size());
            verify(accountRepository).findAll();
        }

        @Test
        @DisplayName("Should get account by ID")
        void shouldGetAccountById() {
            // Given
            UUID accountId = UUID.randomUUID();
            Account account = createTestAccount("ACC001", "John", "Doe");
            when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

            // When
            Optional<Account> result = bankingService.getAccountById(accountId);

            // Then
            assertTrue(result.isPresent());
            assertEquals("John", result.get().getFirstName());
            verify(accountRepository).findById(accountId);
        }

        @Test
        @DisplayName("Should return empty when account not found by ID")
        void shouldReturnEmptyWhenAccountNotFoundById() {
            // Given
            UUID accountId = UUID.randomUUID();
            when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

            // When
            Optional<Account> result = bankingService.getAccountById(accountId);

            // Then
            assertFalse(result.isPresent());
            verify(accountRepository).findById(accountId);
        }

        @Test
        @DisplayName("Should get account by account number")
        void shouldGetAccountByAccountNumber() {
            // Given
            String accountNumber = "ACC001";
            Account account = createTestAccount(accountNumber, "John", "Doe");
            when(accountRepository.findByAccountNumber(accountNumber))
                .thenReturn(Optional.of(account));

            // When
            Optional<Account> result = bankingService.getAccountByAccountNumber(accountNumber);

            // Then
            assertTrue(result.isPresent());
            assertEquals(accountNumber, result.get().getAccountNumber());
            verify(accountRepository).findByAccountNumber(accountNumber);
        }
    }

    @Nested
    @DisplayName("Update Account Tests")
    class UpdateAccountTests {

        @Test
        @DisplayName("Should update account type successfully")
        void shouldUpdateAccountTypeSuccessfully() {
            // Given
            UUID accountId = UUID.randomUUID();
            Account existingAccount = createTestAccount("ACC001", "John", "Doe");
            Account updatedAccount = existingAccount.updateAccountType("SAVINGS");

            when(accountRepository.findById(accountId)).thenReturn(Optional.of(existingAccount));
            when(accountRepository.update(any(Account.class))).thenReturn(updatedAccount);

            // When
            Optional<Account> result = bankingService.updateAccount(accountId, "SAVINGS");

            // Then
            assertTrue(result.isPresent());
            assertEquals("SAVINGS", result.get().getAccountType());
            verify(accountRepository).findById(accountId);
            verify(accountRepository).update(any(Account.class));
        }

        @Test
        @DisplayName("Should return empty when updating non-existent account")
        void shouldReturnEmptyWhenUpdatingNonExistentAccount() {
            // Given
            UUID accountId = UUID.randomUUID();
            when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

            // When
            Optional<Account> result = bankingService.updateAccount(accountId, "SAVINGS");

            // Then
            assertFalse(result.isPresent());
            verify(accountRepository).findById(accountId);
            verify(accountRepository, never()).update(any(Account.class));
        }
    }

    @Nested
    @DisplayName("Delete Account Tests")
    class DeleteAccountTests {

        @Test
        @DisplayName("Should delete account with zero balance")
        void shouldDeleteAccountWithZeroBalance() {
            // Given
            UUID accountId = UUID.randomUUID();
            Account account = createTestAccount("ACC001", "John", "Doe");
            // Account has zero balance by default

            when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
            when(accountRepository.deleteById(accountId)).thenReturn(true);

            // When
            boolean result = bankingService.deleteAccount(accountId);

            // Then
            assertTrue(result);
            verify(accountRepository).findById(accountId);
            verify(accountRepository).deleteById(accountId);
        }

        @Test
        @DisplayName("Should throw exception when deleting account with non-zero balance")
        void shouldThrowExceptionWhenDeletingAccountWithNonZeroBalance() {
            // Given
            UUID accountId = UUID.randomUUID();
            Account account = createTestAccount("ACC001", "John", "Doe")
                .deposit(new BigDecimal("1000.00"));

            when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

            // When & Then
            IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> bankingService.deleteAccount(accountId)
            );

            assertTrue(exception.getMessage().contains("Cannot delete account with non-zero balance"));
            verify(accountRepository).findById(accountId);
            verify(accountRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("Should return false when deleting non-existent account")
        void shouldReturnFalseWhenDeletingNonExistentAccount() {
            // Given
            UUID accountId = UUID.randomUUID();
            when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

            // When
            boolean result = bankingService.deleteAccount(accountId);

            // Then
            assertFalse(result);
            verify(accountRepository).findById(accountId);
            verify(accountRepository, never()).deleteById(any());
        }
    }

    @Nested
    @DisplayName("Deposit Tests")
    class DepositTests {

        @Test
        @DisplayName("Should deposit money successfully")
        void shouldDepositMoneySuccessfully() {
            // Given
            UUID accountId = UUID.randomUUID();
            Account account = createTestAccount("ACC001", "John", "Doe");
            Account accountAfterDeposit = account.deposit(new BigDecimal("500.00"));

            when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
            when(accountRepository.update(any(Account.class))).thenReturn(accountAfterDeposit);
            when(transactionRepository.save(any())).thenReturn(null);

            // When
            Account result = bankingService.deposit(accountId, new BigDecimal("500.00"));

            // Then
            assertEquals(new BigDecimal("500.00"), result.getBalance());
            verify(accountRepository).findById(accountId);
            verify(accountRepository).update(any(Account.class));
        }

        @Test
        @DisplayName("Should throw exception when depositing to non-existent account")
        void shouldThrowExceptionWhenDepositingToNonExistentAccount() {
            // Given
            UUID accountId = UUID.randomUUID();
            when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bankingService.deposit(accountId, new BigDecimal("500.00"))
            );

            assertEquals("Account not found", exception.getMessage());
            verify(accountRepository).findById(accountId);
            verify(accountRepository, never()).update(any(Account.class));
        }

        @Test
        @DisplayName("Should throw exception when depositing negative amount")
        void shouldThrowExceptionWhenDepositingNegativeAmount() {
            // Given
            UUID accountId = UUID.randomUUID();
            Account account = createTestAccount("ACC001", "John", "Doe");
            when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

            // When & Then
            assertThrows(
                IllegalArgumentException.class,
                () -> bankingService.deposit(accountId, new BigDecimal("-100.00"))
            );

            verify(accountRepository, never()).update(any(Account.class));
        }
    }

    // Helper method
    private Account createTestAccount(String accountNumber, String firstName, String lastName) {
        return Account.create(
            accountNumber,
            firstName,
            lastName,
            "United States",
            "CHECKING"
        );
    }
}
