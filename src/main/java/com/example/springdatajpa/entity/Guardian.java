package com.example.springdatajpa.entity;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@AttributeOverrides({
        @AttributeOverride(
                name = "name",
                column = @Column(name = "guardianName")
        ),
        @AttributeOverride(
                name = "email",
                column = @Column(name = "guardianEmail")
        ), @AttributeOverride(
        name = "mobile",
        column = @Column(name = "guardianMobileNumber")
)}
)

public class Guardian {

    @NotBlank(message = "Guardian name is required")
    @Size(min = 2, max = 100, message = "Guardian name must be between 2 and 100 characters")
    private String name; //guardianName;
    
    @Email(message = "Guardian email should be valid")
    @Size(max = 100, message = "Guardian email must not exceed 100 characters")
    private String email; //guardianEmail;
    
    @Pattern(regexp = "^[+]?[(]?[\\d\\s\\-()]{7,15}$", message = "Guardian mobile number should be valid")
    @Size(max = 20, message = "Guardian mobile number must not exceed 20 characters")
    private String mobile; //guardianMobileNumber;
}
