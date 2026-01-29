package com.example.OPD_allocation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class TimeSlotDto {
    private Long slotId;

    @NotNull(message = "Doctor ID is required")
    private Long doctorId;

    @NotNull(message = "Slot date is required")
    private LocalDate slotDate;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    private Integer maxTokens = 6;
    private Integer currentTokens = 0;
    private Boolean isAvailable = true;
}
