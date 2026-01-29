package com.example.OPD_allocation.service;

import com.example.OPD_allocation.entity.*;
import com.example.OPD_allocation.repository.TokenRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TokenService {
    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private PatientService patientService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private TimeSlotService timeSlotService;

    @Autowired
    private TokenReallocationService reallocationService;

    @Transactional
    public Token allocateToken(Long patientId, Long doctorId, PriorityLevel priorityLevel,
                               Boolean isEmergency, String notes) {

        Patient patient = patientService.getPatientById(patientId);
        Doctor doctor = doctorService.getDoctorById(doctorId);
        List<Token> existingTokens = tokenRepository.findActiveTokensForPatientOnDate(patientId, LocalDate.now());
        if (!existingTokens.isEmpty()) {
            throw new RuntimeException("Patient already has an active token");
        }
        List<TimeSlot> availableSlots = timeSlotService.getAvailableSlotsForDoctor(doctorId, LocalDate.now());
        if (availableSlots.isEmpty()) {
            throw new RuntimeException("No available slots for this doctor today");
        }
        TimeSlot selectedSlot = availableSlots.get(0);

        if (isEmergency != null && isEmergency) {
            return handleEmergencyToken(patient, doctor, selectedSlot, notes);
        }
        return allocateTokenByPriority(patient, doctor, selectedSlot, priorityLevel);
    }

    @Transactional
    private Token handleEmergencyToken(Patient patient, Doctor doctor,
                                       TimeSlot slot, String notes) {

        // Get all active tokens in this slot
        List<Token> existingTokens = tokenRepository.findByTimeSlotAndStatusOrderByQueuePositionAsc(
                slot, TokenStatus.ACTIVE);
        for (Token existingToken : existingTokens) {
            int oldPosition = existingToken.getQueuePosition();
            existingToken.setQueuePosition(oldPosition + 1);
            tokenRepository.save(existingToken);
            reallocationService.logReallocation(existingToken, slot, slot, ReallocationReason.EMERGENCY_BUMPED, oldPosition, oldPosition + 1);
        }
        Token emergencyToken = new Token();
        emergencyToken.setPatient(patient);
        emergencyToken.setDoctor(doctor);
        emergencyToken.setTimeSlot(slot);
        emergencyToken.setTokenNumber(generateTokenNumber(doctor, slot, 1));
        emergencyToken.setPriorityLevel(PriorityLevel.EMERGENCY);
        emergencyToken.setEmergency(true);
        emergencyToken.setStatus(TokenStatus.ACTIVE);
        emergencyToken.setQueuePosition(1);
        emergencyToken.setEstimatedTime(calculateEstimatedTime(slot, 1));
        emergencyToken.setNotes(notes);

        Token savedToken = tokenRepository.save(emergencyToken);
        slot.incrementTokenCount();
        timeSlotService.createTimeSlot(slot);
        return savedToken;
    }

    @Transactional
    private Token allocateTokenByPriority(Patient patient, Doctor doctor, TimeSlot slot,
                                          PriorityLevel priorityLevel) {

        // Get existing tokens sorted by queue position
        List<Token> existingTokens = tokenRepository.findByTimeSlotAndStatusOrderByQueuePositionAsc(
                slot, TokenStatus.ACTIVE);
        int newPosition = findPositionByPriority(existingTokens, priorityLevel);
        for (Token existingToken : existingTokens) {
            if (existingToken.getQueuePosition() >= newPosition) {
                int oldPosition = existingToken.getQueuePosition();
                existingToken.setQueuePosition(oldPosition + 1);
                tokenRepository.save(existingToken);

                reallocationService.logReallocation(
                        existingToken, slot, slot,
                        ReallocationReason.SYSTEM_AUTO,
                        oldPosition, oldPosition + 1);
            }
        }
        Token token = new Token();
        token.setPatient(patient);
        token.setDoctor(doctor);
        token.setTimeSlot(slot);
        token.setTokenNumber(generateTokenNumber(doctor, slot, newPosition));
        token.setPriorityLevel(priorityLevel);
        token.setEmergency(false);
        token.setStatus(TokenStatus.ACTIVE);
        token.setQueuePosition(newPosition);
        token.setEstimatedTime(calculateEstimatedTime(slot, newPosition));


        Token savedToken = tokenRepository.save(token);
        slot.incrementTokenCount();
        timeSlotService.createTimeSlot(slot);

        return savedToken;
    }
    private int findPositionByPriority(List<Token> existingTokens, PriorityLevel newPriority) {
        int position = 1;

        for (Token token : existingTokens) {
            // Emergency always stays at top
            if (token.getEmergency()) {
                position++;
                continue;
            }

            // Compare priorities
            if (newPriority.getValue() > token.getPriorityLevel().getValue()) {
                position++;
            } else {
                break;
            }
        }

        return position;
    }
    private String generateTokenNumber(Doctor doctor, TimeSlot slot, int position) {
        return String.format("D%d-%s-%03d",
                doctor.getDoctorId(),
                slot.getStartTime().toString(),
                position);
    }
    private LocalDateTime calculateEstimatedTime(TimeSlot slot, int queuePosition) {
        LocalDateTime slotDateTime = LocalDateTime.of(slot.getSlotDate(), slot.getStartTime());
        return slotDateTime.plusMinutes((queuePosition - 1) * 5); // 5 min per patient
    }
    @Transactional
    public void cancelToken(Long tokenId) {
        Token token = tokenRepository.findById(tokenId)
                .orElseThrow(() -> new RuntimeException("Token not found"));

        if (token.getStatus() != TokenStatus.ACTIVE) {
            throw new RuntimeException("Only active tokens can be cancelled");
        }

        TimeSlot slot = token.getTimeSlot();
        int cancelledPosition = token.getQueuePosition();

        // Mark as cancelled
        token.setStatus(TokenStatus.CANCELLED);
        tokenRepository.save(token);

        // Move everyone up
        List<Token> tokensAfter = tokenRepository.findByTimeSlotAndStatusOrderByQueuePositionAsc(
                        slot, TokenStatus.ACTIVE).stream()
                .filter(t -> t.getQueuePosition() > cancelledPosition)
                .collect(Collectors.toList());

        for (Token afterToken : tokensAfter) {
            int oldPos = afterToken.getQueuePosition();
            afterToken.setQueuePosition(oldPos - 1);
            tokenRepository.save(afterToken);

            reallocationService.logReallocation(
                    afterToken, slot, slot,
                    ReallocationReason.SYSTEM_AUTO,
                    oldPos, oldPos - 1);
        }

        // Update slot count
        slot.decrementTokenCount();
        timeSlotService.createTimeSlot(slot);
    }
    public List<Token> getQueueStatus(Long slotId) {
        TimeSlot slot = timeSlotService.getSlotById(slotId);
        return tokenRepository.findByTimeSlotAndStatusOrderByQueuePositionAsc(
                slot, TokenStatus.ACTIVE);
    }
    public Token getTokenById(Long tokenId) {
        return tokenRepository.findById(tokenId)
                .orElseThrow(() -> new RuntimeException("Token not found"));
    }
    public List<Token> getPatientHistory(Long patientId) {
        Patient patient = patientService.getPatientById(patientId);
        return tokenRepository.findByPatient(patient);
    }
    @Transactional
    public Token updateTokenStatus(Long tokenId, TokenStatus newStatus) {
        Token token = getTokenById(tokenId);
        token.setStatus(newStatus);

        if (newStatus == TokenStatus.COMPLETED) {
            token.setActualServiceTime(LocalDateTime.now());
        }

        return tokenRepository.save(token);
    }
}
