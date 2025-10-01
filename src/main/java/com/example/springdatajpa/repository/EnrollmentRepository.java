package com.example.springdatajpa.repository;

import com.example.springdatajpa.entity.Enrollment;
import com.example.springdatajpa.entity.Student;
import com.example.springdatajpa.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    
    // Derived query methods
    List<Enrollment> findByStudent(Student student);
    
    List<Enrollment> findByCourse(Course course);
    
    List<Enrollment> findByStudentStudentId(Long studentId);
    
    List<Enrollment> findByCourseCourseId(Long courseId);
    
    List<Enrollment> findByEnrollmentStatus(Enrollment.EnrollmentStatus status);
    
    List<Enrollment> findByAcademicYear(Integer academicYear);
    
    List<Enrollment> findBySemester(String semester);
    
    List<Enrollment> findByEnrollmentDateBetween(LocalDate startDate, LocalDate endDate);
    
    Optional<Enrollment> findByStudentAndCourseAndSemester(Student student, Course course, String semester);
    
    List<Enrollment> findByGradeIsNotNull();
    
    List<Enrollment> findByGradePointsGreaterThan(BigDecimal gradePoints);
    
    Page<Enrollment> findByStudentStudentIdAndEnrollmentStatus(
            Long studentId, Enrollment.EnrollmentStatus status, Pageable pageable);
    
    // JPQL queries
    @Query("SELECT e FROM Enrollment e WHERE e.student.studentId = :studentId AND e.academicYear = :year")
    List<Enrollment> findStudentEnrollmentsByYear(@Param("studentId") Long studentId, 
                                                 @Param("year") Integer academicYear);
    
    @Query("SELECT e FROM Enrollment e WHERE e.course.department.departmentId = :deptId AND e.semester = :semester")
    List<Enrollment> findEnrollmentsByDepartmentAndSemester(@Param("deptId") Long departmentId,
                                                           @Param("semester") String semester);
    
    @Query("SELECT e FROM Enrollment e WHERE e.attendancePercentage < :minAttendance AND e.enrollmentStatus = 'ACTIVE'")
    List<Enrollment> findLowAttendanceEnrollments(@Param("minAttendance") BigDecimal minAttendance);
    
    @Query("SELECT AVG(e.gradePoints) FROM Enrollment e WHERE e.student.studentId = :studentId AND e.grade IS NOT NULL")
    Double calculateStudentGPA(@Param("studentId") Long studentId);
    
    @Query("SELECT e FROM Enrollment e WHERE e.course.courseId = :courseId AND e.grade IS NOT NULL ORDER BY e.gradePoints DESC")
    List<Enrollment> findCourseEnrollmentsByGrade(@Param("courseId") Long courseId);
    
    // Native SQL queries
    @Query(value = "SELECT s.first_name, s.last_name, COUNT(e.enrollment_id) as total_enrollments " +
           "FROM tbl_student s JOIN tbl_enrollment e ON s.student_id = e.student_id " +
           "WHERE e.academic_year = :year GROUP BY s.student_id, s.first_name, s.last_name " +
           "ORDER BY total_enrollments DESC LIMIT :limit",
           nativeQuery = true)
    List<Object[]> findMostActiveStudentsByYear(@Param("year") Integer year, @Param("limit") int limit);
    
    @Query(value = "SELECT c.title, c.course_code, COUNT(e.enrollment_id) as enrollment_count, " +
           "AVG(e.grade_points) as avg_grade " +
           "FROM tbl_course c LEFT JOIN tbl_enrollment e ON c.course_id = e.course_id " +
           "WHERE e.semester = :semester GROUP BY c.course_id " +
           "ORDER BY enrollment_count DESC",
           nativeQuery = true)
    List<Object[]> getCourseStatisticsBySemester(@Param("semester") String semester);
    
    // Projection interfaces
    interface StudentEnrollmentSummary {
        String getStudentName();
        String getStudentEmail();
        Long getTotalEnrollments();
        Double getAverageGrade();
    }
    
    @Query("SELECT CONCAT(s.firstName, ' ', s.lastName) as studentName, s.emailId as studentEmail, " +
           "COUNT(e) as totalEnrollments, AVG(e.gradePoints) as averageGrade " +
           "FROM Enrollment e JOIN e.student s " +
           "WHERE e.academicYear = :year AND e.grade IS NOT NULL " +
           "GROUP BY s ORDER BY averageGrade DESC")
    List<StudentEnrollmentSummary> getStudentPerformanceSummary(@Param("year") Integer academicYear);
    
    interface CourseEnrollmentStats {
        String getCourseTitle();
        String getCourseCode();
        Long getEnrollmentCount();
        Long getCompletedCount();
        Double getAverageGrade();
        Double getPassRate();
    }
    
    @Query("SELECT c.title as courseTitle, c.courseCode as courseCode, " +
           "COUNT(e) as enrollmentCount, " +
           "SUM(CASE WHEN e.enrollmentStatus = 'COMPLETED' THEN 1 ELSE 0 END) as completedCount, " +
           "AVG(e.gradePoints) as averageGrade, " +
           "(SUM(CASE WHEN e.gradePoints >= 2.0 THEN 1 ELSE 0 END) * 100.0 / COUNT(e)) as passRate " +
           "FROM Enrollment e JOIN e.course c " +
           "WHERE e.academicYear = :year " +
           "GROUP BY c ORDER BY enrollmentCount DESC")
    List<CourseEnrollmentStats> getCourseEnrollmentStatistics(@Param("year") Integer academicYear);
    
    // Complex search method
    @Query("SELECT e FROM Enrollment e WHERE " +
           "(:studentId IS NULL OR e.student.studentId = :studentId) AND " +
           "(:courseId IS NULL OR e.course.courseId = :courseId) AND " +
           "(:semester IS NULL OR e.semester = :semester) AND " +
           "(:academicYear IS NULL OR e.academicYear = :academicYear) AND " +
           "(:status IS NULL OR e.enrollmentStatus = :status)")
    List<Enrollment> searchEnrollments(@Param("studentId") Long studentId,
                                     @Param("courseId") Long courseId,
                                     @Param("semester") String semester,
                                     @Param("academicYear") Integer academicYear,
                                     @Param("status") Enrollment.EnrollmentStatus status);
    
    // Using named queries
    List<Enrollment> findByStudentAndSemester(@Param("studentId") Long studentId, 
                                            @Param("semester") String semester);
    
    List<Enrollment> findByCourseAndAcademicYear(@Param("courseId") Long courseId, 
                                               @Param("academicYear") Integer academicYear);
}