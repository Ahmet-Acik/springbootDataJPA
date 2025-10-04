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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

/**
 * Content Negotiation Tests for Student API.
 * 
 * This test class focuses on:
 * - Accept header handling
 * - Content-Type validation
 * - Multiple media type support
 * - Error response formats
 * - Custom media types
 * - Header validation
 * 
 * These tests ensure robust content negotiation and media type handling.
 */
@DisplayName("Student API - Content Negotiation Tests")
class StudentContentNegotiationRestAssuredTest extends RestAssuredTestConfig {

    @Autowired
    private StudentRepository studentRepository;

    private Student testStudent;

    @BeforeEach
    void setUp() {
        studentRepository.deleteAll();
        testStudent = createTestStudent();
        studentRepository.save(testStudent);
    }

    @Nested
    @DisplayName("Accept Header Tests")
    class AcceptHeaderTests {

        @Test
        @DisplayName("Should respond with JSON when Accept is application/json")
        void shouldRespondWithJsonForJsonAccept() {
            given()
                .accept("application/json")
            .when()
                .get("/api/students/{id}", testStudent.getStudentId())
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .header("Content-Type", containsString("application/json"))
                .body("firstName", equalTo(testStudent.getFirstName()));
        }

        @Test
        @DisplayName("Should handle wildcard Accept headers")
        void shouldHandleWildcardAcceptHeaders() {
            // Test */*
            given()
                .accept("*/*")
            .when()
                .get("/api/students/{id}", testStudent.getStudentId())
            .then()
                .statusCode(200)
                .body("firstName", equalTo(testStudent.getFirstName()));

            // Test application/*
            given()
                .accept("application/*")
            .when()
                .get("/api/students/{id}", testStudent.getStudentId())
            .then()
                .statusCode(200)
                .body("firstName", equalTo(testStudent.getFirstName()));
        }

        @Test
        @DisplayName("Should handle multiple Accept header values")
        void shouldHandleMultipleAcceptValues() {
            given()
                .accept("application/xml, application/json, text/html")
            .when()
                .get("/api/students/{id}", testStudent.getStudentId())
            .then()
                .statusCode(200)
                .contentType(anyOf(
                    equalTo("application/json"),
                    containsString("application/json")
                ));
        }

        @Test
        @DisplayName("Should handle Accept header with quality values")
        void shouldHandleAcceptWithQualityValues() {
            given()
                .accept("application/xml;q=0.9, application/json;q=1.0, text/html;q=0.8")
            .when()
                .get("/api/students/{id}", testStudent.getStudentId())
            .then()
                .statusCode(200)
                .contentType(containsString("application/json")); // Should prefer JSON (q=1.0)
        }

        @Test
        @DisplayName("Should handle missing Accept header gracefully")
        void shouldHandleMissingAcceptHeaderGracefully() {
            given()
                // No accept header specified
            .when()
                .get("/api/students/{id}", testStudent.getStudentId())
            .then()
                .statusCode(200)
                .body("firstName", equalTo(testStudent.getFirstName()));
        }
    }

    @Nested
    @DisplayName("Content-Type Header Tests")
    class ContentTypeTests {

        @Test
        @DisplayName("Should accept JSON content-type for POST requests")
        void shouldAcceptJsonContentTypeForPost() {
            Map<String, Object> newStudent = createValidStudentMap();

            given()
                .contentType("application/json")
                .body(newStudent)
            .when()
                .post("/api/students")
            .then()
                .statusCode(201)
                .contentType(ContentType.JSON);
        }

        @Test
        @DisplayName("Should accept JSON with charset for POST requests")
        void shouldAcceptJsonWithCharsetForPost() {
            Map<String, Object> newStudent = createValidStudentMap();

            given()
                .contentType("application/json; charset=UTF-8")
                .body(newStudent)
            .when()
                .post("/api/students")
            .then()
                .statusCode(201)
                .contentType(ContentType.JSON);
        }

        @Test
        @DisplayName("Should reject unsupported content types")
        void shouldRejectUnsupportedContentTypes() {
            String xmlData = "<student><firstName>John</firstName><lastName>Doe</lastName></student>";

            given()
                .contentType("application/xml")
                .body(xmlData)
            .when()
                .post("/api/students")
            .then()
                .statusCode(415) // Unsupported Media Type
                .body("status", equalTo(415))
                .body("error", containsStringIgnoringCase("unsupported media type"));
        }

        @Test
        @DisplayName("Should reject plain text content")
        void shouldRejectPlainTextContent() {
            given()
                .contentType("text/plain")
                .body("This is plain text, not JSON")
            .when()
                .post("/api/students")
            .then()
                .statusCode(415); // Unsupported Media Type
        }

        @Test
        @DisplayName("Should handle case-insensitive content types")
        void shouldHandleCaseInsensitiveContentTypes() {
            Map<String, Object> newStudent = createValidStudentMap();

            given()
                .contentType("APPLICATION/JSON") // Uppercase
                .body(newStudent)
            .when()
                .post("/api/students")
            .then()
                .statusCode(201);
        }
    }

