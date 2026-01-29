package com.example.OPD_allocation.controller;


import com.example.OPD_allocation.entity.Patient;
import com.example.OPD_allocation.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
public class PatientController {
    @Autowired
    private PatientService patientService;

    @PostMapping
    public Patient createPatient(@RequestBody Patient patient){
        return patientService.createPatient(patient);
    }
    @GetMapping
    public List<Patient> getAllPatients(){
        return patientService.getAllPatients();
    }
    @GetMapping("{id}")
    public Patient getPatient(@PathVariable Long id){
        return patientService.getPatientById(id);
    }

}
