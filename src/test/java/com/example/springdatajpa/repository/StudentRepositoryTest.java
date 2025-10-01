package com.example.springdatajpa.repository;

import com.example.springdatajpa.entity.Guardian;
import com.example.springdatajpa.entity.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.apache.commons.lang3.RandomStringUtils.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Student Repository Tests")
class StudentRepositoryTest {
    
    @Autowired
    private StudentRepository studentRepository;

    private String randomEmail;
    private String randomFirstName;
    private String randomLastName;
    private String randomMobileNumber;
    private Guardian guardian;
    private Student student;

    @BeforeEach
    void setUp() {
        randomEmail = randomAlphanumeric(8) + "@test.com";
        randomFirstName = randomAlphabetic(5);
        randomLastName = randomAlphabetic(5);
        randomMobileNumber = "0" + randomNumeric(9);
    }

    @Test
    @DisplayName("Save Student Without Guardian")
    public void saveStudent() {
        student = Student.builder()
                .emailId(randomEmail)
                .firstName(randomFirstName)
                .lastName(randomLastName)
                .admissionDate(LocalDate.now())
                .dateOfBirth(LocalDate.of(2000, 1, 1))
                .studentStatus(Student.StudentStatus.ACTIVE)
                .gpa(new BigDecimal("3.50"))
                .build();
        
        Student savedStudent = studentRepository.save(student);
        
        assertNotNull(savedStudent);
        assertNotNull(savedStudent.getStudentId());
        assertEquals(randomEmail, savedStudent.getEmailId());
        assertEquals(randomFirstName, savedStudent.getFirstName());
        assertEquals(randomLastName, savedStudent.getLastName());
    }

    @Test
    @DisplayName("Save Student With Guardian")
    public void saveStudentWithGuardian() {
        guardian = Guardian.builder()
                .email(randomEmail)
                .name(randomFirstName + " Guardian")
                .mobile(randomMobileNumber)
                .build();
                
        student = Student.builder()
                .emailId(randomEmail)
                .firstName(randomFirstName)
                .lastName(randomLastName)
                .guardian(guardian)
                .admissionDate(LocalDate.now())
                .dateOfBirth(LocalDate.of(1999, 5, 15))
                .studentStatus(Student.StudentStatus.ACTIVE)
                .gpa(new BigDecimal("3.75"))
                .build();
                
        Student savedStudent = studentRepository.save(student);
        
        assertNotNull(savedStudent);
        assertNotNull(savedStudent.getGuardian());
        assertEquals(guardian.getName(), savedStudent.getGuardian().getName());
        assertEquals(guardian.getEmail(), savedStudent.getGuardian().getEmail());
        assertEquals(guardian.getMobile(), savedStudent.getGuardian().getMobile());
    }

    @Test
    @DisplayName("Find All Students")
    public void findAllStudents() {
        List<Student> students = studentRepository.findAll();
        assertNotNull(students);
        assertTrue(students.size() > 0);
        
        System.out.println("Total students: " + students.size());
        students.forEach(s -> System.out.println(s.getFirstName() + " " + s.getLastName()));
    }

    @Test
    @DisplayName("Find Student By First Name")
    public void findStudentByFirstName() {
        List<Student> students = studentRepository.findByFirstName("Alice");
        assertNotNull(students);
        
        students.forEach(s -> {
            assertEquals("Alice", s.getFirstName());
            System.out.println("Found student: " + s.getFirstName() + " " + s.getLastName());
        });
    }

    @Test
    @DisplayName("Find Student By First Name Containing")
    public void findStudentByFirstNameContaining() {
        List<Student> students = studentRepository.findByFirstNameContainingIgnoreCase("al");
        assertNotNull(students);
        
        students.forEach(s -> {
            assertTrue(s.getFirstName().toLowerCase().contains("al"));
            System.out.println("Found student: " + s.getFirstName() + " " + s.getLastName());
        });
    }

    @Test
    @DisplayName("Find Active Students")
    public void findActiveStudents() {
        List<Student> students = studentRepository.findByIsActiveTrue();
        assertNotNull(students);
        
        students.forEach(s -> {
            assertTrue(s.getIsActive());
            System.out.println("Active student: " + s.getFirstName() + " " + s.getLastName());
        });
    }

