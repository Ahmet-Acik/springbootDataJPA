package com.example.springdatajpa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(
    name = "tbl_course",
    indexes = {
        @Index(name = "idx_course_code", columnList = "course_code"),
        @Index(name = "idx_course_title", columnList = "title"),
        @Index(name = "idx_department_id", columnList = "department_id")
    }
)
@EntityListeners(AuditingEntityListener.class)
@NamedQueries({
        @NamedQuery(
                name = "Course.findByDepartmentId",
                query = "SELECT c FROM Course c WHERE c.department.departmentId = :departmentId"
        ),
        @NamedQuery(
                name = "Course.findByCreditHoursGreaterThan",
                query = "SELECT c FROM Course c WHERE c.creditHours > :creditHours"
        )
})
public class Course {
    
    @Id
    @SequenceGenerator(
            name = "course_sequence",
            sequenceName = "course_sequence",
            allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "course_sequence")
    private Long courseId;
    
    @Column(name = "title", nullable = false, length = 200)
    private String title;
    
    @Column(name = "course_code", unique = true, nullable = false, length = 20)
    private String courseCode;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "credit_hours", precision = 3, scale = 1)
    private BigDecimal creditHours;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "course_level")
    private CourseLevel courseLevel;
    
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
    
    // Many-to-One relationship with Department
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "department_id",
            nullable = false,
            referencedColumnName = "departmentId"
    )
    private Department department;
    
    // Many-to-Many relationship with Student through Enrollment
    @OneToMany(
            mappedBy = "course",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY
    )
    private List<Enrollment> enrollments;
    
    // Auditing fields
    @CreatedDate
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;
    
    @LastModifiedDate
    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;
    
    // Enum for Course Level
    public enum CourseLevel {
        BEGINNER,
        INTERMEDIATE,
        ADVANCED,
        EXPERT
    }
}