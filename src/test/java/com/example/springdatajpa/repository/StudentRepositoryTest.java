package com.example.springdatajpa.repository;

import com.example.springdatajpa.entity.Guardian;
import com.example.springdatajpa.entity.Student;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.apache.commons.lang3.RandomStringUtils.*;

@SpringBootTest
@ActiveProfiles("test")
class StudentRepositoryTest {
    @Autowired
    private StudentRepository studentRepository;

    String randomEmail = randomAlphanumeric(8) + "@gmail.com";
    String randomFirstName = randomAlphabetic(5);
    String randomLastName = randomAlphabetic(5);
    String randomMobileNumber = "0" + randomNumeric(9);

    Guardian guardian;
    Student student;

    @Test
    public void saveStudent() {
        student = Student.builder()
                .emailId(randomEmail)
                .firstName(randomFirstName)
                .lastName(randomLastName)
                .guardian(guardian)
                .build();
        studentRepository.save(student);
    }

    public void saveStudentWithGuardian() {

        guardian = Guardian.builder().email(randomEmail)
                .name(randomFirstName)
                .mobile(randomMobileNumber)
                .build();
        student = Student.builder()
                .emailId(randomEmail)
                .firstName(randomFirstName)
                .lastName(randomLastName)
                .guardian(guardian)
                .build();
        studentRepository.save(student);
    }


    @Test
    public void findAllStudent() {
        List<Student> students = studentRepository.findAll();
        students.forEach(System.out::println);
    }


    @Test
    public void findStudentByFirstName() {
        List<Student> students = studentRepository.findByFirstName("b");
        students.forEach(System.out::println);
    }


}