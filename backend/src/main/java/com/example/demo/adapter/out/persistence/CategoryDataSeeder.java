package com.example.demo.adapter.out.persistence;

import com.example.demo.application.ports.out.CategoryRepository;
import com.example.demo.domain.TransactionCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Seeds default transaction categories into the database
 * Only runs in dev profile
 */
@Component
@Profile("dev")
public class CategoryDataSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(CategoryDataSeeder.class);

    private final CategoryRepository categoryRepository;

    public CategoryDataSeeder(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(String... args) {
        if (categoryRepository.findAll().isEmpty()) {
            logger.info("Seeding default transaction categories...");
            seedDefaultCategories();
            logger.info("Default categories seeded successfully!");
        } else {
            logger.info("Categories already exist, skipping seed.");
        }
    }

    private void seedDefaultCategories() {
        // Income categories
        createCategoryIfNotExists("SALARY", "Regular salary or wages", TransactionCategory.CategoryType.INCOME, "#27ae60");
        createCategoryIfNotExists("INVESTMENT", "Investment returns and dividends", TransactionCategory.CategoryType.INCOME, "#16a085");
        createCategoryIfNotExists("REFUND", "Refunds and reimbursements", TransactionCategory.CategoryType.INCOME, "#3498db");
        createCategoryIfNotExists("FREELANCE", "Freelance or contract work", TransactionCategory.CategoryType.INCOME, "#9b59b6");
        createCategoryIfNotExists("BONUS", "Bonuses and commissions", TransactionCategory.CategoryType.INCOME, "#f39c12");

        // Expense categories
        createCategoryIfNotExists("GROCERIES", "Food and groceries", TransactionCategory.CategoryType.EXPENSE, "#e74c3c");
        createCategoryIfNotExists("UTILITIES", "Utility bills (electric, water, gas)", TransactionCategory.CategoryType.EXPENSE, "#e67e22");
        createCategoryIfNotExists("RENT", "Rent or mortgage payments", TransactionCategory.CategoryType.EXPENSE, "#c0392b");
        createCategoryIfNotExists("ENTERTAINMENT", "Entertainment and leisure", TransactionCategory.CategoryType.EXPENSE, "#9b59b6");
        createCategoryIfNotExists("HEALTHCARE", "Medical and healthcare expenses", TransactionCategory.CategoryType.EXPENSE, "#1abc9c");
        createCategoryIfNotExists("TRANSPORTATION", "Transport and fuel costs", TransactionCategory.CategoryType.EXPENSE, "#3498db");
        createCategoryIfNotExists("SHOPPING", "Shopping and personal items", TransactionCategory.CategoryType.EXPENSE, "#e91e63");
        createCategoryIfNotExists("DINING", "Dining out and restaurants", TransactionCategory.CategoryType.EXPENSE, "#ff5722");
        createCategoryIfNotExists("EDUCATION", "Education and courses", TransactionCategory.CategoryType.EXPENSE, "#2196f3");
        createCategoryIfNotExists("INSURANCE", "Insurance premiums", TransactionCategory.CategoryType.EXPENSE, "#607d8b");

        // Other
        createCategoryIfNotExists("OTHER", "Other miscellaneous transactions", TransactionCategory.CategoryType.INCOME, "#95a5a6");
        createCategoryIfNotExists("TRANSFER", "Transfer between accounts", TransactionCategory.CategoryType.EXPENSE, "#7f8c8d");
    }

    private void createCategoryIfNotExists(String name, String description,
                                          TransactionCategory.CategoryType type, String color) {
        if (!categoryRepository.existsByName(name)) {
            TransactionCategory category = TransactionCategory.create(name, description, type, color);
            categoryRepository.save(category);
            logger.debug("Created category: {}", name);
        }
    }
}
