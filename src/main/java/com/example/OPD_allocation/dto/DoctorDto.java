package com.example.OPD_allocation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DoctorDto {
    private Long doctorId;
    @NotBlank(message = "Name is required")
    private String name;

}
