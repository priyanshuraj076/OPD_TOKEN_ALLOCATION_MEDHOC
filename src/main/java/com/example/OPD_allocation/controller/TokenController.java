package com.example.OPD_allocation.controller;


import com.example.OPD_allocation.entity.PriorityLevel;
import com.example.OPD_allocation.entity.Token;
import com.example.OPD_allocation.entity.TokenStatus;
import com.example.OPD_allocation.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tokens")
@CrossOrigin(origins = "*")
public class TokenController {

    @Autowired
    private TokenService tokenService;

    // ALLOCATE TOKEN (Main API)
    @PostMapping("/allocate")
    public ResponseEntity<Token> allocateToken(@RequestBody Map<String, Object> request) {
        Long patientId = Long.valueOf(request.get("patientId").toString());
        Long doctorId = Long.valueOf(request.get("doctorId").toString());
        PriorityLevel priorityLevel = PriorityLevel.valueOf(request.get("priorityLevel").toString());
        Boolean isEmergency = request.containsKey("isEmergency") ?
                Boolean.valueOf(request.get("isEmergency").toString()) : false;
        String notes = request.containsKey("notes") ? request.get("notes").toString() : null;

        Token token = tokenService.allocateToken(patientId, doctorId, priorityLevel, isEmergency, notes);
        return ResponseEntity.ok(token);
    }

    // CANCEL TOKEN
    @PutMapping("/{id}/cancel")
    public ResponseEntity<String> cancelToken(@PathVariable Long id) {
        tokenService.cancelToken(id);
        return ResponseEntity.ok("Token cancelled successfully");
    }

    // GET QUEUE STATUS for a Time Slot
    @GetMapping("/queue/{slotId}")
    public ResponseEntity<List<Token>> getQueueStatus(@PathVariable Long slotId) {
        return ResponseEntity.ok(tokenService.getQueueStatus(slotId));
    }

    // GET TOKEN BY ID
    @GetMapping("/{id}")
    public ResponseEntity<Token> getTokenById(@PathVariable Long id) {
        return ResponseEntity.ok(tokenService.getTokenById(id));
    }

    // GET PATIENT HISTORY
    @GetMapping("/patient/{patientId}/history")
    public ResponseEntity<List<Token>> getPatientHistory(@PathVariable Long patientId) {
        return ResponseEntity.ok(tokenService.getPatientHistory(patientId));
    }

    // UPDATE TOKEN STATUS (Mark as Completed, No-Show, etc.)
    @PutMapping("/{id}/status")
    public ResponseEntity<Token> updateTokenStatus(
            @PathVariable Long id,
            @RequestParam TokenStatus status) {
        return ResponseEntity.ok(tokenService.updateTokenStatus(id, status));
    }
}
