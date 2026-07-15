package com.blog.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class to verify database configuration
 */
@SpringBootTest
@ActiveProfiles("dev")
class DatabaseConfigurationTest extends com.blog.BaseTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void testDataSourceConfiguration() {
        assertNotNull(dataSource, "DataSource should be configured");
    }

    @Test
    void testDatabaseConnection() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            assertNotNull(connection, "Should be able to get a database connection");
            assertFalse(connection.isClosed(), "Connection should be open");
            assertTrue(connection.isValid(5), "Connection should be valid");
        }
    }

    @Test
    void testHikariCPConfiguration() {
        // Verify that HikariCP is being used
        String dataSourceClassName = dataSource.getClass().getName();
        assertTrue(dataSourceClassName.contains("Hikari"), 
            "Should be using HikariCP as connection pool");
    }
}
