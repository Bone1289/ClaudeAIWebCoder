package com.example.demo.domain;

import java.time.LocalDateTime;

/**
 * Transaction Category domain entity
 * Represents a categorization for transactions (e.g., SALARY, GROCERIES, UTILITIES)
 */
public class TransactionCategory {

    private Long id;
    private String name;
    private String description;
    private CategoryType type;  // INCOME or EXPENSE
    private String color;  // Hex color for UI display
    private boolean active;
    private LocalDateTime createdAt;

    public enum CategoryType {
        INCOME,
        EXPENSE
    }

    // Private constructor to enforce factory methods
    private TransactionCategory(Long id, String name, String description, CategoryType type,
                                String color, boolean active, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.color = color;
        this.active = active;
        this.createdAt = createdAt;
    }

    /**
     * Factory method to create a new category
     */
    public static TransactionCategory create(String name, String description, CategoryType type, String color) {
        validateName(name);
        validateType(type);

        return new TransactionCategory(
            null,
            name.toUpperCase(),
            description,
            type,
            color != null ? color : "#3498db",
            true,
            LocalDateTime.now()
        );
    }

    /**
     * Factory method to reconstitute from persistence
     */
    public static TransactionCategory reconstitute(Long id, String name, String description,
                                                   CategoryType type, String color,
                                                   boolean active, LocalDateTime createdAt) {
        return new TransactionCategory(id, name, description, type, color, active, createdAt);
    }

    /**
     * Update category details
     */
    public TransactionCategory update(String name, String description, String color) {
        validateName(name);

        return new TransactionCategory(
            this.id,
            name.toUpperCase(),
            description,
            this.type,
            color != null ? color : this.color,
            this.active,
            this.createdAt
        );
    }

    /**
     * Deactivate category (soft delete)
     */
    public TransactionCategory deactivate() {
        return new TransactionCategory(
            this.id,
            this.name,
            this.description,
            this.type,
            this.color,
            false,
            this.createdAt
        );
    }

    /**
     * Reactivate category
     */
    public TransactionCategory activate() {
        return new TransactionCategory(
            this.id,
            this.name,
            this.description,
            this.type,
            this.color,
            true,
            this.createdAt
        );
    }

    private static void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be empty");
        }
        if (name.length() > 50) {
            throw new IllegalArgumentException("Category name cannot exceed 50 characters");
        }
    }

    private static void validateType(CategoryType type) {
        if (type == null) {
            throw new IllegalArgumentException("Category type cannot be null");
        }
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public CategoryType getType() {
        return type;
    }

    public String getColor() {
        return color;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
