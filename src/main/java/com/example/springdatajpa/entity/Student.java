package com.example.springdatajpa.entity;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import com.fasterxml.jackson.annotation.JsonManagedReference;

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
    
    @NotBlank(message = "First name is required")
    @Size(min = 1, max = 50, message = "First name must be between 1 and 50 characters")
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(min = 1, max = 50, message = "Last name must be between 1 and 50 characters")
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    @Column(name = "email_address", nullable = false, unique = true, length = 100)
    private String emailId;
    
    @Size(max = 20, message = "Student ID number must not exceed 20 characters")
    @Column(name = "student_id_number", unique = true, length = 20)
    private String studentIdNumber;
    
    @PastOrPresent(message = "Admission date must be in the past or present")
    @Column(name = "admission_date")
    @Temporal(TemporalType.DATE)
    private LocalDate admissionDate;
    
    @Past(message = "Date of birth must be in the past")
    @Column(name = "date_of_birth")
    @Temporal(TemporalType.DATE)
    private LocalDate dateOfBirth;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "student_status")
    @Builder.Default
    private StudentStatus studentStatus = StudentStatus.ACTIVE;
    
    @DecimalMin(value = "0.0", message = "GPA must be at least 0.0")
    @DecimalMax(value = "4.0", message = "GPA must not exceed 4.0")
    @Digits(integer = 2, fraction = 2, message = "GPA must have at most 2 integer digits and 2 fractional digits")
    @Column(name = "gpa", precision = 4, scale = 2)
    private BigDecimal gpa;
    
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Valid
    @Embedded
    private Guardian guardian;
    
    // One-to-Many relationship with Enrollment
    @OneToMany(
            mappedBy = "student",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY
    )
    @JsonManagedReference
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
