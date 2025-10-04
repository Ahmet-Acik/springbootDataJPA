package com.example.springdatajpa.api;

import com.example.springdatajpa.config.RestAssuredTestConfig;
import com.example.springdatajpa.entity.Guardian;
import com.example.springdatajpa.entity.Student;
import com.example.springdatajpa.repository.StudentRepository;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static io.restassured.RestAssured.*;
import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;

/**
 * Performance and Concurrency Tests for Student API.
 * 
 * This test class focuses on:
 * - Response time validation
 * - Bulk operations performance
 * - Concurrent request handling
 * - Load testing scenarios
 * - Resource usage patterns
 * - Scalability testing
 * 
 * These tests ensure the API performs well under various load conditions.
 */
@DisplayName("Student API - Performance and Concurrency Tests")
class StudentPerformanceRestAssuredTest extends RestAssuredTestConfig {

    @Autowired
    private StudentRepository studentRepository;

    @BeforeEach
    void setUp() {
        studentRepository.deleteAll();
    }

    @Nested
    @DisplayName("Response Time Tests")
    class ResponseTimeTests {

        @Test
        @DisplayName("Should respond to GET requests within acceptable time")
        void shouldRespondToGetRequestsQuickly() {
            // Create some test data
            createTestStudents(5);

            given()
            .when()
                .get("/api/students")
            .then()
                .statusCode(200)
                .time(lessThan(1000L)) // Less than 1 second
                .body("content", hasSize(greaterThan(0)));
        }

        @Test
        @DisplayName("Should create students within acceptable time")
        void shouldCreateStudentsQuickly() {
            Map<String, Object> studentData = createValidStudentMap();

            given()
                .contentType(ContentType.JSON)
                .body(studentData)
            .when()
                .post("/api/students")
            .then()
                .statusCode(201)
                .time(lessThan(2000L)) // Less than 2 seconds
                .body("studentId", notNullValue());
        }
        
        @Test
        @DisplayName("Should handle large result sets efficiently")
        void shouldHandleLargeResultSetsEfficiently() {
            // Create a larger dataset
            createTestStudents(50);

            given()
                .queryParam("size", 50)
            .when()
                .get("/api/students")
            .then()
                .statusCode(200)
                .time(lessThan(3000L)) // Less than 3 seconds for 50 records
                .body("content", hasSize(50));
        }

        @Test
        @DisplayName("Should handle complex queries efficiently")
        void shouldHandleComplexQueriesEfficiently() {
            createTestStudents(20);

            given()
                .queryParam("page", 0)
                .queryParam("size", 10)
                .queryParam("sort", "gpa,desc")
                .queryParam("sort", "lastName,asc")
                .queryParam("minGpa", "2.0")
                .queryParam("isActive", "true")
            .when()
                .get("/api/students")
            .then()
                .statusCode(200)
                .time(lessThan(2000L)); // Less than 2 seconds for complex query
        }
    }

    @Nested
    @DisplayName("Bulk Operations Tests")
    class BulkOperationsTests {

        @Test
        @DisplayName("Should handle multiple sequential creations efficiently")
        void shouldHandleSequentialCreationsEfficiently() {
            int numberOfStudents = 10;
            long startTime = System.currentTimeMillis();

            for (int i = 0; i < numberOfStudents; i++) {
                Map<String, Object> studentData = createValidStudentMap();
                studentData.put("emailId", "bulk.test." + i + "@example.com");
                studentData.put("studentIdNumber", "BULK" + String.format("%03d", i));

                given()
                    .contentType(ContentType.JSON)
                    .body(studentData)
                .when()
                    .post("/api/students")
                .then()
                    .statusCode(201);
            }

            long totalTime = System.currentTimeMillis() - startTime;
            
            // Average should be reasonable (less than 500ms per student)
            assertThat(totalTime / numberOfStudents).isLessThan(500L);
        }

        @Test
        @DisplayName("Should handle bulk retrieval operations efficiently")
        void shouldHandleBulkRetrievalEfficiently() {
            // Create test data
            List<Student> students = createTestStudents(30);
            
            long startTime = System.currentTimeMillis();

            // Retrieve all students
            Response response = given()
                .queryParam("size", 100)
            .when()
                .get("/api/students")
            .then()
                .statusCode(200)
                .extract().response();

            long totalTime = System.currentTimeMillis() - startTime;

            assertThat(totalTime).isLessThan(2000L); // Less than 2 seconds
            assertThat(response.jsonPath().getList("content")).hasSize(30);
        }

        @Test
        @DisplayName("Should handle pagination efficiently with large datasets")
        void shouldHandlePaginationEfficiently() {
            createTestStudents(100);

            // Test multiple page requests
            for (int page = 0; page < 5; page++) {
                given()
                    .queryParam("page", page)
                    .queryParam("size", 20)
                .when()
                    .get("/api/students")
                .then()
                    .statusCode(200)
                    .time(lessThan(1000L)) // Each page should load quickly
                    .body("content", hasSize(20));
            }
        }
    }

