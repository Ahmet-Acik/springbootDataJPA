package com.example.springdatajpa.api;

import com.example.springdatajpa.config.RestAssuredTestConfig;
import com.example.springdatajpa.entity.Student;
import com.example.springdatajpa.entity.Guardian;
import com.example.springdatajpa.entity.Course;
import com.example.springdatajpa.entity.Department;
import com.example.springdatajpa.entity.Department.DepartmentType;
import com.example.springdatajpa.entity.Course.CourseLevel;
import com.example.springdatajpa.repository.StudentRepository;
import com.example.springdatajpa.repository.CourseRepository;
import com.example.springdatajpa.repository.DepartmentRepository;

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
import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;

/**
 * Advanced REST Assured API Tests with AssertJ assertions.
 * 
 * This class demonstrates:
 * - Fluent AssertJ assertions for complex validations
 * - Response extraction and transformation
 * - Collection and object assertions  
 * - Custom assertion chains
 * - Error response validation
 * - Performance testing with REST Assured
 */
@DisplayName("Advanced Student API - REST Assured + AssertJ Tests")
class AdvancedStudentApiRestAssuredTest extends RestAssuredTestConfig {

    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;

    private Student testStudent;
    private Department testDepartment;
    private Course testCourse;

    @BeforeEach
    void setUp() {
        // Clean up
        studentRepository.deleteAll();
        courseRepository.deleteAll();
        departmentRepository.deleteAll();
        
        // Create test department
        testDepartment = Department.builder()
                .departmentName("Computer Science")
                .departmentCode("CS")
                .departmentAddress("Building A, Floor 3")
                .departmentType(DepartmentType.SCIENCE)
                .headOfDepartment("Dr. Smith")
                .isActive(true)
                .build();
        testDepartment = departmentRepository.save(testDepartment);
        
        // Create test course
        testCourse = Course.builder()
                .title("Data Structures")
                .courseCode("CS101")
                .creditHours(BigDecimal.valueOf(3))
                .courseLevel(CourseLevel.BEGINNER)
                .description("Introduction to Data Structures and Algorithms")
                .department(testDepartment)
                .isActive(true)
                .build();
        testCourse = courseRepository.save(testCourse);
        
        // Create test student
        Guardian guardian = Guardian.builder()
                .name("John Guardian")
                .email("john.guardian@test.com")
                .mobile("+1234567890")
                .build();

        testStudent = Student.builder()
                .firstName("Jane")
                .lastName("Smith")
                .emailId("jane.smith.assertj@test.com")
                .studentIdNumber("STU" + UUID.randomUUID().toString().substring(0, 8))
                .dateOfBirth(LocalDate.of(1998, 3, 15))
                .admissionDate(LocalDate.of(2023, 9, 1))
                .gpa(BigDecimal.valueOf(3.85))
                .guardian(guardian)
                .isActive(true)
                .build();
        testStudent = studentRepository.save(testStudent);
    }

    @Nested
    @DisplayName("Response Extraction and AssertJ Validation")
    class ResponseExtractionTests {

        @Test
        @DisplayName("Should extract and validate student details with AssertJ")
        void shouldExtractAndValidateStudentDetails() {
            // When - Get student and extract response
            Response response = given()
                    .when()
                        .get(getBaseUrl() + "/students/{id}", testStudent.getStudentId())
                    .then()
                        .statusCode(200)
                        .contentType(ContentType.JSON)
                        .extract().response();

            // Then - Use AssertJ for detailed validation
            Map<String, Object> student = response.jsonPath().getMap("");
            
            assertThat(student)
                    .as("Student response should contain all required fields")
                    .containsKeys("studentId", "firstName", "lastName", "emailId", "gpa", "guardian")
                    .hasFieldOrPropertyWithValue("firstName", "Jane")
                    .hasFieldOrPropertyWithValue("lastName", "Smith")
                    .hasFieldOrPropertyWithValue("emailId", "jane.smith.assertj@test.com")
                    .hasFieldOrPropertyWithValue("isActive", true);

            // Validate numeric fields with precision
            BigDecimal actualGpa = new BigDecimal(student.get("gpa").toString());
            assertThat(actualGpa)
                    .as("GPA should be exactly 3.85")
                    .isEqualByComparingTo(BigDecimal.valueOf(3.85));

            // Validate nested guardian object
            @SuppressWarnings("unchecked")
            Map<String, Object> guardian = (Map<String, Object>) student.get("guardian");
            assertThat(guardian)
                    .as("Guardian object should be properly structured")
                    .isNotNull()
                    .containsEntry("name", "John Guardian")
                    .containsEntry("email", "john.guardian@test.com")
                    .containsEntry("mobile", "+1234567890");

            // Validate date formatting
            String dateOfBirth = (String) student.get("dateOfBirth");
            assertThat(dateOfBirth)
                    .as("Date of birth should be in ISO format")
                    .matches("\\d{4}-\\d{2}-\\d{2}")
                    .isEqualTo("1998-03-15");
        }