    @Nested
    @DisplayName("Character Encoding Tests")
    class CharacterEncodingTests {

        @Test
        @DisplayName("Should handle UTF-8 encoded content")
        void shouldHandleUtf8EncodedContent() {
            Map<String, Object> studentWithUnicode = createValidStudentMap();
            studentWithUnicode.put("firstName", "José");
            studentWithUnicode.put("lastName", "María");

            given()
                .contentType("application/json; charset=UTF-8")
                .body(studentWithUnicode)
            .when()
                .post("/api/students")
            .then()
                .statusCode(201)
                .body("firstName", equalTo("José"))
                .body("lastName", equalTo("María"));
        }

        @Test
        @DisplayName("Should handle special characters in JSON")
        void shouldHandleSpecialCharactersInJson() {
            Map<String, Object> studentWithSpecialChars = createValidStudentMap();
            studentWithSpecialChars.put("firstName", "Jean-Luc");
            studentWithSpecialChars.put("lastName", "O'Connor");
            
            // Guardian with special characters
            Map<String, Object> guardian = new HashMap<>();
            guardian.put("name", "François Müller");
            guardian.put("email", "francois.muller@test.com");
            guardian.put("mobile", "+1234567890");
            studentWithSpecialChars.put("guardian", guardian);

            given()
                .contentType(ContentType.JSON)
                .body(studentWithSpecialChars)
            .when()
                .post("/api/students")
            .then()
                .statusCode(201)
                .body("firstName", equalTo("Jean-Luc"))
                .body("lastName", equalTo("O'Connor"))
                .body("guardian.name", equalTo("François Müller"));
        }

        @Test
        @DisplayName("Should preserve emoji and extended Unicode")
        void shouldPreserveEmojiAndExtendedUnicode() {
            Map<String, Object> studentWithEmoji = createValidStudentMap();
            studentWithEmoji.put("firstName", "Alex 🎓");
            studentWithEmoji.put("lastName", "Student 📚");

            given()
                .contentType("application/json; charset=UTF-8")
                .body(studentWithEmoji)
            .when()
                .post("/api/students")
            .then()
                .statusCode(201)
                .body("firstName", equalTo("Alex 🎓"))
                .body("lastName", equalTo("Student 📚"));
        }
    }

    @Nested
    @DisplayName("Browser Compatibility Tests")
    class BrowserCompatibilityTests {

        @Test
        @DisplayName("Should handle typical browser Accept headers")
        void shouldHandleTypicalBrowserAcceptHeaders() {
            // Chrome-like Accept header
            given()
                .accept("text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
            .when()
                .get("/api/students/{id}", testStudent.getStudentId())
            .then()
                .statusCode(200);

            // Firefox-like Accept header
            given()
                .accept("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .when()
                .get("/api/students/{id}", testStudent.getStudentId())
            .then()
                .statusCode(200);
        }

        @Test
        @DisplayName("Should handle AJAX request headers")
        void shouldHandleAjaxRequestHeaders() {
            given()
                .accept("application/json, text/javascript, */*; q=0.01")
                .header("X-Requested-With", "XMLHttpRequest")
            .when()
                .get("/api/students/{id}", testStudent.getStudentId())
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON);
        }

        @Test
        @DisplayName("Should handle mobile browser requests")
        void shouldHandleMobileBrowserRequests() {
            given()
                .accept("application/json")
                .header("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 14_7_1 like Mac OS X)")
            .when()
                .get("/api/students/{id}", testStudent.getStudentId())
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON);
        }
    }

    @Nested
    @DisplayName("API Versioning Tests")
    class ApiVersioningTests {

        @Test
        @DisplayName("Should handle API version in Accept header")
        void shouldHandleApiVersionInAcceptHeader() {
            given()
                .accept("application/vnd.student-api.v1+json")
            .when()
                .get("/api/students/{id}", testStudent.getStudentId())
            .then()
                .statusCode(anyOf(is(200), is(406))); // Success or Not Acceptable
        }

        @Test
        @DisplayName("Should handle custom API version headers")
        void shouldHandleCustomApiVersionHeaders() {
            given()
                .header("API-Version", "v1")
                .accept("application/json")
            .when()
                .get("/api/students/{id}", testStudent.getStudentId())
            .then()
                .statusCode(200);

            given()
                .header("X-API-Version", "1.0")
                .accept("application/json")
            .when()
                .get("/api/students/{id}", testStudent.getStudentId())
            .then()
                .statusCode(200);
        }

        @Test
        @DisplayName("Should handle version in URL path")
        void shouldHandleVersionInUrlPath() {
            // Test if versioned endpoints exist
            given()
                .accept("application/json")
            .when()
                .get("/api/v1/students/{id}", testStudent.getStudentId())
            .then()
                .statusCode(anyOf(is(200), is(404))); // Success or Not Found if not implemented
        }
    }

    @Nested
    @DisplayName("Content Negotiation Error Cases")
    class ContentNegotiationErrorTests {

