package com.example.OPD_allocation.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name="token_reallocations")
@Data
public class TokenReallocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="reallocation_id")
    private Long reallocationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="token_id",nullable=false)
    private Token token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="old_slot_id",nullable=false)
    private TimeSlot oldSlot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="new_slot_id",nullable=false)
    private TimeSlot newSlot;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false, length = 50)
    private ReallocationReason reason;

    @Column(name = "old_queue_position")
    private Integer oldQueuePosition;

    @Column(name = "new_queue_position")
    private Integer newQueuePosition;
    @Column(name = "reallocation_time", nullable = false, updatable = false)
    private LocalDateTime reallocationTime;

    // Lifecycle callback
    @PrePersist
    protected void onCreate() {
        reallocationTime = LocalDateTime.now();
    }

    public Long getReallocationId() {
        return reallocationId;
    }

    public void setReallocationId(Long reallocationId) {
        this.reallocationId = reallocationId;
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public TimeSlot getOldSlot() {
        return oldSlot;
    }

    public void setOldSlot(TimeSlot oldSlot) {
        this.oldSlot = oldSlot;
    }

    public TimeSlot getNewSlot() {
        return newSlot;
    }

    public void setNewSlot(TimeSlot newSlot) {
        this.newSlot = newSlot;
    }

    public ReallocationReason getReason() {
        return reason;
    }

    public void setReason(ReallocationReason reason) {
        this.reason = reason;
    }

    public Integer getOldQueuePosition() {
        return oldQueuePosition;
    }

    public void setOldQueuePosition(Integer oldQueuePosition) {
        this.oldQueuePosition = oldQueuePosition;
    }

    public Integer getNewQueuePosition() {
        return newQueuePosition;
    }

    public void setNewQueuePosition(Integer newQueuePosition) {
        this.newQueuePosition = newQueuePosition;
    }

    public LocalDateTime getReallocationTime() {
        return reallocationTime;
    }

    public void setReallocationTime(LocalDateTime reallocationTime) {
        this.reallocationTime = reallocationTime;
    }
}
