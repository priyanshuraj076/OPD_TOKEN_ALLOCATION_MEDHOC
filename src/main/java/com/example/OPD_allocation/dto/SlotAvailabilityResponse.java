package com.example.OPD_allocation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SlotAvailabilityResponse {
    private DoctorInfo doctor;
    private String date;
    private List<SlotInfo> slots;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DoctorInfo {
        private Long id;
        private String name;
        private String specialization;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SlotInfo {
        private Long slotId;
        private String time;
        private Integer maxCapacity;
        private Integer booked;
        private Integer available;
        private String status;
    }
}

