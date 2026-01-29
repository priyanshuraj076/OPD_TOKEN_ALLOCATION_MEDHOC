package com.example.OPD_allocation.dto;

import com.example.OPD_allocation.entity.PriorityLevel;
import jakarta.validation.constraints.NotNull;

public class TokenAllocationRequest {
    @NotNull(message = "Patient ID is required")
    private Long patientId;

    @NotNull(message = "Doctor ID is required")
    private Long doctorId;

    @NotNull(message = "Priority level is required")
    private PriorityLevel priorityLevel;

    private Boolean isEmergency = false;
    private String notes;
}
