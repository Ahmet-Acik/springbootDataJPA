package com.example.springdatajpa.service;

import com.example.springdatajpa.entity.Student;
import com.example.springdatajpa.entity.Course;
import com.example.springdatajpa.entity.Department;
import com.example.springdatajpa.entity.Enrollment;
import com.example.springdatajpa.service.StudentService;
import com.example.springdatajpa.service.CourseService;
import com.example.springdatajpa.service.DepartmentService;
import com.example.springdatajpa.repository.StudentRepository;
import com.example.springdatajpa.repository.CourseRepository;
import com.example.springdatajpa.repository.DepartmentRepository;
import com.example.springdatajpa.repository.EnrollmentRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.apache.commons.lang3.RandomStringUtils.*;

/**
 * Integration tests for StudentService
 * Tests the service layer integration with repository layer including transaction management
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Student Service Integration Tests")
class StudentServiceIntegrationTest {

    @Autowired
    private StudentService studentService;
    
    @Autowired
    private CourseService courseService;
    
    @Autowired
    private DepartmentService departmentService;
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private EnrollmentRepository enrollmentRepository;

    private Student testStudent;
    private Course testCourse;
    private Department testDepartment;

    @BeforeEach
    void setUp() {
        // Clean up before each test
        enrollmentRepository.deleteAll();
        studentRepository.deleteAll();
        courseRepository.deleteAll();
        departmentRepository.deleteAll();
        
        // Create test department
        testDepartment = Department.builder()
                .departmentName("Computer Science")
                .departmentCode("CS")
                .departmentAddress("Building A")
                .headOfDepartment("Dr. Smith")
                .departmentType(Department.DepartmentType.ENGINEERING)
                .isActive(true)
                .build();
        testDepartment = departmentRepository.save(testDepartment);

        // Create test course
        testCourse = Course.builder()
                .title("Introduction to Programming")
                .courseCode("CS101")
                .description("Basic programming concepts")
                .creditHours(new BigDecimal("3.0"))
                .courseLevel(Course.CourseLevel.BEGINNER)
                .isActive(true)
                .department(testDepartment)
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
    @DisplayName("Student CRUD Operations")
    class StudentCRUDOperations {

        @Test
        @DisplayName("Should create student successfully")
        void shouldCreateStudentSuccessfully() {
            // When
            Student createdStudent = studentService.createStudent(testStudent);

            // Then
            assertNotNull(createdStudent);
            assertNotNull(createdStudent.getStudentId());
            assertEquals(testStudent.getFirstName(), createdStudent.getFirstName());
            assertEquals(testStudent.getLastName(), createdStudent.getLastName());
            assertEquals(testStudent.getEmailId(), createdStudent.getEmailId());
            assertTrue(createdStudent.getIsActive());
        }

        @Test
        @DisplayName("Should not create student with duplicate email")
        void shouldNotCreateStudentWithDuplicateEmail() {
            // Given
            studentService.createStudent(testStudent);

            // When & Then
            Student duplicateStudent = Student.builder()
                    .firstName("Jane")
                    .lastName("Smith")
                    .emailId(testStudent.getEmailId()) // Same email
                    .studentIdNumber("STU002")
                    .admissionDate(LocalDate.now())
                    .dateOfBirth(LocalDate.of(2001, 1, 1))
                    .studentStatus(Student.StudentStatus.ACTIVE)
                    .gpa(new BigDecimal("3.75"))
                    .isActive(true)
                    .build();

            assertThrows(IllegalArgumentException.class, () -> {
                studentService.createStudent(duplicateStudent);
            });
        }

        @Test
        @DisplayName("Should retrieve student by ID")
        void shouldRetrieveStudentById() {
            // Given
            Student createdStudent = studentService.createStudent(testStudent);

            // When
            Optional<Student> foundStudent = studentService.getStudentById(createdStudent.getStudentId());

            // Then
            assertTrue(foundStudent.isPresent());
            assertEquals(createdStudent.getStudentId(), foundStudent.get().getStudentId());
            assertEquals(createdStudent.getEmailId(), foundStudent.get().getEmailId());
        }

        @Test
        @DisplayName("Should update student successfully")
        void shouldUpdateStudentSuccessfully() {
            // Given
            Student createdStudent = studentService.createStudent(testStudent);
            String newFirstName = "Jane";
            String newLastName = "Smith";

            // When
            createdStudent.setFirstName(newFirstName);
            createdStudent.setLastName(newLastName);
            Student updatedStudent = studentService.updateStudent(createdStudent.getStudentId(), createdStudent);

            // Then
            assertNotNull(updatedStudent);
            assertEquals(newFirstName, updatedStudent.getFirstName());
            assertEquals(newLastName, updatedStudent.getLastName());
            assertEquals(createdStudent.getEmailId(), updatedStudent.getEmailId());
        }

        @Test
        @DisplayName("Should soft delete student")
        void shouldSoftDeleteStudent() {
            // Given
            Student createdStudent = studentService.createStudent(testStudent);

            // When
            studentService.deleteStudent(createdStudent.getStudentId());

            // Then
            Optional<Student> foundStudent = studentService.getStudentById(createdStudent.getStudentId());
            assertTrue(foundStudent.isPresent());
            assertFalse(foundStudent.get().getIsActive());
        }
    }

    @Nested
    @DisplayName("Student Search Operations")
    class StudentSearchOperations {

        @Test
        @DisplayName("Should search students by criteria")
        void shouldSearchStudentsByCriteria() {
            // Given
            Student student1 = Student.builder()
                    .firstName("Alice")
                    .lastName("Johnson")
                    .emailId("alice@test.com")
                    .studentIdNumber("STU003")
                    .admissionDate(LocalDate.now())
                    .studentStatus(Student.StudentStatus.ACTIVE)
                    .gpa(new BigDecimal("3.80"))
                    .isActive(true)
                    .build();

            Student student2 = Student.builder()
                    .firstName("Bob")
                    .lastName("Williams")
                    .emailId("bob@test.com")
                    .studentIdNumber("STU004")
                    .admissionDate(LocalDate.now())
                    .studentStatus(Student.StudentStatus.INACTIVE)
                    .gpa(new BigDecimal("2.50"))
                    .isActive(true)
                    .build();

            studentService.createStudent(student1);
            studentService.createStudent(student2);

            // When
            List<Student> activeStudents = studentService.searchStudents(
                    null, null, null, Student.StudentStatus.ACTIVE, null, null);

            List<Student> highGpaStudents = studentService.searchStudents(
                    null, null, null, null, new BigDecimal("3.0"), null);

            // Then
            assertEquals(1, activeStudents.size());
            assertEquals("Alice", activeStudents.get(0).getFirstName());

            assertEquals(1, highGpaStudents.size());
            assertEquals("Alice", highGpaStudents.get(0).getFirstName());
        }

        @Test
        @DisplayName("Should get active students with pagination")
        void shouldGetActiveStudentsWithPagination() {
            // Given
            for (int i = 0; i < 5; i++) {
                Student student = Student.builder()
                        .firstName("Student" + i)
                        .lastName("Test")
                        .emailId("student" + i + "@test.com")
                        .studentIdNumber("STU00" + i)
                        .admissionDate(LocalDate.now())
                        .studentStatus(Student.StudentStatus.ACTIVE)
                        .gpa(new BigDecimal("3.00"))
                        .isActive(true)
                        .build();
                studentService.createStudent(student);
            }

            // When
            Page<Student> firstPage = studentService.getActiveStudents(PageRequest.of(0, 3));
            Page<Student> secondPage = studentService.getActiveStudents(PageRequest.of(1, 3));

            // Then
            assertEquals(3, firstPage.getContent().size());
            assertEquals(2, secondPage.getContent().size());
            assertEquals(5, firstPage.getTotalElements());
            assertTrue(firstPage.hasNext());
            assertFalse(secondPage.hasNext());
        }
    }

    @Nested
    @DisplayName("Student Enrollment Operations")
    class StudentEnrollmentOperations {

        @Test
        @DisplayName("Should enroll student in course successfully")
        void shouldEnrollStudentInCourseSuccessfully() {
            // Given
            Student createdStudent = studentService.createStudent(testStudent);
            String semester = "Fall 2024";
            Integer academicYear = 2024;

            // When
            Enrollment enrollment = studentService.enrollStudentInCourse(
                    createdStudent.getStudentId(), 
                    testCourse.getCourseId(), 
                    semester, 
                    academicYear);

            // Then
            assertNotNull(enrollment);
            assertEquals(createdStudent.getStudentId(), enrollment.getStudent().getStudentId());
            assertEquals(testCourse.getCourseId(), enrollment.getCourse().getCourseId());
            assertEquals(semester, enrollment.getSemester());
            assertEquals(academicYear, enrollment.getAcademicYear());
            assertEquals(Enrollment.EnrollmentStatus.ACTIVE, enrollment.getEnrollmentStatus());
        }

        @Test
        @DisplayName("Should not allow duplicate enrollment")
        void shouldNotAllowDuplicateEnrollment() {
            // Given
            Student createdStudent = studentService.createStudent(testStudent);
            String semester = "Fall 2024";
            Integer academicYear = 2024;

            // First enrollment
            studentService.enrollStudentInCourse(
                    createdStudent.getStudentId(), 
                    testCourse.getCourseId(), 
                    semester, 
                    academicYear);

            // When & Then - Attempt duplicate enrollment
            assertThrows(RuntimeException.class, () -> {
                studentService.enrollStudentInCourse(
                        createdStudent.getStudentId(), 
                        testCourse.getCourseId(), 
                        semester, 
                        academicYear);
            });
        }

        @Test
        @DisplayName("Should enroll student in multiple courses")
        void shouldEnrollStudentInMultipleCourses() {
            // Given
            Student createdStudent = studentService.createStudent(testStudent);
            
            Course course2 = Course.builder()
                    .title("Data Structures")
                    .courseCode("CS102")
                    .description("Advanced data structures")
                    .creditHours(new BigDecimal("4.0"))
                    .courseLevel(Course.CourseLevel.INTERMEDIATE)
                    .isActive(true)
                    .department(testDepartment)
                    .build();
            course2 = courseRepository.save(course2);

            List<Long> courseIds = List.of(testCourse.getCourseId(), course2.getCourseId());
            String semester = "Fall 2024";
            Integer academicYear = 2024;

            // When
            studentService.enrollStudentInMultipleCourses(
                    createdStudent.getStudentId(), 
                    courseIds, 
                    semester, 
                    academicYear);

            // Then
            List<Enrollment> enrollments = enrollmentRepository.findAll();
            assertEquals(2, enrollments.size());
            
            List<Long> enrolledCourseIds = enrollments.stream()
                    .map(e -> e.getCourse().getCourseId())
                    .toList();
            assertTrue(enrolledCourseIds.containsAll(courseIds));
        }
    }

    @Nested
    @DisplayName("Student Grade Operations")
    class StudentGradeOperations {

        @Test
        @DisplayName("Should update grade and recalculate GPA")
        void shouldUpdateGradeAndRecalculateGPA() {
            // Given
            Student createdStudent = studentService.createStudent(testStudent);
            Enrollment enrollment = studentService.enrollStudentInCourse(
                    createdStudent.getStudentId(), 
                    testCourse.getCourseId(), 
                    "Fall 2024", 
                    2024);

            String grade = "A";
            BigDecimal gradePoints = new BigDecimal("4.0");

            // When
            studentService.updateGradeAndCalculateGPA(enrollment.getEnrollmentId(), grade, gradePoints);

            // Then
            Enrollment updatedEnrollment = enrollmentRepository.findById(enrollment.getEnrollmentId()).orElse(null);
            assertNotNull(updatedEnrollment);
            assertEquals(grade, updatedEnrollment.getGrade());
            assertEquals(gradePoints, updatedEnrollment.getGradePoints());

            // Verify GPA was recalculated
            Student updatedStudent = studentRepository.findById(createdStudent.getStudentId()).orElse(null);
            assertNotNull(updatedStudent);
            assertNotNull(updatedStudent.getGpa());
        }
    }

    @Nested
    @DisplayName("Batch Operations")
    class BatchOperations {

        @Test
        @DisplayName("Should create multiple students in batch")
        void shouldCreateMultipleStudentsInBatch() {
            // Given
            List<Student> students = List.of(
                    Student.builder()
                            .firstName("Alice")
                            .lastName("Johnson")
                            .emailId("alice@test.com")
                            .studentIdNumber("STU005")
                            .admissionDate(LocalDate.now())
                            .studentStatus(Student.StudentStatus.ACTIVE)
                            .gpa(new BigDecimal("3.80"))
                            .isActive(true)
                            .build(),
                    Student.builder()
                            .firstName("Bob")
                            .lastName("Wilson")
                            .emailId("bob@test.com")
                            .studentIdNumber("STU006")
                            .admissionDate(LocalDate.now())
                            .studentStatus(Student.StudentStatus.ACTIVE)
                            .gpa(new BigDecimal("3.60"))
                            .isActive(true)
                            .build()
            );

            // When
            List<Student> createdStudents = studentService.createStudentsBatch(students);

            // Then
            assertEquals(2, createdStudents.size());
            assertNotNull(createdStudents.get(0).getStudentId());
            assertNotNull(createdStudents.get(1).getStudentId());
            
            List<Student> allStudents = studentService.getAllStudents();
            assertEquals(2, allStudents.size());
        }

        @Test
        @DisplayName("Should rollback batch creation on error")
        void shouldRollbackBatchCreationOnError() {
            // Given - List with one valid and one invalid student (duplicate email)
            studentService.createStudent(testStudent); // Create first student
            
            List<Student> students = List.of(
                    Student.builder()
                            .firstName("Alice")
                            .lastName("Johnson")
                            .emailId("alice@test.com")
                            .studentIdNumber("STU007")
                            .admissionDate(LocalDate.now())
                            .studentStatus(Student.StudentStatus.ACTIVE)
                            .gpa(new BigDecimal("3.80"))
                            .isActive(true)
                            .build(),
                    Student.builder()
                            .firstName("Bob")
                            .lastName("Wilson")
                            .emailId(testStudent.getEmailId()) // Duplicate email
                            .studentIdNumber("STU008")
                            .admissionDate(LocalDate.now())
                            .studentStatus(Student.StudentStatus.ACTIVE)
                            .gpa(new BigDecimal("3.60"))
                            .isActive(true)
                            .build()
            );

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                studentService.createStudentsBatch(students);
            });

            // Verify no students from the batch were created (rollback occurred)
            List<Student> allStudents = studentService.getAllStudents();
            assertEquals(1, allStudents.size()); // Only the initial test student
            assertEquals(testStudent.getEmailId(), allStudents.get(0).getEmailId());
        }
    }
}