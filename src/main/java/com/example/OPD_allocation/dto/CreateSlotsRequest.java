package com.example.OPD_allocation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSlotsRequest {
    private LocalDate date;
    private List<SlotInfo> slots;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SlotInfo {
        private String startTime;  // "09:00"
        private String endTime;    // "10:00"
        private Integer maxCapacity;
    }
}

