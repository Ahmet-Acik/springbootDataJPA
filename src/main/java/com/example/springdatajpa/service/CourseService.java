package com.example.springdatajpa.service;

import com.example.springdatajpa.entity.Course;
import com.example.springdatajpa.entity.Department;
import com.example.springdatajpa.repository.CourseRepository;
import com.example.springdatajpa.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseService {

    private final CourseRepository courseRepository;
    private final DepartmentRepository departmentRepository;

    @Transactional
    public Course createCourse(Course course) {
        if (course.getIsActive() == null) {
            course.setIsActive(true);
        }
        return courseRepository.save(course);
    }

    @Transactional
    public Course createCourseForDepartment(Course course, Long departmentId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + departmentId));
        course.setDepartment(department);
        if (course.getIsActive() == null) {
            course.setIsActive(true);
        }
        return courseRepository.save(course);
    }

    public Optional<Course> findById(Long id) {
        return courseRepository.findById(id);
    }

    public List<Course> findAllCourses() {
        return courseRepository.findAll();
    }

    public Page<Course> findAllCourses(Pageable pageable) {
        return courseRepository.findAll(pageable);
    }

    public Optional<Course> findByCourseCode(String courseCode) {
        return courseRepository.findByCourseCode(courseCode);
    }

    public List<Course> findByDepartment(Long departmentId) {
        return courseRepository.findByDepartmentDepartmentId(departmentId);
    }

    public List<Course> findByDepartmentName(String departmentName) {
        return courseRepository.findActiveCoursesByDepartmentName(departmentName);
    }

    public List<Course> findActiveCourses() {
        return courseRepository.findByIsActiveTrueOrderByTitleAsc();
    }

    public List<Course> findByCreditHoursBetween(BigDecimal minCredits, BigDecimal maxCredits) {
        return courseRepository.findByCreditHoursBetween(minCredits, maxCredits);
    }

    public List<Course> findByCreditHoursAndLevel(BigDecimal creditHours, Course.CourseLevel level) {
        return courseRepository.findCoursesByCreditsAndLevel(creditHours, level);
    }

    public List<Course> findCoursesWithLongDescription(int minLength) {
        return courseRepository.findCoursesWithDetailedDescription(minLength);
    }

    public Double getAverageCreditHoursByDepartment(Long departmentId) {
        return courseRepository.findAverageCreditHoursByDepartment(departmentId);
    }

    @Transactional
    public Course updateCourse(Course course) {
        if (!courseRepository.existsById(course.getCourseId())) {
            throw new RuntimeException("Course not found with id: " + course.getCourseId());
        }
        return courseRepository.save(course);
    }

    @Transactional
    public Course updateCourseTitle(Long courseId, String newTitle) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));
        course.setTitle(newTitle);
        return courseRepository.save(course);
    }

    @Transactional
    public Course updateCourseDescription(Long courseId, String newDescription) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));
        course.setDescription(newDescription);
        return courseRepository.save(course);
    }

    @Transactional
    public Course activateCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));
        course.setIsActive(true);
        return courseRepository.save(course);
    }

    @Transactional
    public Course deactivateCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));
        course.setIsActive(false);
        return courseRepository.save(course);
    }

    @Transactional
    public void deleteCourse(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new RuntimeException("Course not found with id: " + id);
        }
        courseRepository.deleteById(id);
    }

    public long countActiveCourses() {
        return courseRepository.findByIsActiveTrueOrderByTitleAsc().size();
    }

    public long countCoursesByDepartment(Long departmentId) {
        return courseRepository.findByDepartmentDepartmentId(departmentId).size();
    }

    public boolean existsByCourseCode(String courseCode) {
        return courseRepository.findByCourseCode(courseCode).isPresent();
    }

    public List<Course> searchCourses(String searchTerm) {
        // Search by title, course code, or description
        return courseRepository.findAll().stream()
                .filter(course -> 
                    course.getTitle().toLowerCase().contains(searchTerm.toLowerCase()) ||
                    course.getCourseCode().toLowerCase().contains(searchTerm.toLowerCase()) ||
                    (course.getDescription() != null && course.getDescription().toLowerCase().contains(searchTerm.toLowerCase())))
                .toList();
    }

    @Transactional
    public Course transferCourseToDepartment(Long courseId, Long newDepartmentId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));
        Department newDepartment = departmentRepository.findById(newDepartmentId)
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + newDepartmentId));
        
        course.setDepartment(newDepartment);
        return courseRepository.save(course);
    }
}