package com.example.demo.application.ports.in;

import com.example.demo.domain.Transaction;

import java.util.List;

/**
 * Input port for retrieving transaction history
 */
public interface GetTransactionHistoryUseCase {
    List<Transaction> getTransactionHistory(Long accountId);
    List<Transaction> getAllTransactions();
}
