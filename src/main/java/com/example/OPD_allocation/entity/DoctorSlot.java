package com.example.OPD_allocation.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "doctor_slots")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    @JsonBackReference("doctor-slots")
    private Doctor doctor;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "max_capacity", nullable = false)
    private Integer maxCapacity;

    @Column(name = "current_count", nullable = false)
    private Integer currentCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SlotStatus status = SlotStatus.ACTIVE;

    @OneToMany(mappedBy = "slot", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("slot-tokens")
    private List<Token> tokens = new ArrayList<>();

    public enum SlotStatus {
        ACTIVE,
        CANCELLED,
        COMPLETED,
        DELAYED
    }

    // Helper methods
    public boolean isFull() {
        return currentCount >= maxCapacity;
    }

    public boolean canAcceptToken() {
        return status == SlotStatus.ACTIVE && !isFull();
    }

    public void incrementCount() {
        this.currentCount++;
    }

    public void decrementCount() {
        if (this.currentCount > 0) {
            this.currentCount--;
        }
    }

    public String getTimeRange() {
        return startTime + " - " + endTime;
    }
}

