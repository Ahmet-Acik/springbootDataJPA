package com.example.springdatajpa.repository;

import com.example.springdatajpa.entity.Course;
import com.example.springdatajpa.entity.Department;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.apache.commons.lang3.RandomStringUtils.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Course Repository Tests")
class CourseRepositoryTest {
    
    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;

    private Department testDepartment;
    private Course testCourse;
    private String randomCourseCode;
    private String randomTitle;

    @BeforeEach
    void setUp() {
        // Clean up before each test
        courseRepository.deleteAll();
        departmentRepository.deleteAll();
        
        // Generate random data
        randomCourseCode = "CS" + randomNumeric(3);
        randomTitle = "Test Course " + randomAlphabetic(5);
        
        // Create test department
        testDepartment = Department.builder()
                .departmentName("Computer Science")
                .departmentCode("CS")
                .departmentType(Department.DepartmentType.ENGINEERING)
                .headOfDepartment("Dr. Test Professor")
                .isActive(true)
                .build();
        
        testDepartment = departmentRepository.save(testDepartment);
        
        // Create test course
        testCourse = Course.builder()
                .courseCode(randomCourseCode)
                .title(randomTitle)
                .description("A comprehensive test course covering various topics")
                .creditHours(new BigDecimal("3.0"))
                .courseLevel(Course.CourseLevel.INTERMEDIATE)
                .department(testDepartment)
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("Save Course Successfully")
    void saveCourse() {
        // When
        Course savedCourse = courseRepository.save(testCourse);
        
        // Then
        assertNotNull(savedCourse);
        assertNotNull(savedCourse.getCourseId());
        assertEquals(randomCourseCode, savedCourse.getCourseCode());
        assertEquals(randomTitle, savedCourse.getTitle());
        assertEquals(testDepartment.getDepartmentId(), savedCourse.getDepartment().getDepartmentId());
        assertTrue(savedCourse.getIsActive());
        assertNotNull(savedCourse.getCreatedDate());
    }

    @Test
    @DisplayName("Find Course By Course Code")
    void findByCourseCode() {
        // Given
        courseRepository.save(testCourse);
        
        // When
        Optional<Course> found = courseRepository.findByCourseCode(randomCourseCode);
        
        // Then
        assertTrue(found.isPresent());
        assertEquals(randomCourseCode, found.get().getCourseCode());
        assertEquals(randomTitle, found.get().getTitle());
    }

    @Test
    @DisplayName("Find Course By Non-Existent Course Code")
    void findByNonExistentCourseCode() {
        // When
        Optional<Course> found = courseRepository.findByCourseCode("NONEXISTENT123");
        
        // Then
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Find Courses By Title Containing (Case Insensitive)")
    void findByTitleContainingIgnoreCase() {
        // Given
        Course course1 = Course.builder()
                .courseCode("CS101")
                .title("Introduction to Programming")
                .creditHours(new BigDecimal("3.0"))
                .courseLevel(Course.CourseLevel.BEGINNER)
                .department(testDepartment)
                .isActive(true)
                .build();
        
        Course course2 = Course.builder()
                .courseCode("CS102")
                .title("Advanced Programming Concepts")
                .creditHours(new BigDecimal("4.0"))
                .courseLevel(Course.CourseLevel.ADVANCED)
                .department(testDepartment)
                .isActive(true)
                .build();
        
        courseRepository.saveAll(List.of(course1, course2));
        
        // When
        List<Course> foundCourses = courseRepository.findByTitleContainingIgnoreCase("programming");
        
        // Then
        assertEquals(2, foundCourses.size());
        assertTrue(foundCourses.stream().anyMatch(c -> c.getTitle().contains("Introduction")));
        assertTrue(foundCourses.stream().anyMatch(c -> c.getTitle().contains("Advanced")));
    }

    @Test
    @DisplayName("Find Courses By Department")
    void findByDepartment() {
        // Given
        courseRepository.save(testCourse);
        
        // When
        List<Course> foundCourses = courseRepository.findByDepartment(testDepartment);
        
        // Then
        assertEquals(1, foundCourses.size());
        assertEquals(testCourse.getCourseCode(), foundCourses.get(0).getCourseCode());
    }

    @Test
    @DisplayName("Find Courses By Department ID")
    void findByDepartmentDepartmentId() {
        // Given
        courseRepository.save(testCourse);
        
        // When
        List<Course> foundCourses = courseRepository.findByDepartmentDepartmentId(testDepartment.getDepartmentId());
        
        // Then
        assertEquals(1, foundCourses.size());
        assertEquals(testCourse.getCourseCode(), foundCourses.get(0).getCourseCode());
    }

    @Test
    @DisplayName("Find Courses By Credit Hours Greater Than")
    void findByCreditHoursGreaterThan() {
        // Given
        Course lowCreditCourse = Course.builder()
                .courseCode("CS100")
                .title("Low Credit Course")
                .creditHours(new BigDecimal("1.0"))
                .courseLevel(Course.CourseLevel.BEGINNER)
                .department(testDepartment)
                .isActive(true)
                .build();
        
        Course highCreditCourse = Course.builder()
                .courseCode("CS400")
                .title("High Credit Course")
                .creditHours(new BigDecimal("5.0"))
                .courseLevel(Course.CourseLevel.ADVANCED)
                .department(testDepartment)
                .isActive(true)
                .build();
        
        courseRepository.saveAll(List.of(lowCreditCourse, highCreditCourse));
        
        // When
        List<Course> foundCourses = courseRepository.findByCreditHoursGreaterThan(new BigDecimal("2.0"));
        
        // Then
        assertEquals(1, foundCourses.size());
        assertEquals("CS400", foundCourses.get(0).getCourseCode());
    }

    @Test
    @DisplayName("Find Courses By Credit Hours Between")
    void findByCreditHoursBetween() {
        // Given
        Course course1 = Course.builder()
                .courseCode("CS200")
                .title("Medium Credit Course 1")
                .creditHours(new BigDecimal("2.5"))
                .courseLevel(Course.CourseLevel.INTERMEDIATE)
                .department(testDepartment)
                .isActive(true)
                .build();
        
        Course course2 = Course.builder()
                .courseCode("CS300")
                .title("Medium Credit Course 2")
                .creditHours(new BigDecimal("3.5"))
                .courseLevel(Course.CourseLevel.INTERMEDIATE)
                .department(testDepartment)
                .isActive(true)
                .build();
        
        Course course3 = Course.builder()
                .courseCode("CS500")
                .title("High Credit Course")
                .creditHours(new BigDecimal("6.0"))
                .courseLevel(Course.CourseLevel.EXPERT)
                .department(testDepartment)
                .isActive(true)
                .build();
        
        courseRepository.saveAll(List.of(course1, course2, course3));
        
        // When
        List<Course> foundCourses = courseRepository.findByCreditHoursBetween(
                new BigDecimal("2.0"), new BigDecimal("4.0"));
        
        // Then
        assertEquals(2, foundCourses.size());
        assertTrue(foundCourses.stream().anyMatch(c -> c.getCourseCode().equals("CS200")));
        assertTrue(foundCourses.stream().anyMatch(c -> c.getCourseCode().equals("CS300")));
    }

    @Test
    @DisplayName("Find Courses By Course Level")
    void findByCourseLevel() {
        // Given
        Course beginnerCourse = Course.builder()
                .courseCode("CS101")
                .title("Beginner Course")
                .creditHours(new BigDecimal("2.0"))
                .courseLevel(Course.CourseLevel.BEGINNER)
                .department(testDepartment)
                .isActive(true)
                .build();
        
        Course advancedCourse = Course.builder()
                .courseCode("CS401")
                .title("Advanced Course")
                .creditHours(new BigDecimal("4.0"))
                .courseLevel(Course.CourseLevel.ADVANCED)
                .department(testDepartment)
                .isActive(true)
                .build();
        
        courseRepository.saveAll(List.of(beginnerCourse, advancedCourse));
        
        // When
        List<Course> beginnerCourses = courseRepository.findByCourseLevel(Course.CourseLevel.BEGINNER);
        
        // Then
        assertEquals(1, beginnerCourses.size());
        assertEquals("CS101", beginnerCourses.get(0).getCourseCode());
    }

    @Test
    @DisplayName("Find Active Courses Ordered By Title")
    void findByIsActiveTrueOrderByTitleAsc() {
        // Given
        Course course1 = Course.builder()
                .courseCode("CS201")
                .title("Zebra Course") // Should be last alphabetically
                .creditHours(new BigDecimal("3.0"))
                .courseLevel(Course.CourseLevel.INTERMEDIATE)
                .department(testDepartment)
                .isActive(true)
                .build();
        
        Course course2 = Course.builder()
                .courseCode("CS202")
                .title("Alpha Course") // Should be first alphabetically
                .creditHours(new BigDecimal("3.0"))
                .courseLevel(Course.CourseLevel.INTERMEDIATE)
                .department(testDepartment)
                .isActive(true)
                .build();
        
        Course inactiveCourse = Course.builder()
                .courseCode("CS203")
                .title("Inactive Course")
                .creditHours(new BigDecimal("3.0"))
                .courseLevel(Course.CourseLevel.INTERMEDIATE)
                .department(testDepartment)
                .isActive(false)
                .build();
        
        courseRepository.saveAll(List.of(course1, course2, inactiveCourse));
        
        // When
        List<Course> activeCourses = courseRepository.findByIsActiveTrueOrderByTitleAsc();
        
        // Then
        assertEquals(2, activeCourses.size());
        assertEquals("Alpha Course", activeCourses.get(0).getTitle());
        assertEquals("Zebra Course", activeCourses.get(1).getTitle());
    }

    @Test
    @DisplayName("Find Courses By Department Name with Pagination")
    void findByDepartmentDepartmentNameWithPagination() {
        // Given
        for (int i = 1; i <= 5; i++) {
            Course course = Course.builder()
                    .courseCode("CS" + (200 + i))
                    .title("Course " + i)
                    .creditHours(new BigDecimal("3.0"))
                    .courseLevel(Course.CourseLevel.INTERMEDIATE)
                    .department(testDepartment)
                    .isActive(true)
                    .build();
            courseRepository.save(course);
        }
        
        // When
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by("title"));
        Page<Course> coursePage = courseRepository.findByDepartmentDepartmentName(
                "Computer Science", pageRequest);
        
        // Then
        assertEquals(5, coursePage.getTotalElements());
        assertEquals(2, coursePage.getTotalPages());
        assertEquals(3, coursePage.getContent().size());
        assertTrue(coursePage.isFirst());
        assertFalse(coursePage.isLast());
    }

    @Test
    @DisplayName("Find Active Courses By Department Name (JPQL)")
    void findActiveCoursesByDepartmentName() {
        // Given
        Course activeCourse = Course.builder()
                .courseCode("CS301")
                .title("Active Course")
                .creditHours(new BigDecimal("3.0"))
                .courseLevel(Course.CourseLevel.INTERMEDIATE)
                .department(testDepartment)
                .isActive(true)
                .build();
        
        Course inactiveCourse = Course.builder()
                .courseCode("CS302")
                .title("Inactive Course")
                .creditHours(new BigDecimal("3.0"))
                .courseLevel(Course.CourseLevel.INTERMEDIATE)
                .department(testDepartment)
                .isActive(false)
                .build();
        
        courseRepository.saveAll(List.of(activeCourse, inactiveCourse));
        
        // When
        List<Course> activeCourses = courseRepository.findActiveCoursesByDepartmentName("Computer Science");
        
        // Then
        assertEquals(1, activeCourses.size());
        assertEquals("CS301", activeCourses.get(0).getCourseCode());
        assertTrue(activeCourses.get(0).getIsActive());
    }

    @Test
    @DisplayName("Find Courses By Credits And Level (JPQL)")
    void findCoursesByCreditsAndLevel() {
        // Given
        Course matchingCourse = Course.builder()
                .courseCode("CS401")
                .title("Advanced High Credit Course")
                .creditHours(new BigDecimal("4.0"))
                .courseLevel(Course.CourseLevel.ADVANCED)
                .department(testDepartment)
                .isActive(true)
                .build();
        
        Course nonMatchingCourse = Course.builder()
                .courseCode("CS101")
                .title("Beginner Low Credit Course")
                .creditHours(new BigDecimal("2.0"))
                .courseLevel(Course.CourseLevel.BEGINNER)
                .department(testDepartment)
                .isActive(true)
                .build();
        
        courseRepository.saveAll(List.of(matchingCourse, nonMatchingCourse));
        
        // When
        List<Course> foundCourses = courseRepository.findCoursesByCreditsAndLevel(
                new BigDecimal("3.0"), Course.CourseLevel.ADVANCED);
        
        // Then
        assertEquals(1, foundCourses.size());
        assertEquals("CS401", foundCourses.get(0).getCourseCode());
    }

    @Test
    @DisplayName("Find Courses With Detailed Description (JPQL)")
    void findCoursesWithDetailedDescription() {
        // Given
        Course detailedCourse = Course.builder()
                .courseCode("CS501")
                .title("Detailed Course")
                .description("This is a very detailed description that is quite long and comprehensive")
                .creditHours(new BigDecimal("3.0"))
                .courseLevel(Course.CourseLevel.ADVANCED)
                .department(testDepartment)
                .isActive(true)
                .build();
        
        Course shortDescriptionCourse = Course.builder()
                .courseCode("CS502")
                .title("Short Course")
                .description("Short")
                .creditHours(new BigDecimal("3.0"))
                .courseLevel(Course.CourseLevel.ADVANCED)
                .department(testDepartment)
                .isActive(true)
                .build();
        
        courseRepository.saveAll(List.of(detailedCourse, shortDescriptionCourse));
        
        // When
        List<Course> detailedCourses = courseRepository.findCoursesWithDetailedDescription(20);
        
        // Then
        assertEquals(1, detailedCourses.size());
        assertEquals("CS501", detailedCourses.get(0).getCourseCode());
    }

    @Test
    @DisplayName("Find Average Credit Hours By Department (Native Query)")
    void findAverageCreditHoursByDepartment() {
        // Given
        Course course1 = Course.builder()
                .courseCode("CS601")
                .title("Course 1")
                .creditHours(new BigDecimal("2.0"))
                .courseLevel(Course.CourseLevel.INTERMEDIATE)
                .department(testDepartment)
                .isActive(true)
                .build();
        
        Course course2 = Course.builder()
                .courseCode("CS602")
                .title("Course 2")
                .creditHours(new BigDecimal("4.0"))
                .courseLevel(Course.CourseLevel.INTERMEDIATE)
                .department(testDepartment)
                .isActive(true)
                .build();
        
        courseRepository.saveAll(List.of(course1, course2));
        
        // When
        Double averageCredits = courseRepository.findAverageCreditHoursByDepartment(testDepartment.getDepartmentId());
        
        // Then
        assertNotNull(averageCredits);
        assertEquals(3.0, averageCredits, 0.01);
    }

    @Test
    @DisplayName("Search Courses with Multiple Criteria")
    void searchCourses() {
        // Given
        Course course1 = Course.builder()
                .courseCode("CS701")
                .title("Data Structures")
                .creditHours(new BigDecimal("3.0"))
                .courseLevel(Course.CourseLevel.INTERMEDIATE)
                .department(testDepartment)
                .isActive(true)
                .build();
        
        Course course2 = Course.builder()
                .courseCode("CS702")
                .title("Database Systems")
                .creditHours(new BigDecimal("4.0"))
                .courseLevel(Course.CourseLevel.ADVANCED)
                .department(testDepartment)
                .isActive(true)
                .build();
        
        courseRepository.saveAll(List.of(course1, course2));
        
        // When - Search for courses with "Data" in title and minimum 3 credit hours
        List<Course> foundCourses = courseRepository.searchCourses(
                "Data", 
                testDepartment.getDepartmentId(), 
                null, 
                new BigDecimal("3.0")
        );
        
        // Then
        assertEquals(2, foundCourses.size());
        assertTrue(foundCourses.stream().allMatch(c -> c.getTitle().contains("Data")));
        assertTrue(foundCourses.stream().allMatch(c -> c.getCreditHours().compareTo(new BigDecimal("3.0")) >= 0));
    }

    @Test
    @DisplayName("Get Course Statistics (Projection)")
    void getCourseStatistics() {
        // Given
        courseRepository.save(testCourse);
        
        // When
        List<CourseRepository.CourseStatistics> statistics = courseRepository.getCourseStatistics();
        
        // Then
        assertFalse(statistics.isEmpty());
        CourseRepository.CourseStatistics stat = statistics.get(0);
        assertNotNull(stat.getTitle());
        assertNotNull(stat.getCourseCode());
        assertNotNull(stat.getCreditHours());
        assertNotNull(stat.getDepartmentName());
        assertNotNull(stat.getEnrollmentCount());
    }

    @Test
    @DisplayName("Named Query - Find By Department ID")
    void findByDepartmentIdNamedQuery() {
        // Given
        courseRepository.save(testCourse);
        
        // When
        List<Course> foundCourses = courseRepository.findByDepartmentId(testDepartment.getDepartmentId());
        
        // Then
        assertEquals(1, foundCourses.size());
        assertEquals(testCourse.getCourseCode(), foundCourses.get(0).getCourseCode());
    }

    @Test
    @DisplayName("Repository Operations - Update Course")
    void updateCourse() {
        // Given
        Course savedCourse = courseRepository.save(testCourse);
        String newTitle = "Updated Course Title";
        
        // When
        savedCourse.setTitle(newTitle);
        Course updatedCourse = courseRepository.save(savedCourse);
        
        // Then
        assertEquals(newTitle, updatedCourse.getTitle());
        assertNotNull(updatedCourse.getLastModifiedDate());
    }

    @Test
    @DisplayName("Repository Operations - Delete Course")
    void deleteCourse() {
        // Given
        Course savedCourse = courseRepository.save(testCourse);
        Long courseId = savedCourse.getCourseId();
        
        // When
        courseRepository.delete(savedCourse);
        
        // Then
        Optional<Course> deletedCourse = courseRepository.findById(courseId);
        assertFalse(deletedCourse.isPresent());
    }

    @Test
    @DisplayName("Repository Operations - Count Courses")
    void countCourses() {
        // Given
        Course course1 = Course.builder()
                .courseCode("CS801")
                .title("Course 1")
                .creditHours(new BigDecimal("3.0"))
                .courseLevel(Course.CourseLevel.INTERMEDIATE)
                .department(testDepartment)
                .isActive(true)
                .build();
        
        Course course2 = Course.builder()
                .courseCode("CS802")
                .title("Course 2")
                .creditHours(new BigDecimal("3.0"))
                .courseLevel(Course.CourseLevel.INTERMEDIATE)
                .department(testDepartment)
                .isActive(true)
                .build();
        
        courseRepository.saveAll(List.of(course1, course2));
        
        // When
        long count = courseRepository.count();
        
        // Then
        assertEquals(2, count);
    }
}