package com.example.OPD_allocation.service;


import com.example.OPD_allocation.entity.Patient;
import com.example.OPD_allocation.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatientService {

    @Autowired
    private PatientRepository patientRepository;
    public Patient createPatient(Patient patient){
        return patientRepository.save(patient);
    }
    public List<Patient> getAllPatients(){
        return patientRepository.findAll();
    }
    public Patient getPatientById(Long id){
        return patientRepository.findById(id).orElseThrow(()->new RuntimeException("Patient not found with id "+ id));
    }
}
