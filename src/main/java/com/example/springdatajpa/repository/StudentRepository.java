package com.example.springdatajpa.repository;

import com.example.springdatajpa.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    // Derived query methods
    List<Student> findByFirstName(String firstName);
    
    List<Student> findByLastName(String lastName);
    
    List<Student> findByFirstNameAndLastName(String firstName, String lastName);
    
    List<Student> findByFirstNameContainingIgnoreCase(String firstName);
    
    List<Student> findByEmailId(String emailId);
    
    Optional<Student> findByStudentIdNumber(String studentIdNumber);
    
    List<Student> findByStudentStatus(Student.StudentStatus status);
    
    List<Student> findByIsActiveTrue();
    
    List<Student> findByAdmissionDateBetween(LocalDate startDate, LocalDate endDate);
    
    List<Student> findByGpaGreaterThan(BigDecimal gpa);
    
    List<Student> findByGpaBetween(BigDecimal minGpa, BigDecimal maxGpa);
    
    Page<Student> findByStudentStatusAndIsActive(Student.StudentStatus status, Boolean isActive, Pageable pageable);
    
    // JPQL queries
    @Query("SELECT s FROM Student s WHERE s.guardian.name = :guardianName")
    List<Student> findByGuardianName(@Param("guardianName") String guardianName);
    
    @Query("SELECT s FROM Student s WHERE s.guardian.email = :guardianEmail")
    List<Student> findByGuardianEmail(@Param("guardianEmail") String guardianEmail);
    
    @Query("SELECT s FROM Student s WHERE YEAR(s.admissionDate) = :year")
    List<Student> findByAdmissionYear(@Param("year") int year);
    
    @Query("SELECT s FROM Student s JOIN s.enrollments e WHERE e.course.courseId = :courseId")
    List<Student> findStudentsEnrolledInCourse(@Param("courseId") Long courseId);
    
    @Query("SELECT s FROM Student s WHERE SIZE(s.enrollments) > :minEnrollments")
    List<Student> findStudentsWithMinimumEnrollments(@Param("minEnrollments") int minEnrollments);
    
    @Query("SELECT AVG(s.gpa) FROM Student s WHERE s.studentStatus = :status AND s.gpa IS NOT NULL")
    Double findAverageGpaByStatus(@Param("status") Student.StudentStatus status);
    
    // Native SQL queries
    @Query(value = "SELECT s.*, COUNT(e.enrollment_id) as enrollment_count " +
           "FROM tbl_student s LEFT JOIN tbl_enrollment e ON s.student_id = e.student_id " +
           "GROUP BY s.student_id ORDER BY enrollment_count DESC LIMIT :limit",
           nativeQuery = true)
    List<Object[]> findMostActiveStudents(@Param("limit") int limit);
    
    @Query(value = "SELECT EXTRACT(YEAR FROM s.admission_date) as admission_year, COUNT(*) as student_count " +
           "FROM tbl_student s WHERE s.is_active = true " +
           "GROUP BY EXTRACT(YEAR FROM s.admission_date) ORDER BY admission_year DESC",
           nativeQuery = true)
    List<Object[]> getStudentCountByAdmissionYear();
    
    // Modifying queries
    @Modifying
    @Query("UPDATE Student s SET s.isActive = false WHERE s.studentId = :studentId")
    int deactivateStudent(@Param("studentId") Long studentId);
    
    @Modifying
    @Query("UPDATE Student s SET s.gpa = :gpa WHERE s.studentId = :studentId")
    int updateStudentGpa(@Param("studentId") Long studentId, @Param("gpa") BigDecimal gpa);
    
    // Projection interfaces
    interface StudentSummary {
        String getFirstName();
        String getLastName();
        String getEmailId();
        String getStudentIdNumber();
        BigDecimal getGpa();
    }
    
    @Query("SELECT s.firstName as firstName, s.lastName as lastName, s.emailId as emailId, " +
           "s.studentIdNumber as studentIdNumber, s.gpa as gpa " +
           "FROM Student s WHERE s.isActive = true ORDER BY s.gpa DESC")
    List<StudentSummary> getActiveStudentSummaries();
    
    interface StudentWithEnrollmentCount {
        Long getStudentId();
        String getFullName();
        String getEmailId();
        Long getEnrollmentCount();
        BigDecimal getGpa();
    }
    
    @Query("SELECT s.studentId as studentId, CONCAT(s.firstName, ' ', s.lastName) as fullName, " +
           "s.emailId as emailId, COUNT(e) as enrollmentCount, s.gpa as gpa " +
           "FROM Student s LEFT JOIN s.enrollments e " +
           "GROUP BY s ORDER BY enrollmentCount DESC")
    List<StudentWithEnrollmentCount> getStudentsWithEnrollmentCount();
    
    // Complex search method
    @Query("SELECT s FROM Student s WHERE " +
           "(:firstName IS NULL OR UPPER(s.firstName) LIKE UPPER(CONCAT('%', :firstName, '%'))) AND " +
           "(:lastName IS NULL OR UPPER(s.lastName) LIKE UPPER(CONCAT('%', :lastName, '%'))) AND " +
           "(:email IS NULL OR UPPER(s.emailId) LIKE UPPER(CONCAT('%', :email, '%'))) AND " +
           "(:status IS NULL OR s.studentStatus = :status) AND " +
           "(:minGpa IS NULL OR s.gpa >= :minGpa) AND " +
           "(:maxGpa IS NULL OR s.gpa <= :maxGpa)")
    List<Student> searchStudents(@Param("firstName") String firstName,
                               @Param("lastName") String lastName,
                               @Param("email") String email,
                               @Param("status") Student.StudentStatus status,
                               @Param("minGpa") BigDecimal minGpa,
                               @Param("maxGpa") BigDecimal maxGpa);
    
    // Using named queries
    List<Student> findByFirstNameContaining(@Param("firstName") String firstName);
    
    List<Student> findActiveStudents();
}
