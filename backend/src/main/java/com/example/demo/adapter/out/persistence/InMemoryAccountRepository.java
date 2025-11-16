package com.example.demo.adapter.out.persistence;

import com.example.demo.application.ports.out.AccountRepository;
import com.example.demo.domain.Account;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * In-memory implementation of AccountRepository
 * Output adapter for account persistence
 */
@Repository
public class InMemoryAccountRepository implements AccountRepository {

    private final Map<Long, Account> accounts = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);
    private final AtomicLong accountNumberCounter = new AtomicLong(100000001L);

    public InMemoryAccountRepository() {
        // Initialize with sample accounts
        Account account1 = Account.of(
                idCounter.getAndIncrement(),
                "ACC100000001",
                1L, // Customer ID
                "CHECKING",
                new BigDecimal("1000.00"),
                Account.AccountStatus.ACTIVE,
                LocalDateTime.now().minusDays(30),
                LocalDateTime.now()
        );

        Account account2 = Account.of(
                idCounter.getAndIncrement(),
                "ACC100000002",
                1L, // Customer ID
                "SAVINGS",
                new BigDecimal("5000.00"),
                Account.AccountStatus.ACTIVE,
                LocalDateTime.now().minusDays(60),
                LocalDateTime.now()
        );

        Account account3 = Account.of(
                idCounter.getAndIncrement(),
                "ACC100000003",
                2L, // Different customer
                "CHECKING",
                new BigDecimal("750.50"),
                Account.AccountStatus.ACTIVE,
                LocalDateTime.now().minusDays(15),
                LocalDateTime.now()
        );

        accounts.put(account1.getId(), account1);
        accounts.put(account2.getId(), account2);
        accounts.put(account3.getId(), account3);

        accountNumberCounter.set(100000004L);
    }

    @Override
    public Account save(Account account) {
        Long id = idCounter.getAndIncrement();
        Account savedAccount = Account.of(
                id,
                account.getAccountNumber(),
                account.getCustomerId(),
                account.getAccountType(),
                account.getBalance(),
                account.getStatus(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
        accounts.put(id, savedAccount);
        return savedAccount;
    }

    @Override
    public Optional<Account> findById(Long id) {
        return Optional.ofNullable(accounts.get(id));
    }

    @Override
    public Optional<Account> findByAccountNumber(String accountNumber) {
        return accounts.values().stream()
                .filter(account -> account.getAccountNumber().equals(accountNumber))
                .findFirst();
    }

    @Override
    public List<Account> findAll() {
        return new ArrayList<>(accounts.values());
    }

    @Override
    public List<Account> findByCustomerId(Long customerId) {
        return accounts.values().stream()
                .filter(account -> account.getCustomerId().equals(customerId))
                .collect(Collectors.toList());
    }

    @Override
    public Account update(Account account) {
        if (account.getId() == null || !accounts.containsKey(account.getId())) {
            throw new IllegalArgumentException("Cannot update account: account not found");
        }
        accounts.put(account.getId(), account);
        return account;
    }

    @Override
    public boolean deleteById(Long id) {
        return accounts.remove(id) != null;
    }

    @Override
    public String generateAccountNumber() {
        return "ACC" + accountNumberCounter.getAndIncrement();
    }
}