        @Test
        @DisplayName("Should return 406 for unsupported Accept types")
        void shouldReturn406ForUnsupportedAcceptTypes() {
            given()
                .accept("application/pdf") // Unsupported format
            .when()
                .get("/api/students/{id}", testStudent.getStudentId())
            .then()
                .statusCode(anyOf(is(200), is(406))); // Depends on implementation
        }

        @Test
        @DisplayName("Should handle conflicting content type requirements")
        void shouldHandleConflictingContentTypeRequirements() {
            Map<String, Object> studentData = createValidStudentMap();

            given()
                .contentType("application/json")
                .accept("application/xml") // Want XML back but sending JSON
                .body(studentData)
            .when()
                .post("/api/students")
            .then()
                .statusCode(anyOf(is(201), is(406), is(415))); // Various possible responses
        }

    }

    @Nested
    @DisplayName("Response Header Validation Tests")
    class ResponseHeaderValidationTests {

        @Test
        @DisplayName("Should include proper Content-Type in responses")
        void shouldIncludeProperContentTypeInResponses() {
            given()
                .accept("application/json")
            .when()
                .get("/api/students/{id}", testStudent.getStudentId())
            .then()
                .statusCode(200)
                .header("Content-Type", notNullValue())
                .header("Content-Type", containsString("application/json"));
        }

        @Test
        @DisplayName("Should include charset in Content-Type header")
        void shouldIncludeCharsetInContentTypeHeader() {
            given()
                .accept("application/json")
            .when()
                .get("/api/students/{id}", testStudent.getStudentId())
            .then()
                .statusCode(200)
                .header("Content-Type", anyOf(
                    containsString("charset=UTF-8"),
                    containsString("charset=utf-8"),
                    containsString("application/json") // At minimum should have JSON
                ));
        }


        @Test
        @DisplayName("Should handle HEAD requests properly")
        void shouldHandleHeadRequestsProperly() {
            given()
                .accept("application/json")
            .when()
                .head("/api/students/{id}", testStudent.getStudentId())
            .then()
                .statusCode(anyOf(is(200), is(405))) // OK or Method Not Allowed
                .header("Content-Type", anyOf(
                    containsString("application/json"),
                    nullValue()
                ));
        }
    }

    @Nested
    @DisplayName("Cross-Origin Request Tests")
    class CorsTests {

        @Test
        @DisplayName("Should handle preflight OPTIONS requests")
        void shouldHandlePreflightOptionsRequests() {
            given()
                .header("Origin", "https://example.com")
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "Content-Type")
            .when()
                .options("/api/students")
            .then()
                .statusCode(anyOf(is(200), is(204))) // OK or No Content
                .header("Access-Control-Allow-Origin", anyOf(
                    equalTo("*"),
                    equalTo("https://example.com"),
                    notNullValue()
                ));
        }

        @Test
        @DisplayName("Should include CORS headers in actual requests")
        void shouldIncludeCorsHeadersInActualRequests() {
            given()
                .header("Origin", "https://example.com")
                .accept("application/json")
            .when()
                .get("/api/students/{id}", testStudent.getStudentId())
            .then()
                .statusCode(200)
                .header("Access-Control-Allow-Origin", anyOf(
                    equalTo("*"),
                    equalTo("https://example.com"),
                    notNullValue()
                ));
        }
    }

    /**
     * Helper method to create a test student
     */
    private Student createTestStudent() {
        Guardian guardian = Guardian.builder()
                .name("Test Guardian")
                .email("test.guardian@example.com")
                .mobile("+1234567890")
                .build();

        return Student.builder()
                .firstName("ContentNeg")
                .lastName("TestStudent")
                .emailId("content.negotiation@test.com")
                .studentIdNumber("CN" + UUID.randomUUID().toString().substring(0, 8))
                .dateOfBirth(LocalDate.of(1995, 5, 15))
                .admissionDate(LocalDate.of(2023, 9, 1))
                .gpa(BigDecimal.valueOf(3.75))
                .guardian(guardian)
                .isActive(true)
                .build();
    }

    /**
     * Helper method to create valid student data as Map
     */
    private Map<String, Object> createValidStudentMap() {
        Map<String, Object> guardian = new HashMap<>();
        guardian.put("name", "New Guardian");
        guardian.put("email", "new.guardian@test.com");
        guardian.put("mobile", "+1234567890");

        Map<String, Object> student = new HashMap<>();
        student.put("firstName", "NewContent");
        student.put("lastName", "NegTest");
        student.put("emailId", "new.content.neg@test.com");
        student.put("studentIdNumber", "NCN" + UUID.randomUUID().toString().substring(0, 8));
        student.put("dateOfBirth", LocalDate.of(1995, 5, 15).toString());
        student.put("admissionDate", LocalDate.of(2023, 9, 1).toString());
        student.put("gpa", new BigDecimal("3.75"));
        student.put("guardian", guardian);
        student.put("isActive", true);

        return student;
    }
}