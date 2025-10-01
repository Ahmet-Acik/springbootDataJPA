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
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(
        name = "tbl_student",
        uniqueConstraints = @UniqueConstraint(
                name = "emailid_unique",
                columnNames = "email_address"
        ),
        indexes = {
                @Index(name = "idx_student_name", columnList = "first_name, last_name"),
                @Index(name = "idx_student_email", columnList = "email_address"),
                @Index(name = "idx_admission_date", columnList = "admission_date")
        }
)
@EntityListeners(AuditingEntityListener.class)
@NamedQueries({
        @NamedQuery(
                name = "Student.findByFirstNameContaining",
                query = "SELECT s FROM Student s WHERE s.firstName LIKE CONCAT('%', :firstName, '%')"
        ),
        @NamedQuery(
                name = "Student.findActiveStudents",
                query = "SELECT s FROM Student s WHERE s.isActive = true"
        )
})
public class Student {
    
    @Id
    @SequenceGenerator(
            name = "student_sequence",
            sequenceName = "student_sequence",
            allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "student_sequence")
    private Long studentId;
    
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;
    
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(name = "email_address", nullable = false, unique = true, length = 100)
    private String emailId;
    
    @Column(name = "student_id_number", unique = true, length = 20)
    private String studentIdNumber;
    
    @Column(name = "admission_date")
    @Temporal(TemporalType.DATE)
    private LocalDate admissionDate;
    
    @Column(name = "date_of_birth")
    @Temporal(TemporalType.DATE)
    private LocalDate dateOfBirth;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "student_status")
    @Builder.Default
    private StudentStatus studentStatus = StudentStatus.ACTIVE;
    
    @Column(name = "gpa", precision = 4, scale = 2)
    private BigDecimal gpa;
    
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Embedded
    private Guardian guardian;
    
    // One-to-Many relationship with Enrollment
    @OneToMany(
            mappedBy = "student",
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
    
    // Enum for Student Status
    public enum StudentStatus {
        ACTIVE,
        INACTIVE,
        GRADUATED,
        SUSPENDED,
        EXPELLED
    }
}
