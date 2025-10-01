package com.example.springdatajpa.repository;

import com.example.springdatajpa.entity.Course;
import com.example.springdatajpa.entity.Department;
import com.example.springdatajpa.entity.Enrollment;
import com.example.springdatajpa.entity.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;



import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.apache.commons.lang3.RandomStringUtils.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Enrollment Repository Tests")
class EnrollmentRepositoryTest {
    
    @Autowired
    private EnrollmentRepository enrollmentRepository;
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    


    private Student testStudent1;
    private Student testStudent2;
    private Course testCourse1;
    private Course testCourse2;
    private Department testDepartment;
    private Enrollment testEnrollment;

    @BeforeEach
    void setUp() {
        // Clean up before each test
        enrollmentRepository.deleteAll();
        courseRepository.deleteAll();
        studentRepository.deleteAll();
        departmentRepository.deleteAll();
        
        // Create test department with unique code to avoid constraint violations
        String uniqueDeptCode = "CS" + System.currentTimeMillis() % 10000;
        testDepartment = Department.builder()
                .departmentName("Computer Science Department")
                .departmentCode(uniqueDeptCode)
                .departmentType(Department.DepartmentType.ENGINEERING)
                .isActive(true)
                .build();
        testDepartment = departmentRepository.save(testDepartment);
        
        // Create test students
        testStudent1 = Student.builder()
                .firstName("John")
                .lastName("Doe")
                .emailId("john.doe@" + randomAlphabetic(5).toLowerCase() + ".com")
                .studentIdNumber("STU" + randomNumeric(6))
                .gpa(new BigDecimal("3.5"))
                .admissionDate(LocalDate.now().minusYears(2))
                .studentStatus(Student.StudentStatus.ACTIVE)
                .isActive(true)
                .build();
        testStudent1 = studentRepository.save(testStudent1);
        
        testStudent2 = Student.builder()
                .firstName("Jane")
                .lastName("Smith")
                .emailId("jane.smith@" + randomAlphabetic(5).toLowerCase() + ".com")
                .studentIdNumber("STU" + randomNumeric(6))
                .gpa(new BigDecimal("3.8"))
                .admissionDate(LocalDate.now().minusYears(1))
                .studentStatus(Student.StudentStatus.ACTIVE)
                .isActive(true)
                .build();
        testStudent2 = studentRepository.save(testStudent2);
        
        // Create test courses with unique codes
        String uniqueCourseCode1 = "CS101_" + System.currentTimeMillis() % 10000;
        String uniqueCourseCode2 = "CS201_" + System.currentTimeMillis() % 10000;
        
        testCourse1 = Course.builder()
                .courseCode(uniqueCourseCode1)
                .title("Introduction to Computer Science")
                .creditHours(new BigDecimal("3.0"))
                .courseLevel(Course.CourseLevel.BEGINNER)
                .department(testDepartment)
                .isActive(true)
                .build();
        testCourse1 = courseRepository.save(testCourse1);
        
        testCourse2 = Course.builder()
                .courseCode(uniqueCourseCode2)
                .title("Data Structures")
                .creditHours(new BigDecimal("4.0"))
                .courseLevel(Course.CourseLevel.INTERMEDIATE)
                .department(testDepartment)
                .isActive(true)
                .build();
        testCourse2 = courseRepository.save(testCourse2);
        
        // Create test enrollment
        testEnrollment = Enrollment.builder()
                .student(testStudent1)
                .course(testCourse1)
                .enrollmentDate(LocalDate.now().minusMonths(3))
                .semester("Fall 2024")
                .academicYear(2024)
                .enrollmentStatus(Enrollment.EnrollmentStatus.ACTIVE)
                .attendancePercentage(new BigDecimal("85.5"))
                .build();
    }

    @Test
    @DisplayName("Save Enrollment Successfully")
    void saveEnrollment() {
        // When
        Enrollment savedEnrollment = enrollmentRepository.save(testEnrollment);
        
        // Then
        assertNotNull(savedEnrollment);
        assertNotNull(savedEnrollment.getEnrollmentId());
        assertEquals(testStudent1.getStudentId(), savedEnrollment.getStudent().getStudentId());
        assertEquals(testCourse1.getCourseId(), savedEnrollment.getCourse().getCourseId());
        assertEquals("Fall 2024", savedEnrollment.getSemester());
        assertEquals(2024, savedEnrollment.getAcademicYear());
        assertEquals(Enrollment.EnrollmentStatus.ACTIVE, savedEnrollment.getEnrollmentStatus());
        assertNotNull(savedEnrollment.getCreatedDate());
    }

