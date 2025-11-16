package com.example.demo.application.service;

import com.example.demo.application.ports.in.*;
import com.example.demo.application.ports.out.AccountRepository;
import com.example.demo.application.ports.out.TransactionRepository;
import com.example.demo.domain.Account;
import com.example.demo.domain.Transaction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Banking service implementing all banking use cases
 * This is the core application service containing the business logic
 */
@Service
@Transactional
public class BankingService implements
        CreateAccountUseCase,
        GetAccountUseCase,
        DepositUseCase,
        WithdrawUseCase,
        TransferUseCase,
        GetTransactionHistoryUseCase {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public BankingService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public Account createAccount(Long customerId, String accountType) {
        if (customerId == null) {
            throw new IllegalArgumentException("Customer ID is required");
        }

        String accountNumber = accountRepository.generateAccountNumber();
        Account account = Account.create(accountNumber, customerId, accountType);

        return accountRepository.save(account);
    }

    @Override
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    @Override
    public Optional<Account> getAccountById(Long id) {
        return accountRepository.findById(id);
    }

    @Override
    public Optional<Account> getAccountByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber);
    }

    @Override
    public List<Account> getAccountsByCustomerId(Long customerId) {
        return accountRepository.findByCustomerId(customerId);
    }

    @Override
    public Account deposit(Long accountId, BigDecimal amount, String description) {
        return deposit(accountId, amount, description, Transaction.TransactionCategory.OTHER);
    }

    @Override
    public Account deposit(Long accountId, BigDecimal amount, String description, Transaction.TransactionCategory category) {
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
                category
        );
        transactionRepository.save(transaction);

        return savedAccount;
    }

    @Override
    public Account withdraw(Long accountId, BigDecimal amount, String description) {
        return withdraw(accountId, amount, description, Transaction.TransactionCategory.OTHER);
    }

    @Override
    public Account withdraw(Long accountId, BigDecimal amount, String description, Transaction.TransactionCategory category) {
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
                category
        );
        transactionRepository.save(transaction);

        return savedAccount;
    }

    @Override
    public void transfer(Long fromAccountId, Long toAccountId, BigDecimal amount, String description) {
        if (fromAccountId.equals(toAccountId)) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }

        // Get both accounts
        Account fromAccount = accountRepository.findById(fromAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Source account not found with id: " + fromAccountId));

        Account toAccount = accountRepository.findById(toAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Destination account not found with id: " + toAccountId));

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
                description
        );
        transactionRepository.save(transferOut);

        Transaction transferIn = Transaction.createTransferIn(
                toAccountId,
                amount,
                savedToAccount.getBalance(),
                fromAccountId,
                description
        );
        transactionRepository.save(transferIn);
    }

    @Override
    public List<Transaction> getTransactionHistory(Long accountId) {
        return transactionRepository.findByAccountId(accountId);
    }

    @Override
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }
}
