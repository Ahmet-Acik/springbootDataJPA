package com.example.springdatajpa.controller;

import com.example.springdatajpa.entity.Department;
import com.example.springdatajpa.service.DepartmentService;
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
@RequestMapping("/api/departments")
@RequiredArgsConstructor
@Tag(name = "Department Management", description = "APIs for managing departments in the educational system")
public class DepartmentController {

    private final DepartmentService departmentService;

    @Operation(
            summary = "Create a new department",
            description = "Creates a new department record in the system"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Department created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Department.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping
    public ResponseEntity<Department> createDepartment(@RequestBody Department department) {
        Department createdDepartment = departmentService.createDepartment(department);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDepartment);
    }

    @Operation(summary = "Get department by ID", description = "Retrieves a department by its ID")
    @GetMapping("/{id}")
    public ResponseEntity<Department> getDepartmentById(
            @Parameter(description = "Department ID", required = true, example = "1")
            @PathVariable Long id) {
        return departmentService.getDepartmentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get all departments", description = "Retrieves all departments")
    @GetMapping
    public ResponseEntity<List<Department>> getAllDepartments() {
        List<Department> departments = departmentService.getAllDepartments();
        return ResponseEntity.ok(departments);
    }

    @Operation(summary = "Search departments", description = "Search departments by keyword")
    @GetMapping("/search")
    public ResponseEntity<List<Department>> searchDepartments(
            @Parameter(description = "Search keyword", required = true, example = "Computer")
            @RequestParam String keyword) {
        List<Department> departments = departmentService.searchDepartments(keyword);
        return ResponseEntity.ok(departments);
    }

    @Operation(summary = "Get department summaries", description = "Get summary information for all departments")
    @GetMapping("/summaries")
    public ResponseEntity<List<?>> getDepartmentSummaries() {
        var summaries = departmentService.getDepartmentSummaries();
        return ResponseEntity.ok(summaries);
    }

    @Operation(summary = "Update department", description = "Updates an existing department")
    @PutMapping("/{id}")
    public ResponseEntity<Department> updateDepartment(
            @PathVariable Long id,
            @RequestBody Department department) {
        try {
            Department updatedDepartment = departmentService.updateDepartment(id, department);
            return ResponseEntity.ok(updatedDepartment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Delete department", description = "Soft deletes a department")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {
        try {
            departmentService.deleteDepartment(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Get department statistics", description = "Get basic statistics about departments")
    @GetMapping("/stats")
    public ResponseEntity<String> getDepartmentStats() {
        long totalDepartments = departmentService.getAllDepartments().size();
        long activeDepartments = departmentService.getActiveDepartments().size();
        return ResponseEntity.ok(String.format("Total departments: %d, Active departments: %d", 
                totalDepartments, activeDepartments));
    }
}