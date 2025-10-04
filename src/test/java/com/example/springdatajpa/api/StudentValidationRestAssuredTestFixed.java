package com.example.springdatajpa.api;

import com.example.springdatajpa.entity.Guardian;
import com.example.springdatajpa.entity.Student;
import com.example.springdatajpa.repository.StudentRepository;

import io.restassured.RestAssured;
import io.restassured.config.JsonConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.path.json.config.JsonPathConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

/**
 * Fixed Data Validation Tests for Student API.
 * 
 * This test class focuses on:
 * - Input validation scenarios
 * - Edge cases and boundary conditions
 * - Data format validation
 * - Field constraint testing
 * - Error response validation
 * 
 * Restructured to avoid @Nested class issues that bypass validation context.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Student API - Data Validation Tests (Fixed)")
@Transactional
class StudentValidationRestAssuredTestFixed {

    @Autowired
    private StudentRepository studentRepository;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        studentRepository.deleteAll();
        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        
        // Configure JSON path to use BigDecimal for numbers (better precision)
        RestAssured.config = RestAssuredConfig.config()
                .jsonConfig(JsonConfig.jsonConfig()
                        .numberReturnType(JsonPathConfig.NumberReturnType.BIG_DECIMAL));
    }
   
    @Test
    @DisplayName("Should accept valid email formats")
    void shouldAcceptValidEmailFormats() {
        String[] validEmails = {
            "test@example.com",
            "user.name@domain.co.uk",
            "user+tag@example.org",
            "123@example.com"
        };

        for (String validEmail : validEmails) {
            studentRepository.deleteAll(); // Clean between tests
            
            Map<String, Object> studentData = createValidStudentData();
            studentData.put("emailId", validEmail);

            given()
                .contentType(ContentType.JSON)
                .body(studentData)
            .when()
                .post("/api/students")
            .then()
                .statusCode(201)
                .body("emailId", equalTo(validEmail));
        }
    }


    @Test
    @DisplayName("Should validate date constraints")
    void shouldValidateDateConstraints() {
        Map<String, Object> studentData = createValidStudentData();
        
        // Test future admission date (should be rejected)
        studentData.put("admissionDate", LocalDate.now().plusDays(1).toString());
        
        given()
            .contentType(ContentType.JSON) 
            .body(studentData)
        .when()
            .post("/api/students")
        .then()
            .statusCode(400);
    }

    /**
     * Helper method to create valid Student object for testing
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
                .emailId("john.doe.validation@test.com")
                .studentIdNumber("STU" + UUID.randomUUID().toString().substring(0, 8))
                .dateOfBirth(LocalDate.of(1995, 5, 15))
                .admissionDate(LocalDate.of(2023, 9, 1))
                .gpa(new BigDecimal("3.75"))
                .guardian(guardian)
                .isActive(true)
                .build();
    }

    /**
     * Helper method to create valid student data as Map for JSON testing
     */
    private Map<String, Object> createValidStudentData() {
        Map<String, Object> guardian = new HashMap<>();
        guardian.put("name", "Jane Doe");
        guardian.put("email", "jane.doe@guardian.com");
        guardian.put("mobile", "+1234567890");

        Map<String, Object> student = new HashMap<>();
        student.put("firstName", "John");
        student.put("lastName", "Doe");
        student.put("emailId", "john.doe.validation@test.com");
        student.put("studentIdNumber", "STU" + UUID.randomUUID().toString().substring(0, 8));
        student.put("dateOfBirth", LocalDate.of(1995, 5, 15).toString());
        student.put("admissionDate", LocalDate.of(2023, 9, 1).toString());
        student.put("gpa", new BigDecimal("3.75"));
        student.put("guardian", guardian);
        student.put("isActive", true);

        return student;
    }
}