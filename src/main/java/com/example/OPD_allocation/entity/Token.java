package com.example.OPD_allocation.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name="tokens")
@Data
public class Token {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="token_id")
    private Long tokenId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="patient_id",nullable=false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="doctor_id",nullable=false)
    private Doctor doctor;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="slot_id",nullable=false)
    private TimeSlot timeSlot;
    @Column(name="token_number",nullable=false,unique = true,length = 20)
    private String tokenNumber;
    @Enumerated(EnumType.STRING)
    @Column(name="priority_level",nullable=false,length = 20)
    private PriorityLevel  priorityLevel;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TokenStatus status = TokenStatus.ACTIVE;
    @Column(name = "is_emergency", nullable = false)
    private Boolean isEmergency = false;
    @Column(name = "queue_position", nullable = false)
    private Integer queuePosition;
    @Column(name = "estimated_time")
    private LocalDateTime estimatedTime;
    @Column(name="actual_service_time")
    private LocalDateTime actualServiceTime;
    @Column(length = 1000)
    private String notes;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "token", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TokenReallocation> reallocations;
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }


//this part below is getter and setter

    public Long getTokenId() {
        return tokenId;
    }

    public void setTokenId(Long tokenId) {
        this.tokenId = tokenId;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public TimeSlot getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(TimeSlot timeSlot) {
        this.timeSlot = timeSlot;
    }

    public String getTokenNumber() {
        return tokenNumber;
    }

    public void setTokenNumber(String tokenNumber) {
        this.tokenNumber = tokenNumber;
    }

    public PriorityLevel getPriorityLevel() {
        return priorityLevel;
    }

    public void setPriorityLevel(PriorityLevel priorityLevel) {
        this.priorityLevel = priorityLevel;
    }

    public TokenStatus getStatus() {
        return status;
    }

    public void setStatus(TokenStatus status) {
        this.status = status;
    }

    public Boolean getEmergency() {
        return isEmergency;
    }

    public void setEmergency(Boolean emergency) {
        isEmergency = emergency;
    }

    public Integer getQueuePosition() {
        return queuePosition;
    }

    public void setQueuePosition(Integer queuePosition) {
        this.queuePosition = queuePosition;
    }

    public LocalDateTime getEstimatedTime() {
        return estimatedTime;
    }

    public void setEstimatedTime(LocalDateTime estimatedTime) {
        this.estimatedTime = estimatedTime;
    }

    public LocalDateTime getActualServiceTime() {
        return actualServiceTime;
    }

    public void setActualServiceTime(LocalDateTime actualServiceTime) {
        this.actualServiceTime = actualServiceTime;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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

    public List<TokenReallocation> getReallocations() {
        return reallocations;
    }

    public void setReallocations(List<TokenReallocation> reallocations) {
        this.reallocations = reallocations;
    }
}
