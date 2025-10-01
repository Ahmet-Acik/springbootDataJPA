package com.example.springdatajpa.config;

import com.example.springdatajpa.entity.Department;
import com.example.springdatajpa.entity.Course;
import com.example.springdatajpa.entity.Student;
import com.example.springdatajpa.entity.Guardian;
import com.example.springdatajpa.entity.Enrollment;
import com.example.springdatajpa.repository.DepartmentRepository;
import com.example.springdatajpa.repository.CourseRepository;
import com.example.springdatajpa.repository.StudentRepository;
import com.example.springdatajpa.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("mysql") // Only run when mysql profile is active
public class DatabaseInitializer implements CommandLineRunner {

    private final DepartmentRepository departmentRepository;
    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Starting database initialization...");

        // Check if data already exists
        if (departmentRepository.count() > 0) {
            log.info("Database already contains data. Skipping initialization.");
            return;
        }

        try {
            initializeDepartments();
            initializeCourses();
            initializeStudents();
            initializeEnrollments();
            log.info("Database initialization completed successfully!");
        } catch (Exception e) {
            log.error("Error during database initialization: ", e);
            throw e;
        }
    }

    private void initializeDepartments() {
        log.info("Initializing departments...");

        List<Department> departments = List.of(
            Department.builder()
                .departmentName("Computer Science")
                .departmentAddress("123 Tech Building")
                .departmentCode("CS")
                .headOfDepartment("Dr. John Smith")
                .departmentType(Department.DepartmentType.ENGINEERING)
                .isActive(true)
                .build(),
            
            Department.builder()
                .departmentName("Mathematics")
                .departmentAddress("456 Science Hall")
                .departmentCode("MATH")
                .headOfDepartment("Dr. Sarah Johnson")
                .departmentType(Department.DepartmentType.SCIENCE)
                .isActive(true)
                .build(),
            
            Department.builder()
                .departmentName("English Literature")
                .departmentAddress("789 Arts Building")
                .departmentCode("ENG")
                .headOfDepartment("Dr. Michael Brown")
                .departmentType(Department.DepartmentType.ARTS)
                .isActive(true)
                .build(),
            
            Department.builder()
                .departmentName("Business Administration")
                .departmentAddress("321 Business Center")
                .departmentCode("BUS")
                .headOfDepartment("Dr. Emily Davis")
                .departmentType(Department.DepartmentType.COMMERCE)
                .isActive(true)
                .build(),
            
            Department.builder()
                .departmentName("Physics")
                .departmentAddress("654 Physics Lab")
                .departmentCode("PHY")
                .headOfDepartment("Dr. Robert Wilson")
                .departmentType(Department.DepartmentType.SCIENCE)
                .isActive(true)
                .build()
        );

        departmentRepository.saveAll(departments);
        log.info("Saved {} departments", departments.size());
    }

    private void initializeCourses() {
        log.info("Initializing courses...");

        // Get departments
        Department csDept = departmentRepository.findByDepartmentCode("CS").orElseThrow();
        Department mathDept = departmentRepository.findByDepartmentCode("MATH").orElseThrow();
        Department engDept = departmentRepository.findByDepartmentCode("ENG").orElseThrow();
        Department busDept = departmentRepository.findByDepartmentCode("BUS").orElseThrow();
        Department phyDept = departmentRepository.findByDepartmentCode("PHY").orElseThrow();

        List<Course> courses = List.of(
            // Computer Science Courses
            Course.builder()
                .title("Introduction to Programming")
                .courseCode("CS101")
                .description("Basic programming concepts using Java")
                .creditHours(BigDecimal.valueOf(3.0))
                .courseLevel(Course.CourseLevel.BEGINNER)
                .department(csDept)
                .isActive(true)
                .build(),
            
            Course.builder()
                .title("Data Structures and Algorithms")
                .courseCode("CS201")
                .description("Study of fundamental data structures and algorithms")
                .creditHours(BigDecimal.valueOf(4.0))
                .courseLevel(Course.CourseLevel.INTERMEDIATE)
                .department(csDept)
                .isActive(true)
                .build(),
            
            Course.builder()
                .title("Database Management Systems")
                .courseCode("CS301")
                .description("Design and implementation of database systems")
                .creditHours(BigDecimal.valueOf(3.0))
                .courseLevel(Course.CourseLevel.INTERMEDIATE)
                .department(csDept)
                .isActive(true)
                .build(),
            
            // Mathematics Courses
            Course.builder()
                .title("Calculus I")
                .courseCode("MATH101")
                .description("Differential and integral calculus")
                .creditHours(BigDecimal.valueOf(4.0))
                .courseLevel(Course.CourseLevel.BEGINNER)
                .department(mathDept)
                .isActive(true)
                .build(),
            
            Course.builder()
                .title("Linear Algebra")
                .courseCode("MATH201")
                .description("Vector spaces and linear transformations")
                .creditHours(BigDecimal.valueOf(3.0))
                .courseLevel(Course.CourseLevel.INTERMEDIATE)
                .department(mathDept)
                .isActive(true)
                .build(),
            
            // English Literature Courses
            Course.builder()
                .title("English Composition")
                .courseCode("ENG101")
                .description("Writing and communication skills")
                .creditHours(BigDecimal.valueOf(3.0))
                .courseLevel(Course.CourseLevel.BEGINNER)
                .department(engDept)
                .isActive(true)
                .build(),
            
            // Business Courses
            Course.builder()
                .title("Principles of Management")
                .courseCode("BUS101")
                .description("Fundamental management concepts")
                .creditHours(BigDecimal.valueOf(3.0))
                .courseLevel(Course.CourseLevel.BEGINNER)
                .department(busDept)
                .isActive(true)
                .build(),
            
            // Physics Courses
            Course.builder()
                .title("General Physics I")
                .courseCode("PHY101")
                .description("Mechanics and thermodynamics")
                .creditHours(BigDecimal.valueOf(4.0))
                .courseLevel(Course.CourseLevel.BEGINNER)
                .department(phyDept)
                .isActive(true)
                .build()
        );

        courseRepository.saveAll(courses);
        log.info("Saved {} courses", courses.size());
    }

    private void initializeStudents() {
        log.info("Initializing students...");

        List<Student> students = List.of(
            Student.builder()
                .firstName("Alice")
                .lastName("Johnson")
                .emailId("alice.johnson@student.edu")
                .studentIdNumber("STU001")
                .admissionDate(LocalDate.of(2023, 9, 1))
                .dateOfBirth(LocalDate.of(2002, 5, 15))
                .studentStatus(Student.StudentStatus.ACTIVE)
                .gpa(BigDecimal.valueOf(3.75))
                .guardian(Guardian.builder()
                    .name("Robert Johnson")
                    .email("robert.johnson@email.com")
                    .mobile("+1-555-0101")
                    .build())
                .isActive(true)
                .build(),
            
            Student.builder()
                .firstName("Bob")
                .lastName("Smith")
                .emailId("bob.smith@student.edu")
                .studentIdNumber("STU002")
                .admissionDate(LocalDate.of(2023, 9, 1))
                .dateOfBirth(LocalDate.of(2002, 3, 22))
                .studentStatus(Student.StudentStatus.ACTIVE)
                .gpa(BigDecimal.valueOf(3.25))
                .guardian(Guardian.builder()
                    .name("Mary Smith")
                    .email("mary.smith@email.com")
                    .mobile("+1-555-0102")
                    .build())
                .isActive(true)
                .build(),
            
            Student.builder()
                .firstName("Carol")
                .lastName("Davis")
                .emailId("carol.davis@student.edu")
                .studentIdNumber("STU003")
                .admissionDate(LocalDate.of(2023, 9, 1))
                .dateOfBirth(LocalDate.of(2002, 7, 8))
                .studentStatus(Student.StudentStatus.ACTIVE)
                .gpa(BigDecimal.valueOf(3.90))
                .guardian(Guardian.builder()
                    .name("James Davis")
                    .email("james.davis@email.com")
                    .mobile("+1-555-0103")
                    .build())
                .isActive(true)
                .build(),
            
            Student.builder()
                .firstName("David")
                .lastName("Wilson")
                .emailId("david.wilson@student.edu")
                .studentIdNumber("STU004")
                .admissionDate(LocalDate.of(2022, 9, 1))
                .dateOfBirth(LocalDate.of(2001, 12, 3))
                .studentStatus(Student.StudentStatus.ACTIVE)
                .gpa(BigDecimal.valueOf(3.50))
                .guardian(Guardian.builder()
                    .name("Linda Wilson")
                    .email("linda.wilson@email.com")
                    .mobile("+1-555-0104")
                    .build())
                .isActive(true)
                .build(),
            
            Student.builder()
                .firstName("Emma")
                .lastName("Brown")
                .emailId("emma.brown@student.edu")
                .studentIdNumber("STU005")
                .admissionDate(LocalDate.of(2022, 9, 1))
                .dateOfBirth(LocalDate.of(2001, 9, 18))
                .studentStatus(Student.StudentStatus.ACTIVE)
                .gpa(BigDecimal.valueOf(3.85))
                .guardian(Guardian.builder()
                    .name("Michael Brown")
                    .email("michael.brown@email.com")
                    .mobile("+1-555-0105")
                    .build())
                .isActive(true)
                .build()
        );

        studentRepository.saveAll(students);
        log.info("Saved {} students", students.size());
    }

    private void initializeEnrollments() {
        log.info("Initializing enrollments...");

        // Get some students and courses for enrollment
        List<Student> students = studentRepository.findAll();
        List<Course> courses = courseRepository.findAll();

        if (students.size() >= 3 && courses.size() >= 3) {
            List<Enrollment> enrollments = List.of(
                Enrollment.builder()
                    .student(students.get(0))
                    .course(courses.get(0)) // CS101
                    .enrollmentDate(LocalDate.of(2023, 8, 15))
                    .semester("Fall 2023")
                    .academicYear(2023)
                    .enrollmentStatus(Enrollment.EnrollmentStatus.COMPLETED)
                    .grade("A")
                    .gradePoints(BigDecimal.valueOf(4.0))
                    .attendancePercentage(BigDecimal.valueOf(95.5))
                    .build(),
                
                Enrollment.builder()
                    .student(students.get(0))
                    .course(courses.get(3)) // MATH101
                    .enrollmentDate(LocalDate.of(2023, 8, 15))
                    .semester("Fall 2023")
                    .academicYear(2023)
                    .enrollmentStatus(Enrollment.EnrollmentStatus.COMPLETED)
                    .grade("B+")
                    .gradePoints(BigDecimal.valueOf(3.3))
                    .attendancePercentage(BigDecimal.valueOf(88.2))
                    .build(),
                
                Enrollment.builder()
                    .student(students.get(1))
                    .course(courses.get(0)) // CS101
                    .enrollmentDate(LocalDate.of(2023, 8, 15))
                    .semester("Fall 2023")
                    .academicYear(2023)
                    .enrollmentStatus(Enrollment.EnrollmentStatus.COMPLETED)
                    .grade("B")
                    .gradePoints(BigDecimal.valueOf(3.0))
                    .attendancePercentage(BigDecimal.valueOf(82.5))
                    .build(),
                
                Enrollment.builder()
                    .student(students.get(2))
                    .course(courses.get(1)) // CS201
                    .enrollmentDate(LocalDate.of(2024, 8, 15))
                    .semester("Fall 2024")
                    .academicYear(2024)
                    .enrollmentStatus(Enrollment.EnrollmentStatus.ACTIVE)
                    .attendancePercentage(BigDecimal.valueOf(96.8))
                    .build(),
                
                Enrollment.builder()
                    .student(students.get(2))
                    .course(courses.get(4)) // MATH201
                    .enrollmentDate(LocalDate.of(2024, 8, 15))
                    .semester("Fall 2024")
                    .academicYear(2024)
                    .enrollmentStatus(Enrollment.EnrollmentStatus.ACTIVE)
                    .attendancePercentage(BigDecimal.valueOf(93.2))
                    .build()
            );

            enrollmentRepository.saveAll(enrollments);
            log.info("Saved {} enrollments", enrollments.size());
        }
    }
}