    @Test
    @DisplayName("Find Enrollments By Student")
    void findByStudent() {
        // Given
        enrollmentRepository.save(testEnrollment);
        
        Enrollment anotherEnrollment = Enrollment.builder()
                .student(testStudent1)
                .course(testCourse2)
                .enrollmentDate(LocalDate.now().minusMonths(2))
                .semester("Fall 2024")
                .academicYear(2024)
                .enrollmentStatus(Enrollment.EnrollmentStatus.ACTIVE)
                .build();
        enrollmentRepository.save(anotherEnrollment);
        
        // When
        List<Enrollment> enrollments = enrollmentRepository.findByStudent(testStudent1);
        
        // Then
        assertEquals(2, enrollments.size());
        assertTrue(enrollments.stream().allMatch(e -> e.getStudent().getStudentId().equals(testStudent1.getStudentId())));
    }

    @Test
    @DisplayName("Find Enrollments By Course")
    void findByCourse() {
        // Given
        enrollmentRepository.save(testEnrollment);
        
        Enrollment anotherEnrollment = Enrollment.builder()
                .student(testStudent2)
                .course(testCourse1)
                .enrollmentDate(LocalDate.now().minusMonths(2))
                .semester("Fall 2024")
                .academicYear(2024)
                .enrollmentStatus(Enrollment.EnrollmentStatus.ACTIVE)
                .build();
        enrollmentRepository.save(anotherEnrollment);
        
        // When
        List<Enrollment> enrollments = enrollmentRepository.findByCourse(testCourse1);
        
        // Then
        assertEquals(2, enrollments.size());
        assertTrue(enrollments.stream().allMatch(e -> e.getCourse().getCourseId().equals(testCourse1.getCourseId())));
    }

    @Test
    @DisplayName("Find Enrollments By Student ID")
    void findByStudentStudentId() {
        // Given
        enrollmentRepository.save(testEnrollment);
        
        // When
        List<Enrollment> enrollments = enrollmentRepository.findByStudentStudentId(testStudent1.getStudentId());
        
        // Then
        assertEquals(1, enrollments.size());
        assertEquals(testStudent1.getStudentId(), enrollments.get(0).getStudent().getStudentId());
    }

    @Test
    @DisplayName("Find Enrollments By Course ID")
    void findByCourseCourseId() {
        // Given
        enrollmentRepository.save(testEnrollment);
        
        // When
        List<Enrollment> enrollments = enrollmentRepository.findByCourseCourseId(testCourse1.getCourseId());
        
        // Then
        assertEquals(1, enrollments.size());
        assertEquals(testCourse1.getCourseId(), enrollments.get(0).getCourse().getCourseId());
    }

    @Test
    @DisplayName("Find Enrollments By Status")
    void findByEnrollmentStatus() {
        // Given
        enrollmentRepository.save(testEnrollment);
        
        Enrollment completedEnrollment = Enrollment.builder()
                .student(testStudent2)
                .course(testCourse2)
                .enrollmentDate(LocalDate.now().minusMonths(6))
                .semester("Spring 2024")
                .academicYear(2024)
                .enrollmentStatus(Enrollment.EnrollmentStatus.COMPLETED)
                .grade("A")
                .gradePoints(new BigDecimal("4.0"))
                .build();
        enrollmentRepository.save(completedEnrollment);
        
        // When
        List<Enrollment> activeEnrollments = enrollmentRepository.findByEnrollmentStatus(Enrollment.EnrollmentStatus.ACTIVE);
        List<Enrollment> completedEnrollments = enrollmentRepository.findByEnrollmentStatus(Enrollment.EnrollmentStatus.COMPLETED);
        
        // Then
        assertEquals(1, activeEnrollments.size());
        assertEquals(Enrollment.EnrollmentStatus.ACTIVE, activeEnrollments.get(0).getEnrollmentStatus());
        
        assertEquals(1, completedEnrollments.size());
        assertEquals(Enrollment.EnrollmentStatus.COMPLETED, completedEnrollments.get(0).getEnrollmentStatus());
    }

    @Test
    @DisplayName("Find Enrollments By Academic Year")
    void findByAcademicYear() {
        // Given
        enrollmentRepository.save(testEnrollment);
        
        Enrollment nextYearEnrollment = Enrollment.builder()
                .student(testStudent2)
                .course(testCourse2)
                .enrollmentDate(LocalDate.now())
                .semester("Spring 2025")
                .academicYear(2025)
                .enrollmentStatus(Enrollment.EnrollmentStatus.ACTIVE)
                .build();
        enrollmentRepository.save(nextYearEnrollment);
        
        // When
        List<Enrollment> enrollments2024 = enrollmentRepository.findByAcademicYear(2024);
        List<Enrollment> enrollments2025 = enrollmentRepository.findByAcademicYear(2025);
        
        // Then
        assertEquals(1, enrollments2024.size());
        assertEquals(2024, enrollments2024.get(0).getAcademicYear());
        
        assertEquals(1, enrollments2025.size());
        assertEquals(2025, enrollments2025.get(0).getAcademicYear());
    }

