package com.example.OPD_allocation.service;

import com.example.OPD_allocation.dto.EmergencyAllocationRequest;
import com.example.OPD_allocation.dto.TokenAllocationRequest;
import com.example.OPD_allocation.dto.TokenResponse;
import com.example.OPD_allocation.entity.Doctor;
import com.example.OPD_allocation.entity.DoctorSlot;
import com.example.OPD_allocation.entity.Reallocation;
import com.example.OPD_allocation.entity.Token;
import com.example.OPD_allocation.repository.ReallocationRepository;
import com.example.OPD_allocation.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final TokenRepository tokenRepository;
    private final DoctorService doctorService;
    private final DoctorSlotService slotService;
    private final ReallocationRepository reallocationRepository;
    private final AuditLogService auditLogService;

    /**
     * Main token allocation algorithm following the documentation
     */
    @Transactional
    public TokenResponse allocateToken(TokenAllocationRequest request) {
        // Validate token type
        Token.TokenType tokenType = parseTokenType(request.getTokenType());

        // Step 1: Handle EMERGENCY
        if (tokenType == Token.TokenType.EMERGENCY) {
            return allocateEmergencyToken(request);
        }

        // Check if patient already has a token for this doctor today
        checkDuplicateToken(request.getPatientPhone(), request.getDoctorId(), request.getPreferredDate());

        Doctor doctor = doctorService.getDoctorById(request.getDoctorId());

        // Step 2: Try preferred slot
        DoctorSlot preferredSlot = findPreferredSlot(doctor, request.getPreferredDate(), request.getPreferredTime());

        if (preferredSlot != null && preferredSlot.canAcceptToken()) {
            return createAndAllocateToken(request, preferredSlot, doctor, false);
        }

        // Step 3: Handle PAID_PRIORITY bumping
        if (tokenType == Token.TokenType.PAID_PRIORITY && preferredSlot != null && preferredSlot.isFull()) {
            Token bumpedToken = findLowestPriorityToken(preferredSlot.getId());

            if (bumpedToken != null && canBump(bumpedToken)) {
                DoctorSlot nextSlot = findNextAvailableSlot(request.getDoctorId(), request.getPreferredDate());

                if (nextSlot != null) {
                    reallocateToken(bumpedToken, nextSlot, "Bumped by paid priority");
                    preferredSlot.decrementCount();
                    return createAndAllocateToken(request, preferredSlot, doctor, false);
                }
            }
        }

        // Step 4: Find next available slot
        DoctorSlot nextSlot = findNextAvailableSlot(request.getDoctorId(), request.getPreferredDate());

        if (nextSlot != null) {
            return createAndAllocateToken(request, nextSlot, doctor, true);
        }

        // Step 5: All slots full - add to waiting list
        return addToWaitingList(request, doctor);
    }

    /**
     * Emergency allocation - can exceed slot capacity
     */
    @Transactional
    public TokenResponse allocateEmergencyToken(TokenAllocationRequest request) {
        Doctor doctor = doctorService.getDoctorById(request.getDoctorId());
        LocalDate date = request.getPreferredDate();

        // Find current active slot or next available slot
        DoctorSlot slot = slotService.findCurrentActiveSlot(
            request.getDoctorId(), date, LocalTime.now()
        );

        if (slot == null) {
            // Find first active slot today
            List<DoctorSlot> slots = slotService.findAvailableSlots(request.getDoctorId(), date);
            if (!slots.isEmpty()) {
                slot = slots.get(0);
            } else {
                throw new RuntimeException("No active slots available for emergency");
            }
        }

        // Emergency can exceed capacity
        Token token = new Token();
        token.setTokenNumber(generateEmergencyTokenNumber());
        token.setPatientName(request.getPatientName());
        token.setPatientPhone(request.getPatientPhone());
        token.setDoctor(doctor);
        token.setSlot(slot);
        token.setTokenType(Token.TokenType.EMERGENCY);
        token.setStatus(Token.TokenStatus.WAITING);

        Token saved = tokenRepository.save(token);
        slot.incrementCount();
        slotService.getSlotByIdWithLock(slot.getId()); // Update slot

        auditLogService.log("TOKEN_CREATED", "token", saved.getId(),
            "Emergency token " + saved.getTokenNumber() + " created for " + saved.getPatientName());

        return buildTokenResponse(saved, "Emergency - Immediate", false);
    }

    /**
     * Emergency allocation with separate request DTO
     */
    @Transactional
    public TokenResponse allocateEmergency(EmergencyAllocationRequest request) {
        TokenAllocationRequest allocRequest = new TokenAllocationRequest();
        allocRequest.setPatientName(request.getPatientName());
        allocRequest.setPatientPhone(request.getPatientPhone());
        allocRequest.setDoctorId(request.getDoctorId());
        allocRequest.setPreferredDate(LocalDate.parse(request.getDate()));
        allocRequest.setTokenType("emergency");

        return allocateEmergencyToken(allocRequest);
    }

    /**
     * Cancel token and free capacity
     */
    @Transactional
    public void cancelToken(Long tokenId, String reason) {
        Token token = tokenRepository.findById(tokenId)
            .orElseThrow(() -> new RuntimeException("Token not found"));

        if (token.getStatus() == Token.TokenStatus.CANCELLED ||
            token.getStatus() == Token.TokenStatus.COMPLETED) {
            throw new RuntimeException("Token already " + token.getStatus());
        }

        token.setStatus(Token.TokenStatus.CANCELLED);
        tokenRepository.save(token);

        // Free up slot capacity
        DoctorSlot slot = token.getSlot();
        slot.decrementCount();

        auditLogService.log("TOKEN_CANCELLED", "token", tokenId,
            "Token " + token.getTokenNumber() + " cancelled. Reason: " + reason);

        // Check waiting list and promote
        promoteFromWaitingList(slot);
    }

    /**
     * Mark token as no-show
     */
    @Transactional
    public void markNoShow(Long tokenId) {
        Token token = tokenRepository.findById(tokenId)
            .orElseThrow(() -> new RuntimeException("Token not found"));

        token.setStatus(Token.TokenStatus.NO_SHOW);
        tokenRepository.save(token);

        // Free up slot capacity
        DoctorSlot slot = token.getSlot();
        slot.decrementCount();

        auditLogService.log("TOKEN_NO_SHOW", "token", tokenId,
            "Token " + token.getTokenNumber() + " marked as no-show");

        // Promote from waiting list
        promoteFromWaitingList(slot);
    }

    /**
     * Update token status
     */
    @Transactional
    public void updateTokenStatus(Long tokenId, String statusStr) {
        Token token = tokenRepository.findById(tokenId)
            .orElseThrow(() -> new RuntimeException("Token not found"));

        Token.TokenStatus newStatus = Token.TokenStatus.valueOf(statusStr.toUpperCase());
        Token.TokenStatus oldStatus = token.getStatus();

        token.setStatus(newStatus);

        if (newStatus == Token.TokenStatus.IN_CONSULTATION) {
            token.setCalledAt(java.time.LocalDateTime.now());
        } else if (newStatus == Token.TokenStatus.COMPLETED) {
            token.setCompletedAt(java.time.LocalDateTime.now());
        }

        tokenRepository.save(token);

        auditLogService.log("TOKEN_STATUS_UPDATED", "token", tokenId,
            "Token " + token.getTokenNumber() + " status changed from " + oldStatus + " to " + newStatus);
    }

    /**
     * Get token by token number
     */
    public TokenResponse getTokenByNumber(String tokenNumber) {
        Token token = tokenRepository.findByTokenNumber(tokenNumber)
            .orElseThrow(() -> new RuntimeException("Token not found: " + tokenNumber));

        Long waitingCount = tokenRepository.countWaitingTokens(token.getSlot().getId());
        int position = calculateQueuePosition(token);

        TokenResponse response = new TokenResponse();
        response.setTokenNumber(token.getTokenNumber());
        response.setDoctorName(token.getDoctor().getName());
        response.setDate(token.getSlot().getDate().toString());
        response.setSlotTime(token.getSlot().getTimeRange());
        response.setStatus(token.getStatus().name().toLowerCase());
        response.setPositionInQueue(position);
        response.setEstimatedWaitTime(calculateWaitTime(position) + " minutes");

        return response;
    }

    /**
     * Get all tokens for a slot
     */
    public List<Token> getTokensBySlot(Long slotId) {
        return tokenRepository.findTokensBySlotOrderedByPriority(slotId);
    }

    // ==================== Helper Methods ====================

    private TokenResponse createAndAllocateToken(TokenAllocationRequest request, DoctorSlot slot,
                                                  Doctor doctor, boolean reallocated) {
        // Use pessimistic locking to prevent double booking
        DoctorSlot lockedSlot = slotService.getSlotByIdWithLock(slot.getId());

        Token token = new Token();
        token.setTokenNumber(generateTokenNumber());
        token.setPatientName(request.getPatientName());
        token.setPatientPhone(request.getPatientPhone());
        token.setDoctor(doctor);
        token.setSlot(lockedSlot);
        token.setTokenType(parseTokenType(request.getTokenType()));
        token.setStatus(Token.TokenStatus.WAITING);

        Token saved = tokenRepository.save(token);
        lockedSlot.incrementCount();

        auditLogService.log("TOKEN_CREATED", "token", saved.getId(),
            "Token " + saved.getTokenNumber() + " created for " + saved.getPatientName() +
            " - Type: " + saved.getTokenType());

        return buildTokenResponse(saved, reallocated ? "Preferred slot full, allocated to next available" : null, reallocated);
    }

    private DoctorSlot findPreferredSlot(Doctor doctor, LocalDate date, String preferredTime) {
        if (preferredTime == null) {
            return null;
        }

        try {
            LocalTime time = LocalTime.parse(preferredTime);
            return slotService.findCurrentActiveSlot(doctor.getId(), date, time);
        } catch (Exception e) {
            return null;
        }
    }

    private DoctorSlot findNextAvailableSlot(Long doctorId, LocalDate date) {
        List<DoctorSlot> availableSlots = slotService.findAvailableSlots(doctorId, date);
        return availableSlots.isEmpty() ? null : availableSlots.get(0);
    }

    private Token findLowestPriorityToken(Long slotId) {
        List<Token> tokens = tokenRepository.findWaitingTokensBySlotOrderedByPriority(slotId);

        // Find lowest priority (WALKIN is lowest)
        for (int i = tokens.size() - 1; i >= 0; i--) {
            Token token = tokens.get(i);
            if (token.getTokenType() == Token.TokenType.WALKIN ||
                token.getTokenType() == Token.TokenType.ONLINE) {
                return token;
            }
        }

        return null;
    }

    private boolean canBump(Token token) {
        // Can only bump WALKIN and ONLINE types
        return token.getTokenType() == Token.TokenType.WALKIN ||
               token.getTokenType() == Token.TokenType.ONLINE;
    }

    @Transactional
    protected void reallocateToken(Token token, DoctorSlot newSlot, String reason) {
        DoctorSlot oldSlot = token.getSlot();

        // Create reallocation record
        Reallocation reallocation = new Reallocation();
        reallocation.setToken(token);
        reallocation.setOldSlot(oldSlot);
        reallocation.setNewSlot(newSlot);
        reallocation.setReason(reason);
        reallocationRepository.save(reallocation);

        // Update token
        token.setSlot(newSlot);
        tokenRepository.save(token);

        // Update slot counts
        oldSlot.decrementCount();
        newSlot.incrementCount();

        auditLogService.log("TOKEN_REALLOCATED", "token", token.getId(),
            "Token " + token.getTokenNumber() + " reallocated from slot " + oldSlot.getId() +
            " to " + newSlot.getId() + ". Reason: " + reason);

        // TODO: Send SMS notification
        System.out.println("SMS sent to " + token.getPatientPhone() +
            ": Your slot has been moved to " + newSlot.getTimeRange());
    }

    private TokenResponse addToWaitingList(TokenAllocationRequest request, Doctor doctor) {
        Token token = new Token();
        token.setTokenNumber(generateWaitingListTokenNumber());
        token.setPatientName(request.getPatientName());
        token.setPatientPhone(request.getPatientPhone());
        token.setDoctor(doctor);
        token.setTokenType(parseTokenType(request.getTokenType()));
        token.setStatus(Token.TokenStatus.WAITING_LIST);

        // Create a dummy slot reference (or handle differently)
        List<DoctorSlot> slots = slotService.findAvailableSlots(request.getDoctorId(), request.getPreferredDate());
        if (!slots.isEmpty()) {
            token.setSlot(slots.get(0));
        }

        Token saved = tokenRepository.save(token);

        auditLogService.log("TOKEN_WAITING_LIST", "token", saved.getId(),
            "Token " + saved.getTokenNumber() + " added to waiting list");

        TokenResponse response = new TokenResponse();
        response.setTokenNumber(saved.getTokenNumber());
        response.setDoctorName(doctor.getName());
        response.setStatus("waiting_list");

        return response;
    }

    private void promoteFromWaitingList(DoctorSlot slot) {
        List<Token> waitingList = tokenRepository.findWaitingListTokens(
            slot.getDoctor().getId(), slot.getDate()
        );

        if (!waitingList.isEmpty() && slot.canAcceptToken()) {
            Token promoted = waitingList.get(0);
            promoted.setSlot(slot);
            promoted.setStatus(Token.TokenStatus.WAITING);
            tokenRepository.save(promoted);

            slot.incrementCount();

            auditLogService.log("TOKEN_PROMOTED", "token", promoted.getId(),
                "Token " + promoted.getTokenNumber() + " promoted from waiting list");

            // TODO: Send SMS
            System.out.println("SMS sent to " + promoted.getPatientPhone() +
                ": Slot now available for " + slot.getTimeRange());
        }
    }

    private void checkDuplicateToken(String phone, Long doctorId, LocalDate date) {
        List<Token> existing = tokenRepository.findActiveTokensForPatient(phone, doctorId, date);

        if (!existing.isEmpty()) {
            throw new RuntimeException("You already have a token for this doctor today");
        }
    }

    private TokenResponse buildTokenResponse(Token token, String message, boolean reallocated) {
        int position = calculateQueuePosition(token);

        TokenResponse response = new TokenResponse();
        response.setTokenNumber(token.getTokenNumber());
        response.setDoctorName(token.getDoctor().getName());
        response.setDate(token.getSlot().getDate().toString());
        response.setSlotTime(token.getSlot().getTimeRange());
        response.setEstimatedWaitTime(calculateWaitTime(position) + " minutes");
        response.setPositionInQueue(position);
        response.setStatus(token.getStatus().name().toLowerCase());

        return response;
    }

    private int calculateQueuePosition(Token token) {
        List<Token> allTokens = tokenRepository.findTokensBySlotOrderedByPriority(token.getSlot().getId());

        for (int i = 0; i < allTokens.size(); i++) {
            if (allTokens.get(i).getId().equals(token.getId())) {
                return i + 1;
            }
        }

        return 0;
    }

    private String calculateWaitTime(int position) {
        // Assume 10 minutes per patient
        int waitMinutes = position * 10;
        return String.valueOf(waitMinutes);
    }

    private Token.TokenType parseTokenType(String type) {
        try {
            return Token.TokenType.valueOf(type.toUpperCase());
        } catch (Exception e) {
            throw new RuntimeException("Invalid token type: " + type +
                ". Allowed: emergency, paid_priority, followup, online, walkin");
        }
    }

    private synchronized String generateTokenNumber() {
        // Get count of existing tokens to ensure uniqueness
        long count = tokenRepository.count();
        String tokenNumber;
        int attempt = 0;
        
        do {
            tokenNumber = String.format("T%03d", count + attempt + 1);
            attempt++;
        } while (tokenRepository.findByTokenNumber(tokenNumber).isPresent() && attempt < 1000);
        
        return tokenNumber;
    }

    private synchronized String generateEmergencyTokenNumber() {
        // Get count of existing tokens to ensure uniqueness
        long count = tokenRepository.count();
        String tokenNumber;
        int attempt = 0;
        
        do {
            tokenNumber = String.format("E%03d", count + attempt + 1);
            attempt++;
        } while (tokenRepository.findByTokenNumber(tokenNumber).isPresent() && attempt < 1000);
        
        return tokenNumber;
    }

    private synchronized String generateWaitingListTokenNumber() {
        // Get count of existing tokens to ensure uniqueness
        long count = tokenRepository.count();
        String tokenNumber;
        int attempt = 0;
        
        do {
            tokenNumber = String.format("W%03d", count + attempt + 1);
            attempt++;
        } while (tokenRepository.findByTokenNumber(tokenNumber).isPresent() && attempt < 1000);
        
        return tokenNumber;
    }
}
