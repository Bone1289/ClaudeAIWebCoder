package com.example.demo.adapter.in.web;

import com.example.demo.adapter.in.web.dto.ApiResponse;
import com.example.demo.adapter.in.web.dto.CreateUserRequest;
import com.example.demo.adapter.in.web.dto.UpdateUserRequest;
import com.example.demo.adapter.in.web.dto.UserResponse;
import com.example.demo.application.ports.in.CreateUserUseCase;
import com.example.demo.application.ports.in.DeleteUserUseCase;
import com.example.demo.application.ports.in.GetUserUseCase;
import com.example.demo.application.ports.in.UpdateUserUseCase;
import com.example.demo.domain.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller - Input Adapter
 * This adapter converts HTTP requests to use case calls
 * It depends on use case interfaces (ports), not on concrete implementations
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final CreateUserUseCase createUserUseCase;
    private final GetUserUseCase getUserUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final DeleteUserUseCase deleteUserUseCase;

    public UserController(
            CreateUserUseCase createUserUseCase,
            GetUserUseCase getUserUseCase,
            UpdateUserUseCase updateUserUseCase,
            DeleteUserUseCase deleteUserUseCase) {
        this.createUserUseCase = createUserUseCase;
        this.getUserUseCase = getUserUseCase;
        this.updateUserUseCase = updateUserUseCase;
        this.deleteUserUseCase = deleteUserUseCase;
    }

    /**
     * GET /api/users - Get all users
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> users = getUserUseCase.getAllUsers()
                .stream()
                .map(UserResponse::fromDomain)
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                ApiResponse.success("Users retrieved successfully", users)
        );
    }

    /**
     * GET /api/users/{id} - Get user by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        return getUserUseCase.getUserById(id)
                .map(user -> ResponseEntity.ok(
                        ApiResponse.success("User found", UserResponse.fromDomain(user))
                ))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("User not found with id: " + id)));
    }

    /**
     * POST /api/users - Create a new user
     */
    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@RequestBody CreateUserRequest request) {
        try {
            User user = createUserUseCase.createUser(
                    request.getName(),
                    request.getEmail(),
                    request.getRole()
            );

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(
                            "User created successfully",
                            UserResponse.fromDomain(user)
                    ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * PUT /api/users/{id} - Update an existing user
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @RequestBody UpdateUserRequest request) {

        try {
            return updateUserUseCase.updateUser(
                            id,
                            request.getName(),
                            request.getEmail(),
                            request.getRole()
                    )
                    .map(user -> ResponseEntity.ok(
                            ApiResponse.success(
                                    "User updated successfully",
                                    UserResponse.fromDomain(user)
                            )
                    ))
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(ApiResponse.error("User not found with id: " + id)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * DELETE /api/users/{id} - Delete a user
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        boolean deleted = deleteUserUseCase.deleteUser(id);

        if (deleted) {
            return ResponseEntity.ok(
                    ApiResponse.success("User deleted successfully", null)
            );
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("User not found with id: " + id));
        }
    }
}
