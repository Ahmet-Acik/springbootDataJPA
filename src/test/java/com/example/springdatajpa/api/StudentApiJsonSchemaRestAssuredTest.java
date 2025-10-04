package com.example.springdatajpa.api;

import com.example.springdatajpa.config.RestAssuredTestConfig;
import com.example.springdatajpa.entity.Student;
import com.example.springdatajpa.entity.Guardian;
import com.example.springdatajpa.repository.StudentRepository;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.*;
import static io.restassured.module.jsv.JsonSchemaValidator.*;
import static org.assertj.core.api.Assertions.*;

/**
 * JSON Schema Validation and Custom AssertJ Tests
 * 
 * This class demonstrates:
 * - JSON Schema validation with REST Assured
 * - Custom AssertJ assertions for domain objects
 * - Fluent assertion chains
 * - Complex validation scenarios
 * - Custom error messages and descriptions
 */
@DisplayName("JSON Schema & Custom AssertJ Tests")
class StudentApiJsonSchemaRestAssuredTest extends RestAssuredTestConfig {

    @Autowired
    private StudentRepository studentRepository;

    private Student testStudent;

    @BeforeEach
    void setUp() {
        studentRepository.deleteAll();
        
        Guardian guardian = Guardian.builder()
                .name("Schema Guardian")
                .email("schema.guardian@test.com")
                .mobile("+9876543210")
                .build();

        testStudent = Student.builder()
                .firstName("Schema")
                .lastName("Tester")
                .emailId("schema.tester@test.com")
                .studentIdNumber("STU" + UUID.randomUUID().toString().substring(0, 8))
                .dateOfBirth(LocalDate.of(1999, 1, 1))
                .admissionDate(LocalDate.of(2024, 1, 15))
                .gpa(BigDecimal.valueOf(3.95))
                .guardian(guardian)
                .isActive(true)
                .build();
        testStudent = studentRepository.save(testStudent);
    }

    @Nested
    @DisplayName("JSON Schema Validation Tests")
    class JsonSchemaValidationTests {

        @Test
        @DisplayName("Should validate student response against JSON schema")
        void shouldValidateStudentSchema() {
            // Define JSON schema for student response
            String studentSchema = """
                {
                    "$schema": "http://json-schema.org/draft-07/schema#",
                    "type": "object",
                    "required": ["studentId", "firstName", "lastName", "emailId", "gpa", "guardian"],
                    "properties": {
                        "studentId": {"type": "integer"},
                        "firstName": {"type": "string", "minLength": 1},
                        "lastName": {"type": "string", "minLength": 1},
                        "emailId": {"type": "string", "pattern": "^[\\\\w.-]+@[\\\\w.-]+\\\\.[a-zA-Z]{2,}$"},
                        "studentIdNumber": {"type": "string"},
                        "dateOfBirth": {"type": "string", "format": "date"},
                        "admissionDate": {"type": "string", "format": "date"},
                        "gpa": {"type": "number", "minimum": 0, "maximum": 4},
                        "guardian": {
                            "type": "object",
                            "required": ["name", "email", "mobile"],
                            "properties": {
                                "name": {"type": "string", "minLength": 1},
                                "email": {"type": "string", "pattern": "^[\\\\w.-]+@[\\\\w.-]+\\\\.[a-zA-Z]{2,}$"},
                                "mobile": {"type": "string", "pattern": "^\\\\+?[0-9]{10,15}$"}
                            }
                        },
                        "isActive": {"type": "boolean"},
                        "createdDate": {"type": "string"},
                        "lastModifiedDate": {"type": "string"}
                    }
                }
                """;

            // When & Then - Validate against schema
            given()
                .when()
                    .get(getBaseUrl() + "/students/{id}", testStudent.getStudentId())
                .then()
                    .statusCode(200)
                    .contentType(ContentType.JSON)
                    .body(matchesJsonSchema(studentSchema));
        }

