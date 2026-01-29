package com.example.OPD_allocation.repository;

import com.example.OPD_allocation.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {
    Optional<Token> findByTokenNumber(String tokenNumber);
    List<Token> findByTimeSlotOrderByQueuePositionAsc(TimeSlot timeSlot);
    List<Token> findByTimeSlotAndStatusOrderByQueuePositionAsc(TimeSlot timeSlot, TokenStatus status);
    List<Token> findByPatient(Patient patient);

    @Query("SELECT t FROM Token t WHERE t.patient.patientId = :patientId AND t.timeSlot.slotDate = :date AND t.status = 'ACTIVE'")
    List<Token> findActiveTokensForPatientOnDate(@Param("patientId") Long patientId, @Param("date") LocalDate date);

    @Query("SELECT COUNT(t) FROM Token t WHERE t.timeSlot.slotId = :slotId AND t.status = 'ACTIVE'")
    Long countActiveTokensInSlot(@Param("slotId") Long slotId);

    List<Token> findByPriorityLevelAndStatus(PriorityLevel priorityLevel, TokenStatus status);
    List<Token> findByIsEmergencyTrueAndStatus(TokenStatus status);

    @Query("SELECT COALESCE(MAX(t.queuePosition), 0) + 1 FROM Token t WHERE t.timeSlot.slotId = :slotId AND t.status = 'ACTIVE'")
    Integer getNextQueuePosition(@Param("slotId") Long slotId);
}