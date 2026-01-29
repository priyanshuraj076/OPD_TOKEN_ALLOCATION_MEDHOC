package com.example.OPD_allocation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReallocationResponse {
    private Integer reallocatedCount;
    private Integer notificationsSent;
    private List<ReallocationDetail> details;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReallocationDetail {
        private String tokenNumber;
        private String fromSlot;
        private String toSlot;
    }
}

