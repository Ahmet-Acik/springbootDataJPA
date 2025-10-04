package com.example.springdatajpa.config;

import io.restassured.RestAssured;
import io.restassured.config.JsonConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.path.json.config.JsonPathConfig;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

/**
 * Base configuration class for REST Assured tests.
 * This class provides common setup for REST Assured integration tests.
 * 
 * Benefits of REST Assured over MockMvc:
 * - True HTTP calls (more realistic testing)
 * - Better JSON path support
 * - More fluent API for complex scenarios
 * - Can test against running applications
 * 
 * Use REST Assured when:
 * - Testing complex JSON responses
 * - Need true end-to-end HTTP testing
 * - Testing API contracts
 * - Performance testing with real HTTP overhead
 * 
 * Use MockMvc when:
 * - Fast integration tests
 * - Spring-specific testing features needed
 * - Testing Spring Security integration
 * - Lighter weight testing
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class RestAssuredTestConfig {

    @LocalServerPort
    protected int port;

    @BeforeEach
    void setUpRestAssured() {
        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        
        // Configure JSON path to use BigDecimal for numbers (better precision)
        RestAssured.config = RestAssuredConfig.config()
                .jsonConfig(JsonConfig.jsonConfig()
                        .numberReturnType(JsonPathConfig.NumberReturnType.BIG_DECIMAL));
    }

    /**
     * Helper method to get the base URL for API endpoints
     */
    protected String getBaseUrl() {
        return "http://localhost:" + port + "/api";
    }
}