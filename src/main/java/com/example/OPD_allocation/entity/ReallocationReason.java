package com.example.OPD_allocation.entity;

public enum ReallocationReason {
    DOCTOR_DELAY("Doctor Running Late"),
    SLOT_FULL("Slot Reached Maximum Capacity"),
    EMERGENCY_BUMPED("Bumped by Emergency Patient"),
    PATIENT_REQUEST("Patient Requested Time Change"),
    DOCTOR_UNAVAILABLE("Doctor Became Unavailable"),
    SYSTEM_AUTO("System Auto-Reallocation"),
    ADMIN_MANUAL("Manual Admin Intervention");

    private final String description;

    ReallocationReason(String description) {
        this.description = description;
    }
    public String getDescription() {
        return description;
    }
}