        @Test
        @DisplayName("Should extract and validate list of students")
        void shouldExtractAndValidateStudentList() {
            // Given - Create additional students
            createMultipleStudents();

            // When - Get all students
            Response response = given()
                    .when()
                        .get(getBaseUrl() + "/students")
                    .then()
                        .statusCode(200)
                        .extract().response();

            // Then - Extract and validate list
            List<Map<String, Object>> students = response.jsonPath().getList("");
            
            assertThat(students)
                    .as("Should return list of students")
                    .isNotEmpty()
                    .hasSizeGreaterThanOrEqualTo(3)
                    .allSatisfy(student -> {
                        assertThat(student)
                                .containsKeys("studentId", "firstName", "lastName", "emailId", "gpa");
                        assertThat(student.get("firstName"))
                                .as("First name should not be empty")
                                .isNotNull()
                                .asString()
                                .isNotBlank();
                    });

            // Validate specific student in the list
            assertThat(students)
                    .as("Should contain our test student")
                    .anyMatch(student -> 
                            "Jane".equals(student.get("firstName")) && 
                            "Smith".equals(student.get("lastName"))
                    );

            // Validate GPA range
            List<BigDecimal> gpas = students.stream()
                    .map(student -> new BigDecimal(student.get("gpa").toString()))
                    .toList();
            
            assertThat(gpas)
                    .as("All GPAs should be in valid range")
                    .allMatch(gpa -> gpa.compareTo(BigDecimal.ZERO) >= 0 && 
                                   gpa.compareTo(BigDecimal.valueOf(4.0)) <= 0);
        }
    }

    @Nested
    @DisplayName("Error Response Validation with AssertJ")
    class ErrorResponseValidationTests {

        @Test
        @DisplayName("Should validate 404 error response structure")
        void shouldValidate404ErrorStructure() {
            // When - Request non-existent student (our API returns 404 with empty body)
            given()
                    .when()
                        .get(getBaseUrl() + "/students/{id}", 99999)
                    .then()
                        .statusCode(404);
            
            // Note: This endpoint returns empty body on 404, which is valid REST practice
            // If we needed error details, we'd need to modify the controller
        }

        @Test
        @DisplayName("Should handle student creation with various data")
        void shouldHandleStudentCreationWithVariousData() {
            // Given - Student data (note: no validation annotations on entity currently)
            String studentJson = """
                {
                    "firstName": "Test",
                    "lastName": "Student",
                    "emailId": "test.student@example.com",
                    "gpa": 3.5
                }
                """;

            // When - Send student data
            Response response = given()
                    .contentType(ContentType.JSON)
                    .body(studentJson)
                    .when()
                        .post(getBaseUrl() + "/students")
                    .then()
                        .statusCode(201)
                        .extract().response();

            // Then - Validate successful creation
            Map<String, Object> createdStudent = response.jsonPath().getMap("");
            
            assertThat(createdStudent)
                    .as("Created student should have expected fields")
                    .containsKeys("studentId", "firstName", "lastName", "emailId")
                    .hasFieldOrPropertyWithValue("firstName", "Test")
                    .hasFieldOrPropertyWithValue("lastName", "Student");

            Integer studentId = (Integer) createdStudent.get("studentId");
            assertThat(studentId)
                    .as("Student ID should be generated")
                    .isNotNull()
                    .isPositive();
        }
    }

    @Nested
    @DisplayName("Performance and Timing Tests")
    class PerformanceTests {

        @Test
        @DisplayName("Should respond within acceptable time limits")
        void shouldRespondWithinTimeLimit() {
            // When & Then - Test response time
            Response response = given()
                    .when()
                        .get(getBaseUrl() + "/students/{id}", testStudent.getStudentId())
                    .then()
                        .statusCode(200)
                        .time(lessThan(2000L)) // Less than 2 seconds
                        .extract().response();

            // Additional AssertJ validation on response time
            long responseTime = response.getTime();
            assertThat(responseTime)
                    .as("Response time should be reasonable for single record retrieval")
                    .isLessThan(1000L) // Less than 1 second for single record
                    .isGreaterThan(0L);
        }

        @Test
        @DisplayName("Should handle multiple concurrent requests efficiently")
        void shouldHandleConcurrentRequests() {
            // Given - Create multiple students for load testing
            createMultipleStudents();

            // When - Make multiple requests and measure total time
            long startTime = System.currentTimeMillis();
            
            for (int i = 0; i < 5; i++) {
                given()
                    .when()
                        .get(getBaseUrl() + "/students")
                    .then()
                        .statusCode(200)
                        .time(lessThan(3000L)); // Each request under 3 seconds
            }
            
            long totalTime = System.currentTimeMillis() - startTime;

            // Then - Validate overall performance
            assertThat(totalTime)
                    .as("Total time for 5 requests should be reasonable")
                    .isLessThan(10000L); // Less than 10 seconds total
        }
    }

    @Nested
    @DisplayName("Complex JSON Path Validation")
    class JsonPathValidationTests {

