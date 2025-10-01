package com.example.springdatajpa.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;

/**
 * Base test configuration for integration tests
 * This configuration is used across different test types to ensure consistency
 */
@TestConfiguration
@EnableTransactionManagement
public class BaseIntegrationTestConfig {

    /**
     * Common test utilities and helpers can be defined here
     */
    
    /**
     * Abstract base class for repository integration tests
     */
    @ActiveProfiles("test")
    public abstract static class RepositoryIntegrationTest {
        // Common repository test setup
    }
    
    /**
     * Abstract base class for service integration tests
     */
    @ActiveProfiles("test") 
    public abstract static class ServiceIntegrationTest {
        // Common service test setup
    }
    
    /**
     * Abstract base class for web layer integration tests
     */
    @ActiveProfiles("test")
    @AutoConfigureWebMvc
    public abstract static class WebIntegrationTest {
        // Common web test setup
    }
    
    /**
     * Abstract base class for full end-to-end integration tests
     */
    @ActiveProfiles("test")
    public abstract static class EndToEndIntegrationTest {
        // Common e2e test setup
    }
}