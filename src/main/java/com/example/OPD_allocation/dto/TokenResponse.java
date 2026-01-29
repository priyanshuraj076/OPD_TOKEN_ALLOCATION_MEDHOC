package com.example.OPD_allocation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponse {
    private String tokenNumber;
    private String doctorName;
    private String date;
    private String slotTime;
    private String estimatedWaitTime;
    private Integer positionInQueue;
    private String status;
}

