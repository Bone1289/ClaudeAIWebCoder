package com.example.demo.application.service;

import com.example.demo.application.ports.in.ManageCategoryUseCase;
import com.example.demo.application.ports.out.CategoryRepository;
import com.example.demo.domain.TransactionCategory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Application service for managing transaction categories
 */
@Service
@Transactional
public class CategoryService implements ManageCategoryUseCase {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public TransactionCategory createCategory(String name, String description,
                                             TransactionCategory.CategoryType type, String color) {
        // Check if category with same name already exists
        if (categoryRepository.existsByName(name.toUpperCase())) {
            throw new IllegalArgumentException("Category with name '" + name + "' already exists");
        }

        TransactionCategory category = TransactionCategory.create(name, description, type, color);
        return categoryRepository.save(category);
    }

    @Override
    public TransactionCategory updateCategory(UUID id, String name, String description, String color) {
        TransactionCategory category = categoryRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + id));

        // Check if new name conflicts with another category
        if (!category.getName().equalsIgnoreCase(name)) {
            if (categoryRepository.existsByName(name.toUpperCase())) {
                throw new IllegalArgumentException("Category with name '" + name + "' already exists");
            }
        }

        TransactionCategory updated = category.update(name, description, color);
        return categoryRepository.update(updated);
    }

    @Override
    public TransactionCategory deactivateCategory(UUID id) {
        TransactionCategory category = categoryRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + id));

        TransactionCategory deactivated = category.deactivate();
        return categoryRepository.update(deactivated);
    }

    @Override
    public TransactionCategory activateCategory(UUID id) {
        TransactionCategory category = categoryRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + id));

        TransactionCategory activated = category.activate();
        return categoryRepository.update(activated);
    }

    @Override
    public void deleteCategory(UUID id) {
        if (!categoryRepository.findById(id).isPresent()) {
            throw new IllegalArgumentException("Category not found with ID: " + id);
        }
        categoryRepository.delete(id);
    }

    @Override
    public Optional<TransactionCategory> getCategoryById(UUID id) {
        return categoryRepository.findById(id);
    }

    @Override
    public List<TransactionCategory> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public List<TransactionCategory> getActiveCategories() {
        return categoryRepository.findAllActive();
    }

    @Override
    public List<TransactionCategory> getCategoriesByType(TransactionCategory.CategoryType type) {
        return categoryRepository.findByType(type);
    }

    @Override
    public List<TransactionCategory> getActiveCategoriesByType(TransactionCategory.CategoryType type) {
        return categoryRepository.findActiveByType(type);
    }
}
