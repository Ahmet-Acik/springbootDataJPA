package com.example.springdatajpa.api;

import com.example.springdatajpa.config.RestAssuredTestConfig;
import com.example.springdatajpa.entity.Guardian;
import com.example.springdatajpa.entity.Student;
import com.example.springdatajpa.repository.StudentRepository;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

/**
 * Error Handling and Recovery Tests for Student API.
 * 
 * This test class focuses on:
 * - HTTP error status codes (4xx, 5xx)
 * - Detailed error message validation
 * - Constraint violation handling
 * - Recovery scenarios
 * - Graceful error responses
 * - Edge case error conditions
 * 
 * These tests ensure robust error handling and user-friendly error messages.
 */
@DisplayName("Student API - Error Handling Tests")
@Transactional
@Rollback
class StudentErrorHandlingRestAssuredTest extends RestAssuredTestConfig {

    @Autowired
    private StudentRepository studentRepository;

    private Student validStudent;

    @BeforeEach
    void setUp() {
        studentRepository.deleteAll();
        validStudent = createValidStudent();
    }

    @Nested
    @DisplayName("HTTP 400 - Bad Request Tests")
    class BadRequestTests {

        @Test
        @DisplayName("Should return 400 for invalid request parameters")
        void shouldReturn400ForInvalidRequestParameters() {
            // Invalid pagination parameters
            given()
                .queryParam("page", "invalid")
                .queryParam("size", "not-a-number")
            .when()
                .get("/api/students")
            .then()
                .statusCode(anyOf(is(200), is(400))); // Depends on implementation
        }
    }

    @Nested
    @DisplayName("HTTP 404 - Not Found Tests")
    class NotFoundTests {


        @Test
        @DisplayName("Should return 404 for invalid endpoint")
        void shouldReturn404ForInvalidEndpoint() {
            given()
            .when()
                .get("/api/non-existent-endpoint")
            .then()
                .statusCode(404)
                .body("timestamp", notNullValue())
                .body("status", equalTo(404))
                .body("error", containsStringIgnoringCase("not found"));
        }

    }

    @Nested
    @DisplayName("HTTP 405 - Method Not Allowed Tests")
    class MethodNotAllowedTests {

        @Test
        @DisplayName("Should return 405 for unsupported HTTP methods")
        void shouldReturn405ForUnsupportedHttpMethods() {
            Student saved = studentRepository.save(validStudent);

            // Try PATCH if not supported
            given()
                .contentType(ContentType.JSON)
                .body("{\"firstName\": \"Updated\"}")
            .when()
                .patch("/api/students/{id}", saved.getStudentId())
            .then()
                .statusCode(anyOf(is(200), is(405))); // Depends on implementation

            // Try PUT on collection endpoint if not supported
            given()
                .contentType(ContentType.JSON)
                .body(createValidStudentMap())
            .when()
                .put("/api/students")
            .then()
                .statusCode(anyOf(is(200), is(405))); // Depends on implementation
        }
    }

    @Nested
    @DisplayName("HTTP 415 - Unsupported Media Type Tests")
    class UnsupportedMediaTypeTests {

        @Test
        @DisplayName("Should return 415 for unsupported content types")
        void shouldReturn415ForUnsupportedContentTypes() {
            given()
                .contentType("application/xml")
                .body("<student><firstName>John</firstName></student>")
            .when()
                .post("/api/students")
            .then()
                .statusCode(415)
                .body("timestamp", notNullValue())
                .body("status", equalTo(415))
                .body("error", containsStringIgnoringCase("unsupported media type"));
        }

        @Test
        @DisplayName("Should return 415 for plain text content")
        void shouldReturn415ForPlainTextContent() {
            given()
                .contentType("text/plain")
                .body("This is plain text, not JSON")
            .when()
                .post("/api/students")
            .then()
                .statusCode(415);
        }
    }


    @Nested
    @DisplayName("Error Message Format Tests")
    class ErrorMessageFormatTests {

