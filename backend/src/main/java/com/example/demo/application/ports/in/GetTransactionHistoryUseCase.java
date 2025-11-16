package com.example.demo.application.ports.in;

import com.example.demo.domain.Transaction;

import java.util.List;
import java.util.UUID;

/**
 * Input port for retrieving transaction history
 */
public interface GetTransactionHistoryUseCase {
    List<Transaction> getTransactionHistory(UUID accountId);
    List<Transaction> getAllTransactions();
}
