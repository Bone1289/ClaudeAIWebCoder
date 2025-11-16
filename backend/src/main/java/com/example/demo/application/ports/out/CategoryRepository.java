package com.example.demo.application.ports.out;

import com.example.demo.domain.TransactionCategory;

import java.util.List;
import java.util.Optional;

/**
 * Output port for category persistence
 */
public interface CategoryRepository {

    /**
     * Save a new category
     */
    TransactionCategory save(TransactionCategory category);

    /**
     * Update an existing category
     */
    TransactionCategory update(TransactionCategory category);

    /**
     * Find category by ID
     */
    Optional<TransactionCategory> findById(Long id);

    /**
     * Find category by name
     */
    Optional<TransactionCategory> findByName(String name);

    /**
     * Get all categories
     */
    List<TransactionCategory> findAll();

    /**
     * Get all active categories
     */
    List<TransactionCategory> findAllActive();

    /**
     * Get categories by type (INCOME or EXPENSE)
     */
    List<TransactionCategory> findByType(TransactionCategory.CategoryType type);

    /**
     * Get active categories by type
     */
    List<TransactionCategory> findActiveByType(TransactionCategory.CategoryType type);

    /**
     * Check if category name exists
     */
    boolean existsByName(String name);

    /**
     * Delete category
     */
    void delete(Long id);
}
