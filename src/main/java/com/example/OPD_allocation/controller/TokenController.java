package com.example.OPD_allocation.controller;

import com.example.OPD_allocation.dto.*;
import com.example.OPD_allocation.entity.Token;
import com.example.OPD_allocation.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tokens")
@RequiredArgsConstructor
public class TokenController {

    private final TokenService tokenService;

    /**
     * POST /api/tokens/allocate - Main token allocation endpoint
     */
    @PostMapping("/allocate")
    public ResponseEntity<ApiResponse<TokenResponse>> allocateToken(@RequestBody TokenAllocationRequest request) {
        try {
            TokenResponse response = tokenService.allocateToken(request);

            if (response.getStatus() != null && response.getStatus().equals("waiting_list")) {
                return ResponseEntity.ok(ApiResponse.error("All slots full for this doctor today"));
            }

            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * POST /api/tokens/emergency-allocate - Emergency allocation
     */
    @PostMapping("/emergency-allocate")
    public ResponseEntity<ApiResponse<TokenResponse>> allocateEmergency(@RequestBody EmergencyAllocationRequest request) {
        try {
            TokenResponse response = tokenService.allocateEmergency(request);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * GET /api/tokens/{token_number} - Get token status
     */
    @GetMapping("/{tokenNumber}")
    public ResponseEntity<ApiResponse<TokenResponse>> getToken(@PathVariable String tokenNumber) {
        try {
            TokenResponse response = tokenService.getTokenByNumber(tokenNumber);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * POST /api/tokens/{token_id}/cancel - Cancel a token
     */
    @PostMapping("/{tokenId}/cancel")
    public ResponseEntity<ApiResponse<Map<String, Object>>> cancelToken(
            @PathVariable Long tokenId,
            @RequestBody Map<String, String> requestBody) {
        try {
            String reason = requestBody.getOrDefault("reason", "No reason provided");
            tokenService.cancelToken(tokenId, reason);

            Map<String, Object> response = new HashMap<>();
            response.put("slot_freed", true);

            return ResponseEntity.ok(ApiResponse.success("Token cancelled successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * POST /api/tokens/{token_id}/mark-no-show - Mark as no-show
     */
    @PostMapping("/{tokenId}/mark-no-show")
    public ResponseEntity<ApiResponse<String>> markNoShow(@PathVariable Long tokenId) {
        try {
            tokenService.markNoShow(tokenId);
            return ResponseEntity.ok(ApiResponse.success("Token marked as no-show", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * PUT /api/tokens/{token_id}/status - Update token status
     */
    @PutMapping("/{tokenId}/status")
    public ResponseEntity<ApiResponse<String>> updateStatus(
            @PathVariable Long tokenId,
            @RequestBody Map<String, String> requestBody) {
        try {
            String status = requestBody.get("status");
            tokenService.updateTokenStatus(tokenId, status);
            return ResponseEntity.ok(ApiResponse.success("Status updated successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}

