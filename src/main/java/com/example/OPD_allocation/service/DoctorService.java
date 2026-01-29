package com.example.OPD_allocation.service;

import com.example.OPD_allocation.dto.CreateDoctorRequest;
import com.example.OPD_allocation.entity.Doctor;
import com.example.OPD_allocation.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public Doctor createDoctor(CreateDoctorRequest request) {
        Doctor doctor = new Doctor();
        doctor.setName(request.getName());
        doctor.setSpecialization(request.getSpecialization());

        Doctor saved = doctorRepository.save(doctor);

        auditLogService.log("DOCTOR_CREATED", "doctor", saved.getId(),
            "Doctor " + saved.getName() + " created");

        return saved;
    }

    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    public Doctor getDoctorById(Long id) {
        return doctorRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Doctor not found with id: " + id));
    }
}

