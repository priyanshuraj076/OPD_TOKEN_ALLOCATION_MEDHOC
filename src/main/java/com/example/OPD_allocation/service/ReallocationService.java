package com.example.OPD_allocation.service;

import com.example.OPD_allocation.dto.ReallocationResponse;
import com.example.OPD_allocation.entity.DoctorSlot;
import com.example.OPD_allocation.entity.Reallocation;
import com.example.OPD_allocation.entity.Token;
import com.example.OPD_allocation.repository.ReallocationRepository;
import com.example.OPD_allocation.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReallocationService {

    private final TokenRepository tokenRepository;
    private final DoctorSlotService slotService;
    private final ReallocationRepository reallocationRepository;
    private final AuditLogService auditLogService;

    /**
     * Reallocate overflow tokens when doctor is delayed
     */
    @Transactional
    public ReallocationResponse reallocateOverflow(Long slotId, String reason, String strategy) {
        DoctorSlot currentSlot = slotService.getSlotById(slotId);

        // Get all waiting tokens sorted by priority (lowest first)
        List<Token> waitingTokens = tokenRepository.findWaitingTokensBySlotOrderedByPriority(slotId);

        List<ReallocationResponse.ReallocationDetail> details = new ArrayList<>();
        int reallocatedCount = 0;

        // Find tokens to move (lowest priority first)
        for (int i = waitingTokens.size() - 1; i >= 0 && reallocatedCount < 3; i--) {
            Token token = waitingTokens.get(i);

            // Only move WALKIN and ONLINE tokens
            if (token.getTokenType() == Token.TokenType.WALKIN ||
                token.getTokenType() == Token.TokenType.ONLINE) {

                // Find next available slot
                List<DoctorSlot> availableSlots = slotService.findAvailableSlots(
                    currentSlot.getDoctor().getId(), currentSlot.getDate()
                );

                for (DoctorSlot nextSlot : availableSlots) {
                    if (nextSlot.getId().equals(slotId)) continue;

                    if (nextSlot.canAcceptToken()) {
                        // Reallocate
                        Reallocation reallocation = new Reallocation();
                        reallocation.setToken(token);
                        reallocation.setOldSlot(currentSlot);
                        reallocation.setNewSlot(nextSlot);
                        reallocation.setReason(reason);
                        reallocationRepository.save(reallocation);

                        token.setSlot(nextSlot);
                        tokenRepository.save(token);

                        currentSlot.decrementCount();
                        nextSlot.incrementCount();

                        details.add(new ReallocationResponse.ReallocationDetail(
                            token.getTokenNumber(),
                            currentSlot.getTimeRange(),
                            nextSlot.getTimeRange()
                        ));

                        reallocatedCount++;

                        // Log
                        auditLogService.log("TOKEN_REALLOCATED", "token", token.getId(),
                            "Token reallocated due to: " + reason);

                        // TODO: Send SMS
                        System.out.println("SMS sent to " + token.getPatientPhone() +
                            ": Your slot moved from " + currentSlot.getTimeRange() +
                            " to " + nextSlot.getTimeRange());

                        break;
                    }
                }
            }
        }

        // Update slot status if needed
        if ("DELAYED".equalsIgnoreCase(strategy)) {
            currentSlot.setStatus(DoctorSlot.SlotStatus.DELAYED);
        }

        return new ReallocationResponse(reallocatedCount, reallocatedCount, details);
    }
}

