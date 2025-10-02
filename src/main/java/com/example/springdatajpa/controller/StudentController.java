package com.example.springdatajpa.controller;

import com.example.springdatajpa.entity.Student;
import com.example.springdatajpa.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@Tag(name = "Student Management", description = "Comprehensive APIs for managing students in the educational system")
public class StudentController {

    private final StudentService studentService;

    @Operation(
            summary = "Create a new student",
            description = "Creates a new student record in the system"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Student created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Student.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping
    public ResponseEntity<Student> createStudent(@Valid @RequestBody Student student) {
        try {
            Student createdStudent = studentService.createStudent(student);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdStudent);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Get student by ID", description = "Retrieves a student by their ID")
    @GetMapping("/{id}")
    public ResponseEntity<Student> getStudentById(
            @Parameter(description = "Student ID", required = true, example = "1")
            @PathVariable Long id) {
        return studentService.getStudentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get all students", description = "Retrieves all students")
    @GetMapping
    public ResponseEntity<List<Student>> getAllStudents() {
        List<Student> students = studentService.getAllStudents();
        return ResponseEntity.ok(students);
    }

    @Operation(summary = "Get active students with pagination")
    @GetMapping("/active")
    public ResponseEntity<Page<Student>> getActiveStudents(
            @Parameter(description = "Page number", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("firstName"));
        Page<Student> students = studentService.getActiveStudents(pageable);
        return ResponseEntity.ok(students);
    }

    @Operation(summary = "Update student", description = "Updates an existing student")
    @PutMapping("/{id}")
    public ResponseEntity<Student> updateStudent(
            @PathVariable Long id,
            @RequestBody Student student) {
        try {
            Student updatedStudent = studentService.updateStudent(id, student);
            return ResponseEntity.ok(updatedStudent);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Delete student", description = "Soft deletes a student (marks as inactive)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        try {
            studentService.deleteStudent(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Search students", description = "Search students by various criteria")
    @GetMapping("/search")
    public ResponseEntity<List<Student>> searchStudents(
            @Parameter(description = "First name", example = "John")
            @RequestParam(required = false) String firstName,
            @Parameter(description = "Last name", example = "Doe")
            @RequestParam(required = false) String lastName,
            @Parameter(description = "Email address", example = "john@example.com")
            @RequestParam(required = false) String email,
            @Parameter(description = "Student status", example = "ACTIVE")
            @RequestParam(required = false) String status,
            @Parameter(description = "Minimum GPA", example = "3.0")
            @RequestParam(required = false) BigDecimal minGpa,
            @Parameter(description = "Maximum GPA", example = "4.0")
            @RequestParam(required = false) BigDecimal maxGpa) {
        
        Student.StudentStatus studentStatus = null;
        if (status != null) {
            try {
                studentStatus = Student.StudentStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
        }
        
        List<Student> students = studentService.searchStudents(firstName, lastName, email, studentStatus, minGpa, maxGpa);
        return ResponseEntity.ok(students);
    }

    @Operation(summary = "Get comprehensive student statistics", description = "Get detailed statistics about students")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStudentStats() {
        Map<String, Object> stats = new HashMap<>();
        
        List<Student> allStudents = studentService.getAllStudents();
        stats.put("totalStudents", allStudents.size());
        
        long activeStudents = allStudents.stream()
                .filter(s -> s.getIsActive() != null && s.getIsActive())
                .count();
        stats.put("activeStudents", activeStudents);
        
        long inactiveStudents = allStudents.size() - activeStudents;
        stats.put("inactiveStudents", inactiveStudents);
        
        // Add enrollment statistics
        var enrollmentStats = studentService.getStudentEnrollmentStatistics();
        stats.put("enrollmentStatistics", enrollmentStats);
        
        return ResponseEntity.ok(stats);
    }

    @Operation(summary = "Create multiple students", description = "Create multiple students in a batch operation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Students created successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping("/batch")
    public ResponseEntity<List<Student>> createStudentsBatch(@RequestBody List<Student> students) {
        try {
            List<Student> createdStudents = studentService.createStudentsBatch(students);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdStudents);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Enroll student in course", description = "Enroll a student in a specific course")
    @PostMapping("/{studentId}/enroll")
    public ResponseEntity<Map<String, Object>> enrollStudentInCourse(
            @Parameter(description = "Student ID", required = true, example = "1")
            @PathVariable Long studentId,
            @Parameter(description = "Course ID", required = true, example = "1")
            @RequestParam Long courseId,
            @Parameter(description = "Semester", required = true, example = "Fall 2024")
            @RequestParam String semester,
            @Parameter(description = "Academic Year", required = true, example = "2024")
            @RequestParam Integer academicYear) {
        try {
            studentService.enrollStudentInCourse(studentId, courseId, semester, academicYear);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Student enrolled successfully");
            response.put("studentId", studentId);
            response.put("courseId", courseId);
            response.put("semester", semester);
            response.put("academicYear", academicYear);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Enrollment failed");
            errorResponse.put("message", e.getMessage());
            
            // Return 404 for not found resources, 400 for other validation errors
            if (e.getMessage().contains("not found") || e.getMessage().contains("does not exist")) {
                return ResponseEntity.status(404).body(errorResponse);
            }
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @Operation(summary = "Enroll student in multiple courses", description = "Enroll a student in multiple courses at once")
    @PostMapping("/{studentId}/enroll-multiple")
    public ResponseEntity<String> enrollStudentInMultipleCourses(
            @Parameter(description = "Student ID", required = true, example = "1")
            @PathVariable Long studentId,
            @RequestBody List<Long> courseIds,
            @Parameter(description = "Semester", required = true, example = "Fall 2024")
            @RequestParam String semester,
            @Parameter(description = "Academic Year", required = true, example = "2024")
            @RequestParam Integer academicYear) {
        try {
            studentService.enrollStudentInMultipleCourses(studentId, courseIds, semester, academicYear);
            return ResponseEntity.ok("Student enrolled in " + courseIds.size() + " courses successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Enrollment failed: " + e.getMessage());
        }
    }

    @Operation(summary = "Update grade and recalculate GPA", description = "Updates a grade for a specific enrollment and recalculates student's GPA")
    @PutMapping("/enrollment/{enrollmentId}/grade")
    public ResponseEntity<String> updateGradeAndCalculateGPA(
            @Parameter(description = "Enrollment ID", required = true, example = "1")
            @PathVariable Long enrollmentId,
            @Parameter(description = "Grade", required = true, example = "A")
            @RequestParam String grade,
            @Parameter(description = "Grade Points", required = true, example = "4.0")
            @RequestParam BigDecimal gradePoints) {
        try {
            studentService.updateGradeAndCalculateGPA(enrollmentId, grade, gradePoints);
            return ResponseEntity.ok("Grade updated and GPA recalculated successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Grade update failed: " + e.getMessage());
        }
    }

    @Operation(summary = "Get student enrollment statistics", description = "Get statistics about student enrollments")
    @GetMapping("/enrollment-stats")
    public ResponseEntity<List<?>> getStudentEnrollmentStatistics() {
        var enrollmentStats = studentService.getStudentEnrollmentStatistics();
        return ResponseEntity.ok(enrollmentStats);
    }

    @Operation(summary = "Perform bulk grade update", description = "Update grades for all students in a specific semester")
    @PutMapping("/bulk-grade-update")
    public ResponseEntity<String> performBulkGradeUpdate(
            @Parameter(description = "Semester", required = true, example = "Fall 2024")
            @RequestParam String semester,
            @Parameter(description = "Academic Year", required = true, example = "2024")
            @RequestParam Integer academicYear) {
        try {
            studentService.performBulkGradeUpdate(semester, academicYear);
            return ResponseEntity.ok("Bulk grade update completed successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Bulk grade update failed: " + e.getMessage());
        }
    }
}