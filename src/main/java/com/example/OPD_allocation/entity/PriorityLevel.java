package com.example.OPD_allocation.entity;

public enum PriorityLevel {
    EMERGENCY(1),      // Highest priority
    PAID_PRIORITY(2),  // Paid fast-track
    FOLLOW_UP(3),      // Follow-up visits
    ONLINE(4),         // Online bookings
    WALK_IN(5);        // Lowest priority
    private final int value;
    PriorityLevel(int value){
        this.value=value;
    }
    public int getValue() {
        return value;
    }
}
