package com.example.OPD_allocation.dto;

import jakarta.validation.constraints.*;
import lombok.Data;


@Data
public class PatientDto {
    private Long patientId;

    @NotBlank(message="name is required")
    private String name;

    @NotBlank(message="Contact is required")
    @Pattern(regexp="^[0-9]{10}",message = "Contact must be 10 digit")
    private String contact;

    @NotNull(message = "Age is required")
    @Min(value=1,message="Age must be positive")
    @Max(value=120,message="Invalid age")
    private Integer age;

    @NotBlank(message="Gender is required")
    private String gender;
}
