package com.example.demo.adapter.in.web.banking;

import com.example.demo.adapter.in.web.banking.dto.*;
import com.example.demo.adapter.in.web.dto.ApiResponse;
import com.example.demo.application.ports.in.*;
import com.example.demo.domain.Account;
import com.example.demo.domain.Transaction;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    private final DepositUseCase depositUseCase;
    private final WithdrawUseCase withdrawUseCase;
    private final TransferUseCase transferUseCase;
    private final GetTransactionHistoryUseCase getTransactionHistoryUseCase;

    public BankingController(CreateAccountUseCase createAccountUseCase,
                            GetAccountUseCase getAccountUseCase,
                            DepositUseCase depositUseCase,
                            WithdrawUseCase withdrawUseCase,
                            TransferUseCase transferUseCase,
                            GetTransactionHistoryUseCase getTransactionHistoryUseCase) {
        this.createAccountUseCase = createAccountUseCase;
        this.getAccountUseCase = getAccountUseCase;
        this.depositUseCase = depositUseCase;
        this.withdrawUseCase = withdrawUseCase;
        this.transferUseCase = transferUseCase;
        this.getTransactionHistoryUseCase = getTransactionHistoryUseCase;
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

    @PostMapping("/accounts/{id}/deposit")
    public ResponseEntity<ApiResponse<AccountResponse>> deposit(@PathVariable Long id, @RequestBody TransactionRequest request) {
        try {
            Account account = depositUseCase.deposit(id, request.getAmount(), request.getDescription());
            return ResponseEntity.ok(ApiResponse.success("Deposit successful", AccountResponse.fromDomain(account)));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/accounts/{id}/withdraw")
    public ResponseEntity<ApiResponse<AccountResponse>> withdraw(@PathVariable Long id, @RequestBody TransactionRequest request) {
        try {
            Account account = withdrawUseCase.withdraw(id, request.getAmount(), request.getDescription());
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
}
