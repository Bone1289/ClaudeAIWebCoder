package com.example.demo.application.service;

import com.example.demo.application.ports.in.*;
import com.example.demo.application.ports.out.AccountRepository;
import com.example.demo.application.ports.out.CategoryRepository;
import com.example.demo.application.ports.out.TransactionRepository;
import com.example.demo.application.ports.out.UserRepository;
import com.example.demo.domain.Account;
import com.example.demo.domain.AuditLog;
import com.example.demo.domain.Transaction;
import com.example.demo.domain.TransactionCategory;
import com.example.demo.domain.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Banking service implementing all banking use cases
 * This is the core application service containing the business logic
 * Now fetches categories from database instead of using enums
 */
@Service
@Transactional
public class BankingService implements
        CreateAccountUseCase,
        GetAccountUseCase,
        UpdateAccountUseCase,
        DeleteAccountUseCase,
        DepositUseCase,
        WithdrawUseCase,
        TransferUseCase,
        GetTransactionHistoryUseCase {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final AuditService auditService;
    private final UserRepository userRepository;

    public BankingService(AccountRepository accountRepository,
                         TransactionRepository transactionRepository,
                         CategoryRepository categoryRepository,
                         AuditService auditService,
                         UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.auditService = auditService;
        this.userRepository = userRepository;
    }

    @Override
    public Account createAccount(UUID userId, String firstName, String lastName, String nationality, String accountType) {
        String accountNumber = accountRepository.generateAccountNumber();
        Account account = Account.create(userId, accountNumber, firstName, lastName, nationality, accountType);

        Account savedAccount = accountRepository.save(account);

        // Audit log account creation
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            String username = userOpt.map(User::getUsername).orElse("unknown");

            auditService.logSuccess(
                    AuditLog.AuditAction.ACCOUNT_CREATED,
                    userId.getMostSignificantBits(),
                    username,
                    "Account",
                    savedAccount.getId().toString(),
                    String.format("Account created: accountNumber=%s, type=%s, holder=%s %s",
                            accountNumber, accountType, firstName, lastName),
                    getCurrentRequest()
            );
        } catch (Exception e) {
            // Don't fail account creation if audit logging fails
            e.printStackTrace();
        }

        return savedAccount;
    }

    @Override
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    @Override
    public List<Account> getAccountsByUserId(UUID userId) {
        return accountRepository.findByUserId(userId);
    }

    @Override
    public Optional<Account> getAccountById(UUID id) {
        return accountRepository.findById(id);
    }

    @Override
    public Optional<Account> getAccountByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber);
    }

    @Override
    public Optional<Account> updateAccount(UUID id, String accountType) {
        Optional<Account> accountOpt = accountRepository.findById(id);
        if (accountOpt.isEmpty()) {
            return Optional.empty();
        }

        Account account = accountOpt.get();
        Account updatedAccount = account.updateAccountType(accountType);
        Account savedAccount = accountRepository.update(updatedAccount);
        return Optional.of(savedAccount);
    }

    @Override
    public boolean deleteAccount(UUID id) {
        Optional<Account> accountOpt = accountRepository.findById(id);
        if (accountOpt.isEmpty()) {
            return false;
        }

        Account account = accountOpt.get();

        // Only allow deletion if balance is zero (business rule)
        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalStateException("Cannot delete account with non-zero balance. Please transfer or withdraw all funds first.");
        }

        boolean deleted = accountRepository.deleteById(id);

        // Audit log account closure/deletion
        if (deleted) {
            try {
                Optional<User> userOpt = userRepository.findById(account.getUserId());
                String username = userOpt.map(User::getUsername).orElse("unknown");

                auditService.logSuccess(
                        AuditLog.AuditAction.ACCOUNT_CLOSED,
                        account.getUserId().getMostSignificantBits(),
                        username,
                        "Account",
                        account.getId().toString(),
                        String.format("Account closed: accountNumber=%s, type=%s, finalBalance=%s",
                                account.getAccountNumber(), account.getAccountType(), account.getBalance()),
                        getCurrentRequest()
                );
            } catch (Exception e) {
                // Don't fail account deletion if audit logging fails
                e.printStackTrace();
            }
        }

        return deleted;
    }

    @Override
    public Account deposit(UUID accountId, BigDecimal amount, String description) {
        // Get "OTHER" category as default
        UUID categoryId = getDefaultCategoryId();
        return deposit(accountId, amount, description, categoryId);
    }

    @Override
    public Account deposit(UUID accountId, BigDecimal amount, String description, UUID categoryId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found with id: " + accountId));

        // Perform deposit (domain logic handles validation)
        Account updatedAccount = account.deposit(amount);

        // Save updated account
        Account savedAccount = accountRepository.update(updatedAccount);

        // Record transaction with category
        Transaction transaction = Transaction.createDeposit(
                accountId,
                amount,
                savedAccount.getBalance(),
                description,
                categoryId
        );
        transactionRepository.save(transaction);

        // Audit log deposit transaction
        try {
            Optional<User> userOpt = userRepository.findById(savedAccount.getUserId());
            String username = userOpt.map(User::getUsername).orElse("unknown");

            auditService.logSuccess(
                    AuditLog.AuditAction.TRANSACTION_DEPOSIT,
                    savedAccount.getUserId().getMostSignificantBits(),
                    username,
                    "Transaction",
                    transaction.getId().toString(),
                    String.format("Deposit: amount=%s, accountNumber=%s, newBalance=%s, description=%s",
                            amount, savedAccount.getAccountNumber(), savedAccount.getBalance(), description),
                    getCurrentRequest()
            );
        } catch (Exception e) {
            // Don't fail transaction if audit logging fails
            e.printStackTrace();
        }

        return savedAccount;
    }

    @Override
    public Account withdraw(UUID accountId, BigDecimal amount, String description) {
        // Get "OTHER" category as default
        UUID categoryId = getDefaultCategoryId();
        return withdraw(accountId, amount, description, categoryId);
    }

    @Override
    public Account withdraw(UUID accountId, BigDecimal amount, String description, UUID categoryId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found with id: " + accountId));

        // Perform withdrawal (domain logic handles validation and balance check)
        Account updatedAccount = account.withdraw(amount);

        // Save updated account
        Account savedAccount = accountRepository.update(updatedAccount);

        // Record transaction with category
        Transaction transaction = Transaction.createWithdrawal(
                accountId,
                amount,
                savedAccount.getBalance(),
                description,
                categoryId
        );
        transactionRepository.save(transaction);

        // Audit log withdrawal transaction
        try {
            Optional<User> userOpt = userRepository.findById(savedAccount.getUserId());
            String username = userOpt.map(User::getUsername).orElse("unknown");

            auditService.logSuccess(
                    AuditLog.AuditAction.TRANSACTION_WITHDRAWAL,
                    savedAccount.getUserId().getMostSignificantBits(),
                    username,
                    "Transaction",
                    transaction.getId().toString(),
                    String.format("Withdrawal: amount=%s, accountNumber=%s, newBalance=%s, description=%s",
                            amount, savedAccount.getAccountNumber(), savedAccount.getBalance(), description),
                    getCurrentRequest()
            );
        } catch (Exception e) {
            // Don't fail transaction if audit logging fails
            e.printStackTrace();
        }

        return savedAccount;
    }

    @Override
    public void transfer(UUID fromAccountId, UUID toAccountId, BigDecimal amount, String description) {
        if (fromAccountId.equals(toAccountId)) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }

        // Get both accounts
        Account fromAccount = accountRepository.findById(fromAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Source account not found with id: " + fromAccountId));

        Account toAccount = accountRepository.findById(toAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Destination account not found with id: " + toAccountId));

        // Get "TRANSFER" category
        UUID transferCategoryId = getTransferCategoryId();

        // Perform transfer (withdraw from source)
        Account updatedFromAccount = fromAccount.withdraw(amount);
        Account savedFromAccount = accountRepository.update(updatedFromAccount);

        // Deposit to destination
        Account updatedToAccount = toAccount.deposit(amount);
        Account savedToAccount = accountRepository.update(updatedToAccount);

        // Record both transactions
        Transaction transferOut = Transaction.createTransferOut(
                fromAccountId,
                amount,
                savedFromAccount.getBalance(),
                toAccountId,
                description,
                transferCategoryId
        );
        transactionRepository.save(transferOut);

        Transaction transferIn = Transaction.createTransferIn(
                toAccountId,
                amount,
                savedToAccount.getBalance(),
                fromAccountId,
                description,
                transferCategoryId
        );
        transactionRepository.save(transferIn);

        // Audit log transfer transaction
        try {
            Optional<User> userOpt = userRepository.findById(savedFromAccount.getUserId());
            String username = userOpt.map(User::getUsername).orElse("unknown");

            auditService.logSuccess(
                    AuditLog.AuditAction.TRANSACTION_TRANSFER,
                    savedFromAccount.getUserId().getMostSignificantBits(),
                    username,
                    "Transaction",
                    transferOut.getId().toString(),
                    String.format("Transfer: amount=%s, from=%s (balance=%s), to=%s (balance=%s), description=%s",
                            amount, savedFromAccount.getAccountNumber(), savedFromAccount.getBalance(),
                            savedToAccount.getAccountNumber(), savedToAccount.getBalance(), description),
                    getCurrentRequest()
            );
        } catch (Exception e) {
            // Don't fail transaction if audit logging fails
            e.printStackTrace();
        }
    }

    @Override
    public List<Transaction> getTransactionHistory(UUID accountId) {
        return transactionRepository.findByAccountId(accountId);
    }

    @Override
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    /**
     * Get default "OTHER" category ID
     */
    private UUID getDefaultCategoryId() {
        return categoryRepository.findByName("OTHER")
                .map(TransactionCategory::getId)
                .orElseThrow(() -> new IllegalStateException("Default 'OTHER' category not found"));
    }

    /**
     * Get "TRANSFER" category ID
     */
    private UUID getTransferCategoryId() {
        return categoryRepository.findByName("TRANSFER")
                .map(TransactionCategory::getId)
                .orElseThrow(() -> new IllegalStateException("'TRANSFER' category not found"));
    }

    /**
     * Get current HttpServletRequest from RequestContextHolder
     */
    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