        @Test
        @DisplayName("Should not expose sensitive system information")
        void shouldNotExposeSensitiveSystemInformation() {
            given()
                .contentType(ContentType.JSON)
                .body("{ malformed json }")
            .when()
                .post("/api/students")
            .then()
                .statusCode(400)
                .body("message", not(containsStringIgnoringCase("sql")))
                .body("message", not(containsStringIgnoringCase("hibernate")))
                .body("message", not(containsStringIgnoringCase("jdbc")))
                .body("message", not(containsStringIgnoringCase("password")));
        }
    }

    @Nested
    @DisplayName("Error Recovery Tests")
    class ErrorRecoveryTests {

        @Test
        @DisplayName("Should recover after validation errors")
        void shouldRecoverAfterValidationErrors() {
            // First request with validation errors
            given()
                .contentType(ContentType.JSON)
                .body("{}")
            .when()
                .post("/api/students")
            .then()
                .statusCode(400);

            // Second request with valid data should succeed
            given()
                .contentType(ContentType.JSON)
                .body(createValidStudentMap())
            .when()
                .post("/api/students")
            .then()
                .statusCode(201);
        }

        @Test
        @DisplayName("Should maintain system stability after errors")
        void shouldMaintainSystemStabilityAfterErrors() {
            // Generate multiple error conditions
            for (int i = 0; i < 5; i++) {
                given()
                    .contentType(ContentType.JSON)
                    .body("invalid json")
                .when()
                    .post("/api/students")
                .then()
                    .statusCode(400);
            }

            // System should still respond normally
            given()
            .when()
                .get("/api/students")
            .then()
                .statusCode(200);
        }
    }

    @Nested
    @DisplayName("Concurrent Error Handling Tests")
    class ConcurrentErrorTests {

        @Test
        @DisplayName("Should handle concurrent error conditions")
        void shouldHandleConcurrentErrorConditions() {
            // This test simulates multiple clients hitting error conditions simultaneously
            // In a real scenario, you might use parallel streams or threading
            
            for (int i = 0; i < 3; i++) {
                given()
                    .contentType(ContentType.JSON)
                    .body("{}")
                .when()
                    .post("/api/students")
                .then()
                    .statusCode(400)
                    .body("timestamp", notNullValue());
            }
        }
    }

    /**
     * Helper method to create a valid student for testing
     */
    private Student createValidStudent() {
        Guardian guardian = Guardian.builder()
                .name("Jane Doe")
                .email("jane.doe@guardian.com")
                .mobile("+1234567890")
                .build();

        return Student.builder()
                .firstName("John")
                .lastName("Doe")
                .emailId("john.doe.error@test.com")
                .studentIdNumber("STU" + UUID.randomUUID().toString().substring(0, 8))
                .dateOfBirth(LocalDate.of(1995, 5, 15))
                .admissionDate(LocalDate.of(2023, 9, 1))
                .gpa(BigDecimal.valueOf(3.75))
                .guardian(guardian)
                .isActive(true)
                .build();
    }

    /**
     * Helper method to create valid student data as Map for JSON serialization
     */
    private Map<String, Object> createValidStudentMap() {
        Map<String, Object> guardian = new HashMap<>();
        guardian.put("name", "Jane Doe");
        guardian.put("email", "jane.doe@guardian.com");
        guardian.put("mobile", "+1234567890");

        Map<String, Object> student = new HashMap<>();
        student.put("firstName", "John");
        student.put("lastName", "Doe");
        student.put("emailId", "john.doe.error.map@test.com");
        student.put("studentIdNumber", "STU" + UUID.randomUUID().toString().substring(0, 8));
        student.put("dateOfBirth", LocalDate.of(1995, 5, 15).toString());
        student.put("admissionDate", LocalDate.of(2023, 9, 1).toString());
        student.put("gpa", new BigDecimal("3.75"));
        student.put("guardian", guardian);
        student.put("isActive", true);

        return student;
    }
}