    @Nested
    @DisplayName("Concurrent Access Tests")
    class ConcurrentAccessTests {

        @Test
        @DisplayName("Should handle concurrent read requests")
        void shouldHandleConcurrentReadRequests() {
            // Create test data
            createTestStudents(10);

            // Create multiple concurrent read requests
            int numberOfThreads = 5;
            ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
            List<CompletableFuture<Response>> futures = new ArrayList<>();

            for (int i = 0; i < numberOfThreads; i++) {
                CompletableFuture<Response> future = CompletableFuture.supplyAsync(() -> {
                    return given()
                        .when()
                            .get("/api/students")
                        .then()
                            .statusCode(200)
                            .time(lessThan(3000L))
                            .extract().response();
                }, executor);
                futures.add(future);
            }

            // Wait for all requests to complete
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0]));
            
            assertThatCode(() -> allFutures.get(10, TimeUnit.SECONDS))
                .doesNotThrowAnyException();

            executor.shutdown();
        }

        @Test
        @DisplayName("Should handle concurrent write requests")
        void shouldHandleConcurrentWriteRequests() {
            int numberOfThreads = 3;
            ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
            List<CompletableFuture<Response>> futures = new ArrayList<>();

            for (int i = 0; i < numberOfThreads; i++) {
                final int threadId = i;
                CompletableFuture<Response> future = CompletableFuture.supplyAsync(() -> {
                    Map<String, Object> studentData = createValidStudentMap();
                    studentData.put("emailId", "concurrent.test." + threadId + "@example.com");
                    studentData.put("studentIdNumber", "CONC" + String.format("%03d", threadId));

                    return given()
                        .contentType(ContentType.JSON)
                        .body(studentData)
                    .when()
                        .post("/api/students")
                    .then()
                        .statusCode(201)
                        .time(lessThan(5000L))
                        .extract().response();
                }, executor);
                futures.add(future);
            }

            // Wait for all requests to complete
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0]));
            
            assertThatCode(() -> allFutures.get(15, TimeUnit.SECONDS))
                .doesNotThrowAnyException();

            // Verify all students were created
            assertThat(studentRepository.count()).isEqualTo(numberOfThreads);

            executor.shutdown();
        }

    }

    @Nested
    @DisplayName("Load Testing Scenarios")
    class LoadTestingTests {

        @Test
        @DisplayName("Should maintain performance under sustained load")
        void shouldMaintainPerformanceUnderSustainedLoad() {
            // Create initial dataset
            createTestStudents(20);

            // Simulate sustained load with multiple requests
            long startTime = System.currentTimeMillis();
            List<Long> responseTimes = new ArrayList<>();

            for (int i = 0; i < 20; i++) {
                long requestStart = System.currentTimeMillis();
                
                given()
                .when()
                    .get("/api/students")
                .then()
                    .statusCode(200);
                
                long requestTime = System.currentTimeMillis() - requestStart;
                responseTimes.add(requestTime);
                
                // Small delay between requests
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            long totalTime = System.currentTimeMillis() - startTime;
            
            // Calculate statistics
            double avgResponseTime = responseTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);
            
            long maxResponseTime = responseTimes.stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0L);

            // Assertions
            assertThat(avgResponseTime).isLessThan(1000.0); // Average under 1 second
            assertThat(maxResponseTime).isLessThan(3000L);  // Max under 3 seconds
            assertThat(totalTime).isLessThan(25000L);       // Total under 25 seconds
        }

        @Test
        @DisplayName("Should handle burst traffic patterns")
        void shouldHandleBurstTrafficPatterns() {
            createTestStudents(10);

            // Simulate burst of requests
            ExecutorService executor = Executors.newFixedThreadPool(10);
            List<CompletableFuture<Long>> futures = new ArrayList<>();

            long burstStart = System.currentTimeMillis();

            // Create burst of 10 concurrent requests
            for (int i = 0; i < 10; i++) {
                CompletableFuture<Long> future = CompletableFuture.supplyAsync(() -> {
                    long requestStart = System.currentTimeMillis();
                    
                    given()
                    .when()
                        .get("/api/students")
                    .then()
                        .statusCode(200);
                    
                    return System.currentTimeMillis() - requestStart;
                }, executor);
                futures.add(future);
            }

            // Wait for all requests and collect response times
            List<Long> responseTimes = futures.stream()
                .map(CompletableFuture::join)
                .toList();

            long burstDuration = System.currentTimeMillis() - burstStart;

            // Verify burst handling
            double avgResponseTime = responseTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);

            assertThat(avgResponseTime).isLessThan(2000.0); // Average under 2 seconds
            assertThat(burstDuration).isLessThan(10000L);   // Burst completed under 10 seconds
            assertThat(responseTimes).hasSize(10);          // All requests completed

            executor.shutdown();
        }
    }

    @Nested
    @DisplayName("Memory and Resource Usage Tests")
    class ResourceUsageTests {

        @Test
        @DisplayName("Should handle large payloads efficiently")
        void shouldHandleLargePayloadsEfficiently() {
            // Create student with larger text fields
            Map<String, Object> largePayload = createValidStudentMap();
            largePayload.put("firstName", "A".repeat(100));
            largePayload.put("lastName", "B".repeat(100));
            
            // Add large guardian data
            Map<String, Object> largeGuardian = new HashMap<>();
            largeGuardian.put("name", "C".repeat(100));
            largeGuardian.put("email", "large.guardian@verylongdomainname.com");
            largeGuardian.put("mobile", "+1234567890123456789");
            largePayload.put("guardian", largeGuardian);

            given()
                .contentType(ContentType.JSON)
                .body(largePayload)
            .when()
                .post("/api/students")
            .then()
                .statusCode(anyOf(is(201), is(400))) // Success or validation error
                .time(lessThan(3000L)); // Should handle within reasonable time
        }

        @Test
        @DisplayName("Should maintain performance with repeated operations")
        void shouldMaintainPerformanceWithRepeatedOperations() {
            List<Long> createTimes = new ArrayList<>();
            List<Long> readTimes = new ArrayList<>();

            // Perform repeated create and read operations
            for (int i = 0; i < 5; i++) {
                // Create operation
                long createStart = System.currentTimeMillis();
                Map<String, Object> studentData = createValidStudentMap();
                studentData.put("emailId", "repeated.test." + i + "@example.com");
                studentData.put("studentIdNumber", "RPT" + String.format("%03d", i));

                given()
                    .contentType(ContentType.JSON)
                    .body(studentData)
                .when()
                    .post("/api/students")
                .then()
                    .statusCode(201);
                
                createTimes.add(System.currentTimeMillis() - createStart);

                // Read operation
                long readStart = System.currentTimeMillis();
                given()
                .when()
                    .get("/api/students")
                .then()
                    .statusCode(200);
                
                readTimes.add(System.currentTimeMillis() - readStart);
            }

            // Verify performance doesn't degrade significantly
            assertThat(createTimes.get(createTimes.size() - 1))
                .isLessThan(createTimes.get(0) * 3); // Last shouldn't be 3x slower than first
            
            assertThat(readTimes.get(readTimes.size() - 1))
                .isLessThan(readTimes.get(0) * 3); // Last shouldn't be 3x slower than first
        }
    }

    /**
     * Helper method to create test students in bulk
     */
    private List<Student> createTestStudents(int count) {
        List<Student> students = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            Guardian guardian = Guardian.builder()
                    .name("Guardian " + i)
                    .email("guardian." + i + "@test.com")
                    .mobile("+123456789" + String.format("%02d", i))
                    .build();

            Student student = Student.builder()
                    .firstName("TestFirst" + i)
                    .lastName("TestLast" + i)
                    .emailId("performance.test." + i + "@example.com")
                    .studentIdNumber("PERF" + String.format("%03d", i))
                    .dateOfBirth(LocalDate.of(1995 + (i % 5), 1 + (i % 12), 1 + (i % 28)))
                    .admissionDate(LocalDate.of(2023, 1 + (i % 12), 1 + (i % 28)))
                    .gpa(BigDecimal.valueOf(2.0 + (i % 3) * 0.5))
                    .guardian(guardian)
                    .isActive(i % 2 == 0)
                    .build();

            students.add(student);
        }

        return studentRepository.saveAll(students);
    }

    /**
     * Helper method to create valid student data as Map for JSON serialization
     */
    private Map<String, Object> createValidStudentMap() {
        Map<String, Object> guardian = new HashMap<>();
        guardian.put("name", "Performance Guardian");
        guardian.put("email", "performance.guardian@test.com");
        guardian.put("mobile", "+1234567890");

        Map<String, Object> student = new HashMap<>();
        student.put("firstName", "Performance");
        student.put("lastName", "Test");
        student.put("emailId", "performance.test@example.com");
        student.put("studentIdNumber", "PERF" + UUID.randomUUID().toString().substring(0, 8));
        student.put("dateOfBirth", LocalDate.of(1995, 5, 15).toString());
        student.put("admissionDate", LocalDate.of(2023, 9, 1).toString());
        student.put("gpa", new BigDecimal("3.75"));
        student.put("guardian", guardian);
        student.put("isActive", true);

        return student;
    }
}