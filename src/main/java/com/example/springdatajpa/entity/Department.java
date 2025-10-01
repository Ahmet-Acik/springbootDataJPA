package com.example.springdatajpa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "tbl_department")
@EntityListeners(AuditingEntityListener.class)
@NamedQueries({
        @NamedQuery(
                name = "Department.findByDepartmentNameIgnoreCase",
                query = "SELECT d FROM Department d WHERE UPPER(d.departmentName) = UPPER(:departmentName)"
        ),
        @NamedQuery(
                name = "Department.findActiveDepartments",
                query = "SELECT d FROM Department d WHERE d.isActive = true"
        )
})
public class Department {
    
    @Id
    @SequenceGenerator(
            name = "department_sequence",
            sequenceName = "department_sequence",
            allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "department_sequence")
    private Long departmentId;
    
    @Column(name = "department_name", nullable = false, length = 100)
    private String departmentName;
    
    @Column(name = "department_address", length = 255)
    private String departmentAddress;
    
    @Column(name = "department_code", unique = true, length = 10)
    private String departmentCode;
    
    @Column(name = "head_of_department", length = 100)
    private String headOfDepartment;
    
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "department_type")
    private DepartmentType departmentType;
    
    // One-to-Many relationship with Course
    @OneToMany(
            mappedBy = "department",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY
    )
    private List<Course> courses;
    
    // Auditing fields
    @CreatedDate
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;
    
    @LastModifiedDate
    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;
    
    // Enum for Department Type
    public enum DepartmentType {
        SCIENCE,
        ARTS,
        COMMERCE,
        ENGINEERING,
        MEDICAL,
        LAW
    }
}