    @Test
    @DisplayName("Find Enrollments By Semester")
    void findBySemester() {
        // Given
        enrollmentRepository.save(testEnrollment);
        
        Enrollment springEnrollment = Enrollment.builder()
                .student(testStudent2)
                .course(testCourse2)
                .enrollmentDate(LocalDate.now())
                .semester("Spring 2024")
                .academicYear(2024)
                .enrollmentStatus(Enrollment.EnrollmentStatus.ACTIVE)
                .build();
        enrollmentRepository.save(springEnrollment);
        
        // When
        List<Enrollment> fallEnrollments = enrollmentRepository.findBySemester("Fall 2024");
        List<Enrollment> springEnrollments = enrollmentRepository.findBySemester("Spring 2024");
        
        // Then
        assertEquals(1, fallEnrollments.size());
        assertEquals("Fall 2024", fallEnrollments.get(0).getSemester());
        
        assertEquals(1, springEnrollments.size());
        assertEquals("Spring 2024", springEnrollments.get(0).getSemester());
    }

    @Test
    @DisplayName("Find Enrollments Between Dates")
    void findByEnrollmentDateBetween() {
        // Given
        enrollmentRepository.save(testEnrollment);
        
        Enrollment recentEnrollment = Enrollment.builder()
                .student(testStudent2)
                .course(testCourse2)
                .enrollmentDate(LocalDate.now().minusDays(10))
                .semester("Fall 2024")
                .academicYear(2024)
                .enrollmentStatus(Enrollment.EnrollmentStatus.ACTIVE)
                .build();
        enrollmentRepository.save(recentEnrollment);
        
        // When
        LocalDate startDate = LocalDate.now().minusMonths(1);
        LocalDate endDate = LocalDate.now();
        List<Enrollment> recentEnrollments = enrollmentRepository.findByEnrollmentDateBetween(startDate, endDate);
        
        // Then
        assertEquals(1, recentEnrollments.size());
        assertTrue(recentEnrollments.get(0).getEnrollmentDate().isAfter(startDate.minusDays(1)));
        assertTrue(recentEnrollments.get(0).getEnrollmentDate().isBefore(endDate.plusDays(1)));
    }

    @Test
    @DisplayName("Find Unique Enrollment By Student, Course, and Semester")
    void findByStudentAndCourseAndSemester() {
        // Given
        enrollmentRepository.save(testEnrollment);
        
        // When
        Optional<Enrollment> found = enrollmentRepository.findByStudentAndCourseAndSemester(
                testStudent1, testCourse1, "Fall 2024");
        Optional<Enrollment> notFound = enrollmentRepository.findByStudentAndCourseAndSemester(
                testStudent1, testCourse1, "Spring 2024");
        
        // Then
        assertTrue(found.isPresent());
        assertEquals(testStudent1.getStudentId(), found.get().getStudent().getStudentId());
        assertEquals(testCourse1.getCourseId(), found.get().getCourse().getCourseId());
        assertEquals("Fall 2024", found.get().getSemester());
        
        assertFalse(notFound.isPresent());
    }

    @Test
    @DisplayName("Find Enrollments With Grades")
    void findByGradeIsNotNull() {
        // Given
        testEnrollment.setGrade("B+");
        testEnrollment.setGradePoints(new BigDecimal("3.3"));
        enrollmentRepository.save(testEnrollment);
        
        Enrollment noGradeEnrollment = Enrollment.builder()
                .student(testStudent2)
                .course(testCourse2)
                .enrollmentDate(LocalDate.now())
                .semester("Fall 2024")
                .academicYear(2024)
                .enrollmentStatus(Enrollment.EnrollmentStatus.ACTIVE)
                .build();
        enrollmentRepository.save(noGradeEnrollment);
        
        // When
        List<Enrollment> gradedEnrollments = enrollmentRepository.findByGradeIsNotNull();
        
        // Then
        assertEquals(1, gradedEnrollments.size());
        assertNotNull(gradedEnrollments.get(0).getGrade());
        assertEquals("B+", gradedEnrollments.get(0).getGrade());
    }

    @Test
    @DisplayName("Find Enrollments With High Grade Points")
    void findByGradePointsGreaterThan() {
        // Given
        testEnrollment.setGrade("A");
        testEnrollment.setGradePoints(new BigDecimal("4.0"));
        enrollmentRepository.save(testEnrollment);
        
        Enrollment lowGradeEnrollment = Enrollment.builder()
                .student(testStudent2)
                .course(testCourse2)
                .enrollmentDate(LocalDate.now())
                .semester("Fall 2024")
                .academicYear(2024)
                .enrollmentStatus(Enrollment.EnrollmentStatus.COMPLETED)
                .grade("C")
                .gradePoints(new BigDecimal("2.0"))
                .build();
        enrollmentRepository.save(lowGradeEnrollment);
        
        // When
        List<Enrollment> highGradeEnrollments = enrollmentRepository.findByGradePointsGreaterThan(new BigDecimal("3.0"));
        
        // Then
        assertEquals(1, highGradeEnrollments.size());
        assertTrue(highGradeEnrollments.get(0).getGradePoints().compareTo(new BigDecimal("3.0")) > 0);
    }

