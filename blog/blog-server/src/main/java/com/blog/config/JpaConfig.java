package com.blog.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * JPA Configuration
 * 
 * This configuration class enables:
 * - JPA Repositories for data access
 * - JPA Auditing for automatic timestamp management
 * - Transaction Management for data consistency
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.blog.repository")
@EnableJpaAuditing
@EnableTransactionManagement
public class JpaConfig {
    
    // Additional JPA configuration can be added here if needed
    // For example: custom entity manager factory, transaction manager, etc.
}
