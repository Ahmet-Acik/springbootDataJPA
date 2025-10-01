package com.example.springdatajpa.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;

/**
 * Base test configuration for integration tests
 * Provides common setup for all integration tests including test database configuration
 */
@TestConfiguration
@ActiveProfiles("test")
@EnableTransactionManagement
public class TestConfig {

    /**
     * Creates a test-specific H2 in-memory database for integration tests
     * This ensures test isolation and fast test execution
     */
    @Bean
    @Primary
    public DataSource testDataSource() {
        return DataSourceBuilder
                .create()
                .driverClassName("org.h2.Driver")
                .url("jdbc:h2:mem:integrationtest;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
                .username("sa")
                .password("")
                .build();
    }
}