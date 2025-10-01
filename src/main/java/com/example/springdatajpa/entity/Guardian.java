package com.example.springdatajpa.entity;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
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

    private String name; //guardianName;
    private String email; //guardianEmail;
    private String mobile; //guardianMobileNumber;
}
