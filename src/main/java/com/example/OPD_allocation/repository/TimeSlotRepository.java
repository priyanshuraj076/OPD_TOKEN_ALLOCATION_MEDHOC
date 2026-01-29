package com.example.OPD_allocation.repository;

import com.example.OPD_allocation.entity.Doctor;
import com.example.OPD_allocation.entity.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {
    List<TimeSlot> findByDoctorAndSlotDate(Doctor doctor, LocalDate slotDate);
    List<TimeSlot> findByDoctorAndSlotDateAndIsAvailableTrue(Doctor doctor, LocalDate slotDate);
    Optional<TimeSlot> findByDoctorAndSlotDateAndStartTime(Doctor doctor, LocalDate slotDate, LocalTime startTime);

    @Query("SELECT ts FROM TimeSlot ts WHERE ts.isAvailable = true AND ts.currentTokens < ts.maxTokens")
    List<TimeSlot> findAllAvailableSlots();

    @Query("SELECT ts FROM TimeSlot ts WHERE ts.doctor.doctorId = :doctorId AND ts.slotDate = :date AND ts.isAvailable = true AND ts.currentTokens < ts.maxTokens ORDER BY ts.startTime")
    List<TimeSlot> findAvailableSlotsForDoctor(@Param("doctorId") Long doctorId, @Param("date") LocalDate date);
}
