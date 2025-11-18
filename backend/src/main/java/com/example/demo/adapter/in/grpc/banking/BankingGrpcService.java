package com.example.demo.adapter.in.grpc.banking;

import com.example.demo.application.service.BankingService;
import com.example.demo.application.service.ReportingService;
import com.example.demo.config.security.SecurityUtil;
import com.example.demo.domain.Account;
import com.example.demo.domain.Transaction;
import com.example.demo.domain.User;
import com.example.demo.grpc.banking.*;
import com.example.demo.grpc.common.Empty;
import com.example.demo.grpc.common.IdRequest;
import com.google.protobuf.Timestamp;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * gRPC service adapter for banking operations
 * Follows hexagonal architecture pattern - this is an input adapter
 */
@GrpcService
public class BankingGrpcService extends BankingServiceGrpc.BankingServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(BankingGrpcService.class);

    private final BankingService bankingService;
    private final ReportingService reportingService;

    public BankingGrpcService(BankingService bankingService, ReportingService reportingService) {
        this.bankingService = bankingService;
        this.reportingService = reportingService;
    }

    @Override
    public void createAccount(CreateAccountRequest request, StreamObserver<CreateAccountResponse> responseObserver) {
        try {
            User currentUser = SecurityUtil.getCurrentUser();
            logger.info("gRPC CreateAccount request for user: {}", currentUser.getId());

            Account account = bankingService.createAccount(
                    currentUser.getId(),
                    request.getFirstName(),
                    request.getLastName(),
                    request.getNationality(),
                    request.getAccountType()
            );

            CreateAccountResponse response = CreateAccountResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Account created successfully")
                    .setAccount(mapToAccountResponse(account))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (IllegalArgumentException e) {
            logger.error("CreateAccount validation error: {}", e.getMessage());
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            logger.error("CreateAccount error", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Failed to create account: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void getAccount(IdRequest request, StreamObserver<GetAccountResponse> responseObserver) {
        try {
            logger.info("gRPC GetAccount request for id: {}", request.getId());

            Account account = bankingService.getAccountById(UUID.fromString(request.getId()))
                    .orElseThrow(() -> new IllegalArgumentException("Account not found"));

            GetAccountResponse response = GetAccountResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Account retrieved successfully")
                    .setAccount(mapToAccountResponse(account))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (IllegalArgumentException e) {
            logger.error("GetAccount error: {}", e.getMessage());
            responseObserver.onError(io.grpc.Status.NOT_FOUND
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            logger.error("GetAccount error", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Failed to get account: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void getAllAccounts(Empty request, StreamObserver<GetAllAccountsResponse> responseObserver) {
        try {
            User currentUser = SecurityUtil.getCurrentUser();
            logger.info("gRPC GetAllAccounts request for user: {}", currentUser.getId());

            List<Account> accounts = bankingService.getAccountsByUserId(currentUser.getId());

            GetAllAccountsResponse response = GetAllAccountsResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Accounts retrieved successfully")
                    .addAllAccounts(accounts.stream()
                            .map(this::mapToAccountResponse)
                            .collect(Collectors.toList()))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            logger.error("GetAllAccounts error", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Failed to get accounts: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void updateAccount(UpdateAccountRequest request, StreamObserver<UpdateAccountResponse> responseObserver) {
        try {
            logger.info("gRPC UpdateAccount request for id: {}", request.getId());

            Account account = bankingService.updateAccount(
                    UUID.fromString(request.getId()),
                    request.getAccountType()
            ).orElseThrow(() -> new IllegalArgumentException("Account not found"));

            UpdateAccountResponse response = UpdateAccountResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Account updated successfully")
                    .setAccount(mapToAccountResponse(account))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (IllegalArgumentException e) {
            logger.error("UpdateAccount error: {}", e.getMessage());
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            logger.error("UpdateAccount error", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Failed to update account: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void deleteAccount(IdRequest request, StreamObserver<DeleteAccountResponse> responseObserver) {
        try {
            logger.info("gRPC DeleteAccount request for id: {}", request.getId());

            boolean deleted = bankingService.deleteAccount(UUID.fromString(request.getId()));

            DeleteAccountResponse response = DeleteAccountResponse.newBuilder()
                    .setSuccess(deleted)
                    .setMessage(deleted ? "Account deleted successfully" : "Account not found")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (IllegalStateException e) {
            logger.error("DeleteAccount business rule error: {}", e.getMessage());
            responseObserver.onError(io.grpc.Status.FAILED_PRECONDITION
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            logger.error("DeleteAccount error", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Failed to delete account: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void deposit(TransactionRequest request, StreamObserver<TransactionResponse> responseObserver) {
        try {
            logger.info("gRPC Deposit request for account: {}", request.getAccountId());

            UUID categoryId = request.getCategoryId().isEmpty() ? null : UUID.fromString(request.getCategoryId());
            Account account = categoryId != null
                    ? bankingService.deposit(
                            UUID.fromString(request.getAccountId()),
                            new BigDecimal(request.getAmount()),
                            request.getDescription(),
                            categoryId)
                    : bankingService.deposit(
                            UUID.fromString(request.getAccountId()),
                            new BigDecimal(request.getAmount()),
                            request.getDescription());

            // Get the latest transaction
            List<Transaction> transactions = bankingService.getTransactionHistory(account.getId());
            Transaction latestTransaction = transactions.get(0); // Most recent

            TransactionResponse response = TransactionResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Deposit successful")
                    .setTransaction(mapToTransactionDetail(latestTransaction))
                    .setAccount(mapToAccountResponse(account))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (IllegalArgumentException e) {
            logger.error("Deposit validation error: {}", e.getMessage());
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            logger.error("Deposit error", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Failed to process deposit: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void withdraw(TransactionRequest request, StreamObserver<TransactionResponse> responseObserver) {
        try {
            logger.info("gRPC Withdraw request for account: {}", request.getAccountId());

            UUID categoryId = request.getCategoryId().isEmpty() ? null : UUID.fromString(request.getCategoryId());
            Account account = categoryId != null
                    ? bankingService.withdraw(
                            UUID.fromString(request.getAccountId()),
                            new BigDecimal(request.getAmount()),
                            request.getDescription(),
                            categoryId)
                    : bankingService.withdraw(
                            UUID.fromString(request.getAccountId()),
                            new BigDecimal(request.getAmount()),
                            request.getDescription());

            // Get the latest transaction
            List<Transaction> transactions = bankingService.getTransactionHistory(account.getId());
            Transaction latestTransaction = transactions.get(0);

            TransactionResponse response = TransactionResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Withdrawal successful")
                    .setTransaction(mapToTransactionDetail(latestTransaction))
                    .setAccount(mapToAccountResponse(account))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.error("Withdraw error: {}", e.getMessage());
            responseObserver.onError(io.grpc.Status.FAILED_PRECONDITION
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            logger.error("Withdraw error", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Failed to process withdrawal: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void transfer(TransferRequest request, StreamObserver<TransferResponse> responseObserver) {
        try {
            logger.info("gRPC Transfer request from: {} to: {}",
                    request.getFromAccountId(), request.getToAccountId());

            UUID fromAccountId = UUID.fromString(request.getFromAccountId());
            UUID toAccountId = UUID.fromString(request.getToAccountId());

            // Execute transfer
            bankingService.transfer(
                    fromAccountId,
                    toAccountId,
                    new BigDecimal(request.getAmount()),
                    request.getDescription()
            );

            // Fetch updated accounts
            Account fromAccount = bankingService.getAccountById(fromAccountId)
                    .orElseThrow(() -> new IllegalStateException("Source account not found after transfer"));
            Account toAccount = bankingService.getAccountById(toAccountId)
                    .orElseThrow(() -> new IllegalStateException("Destination account not found after transfer"));

            // Get transactions for both accounts
            List<Transaction> fromTransactions = bankingService.getTransactionHistory(fromAccount.getId());
            List<Transaction> toTransactions = bankingService.getTransactionHistory(toAccount.getId());

            TransferResponse response = TransferResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Transfer successful")
                    .setFromTransaction(mapToTransactionDetail(fromTransactions.get(0)))
                    .setToTransaction(mapToTransactionDetail(toTransactions.get(0)))
                    .setFromAccount(mapToAccountResponse(fromAccount))
                    .setToAccount(mapToAccountResponse(toAccount))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.error("Transfer error: {}", e.getMessage());
            responseObserver.onError(io.grpc.Status.FAILED_PRECONDITION
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            logger.error("Transfer error", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Failed to process transfer: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void getAccountTransactions(GetAccountTransactionsRequest request,
                                       StreamObserver<GetAccountTransactionsResponse> responseObserver) {
        try {
            logger.info("gRPC GetAccountTransactions request for account: {}", request.getAccountId());

            List<Transaction> transactions = bankingService.getTransactionHistory(
                    UUID.fromString(request.getAccountId())
            );

            GetAccountTransactionsResponse response = GetAccountTransactionsResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Transactions retrieved successfully")
                    .addAllTransactions(transactions.stream()
                            .map(this::mapToTransactionDetail)
                            .collect(Collectors.toList()))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            logger.error("GetAccountTransactions error", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Failed to get transactions: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void getAllTransactions(com.example.demo.grpc.common.PaginationRequest request,
                                   StreamObserver<GetAllTransactionsResponse> responseObserver) {
        try {
            User currentUser = SecurityUtil.getCurrentUser();
            logger.info("gRPC GetAllTransactions request for user: {}", currentUser.getId());

            // Get all transactions for all user's accounts
            List<Account> userAccounts = bankingService.getAccountsByUserId(currentUser.getId());
            List<Transaction> transactions = userAccounts.stream()
                    .flatMap(account -> bankingService.getTransactionHistory(account.getId()).stream())
                    .collect(Collectors.toList());

            GetAllTransactionsResponse response = GetAllTransactionsResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Transactions retrieved successfully")
                    .addAllTransactions(transactions.stream()
                            .map(this::mapToTransactionDetail)
                            .collect(Collectors.toList()))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            logger.error("GetAllTransactions error", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Failed to get transactions: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void getAccountStatement(GetAccountStatementRequest request,
                                    StreamObserver<GetAccountStatementResponse> responseObserver) {
        try {
            logger.info("gRPC GetAccountStatement request for account: {}", request.getAccountId());

            LocalDateTime startDate = LocalDateTime.ofInstant(
                    Instant.ofEpochSecond(request.getStartDate().getSeconds()),
                    ZoneOffset.UTC
            );
            LocalDateTime endDate = LocalDateTime.ofInstant(
                    Instant.ofEpochSecond(request.getEndDate().getSeconds()),
                    ZoneOffset.UTC
            );

            var statement = reportingService.generateStatement(
                    UUID.fromString(request.getAccountId()),
                    startDate,
                    endDate
            );

            GetAccountStatementResponse response = GetAccountStatementResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Statement generated successfully")
                    .setAccount(mapToAccountResponse(statement.account()))
                    .addAllTransactions(statement.transactions().stream()
                            .map(this::mapToTransactionDetail)
                            .collect(Collectors.toList()))
                    .setSummary(StatementSummary.newBuilder()
                            .setOpeningBalance(statement.openingBalance().toString())
                            .setClosingBalance(statement.closingBalance().toString())
                            .setTotalDeposits(statement.totalDeposits().toString())
                            .setTotalWithdrawals(statement.totalWithdrawals().toString())
                            .setTransactionCount(statement.transactionCount())
                            .build())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            logger.error("GetAccountStatement error", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Failed to generate statement: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void getCategoryReport(GetCategoryReportRequest request,
                                  StreamObserver<GetCategoryReportResponse> responseObserver) {
        try {
            logger.info("gRPC GetCategoryReport request for account: {}", request.getAccountId());

            // Note: The current implementation only supports Transaction.TransactionType
            // For now, we default to EXPENSE type (most common use case for category reports)
            // The date range in the request is currently ignored by the service
            var report = reportingService.generateCategoryReport(
                    UUID.fromString(request.getAccountId()),
                    Transaction.TransactionType.WITHDRAWAL  // WITHDRAWAL represents expenses
            );

            GetCategoryReportResponse response = GetCategoryReportResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Category report generated successfully")
                    .addAllCategories(report.categories().stream()
                            .map(categorySummary -> CategorySpending.newBuilder()
                                    .setCategoryId(categorySummary.category().getId().toString())
                                    .setCategoryName(categorySummary.category().getName())
                                    .setTotalAmount(categorySummary.amount().toString())
                                    .setTransactionCount(categorySummary.count())
                                    .setPercentage(categorySummary.percentage().doubleValue())
                                    .build())
                            .collect(Collectors.toList()))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            logger.error("GetCategoryReport error", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Failed to generate category report: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    /**
     * Maps domain Account to gRPC AccountResponse
     */
    private AccountResponse mapToAccountResponse(Account account) {
        return AccountResponse.newBuilder()
                .setId(account.getId().toString())
                .setUserId(account.getUserId().toString())
                .setAccountNumber(account.getAccountNumber())
                .setFirstName(account.getFirstName())
                .setLastName(account.getLastName())
                .setNationality(account.getNationality())
                .setAccountType(account.getAccountType())
                .setBalance(account.getBalance().toString())
                .setStatus(account.getStatus().name())
                .setCreatedAt(Timestamp.newBuilder()
                        .setSeconds(account.getCreatedAt().toEpochSecond(ZoneOffset.UTC))
                        .setNanos(account.getCreatedAt().getNano())
                        .build())
                .build();
    }

    /**
     * Maps domain Transaction to gRPC TransactionDetail
     */
    private TransactionDetail mapToTransactionDetail(Transaction transaction) {
        var builder = TransactionDetail.newBuilder()
                .setId(transaction.getId().toString())
                .setAccountId(transaction.getAccountId().toString())
                .setType(transaction.getType().name())
                .setAmount(transaction.getAmount().toString())
                .setBalanceAfter(transaction.getBalanceAfter().toString())
                .setDescription(transaction.getDescription())
                .setTimestamp(Timestamp.newBuilder()
                        .setSeconds(transaction.getCreatedAt().toEpochSecond(ZoneOffset.UTC))
                        .setNanos(transaction.getCreatedAt().getNano())
                        .build());

        if (transaction.getCategoryId() != null) {
            builder.setCategoryId(transaction.getCategoryId().toString());
        }

        if (transaction.getRelatedAccountId() != null) {
            builder.setRelatedAccountId(transaction.getRelatedAccountId().toString());
        }

        return builder.build();
    }
}
