package com.example.springdatajpa.service;

import com.example.springdatajpa.entity.Student;
import com.example.springdatajpa.entity.Course;
import com.example.springdatajpa.entity.Enrollment;
import com.example.springdatajpa.repository.StudentRepository;
import com.example.springdatajpa.repository.CourseRepository;
import com.example.springdatajpa.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentService {

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    // Read operations (no transaction needed)
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public Optional<Student> getStudentById(Long id) {
        return studentRepository.findById(id);
    }

    public Page<Student> getActiveStudents(Pageable pageable) {
        return studentRepository.findByStudentStatusAndIsActive(
                Student.StudentStatus.ACTIVE, true, pageable);
    }

    public List<Student> searchStudents(String firstName, String lastName, String email,
                                      Student.StudentStatus status, BigDecimal minGpa, BigDecimal maxGpa) {
        return studentRepository.searchStudents(firstName, lastName, email, status, minGpa, maxGpa);
    }

    // Write operations with transactions
    @Transactional
    public Student createStudent(Student student) {
        // Validate unique constraints
        if (studentRepository.findByEmailId(student.getEmailId()).size() > 0) {
            throw new IllegalArgumentException("Email already exists: " + student.getEmailId());
        }
        
        if (student.getStudentIdNumber() != null && 
            studentRepository.findByStudentIdNumber(student.getStudentIdNumber()).isPresent()) {
            throw new IllegalArgumentException("Student ID number already exists: " + student.getStudentIdNumber());
        }

        return studentRepository.save(student);
    }

    @Transactional
    public Student updateStudent(Long studentId, Student updatedStudent) {
        Student existingStudent = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + studentId));

        existingStudent.setFirstName(updatedStudent.getFirstName());
        existingStudent.setLastName(updatedStudent.getLastName());
        existingStudent.setEmailId(updatedStudent.getEmailId());
        existingStudent.setGuardian(updatedStudent.getGuardian());
        existingStudent.setStudentStatus(updatedStudent.getStudentStatus());

        return studentRepository.save(existingStudent);
    }

    @Transactional
    public void deleteStudent(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + studentId));
        
        // Soft delete - just mark as inactive
        student.setIsActive(false);
        studentRepository.save(student);
    }

    // Complex transactional operations
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public Enrollment enrollStudentInCourse(Long studentId, Long courseId, String semester, Integer academicYear) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        // Check if student is already enrolled in this course for the same semester
        Optional<Enrollment> existingEnrollment = enrollmentRepository
                .findByStudentAndCourseAndSemester(student, course, semester);
        
        if (existingEnrollment.isPresent()) {
            throw new IllegalStateException("Student is already enrolled in this course for the given semester");
        }

        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .course(course)
                .semester(semester)
                .academicYear(academicYear)
                .enrollmentDate(LocalDate.now())
                .enrollmentStatus(Enrollment.EnrollmentStatus.ACTIVE)
                .build();

        return enrollmentRepository.save(enrollment);
    }

    @Transactional
    public void updateGradeAndCalculateGPA(Long enrollmentId, String grade, BigDecimal gradePoints) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new IllegalArgumentException("Enrollment not found"));

        // Update enrollment grade
        enrollment.setGrade(grade);
        enrollment.setGradePoints(gradePoints);
        enrollment.setEnrollmentStatus(Enrollment.EnrollmentStatus.COMPLETED);
        enrollmentRepository.save(enrollment);

        // Recalculate student GPA
        Long studentId = enrollment.getStudent().getStudentId();
        Double newGPA = enrollmentRepository.calculateStudentGPA(studentId);
        
        if (newGPA != null) {
            studentRepository.updateStudentGpa(studentId, BigDecimal.valueOf(newGPA));
        }
    }

    // Batch operations with transaction
    @Transactional
    public List<Student> createStudentsBatch(List<Student> students) {
        // Validate all students before saving any
        for (Student student : students) {
            if (studentRepository.findByEmailId(student.getEmailId()).size() > 0) {
                throw new IllegalArgumentException("Email already exists: " + student.getEmailId());
            }
        }

        return studentRepository.saveAll(students);
    }

    // Transaction with rollback scenario
    @Transactional(rollbackFor = Exception.class)
    public void enrollStudentInMultipleCourses(Long studentId, List<Long> courseIds, 
                                             String semester, Integer academicYear) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        for (Long courseId : courseIds) {
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));

            // Check enrollment capacity or other business rules
            long currentEnrollments = enrollmentRepository.findByCourseCourseId(courseId).size();
            if (currentEnrollments >= 30) { // Assuming max 30 students per course
                throw new IllegalStateException("Course is full: " + course.getTitle());
            }

            Enrollment enrollment = Enrollment.builder()
                    .student(student)
                    .course(course)
                    .semester(semester)
                    .academicYear(academicYear)
                    .enrollmentDate(LocalDate.now())
                    .enrollmentStatus(Enrollment.EnrollmentStatus.ACTIVE)
                    .build();

            enrollmentRepository.save(enrollment);
        }
    }

    // Read-only transaction with specific isolation level
    @Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
    public List<StudentRepository.StudentWithEnrollmentCount> getStudentEnrollmentStatistics() {
        return studentRepository.getStudentsWithEnrollmentCount();
    }

    // Transaction with timeout
    @Transactional(timeout = 30) // 30 seconds timeout
    public void performBulkGradeUpdate(String semester, Integer academicYear) {
        List<Enrollment> enrollments = enrollmentRepository.findBySemester(semester);
        
        for (Enrollment enrollment : enrollments) {
            if (enrollment.getAcademicYear().equals(academicYear) && enrollment.getGrade() == null) {
                // Simulate some complex grade calculation
                BigDecimal calculatedGrade = calculateGradeBasedOnAttendance(enrollment);
                enrollment.setGradePoints(calculatedGrade);
                enrollment.setGrade(convertGradePointsToLetter(calculatedGrade));
                enrollmentRepository.save(enrollment);
            }
        }
    }

    // Helper methods
    private BigDecimal calculateGradeBasedOnAttendance(Enrollment enrollment) {
        BigDecimal attendance = enrollment.getAttendancePercentage();
        if (attendance == null) return BigDecimal.valueOf(2.0); // Default C grade
        
        if (attendance.compareTo(BigDecimal.valueOf(90)) >= 0) return BigDecimal.valueOf(4.0); // A
        if (attendance.compareTo(BigDecimal.valueOf(80)) >= 0) return BigDecimal.valueOf(3.0); // B
        if (attendance.compareTo(BigDecimal.valueOf(70)) >= 0) return BigDecimal.valueOf(2.0); // C
        if (attendance.compareTo(BigDecimal.valueOf(60)) >= 0) return BigDecimal.valueOf(1.0); // D
        return BigDecimal.valueOf(0.0); // F
    }

    private String convertGradePointsToLetter(BigDecimal gradePoints) {
        if (gradePoints.compareTo(BigDecimal.valueOf(3.5)) >= 0) return "A";
        if (gradePoints.compareTo(BigDecimal.valueOf(2.5)) >= 0) return "B";
        if (gradePoints.compareTo(BigDecimal.valueOf(1.5)) >= 0) return "C";
        if (gradePoints.compareTo(BigDecimal.valueOf(0.5)) >= 0) return "D";
        return "F";
    }
}