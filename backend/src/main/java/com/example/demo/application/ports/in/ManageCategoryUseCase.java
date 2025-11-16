package com.example.demo.application.ports.in;

import com.example.demo.domain.TransactionCategory;

import java.util.List;
import java.util.Optional;

/**
 * Input port for managing transaction categories
 */
public interface ManageCategoryUseCase {

    /**
     * Create a new category
     */
    TransactionCategory createCategory(String name, String description,
                                      TransactionCategory.CategoryType type, String color);

    /**
     * Update an existing category
     */
    TransactionCategory updateCategory(Long id, String name, String description, String color);

    /**
     * Deactivate a category (soft delete)
     */
    TransactionCategory deactivateCategory(Long id);

    /**
     * Reactivate a category
     */
    TransactionCategory activateCategory(Long id);

    /**
     * Delete a category permanently
     */
    void deleteCategory(Long id);

    /**
     * Get category by ID
     */
    Optional<TransactionCategory> getCategoryById(Long id);

    /**
     * Get all categories
     */
    List<TransactionCategory> getAllCategories();

    /**
     * Get all active categories
     */
    List<TransactionCategory> getActiveCategories();

    /**
     * Get categories by type
     */
    List<TransactionCategory> getCategoriesByType(TransactionCategory.CategoryType type);

    /**
     * Get active categories by type
     */
    List<TransactionCategory> getActiveCategoriesByType(TransactionCategory.CategoryType type);
}
