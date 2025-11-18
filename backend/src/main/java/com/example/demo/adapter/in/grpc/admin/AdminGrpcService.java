package com.example.demo.adapter.in.grpc.admin;

import com.example.demo.application.service.AdminService;
import com.example.demo.application.service.BankingService;
import com.example.demo.domain.Account;
import com.example.demo.domain.User;
import com.example.demo.grpc.admin.*;
import com.example.demo.grpc.banking.AccountResponse;
import com.example.demo.grpc.common.Empty;
import com.example.demo.grpc.common.IdRequest;
import com.example.demo.grpc.common.UserResponse;
import com.google.protobuf.Timestamp;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * gRPC service adapter for admin operations
 * Requires ADMIN role for all operations
 * Follows hexagonal architecture pattern - this is an input adapter
 */
@GrpcService
public class AdminGrpcService extends AdminServiceGrpc.AdminServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(AdminGrpcService.class);

    private final AdminService adminService;
    private final BankingService bankingService;

    public AdminGrpcService(AdminService adminService, BankingService bankingService) {
        this.adminService = adminService;
        this.bankingService = bankingService;
    }

    @Override
    public void getAllUsers(Empty request, StreamObserver<GetAllUsersResponse> responseObserver) {
        try {
            logger.info("gRPC GetAllUsers request (Admin)");

            List<User> users = adminService.getAllUsers();

            GetAllUsersResponse response = GetAllUsersResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Users retrieved successfully")
                    .addAllUsers(users.stream()
                            .map(this::mapToUserResponse)
                            .collect(Collectors.toList()))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            logger.error("GetAllUsers error", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Failed to get users: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void getUser(IdRequest request, StreamObserver<GetUserResponse> responseObserver) {
        try {
            logger.info("gRPC GetUser request for id: {}", request.getId());

            User user = adminService.getUserById(UUID.fromString(request.getId()))
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            GetUserResponse response = GetUserResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("User retrieved successfully")
                    .setUser(mapToUserResponse(user))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (IllegalArgumentException e) {
            logger.error("GetUser error: {}", e.getMessage());
            responseObserver.onError(io.grpc.Status.NOT_FOUND
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            logger.error("GetUser error", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Failed to get user: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void suspendUser(IdRequest request, StreamObserver<SuspendUserResponse> responseObserver) {
        try {
            logger.info("gRPC SuspendUser request for id: {}", request.getId());

            User user = adminService.suspendUser(UUID.fromString(request.getId()));

            SuspendUserResponse response = SuspendUserResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("User suspended successfully")
                    .setUser(mapToUserResponse(user))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            logger.error("SuspendUser error", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Failed to suspend user: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void activateUser(IdRequest request, StreamObserver<ActivateUserResponse> responseObserver) {
        try {
            logger.info("gRPC ActivateUser request for id: {}", request.getId());

            User user = adminService.activateUser(UUID.fromString(request.getId()));

            ActivateUserResponse response = ActivateUserResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("User activated successfully")
                    .setUser(mapToUserResponse(user))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            logger.error("ActivateUser error", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Failed to activate user: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void lockUser(IdRequest request, StreamObserver<LockUserResponse> responseObserver) {
        try {
            logger.info("gRPC LockUser request for id: {}", request.getId());

            User user = adminService.lockUser(UUID.fromString(request.getId()));

            LockUserResponse response = LockUserResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("User locked successfully")
                    .setUser(mapToUserResponse(user))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            logger.error("LockUser error", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Failed to lock user: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void deleteUser(IdRequest request, StreamObserver<DeleteUserResponse> responseObserver) {
        try {
            logger.info("gRPC DeleteUser request for id: {}", request.getId());

            adminService.deleteUser(UUID.fromString(request.getId()));

            DeleteUserResponse response = DeleteUserResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("User deleted successfully")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            logger.error("DeleteUser error", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Failed to delete user: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void getAllAdminAccounts(Empty request, StreamObserver<GetAllAdminAccountsResponse> responseObserver) {
        try {
            logger.info("gRPC GetAllAdminAccounts request (Admin)");

            List<Account> accounts = bankingService.getAllAccounts();

            GetAllAdminAccountsResponse response = GetAllAdminAccountsResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Accounts retrieved successfully")
                    .addAllAccounts(accounts.stream()
                            .map(this::mapToAccountResponse)
                            .collect(Collectors.toList()))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            logger.error("GetAllAdminAccounts error", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Failed to get accounts: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void getAdminAccount(IdRequest request, StreamObserver<GetAdminAccountResponse> responseObserver) {
        try {
            logger.info("gRPC GetAdminAccount request for id: {}", request.getId());

            Account account = bankingService.getAccountById(UUID.fromString(request.getId()))
                    .orElseThrow(() -> new IllegalArgumentException("Account not found"));

            GetAdminAccountResponse response = GetAdminAccountResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Account retrieved successfully")
                    .setAccount(mapToAccountResponse(account))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (IllegalArgumentException e) {
            logger.error("GetAdminAccount error: {}", e.getMessage());
            responseObserver.onError(io.grpc.Status.NOT_FOUND
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            logger.error("GetAdminAccount error", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Failed to get account: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void getUserAccounts(IdRequest request, StreamObserver<GetUserAccountsResponse> responseObserver) {
        try {
            logger.info("gRPC GetUserAccounts request for user id: {}", request.getId());

            List<Account> accounts = bankingService.getAccountsByUserId(UUID.fromString(request.getId()));

            GetUserAccountsResponse response = GetUserAccountsResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("User accounts retrieved successfully")
                    .addAllAccounts(accounts.stream()
                            .map(this::mapToAccountResponse)
                            .collect(Collectors.toList()))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            logger.error("GetUserAccounts error", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Failed to get user accounts: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    /**
     * Maps domain User to gRPC UserResponse
     */
    private UserResponse mapToUserResponse(User user) {
        return UserResponse.newBuilder()
                .setId(user.getId().toString())
                .setEmail(user.getEmail())
                .setUsername(user.getUsername())
                .setFirstName(user.getFirstName())
                .setLastName(user.getLastName())
                .setRole(user.getRole().name())
                .setStatus(user.getStatus().name())
                .setCreatedAt(Timestamp.newBuilder()
                        .setSeconds(user.getCreatedAt().toEpochSecond(ZoneOffset.UTC))
                        .setNanos(user.getCreatedAt().getNano())
                        .build())
                .build();
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
}
