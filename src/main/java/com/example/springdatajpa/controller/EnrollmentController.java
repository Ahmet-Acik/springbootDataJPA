package com.example.springdatajpa.controller;

import com.example.springdatajpa.entity.Enrollment;
import com.example.springdatajpa.service.EnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
@Tag(name = "Enrollment Management", description = "APIs for managing student course enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @Operation(
            summary = "Create a new enrollment",
            description = "Enrolls a student in a course"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Enrollment created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Enrollment.class))),
            @ApiResponse(responseCode = "400", description = "Invalid enrollment data",
                    content = @Content),
            @ApiResponse(responseCode = "409", description = "Enrollment already exists",
                    content = @Content)
    })
    @PostMapping
    public ResponseEntity<Enrollment> createEnrollment(@Valid @RequestBody Enrollment enrollment) {
        try {
            Enrollment createdEnrollment = enrollmentService.createEnrollment(enrollment);
            return new ResponseEntity<>(createdEnrollment, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @Operation(summary = "Get all enrollments", description = "Retrieves all student enrollments")
    @GetMapping
    public ResponseEntity<List<Enrollment>> getAllEnrollments() {
        List<Enrollment> enrollments = enrollmentService.findAllEnrollments();
        return ResponseEntity.ok(enrollments);
    }

    @Operation(summary = "Get enrollment by ID", description = "Retrieves a specific enrollment by its ID")
    @GetMapping("/{id}")
    public ResponseEntity<Enrollment> getEnrollmentById(
            @Parameter(description = "Enrollment ID", required = true, example = "1")
            @PathVariable Long id) {
        return enrollmentService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get enrollments by student", description = "Retrieves all enrollments for a specific student")
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Enrollment>> getEnrollmentsByStudent(
            @Parameter(description = "Student ID", required = true, example = "1")
            @PathVariable Long studentId) {
        List<Enrollment> enrollments = enrollmentService.findByStudent(studentId);
        return ResponseEntity.ok(enrollments);
    }

    @Operation(summary = "Get enrollments by course", description = "Retrieves all enrollments for a specific course")
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<Enrollment>> getEnrollmentsByCourse(
            @Parameter(description = "Course ID", required = true, example = "1")
            @PathVariable Long courseId) {
        List<Enrollment> enrollments = enrollmentService.findByCourse(courseId);
        return ResponseEntity.ok(enrollments);
    }

    @Operation(summary = "Update enrollment grade", description = "Updates the grade for a specific enrollment")
    @PutMapping("/{id}/grade")
    public ResponseEntity<Enrollment> updateEnrollmentGrade(
            @Parameter(description = "Enrollment ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "New grade", required = true, example = "A")
            @RequestParam String grade,
            @Parameter(description = "Grade points", required = true, example = "4.0")
            @RequestParam BigDecimal gradePoints) {
        try {
            Enrollment updatedEnrollment = enrollmentService.updateGrade(id, grade, gradePoints);
            return ResponseEntity.ok(updatedEnrollment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Drop enrollment", description = "Removes a student's enrollment from a course")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> dropEnrollment(
            @Parameter(description = "Enrollment ID", required = true, example = "1")
            @PathVariable Long id) {
        try {
            enrollmentService.dropEnrollment(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Get active enrollments", description = "Retrieves all active enrollments")
    @GetMapping("/active")
    public ResponseEntity<List<Enrollment>> getActiveEnrollments() {
        List<Enrollment> activeEnrollments = enrollmentService.findByStatus(Enrollment.EnrollmentStatus.ACTIVE);
        return ResponseEntity.ok(activeEnrollments);
    }

    @Operation(summary = "Get graded enrollments", description = "Gets all enrollments that have been graded")
    @GetMapping("/graded")
    public ResponseEntity<List<Enrollment>> getGradedEnrollments() {
        List<Enrollment> gradedEnrollments = enrollmentService.findGradedEnrollments();
        return ResponseEntity.ok(gradedEnrollments);
    }

    @Operation(summary = "Enroll student in course", description = "Enrolls a student in a specific course")
    @PostMapping("/enroll")
    public ResponseEntity<Enrollment> enrollStudentInCourse(
            @Parameter(description = "Student ID", required = true, example = "1")
            @RequestParam Long studentId,
            @Parameter(description = "Course ID", required = true, example = "1")
            @RequestParam Long courseId,
            @Parameter(description = "Semester", required = true, example = "Fall 2024")
            @RequestParam String semester,
            @Parameter(description = "Academic Year", required = true, example = "2024")
            @RequestParam Integer academicYear) {
        try {
            Enrollment enrollment = enrollmentService.enrollStudentInCourse(studentId, courseId, semester, academicYear);
            return ResponseEntity.ok(enrollment);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}