package com.example.demo.adapter.in.web.admin;

import com.example.demo.adapter.in.web.auth.dto.UserResponse;
import com.example.demo.adapter.in.web.banking.dto.AccountResponse;
import com.example.demo.adapter.in.web.dto.ApiResponse;
import com.example.demo.application.ports.out.AccountRepository;
import com.example.demo.application.ports.out.UserRepository;
import com.example.demo.domain.Account;
import com.example.demo.domain.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Admin REST Controller
 * Handles administrative operations for users and accounts
 * Only accessible by users with ADMIN role
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    public AdminController(UserRepository userRepository, AccountRepository accountRepository) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
    }

    /**
     * GET /api/admin/users - Get all users
     */
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> users = userRepository.findAll().stream()
                .map(UserResponse::fromDomain)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", users));
    }

    /**
     * GET /api/admin/users/{id} - Get user by ID
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable UUID id) {
        return userRepository.findById(id)
                .map(user -> ResponseEntity.ok(ApiResponse.success("User retrieved successfully", UserResponse.fromDomain(user))))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("User not found")));
    }

    /**
     * PUT /api/admin/users/{id}/suspend - Suspend a user
     */
    @PutMapping("/users/{id}/suspend")
    public ResponseEntity<ApiResponse<UserResponse>> suspendUser(@PathVariable UUID id) {
        return userRepository.findById(id)
                .map(user -> {
                    User suspendedUser = user.suspend();
                    userRepository.update(suspendedUser);
                    return ResponseEntity.ok(ApiResponse.success("User suspended successfully", UserResponse.fromDomain(suspendedUser)));
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("User not found")));
    }

    /**
     * PUT /api/admin/users/{id}/activate - Activate a user
     */
    @PutMapping("/users/{id}/activate")
    public ResponseEntity<ApiResponse<UserResponse>> activateUser(@PathVariable UUID id) {
        return userRepository.findById(id)
                .map(user -> {
                    User activatedUser = user.activate();
                    userRepository.update(activatedUser);
                    return ResponseEntity.ok(ApiResponse.success("User activated successfully", UserResponse.fromDomain(activatedUser)));
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("User not found")));
    }

    /**
     * PUT /api/admin/users/{id}/lock - Lock a user
     */
    @PutMapping("/users/{id}/lock")
    public ResponseEntity<ApiResponse<UserResponse>> lockUser(@PathVariable UUID id) {
        return userRepository.findById(id)
                .map(user -> {
                    User lockedUser = user.lock();
                    userRepository.update(lockedUser);
                    return ResponseEntity.ok(ApiResponse.success("User locked successfully", UserResponse.fromDomain(lockedUser)));
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("User not found")));
    }

    /**
     * DELETE /api/admin/users/{id} - Delete a user
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID id) {
        if (userRepository.deleteById(id)) {
            return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("User not found"));
    }

    /**
     * GET /api/admin/accounts - Get all accounts
     */
    @GetMapping("/accounts")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getAllAccounts() {
        List<AccountResponse> accounts = accountRepository.findAll().stream()
                .map(AccountResponse::fromDomain)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Accounts retrieved successfully", accounts));
    }

    /**
     * GET /api/admin/accounts/{id} - Get account by ID
     */
    @GetMapping("/accounts/{id}")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountById(@PathVariable UUID id) {
        return accountRepository.findById(id)
                .map(account -> ResponseEntity.ok(ApiResponse.success("Account retrieved successfully", AccountResponse.fromDomain(account))))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Account not found")));
    }

    /**
     * GET /api/admin/users/{userId}/accounts - Get all accounts for a specific user
     */
    @GetMapping("/users/{userId}/accounts")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getAccountsByUserId(@PathVariable UUID userId) {
        List<AccountResponse> accounts = accountRepository.findByUserId(userId).stream()
                .map(AccountResponse::fromDomain)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Accounts retrieved successfully", accounts));
    }
}
