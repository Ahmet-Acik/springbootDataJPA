package com.example.springdatajpa.debug;

import com.example.springdatajpa.entity.Guardian;
import com.example.springdatajpa.entity.Student;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Debug test to diagnose validation differences
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ValidationDiagnosticTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Test
    @DisplayName("Diagnostic - Show what gets sent")
    void diagnosticTest() throws Exception {
        // Test 1: Student object
        Student invalidStudent = Student.builder()
                .firstName("John")
                .lastName("Doe")
                .emailId("invalid-email") // Invalid email
                .gpa(BigDecimal.valueOf(3.5))
                .build();

        System.out.println("=== STUDENT OBJECT JSON ===");
        String studentJson = objectMapper.writeValueAsString(invalidStudent);
        System.out.println(studentJson);

        // Test 2: Map object
        Map<String, Object> studentMap = new HashMap<>();
        studentMap.put("firstName", "John");
        studentMap.put("lastName", "Doe");
        studentMap.put("emailId", "invalid-email");
        studentMap.put("gpa", BigDecimal.valueOf(3.5));

        System.out.println("=== MAP OBJECT JSON ===");
        String mapJson = objectMapper.writeValueAsString(studentMap);
        System.out.println(mapJson);

        // Test both with REST Assured
        System.out.println("=== TESTING STUDENT OBJECT ===");
        try {
            given()
                .contentType(ContentType.JSON)
                .body(invalidStudent)
            .when()
                .post("/api/students")
            .then()
                .statusCode(400); // Should fail validation
            System.out.println("Student object: VALIDATION WORKED");
        } catch (AssertionError e) {
            System.out.println("Student object: VALIDATION FAILED - " + e.getMessage());
        }

        System.out.println("=== TESTING MAP OBJECT ===");
        try {
            given()
                .contentType(ContentType.JSON)
                .body(studentMap)
            .when()
                .post("/api/students")
            .then()
                .statusCode(400); // Should fail validation
            System.out.println("Map object: VALIDATION WORKED");
        } catch (AssertionError e) {
            System.out.println("Map object: VALIDATION FAILED - " + e.getMessage());
        }
    }
}