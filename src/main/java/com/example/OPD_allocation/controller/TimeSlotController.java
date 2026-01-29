package com.example.OPD_allocation.controller;


import com.example.OPD_allocation.entity.TimeSlot;
import com.example.OPD_allocation.service.TimeSlotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/slots")
@CrossOrigin(origins = "*")
public class TimeSlotController {

    @Autowired
    private TimeSlotService timeSlotService;

    // Create Single Time Slot
    @PostMapping
    public ResponseEntity<TimeSlot> createTimeSlot(@RequestBody TimeSlot timeSlot) {
        return ResponseEntity.ok(timeSlotService.createTimeSlot(timeSlot));
    }

    // Create Multiple Slots for a Doctor
    @PostMapping("/bulk")
    public ResponseEntity<List<TimeSlot>> createBulkSlots(@RequestBody Map<String, Object> request) {
        Long doctorId = Long.valueOf(request.get("doctorId").toString());
        LocalDate date = LocalDate.parse(request.get("date").toString());
        LocalTime startTime = LocalTime.parse(request.get("startTime").toString());
        LocalTime endTime = LocalTime.parse(request.get("endTime").toString());
        Integer maxTokens = Integer.valueOf(request.get("maxTokensPerSlot").toString());

        List<TimeSlot> slots = timeSlotService.createSlotsForDoctor(
                doctorId, date, startTime, endTime, maxTokens);

        return ResponseEntity.ok(slots);
    }

    // Get Available Slots for Doctor on a Date
    @GetMapping("/doctor/{doctorId}/available")
    public ResponseEntity<List<TimeSlot>> getAvailableSlots(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(timeSlotService.getAvailableSlotsForDoctor(doctorId, date));
    }

    // Get All Available Slots
    @GetMapping("/available")
    public ResponseEntity<List<TimeSlot>> getAllAvailableSlots() {
        return ResponseEntity.ok(timeSlotService.getAllAvailableSlots());
    }

    // Get Slot by ID
    @GetMapping("/{id}")
    public ResponseEntity<TimeSlot> getSlotById(@PathVariable Long id) {
        return ResponseEntity.ok(timeSlotService.getSlotById(id));
    }
}
