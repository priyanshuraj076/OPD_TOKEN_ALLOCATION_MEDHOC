package com.example.OPD_allocation.dto;

import lombok.Data;

import java.util.List;

@Data
public class QueueStatusResponse {
    private Long slotId;
    private String doctorName;
    private String slotTime;
    private Integer totalTokens;
    private Integer activeTokens;
    private List<TokenResponse> queue;
}
