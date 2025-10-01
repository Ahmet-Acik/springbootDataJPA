package com.example.springdatajpa.service;

import com.example.springdatajpa.entity.Department;
import com.example.springdatajpa.entity.Course;
import com.example.springdatajpa.repository.DepartmentRepository;
import com.example.springdatajpa.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final CourseRepository courseRepository;

    // Read operations
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    public Optional<Department> getDepartmentById(Long id) {
        return departmentRepository.findById(id);
    }

    public Page<Department> getDepartmentsByType(Department.DepartmentType type, Pageable pageable) {
        return departmentRepository.findByDepartmentType(type, pageable);
    }

    public List<Department> searchDepartments(String keyword) {
        return departmentRepository.searchByKeyword(keyword);
    }

    public List<DepartmentRepository.DepartmentSummary> getDepartmentSummaries() {
        return departmentRepository.getDepartmentSummaries();
    }

    // Write operations with transactions
    @Transactional
    public Department createDepartment(Department department) {
        // Validate unique constraints
        if (department.getDepartmentCode() != null) {
            Optional<Department> existingDept = departmentRepository.findByDepartmentCode(department.getDepartmentCode());
            if (existingDept.isPresent()) {
                throw new IllegalArgumentException("Department code already exists: " + department.getDepartmentCode());
            }
        }

        return departmentRepository.save(department);
    }

    @Transactional
    public Department updateDepartment(Long departmentId, Department updatedDepartment) {
        Department existingDepartment = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new IllegalArgumentException("Department not found with id: " + departmentId));

        existingDepartment.setDepartmentName(updatedDepartment.getDepartmentName());
        existingDepartment.setDepartmentAddress(updatedDepartment.getDepartmentAddress());
        existingDepartment.setHeadOfDepartment(updatedDepartment.getHeadOfDepartment());
        existingDepartment.setDepartmentType(updatedDepartment.getDepartmentType());

        return departmentRepository.save(existingDepartment);
    }

    @Transactional
    public void deleteDepartment(Long departmentId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new IllegalArgumentException("Department not found with id: " + departmentId));

        // Check if department has active courses
        List<Course> activeCourses = courseRepository.findByDepartmentDepartmentId(departmentId)
                .stream()
                .filter(Course::getIsActive)
                .toList();

        if (!activeCourses.isEmpty()) {
            throw new IllegalStateException("Cannot delete department with active courses. " +
                    "Please deactivate or reassign courses first.");
        }

        // Soft delete
        department.setIsActive(false);
        departmentRepository.save(department);
    }

    // Complex transactional operations
    @Transactional(propagation = Propagation.REQUIRED)
    public Department createDepartmentWithCourses(Department department, List<Course> courses) {
        // Save department first
        Department savedDepartment = departmentRepository.save(department);

        // Save courses with department reference
        for (Course course : courses) {
            course.setDepartment(savedDepartment);
            courseRepository.save(course);
        }

        return savedDepartment;
    }

    @Transactional
    public void transferCoursesToDepartment(Long fromDepartmentId, Long toDepartmentId, List<Long> courseIds) {
        // Validate both departments exist
        departmentRepository.findById(fromDepartmentId)
                .orElseThrow(() -> new IllegalArgumentException("Source department not found"));

        Department toDepartment = departmentRepository.findById(toDepartmentId)
                .orElseThrow(() -> new IllegalArgumentException("Target department not found"));

        for (Long courseId : courseIds) {
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));

            // Verify course belongs to source department
            if (!course.getDepartment().getDepartmentId().equals(fromDepartmentId)) {
                throw new IllegalArgumentException("Course does not belong to source department: " + courseId);
            }

            course.setDepartment(toDepartment);
            courseRepository.save(course);
        }
    }

    // Nested transaction example
    @Transactional
    public Department createDepartmentWithValidation(Department department) {
        // This will participate in the existing transaction
        Department savedDepartment = createDepartment(department);

        // Call a method that requires a new transaction
        validateDepartmentCreation(savedDepartment.getDepartmentId());

        return savedDepartment;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void validateDepartmentCreation(Long departmentId) {
        // This runs in a separate transaction
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new IllegalArgumentException("Department validation failed"));

        // Perform some validation logic
        if (department.getDepartmentName() == null || department.getDepartmentName().trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid department name");
        }

        // Log the creation (this will be committed even if parent transaction rolls back)
        System.out.println("Department validated: " + department.getDepartmentName());
    }

    // Bulk operations with transaction
    @Transactional
    public List<Department> createDepartmentsBatch(List<Department> departments) {
        try {
            return departmentRepository.saveAll(departments);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("One or more departments violate constraints", e);
        }
    }

    @Transactional
    public void updateHeadOfDepartments(List<Long> departmentIds, List<String> newHeads) {
        if (departmentIds.size() != newHeads.size()) {
            throw new IllegalArgumentException("Department IDs and heads count mismatch");
        }

        for (int i = 0; i < departmentIds.size(); i++) {
            int updated = departmentRepository.updateHeadOfDepartment(departmentIds.get(i), newHeads.get(i));
            if (updated == 0) {
                throw new IllegalArgumentException("Department not found: " + departmentIds.get(i));
            }
        }
    }

    // Transaction with custom rollback conditions
    @Transactional(rollbackFor = {IllegalArgumentException.class, IllegalStateException.class})
    public void reorganizeDepartment(Long departmentId, String newName, String newHead, 
                                   List<Long> coursesToAdd, List<Long> coursesToRemove) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new IllegalArgumentException("Department not found"));

        // Update basic info
        department.setDepartmentName(newName);
        department.setHeadOfDepartment(newHead);
        departmentRepository.save(department);

        // Remove courses
        for (Long courseId : coursesToRemove) {
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));
            
            if (!course.getDepartment().getDepartmentId().equals(departmentId)) {
                throw new IllegalStateException("Course doesn't belong to this department");
            }
            
            // For this example, we'll just deactivate the course
            course.setIsActive(false);
            courseRepository.save(course);
        }

        // Add courses (transfer from other departments)
        for (Long courseId : coursesToAdd) {
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));
            
            course.setDepartment(department);
            courseRepository.save(course);
        }
    }

    // Method to demonstrate transaction propagation
    @Transactional(propagation = Propagation.NEVER)
    public List<Department> getActiveDepartmentsNonTransactional() {
        // This method should never run within a transaction
        return departmentRepository.findByIsActiveTrue();
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void updateDepartmentMandatoryTransaction(Long departmentId, String newName) {
        // This method requires an existing transaction
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new IllegalArgumentException("Department not found"));
        
        department.setDepartmentName(newName);
        departmentRepository.save(department);
    }
}