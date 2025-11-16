package com.example.demo.adapter.out.persistence;

import com.example.demo.application.ports.out.TransactionRepository;
import com.example.demo.domain.Transaction;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * In-memory implementation of TransactionRepository
 * Output adapter for transaction persistence
 */
@Repository
public class InMemoryTransactionRepository implements TransactionRepository {

    private final Map<Long, Transaction> transactions = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    @Override
    public Transaction save(Transaction transaction) {
        Long id = idCounter.getAndIncrement();
        Transaction savedTransaction = Transaction.of(
                id,
                transaction.getAccountId(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getBalanceAfter(),
                transaction.getDescription(),
                transaction.getRelatedAccountId(),
                transaction.getCreatedAt()
        );
        transactions.put(id, savedTransaction);
        return savedTransaction;
    }

    @Override
    public List<Transaction> findByAccountId(Long accountId) {
        return transactions.values().stream()
                .filter(transaction -> transaction.getAccountId().equals(accountId))
                .sorted((t1, t2) -> t2.getCreatedAt().compareTo(t1.getCreatedAt())) // Most recent first
                .collect(Collectors.toList());
    }

    @Override
    public List<Transaction> findAll() {
        return new ArrayList<>(transactions.values()).stream()
                .sorted((t1, t2) -> t2.getCreatedAt().compareTo(t1.getCreatedAt()))
                .collect(Collectors.toList());
    }
}