    @Test
    @DisplayName("Find Student Enrollments With Pagination")
    void findByStudentStudentIdAndEnrollmentStatusWithPagination() {
        // Given
        enrollmentRepository.save(testEnrollment);
        
        for (int i = 2; i <= 5; i++) {
            Course course = Course.builder()
                    .courseCode("CS" + (100 + i) + "_" + (System.currentTimeMillis() % 1000))
                    .title("Course " + i)
                    .creditHours(new BigDecimal("3.0"))
                    .courseLevel(Course.CourseLevel.INTERMEDIATE)
                    .department(testDepartment)
                    .isActive(true)
                    .build();
            course = courseRepository.save(course);
            
            Enrollment enrollment = Enrollment.builder()
                    .student(testStudent1)
                    .course(course)
                    .enrollmentDate(LocalDate.now().minusMonths(i))
                    .semester("Fall 2024")
                    .academicYear(2024)
                    .enrollmentStatus(Enrollment.EnrollmentStatus.ACTIVE)
                    .build();
            enrollmentRepository.save(enrollment);
        }
        
        // When
        PageRequest pageRequest = PageRequest.of(0, 2, Sort.by("enrollmentDate").descending());
        Page<Enrollment> enrollmentsPage = enrollmentRepository
                .findByStudentStudentIdAndEnrollmentStatus(testStudent1.getStudentId(), 
                        Enrollment.EnrollmentStatus.ACTIVE, pageRequest);
        
        // Then
        assertEquals(5, enrollmentsPage.getTotalElements()); // testEnrollment + 4 additional enrollments
        assertEquals(3, enrollmentsPage.getTotalPages()); // 5 elements with page size 2 = 3 pages
        assertEquals(2, enrollmentsPage.getContent().size());
        assertTrue(enrollmentsPage.isFirst());
        assertFalse(enrollmentsPage.isLast());
    }

    @Test
    @DisplayName("Find Student Enrollments By Year (JPQL)")
    void findStudentEnrollmentsByYear() {
        // Given
        enrollmentRepository.save(testEnrollment);
        
        Enrollment nextYearEnrollment = Enrollment.builder()
                .student(testStudent1)
                .course(testCourse2)
                .enrollmentDate(LocalDate.now())
                .semester("Spring 2025")
                .academicYear(2025)
                .enrollmentStatus(Enrollment.EnrollmentStatus.ACTIVE)
                .build();
        enrollmentRepository.save(nextYearEnrollment);
        
        // When
        List<Enrollment> enrollments2024 = enrollmentRepository
                .findStudentEnrollmentsByYear(testStudent1.getStudentId(), 2024);
        List<Enrollment> enrollments2025 = enrollmentRepository
                .findStudentEnrollmentsByYear(testStudent1.getStudentId(), 2025);
        
        // Then
        assertEquals(1, enrollments2024.size());
        assertEquals(2024, enrollments2024.get(0).getAcademicYear());
        
        assertEquals(1, enrollments2025.size());
        assertEquals(2025, enrollments2025.get(0).getAcademicYear());
    }

    @Test
    @DisplayName("Find Enrollments By Department And Semester (JPQL)")
    @Transactional
    void findEnrollmentsByDepartmentAndSemester() {
        // Given
        enrollmentRepository.save(testEnrollment);
        
        // When
        List<Enrollment> enrollments = enrollmentRepository
                .findEnrollmentsByDepartmentAndSemester(testDepartment.getDepartmentId(), "Fall 2024");
        
        // Then
        assertEquals(1, enrollments.size());
        assertEquals("Fall 2024", enrollments.get(0).getSemester());
        assertEquals(testDepartment.getDepartmentId(), 
                enrollments.get(0).getCourse().getDepartment().getDepartmentId());
    }

    @Test
    @DisplayName("Find Low Attendance Enrollments (JPQL)")
    void findLowAttendanceEnrollments() {
        // Given
        testEnrollment.setAttendancePercentage(new BigDecimal("60.0"));
        enrollmentRepository.save(testEnrollment);
        
        Enrollment goodAttendanceEnrollment = Enrollment.builder()
                .student(testStudent2)
                .course(testCourse2)
                .enrollmentDate(LocalDate.now())
                .semester("Fall 2024")
                .academicYear(2024)
                .enrollmentStatus(Enrollment.EnrollmentStatus.ACTIVE)
                .attendancePercentage(new BigDecimal("90.0"))
                .build();
        enrollmentRepository.save(goodAttendanceEnrollment);
        
        // When
        List<Enrollment> lowAttendanceEnrollments = enrollmentRepository
                .findLowAttendanceEnrollments(new BigDecimal("75.0"));
        
        // Then
        assertEquals(1, lowAttendanceEnrollments.size());
        assertTrue(lowAttendanceEnrollments.get(0).getAttendancePercentage()
                .compareTo(new BigDecimal("75.0")) < 0);
        assertEquals(Enrollment.EnrollmentStatus.ACTIVE, 
                lowAttendanceEnrollments.get(0).getEnrollmentStatus());
    }