    @Test
    @DisplayName("Find Student By Email Address")
    public void findStudentByEmailAddress() {
        List<Student> students = studentRepository.findByEmailId("alice.johnson@email.com");
        if (!students.isEmpty()) {
            assertEquals("alice.johnson@email.com", students.get(0).getEmailId());
            System.out.println("Found student by email: " + students.get(0).getFirstName() + " " + students.get(0).getLastName());
        }
    }

    @Test
    @DisplayName("Find Students By Guardian Name")
    public void findStudentsByGuardianName() {
        List<Student> students = studentRepository.findByGuardianName("John Guardian");
        assertNotNull(students);
        
        students.forEach(s -> {
            assertNotNull(s.getGuardian());
            System.out.println("Student with guardian: " + s.getFirstName() + " - Guardian: " + s.getGuardian().getName());
        });
    }

    @Test
    @DisplayName("Find Students By Guardian Email - JPQL")
    public void findStudentsByGuardianEmail() {
        List<Student> students = studentRepository.findByGuardianEmail("john.guardian@email.com");
        assertNotNull(students);
        
        students.forEach(s -> {
            assertNotNull(s.getGuardian());
            System.out.println("Student with guardian email: " + s.getFirstName() + " - Guardian: " + s.getGuardian().getName());
        });
    }

    @Test
    @DisplayName("Find Students By Admission Year - JPQL")
    public void findStudentsByAdmissionYear() {
        List<Student> students = studentRepository.findByAdmissionYear(2024);
        assertNotNull(students);
        
        students.forEach(s -> {
            assertTrue(s.getAdmissionDate().getYear() == 2024);
            System.out.println("Student admitted in 2024: " + s.getFirstName() + " " + s.getLastName());
        });
    }

    @Test
    @DisplayName("Find Students with Pagination")
    public void findStudentsWithPagination() {
        PageRequest pageRequest = PageRequest.of(0, 5, Sort.by("firstName"));
        Page<Student> studentPage = studentRepository.findAll(pageRequest);
        
        assertNotNull(studentPage);
        assertTrue(studentPage.getContent().size() <= 5);
        
        System.out.println("Total pages: " + studentPage.getTotalPages());
        System.out.println("Total elements: " + studentPage.getTotalElements());
        System.out.println("Current page students:");
        studentPage.getContent().forEach(s -> 
            System.out.println(s.getFirstName() + " " + s.getLastName())
        );
    }

    @Test
    @DisplayName("Find Students By Status")
    public void findStudentsByStatus() {
        List<Student> activeStudents = studentRepository.findByStudentStatus(Student.StudentStatus.ACTIVE);
        assertNotNull(activeStudents);
        
        activeStudents.forEach(s -> {
            assertEquals(Student.StudentStatus.ACTIVE, s.getStudentStatus());
            System.out.println("Active student: " + s.getFirstName() + " " + s.getLastName());
        });
    }

    @Test
    @DisplayName("Find Students By GPA Greater Than")
    public void findStudentsByGpaGreaterThan() {
        List<Student> highGpaStudents = studentRepository.findByGpaGreaterThan(new BigDecimal("3.5"));
        assertNotNull(highGpaStudents);
        
        highGpaStudents.forEach(s -> {
            assertTrue(s.getGpa().compareTo(new BigDecimal("3.5")) > 0);
            System.out.println("High GPA student: " + s.getFirstName() + " " + s.getLastName() + " - GPA: " + s.getGpa());
        });
    }

    @Test
    @DisplayName("Find Students By Admission Date Between")
    public void findStudentsByAdmissionDateBetween() {
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);
        
        List<Student> students = studentRepository.findByAdmissionDateBetween(startDate, endDate);
        assertNotNull(students);
        
        students.forEach(s -> {
            assertTrue(s.getAdmissionDate().isAfter(startDate.minusDays(1)));
            assertTrue(s.getAdmissionDate().isBefore(endDate.plusDays(1)));
            System.out.println("Student admitted in 2023-2024: " + s.getFirstName() + " " + s.getLastName() + 
                             " - Admission: " + s.getAdmissionDate());
        });
    }
}