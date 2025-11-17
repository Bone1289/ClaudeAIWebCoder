package com.example.demo.config;

import com.example.demo.application.ports.in.CreateAccountUseCase;
import com.example.demo.application.ports.in.DepositUseCase;
import com.example.demo.application.ports.in.ManageCategoryUseCase;
import com.example.demo.domain.Account;
import com.example.demo.domain.TransactionCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;

/**
 * Data Initializer for seeding initial data into the database
 * Only runs in 'dev' and 'test' profiles
 */
@Configuration
@Profile({"dev", "test"})
public class DataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    CommandLineRunner initDatabase(
            CreateAccountUseCase createAccountUseCase,
            DepositUseCase depositUseCase,
            ManageCategoryUseCase manageCategoryUseCase) {

        return args -> {
            logger.info("Starting data initialization...");

            // Initialize Categories
            initializeCategories(manageCategoryUseCase);

            // Initialize Accounts
            initializeAccounts(createAccountUseCase, depositUseCase);

            logger.info("Data initialization completed successfully!");
        };
    }

    private void initializeCategories(ManageCategoryUseCase manageCategoryUseCase) {
        logger.info("Initializing transaction categories...");

        // Check if categories already exist
        if (!manageCategoryUseCase.getAllCategories().isEmpty()) {
            logger.info("Categories already exist, skipping initialization");
            return;
        }

        // Income Categories
        manageCategoryUseCase.createCategory(
            "SALARY",
            "Monthly salary and wages",
            TransactionCategory.CategoryType.INCOME,
            "#2ecc71"
        );

        manageCategoryUseCase.createCategory(
            "FREELANCE",
            "Freelance and consulting income",
            TransactionCategory.CategoryType.INCOME,
            "#27ae60"
        );

        manageCategoryUseCase.createCategory(
            "INVESTMENT",
            "Investment returns and dividends",
            TransactionCategory.CategoryType.INCOME,
            "#16a085"
        );

        manageCategoryUseCase.createCategory(
            "BONUS",
            "Performance bonuses and rewards",
            TransactionCategory.CategoryType.INCOME,
            "#1abc9c"
        );

        // Expense Categories
        manageCategoryUseCase.createCategory(
            "GROCERIES",
            "Food and household supplies",
            TransactionCategory.CategoryType.EXPENSE,
            "#e74c3c"
        );

        manageCategoryUseCase.createCategory(
            "UTILITIES",
            "Electricity, water, gas, internet",
            TransactionCategory.CategoryType.EXPENSE,
            "#c0392b"
        );

        manageCategoryUseCase.createCategory(
            "RENT",
            "Housing rent or mortgage",
            TransactionCategory.CategoryType.EXPENSE,
            "#e67e22"
        );

        manageCategoryUseCase.createCategory(
            "TRANSPORTATION",
            "Public transport, fuel, car maintenance",
            TransactionCategory.CategoryType.EXPENSE,
            "#d35400"
        );

        manageCategoryUseCase.createCategory(
            "ENTERTAINMENT",
            "Movies, dining, hobbies",
            TransactionCategory.CategoryType.EXPENSE,
            "#9b59b6"
        );

        manageCategoryUseCase.createCategory(
            "HEALTHCARE",
            "Medical expenses and insurance",
            TransactionCategory.CategoryType.EXPENSE,
            "#8e44ad"
        );

        manageCategoryUseCase.createCategory(
            "EDUCATION",
            "Courses, books, training",
            TransactionCategory.CategoryType.EXPENSE,
            "#3498db"
        );

        manageCategoryUseCase.createCategory(
            "SHOPPING",
            "Clothing, electronics, general shopping",
            TransactionCategory.CategoryType.EXPENSE,
            "#2980b9"
        );

        logger.info("Created 12 transaction categories");
    }

    private void initializeAccounts(CreateAccountUseCase createAccountUseCase, DepositUseCase depositUseCase) {
        logger.info("Initializing sample accounts...");

        // Check if accounts already exist
        try {
            // Create Account 1: John Doe
            Account account1 = createAccountUseCase.createAccount(
                "John",
                "Doe",
                "United States",
                "CHECKING"
            );
            depositUseCase.deposit(account1.getId(), new BigDecimal("5000.00"), "Initial deposit");
            logger.info("Created account for John Doe with initial balance $5000");

            // Create Account 2: Jane Smith
            Account account2 = createAccountUseCase.createAccount(
                "Jane",
                "Smith",
                "United Kingdom",
                "SAVINGS"
            );
            depositUseCase.deposit(account2.getId(), new BigDecimal("10000.00"), "Initial deposit");
            logger.info("Created account for Jane Smith with initial balance $10000");

            // Create Account 3: Carlos Rodriguez
            Account account3 = createAccountUseCase.createAccount(
                "Carlos",
                "Rodriguez",
                "Spain",
                "CHECKING"
            );
            depositUseCase.deposit(account3.getId(), new BigDecimal("3500.50"), "Initial deposit");
            logger.info("Created account for Carlos Rodriguez with initial balance $3500.50");

            // Create Account 4: Yuki Tanaka
            Account account4 = createAccountUseCase.createAccount(
                "Yuki",
                "Tanaka",
                "Japan",
                "SAVINGS"
            );
            depositUseCase.deposit(account4.getId(), new BigDecimal("7500.00"), "Initial deposit");
            logger.info("Created account for Yuki Tanaka with initial balance $7500");

            // Create Account 5: Emma Johnson
            Account account5 = createAccountUseCase.createAccount(
                "Emma",
                "Johnson",
                "Canada",
                "CREDIT"
            );
            depositUseCase.deposit(account5.getId(), new BigDecimal("2000.00"), "Initial deposit");
            logger.info("Created account for Emma Johnson with initial balance $2000");

        } catch (Exception e) {
            logger.warn("Accounts might already exist: {}", e.getMessage());
        }
    }
}
