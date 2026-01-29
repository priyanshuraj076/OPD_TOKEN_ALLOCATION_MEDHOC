package com.example.OPD_allocation.controller;

import com.example.OPD_allocation.entity.Doctor;
import com.example.OPD_allocation.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@CrossOrigin(origins = "*")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    // Create Doctor
    @PostMapping
    public ResponseEntity<Doctor> createDoctor(@RequestBody Doctor doctor) {
        Doctor created = doctorService.createDoctor(doctor);
        return ResponseEntity.ok(created);
    }

    // Get All Doctors
    @GetMapping
    public ResponseEntity<List<Doctor>> getAllDoctors() {
        return ResponseEntity.ok(doctorService.getAllDoctors());
    }

    // Get Doctor by ID
    @GetMapping("/{id}")
    public ResponseEntity<Doctor> getDoctorById(@PathVariable Long id) {
        return ResponseEntity.ok(doctorService.getDoctorById(id));
    }

    // Get Available Doctors
    @GetMapping("/available")
    public ResponseEntity<List<Doctor>> getAvailableDoctors() {
        return ResponseEntity.ok(doctorService.getAvailableDoctors());
    }

    // Get Doctors by Specialization
    @GetMapping("/specialization/{specialization}")
    public ResponseEntity<List<Doctor>> getDoctorsBySpecialization(@PathVariable String specialization) {
        return ResponseEntity.ok(doctorService.getDoctorsBySpecialization(specialization));
    }

    // Update Doctor Availability
    @PutMapping("/{id}/availability")
    public ResponseEntity<Doctor> updateAvailability(
            @PathVariable Long id,
            @RequestParam Boolean isAvailable) {
        return ResponseEntity.ok(doctorService.updateDoctorAvailability(id, isAvailable));
    }
}
