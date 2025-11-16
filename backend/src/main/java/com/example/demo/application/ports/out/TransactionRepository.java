package com.example.demo.application.ports.out;

import com.example.demo.domain.Transaction;

import java.util.List;

/**
 * Output port for transaction persistence
 */
public interface TransactionRepository {
    Transaction save(Transaction transaction);
    List<Transaction> findByAccountId(Long accountId);
    List<Transaction> findAll();
}
