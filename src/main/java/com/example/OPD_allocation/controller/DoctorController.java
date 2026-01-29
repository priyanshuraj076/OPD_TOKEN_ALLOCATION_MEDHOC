package com.example.OPD_allocation.controller;

import com.example.OPD_allocation.dto.ApiResponse;
import com.example.OPD_allocation.dto.CreateDoctorRequest;
import com.example.OPD_allocation.dto.CreateSlotsRequest;
import com.example.OPD_allocation.dto.SlotAvailabilityResponse;
import com.example.OPD_allocation.entity.Doctor;
import com.example.OPD_allocation.service.DoctorService;
import com.example.OPD_allocation.service.DoctorSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;
    private final DoctorSlotService slotService;

    /**
     * POST /api/doctors - Create a new doctor
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> createDoctor(@RequestBody CreateDoctorRequest request) {
        try {
            Doctor doctor = doctorService.createDoctor(request);

            Map<String, Object> response = new HashMap<>();
            response.put("doctor_id", doctor.getId());

            return ResponseEntity.ok(ApiResponse.success("Doctor created successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * GET /api/doctors - Get all doctors
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Doctor>>> getAllDoctors() {
        List<Doctor> doctors = doctorService.getAllDoctors();
        return ResponseEntity.ok(ApiResponse.success(doctors));
    }

    /**
     * GET /api/doctors/{doctor_id} - Get doctor by ID
     */
    @GetMapping("/{doctorId}")
    public ResponseEntity<ApiResponse<Doctor>> getDoctorById(@PathVariable Long doctorId) {
        try {
            Doctor doctor = doctorService.getDoctorById(doctorId);
            return ResponseEntity.ok(ApiResponse.success(doctor));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * POST /api/doctors/{doctor_id}/slots - Create time slots for a doctor
     */
    @PostMapping("/{doctorId}/slots")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createSlots(
            @PathVariable Long doctorId,
            @RequestBody CreateSlotsRequest request) {
        try {
            List<Long> slotIds = slotService.createSlots(doctorId, request);

            Map<String, Object> response = new HashMap<>();
            response.put("slots_created", slotIds.size());
            response.put("slot_ids", slotIds);

            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * GET /api/doctors/{doctor_id}/slots?date=2026-01-28 - Get slot availability
     */
    @GetMapping("/{doctorId}/slots")
    public ResponseEntity<ApiResponse<SlotAvailabilityResponse>> getSlotAvailability(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            SlotAvailabilityResponse response = slotService.getSlotAvailability(doctorId, date);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}

