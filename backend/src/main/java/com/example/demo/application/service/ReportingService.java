package com.example.demo.application.service;

import com.example.demo.application.ports.in.GenerateAccountStatementUseCase;
import com.example.demo.application.ports.in.GenerateCategoryReportUseCase;
import com.example.demo.application.ports.out.AccountRepository;
import com.example.demo.application.ports.out.TransactionRepository;
import com.example.demo.domain.Account;
import com.example.demo.domain.AccountStatement;
import com.example.demo.domain.CategoryReport;
import com.example.demo.domain.Transaction;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Application service for generating reports and statements
 * Implements use cases for account statements and category reports
 */
@Service
public class ReportingService implements GenerateAccountStatementUseCase, GenerateCategoryReportUseCase {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public ReportingService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public AccountStatement generateStatement(Long accountId, LocalDateTime startDate, LocalDateTime endDate) {
        // Get account
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found with ID: " + accountId));

        // Get transactions in date range
        List<Transaction> transactions = transactionRepository.findByAccountIdAndDateRange(accountId, startDate, endDate);

        // Calculate opening balance
        // Get all transactions before start date and calculate balance at that point
        BigDecimal openingBalance = calculateBalanceAtDate(accountId, startDate);

        // Calculate totals
        BigDecimal totalDeposits = transactions.stream()
                .filter(t -> t.getType() == Transaction.TransactionType.DEPOSIT)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalWithdrawals = transactions.stream()
                .filter(t -> t.getType() == Transaction.TransactionType.WITHDRAWAL)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Closing balance
        BigDecimal closingBalance = openingBalance.add(totalDeposits).subtract(totalWithdrawals);

        return new AccountStatement(
                account,
                startDate,
                endDate,
                openingBalance,
                closingBalance,
                totalDeposits,
                totalWithdrawals,
                transactions.size(),
                transactions
        );
    }

    @Override
    public CategoryReport generateCategoryReport(Long accountId, Transaction.TransactionType transactionType) {
        // Verify account exists
        accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found with ID: " + accountId));

        // Get category summaries from repository
        List<CategoryReport.CategorySummary> categorySummaries =
                transactionRepository.getCategorySummary(accountId, transactionType);

        // Calculate totals
        BigDecimal totalAmount = categorySummaries.stream()
                .map(CategoryReport.CategorySummary::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalTransactions = categorySummaries.stream()
                .mapToInt(CategoryReport.CategorySummary::count)
                .sum();

        return new CategoryReport(
                accountId,
                transactionType,
                categorySummaries,
                totalAmount,
                totalTransactions
        );
    }

    /**
     * Calculate account balance at a specific date
     * by summing all transactions before that date
     */
    private BigDecimal calculateBalanceAtDate(Long accountId, LocalDateTime date) {
        List<Transaction> allTransactions = transactionRepository.findByAccountId(accountId);

        BigDecimal balance = BigDecimal.ZERO;
        for (Transaction transaction : allTransactions) {
            if (transaction.getCreatedAt().isBefore(date)) {
                if (transaction.getType() == Transaction.TransactionType.DEPOSIT) {
                    balance = balance.add(transaction.getAmount());
                } else if (transaction.getType() == Transaction.TransactionType.WITHDRAWAL) {
                    balance = balance.subtract(transaction.getAmount());
                }
            }
        }

        return balance;
    }
}
