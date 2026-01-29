package com.example.OPD_allocation.controller;


import com.example.OPD_allocation.entity.TokenReallocation;
import com.example.OPD_allocation.service.TokenReallocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reallocations")
@CrossOrigin(origins = "*")
public class TokenReallocationController {

    @Autowired
    private TokenReallocationService reallocationService;

    // Handle Doctor Delay - Reallocate all tokens
    @PostMapping("/doctor-delay")
    public ResponseEntity<String> handleDoctorDelay(@RequestBody Map<String, Object> request) {
        Long doctorId = Long.valueOf(request.get("doctorId").toString());
        LocalDate date = LocalDate.parse(request.get("date").toString());
        Integer delayMinutes = Integer.valueOf(request.get("delayMinutes").toString());

        reallocationService.handleDoctorDelay(doctorId, date, delayMinutes);
        return ResponseEntity.ok("All tokens reallocated due to doctor delay");
    }

    // Get Reallocation History for a Token
    @GetMapping("/token/{tokenId}/history")
    public ResponseEntity<List<TokenReallocation>> getTokenHistory(@PathVariable Long tokenId) {
        return ResponseEntity.ok(reallocationService.getTokenHistory(tokenId));
    }
}