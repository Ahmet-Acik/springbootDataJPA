package com.example.springdatajpa.controller;

import com.example.springdatajpa.entity.Course;
import com.example.springdatajpa.service.CourseService;
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

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Tag(name = "Course Management", description = "APIs for managing courses in the educational system")
public class CourseController {

    private final CourseService courseService;

    @Operation(
            summary = "Create a new course",
            description = "Creates a new course record in the system"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Course created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Course.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping
    public ResponseEntity<Course> createCourse(@RequestBody Course course) {
        Course createdCourse = courseService.createCourse(course);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCourse);
    }

    @Operation(summary = "Get course by ID", description = "Retrieves a course by its ID")
    @GetMapping("/{id}")
    public ResponseEntity<Course> getCourseById(
            @Parameter(description = "Course ID", required = true, example = "1")
            @PathVariable Long id) {
        return courseService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get all courses", description = "Retrieves all courses")
    @GetMapping
    public ResponseEntity<List<Course>> getAllCourses() {
        List<Course> courses = courseService.findAllCourses();
        return ResponseEntity.ok(courses);
    }

    @Operation(summary = "Get active courses", description = "Retrieves all active courses")
    @GetMapping("/active")
    public ResponseEntity<List<Course>> getActiveCourses() {
        List<Course> activeCourses = courseService.findActiveCourses();
        return ResponseEntity.ok(activeCourses);
    }

    @Operation(summary = "Get course by code", description = "Finds a course by its course code")
    @GetMapping("/code/{courseCode}")
    public ResponseEntity<Course> getCourseByCode(
            @Parameter(description = "Course code", required = true, example = "CS101")
            @PathVariable String courseCode) {
        return courseService.findByCourseCode(courseCode)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get courses by department", description = "Retrieves courses for a specific department")
    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<Course>> getCoursesByDepartment(
            @Parameter(description = "Department ID", required = true, example = "1")
            @PathVariable Long departmentId) {
        List<Course> courses = courseService.findByDepartment(departmentId);
        return ResponseEntity.ok(courses);
    }

    @Operation(summary = "Update course", description = "Updates an existing course")
    @PutMapping("/{id}")
    public ResponseEntity<Course> updateCourse(
            @PathVariable Long id,
            @RequestBody Course course) {
        if (!courseService.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        course.setCourseId(id);
        Course updatedCourse = courseService.updateCourse(course);
        return ResponseEntity.ok(updatedCourse);
    }

    @Operation(summary = "Activate course", description = "Activates a course")
    @PatchMapping("/{id}/activate")
    public ResponseEntity<Course> activateCourse(@PathVariable Long id) {
        try {
            Course activatedCourse = courseService.activateCourse(id);
            return ResponseEntity.ok(activatedCourse);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Deactivate course", description = "Deactivates a course")
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Course> deactivateCourse(@PathVariable Long id) {
        try {
            Course deactivatedCourse = courseService.deactivateCourse(id);
            return ResponseEntity.ok(deactivatedCourse);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Delete course", description = "Deletes a course")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        try {
            courseService.deleteCourse(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Get course statistics", description = "Get basic statistics about courses")
    @GetMapping("/stats")
    public ResponseEntity<String> getCourseStats() {
        long totalCourses = courseService.findAllCourses().size();
        long activeCourses = courseService.countActiveCourses();
        return ResponseEntity.ok(String.format("Total courses: %d, Active courses: %d", 
                totalCourses, activeCourses));
    }
}