    @Test
    @DisplayName("Calculate Student GPA (JPQL)")
    @Transactional
    void calculateStudentGPA() {
        // Given
        testEnrollment.setGrade("A");
        testEnrollment.setGradePoints(new BigDecimal("4.0"));
        testEnrollment.setEnrollmentStatus(Enrollment.EnrollmentStatus.COMPLETED);
        enrollmentRepository.save(testEnrollment);
        
        Enrollment anotherEnrollment = Enrollment.builder()
                .student(testStudent1)
                .course(testCourse2)
                .enrollmentDate(LocalDate.now().minusMonths(2))
                .semester("Fall 2024")
                .academicYear(2024)
                .enrollmentStatus(Enrollment.EnrollmentStatus.COMPLETED)
                .grade("B+")
                .gradePoints(new BigDecimal("3.3"))
                .build();
        enrollmentRepository.save(anotherEnrollment);
        
        // When
        Double gpa = enrollmentRepository.calculateStudentGPA(testStudent1.getStudentId());
        
        // Then
        assertNotNull(gpa);
        assertEquals(3.65, gpa, 0.01); // (4.0 + 3.3) / 2 = 3.65
    }

    @Test
    @DisplayName("Find Course Enrollments By Grade (JPQL)")
    void findCourseEnrollmentsByGrade() {
        // Given
        testEnrollment.setGrade("B+");
        testEnrollment.setGradePoints(new BigDecimal("3.3"));
        enrollmentRepository.save(testEnrollment);
        
        Enrollment higherGradeEnrollment = Enrollment.builder()
                .student(testStudent2)
                .course(testCourse1)
                .enrollmentDate(LocalDate.now())
                .semester("Fall 2024")
                .academicYear(2024)
                .enrollmentStatus(Enrollment.EnrollmentStatus.COMPLETED)
                .grade("A")
                .gradePoints(new BigDecimal("4.0"))
                .build();
        enrollmentRepository.save(higherGradeEnrollment);
        
        // When
        List<Enrollment> enrollmentsByGrade = enrollmentRepository
                .findCourseEnrollmentsByGrade(testCourse1.getCourseId());
        
        // Then
        assertEquals(2, enrollmentsByGrade.size());
        // Should be ordered by grade points DESC
        assertTrue(enrollmentsByGrade.get(0).getGradePoints()
                .compareTo(enrollmentsByGrade.get(1).getGradePoints()) >= 0);
        assertEquals(0, new BigDecimal("4.0").compareTo(enrollmentsByGrade.get(0).getGradePoints()));
    }

    @Test
    @DisplayName("Find Most Active Students By Year (Native SQL)")
    void findMostActiveStudentsByYear() {
        // Given
        enrollmentRepository.save(testEnrollment);
        
        // Add more enrollments for student1
        Enrollment enrollment2 = Enrollment.builder()
                .student(testStudent1)
                .course(testCourse2)
                .enrollmentDate(LocalDate.now().minusMonths(2))
                .semester("Fall 2024")
                .academicYear(2024)
                .enrollmentStatus(Enrollment.EnrollmentStatus.ACTIVE)
                .build();
        enrollmentRepository.save(enrollment2);
        
        // Add one enrollment for student2
        Enrollment enrollment3 = Enrollment.builder()
                .student(testStudent2)
                .course(testCourse1)
                .enrollmentDate(LocalDate.now().minusMonths(1))
                .semester("Fall 2024")
                .academicYear(2024)
                .enrollmentStatus(Enrollment.EnrollmentStatus.ACTIVE)
                .build();
        enrollmentRepository.save(enrollment3);
        
        // When
        List<Object[]> mostActiveStudents = enrollmentRepository
                .findMostActiveStudentsByYear(2024, 2);
        
        // Then
        assertNotNull(mostActiveStudents);
        assertEquals(2, mostActiveStudents.size());
        
        // First student should have more enrollments
        Object[] firstStudent = mostActiveStudents.get(0);
        assertEquals("John", firstStudent[0]); // first_name
        assertEquals("Doe", firstStudent[1]);  // last_name
        assertEquals(2L, ((Number) firstStudent[2]).longValue()); // total_enrollments
    }

    @Test
    @DisplayName("Get Course Statistics By Semester (Native SQL)")
    void getCourseStatisticsBySemester() {
        // Given
        testEnrollment.setGrade("A");
        testEnrollment.setGradePoints(new BigDecimal("4.0"));
        enrollmentRepository.save(testEnrollment);
        
        Enrollment enrollment2 = Enrollment.builder()
                .student(testStudent2)
                .course(testCourse1)
                .enrollmentDate(LocalDate.now())
                .semester("Fall 2024")
                .academicYear(2024)
                .enrollmentStatus(Enrollment.EnrollmentStatus.COMPLETED)
                .grade("B")
                .gradePoints(new BigDecimal("3.0"))
                .build();
        enrollmentRepository.save(enrollment2);
        
        // When
        List<Object[]> courseStats = enrollmentRepository.getCourseStatisticsBySemester("Fall 2024");
        
        // Then
        assertNotNull(courseStats);
        assertEquals(1, courseStats.size());
        
        Object[] stats = courseStats.get(0);
        assertEquals("Introduction to Computer Science", stats[0]); // title
        assertTrue(((String) stats[1]).startsWith("CS101_")); // course_code
        assertEquals(2L, ((Number) stats[2]).longValue()); // enrollment_count
        assertEquals(3.5, ((Number) stats[3]).doubleValue(), 0.1); // avg_grade
    }

