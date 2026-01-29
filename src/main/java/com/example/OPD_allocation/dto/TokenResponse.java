package com.example.OPD_allocation.dto;

import com.example.OPD_allocation.entity.PriorityLevel;
import com.example.OPD_allocation.entity.TokenStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TokenResponse {
    private Long tokenId;
    private String tokenNumber;
    private Long patientId;
    private String patientName;
    private Long doctorId;
    private String doctorName;
    private Long slotId;
    private LocalDateTime estimatedTime;
    private PriorityLevel priorityLevel;
    private TokenStatus status;
    private Integer queuePosition;
    private Boolean isEmergency;
    private String notes;
}
