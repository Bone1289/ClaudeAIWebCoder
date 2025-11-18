package com.example.demo.adapter.in.graphql;

import com.example.demo.adapter.in.graphql.dto.*;
import com.example.demo.application.ports.in.*;
import com.example.demo.config.security.SecurityUtil;
import com.example.demo.domain.Account;
import com.example.demo.domain.AccountStatement;
import com.example.demo.domain.CategoryReport;
import com.example.demo.domain.Transaction;
import com.example.demo.domain.TransactionCategory;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * GraphQL Resolver for Banking operations
 */
@Controller
public class BankingResolver {

    private final CreateAccountUseCase createAccountUseCase;
    private final GetAccountUseCase getAccountUseCase;
    private final UpdateAccountUseCase updateAccountUseCase;
    private final DeleteAccountUseCase deleteAccountUseCase;
    private final DepositUseCase depositUseCase;
    private final WithdrawUseCase withdrawUseCase;
    private final TransferUseCase transferUseCase;
    private final GetTransactionHistoryUseCase getTransactionHistoryUseCase;
    private final GenerateAccountStatementUseCase generateAccountStatementUseCase;
    private final GenerateCategoryReportUseCase generateCategoryReportUseCase;
    private final ManageCategoryUseCase manageCategoryUseCase;

    public BankingResolver(CreateAccountUseCase createAccountUseCase,
                          GetAccountUseCase getAccountUseCase,
                          UpdateAccountUseCase updateAccountUseCase,
                          DeleteAccountUseCase deleteAccountUseCase,
                          DepositUseCase depositUseCase,
                          WithdrawUseCase withdrawUseCase,
                          TransferUseCase transferUseCase,
                          GetTransactionHistoryUseCase getTransactionHistoryUseCase,
                          GenerateAccountStatementUseCase generateAccountStatementUseCase,
                          GenerateCategoryReportUseCase generateCategoryReportUseCase,
                          ManageCategoryUseCase manageCategoryUseCase) {
        this.createAccountUseCase = createAccountUseCase;
        this.getAccountUseCase = getAccountUseCase;
        this.updateAccountUseCase = updateAccountUseCase;
        this.deleteAccountUseCase = deleteAccountUseCase;
        this.depositUseCase = depositUseCase;
        this.withdrawUseCase = withdrawUseCase;
        this.transferUseCase = transferUseCase;
        this.getTransactionHistoryUseCase = getTransactionHistoryUseCase;
        this.generateAccountStatementUseCase = generateAccountStatementUseCase;
        this.generateCategoryReportUseCase = generateCategoryReportUseCase;
        this.manageCategoryUseCase = manageCategoryUseCase;
    }

    // ==================== Queries ====================

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public List<AccountDTO> accounts() {
        UUID userId = SecurityUtil.getCurrentUserId();
        return getAccountUseCase.getAccountsByUserId(userId).stream()
                .map(AccountDTO::fromDomain)
                .collect(Collectors.toList());
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public AccountDTO account(@Argument UUID id) {
        UUID userId = SecurityUtil.getCurrentUserId();
        return getAccountUseCase.getAccountById(id)
                .filter(account -> account.getUserId().equals(userId))
                .map(AccountDTO::fromDomain)
                .orElseThrow(() -> new RuntimeException("Account not found"));
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public List<TransactionDTO> transactions() {
        return getTransactionHistoryUseCase.getAllTransactions().stream()
                .map(TransactionDTO::fromDomain)
                .collect(Collectors.toList());
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public List<TransactionDTO> transactionHistory(@Argument UUID accountId) {
        return getTransactionHistoryUseCase.getTransactionHistory(accountId).stream()
                .map(TransactionDTO::fromDomain)
                .collect(Collectors.toList());
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public AccountStatementDTO accountStatement(@Argument UUID accountId,
                                               @Argument LocalDate startDate,
                                               @Argument LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        AccountStatement statement = generateAccountStatementUseCase.generateStatement(
                accountId, startDateTime, endDateTime);
        return AccountStatementDTO.fromDomain(statement);
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public List<CategoryReportDTO> categoryReport(@Argument UUID accountId,
                                                  @Argument TransactionCategory.CategoryType type) {
        Transaction.TransactionType transactionType = type == TransactionCategory.CategoryType.INCOME
                ? Transaction.TransactionType.DEPOSIT
                : Transaction.TransactionType.WITHDRAWAL;

        CategoryReport report = generateCategoryReportUseCase.generateCategoryReport(accountId, transactionType);
        return report.categories().stream()
                .map(CategoryReportDTO::fromDomain)
                .collect(Collectors.toList());
    }

    // ==================== Mutations ====================

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public AccountDTO createAccount(@Argument CreateAccountInputDTO input) {
        UUID userId = SecurityUtil.getCurrentUserId();
        Account account = createAccountUseCase.createAccount(
                userId,
                input.firstName(),
                input.lastName(),
                input.nationality(),
                input.accountType()
        );
        return AccountDTO.fromDomain(account);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public AccountDTO updateAccount(@Argument UUID id, @Argument UpdateAccountInputDTO input) {
        Account account = updateAccountUseCase.updateAccount(id, input.accountType())
                .orElseThrow(() -> new RuntimeException("Account not found"));
        return AccountDTO.fromDomain(account);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public Boolean deleteAccount(@Argument UUID id) {
        return deleteAccountUseCase.deleteAccount(id);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public TransactionDTO deposit(@Argument UUID accountId, @Argument DepositInputDTO input) {
        Account account;
        if (input.categoryId() != null) {
            account = depositUseCase.deposit(accountId, input.amount(), input.description(), input.categoryId());
        } else {
            account = depositUseCase.deposit(accountId, input.amount(), input.description());
        }

        // Return the most recent transaction
        List<Transaction> transactions = getTransactionHistoryUseCase.getTransactionHistory(accountId);
        return TransactionDTO.fromDomain(transactions.get(0));
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public TransactionDTO withdraw(@Argument UUID accountId, @Argument WithdrawInputDTO input) {
        Account account;
        if (input.categoryId() != null) {
            account = withdrawUseCase.withdraw(accountId, input.amount(), input.description(), input.categoryId());
        } else {
            account = withdrawUseCase.withdraw(accountId, input.amount(), input.description());
        }

        // Return the most recent transaction
        List<Transaction> transactions = getTransactionHistoryUseCase.getTransactionHistory(accountId);
        return TransactionDTO.fromDomain(transactions.get(0));
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public TransactionDTO transfer(@Argument UUID fromAccountId, @Argument TransferInputDTO input) {
        transferUseCase.transfer(fromAccountId, input.toAccountId(), input.amount(), input.description());

        // Return the most recent transaction
        List<Transaction> transactions = getTransactionHistoryUseCase.getTransactionHistory(fromAccountId);
        return TransactionDTO.fromDomain(transactions.get(0));
    }

    // ==================== Field Resolvers ====================

    @SchemaMapping(typeName = "Account", field = "transactions")
    public List<TransactionDTO> accountTransactions(AccountDTO account) {
        return getTransactionHistoryUseCase.getTransactionHistory(account.id()).stream()
                .map(TransactionDTO::fromDomain)
                .collect(Collectors.toList());
    }

    @SchemaMapping(typeName = "Transaction", field = "category")
    public TransactionCategoryDTO transactionCategory(TransactionDTO transaction) {
        if (transaction.categoryId() == null) {
            return null;
        }

        return manageCategoryUseCase.getCategoryById(transaction.categoryId())
                .map(TransactionCategoryDTO::fromDomain)
                .orElse(null);
    }
}
