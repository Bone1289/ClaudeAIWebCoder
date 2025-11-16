-- Virtual Bank Sample Data Seeding Script
-- Run this after the application has created the tables (ddl-auto=update)
-- This will populate the database with sample categories and test data

USE virtualbank;

-- Insert sample transaction categories
-- Note: The application automatically seeds categories via CategoryDataSeeder.java
-- This script is for manual seeding if needed

-- Income Categories
INSERT IGNORE INTO transaction_category (name, description, type, color, active, created_at) VALUES
('SALARY', 'Monthly salary income', 'INCOME', '#2ecc71', true, NOW()),
('FREELANCE', 'Freelance project income', 'INCOME', '#27ae60', true, NOW()),
('INVESTMENT', 'Investment returns and dividends', 'INCOME', '#16a085', true, NOW()),
('GIFT', 'Gifts and bonuses received', 'INCOME', '#1abc9c', true, NOW()),
('OTHER_INCOME', 'Other miscellaneous income', 'INCOME', '#3498db', true, NOW());

-- Expense Categories
INSERT IGNORE INTO transaction_category (name, description, type, color, active, created_at) VALUES
('GROCERIES', 'Supermarket and food shopping', 'EXPENSE', '#e74c3c', true, NOW()),
('UTILITIES', 'Electricity, water, gas, internet', 'EXPENSE', '#c0392b', true, NOW()),
('RENT', 'Monthly rent payment', 'EXPENSE', '#e67e22', true, NOW()),
('TRANSPORT', 'Public transport and fuel costs', 'EXPENSE', '#d35400', true, NOW()),
('ENTERTAINMENT', 'Movies, dining out, hobbies', 'EXPENSE', '#f39c12', true, NOW()),
('HEALTHCARE', 'Medical expenses and insurance', 'EXPENSE', '#9b59b6', true, NOW()),
('EDUCATION', 'Courses, books, tuition fees', 'EXPENSE', '#8e44ad', true, NOW()),
('SHOPPING', 'Clothing and general shopping', 'EXPENSE', '#34495e', true, NOW()),
('OTHER_EXPENSE', 'Other miscellaneous expenses', 'EXPENSE', '#95a5a6', true, NOW());

-- Insert sample users (optional for testing)
INSERT IGNORE INTO user (name, email, role) VALUES
('John Doe', 'john.doe@example.com', 'USER'),
('Jane Smith', 'jane.smith@example.com', 'ADMIN'),
('Test User', 'test@example.com', 'USER');

-- Insert sample accounts (optional for testing)
-- Note: You may need to adjust customer_id based on your user IDs
INSERT IGNORE INTO account (account_number, customer_id, account_type, balance, status, created_at, updated_at) VALUES
('ACC001', 1, 'CHECKING', 5000.00, 'ACTIVE', NOW(), NOW()),
('ACC002', 1, 'SAVINGS', 15000.00, 'ACTIVE', NOW(), NOW()),
('ACC003', 2, 'CHECKING', 3000.00, 'ACTIVE', NOW(), NOW());

SELECT 'Sample data seeded successfully!' AS Status;
