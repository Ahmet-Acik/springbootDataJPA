package com.example.springdatajpa.controller;

import com.example.springdatajpa.entity.Student;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should return 400 for invalid student data")
    void shouldValidateStudentData() throws Exception {
        // Given - Invalid student data
        Student invalidStudent = Student.builder()
                .firstName("") // Invalid: empty
                .lastName("ValidName")
                .emailId("invalid-email") // Invalid: not a valid email
                .gpa(java.math.BigDecimal.valueOf(5.0)) // Invalid: exceeds 4.0 max
                .build();

        // When & Then
        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidStudent)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should accept valid student data")
    void shouldAcceptValidStudentData() throws Exception {
        // Given - Valid student data
        Student validStudent = Student.builder()
                .firstName("John")
                .lastName("Doe")
                .emailId("john.doe@example.com")
                .gpa(java.math.BigDecimal.valueOf(3.5))
                .build();

        // When & Then
        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validStudent)))
                .andExpect(status().isCreated());
    }
}