        @Test
        @DisplayName("Should validate student list schema")
        void shouldValidateStudentListSchema() {
            // Create additional students
            createAdditionalStudents();

            // Define schema for paginated student list
            String studentListSchema = """
                {
                    "$schema": "http://json-schema.org/draft-07/schema#",
                    "type": "object",
                    "required": ["content", "pageable", "totalElements", "totalPages"],
                    "properties": {
                        "content": {
                            "type": "array",
                            "items": {
                                "type": "object",
                                "required": ["studentId", "firstName", "lastName", "emailId"],
                                "properties": {
                                    "studentId": {"type": "integer"},
                                    "firstName": {"type": "string"},
                                    "lastName": {"type": "string"},
                                    "emailId": {"type": "string"},
                                    "gpa": {"type": "number"}
                                }
                            },
                            "minItems": 1
                        },
                        "pageable": {"type": "object"},
                        "totalElements": {"type": "integer"},
                        "totalPages": {"type": "integer"},
                        "size": {"type": "integer"},
                        "number": {"type": "integer"},
                        "first": {"type": "boolean"},
                        "last": {"type": "boolean"},
                        "empty": {"type": "boolean"}
                    }
                }
                """;

            // When & Then - Validate paginated list against schema
            given()
                .when()
                    .get(getBaseUrl() + "/students")
                .then()
                    .statusCode(200)
                    .contentType(ContentType.JSON)
                    .body(matchesJsonSchema(studentListSchema));
        }
    }

    @Nested
    @DisplayName("Custom AssertJ Assertions")
    class CustomAssertJTests {

        @Test
        @DisplayName("Should validate student with custom assertions")
        void shouldValidateWithCustomAssertions() {
            // When - Get student response
            Response response = given()
                    .when()
                        .get(getBaseUrl() + "/students/{id}", testStudent.getStudentId())
                    .then()
                        .statusCode(200)
                        .extract().response();

            // Extract student data
            Map<String, Object> studentData = response.jsonPath().getMap("");

            // Then - Use custom assertions
            assertThatStudent(studentData)
                    .hasValidId()
                    .hasName("Schema", "Tester")
                    .hasValidEmail()
                    .hasGpaInRange(3.0, 4.0)
                    .isActive()
                    .hasValidGuardian();
        }

        @Test
        @DisplayName("Should validate multiple students with custom assertions")
        void shouldValidateMultipleStudentsWithCustomAssertions() {
            // Given - Create students with different characteristics
            createStudentsWithDifferentCharacteristics();

            // When - Get all students
            Response response = given()
                    .when()
                        .get(getBaseUrl() + "/students")
                    .then()
                        .statusCode(200)
                        .extract().response();

            List<Map<String, Object>> students = response.jsonPath().getList("content");

            // Then - Validate each student with custom assertions
            assertThat(students)
                    .as("Should have multiple students")
                    .hasSizeGreaterThan(1)
                    .allSatisfy(student -> {
                        assertThatStudent(student)
                                .hasValidId()
                                .hasValidEmail()
                                .hasGpaInRange(0.0, 4.0);
                    });

            // Validate specific conditions
            assertThat(students)
                    .as("Should have at least one high-performing student")
                    .anyMatch(student -> {
                        BigDecimal gpa = new BigDecimal(student.get("gpa").toString());
                        return gpa.compareTo(BigDecimal.valueOf(3.5)) > 0;
                    });
        }

        @Test
        @DisplayName("Should validate student data completeness")
        void shouldValidateDataCompleteness() {
            // When - Get student
            Response response = given()
                    .when()
                        .get(getBaseUrl() + "/students/{id}", testStudent.getStudentId())
                    .then()
                        .statusCode(200)
                        .extract().response();

            Map<String, Object> student = response.jsonPath().getMap("");

            // Then - Validate completeness
            assertThatStudent(student)
                    .hasAllRequiredFields()
                    .hasValidTimestamps()
                    .hasNoNullValues();
        }
    }

    @Nested
    @DisplayName("Advanced JSON Path and AssertJ Combinations")
    class AdvancedValidationTests {

