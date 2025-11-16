package com.example.demo.adapter.in.web.banking;

import com.example.demo.adapter.in.web.banking.dto.*;
import com.example.demo.adapter.in.web.dto.ApiResponse;
import com.example.demo.application.ports.in.*;
import com.example.demo.domain.Account;
import com.example.demo.domain.AccountStatement;
import com.example.demo.domain.CategoryReport;
import com.example.demo.domain.Transaction;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Banking REST Controller
 * Handles all banking operations: accounts and transactions
 */
@RestController
@RequestMapping("/api/banking")
public class BankingController {

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

    public BankingController(CreateAccountUseCase createAccountUseCase,
                            GetAccountUseCase getAccountUseCase,
                            UpdateAccountUseCase updateAccountUseCase,
                            DeleteAccountUseCase deleteAccountUseCase,
                            DepositUseCase depositUseCase,
                            WithdrawUseCase withdrawUseCase,
                            TransferUseCase transferUseCase,
                            GetTransactionHistoryUseCase getTransactionHistoryUseCase,
                            GenerateAccountStatementUseCase generateAccountStatementUseCase,
                            GenerateCategoryReportUseCase generateCategoryReportUseCase) {
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
    }

    @PostMapping("/accounts")
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(@RequestBody CreateAccountRequest request) {
        try {
            Account account = createAccountUseCase.createAccount(request.getCustomerId(), request.getAccountType());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Account created successfully", AccountResponse.fromDomain(account)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/accounts")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getAllAccounts() {
        List<AccountResponse> accounts = getAccountUseCase.getAllAccounts().stream()
                .map(AccountResponse::fromDomain)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Accounts retrieved successfully", accounts));
    }

    @GetMapping("/accounts/{id}")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountById(@PathVariable Long id) {
        return getAccountUseCase.getAccountById(id)
                .map(account -> ResponseEntity.ok(
                        ApiResponse.success("Account found", AccountResponse.fromDomain(account))))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Account not found")));
    }

    @GetMapping("/accounts/customer/{customerId}")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getAccountsByCustomer(@PathVariable Long customerId) {
        List<AccountResponse> accounts = getAccountUseCase.getAccountsByCustomerId(customerId).stream()
                .map(AccountResponse::fromDomain)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Customer accounts retrieved", accounts));
    }

    @PutMapping("/accounts/{id}")
    public ResponseEntity<ApiResponse<AccountResponse>> updateAccount(@PathVariable Long id, @RequestBody UpdateAccountRequest request) {
        try {
            return updateAccountUseCase.updateAccount(id, request.getAccountType())
                    .map(account -> ResponseEntity.ok(
                            ApiResponse.success("Account updated successfully", AccountResponse.fromDomain(account))))
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(ApiResponse.error("Account not found")));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/accounts/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(@PathVariable Long id) {
        try {
            boolean deleted = deleteAccountUseCase.deleteAccount(id);
            if (deleted) {
                return ResponseEntity.ok(ApiResponse.success("Account deleted successfully", null));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Account not found"));
            }
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/accounts/{id}/deposit")
    public ResponseEntity<ApiResponse<AccountResponse>> deposit(@PathVariable Long id, @RequestBody TransactionRequest request) {
        try {
            Account account;
            if (request.getCategoryId() != null) {
                account = depositUseCase.deposit(id, request.getAmount(), request.getDescription(), request.getCategoryId());
            } else {
                account = depositUseCase.deposit(id, request.getAmount(), request.getDescription());
            }
            return ResponseEntity.ok(ApiResponse.success("Deposit successful", AccountResponse.fromDomain(account)));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/accounts/{id}/withdraw")
    public ResponseEntity<ApiResponse<AccountResponse>> withdraw(@PathVariable Long id, @RequestBody TransactionRequest request) {
        try {
            Account account;
            if (request.getCategoryId() != null) {
                account = withdrawUseCase.withdraw(id, request.getAmount(), request.getDescription(), request.getCategoryId());
            } else {
                account = withdrawUseCase.withdraw(id, request.getAmount(), request.getDescription());
            }
            return ResponseEntity.ok(ApiResponse.success("Withdrawal successful", AccountResponse.fromDomain(account)));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/accounts/{id}/transfer")
    public ResponseEntity<ApiResponse<Void>> transfer(@PathVariable Long id, @RequestBody TransferRequest request) {
        try {
            transferUseCase.transfer(id, request.getToAccountId(), request.getAmount(), request.getDescription());
            return ResponseEntity.ok(ApiResponse.success("Transfer successful", null));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/accounts/{id}/transactions")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getTransactionHistory(@PathVariable Long id) {
        List<TransactionResponse> transactions = getTransactionHistoryUseCase.getTransactionHistory(id).stream()
                .map(TransactionResponse::fromDomain)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Transaction history retrieved", transactions));
    }

    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getAllTransactions() {
        List<TransactionResponse> transactions = getTransactionHistoryUseCase.getAllTransactions().stream()
                .map(TransactionResponse::fromDomain)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("All transactions retrieved", transactions));
    }

    @GetMapping("/accounts/{id}/statement")
    public ResponseEntity<ApiResponse<AccountStatementResponse>> getAccountStatement(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            AccountStatement statement = generateAccountStatementUseCase.generateStatement(id, startDate, endDate);
            return ResponseEntity.ok(ApiResponse.success("Statement generated successfully",
                    AccountStatementResponse.fromDomain(statement)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/accounts/{id}/category-report")
    public ResponseEntity<ApiResponse<CategoryReportResponse>> getCategoryReport(
            @PathVariable Long id,
            @RequestParam String type) {
        try {
            Transaction.TransactionType transactionType = Transaction.TransactionType.valueOf(type);
            CategoryReport report = generateCategoryReportUseCase.generateCategoryReport(id, transactionType);
            return ResponseEntity.ok(ApiResponse.success("Category report generated successfully",
                    CategoryReportResponse.fromDomain(report)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid transaction type: " + type));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }
}