    @Test
    @DisplayName("Get Student Performance Summary (Projection)")
    @Transactional
    void getStudentPerformanceSummary() {
        // Given
        testEnrollment.setGrade("A");
        testEnrollment.setGradePoints(new BigDecimal("4.0"));
        enrollmentRepository.save(testEnrollment);
        
        Enrollment enrollment2 = Enrollment.builder()
                .student(testStudent1)
                .course(testCourse2)
                .enrollmentDate(LocalDate.now())
                .semester("Fall 2024")
                .academicYear(2024)
                .enrollmentStatus(Enrollment.EnrollmentStatus.COMPLETED)
                .grade("B+")
                .gradePoints(new BigDecimal("3.3"))
                .build();
        enrollmentRepository.save(enrollment2);
        
        // When
        List<EnrollmentRepository.StudentEnrollmentSummary> summaries = 
                enrollmentRepository.getStudentPerformanceSummary(2024);
        
        // Then
        assertFalse(summaries.isEmpty());
        EnrollmentRepository.StudentEnrollmentSummary summary = summaries.get(0);
        assertEquals("John Doe", summary.getStudentName());
        assertNotNull(summary.getStudentEmail());
        assertEquals(2L, summary.getTotalEnrollments());
        assertEquals(3.65, summary.getAverageGrade(), 0.01);
    }

    @Test
    @DisplayName("Get Course Enrollment Statistics (Projection)")
    @Transactional
    void getCourseEnrollmentStatistics() {
        // Given
        testEnrollment.setGrade("A");
        testEnrollment.setGradePoints(new BigDecimal("4.0"));
        testEnrollment.setEnrollmentStatus(Enrollment.EnrollmentStatus.COMPLETED);
        enrollmentRepository.save(testEnrollment);
        
        Enrollment enrollment2 = Enrollment.builder()
                .student(testStudent2)
                .course(testCourse1)
                .enrollmentDate(LocalDate.now())
                .semester("Fall 2024")
                .academicYear(2024)
                .enrollmentStatus(Enrollment.EnrollmentStatus.COMPLETED)
                .grade("F")
                .gradePoints(new BigDecimal("1.0"))
                .build();
        enrollmentRepository.save(enrollment2);
        
        // When
        List<EnrollmentRepository.CourseEnrollmentStats> stats = 
                enrollmentRepository.getCourseEnrollmentStatistics(2024);
        
        // Then
        assertFalse(stats.isEmpty());
        EnrollmentRepository.CourseEnrollmentStats stat = stats.get(0);
        assertEquals("Introduction to Computer Science", stat.getCourseTitle());
        assertTrue(stat.getCourseCode().startsWith("CS101_"));
        assertEquals(2L, stat.getEnrollmentCount());
        assertEquals(2L, stat.getCompletedCount());
        assertEquals(2.5, stat.getAverageGrade(), 0.1);
        assertEquals(50.0, stat.getPassRate(), 0.1); // 1 out of 2 passed (>= 2.0)
    }

    @Test
    @DisplayName("Search Enrollments With Multiple Criteria")
    void searchEnrollments() {
        // Given
        enrollmentRepository.save(testEnrollment);
        
        Enrollment enrollment2 = Enrollment.builder()
                .student(testStudent2)
                .course(testCourse2)
                .enrollmentDate(LocalDate.now())
                .semester("Spring 2024")
                .academicYear(2024)
                .enrollmentStatus(Enrollment.EnrollmentStatus.COMPLETED)
                .build();
        enrollmentRepository.save(enrollment2);
        
        // When - Search by student only
        List<Enrollment> byStudent = enrollmentRepository.searchEnrollments(
                testStudent1.getStudentId(), null, null, null, null);
        
        // When - Search by course and semester
        List<Enrollment> byCourseAndSemester = enrollmentRepository.searchEnrollments(
                null, testCourse1.getCourseId(), "Fall 2024", null, null);
        
        // When - Search by status
        List<Enrollment> byStatus = enrollmentRepository.searchEnrollments(
                null, null, null, null, Enrollment.EnrollmentStatus.COMPLETED);
        
        // Then
        assertEquals(1, byStudent.size());
        assertEquals(testStudent1.getStudentId(), byStudent.get(0).getStudent().getStudentId());
        
        assertEquals(1, byCourseAndSemester.size());
        assertEquals(testCourse1.getCourseId(), byCourseAndSemester.get(0).getCourse().getCourseId());
        assertEquals("Fall 2024", byCourseAndSemester.get(0).getSemester());
        
        assertEquals(1, byStatus.size());
        assertEquals(Enrollment.EnrollmentStatus.COMPLETED, byStatus.get(0).getEnrollmentStatus());
    }

