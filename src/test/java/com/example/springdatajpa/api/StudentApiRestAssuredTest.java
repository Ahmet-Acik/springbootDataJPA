package com.example.springdatajpa.api;

import com.example.springdatajpa.config.RestAssuredTestConfig;
import com.example.springdatajpa.entity.Student;
import com.example.springdatajpa.entity.Guardian;
import com.example.springdatajpa.repository.StudentRepository;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

/**
 * REST Assured API Tests for Student endpoints.
 * 
 * These tests complement the existing MockMvc tests by providing:
 * - True HTTP request/response testing
 * - JSON contract validation
 * - Real HTTP status code behavior
 * 
 * These tests run alongside MockMvc tests without conflicts.
 * Note: These tests use existing endpoints from StudentController.
 */
@DisplayName("Student API - REST Assured Tests")
class StudentApiRestAssuredTest extends RestAssuredTestConfig {

    @Autowired
    private StudentRepository studentRepository;

    private Student testStudent;

    @BeforeEach
    void setUp() {
        studentRepository.deleteAll();
        
        // Create test data
        Guardian guardian = Guardian.builder()
                .name("Jane Doe")
                .email("jane.doe@guardian.com")
                .mobile("+1234567890")
                .build();

        testStudent = Student.builder()
                .firstName("John")
                .lastName("Doe")
                .emailId("john.doe.restassured@test.com")
                .studentIdNumber("STU" + UUID.randomUUID().toString().substring(0, 8))
                .dateOfBirth(LocalDate.of(1995, 5, 15))
                .admissionDate(LocalDate.of(2023, 9, 1))
                .gpa(BigDecimal.valueOf(3.75))
                .guardian(guardian)
                .isActive(true)
                .build();
    }

    @Nested
    @DisplayName("Basic API Testing")
    class BasicApiTests {

        @Test
        @DisplayName("Should return student by ID")
        void shouldReturnStudentById() {
            // Given - Save test student
            Student saved = studentRepository.save(testStudent);

            // When & Then - Verify basic response
            given()
                .when()
                    .get(getBaseUrl() + "/students/{id}", saved.getStudentId())
                .then()
                    .statusCode(200)
                    .contentType(ContentType.JSON)
                    .body("studentId", notNullValue())
                    .body("firstName", equalTo("John"))
                    .body("lastName", equalTo("Doe"));
        }

        @Test
        @DisplayName("Should return all students")
        void shouldReturnAllStudents() {
            // Given - Save test student
            studentRepository.save(testStudent);

            // When & Then - Verify list response
            given()
                .when()
                    .get(getBaseUrl() + "/students")
                .then()
                    .statusCode(200)
                    .contentType(ContentType.JSON)
                    .body("content", hasSize(greaterThan(0)));
        }
    }

    @Nested
    @DisplayName("HTTP Response Testing")
    class HttpResponseTests {

        @Test
        @DisplayName("Should return correct content-type headers")
        void shouldReturnCorrectHeaders() {
            Student saved = studentRepository.save(testStudent);

            given()
                .when()
                    .get(getBaseUrl() + "/students/{id}", saved.getStudentId())
                .then()
                    .statusCode(200)
                    .header("Content-Type", containsString("application/json"));
        }
    }

        @Nested
    @DisplayName("REST Assured Specific Features")
    class RestAssuredFeatures {

        @Test
        @DisplayName("Should demonstrate REST Assured's fluent API")
        void shouldDemonstrateFunctionFeatures() {
            Student saved = studentRepository.save(testStudent);

            // REST Assured's advantage: fluent, readable API for HTTP testing
            given()
                .log().uri()
                .when()
                    .get(getBaseUrl() + "/students/{id}", saved.getStudentId())
                .then()
                    .log().ifError()
                    .statusCode(200)
                    .time(lessThan(2000L)) // Response time validation
                    .contentType(ContentType.JSON)
                    .extract()
                    .jsonPath()
                    .getString("firstName");
        }
    }

    @Nested
    @DisplayName("HTTP Headers and Content-Type Testing")
    class HttpHeadersTests {

        @Test
        @DisplayName("Should return correct content-type headers") 
        void shouldReturnCorrectHeaders() {
            Student saved = studentRepository.save(testStudent);

            given()
                .when()
                    .get(getBaseUrl() + "/students/{id}", saved.getStudentId())
                .then()
                    .statusCode(200)
                    .header("Content-Type", containsString("application/json"));
        }

        @Test
        @DisplayName("Should handle requests with JSON content-type")
        void shouldAcceptJsonContentType() {
            // Test that server properly handles JSON requests (using GET with Accept header)
            given()
                .accept("application/json")
                .when()
                    .get(getBaseUrl() + "/students")
                .then()
                    .statusCode(200)
                    .contentType(ContentType.JSON);
        }
    }
}