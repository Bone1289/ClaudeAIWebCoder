-- Virtual Bank Database Initialization Script
-- This script creates the database and user for local development

-- Create database if it doesn't exist
CREATE DATABASE IF NOT EXISTS virtualbank
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- Use the database
USE virtualbank;

-- Create user for the application (optional, for better security)
-- Uncomment these lines if you want a dedicated database user
-- CREATE USER IF NOT EXISTS 'virtualbank_user'@'localhost' IDENTIFIED BY 'virtualbank_pass';
-- GRANT ALL PRIVILEGES ON virtualbank.* TO 'virtualbank_user'@'localhost';
-- FLUSH PRIVILEGES;

-- Display success message
SELECT 'Database virtualbank created successfully!' AS Status;
