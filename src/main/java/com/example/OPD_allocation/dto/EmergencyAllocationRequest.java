package com.example.OPD_allocation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyAllocationRequest {
    private String patientName;
    private String patientPhone;
    private Long doctorId;
    private String date;  // "2026-01-28"
}

