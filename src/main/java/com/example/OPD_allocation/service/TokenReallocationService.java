package com.example.OPD_allocation.service;

import com.example.OPD_allocation.entity.*;
import com.example.OPD_allocation.repository.TokenReallocationRepository;
import com.example.OPD_allocation.repository.TokenRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class TokenReallocationService {

    @Autowired
    private TokenReallocationRepository reallocationRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private TimeSlotService timeSlotService;


    // Log a reallocation event
    public TokenReallocation logReallocation(Token token, TimeSlot oldSlot, TimeSlot newSlot,
                                             ReallocationReason reason,
                                             Integer oldPosition, Integer newPosition) {

        TokenReallocation reallocation = new TokenReallocation();
        reallocation.setToken(token);
        reallocation.setOldSlot(oldSlot);
        reallocation.setNewSlot(newSlot);
        reallocation.setReason(reason);
        reallocation.setOldQueuePosition(oldPosition);
        reallocation.setNewQueuePosition(newPosition);

        return reallocationRepository.save(reallocation);
    }

    // Handle Doctor Delay - Move all tokens to later slots
    @Transactional
    public void handleDoctorDelay(Long doctorId, LocalDate date, int delayMinutes) {

        List<TimeSlot> affectedSlots = timeSlotService.getAvailableSlotsForDoctor(doctorId, date);

        for (TimeSlot slot : affectedSlots) {
            List<Token> tokens = tokenRepository.findByTimeSlotAndStatusOrderByQueuePositionAsc(
                    slot, TokenStatus.ACTIVE);

            if (tokens.isEmpty()) continue;

            // Find new slot (delayed by delayMinutes)
            LocalTime newStartTime = slot.getStartTime().plusMinutes(delayMinutes);
            TimeSlot newSlot = findOrCreateSlot(doctorId, date, newStartTime);

            // Move all tokens to new slot
            for (Token token : tokens) {
                int oldPosition = token.getQueuePosition();
                token.setTimeSlot(newSlot);
                token.setStatus(TokenStatus.REALLOCATED);
                tokenRepository.save(token);

                logReallocation(token, slot, newSlot,
                        ReallocationReason.DOCTOR_DELAY,
                        oldPosition, oldPosition);
            }

            // Update slot counts
            slot.setCurrentTokens(0);
            timeSlotService.createTimeSlot(slot);

            newSlot.setCurrentTokens(newSlot.getCurrentTokens() + tokens.size());
            timeSlotService.createTimeSlot(newSlot);
        }
    }

    // Find or create a time slot
    private TimeSlot findOrCreateSlot(Long doctorId, LocalDate date, LocalTime startTime) {
        Doctor doctor = new Doctor();
        doctor.setDoctorId(doctorId);

        return timeSlotService.getAvailableSlotsForDoctor(doctorId, date).stream()
                .filter(s -> s.getStartTime().equals(startTime))
                .findFirst()
                .orElseGet(() -> {
                    TimeSlot newSlot = new TimeSlot();
                    newSlot.setDoctor(doctor);
                    newSlot.setSlotDate(date);
                    newSlot.setStartTime(startTime);
                    newSlot.setEndTime(startTime.plusMinutes(15));
                    newSlot.setMaxTokens(4);
                    newSlot.setCurrentTokens(0);
                    newSlot.setAvailable(true);
                    return timeSlotService.createTimeSlot(newSlot);
                });
    }

    // Get reallocation history for a token
    public List<TokenReallocation> getTokenHistory(Long tokenId) {
        Token token = new Token();
        token.setTokenId(tokenId);
        return reallocationRepository.findByTokenOrderByReallocationTimeDesc(token);
    }
}