    @Test
    @DisplayName("Find By Student And Semester (Named Query)")
    void findByStudentAndSemester() {
        // Given
        enrollmentRepository.save(testEnrollment);
        
        Enrollment springEnrollment = Enrollment.builder()
                .student(testStudent1)
                .course(testCourse2)
                .enrollmentDate(LocalDate.now())
                .semester("Spring 2024")
                .academicYear(2024)
                .enrollmentStatus(Enrollment.EnrollmentStatus.ACTIVE)
                .build();
        enrollmentRepository.save(springEnrollment);
        
        // When
        List<Enrollment> fallEnrollments = enrollmentRepository
                .findByStudentAndSemester(testStudent1.getStudentId(), "Fall 2024");
        List<Enrollment> springEnrollments = enrollmentRepository
                .findByStudentAndSemester(testStudent1.getStudentId(), "Spring 2024");
        
        // Then
        assertEquals(1, fallEnrollments.size());
        assertEquals("Fall 2024", fallEnrollments.get(0).getSemester());
        
        assertEquals(1, springEnrollments.size());
        assertEquals("Spring 2024", springEnrollments.get(0).getSemester());
    }

    @Test
    @DisplayName("Find By Course And Academic Year (Named Query)")
    void findByCourseAndAcademicYear() {
        // Given
        enrollmentRepository.save(testEnrollment);
        
        Enrollment nextYearEnrollment = Enrollment.builder()
                .student(testStudent2)
                .course(testCourse1)
                .enrollmentDate(LocalDate.now())
                .semester("Fall 2025")
                .academicYear(2025)
                .enrollmentStatus(Enrollment.EnrollmentStatus.ACTIVE)
                .build();
        enrollmentRepository.save(nextYearEnrollment);
        
        // When
        List<Enrollment> enrollments2024 = enrollmentRepository
                .findByCourseAndAcademicYear(testCourse1.getCourseId(), 2024);
        List<Enrollment> enrollments2025 = enrollmentRepository
                .findByCourseAndAcademicYear(testCourse1.getCourseId(), 2025);
        
        // Then
        assertEquals(1, enrollments2024.size());
        assertEquals(2024, enrollments2024.get(0).getAcademicYear());
        
        assertEquals(1, enrollments2025.size());
        assertEquals(2025, enrollments2025.get(0).getAcademicYear());
    }

    @Test
    @DisplayName("Test Unique Constraint on Student-Course-Semester-Year")
    void testUniqueConstraint() {
        // Given
        enrollmentRepository.save(testEnrollment);
        
        Enrollment duplicateEnrollment = Enrollment.builder()
                .student(testStudent1)
                .course(testCourse1)
                .enrollmentDate(LocalDate.now())
                .semester("Fall 2024") // Same semester
                .academicYear(2024)    // Same academic year
                .enrollmentStatus(Enrollment.EnrollmentStatus.ACTIVE)
                .build();
        
        // When & Then
        assertThrows(Exception.class, () -> {
            enrollmentRepository.save(duplicateEnrollment);
            enrollmentRepository.flush(); // Force immediate database operation
        });
    }

    @Test
    @DisplayName("Test Enrollment Status Enum Values")
    void testEnrollmentStatusEnum() {
        // Given
        Enrollment activeEnrollment = testEnrollment;
        activeEnrollment.setEnrollmentStatus(Enrollment.EnrollmentStatus.ACTIVE);
        
        Enrollment completedEnrollment = Enrollment.builder()
                .student(testStudent2)
                .course(testCourse2)
                .enrollmentDate(LocalDate.now())
                .semester("Fall 2024")
                .academicYear(2024)
                .enrollmentStatus(Enrollment.EnrollmentStatus.COMPLETED)
                .build();
        
        Enrollment droppedEnrollment = Enrollment.builder()
                .student(testStudent1)
                .course(testCourse2)
                .enrollmentDate(LocalDate.now())
                .semester("Spring 2024")
                .academicYear(2024)
                .enrollmentStatus(Enrollment.EnrollmentStatus.DROPPED)
                .build();
        
        // When
        List<Enrollment> savedEnrollments = enrollmentRepository.saveAll(
                List.of(activeEnrollment, completedEnrollment, droppedEnrollment));
        
        // Then
        assertEquals(3, savedEnrollments.size());
        assertEquals(Enrollment.EnrollmentStatus.ACTIVE, savedEnrollments.get(0).getEnrollmentStatus());
        assertEquals(Enrollment.EnrollmentStatus.COMPLETED, savedEnrollments.get(1).getEnrollmentStatus());
        assertEquals(Enrollment.EnrollmentStatus.DROPPED, savedEnrollments.get(2).getEnrollmentStatus());
    }

