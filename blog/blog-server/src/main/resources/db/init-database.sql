-- Database Initialization Script
-- This script creates the database and sets up basic configuration

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS blog_system
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- Use the database
USE blog_system;

-- Set timezone
SET time_zone = '+08:00';

-- Enable event scheduler (for scheduled tasks if needed)
SET GLOBAL event_scheduler = ON;

-- Create indexes will be handled by JPA/Hibernate automatically
-- This script is mainly for initial database setup
