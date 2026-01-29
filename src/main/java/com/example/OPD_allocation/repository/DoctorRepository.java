package com.example.OPD_allocation.repository;

import com.example.OPD_allocation.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    List<Doctor> findByIsAvailableTrue();
    List<Doctor> findBySpecialization(String specialization);
    List<Doctor> findBySpecializationAndIsAvailableTrue(String specialization);
}
