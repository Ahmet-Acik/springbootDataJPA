package com.example.springdatajpa.api;

import com.example.springdatajpa.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SimpleConflictTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private StudentRepository studentRepository;



    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/students";
        studentRepository.deleteAll();
    }

    @Test
    void testDuplicateEmailHandling() throws Exception {
        // First create a student successfully
        Map<String, Object> student1 = createValidStudentMap();
        student1.put("emailId", "test@example.com");
        student1.put("studentIdNumber", "STU001");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request1 = new HttpEntity<>(student1, headers);

        ResponseEntity<String> response1 = restTemplate.postForEntity(baseUrl, request1, String.class);
        System.out.println("First student response: " + response1.getStatusCode() + " - " + response1.getBody());

        // Now try to create another student with the same email
        Map<String, Object> student2 = createValidStudentMap();
        student2.put("emailId", "test@example.com"); // Same email
        student2.put("studentIdNumber", "STU002"); // Different ID

        HttpEntity<Map<String, Object>> request2 = new HttpEntity<>(student2, headers);

        ResponseEntity<String> response2 = restTemplate.postForEntity(baseUrl, request2, String.class);
        System.out.println("Second student response: " + response2.getStatusCode() + " - " + response2.getBody());
        System.out.println("Response headers: " + response2.getHeaders());
    }

    private Map<String, Object> createValidStudentMap() {
        Map<String, Object> student = new HashMap<>();
        student.put("firstName", "John");
        student.put("lastName", "Doe");
        student.put("emailId", "john.doe@example.com");
        student.put("studentIdNumber", "STU" + UUID.randomUUID().toString().substring(0, 8));
        student.put("dateOfBirth", "1990-01-01");
        student.put("admissionDate", "2021-09-01");
        student.put("gpa", 3.5);
        student.put("guardianName", "Jane Doe");
        student.put("guardianEmail", "jane.doe@example.com");
        student.put("guardianMobileNumber", "1234567890");
        student.put("isActive", true);
        return student;
    }
}