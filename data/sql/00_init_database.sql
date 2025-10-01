-- Database initialization script for School Management System
-- This script creates the database if it doesn't exist and sets up initial configuration

-- Create database if it doesn't exist
CREATE DATABASE IF NOT EXISTS schooldb
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

-- Use the database
USE schooldb;

-- Create a dedicated user for the application (optional but recommended)
-- Note: You may need to run this with admin privileges
-- CREATE USER IF NOT EXISTS 'schoolapp'@'localhost' IDENTIFIED BY 'schoolapp2024';
-- GRANT ALL PRIVILEGES ON schooldb.* TO 'schoolapp'@'localhost';
-- FLUSH PRIVILEGES;

-- Enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- Set timezone
SET time_zone = '+00:00';

-- Show database info
SELECT 'Database schooldb created/selected successfully' as status;
SHOW TABLES;