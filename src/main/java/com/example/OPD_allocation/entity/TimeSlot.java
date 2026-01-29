package com.example.OPD_allocation.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Table(name="time_slot")
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="slot_id")
    private Long slotId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="doctor_id",nullable=false)
    private Doctor doctor;
    @Column(name="slot_date",nullable = false)
    private LocalDate slotDate;
    @Column(name="start_time",nullable = false)
    private LocalTime startTime;
    @Column(name="end_time",nullable = false)
    private LocalTime endTime;
    @Column(name="max_tokens",nullable = false)
    private Integer maxTokens=6;
    @Column(name="current_tokens",nullable = false)
    private Integer currentTokens=0;
    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = true;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "timeSlot", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Token> tokens;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    public boolean isFull() {
        return currentTokens >= maxTokens;
    }
    public boolean canAcceptToken() {
        return isAvailable && !isFull();
    }
    public void incrementTokenCount() {
        this.currentTokens++;
    }

    public void decrementTokenCount() {
        if (this.currentTokens > 0) {
            this.currentTokens--;
        }
    }
    //as we know loombook is not working so here i am addin manually


    public Long getSlotId() {
        return slotId;
    }

    public void setSlotId(Long slotId) {
        this.slotId = slotId;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public LocalDate getSlotDate() {
        return slotDate;
    }

    public void setSlotDate(LocalDate slotDate) {
        this.slotDate = slotDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public Integer getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }

    public Integer getCurrentTokens() {
        return currentTokens;
    }

    public void setCurrentTokens(Integer currentTokens) {
        this.currentTokens = currentTokens;
    }

    public Boolean getAvailable() {
        return isAvailable;
    }

    public void setAvailable(Boolean available) {
        isAvailable = available;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<Token> getTokens() {
        return tokens;
    }

    public void setTokens(List<Token> tokens) {
        this.tokens = tokens;
    }
}
