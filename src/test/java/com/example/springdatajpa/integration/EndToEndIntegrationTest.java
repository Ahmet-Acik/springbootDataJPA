package com.example.springdatajpa.integration;

import com.example.springdatajpa.entity.Student;
import com.example.springdatajpa.entity.Course;
import com.example.springdatajpa.entity.Department;
import com.example.springdatajpa.entity.Enrollment;
import com.example.springdatajpa.repository.StudentRepository;
import com.example.springdatajpa.repository.CourseRepository;
import com.example.springdatajpa.repository.DepartmentRepository;
import com.example.springdatajpa.repository.EnrollmentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-End Integration Tests
 * Tests complete workflows from HTTP request to database persistence
 * covering the entire application stack
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("End-to-End Integration Tests")
class EndToEndIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Clean up all data
        enrollmentRepository.deleteAll();
        studentRepository.deleteAll();
        courseRepository.deleteAll();
        departmentRepository.deleteAll();
    }

    @Nested
    @DisplayName("Complete Student Journey E2E Tests")
    class CompleteStudentJourneyTests {

        @Test
        @DisplayName("Complete Student Lifecycle - Create, Enroll, Grade, Statistics")
        void completeStudentLifecycle() throws Exception {
            // Step 1: Create Department via API
            Department department = Department.builder()
                    .departmentName("Computer Science")
                    .departmentCode("CS")
                    .departmentAddress("Building A")
                    .headOfDepartment("Dr. Smith")
                    .departmentType(Department.DepartmentType.ENGINEERING)
                    .isActive(true)
                    .build();

            MvcResult departmentResult = mockMvc.perform(post("/api/departments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(department)))
                    .andExpect(status().isCreated())
                    .andReturn();

            Department createdDepartment = objectMapper.readValue(
                    departmentResult.getResponse().getContentAsString(), Department.class);

            // Step 2: Create Course via API
            Course course = Course.builder()
                    .title("Introduction to Programming")
                    .courseCode("CS101")
                    .description("Basic programming concepts")
                    .creditHours(new BigDecimal("3.0"))
                    .courseLevel(Course.CourseLevel.BEGINNER)
                    .isActive(true)
                    .department(createdDepartment)
                    .build();

            MvcResult courseResult = mockMvc.perform(post("/api/courses")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(course)))
                    .andExpect(status().isCreated())
                    .andReturn();

            Course createdCourse = objectMapper.readValue(
                    courseResult.getResponse().getContentAsString(), Course.class);

            // Step 3: Create Student via API
            Student student = Student.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .emailId("john.doe@university.edu")
                    .studentIdNumber("STU2024001")
                    .admissionDate(LocalDate.now())
                    .dateOfBirth(LocalDate.of(2000, 5, 15))
                    .studentStatus(Student.StudentStatus.ACTIVE)
                    .gpa(new BigDecimal("0.00"))
                    .isActive(true)
                    .build();

            MvcResult studentResult = mockMvc.perform(post("/api/students")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(student)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.firstName").value("John"))
                    .andExpect(jsonPath("$.lastName").value("Doe"))
                    .andExpect(jsonPath("$.emailId").value("john.doe@university.edu"))
                    .andReturn();

            Student createdStudent = objectMapper.readValue(
                    studentResult.getResponse().getContentAsString(), Student.class);

            // Step 4: Enroll Student in Course via API
            mockMvc.perform(post("/api/students/{id}/enroll", createdStudent.getStudentId())
                            .param("courseId", createdCourse.getCourseId().toString())
                            .param("semester", "Fall 2024")
                            .param("academicYear", "2024")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("enrolled successfully")));

            // Step 5: Verify Enrollment in Database
            List<Enrollment> enrollments = enrollmentRepository.findAll();
            assertEquals(1, enrollments.size());
            Enrollment enrollment = enrollments.get(0);
            assertEquals(createdStudent.getStudentId(), enrollment.getStudent().getStudentId());
            assertEquals(createdCourse.getCourseId(), enrollment.getCourse().getCourseId());
            assertEquals("Fall 2024", enrollment.getSemester());
            assertEquals(2024, enrollment.getAcademicYear());

            // Step 6: Update Grade via API
            mockMvc.perform(put("/api/students/enrollment/{id}/grade", enrollment.getEnrollmentId())
                            .param("grade", "A")
                            .param("gradePoints", "4.0")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("Grade updated")));

            // Step 7: Verify Grade Update and GPA Calculation
            Enrollment updatedEnrollment = enrollmentRepository.findById(enrollment.getEnrollmentId()).orElse(null);
            assertNotNull(updatedEnrollment);
            assertEquals("A", updatedEnrollment.getGrade());
            assertEquals(new BigDecimal("4.0"), updatedEnrollment.getGradePoints());

            Student updatedStudent = studentRepository.findById(createdStudent.getStudentId()).orElse(null);
            assertNotNull(updatedStudent);
            assertNotNull(updatedStudent.getGpa());
            assertTrue(updatedStudent.getGpa().compareTo(BigDecimal.ZERO) > 0);

            // Step 8: Check Student Statistics via API
            mockMvc.perform(get("/api/students/stats")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalStudents").value(1))
                    .andExpect(jsonPath("$.activeStudents").value(1))
                    .andExpect(jsonPath("$.inactiveStudents").value(0))
                    .andExpect(jsonPath("$.enrollmentStatistics").exists());

            // Step 9: Search for Student via API
            mockMvc.perform(get("/api/students/search")
                            .param("firstName", "John")
                            .param("status", "ACTIVE")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].firstName").value("John"))
                    .andExpect(jsonPath("$[0].lastName").value("Doe"));

            // Step 10: Verify All Data Persisted Correctly
            List<Student> allStudents = studentRepository.findAll();
            List<Course> allCourses = courseRepository.findAll();
            List<Department> allDepartments = departmentRepository.findAll();
            List<Enrollment> allEnrollments = enrollmentRepository.findAll();

            assertEquals(1, allStudents.size());
            assertEquals(1, allCourses.size());
            assertEquals(1, allDepartments.size());
            assertEquals(1, allEnrollments.size());

            // Verify relationships
            Student persistedStudent = allStudents.get(0);
            Course persistedCourse = allCourses.get(0);
            Department persistedDepartment = allDepartments.get(0);
            Enrollment persistedEnrollment = allEnrollments.get(0);

            assertEquals(persistedDepartment.getDepartmentId(), persistedCourse.getDepartment().getDepartmentId());
            assertEquals(persistedStudent.getStudentId(), persistedEnrollment.getStudent().getStudentId());
            assertEquals(persistedCourse.getCourseId(), persistedEnrollment.getCourse().getCourseId());
        }

        @Test
        @DisplayName("Batch Student Operations E2E Test")
        void batchStudentOperationsE2E() throws Exception {
            // Step 1: Create Department and Course first
            Department department = Department.builder()
                    .departmentName("Mathematics")
                    .departmentCode("MATH")
                    .departmentAddress("Building B")
                    .headOfDepartment("Dr. Johnson")
                    .departmentType(Department.DepartmentType.SCIENCE)
                    .isActive(true)
                    .build();
            department = departmentRepository.save(department);

            Course course1 = Course.builder()
                    .title("Calculus I")
                    .courseCode("MATH101")
                    .description("Introduction to calculus")
                    .creditHours(new BigDecimal("4.0"))
                    .courseLevel(Course.CourseLevel.BEGINNER)
                    .isActive(true)
                    .department(department)
                    .build();
            course1 = courseRepository.save(course1);

            Course course2 = Course.builder()
                    .title("Linear Algebra")
                    .courseCode("MATH201")
                    .description("Introduction to linear algebra")
                    .creditHours(new BigDecimal("3.0"))
                    .courseLevel(Course.CourseLevel.INTERMEDIATE)
                    .isActive(true)
                    .department(department)
                    .build();
            course2 = courseRepository.save(course2);

            // Step 2: Create Multiple Students via Batch API
            List<Student> students = List.of(
                    Student.builder()
                            .firstName("Alice")
                            .lastName("Johnson")
                            .emailId("alice.johnson@university.edu")
                            .studentIdNumber("STU2024002")
                            .admissionDate(LocalDate.now())
                            .dateOfBirth(LocalDate.of(1999, 8, 20))
                            .studentStatus(Student.StudentStatus.ACTIVE)
                            .gpa(new BigDecimal("0.00"))
                            .isActive(true)
                            .build(),
                    Student.builder()
                            .firstName("Bob")
                            .lastName("Wilson")
                            .emailId("bob.wilson@university.edu")
                            .studentIdNumber("STU2024003")
                            .admissionDate(LocalDate.now())
                            .dateOfBirth(LocalDate.of(2001, 3, 10))
                            .studentStatus(Student.StudentStatus.ACTIVE)
                            .gpa(new BigDecimal("0.00"))
                            .isActive(true)
                            .build()
            );

            MvcResult batchResult = mockMvc.perform(post("/api/students/batch")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(students)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").value(org.hamcrest.Matchers.hasSize(2)))
                    .andReturn();

            List<Student> createdStudents = objectMapper.readValue(
                    batchResult.getResponse().getContentAsString(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Student.class));

            // Step 3: Enroll First Student in Multiple Courses
            List<Long> courseIds = List.of(course1.getCourseId(), course2.getCourseId());

            mockMvc.perform(post("/api/students/{id}/enroll-multiple", createdStudents.get(0).getStudentId())
                            .param("semester", "Fall 2024")
                            .param("academicYear", "2024")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(courseIds)))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("2 courses successfully")));

            // Step 4: Verify Multiple Enrollments
            List<Enrollment> enrollments = enrollmentRepository.findAll();
            assertEquals(2, enrollments.size());

            // Step 5: Update Grades for Both Enrollments
            for (Enrollment enrollment : enrollments) {
                mockMvc.perform(put("/api/students/enrollment/{id}/grade", enrollment.getEnrollmentId())
                                .param("grade", "B+")
                                .param("gradePoints", "3.3")
                                .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk());
            }

            // Step 6: Verify Final Statistics
            mockMvc.perform(get("/api/students/stats")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalStudents").value(2))
                    .andExpect(jsonPath("$.activeStudents").value(2))
                    .andExpect(jsonPath("$.inactiveStudents").value(0));

            // Step 7: Verify Enrollment Statistics
            mockMvc.perform(get("/api/students/enrollment-stats")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());

            // Step 8: Final Database Verification
            List<Student> finalStudents = studentRepository.findAll();
            List<Enrollment> finalEnrollments = enrollmentRepository.findAll();

            assertEquals(2, finalStudents.size());
            assertEquals(2, finalEnrollments.size());

            // Verify that first student has updated GPA from multiple courses
            Student firstStudent = finalStudents.stream()
                    .filter(s -> s.getStudentId().equals(createdStudents.get(0).getStudentId()))
                    .findFirst()
                    .orElse(null);
            assertNotNull(firstStudent);
            assertNotNull(firstStudent.getGpa());
            assertTrue(firstStudent.getGpa().compareTo(BigDecimal.ZERO) > 0);
        }
    }

    @Nested
    @DisplayName("Error Handling E2E Tests")
    class ErrorHandlingE2ETests {

        @Test
        @DisplayName("Should handle duplicate student creation gracefully")
        void shouldHandleDuplicateStudentCreation() throws Exception {
            // Step 1: Create first student
            Student student = Student.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .emailId("john.doe@university.edu")
                    .studentIdNumber("STU2024004")
                    .admissionDate(LocalDate.now())
                    .studentStatus(Student.StudentStatus.ACTIVE)
                    .gpa(new BigDecimal("3.50"))
                    .isActive(true)
                    .build();

            mockMvc.perform(post("/api/students")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(student)))
                    .andExpect(status().isCreated());

            // Step 2: Attempt to create duplicate student
            Student duplicateStudent = Student.builder()
                    .firstName("Jane")
                    .lastName("Smith")
                    .emailId("john.doe@university.edu") // Same email
                    .studentIdNumber("STU2024005")
                    .admissionDate(LocalDate.now())
                    .studentStatus(Student.StudentStatus.ACTIVE)
                    .gpa(new BigDecimal("3.75"))
                    .isActive(true)
                    .build();

            mockMvc.perform(post("/api/students")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(duplicateStudent)))
                    .andExpect(status().isBadRequest());

            // Step 3: Verify only one student exists
            List<Student> allStudents = studentRepository.findAll();
            assertEquals(1, allStudents.size());
            assertEquals("John", allStudents.get(0).getFirstName());
        }

        @Test
        @DisplayName("Should handle enrollment in non-existent course")
        void shouldHandleEnrollmentInNonExistentCourse() throws Exception {
            // Step 1: Create student
            Student student = Student.builder()
                    .firstName("Alice")
                    .lastName("Johnson")
                    .emailId("alice.johnson@university.edu")
                    .studentIdNumber("STU2024006")
                    .admissionDate(LocalDate.now())
                    .studentStatus(Student.StudentStatus.ACTIVE)
                    .gpa(new BigDecimal("3.50"))
                    .isActive(true)
                    .build();

            MvcResult studentResult = mockMvc.perform(post("/api/students")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(student)))
                    .andExpect(status().isCreated())
                    .andReturn();

            Student createdStudent = objectMapper.readValue(
                    studentResult.getResponse().getContentAsString(), Student.class);

            // Step 2: Attempt to enroll in non-existent course
            mockMvc.perform(post("/api/students/{id}/enroll", createdStudent.getStudentId())
                            .param("courseId", "999")
                            .param("semester", "Fall 2024")
                            .param("academicYear", "2024")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("failed")));

            // Step 3: Verify no enrollments created
            List<Enrollment> enrollments = enrollmentRepository.findAll();
            assertEquals(0, enrollments.size());
        }
    }
}