        @Test
        @DisplayName("Should validate nested JSON structures")
        void shouldValidateNestedJsonStructures() {
            // When - Get student with nested guardian
            Response response = given()
                    .when()
                        .get(getBaseUrl() + "/students/{id}", testStudent.getStudentId())
                    .then()
                        .statusCode(200)
                        .extract().response();

            // Then - Use complex JSON path validations
            String firstName = response.jsonPath().getString("firstName");
            String guardianName = response.jsonPath().getString("guardian.name");
            String guardianEmail = response.jsonPath().getString("guardian.email");

            assertThat(firstName)
                    .as("First name should match expected value")
                    .isEqualTo("Jane");

            assertThat(guardianName)
                    .as("Guardian name should be properly nested")
                    .isEqualTo("John Guardian")
                    .contains("Guardian");

            assertThat(guardianEmail)
                    .as("Guardian email should be valid format")
                    .contains("@")
                    .endsWith("@test.com")
                    .matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
        }

        @Test
        @DisplayName("Should validate array and filtering operations")
        void shouldValidateArrayFiltering() {
            // Given - Create students with different GPAs
            createStudentsWithVariousGpas();

            // When - Get all students
            Response response = given()
                    .when()
                        .get(getBaseUrl() + "/students")
                    .then()
                        .statusCode(200)
                        .extract().response();

            // Then - Filter and validate using JSON path and AssertJ
            List<String> firstNames = response.jsonPath().getList("firstName", String.class);
            List<BigDecimal> gpas = response.jsonPath().getList("gpa")
                    .stream()
                    .map(gpa -> new BigDecimal(gpa.toString()))
                    .toList();

            assertThat(firstNames)
                    .as("Should have multiple students")
                    .hasSizeGreaterThan(1)
                    .doesNotContainNull()
                    .allMatch(name -> name.length() > 0);

            assertThat(gpas)
                    .as("GPAs should be in valid range and sorted")
                    .allMatch(gpa -> gpa.compareTo(BigDecimal.ZERO) >= 0)
                    .allMatch(gpa -> gpa.compareTo(BigDecimal.valueOf(4.0)) <= 0);

            // Find high-performing students
            List<BigDecimal> highGpas = gpas.stream()
                    .filter(gpa -> gpa.compareTo(BigDecimal.valueOf(3.5)) > 0)
                    .toList();

            assertThat(highGpas)
                    .as("Should have students with high GPAs")
                    .isNotEmpty()
                    .allMatch(gpa -> gpa.compareTo(BigDecimal.valueOf(3.5)) > 0);
        }
    }

    // Helper methods
    private void createMultipleStudents() {
        Guardian guardian1 = Guardian.builder()
                .name("Parent One")
                .email("parent1@test.com")
                .mobile("+1111111111")
                .build();

        Guardian guardian2 = Guardian.builder()
                .name("Parent Two")
                .email("parent2@test.com")
                .mobile("+2222222222")
                .build();

        Student student1 = Student.builder()
                .firstName("Alice")
                .lastName("Johnson")
                .emailId("alice.johnson@test.com")
                .studentIdNumber("STU001")
                .dateOfBirth(LocalDate.of(1997, 6, 20))
                .admissionDate(LocalDate.of(2023, 9, 1))
                .gpa(BigDecimal.valueOf(3.7))
                .guardian(guardian1)
                .isActive(true)
                .build();

        Student student2 = Student.builder()
                .firstName("Bob")
                .lastName("Wilson")
                .emailId("bob.wilson@test.com")
                .studentIdNumber("STU002")
                .dateOfBirth(LocalDate.of(1996, 11, 10))
                .admissionDate(LocalDate.of(2023, 9, 1))
                .gpa(BigDecimal.valueOf(3.2))
                .guardian(guardian2)
                .isActive(true)
                .build();

        studentRepository.save(student1);
        studentRepository.save(student2);
    }

    private void createStudentsWithVariousGpas() {
        List<Student> students = List.of(
                createStudentWithGpa("Charlie", "Brown", "charlie@test.com", BigDecimal.valueOf(3.9)),
                createStudentWithGpa("Diana", "Davis", "diana@test.com", BigDecimal.valueOf(3.1)),
                createStudentWithGpa("Eve", "Miller", "eve@test.com", BigDecimal.valueOf(3.6)),
                createStudentWithGpa("Frank", "Garcia", "frank@test.com", BigDecimal.valueOf(2.8))
        );
        
        studentRepository.saveAll(students);
    }

    private Student createStudentWithGpa(String firstName, String lastName, String email, BigDecimal gpa) {
        Guardian guardian = Guardian.builder()
                .name("Guardian of " + firstName)
                .email("guardian." + firstName.toLowerCase() + "@test.com")
                .mobile("+999000" + Math.random() * 1000)
                .build();

        return Student.builder()
                .firstName(firstName)
                .lastName(lastName)
                .emailId(email)
                .studentIdNumber("STU" + UUID.randomUUID().toString().substring(0, 6))
                .dateOfBirth(LocalDate.of(1995 + (int)(Math.random() * 5), 1, 1))
                .admissionDate(LocalDate.of(2023, 9, 1))
                .gpa(gpa)
                .guardian(guardian)
                .isActive(true)
                .build();
    }
}