        @Test
        @DisplayName("Should validate complex JSON structures with fluent assertions")
        void shouldValidateComplexJsonStructures() {
            // Given - Student with complete data
            Response response = given()
                    .when()
                        .get(getBaseUrl() + "/students/{id}", testStudent.getStudentId())
                    .then()
                        .statusCode(200)
                        .extract().response();

            // When - Extract various data points
            String firstName = response.jsonPath().getString("firstName");
            String lastName = response.jsonPath().getString("lastName");
            String fullName = firstName + " " + lastName;
            BigDecimal gpa = new BigDecimal(response.jsonPath().getString("gpa"));
            String guardianEmail = response.jsonPath().getString("guardian.email");
            Boolean isActive = response.jsonPath().getBoolean("isActive");

            // Then - Chain multiple AssertJ assertions
            assertThat(fullName)
                    .as("Full name should be properly formatted")
                    .isEqualTo("Schema Tester")
                    .contains("Schema")
                    .endsWith("Tester")
                    .doesNotContain("  ") // No double spaces
                    .matches("^[A-Za-z]+ [A-Za-z]+$");

            assertThat(gpa)
                    .as("GPA should be excellent")
                    .isGreaterThan(BigDecimal.valueOf(3.9))
                    .isLessThanOrEqualTo(BigDecimal.valueOf(4.0))
                    .isEqualByComparingTo(BigDecimal.valueOf(3.95));

            assertThat(guardianEmail)
                    .as("Guardian email should follow domain pattern")
                    .contains("@test.com")
                    .startsWith("schema.guardian")
                    .matches("^[\\w.-]+@test\\.com$");

            assertThat(isActive)
                    .as("Student should be active")
                    .isTrue();
        }

        @Test
        @DisplayName("Should validate valid student creation detailed")
        void shouldValidateStudentCreationDetailed() {
            // Given - Student data
            String studentJson = """
                {
                    "firstName": "Detailed",
                    "lastName": "TestStudent", 
                    "emailId": "detailed.test@example.com",
                    "gpa": 3.8
                }
                """;

            // When - Create student
            Response response = given()
                    .contentType(ContentType.JSON)
                    .body(studentJson)
                    .when()
                        .post(getBaseUrl() + "/students")
                    .then()
                        .statusCode(201)
                        .extract().response();

            // Then - Detailed validation with AssertJ
            Map<String, Object> student = response.jsonPath().getMap("");
            String firstName = (String) student.get("firstName");
            String lastName = (String) student.get("lastName");
            Double gpa = ((Number) student.get("gpa")).doubleValue();

            assertThat(student)
                    .as("Student response structure")
                    .hasSizeGreaterThan(3)
                    .containsKeys("studentId", "firstName", "lastName", "emailId");

            assertThat(firstName)
                    .as("First name should be correctly set")
                    .isEqualTo("Detailed");

            assertThat(lastName)
                    .as("Last name should be correctly set")
                    .isEqualTo("TestStudent");

            assertThat(gpa)
                    .as("GPA should be correctly set")
                    .isEqualTo(3.8);
        }
    }

    // Helper methods
    private void createAdditionalStudents() {
        Guardian guardian1 = Guardian.builder()
                .name("Additional Guardian 1")
                .email("additional1@test.com")
                .mobile("+1111111111")
                .build();

        Student student1 = Student.builder()
                .firstName("Additional")
                .lastName("Student1")
                .emailId("additional1@test.com")
                .studentIdNumber("STUADD001")
                .dateOfBirth(LocalDate.of(1998, 5, 15))
                .admissionDate(LocalDate.of(2023, 9, 1))
                .gpa(BigDecimal.valueOf(3.4))
                .guardian(guardian1)
                .isActive(true)
                .build();

        studentRepository.save(student1);
    }