    @Test
    @DisplayName("Repository Operations - Update Enrollment")
    void updateEnrollment() {
        // Given
        Enrollment savedEnrollment = enrollmentRepository.save(testEnrollment);
        String newGrade = "A";
        BigDecimal newGradePoints = new BigDecimal("4.0");
        
        // When
        savedEnrollment.setGrade(newGrade);
        savedEnrollment.setGradePoints(newGradePoints);
        savedEnrollment.setEnrollmentStatus(Enrollment.EnrollmentStatus.COMPLETED);
        Enrollment updatedEnrollment = enrollmentRepository.save(savedEnrollment);
        
        // Then
        assertEquals(newGrade, updatedEnrollment.getGrade());
        assertEquals(newGradePoints, updatedEnrollment.getGradePoints());
        assertEquals(Enrollment.EnrollmentStatus.COMPLETED, updatedEnrollment.getEnrollmentStatus());
        assertNotNull(updatedEnrollment.getLastModifiedDate());
    }

    @Test
    @DisplayName("Repository Operations - Delete Enrollment")
    void deleteEnrollment() {
        // Given
        Enrollment savedEnrollment = enrollmentRepository.save(testEnrollment);
        Long enrollmentId = savedEnrollment.getEnrollmentId();
        
        // When
        enrollmentRepository.delete(savedEnrollment);
        
        // Then
        Optional<Enrollment> deletedEnrollment = enrollmentRepository.findById(enrollmentId);
        assertFalse(deletedEnrollment.isPresent());
    }

    @Test
    @DisplayName("Repository Operations - Count Enrollments")
    void countEnrollments() {
        // Given
        enrollmentRepository.save(testEnrollment);
        
        Enrollment enrollment2 = Enrollment.builder()
                .student(testStudent2)
                .course(testCourse2)
                .enrollmentDate(LocalDate.now())
                .semester("Fall 2024")
                .academicYear(2024)
                .enrollmentStatus(Enrollment.EnrollmentStatus.ACTIVE)
                .build();
        enrollmentRepository.save(enrollment2);
        
        // When
        long count = enrollmentRepository.count();
        
        // Then
        assertEquals(2, count);
    }

    @Test
    @DisplayName("Test Complex Enrollment Scenario")
    @Transactional
    void testComplexEnrollmentScenario() {
        // Given - A student enrolls in multiple courses across different semesters
        List<Enrollment> enrollments = List.of(
            // Fall 2024
            Enrollment.builder()
                .student(testStudent1).course(testCourse1).enrollmentDate(LocalDate.now().minusMonths(4))
                .semester("Fall 2024").academicYear(2024).enrollmentStatus(Enrollment.EnrollmentStatus.COMPLETED)
                .grade("A").gradePoints(new BigDecimal("4.0")).attendancePercentage(new BigDecimal("95.0"))
                .build(),
            
            Enrollment.builder()
                .student(testStudent1).course(testCourse2).enrollmentDate(LocalDate.now().minusMonths(4))
                .semester("Fall 2024").academicYear(2024).enrollmentStatus(Enrollment.EnrollmentStatus.COMPLETED)
                .grade("B+").gradePoints(new BigDecimal("3.3")).attendancePercentage(new BigDecimal("88.0"))
                .build(),
            
            // Spring 2025 - Current semester
            Enrollment.builder()
                .student(testStudent1).course(testCourse1).enrollmentDate(LocalDate.now().minusMonths(1))
                .semester("Spring 2025").academicYear(2025).enrollmentStatus(Enrollment.EnrollmentStatus.ACTIVE)
                .attendancePercentage(new BigDecimal("92.0"))
                .build()
        );
        
        enrollmentRepository.saveAll(enrollments);
        
        // When & Then - Test various query scenarios
        
        // 1. Student's completed courses
        List<Enrollment> completedCourses = enrollmentRepository.findByEnrollmentStatus(Enrollment.EnrollmentStatus.COMPLETED);
        assertEquals(2, completedCourses.size());
        
        // 2. Student's GPA calculation
        Double gpa = enrollmentRepository.calculateStudentGPA(testStudent1.getStudentId());
        assertEquals(3.65, gpa, 0.01);
        
        // 3. Current active enrollments
        List<Enrollment> activeEnrollments = enrollmentRepository.findByEnrollmentStatus(Enrollment.EnrollmentStatus.ACTIVE);
        assertEquals(1, activeEnrollments.size());
        
        // 4. High attendance enrollments
        List<Enrollment> highAttendance = enrollmentRepository.findLowAttendanceEnrollments(new BigDecimal("80.0"));
        assertEquals(0, highAttendance.size()); // All have attendance > 80%
        
        // 5. Student enrollments by academic year
        List<Enrollment> enrollments2024 = enrollmentRepository.findStudentEnrollmentsByYear(testStudent1.getStudentId(), 2024);
        List<Enrollment> enrollments2025 = enrollmentRepository.findStudentEnrollmentsByYear(testStudent1.getStudentId(), 2025);
        assertEquals(2, enrollments2024.size());
        assertEquals(1, enrollments2025.size());
    }
}