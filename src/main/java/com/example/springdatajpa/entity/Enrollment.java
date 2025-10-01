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
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(
    name = "tbl_enrollment",
    uniqueConstraints = @UniqueConstraint(
        name = "enrollment_unique",
        columnNames = {"student_id", "course_id", "semester", "academic_year"}
    ),
    indexes = {
        @Index(name = "idx_student_id", columnList = "student_id"),
        @Index(name = "idx_course_id", columnList = "course_id"),
        @Index(name = "idx_enrollment_date", columnList = "enrollment_date")
    }
)
@EntityListeners(AuditingEntityListener.class)
@NamedQueries({
        @NamedQuery(
                name = "Enrollment.findByStudentAndSemester",
                query = "SELECT e FROM Enrollment e WHERE e.student.studentId = :studentId AND e.semester = :semester"
        ),
        @NamedQuery(
                name = "Enrollment.findByCourseAndAcademicYear",
                query = "SELECT e FROM Enrollment e WHERE e.course.courseId = :courseId AND e.academicYear = :academicYear"
        )
})
public class Enrollment {
    
    @Id
    @SequenceGenerator(
            name = "enrollment_sequence",
            sequenceName = "enrollment_sequence",
            allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "enrollment_sequence")
    private Long enrollmentId;
    
    // Many-to-One relationship with Student
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "student_id",
            nullable = false,
            referencedColumnName = "studentId"
    )
    private Student student;
    
    // Many-to-One relationship with Course
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "course_id",
            nullable = false,
            referencedColumnName = "courseId"
    )
    private Course course;
    
    @Column(name = "enrollment_date", nullable = false)
    @Temporal(TemporalType.DATE)
    private LocalDate enrollmentDate;
    
    @Column(name = "semester", nullable = false, length = 20)
    private String semester; // e.g., "Fall 2024", "Spring 2025"
    
    @Column(name = "academic_year", nullable = false)
    private Integer academicYear;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "enrollment_status")
    @Builder.Default
    private EnrollmentStatus enrollmentStatus = EnrollmentStatus.ACTIVE;
    
    @Column(name = "grade", length = 5)
    private String grade; // A+, A, B+, B, C+, C, D, F
    
    @Column(name = "grade_points", precision = 4, scale = 2)
    private BigDecimal gradePoints; // GPA points for this course
    
    @Column(name = "attendance_percentage", precision = 5, scale = 2)
    private BigDecimal attendancePercentage;
    
    // Auditing fields
    @CreatedDate
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;
    
    @LastModifiedDate
    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;
    
    // Enum for Enrollment Status
    public enum EnrollmentStatus {
        ACTIVE,
        COMPLETED,
        DROPPED,
        WITHDRAWN,
        FAILED
    }
}