    private void createStudentsWithDifferentCharacteristics() {
        // High-performing student
        Guardian highPerformerGuardian = Guardian.builder()
                .name("High Performer Guardian")
                .email("highperformer@test.com")
                .mobile("+5555555555")
                .build();

        Student highPerformer = Student.builder()
                .firstName("High")
                .lastName("Performer")
                .emailId("high.performer@test.com")
                .studentIdNumber("STUHIGH")
                .dateOfBirth(LocalDate.of(1997, 8, 20))
                .admissionDate(LocalDate.of(2023, 9, 1))
                .gpa(BigDecimal.valueOf(3.8))
                .guardian(highPerformerGuardian)
                .isActive(true)
                .build();

        // Average student
        Guardian averageGuardian = Guardian.builder()
                .name("Average Guardian")
                .email("average@test.com")
                .mobile("+3333333333")
                .build();

        Student averageStudent = Student.builder()
                .firstName("Average")
                .lastName("Student")
                .emailId("average.student@test.com")
                .studentIdNumber("STUAVG")
                .dateOfBirth(LocalDate.of(1999, 3, 10))
                .admissionDate(LocalDate.of(2023, 9, 1))
                .gpa(BigDecimal.valueOf(2.9))
                .guardian(averageGuardian)
                .isActive(true)
                .build();

        studentRepository.saveAll(List.of(highPerformer, averageStudent));
    }

    // Custom AssertJ assertion methods
    private StudentAssert assertThatStudent(Map<String, Object> student) {
        return new StudentAssert(student);
    }

    // Custom AssertJ assertion class
    private static class StudentAssert {
        private final Map<String, Object> student;

        public StudentAssert(Map<String, Object> student) {
            this.student = student;
            assertThat(student).isNotNull();
        }

        public StudentAssert hasValidId() {
            assertThat(student.get("studentId"))
                    .as("Student ID should be valid")
                    .isNotNull()
                    .isInstanceOf(Integer.class);
            return this;
        }

        public StudentAssert hasName(String firstName, String lastName) {
            assertThat(student.get("firstName"))
                    .as("First name should match")
                    .isEqualTo(firstName);
            assertThat(student.get("lastName"))
                    .as("Last name should match")
                    .isEqualTo(lastName);
            return this;
        }

        public StudentAssert hasValidEmail() {
            String email = (String) student.get("emailId");
            assertThat(email)
                    .as("Email should be valid format")
                    .isNotNull()
                    .contains("@")
                    .matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
            return this;
        }

        public StudentAssert hasGpaInRange(double min, double max) {
            BigDecimal gpa = new BigDecimal(student.get("gpa").toString());
            assertThat(gpa)
                    .as("GPA should be in range [" + min + ", " + max + "]")
                    .isGreaterThanOrEqualTo(BigDecimal.valueOf(min))
                    .isLessThanOrEqualTo(BigDecimal.valueOf(max));
            return this;
        }

        public StudentAssert isActive() {
            assertThat(student.get("isActive"))
                    .as("Student should be active")
                    .isEqualTo(true);
            return this;
        }

        public StudentAssert hasValidGuardian() {
            @SuppressWarnings("unchecked")
            Map<String, Object> guardian = (Map<String, Object>) student.get("guardian");
            assertThat(guardian)
                    .as("Guardian should be present and valid")
                    .isNotNull()
                    .containsKeys("name", "email", "mobile");
            
            assertThat(guardian.get("email"))
                    .as("Guardian email should be valid")
                    .asString()
                    .contains("@");
            return this;
        }

        public StudentAssert hasAllRequiredFields() {
            assertThat(student)
                    .as("Student should have all required fields")
                    .containsKeys("studentId", "firstName", "lastName", "emailId", "gpa", "guardian", "isActive");
            return this;
        }

        public StudentAssert hasValidTimestamps() {
            assertThat(student.get("createdDate"))
                    .as("Created date should be present")
                    .isNotNull();
            assertThat(student.get("lastModifiedDate"))
                    .as("Last modified date should be present")
                    .isNotNull();
            return this;
        }

        public StudentAssert hasNoNullValues() {
            assertThat(student.values())
                    .as("No student field should be null")
                    .doesNotContainNull();
            return this;
        }
    }
}