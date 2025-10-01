package com.example.springdatajpa.repository;

import com.example.springdatajpa.entity.Course;
import com.example.springdatajpa.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    
    // Derived query methods
    Optional<Course> findByCourseCode(String courseCode);
    
    List<Course> findByTitleContainingIgnoreCase(String title);
    
    List<Course> findByDepartment(Department department);
    
    List<Course> findByDepartmentDepartmentId(Long departmentId);
    
    List<Course> findByCreditHoursGreaterThan(BigDecimal creditHours);
    
    List<Course> findByCreditHoursBetween(BigDecimal minCredit, BigDecimal maxCredit);
    
    List<Course> findByCourseLevel(Course.CourseLevel courseLevel);
    
    List<Course> findByIsActiveTrueOrderByTitleAsc();
    
    Page<Course> findByDepartmentDepartmentName(String departmentName, Pageable pageable);
    
    // JPQL queries
    @Query("SELECT c FROM Course c WHERE c.department.departmentName = :deptName AND c.isActive = true")
    List<Course> findActiveCoursesByDepartmentName(@Param("deptName") String departmentName);
    
    @Query("SELECT c FROM Course c WHERE c.creditHours >= :minCredit AND c.courseLevel = :level")
    List<Course> findCoursesByCreditsAndLevel(@Param("minCredit") BigDecimal minCredit, 
                                             @Param("level") Course.CourseLevel level);
    
    @Query("SELECT c FROM Course c JOIN c.enrollments e GROUP BY c HAVING COUNT(e) > :minEnrollments")
    List<Course> findPopularCourses(@Param("minEnrollments") long minEnrollments);
    
    @Query("SELECT c FROM Course c WHERE c.description IS NOT NULL AND LENGTH(c.description) > :minLength")
    List<Course> findCoursesWithDetailedDescription(@Param("minLength") int minLength);
    
    // Native SQL queries
    @Query(value = "SELECT c.*, COUNT(e.enrollment_id) as enrollment_count " +
           "FROM tbl_course c LEFT JOIN tbl_enrollment e ON c.course_id = e.course_id " +
           "WHERE c.is_active = true GROUP BY c.course_id " +
           "ORDER BY enrollment_count DESC LIMIT :limit",
           nativeQuery = true)
    List<Object[]> findMostEnrolledCourses(@Param("limit") int limit);
    
    @Query(value = "SELECT AVG(c.credit_hours) FROM tbl_course c WHERE c.department_id = :departmentId",
           nativeQuery = true)
    Double findAverageCreditHoursByDepartment(@Param("departmentId") Long departmentId);
    
    // Projection for course statistics
    interface CourseStatistics {
        String getTitle();
        String getCourseCode();
        BigDecimal getCreditHours();
        String getDepartmentName();
        Long getEnrollmentCount();
    }
    
    @Query("SELECT c.title as title, c.courseCode as courseCode, c.creditHours as creditHours, " +
           "c.department.departmentName as departmentName, COUNT(e) as enrollmentCount " +
           "FROM Course c LEFT JOIN c.enrollments e " +
           "GROUP BY c ORDER BY enrollmentCount DESC")
    List<CourseStatistics> getCourseStatistics();
    
    // Custom search method
    @Query("SELECT c FROM Course c WHERE " +
           "(:title IS NULL OR UPPER(c.title) LIKE UPPER(CONCAT('%', :title, '%'))) AND " +
           "(:departmentId IS NULL OR c.department.departmentId = :departmentId) AND " +
           "(:courseLevel IS NULL OR c.courseLevel = :courseLevel) AND " +
           "(:minCredit IS NULL OR c.creditHours >= :minCredit)")
    List<Course> searchCourses(@Param("title") String title,
                              @Param("departmentId") Long departmentId,
                              @Param("courseLevel") Course.CourseLevel courseLevel,
                              @Param("minCredit") BigDecimal minCredit);
    
    // Using named queries
    List<Course> findByDepartmentId(@Param("departmentId") Long departmentId);
}