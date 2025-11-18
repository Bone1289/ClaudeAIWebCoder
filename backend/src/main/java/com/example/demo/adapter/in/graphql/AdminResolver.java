package com.example.demo.adapter.in.graphql;

import com.example.demo.adapter.in.graphql.dto.AccountDTO;
import com.example.demo.adapter.in.graphql.dto.UserDTO;
import com.example.demo.application.ports.out.AccountRepository;
import com.example.demo.application.ports.out.UserRepository;
import com.example.demo.domain.User;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * GraphQL Resolver for Admin operations
 * All operations require ADMIN role
 */
@Controller
@PreAuthorize("hasRole('ADMIN')")
public class AdminResolver {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    public AdminResolver(UserRepository userRepository, AccountRepository accountRepository) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
    }

    // ==================== Queries ====================

    @QueryMapping
    public List<UserDTO> adminUsers() {
        return userRepository.findAll().stream()
                .map(UserDTO::fromDomain)
                .collect(Collectors.toList());
    }

    @QueryMapping
    public UserDTO adminUser(@Argument UUID id) {
        return userRepository.findById(id)
                .map(UserDTO::fromDomain)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @QueryMapping
    public List<AccountDTO> adminAccounts() {
        return accountRepository.findAll().stream()
                .map(AccountDTO::fromDomain)
                .collect(Collectors.toList());
    }

    @QueryMapping
    public AccountDTO adminAccount(@Argument UUID id) {
        return accountRepository.findById(id)
                .map(AccountDTO::fromDomain)
                .orElseThrow(() -> new RuntimeException("Account not found"));
    }

    @QueryMapping
    public List<AccountDTO> adminUserAccounts(@Argument UUID userId) {
        return accountRepository.findByUserId(userId).stream()
                .map(AccountDTO::fromDomain)
                .collect(Collectors.toList());
    }

    // ==================== Mutations ====================

    @MutationMapping
    public UserDTO adminSuspendUser(@Argument UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        User suspendedUser = user.suspend();
        userRepository.update(suspendedUser);
        return UserDTO.fromDomain(suspendedUser);
    }

    @MutationMapping
    public UserDTO adminActivateUser(@Argument UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        User activatedUser = user.activate();
        userRepository.update(activatedUser);
        return UserDTO.fromDomain(activatedUser);
    }

    @MutationMapping
    public UserDTO adminLockUser(@Argument UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        User lockedUser = user.lock();
        userRepository.update(lockedUser);
        return UserDTO.fromDomain(lockedUser);
    }

    @MutationMapping
    public Boolean adminDeleteUser(@Argument UUID id) {
        return userRepository.deleteById(id);
    }
}
