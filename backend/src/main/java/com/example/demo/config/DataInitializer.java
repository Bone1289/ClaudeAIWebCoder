package com.example.demo.config;

import com.example.demo.application.ports.in.CreateAccountUseCase;
import com.example.demo.application.ports.in.DepositUseCase;
import com.example.demo.application.ports.in.ManageCategoryUseCase;
import com.example.demo.application.ports.in.RegisterUserUseCase;
import com.example.demo.domain.Account;
import com.example.demo.domain.TransactionCategory;
import com.example.demo.domain.User;
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
            RegisterUserUseCase registerUserUseCase,
            CreateAccountUseCase createAccountUseCase,
            DepositUseCase depositUseCase,
            ManageCategoryUseCase manageCategoryUseCase) {

        return args -> {
            logger.info("Starting data initialization...");

            // Initialize Categories
            initializeCategories(manageCategoryUseCase);

            // Initialize Users and Accounts
            initializeUsersAndAccounts(registerUserUseCase, createAccountUseCase, depositUseCase);

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

    private void initializeUsersAndAccounts(RegisterUserUseCase registerUserUseCase,
                                           CreateAccountUseCase createAccountUseCase,
                                           DepositUseCase depositUseCase) {
        logger.info("Initializing sample users and accounts...");

        try {
            // Create User 1: Demo User
            User demoUser = registerUserUseCase.registerUser(
                "demo@example.com",
                "demo",
                "password123",
                "Demo",
                "User"
            );
            logger.info("Created user: {} ({})", demoUser.getUsername(), demoUser.getEmail());

            Account demoAccount = createAccountUseCase.createAccount(
                demoUser.getId(),
                "Demo",
                "User",
                "United States",
                "CHECKING"
            );
            depositUseCase.deposit(demoAccount.getId(), new BigDecimal("1000.00"), "Initial deposit");
            logger.info("Created CHECKING account for Demo User with initial balance $1000");

            // Create User 2: John Doe (with multiple accounts)
            User johnDoe = registerUserUseCase.registerUser(
                "john.doe@example.com",
                "johndoe",
                "password123",
                "John",
                "Doe"
            );
            logger.info("Created user: {} ({})", johnDoe.getUsername(), johnDoe.getEmail());

            Account johnChecking = createAccountUseCase.createAccount(
                johnDoe.getId(),
                "John",
                "Doe",
                "United States",
                "CHECKING"
            );
            depositUseCase.deposit(johnChecking.getId(), new BigDecimal("5000.00"), "Initial deposit");
            logger.info("Created CHECKING account for John Doe with initial balance $5000");

            Account johnSavings = createAccountUseCase.createAccount(
                johnDoe.getId(),
                "John",
                "Doe",
                "United States",
                "SAVINGS"
            );
            depositUseCase.deposit(johnSavings.getId(), new BigDecimal("15000.00"), "Initial deposit");
            logger.info("Created SAVINGS account for John Doe with initial balance $15000");

            // Create User 3: Jane Smith
            User janeSmith = registerUserUseCase.registerUser(
                "jane.smith@example.com",
                "janesmith",
                "password123",
                "Jane",
                "Smith"
            );
            logger.info("Created user: {} ({})", janeSmith.getUsername(), janeSmith.getEmail());

            Account janeAccount = createAccountUseCase.createAccount(
                janeSmith.getId(),
                "Jane",
                "Smith",
                "United Kingdom",
                "SAVINGS"
            );
            depositUseCase.deposit(janeAccount.getId(), new BigDecimal("10000.00"), "Initial deposit");
            logger.info("Created SAVINGS account for Jane Smith with initial balance $10000");

            // Create User 4: Carlos Rodriguez
            User carlosRodriguez = registerUserUseCase.registerUser(
                "carlos.rodriguez@example.com",
                "carlosr",
                "password123",
                "Carlos",
                "Rodriguez"
            );
            logger.info("Created user: {} ({})", carlosRodriguez.getUsername(), carlosRodriguez.getEmail());

            Account carlosAccount = createAccountUseCase.createAccount(
                carlosRodriguez.getId(),
                "Carlos",
                "Rodriguez",
                "Spain",
                "CHECKING"
            );
            depositUseCase.deposit(carlosAccount.getId(), new BigDecimal("3500.50"), "Initial deposit");
            logger.info("Created CHECKING account for Carlos Rodriguez with initial balance $3500.50");

            // Create User 5: Yuki Tanaka
            User yukiTanaka = registerUserUseCase.registerUser(
                "yuki.tanaka@example.com",
                "yukitanaka",
                "password123",
                "Yuki",
                "Tanaka"
            );
            logger.info("Created user: {} ({})", yukiTanaka.getUsername(), yukiTanaka.getEmail());

            Account yukiAccount = createAccountUseCase.createAccount(
                yukiTanaka.getId(),
                "Yuki",
                "Tanaka",
                "Japan",
                "SAVINGS"
            );
            depositUseCase.deposit(yukiAccount.getId(), new BigDecimal("7500.00"), "Initial deposit");
            logger.info("Created SAVINGS account for Yuki Tanaka with initial balance $7500");

            logger.info("Created 5 users with 6 total bank accounts (John has 2 accounts)");

        } catch (Exception e) {
            logger.warn("Users/Accounts might already exist: {}", e.getMessage());
        }
    }
}
