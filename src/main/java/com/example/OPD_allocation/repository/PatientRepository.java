package com.example.OPD_allocation.repository;

import com.example.OPD_allocation.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByContact(String contact);
    boolean existsByContact(String contact);
}
