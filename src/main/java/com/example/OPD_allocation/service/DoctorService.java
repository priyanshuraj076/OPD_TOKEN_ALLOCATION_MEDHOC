package com.example.OPD_allocation.service;

import com.example.OPD_allocation.entity.Doctor;
import com.example.OPD_allocation.repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.print.Doc;
import java.util.List;

@Service
public class DoctorService {
    @Autowired
    private DoctorRepository doctorRepository;
    public Doctor createDoctor(Doctor doctor){
        return doctorRepository.save(doctor);
    }

    public List<Doctor> getAllDoctors(){
        return doctorRepository.findAll();
    }
    public Doctor getDoctorById(Long id){
        return doctorRepository.findById(id).orElseThrow(()->new RuntimeException("Doctor not found with id: "+id));

    }
    public List<Doctor> getAvailableDoctors(){
        return doctorRepository.findByIsAvailableTrue();
    }
    public List<Doctor> getDoctorsBySpecialization(String specialization){
        return doctorRepository.findBySpecialization(specialization);
    }
    public Doctor updateDoctorAvailability(Long doctorId,Boolean isAvailable){
        Doctor doctor=getDoctorById(doctorId);
        doctor.setAvailable(isAvailable);
        return doctorRepository.save(doctor);
    }
}
