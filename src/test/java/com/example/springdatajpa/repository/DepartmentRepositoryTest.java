package com.example.springdatajpa.repository;

import com.example.springdatajpa.entity.Course;
import com.example.springdatajpa.entity.Department;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.apache.commons.lang3.RandomStringUtils.*;

import jakarta.persistence.EntityManager;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Department Repository Tests")
class DepartmentRepositoryTest {
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private EntityManager entityManager;

    private Department testDepartment;
    private String randomDepartmentName;
    private String randomDepartmentCode;
    private String randomAddress;

    @BeforeEach
    void setUp() {
        // Clean up before each test
        courseRepository.deleteAll();
        departmentRepository.deleteAll();
        
        // Generate random data
        randomDepartmentName = "Department of " + randomAlphabetic(8);
        randomDepartmentCode = randomAlphabetic(3).toUpperCase();
        randomAddress = randomNumeric(3) + " " + randomAlphabetic(10) + " Street";
        
        // Create test department
        testDepartment = Department.builder()
                .departmentName(randomDepartmentName)
                .departmentCode(randomDepartmentCode)
                .departmentAddress(randomAddress)
                .headOfDepartment("Dr. " + randomAlphabetic(8))
                .departmentType(Department.DepartmentType.ENGINEERING)
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("Save Department Successfully")
    void saveDepartment() {
        // When
        Department savedDepartment = departmentRepository.save(testDepartment);
        
        // Then
        assertNotNull(savedDepartment);
        assertNotNull(savedDepartment.getDepartmentId());
        assertEquals(randomDepartmentName, savedDepartment.getDepartmentName());
        assertEquals(randomDepartmentCode, savedDepartment.getDepartmentCode());
        assertEquals(randomAddress, savedDepartment.getDepartmentAddress());
        assertEquals(Department.DepartmentType.ENGINEERING, savedDepartment.getDepartmentType());
        assertTrue(savedDepartment.getIsActive());
        assertNotNull(savedDepartment.getCreatedDate());
    }

    @Test
    @DisplayName("Find Department By Name")
    void findByDepartmentName() {
        // Given
        departmentRepository.save(testDepartment);
        
        // When
        Optional<Department> found = departmentRepository.findByDepartmentName(randomDepartmentName);
        
        // Then
        assertTrue(found.isPresent());
        assertEquals(randomDepartmentName, found.get().getDepartmentName());
        assertEquals(randomDepartmentCode, found.get().getDepartmentCode());
    }

    @Test
    @DisplayName("Find Department By Non-Existent Name")
    void findByNonExistentDepartmentName() {
        // When
        Optional<Department> found = departmentRepository.findByDepartmentName("Non-Existent Department");
        
        // Then
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Find Departments By Name Containing (Case Insensitive)")
    void findByDepartmentNameContainingIgnoreCase() {
        // Given
        Department dept1 = Department.builder()
                .departmentName("Computer Science Department")
                .departmentCode("CS")
                .departmentType(Department.DepartmentType.SCIENCE)
                .isActive(true)
                .build();
        
        Department dept2 = Department.builder()
                .departmentName("Information Technology Department")
                .departmentCode("IT")
                .departmentType(Department.DepartmentType.ENGINEERING)
                .isActive(true)
                .build();
        
        departmentRepository.saveAll(List.of(dept1, dept2));
        
        // When
        List<Department> foundDepartments = departmentRepository.findByDepartmentNameContainingIgnoreCase("science");
        
        // Then
        assertEquals(1, foundDepartments.size());
        assertEquals("Computer Science Department", foundDepartments.get(0).getDepartmentName());
    }

    @Test
    @DisplayName("Find Departments By Type And Active Status")
    void findByDepartmentTypeAndIsActive() {
        // Given
        Department activeScienceDept = Department.builder()
                .departmentName("Physics Department")
                .departmentCode("PHY")
                .departmentType(Department.DepartmentType.SCIENCE)
                .isActive(true)
                .build();
        
        Department inactiveScienceDept = Department.builder()
                .departmentName("Chemistry Department")
                .departmentCode("CHE")
                .departmentType(Department.DepartmentType.SCIENCE)
                .isActive(false)
                .build();
        
        Department activeEngDept = Department.builder()
                .departmentName("Civil Engineering Department")
                .departmentCode("CE")
                .departmentType(Department.DepartmentType.ENGINEERING)
                .isActive(true)
                .build();
        
        departmentRepository.saveAll(List.of(activeScienceDept, inactiveScienceDept, activeEngDept));
        
        // When
        List<Department> activeScienceDepartments = departmentRepository.findByDepartmentTypeAndIsActive(
                Department.DepartmentType.SCIENCE, true);
        
        // Then
        assertEquals(1, activeScienceDepartments.size());
        assertEquals("Physics Department", activeScienceDepartments.get(0).getDepartmentName());
        assertTrue(activeScienceDepartments.get(0).getIsActive());
    }

    @Test
    @DisplayName("Find Department By Department Code")
    void findByDepartmentCode() {
        // Given
        departmentRepository.save(testDepartment);
        
        // When
        Optional<Department> found = departmentRepository.findByDepartmentCode(randomDepartmentCode);
        
        // Then
        assertTrue(found.isPresent());
        assertEquals(randomDepartmentCode, found.get().getDepartmentCode());
        assertEquals(randomDepartmentName, found.get().getDepartmentName());
    }

    @Test
    @DisplayName("Find Active Departments Only")
    void findByIsActiveTrue() {
        // Given
        Department activeDept = Department.builder()
                .departmentName("Active Department")
                .departmentCode("ACT")
                .departmentType(Department.DepartmentType.ARTS)
                .isActive(true)
                .build();
        
        Department inactiveDept = Department.builder()
                .departmentName("Inactive Department")
                .departmentCode("INA")
                .departmentType(Department.DepartmentType.ARTS)
                .isActive(false)
                .build();
        
        departmentRepository.saveAll(List.of(activeDept, inactiveDept));
        
        // When
        List<Department> activeDepartments = departmentRepository.findByIsActiveTrue();
        
        // Then
        assertEquals(1, activeDepartments.size());
        assertEquals("Active Department", activeDepartments.get(0).getDepartmentName());
        assertTrue(activeDepartments.get(0).getIsActive());
    }

    @Test
    @DisplayName("Find Departments By Type with Pagination")
    void findByDepartmentTypeWithPagination() {
        // Given
        for (int i = 1; i <= 5; i++) {
            Department dept = Department.builder()
                    .departmentName("Science Department " + i)
                    .departmentCode("SC" + i)
                    .departmentType(Department.DepartmentType.SCIENCE)
                    .isActive(true)
                    .build();
            departmentRepository.save(dept);
        }
        
        // When
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by("departmentName"));
        Page<Department> departmentPage = departmentRepository.findByDepartmentType(
                Department.DepartmentType.SCIENCE, pageRequest);
        
        // Then
        assertEquals(5, departmentPage.getTotalElements());
        assertEquals(2, departmentPage.getTotalPages());
        assertEquals(3, departmentPage.getContent().size());
        assertTrue(departmentPage.isFirst());
        assertFalse(departmentPage.isLast());
    }

    @Test
    @DisplayName("Find Departments By Head Of Department (JPQL)")
    void findByHeadOfDepartment() {
        // Given
        String headName = "Dr. " + randomAlphabetic(10);
        testDepartment.setHeadOfDepartment(headName);
        departmentRepository.save(testDepartment);
        
        Department anotherDept = Department.builder()
                .departmentName("Another Department")
                .departmentCode("ANO")
                .headOfDepartment("Dr. " + randomAlphabetic(10))
                .departmentType(Department.DepartmentType.ARTS)
                .isActive(true)
                .build();
        departmentRepository.save(anotherDept);
        
        // When
        List<Department> departments = departmentRepository.findByHeadOfDepartment(headName);
        
        // Then
        assertEquals(1, departments.size());
        assertEquals(headName, departments.get(0).getHeadOfDepartment());
        assertEquals(randomDepartmentName, departments.get(0).getDepartmentName());
    }

    @Test
    @DisplayName("Search Departments By Keyword (JPQL)")
    void searchByKeyword() {
        // Given
        Department dept1 = Department.builder()
                .departmentName("Computer Engineering Department")
                .departmentCode("CE")
                .departmentAddress("Technology Building")
                .departmentType(Department.DepartmentType.ENGINEERING)
                .isActive(true)
                .build();
        
        Department dept2 = Department.builder()
                .departmentName("Biology Department")
                .departmentCode("BIO")
                .departmentAddress("Science Building")
                .departmentType(Department.DepartmentType.SCIENCE)
                .isActive(true)
                .build();
        
        departmentRepository.saveAll(List.of(dept1, dept2));
        
        // When - Search by name keyword
        List<Department> foundByName = departmentRepository.searchByKeyword("Computer");
        
        // When - Search by address keyword
        List<Department> foundByAddress = departmentRepository.searchByKeyword("Science");
        
        // Then
        assertEquals(1, foundByName.size());
        assertEquals("Computer Engineering Department", foundByName.get(0).getDepartmentName());
        
        assertEquals(1, foundByAddress.size());
        assertEquals("Biology Department", foundByAddress.get(0).getDepartmentName());
    }

    @Test
    @DisplayName("Find Departments With Minimum Courses (JPQL)")
    @Transactional
    void findDepartmentsWithMinimumCourses() {
        // Given
        Department deptWithCourses = Department.builder()
                .departmentName("Department with Courses")
                .departmentCode("DWC")
                .departmentType(Department.DepartmentType.ENGINEERING)
                .isActive(true)
                .build();
        deptWithCourses = departmentRepository.save(deptWithCourses);
        
        Department deptWithoutCourses = Department.builder()
                .departmentName("Department without Courses")
                .departmentCode("DWO")
                .departmentType(Department.DepartmentType.ARTS)
                .isActive(true)
                .build();
        departmentRepository.save(deptWithoutCourses);
        
        // Add active courses to first department
        for (int i = 1; i <= 3; i++) {
            Course course = Course.builder()
                    .courseCode("COURSE" + i)
                    .title("Course " + i)
                    .creditHours(new BigDecimal("3.0"))
                    .courseLevel(Course.CourseLevel.INTERMEDIATE)
                    .department(deptWithCourses)
                    .isActive(true)
                    .build();
            courseRepository.save(course);
        }
        
        // When
        List<Department> departments = departmentRepository.findDepartmentsWithMinimumCourses(2);
        
        // Then
        assertEquals(1, departments.size());
        assertEquals("Department with Courses", departments.get(0).getDepartmentName());
    }

    @Test
    @DisplayName("Find Departments Created After Date (Native SQL)")
    void findDepartmentsCreatedAfter() {
        // Given
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
        
        departmentRepository.save(testDepartment);
        
        // When
        List<Department> recentDepartments = departmentRepository.findDepartmentsCreatedAfter(yesterday);
        List<Department> futureDepartments = departmentRepository.findDepartmentsCreatedAfter(tomorrow);
        
        // Then
        assertEquals(1, recentDepartments.size());
        assertEquals(randomDepartmentName, recentDepartments.get(0).getDepartmentName());
        
        assertEquals(0, futureDepartments.size());
    }

    @Test
    @DisplayName("Find Top Departments By Course Count (Native SQL)")
    @Transactional
    void findTopDepartmentsByCourseCount() {
        // Given
        Department dept1 = Department.builder()
                .departmentName("Department A")
                .departmentCode("DA")
                .departmentType(Department.DepartmentType.SCIENCE)
                .isActive(true)
                .build();
        dept1 = departmentRepository.save(dept1);
        
        Department dept2 = Department.builder()
                .departmentName("Department B")
                .departmentCode("DB")
                .departmentType(Department.DepartmentType.ARTS)
                .isActive(true)
                .build();
        dept2 = departmentRepository.save(dept2);
        
        // Add more courses to dept1
        for (int i = 1; i <= 3; i++) {
            Course course = Course.builder()
                    .courseCode("DA" + i)
                    .title("Course " + i)
                    .creditHours(new BigDecimal("3.0"))
                    .courseLevel(Course.CourseLevel.INTERMEDIATE)
                    .department(dept1)
                    .isActive(true)
                    .build();
            courseRepository.save(course);
        }
        
        // Add fewer courses to dept2
        Course course = Course.builder()
                .courseCode("DB1")
                .title("Course 1")
                .creditHours(new BigDecimal("3.0"))
                .courseLevel(Course.CourseLevel.INTERMEDIATE)
                .department(dept2)
                .isActive(true)
                .build();
        courseRepository.save(course);
        
        // When
        List<Object[]> topDepartments = departmentRepository.findTopDepartmentsByCourseCount(2);
        
        // Then
        assertEquals(2, topDepartments.size());
        // The department with more courses should be first
        Object[] firstDept = topDepartments.get(0);
        assertNotNull(firstDept);
    }

    @Test
    @DisplayName("Deactivate Department (Modifying Query)")
    @Transactional
    void deactivateDepartment() {
        // Given
        Department savedDepartment = departmentRepository.save(testDepartment);
        assertTrue(savedDepartment.getIsActive());
        
        // When
        int updatedRows = departmentRepository.deactivateDepartment(savedDepartment.getDepartmentId());
        
        // Then
        assertEquals(1, updatedRows);
        
        // Clear the persistence context to force a fresh query
        entityManager.flush();
        entityManager.clear();
        
        // Verify the department is deactivated
        Optional<Department> updatedDepartment = departmentRepository.findById(savedDepartment.getDepartmentId());
        assertTrue(updatedDepartment.isPresent());
        assertFalse(updatedDepartment.get().getIsActive());
    }

    @Test
    @DisplayName("Update Head Of Department (Modifying Query)")
    @Transactional
    void updateHeadOfDepartment() {
        // Given
        Department savedDepartment = departmentRepository.save(testDepartment);
        String originalHead = savedDepartment.getHeadOfDepartment();
        String newHead = "Dr. " + randomAlphabetic(10);
        
        // When
        int updatedRows = departmentRepository.updateHeadOfDepartment(
                savedDepartment.getDepartmentId(), newHead);
        
        // Then
        assertEquals(1, updatedRows);
        
        // Clear the persistence context to force a fresh query
        entityManager.flush();
        entityManager.clear();
        
        // Verify the head of department is updated
        Optional<Department> updatedDepartment = departmentRepository.findById(savedDepartment.getDepartmentId());
        assertTrue(updatedDepartment.isPresent());
        assertEquals(newHead, updatedDepartment.get().getHeadOfDepartment());
        assertNotEquals(originalHead, updatedDepartment.get().getHeadOfDepartment());
    }

    @Test
    @DisplayName("Get Department Summaries (Projection)")
    @Transactional
    void getDepartmentSummaries() {
        // Given
        Department dept = departmentRepository.save(testDepartment);
        
        // Add some courses
        for (int i = 1; i <= 2; i++) {
            Course course = Course.builder()
                    .courseCode("TEST" + i)
                    .title("Test Course " + i)
                    .creditHours(new BigDecimal("3.0"))
                    .courseLevel(Course.CourseLevel.INTERMEDIATE)
                    .department(dept)
                    .isActive(true)
                    .build();
            courseRepository.save(course);
        }
        
        // When
        List<DepartmentRepository.DepartmentSummary> summaries = departmentRepository.getDepartmentSummaries();
        
        // Then
        assertFalse(summaries.isEmpty());
        DepartmentRepository.DepartmentSummary summary = summaries.get(0);
        assertNotNull(summary.getDepartmentName());
        assertNotNull(summary.getDepartmentCode());
        assertNotNull(summary.getCourseCount());
        assertEquals(2L, summary.getCourseCount());
    }

    @Test
    @DisplayName("Find By Department Name Ignore Case (Named Query)")
    void findByDepartmentNameIgnoreCase() {
        // Given
        departmentRepository.save(testDepartment);
        
        // When
        List<Department> found = departmentRepository.findByDepartmentNameIgnoreCase(
                randomDepartmentName.toUpperCase());
        
        // Then
        assertEquals(1, found.size());
        assertEquals(randomDepartmentName, found.get(0).getDepartmentName());
    }

    @Test
    @DisplayName("Find Active Departments (Named Query)")
    void findActiveDepartments() {
        // Given
        Department activeDept = Department.builder()
                .departmentName("Active Department")
                .departmentCode("ACT")
                .departmentType(Department.DepartmentType.SCIENCE)
                .isActive(true)
                .build();
        
        Department inactiveDept = Department.builder()
                .departmentName("Inactive Department")
                .departmentCode("INA")
                .departmentType(Department.DepartmentType.SCIENCE)
                .isActive(false)
                .build();
        
        departmentRepository.saveAll(List.of(activeDept, inactiveDept));
        
        // When
        List<Department> activeDepartments = departmentRepository.findActiveDepartments();
        
        // Then
        assertEquals(1, activeDepartments.size());
        assertEquals("Active Department", activeDepartments.get(0).getDepartmentName());
        assertTrue(activeDepartments.get(0).getIsActive());
    }

    @Test
    @DisplayName("Repository Operations - Update Department")
    void updateDepartment() {
        // Given
        Department savedDepartment = departmentRepository.save(testDepartment);
        String newName = "Updated " + randomDepartmentName;
        
        // When
        savedDepartment.setDepartmentName(newName);
        Department updatedDepartment = departmentRepository.save(savedDepartment);
        
        // Then
        assertEquals(newName, updatedDepartment.getDepartmentName());
        assertNotNull(updatedDepartment.getLastModifiedDate());
    }

    @Test
    @DisplayName("Repository Operations - Delete Department")
    void deleteDepartment() {
        // Given
        Department savedDepartment = departmentRepository.save(testDepartment);
        Long departmentId = savedDepartment.getDepartmentId();
        
        // When
        departmentRepository.delete(savedDepartment);
        
        // Then
        Optional<Department> deletedDepartment = departmentRepository.findById(departmentId);
        assertFalse(deletedDepartment.isPresent());
    }

    @Test
    @DisplayName("Repository Operations - Count Departments")
    void countDepartments() {
        // Given
        Department dept1 = Department.builder()
                .departmentName("Department 1")
                .departmentCode("D1")
                .departmentType(Department.DepartmentType.SCIENCE)
                .isActive(true)
                .build();
        
        Department dept2 = Department.builder()
                .departmentName("Department 2")
                .departmentCode("D2")
                .departmentType(Department.DepartmentType.ARTS)
                .isActive(true)
                .build();
        
        departmentRepository.saveAll(List.of(dept1, dept2));
        
        // When
        long count = departmentRepository.count();
        
        // Then
        assertEquals(2, count);
    }

    @Test
    @DisplayName("Test Department Types Enum")
    void testDepartmentTypesEnum() {
        // Given
        Department scienceDept = Department.builder()
                .departmentName("Science Department")
                .departmentCode("SCI")
                .departmentType(Department.DepartmentType.SCIENCE)
                .isActive(true)
                .build();
        
        Department artsDept = Department.builder()
                .departmentName("Arts Department")
                .departmentCode("ART")
                .departmentType(Department.DepartmentType.ARTS)
                .isActive(true)
                .build();
        
        Department engDept = Department.builder()
                .departmentName("Engineering Department")
                .departmentCode("ENG")
                .departmentType(Department.DepartmentType.ENGINEERING)
                .isActive(true)
                .build();
        
        // When
        List<Department> savedDepartments = departmentRepository.saveAll(
                List.of(scienceDept, artsDept, engDept));
        
        // Then
        assertEquals(3, savedDepartments.size());
        assertEquals(Department.DepartmentType.SCIENCE, savedDepartments.get(0).getDepartmentType());
        assertEquals(Department.DepartmentType.ARTS, savedDepartments.get(1).getDepartmentType());
        assertEquals(Department.DepartmentType.ENGINEERING, savedDepartments.get(2).getDepartmentType());
    }

    @Test
    @DisplayName("Test Unique Constraint on Department Code")
    void testUniqueDepartmentCode() {
        // Given
        Department dept1 = Department.builder()
                .departmentName("Department 1")
                .departmentCode("SAME")
                .departmentType(Department.DepartmentType.SCIENCE)
                .isActive(true)
                .build();
        departmentRepository.save(dept1);
        
        Department dept2 = Department.builder()
                .departmentName("Department 2")
                .departmentCode("SAME") // Same code as dept1
                .departmentType(Department.DepartmentType.ARTS)
                .isActive(true)
                .build();
        
        // When & Then
        assertThrows(Exception.class, () -> {
            departmentRepository.save(dept2);
            departmentRepository.flush(); // Force immediate database operation
        });
    }
}