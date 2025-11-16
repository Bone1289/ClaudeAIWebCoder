package com.example.demo.adapter.out.persistence;

import com.example.demo.application.ports.out.CategoryRepository;
import com.example.demo.application.ports.out.TransactionRepository;
import com.example.demo.domain.CategoryReport;
import com.example.demo.domain.Transaction;
import com.example.demo.domain.TransactionCategory;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
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
    private final CategoryRepository categoryRepository;

    public InMemoryTransactionRepository(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Transaction save(Transaction transaction) {
        Long id = idCounter.getAndIncrement();
        Transaction savedTransaction = Transaction.of(
                id,
                transaction.getAccountId(),
                transaction.getType(),
                transaction.getCategoryId(),
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

    @Override
    public List<Transaction> findByAccountIdAndDateRange(Long accountId, LocalDateTime startDate, LocalDateTime endDate) {
        return transactions.values().stream()
                .filter(transaction -> transaction.getAccountId().equals(accountId))
                .filter(transaction -> !transaction.getCreatedAt().isBefore(startDate) &&
                                      !transaction.getCreatedAt().isAfter(endDate))
                .sorted((t1, t2) -> t2.getCreatedAt().compareTo(t1.getCreatedAt()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Transaction> findByAccountIdAndCategoryId(Long accountId, Long categoryId) {
        return transactions.values().stream()
                .filter(transaction -> transaction.getAccountId().equals(accountId))
                .filter(transaction -> categoryId.equals(transaction.getCategoryId()))
                .sorted((t1, t2) -> t2.getCreatedAt().compareTo(t1.getCreatedAt()))
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryReport.CategorySummary> getCategorySummary(Long accountId, Transaction.TransactionType type) {
        // Get all transactions for the account with the specified type
        List<Transaction> filteredTransactions = transactions.values().stream()
                .filter(t -> t.getAccountId().equals(accountId))
                .filter(t -> t.getType() == type)
                .collect(Collectors.toList());

        // Calculate total amount for percentage calculation
        BigDecimal totalAmount = filteredTransactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Group by category ID and create summaries
        Map<Long, List<Transaction>> transactionsByCategory = filteredTransactions.stream()
                .filter(t -> t.getCategoryId() != null)
                .collect(Collectors.groupingBy(Transaction::getCategoryId));

        List<CategoryReport.CategorySummary> summaries = new ArrayList<>();

        for (Map.Entry<Long, List<Transaction>> entry : transactionsByCategory.entrySet()) {
            Long categoryId = entry.getKey();
            List<Transaction> categoryTransactions = entry.getValue();

            // Get the full category object from repository
            TransactionCategory category = categoryRepository.findById(categoryId).orElse(null);
            if (category == null) {
                continue; // Skip if category not found
            }

            // Calculate amount and count for this category
            BigDecimal categoryAmount = categoryTransactions.stream()
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            int count = categoryTransactions.size();

            // Calculate percentage
            BigDecimal percentage = totalAmount.compareTo(BigDecimal.ZERO) > 0
                    ? categoryAmount.multiply(BigDecimal.valueOf(100))
                        .divide(totalAmount, 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            summaries.add(new CategoryReport.CategorySummary(category, categoryAmount, count, percentage));
        }

        // Sort by amount descending
        summaries.sort((s1, s2) -> s2.amount().compareTo(s1.amount()));

        return summaries;
    }
}
