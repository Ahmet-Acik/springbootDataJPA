package com.example.springdatajpa.repository;

import com.example.springdatajpa.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    
    // Derived query methods
    Optional<Department> findByDepartmentName(String departmentName);
    
    List<Department> findByDepartmentNameContainingIgnoreCase(String departmentName);
    
    List<Department> findByDepartmentTypeAndIsActive(
            Department.DepartmentType departmentType, Boolean isActive);
    
    Optional<Department> findByDepartmentCode(String departmentCode);
    
    List<Department> findByIsActiveTrue();
    
    Page<Department> findByDepartmentType(Department.DepartmentType departmentType, Pageable pageable);
    
    // JPQL queries
    @Query("SELECT d FROM Department d WHERE d.headOfDepartment = :head")
    List<Department> findByHeadOfDepartment(@Param("head") String headOfDepartment);
    
    @Query("SELECT d FROM Department d WHERE d.departmentName LIKE %:keyword% OR d.departmentAddress LIKE %:keyword%")
    List<Department> searchByKeyword(@Param("keyword") String keyword);
    
    @Query("SELECT d FROM Department d JOIN d.courses c WHERE c.isActive = true GROUP BY d HAVING COUNT(c) > :minCourseCount")
    List<Department> findDepartmentsWithMinimumCourses(@Param("minCourseCount") long minCourseCount);
    
    // Native SQL queries
    @Query(value = "SELECT * FROM tbl_department d WHERE d.created_date >= :fromDate", nativeQuery = true)
    List<Department> findDepartmentsCreatedAfter(@Param("fromDate") LocalDateTime fromDate);
    
    @Query(value = "SELECT d.*, COUNT(c.course_id) as course_count " +
           "FROM tbl_department d LEFT JOIN tbl_course c ON d.department_id = c.department_id " +
           "GROUP BY d.department_id ORDER BY course_count DESC LIMIT :limit",
           nativeQuery = true)
    List<Object[]> findTopDepartmentsByCourseCount(@Param("limit") int limit);
    
    // Modifying queries
    @Modifying
    @Query("UPDATE Department d SET d.isActive = false WHERE d.departmentId = :departmentId")
    int deactivateDepartment(@Param("departmentId") Long departmentId);
    
    @Modifying
    @Query("UPDATE Department d SET d.headOfDepartment = :newHead WHERE d.departmentId = :departmentId")
    int updateHeadOfDepartment(@Param("departmentId") Long departmentId, @Param("newHead") String newHead);
    
    // Projection interfaces for custom return types
    interface DepartmentSummary {
        String getDepartmentName();
        String getDepartmentCode();
        Long getCourseCount();
    }
    
    @Query("SELECT d.departmentName as departmentName, d.departmentCode as departmentCode, " +
           "COUNT(c) as courseCount FROM Department d LEFT JOIN d.courses c GROUP BY d")
    List<DepartmentSummary> getDepartmentSummaries();
    
    // Using named queries
    List<Department> findByDepartmentNameIgnoreCase(@Param("departmentName") String departmentName);
    
    List<Department> findActiveDepartments();
}