package com.example.OPD_allocation.repository;

import com.example.OPD_allocation.entity.DoctorSlot;
import com.example.OPD_allocation.entity.Token;
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

    List<Token> findBySlotIdOrderByCreatedAt(Long slotId);

    List<Token> findBySlotAndStatusOrderByCreatedAtAsc(DoctorSlot slot, Token.TokenStatus status);

    @Query("SELECT t FROM Token t WHERE t.patientPhone = :phone AND t.doctor.id = :doctorId " +
           "AND t.slot.date = :date AND t.status IN ('WAITING', 'IN_CONSULTATION')")
    List<Token> findActiveTokensForPatient(@Param("phone") String phone,
                                           @Param("doctorId") Long doctorId,
                                           @Param("date") LocalDate date);

    @Query("SELECT t FROM Token t WHERE t.slot.id = :slotId AND t.status = 'WAITING' " +
           "ORDER BY t.tokenType, t.createdAt")
    List<Token> findWaitingTokensBySlotOrderedByPriority(@Param("slotId") Long slotId);

    @Query("SELECT t FROM Token t WHERE t.slot.id = :slotId " +
           "ORDER BY CASE t.tokenType " +
           "WHEN 'EMERGENCY' THEN 1 " +
           "WHEN 'PAID_PRIORITY' THEN 2 " +
           "WHEN 'FOLLOWUP' THEN 3 " +
           "WHEN 'ONLINE' THEN 4 " +
           "WHEN 'WALKIN' THEN 5 END, t.createdAt")
    List<Token> findTokensBySlotOrderedByPriority(@Param("slotId") Long slotId);

    @Query("SELECT COUNT(t) FROM Token t WHERE t.slot.id = :slotId AND t.status = 'WAITING'")
    Long countWaitingTokens(@Param("slotId") Long slotId);

    @Query("SELECT t FROM Token t WHERE t.status = 'WAITING_LIST' AND t.doctor.id = :doctorId " +
           "AND t.slot.date = :date ORDER BY t.createdAt")
    List<Token> findWaitingListTokens(@Param("doctorId") Long doctorId, @Param("date") LocalDate date);
}
