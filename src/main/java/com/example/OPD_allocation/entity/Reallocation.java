package com.example.OPD_allocation.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "reallocations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reallocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "token_id", nullable = false)
    @JsonBackReference("token-reallocations")
    private Token token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "old_slot_id", nullable = false)
    private DoctorSlot oldSlot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "new_slot_id", nullable = false)
    private DoctorSlot newSlot;

    @Column(nullable = false, length = 200)
    private String reason;

    @Column(name = "reallocated_at", nullable = false, updatable = false)
    private LocalDateTime reallocatedAt;

    @PrePersist
    protected void onCreate() {
        reallocatedAt = LocalDateTime.now();
    }
}

