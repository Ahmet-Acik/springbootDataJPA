package com.example.springdatajpa.controller;

import com.example.springdatajpa.entity.Student;
import com.example.springdatajpa.entity.Course;
import com.example.springdatajpa.entity.Department;
import com.example.springdatajpa.service.StudentService;
import com.example.springdatajpa.repository.StudentRepository;
import com.example.springdatajpa.repository.DepartmentRepository;
import com.example.springdatajpa.repository.CourseRepository;
import com.example.springdatajpa.config.BaseIntegrationTestConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web layer integration tests for StudentController REST API endpoints.
 * Tests complete HTTP request/response cycle using MockMvc.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@AutoConfigureMockMvc
@Transactional
public class StudentControllerWebIntegrationTest extends BaseIntegrationTestConfig.WebIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StudentService studentService;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Student testStudent;
    private Department testDepartment;
    private Course testCourse;

    @BeforeEach
    void setUp() {
        // Clean repositories
        studentRepository.deleteAll();
        courseRepository.deleteAll();
        departmentRepository.deleteAll();

        // Create test department
        testDepartment = Department.builder()
                .departmentName("Computer Science")
                .departmentCode("CS")
                .headOfDepartment("Dr. Smith")
                .departmentAddress("Building A")
                .departmentType(Department.DepartmentType.ENGINEERING)
                .isActive(true)
                .build();
        testDepartment = departmentRepository.save(testDepartment);

        // Create test course
        testCourse = Course.builder()
                .courseCode("CS101")
                .title("Introduction to Programming")
                .description("Basic programming concepts")
                .creditHours(new BigDecimal("3.0"))
                .courseLevel(Course.CourseLevel.BEGINNER)
                .department(testDepartment)
                .isActive(true)
                .build();
        testCourse = courseRepository.save(testCourse);

        // Create test student
        testStudent = Student.builder()
                .firstName("John")
                .lastName("Doe")
                .emailId("john.doe@test.com")
                .studentIdNumber("STU001")
                .admissionDate(LocalDate.now())
                .dateOfBirth(LocalDate.of(2000, 1, 1))
                .studentStatus(Student.StudentStatus.ACTIVE)
                .gpa(new BigDecimal("3.50"))
                .isActive(true)
                .build();
    }

    @Nested
    @DisplayName("Student API CRUD Operations")
    class StudentCRUDApiTests {

        @Test
        @DisplayName("POST /api/students - Should create new student")
        void shouldCreateStudent() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/students")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testStudent)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.firstName", is(testStudent.getFirstName())))
                    .andExpect(jsonPath("$.lastName", is(testStudent.getLastName())))
                    .andExpect(jsonPath("$.emailId", is(testStudent.getEmailId())))
                    .andExpect(jsonPath("$.studentId").exists())
                    .andExpect(jsonPath("$.isActive", is(true)));
        }

        @Test
        @DisplayName("GET /api/students/{id} - Should retrieve student by ID")
        void shouldRetrieveStudentById() throws Exception {
            // Given
            Student createdStudent = studentService.createStudent(testStudent);

            // When & Then
            mockMvc.perform(get("/api/students/{id}", createdStudent.getStudentId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.studentId", is(createdStudent.getStudentId().intValue())))
                    .andExpect(jsonPath("$.firstName", is(testStudent.getFirstName())))
                    .andExpect(jsonPath("$.lastName", is(testStudent.getLastName())));
        }

        @Test
        @DisplayName("GET /api/students/{id} - Should return 404 for non-existent student")
        void shouldReturn404ForNonExistentStudent() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/students/{id}", 999L)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("PUT /api/students/{id} - Should update existing student")
        void shouldUpdateStudent() throws Exception {
            // Given
            Student createdStudent = studentService.createStudent(testStudent);
            createdStudent.setFirstName("Jane");
            createdStudent.setLastName("Smith");

            // When & Then
            mockMvc.perform(put("/api/students/{id}", createdStudent.getStudentId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createdStudent)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.firstName", is("Jane")))
                    .andExpect(jsonPath("$.lastName", is("Smith")));
        }

        @Test
        @DisplayName("DELETE /api/students/{id} - Should soft delete student")
        void shouldSoftDeleteStudent() throws Exception {
            // Given
            Student createdStudent = studentService.createStudent(testStudent);

            // When & Then
            mockMvc.perform(delete("/api/students/{id}", createdStudent.getStudentId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isNoContent());

            // Verify soft deletion
            mockMvc.perform(get("/api/students/{id}", createdStudent.getStudentId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isActive", is(false)));
        }

        @Test
        @DisplayName("GET /api/students - Should retrieve all students")
        void shouldRetrieveAllStudents() throws Exception {
            // Given
            Student student1 = studentService.createStudent(testStudent);
            Student student2 = Student.builder()
                    .firstName("Jane")
                    .lastName("Smith")
                    .emailId("jane.smith@test.com")
                    .studentIdNumber("STU002")
                    .admissionDate(LocalDate.now())
                    .studentStatus(Student.StudentStatus.ACTIVE)
                    .gpa(new BigDecimal("3.75"))
                    .isActive(true)
                    .build();
            studentService.createStudent(student2);

            // When & Then
            mockMvc.perform(get("/api/students")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].firstName", is(testStudent.getFirstName())))
                    .andExpect(jsonPath("$[1].firstName", is("Jane")));
        }
    }

    @Nested
    @DisplayName("Student Search API Tests")
    class StudentSearchApiTests {

        @BeforeEach
        void setUpSearchData() {
            // Clear existing students to avoid duplicates
            studentRepository.deleteAll();
            
                        // Create students with different GPAs for search testing
            // Create 5 students with regular GPA to total 6 students for pagination test
            for (int i = 1; i <= 5; i++) {
                String uniqueId = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8);
                Student student = Student.builder()
                        .firstName("Student" + i)
                        .lastName("Test")
                        .emailId("student" + i + "_" + uniqueId + "@test.com")
                        .studentIdNumber("STU00" + i + "_" + uniqueId)
                        .admissionDate(LocalDate.now())
                        .studentStatus(Student.StudentStatus.ACTIVE)
                        .gpa(new BigDecimal("3.00"))
                        .isActive(true)
                        .build();
                studentService.createStudent(student);
            }

            // Create one student with higher GPA
            String uniqueId = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8);
            Student highGpaStudent = Student.builder()
                    .firstName("Alice")
                    .lastName("Johnson")
                    .emailId("alice_" + uniqueId + "@test.com")
                    .studentIdNumber("STU003_" + uniqueId)
                    .admissionDate(LocalDate.now())
                    .studentStatus(Student.StudentStatus.ACTIVE)
                    .gpa(new BigDecimal("3.80"))
                    .isActive(true)
                    .build();
            studentService.createStudent(highGpaStudent);
        }        @Test
        @DisplayName("GET /api/students/search - Should search students by GPA")
        void shouldSearchStudentsByGpa() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/students/search")
                            .param("minGpa", "3.5")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].firstName", is("Alice")));
        }

        @Test
        @DisplayName("GET /api/students/active - Should retrieve active students with pagination")
        void shouldRetrieveActiveStudentsWithPagination() throws Exception {
            // When & Then - First page
            mockMvc.perform(get("/api/students/active")
                            .param("page", "0")
                            .param("size", "3")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content", hasSize(3)))
                    .andExpect(jsonPath("$.totalElements", is(6)))
                    .andExpect(jsonPath("$.totalPages", is(2)));

            // Second page
            mockMvc.perform(get("/api/students/active")
                            .param("page", "1")
                            .param("size", "3")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content", hasSize(3)))
                    .andExpect(jsonPath("$.totalElements", is(6)))
                    .andExpect(jsonPath("$.totalPages", is(2)));
        }
    }

    @Nested
    @DisplayName("Student Enrollment API Tests")
    class StudentEnrollmentApiTests {

        @Test
        @DisplayName("POST /api/students/{id}/enroll - Should enroll student in course")
        void shouldEnrollStudentInCourse() throws Exception {
            // Given
            Student createdStudent = studentService.createStudent(testStudent);

            // When & Then
            mockMvc.perform(post("/api/students/{id}/enroll", createdStudent.getStudentId())
                            .param("courseId", testCourse.getCourseId().toString())
                            .param("semester", "FALL")
                            .param("academicYear", "2025")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.studentId", is(createdStudent.getStudentId().intValue())))
                    .andExpect(jsonPath("$.courseId", is(testCourse.getCourseId().intValue())));
        }
    }

    @Nested
    @DisplayName("Student Batch Operations API Tests")
    class StudentBatchOperationsApiTests {

        @Test
        @DisplayName("POST /api/students/batch - Should create multiple students")
        void shouldCreateMultipleStudents() throws Exception {
            // Given
            Course course2 = Course.builder()
                    .courseCode("CS102")
                    .title("Data Structures")
                    .description("Advanced programming concepts")
                    .creditHours(new BigDecimal("4.0"))
                    .courseLevel(Course.CourseLevel.INTERMEDIATE)
                    .department(testDepartment)
                    .isActive(true)
                    .build();
            courseRepository.save(course2);

            List<Student> students = Arrays.asList(
                    Student.builder()
                            .firstName("Student1")
                            .lastName("Test")
                            .emailId("student1@test.com")
                            .studentIdNumber("STU001")
                            .admissionDate(LocalDate.now())
                            .studentStatus(Student.StudentStatus.ACTIVE)
                            .gpa(new BigDecimal("3.25"))
                            .isActive(true)
                            .build(),
                    Student.builder()
                            .firstName("Student2")
                            .lastName("Test")
                            .emailId("student2@test.com")
                            .studentIdNumber("STU002")
                            .admissionDate(LocalDate.now())
                            .studentStatus(Student.StudentStatus.ACTIVE)
                            .gpa(new BigDecimal("3.50"))
                            .isActive(true)
                            .build()
            );

            // When & Then
            mockMvc.perform(post("/api/students/batch")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(students)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].firstName", is("Student1")))
                    .andExpect(jsonPath("$[1].firstName", is("Student2")));
        }
    }

    @Nested
    @DisplayName("Student Error Handling API Tests")
    class StudentErrorHandlingApiTests {

        @Test
        @DisplayName("GET /api/students/{id} - Should handle non-existent student")
        void shouldHandleNonExistentStudent() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/students/{id}", 999L)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("POST /api/students/{id}/enroll - Should handle enrollment errors")
        void shouldHandleEnrollmentErrors() throws Exception {
            // Given
            Student createdStudent = studentService.createStudent(testStudent);

            // When & Then - Invalid course ID
            mockMvc.perform(post("/api/students/{id}/enroll", createdStudent.getStudentId())
                            .param("courseId", "999")
                            .param("semester", "FALL")
                            .param("academicYear", "2025")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }
}