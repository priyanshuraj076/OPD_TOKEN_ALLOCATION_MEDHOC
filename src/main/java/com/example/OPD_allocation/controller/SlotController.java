package com.example.OPD_allocation.controller;

import com.example.OPD_allocation.dto.ApiResponse;
import com.example.OPD_allocation.dto.ReallocationResponse;
import com.example.OPD_allocation.entity.Token;
import com.example.OPD_allocation.service.ReallocationService;
import com.example.OPD_allocation.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/slots")
@RequiredArgsConstructor
public class SlotController {

    private final TokenService tokenService;
    private final ReallocationService reallocationService;

    /**
     * GET /api/slots/{slot_id}/tokens - Get all tokens for a slot
     */
    @GetMapping("/{slotId}/tokens")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSlotTokens(@PathVariable Long slotId) {
        try {
            List<Token> tokens = tokenService.getTokensBySlot(slotId);

            Map<String, Object> response = new HashMap<>();
            response.put("total_tokens", tokens.size());
            response.put("tokens", tokens);

            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * POST /api/slots/{slot_id}/reallocate-overflow - Reallocate overflow tokens
     */
    @PostMapping("/{slotId}/reallocate-overflow")
    public ResponseEntity<ApiResponse<ReallocationResponse>> reallocateOverflow(
            @PathVariable Long slotId,
            @RequestBody Map<String, String> requestBody) {
        try {
            String reason = requestBody.getOrDefault("reason", "Doctor delay");
            String strategy = requestBody.getOrDefault("strategy", "move_to_next_slot");

            ReallocationResponse response = reallocationService.reallocateOverflow(slotId, reason, strategy);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}

