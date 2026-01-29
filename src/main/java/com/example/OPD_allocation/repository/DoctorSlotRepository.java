package com.example.OPD_allocation.repository;

import com.example.OPD_allocation.entity.Doctor;
import com.example.OPD_allocation.entity.DoctorSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorSlotRepository extends JpaRepository<DoctorSlot, Long> {

    List<DoctorSlot> findByDoctorAndDate(Doctor doctor, LocalDate date);

    List<DoctorSlot> findByDoctorAndDateOrderByStartTime(Doctor doctor, LocalDate date);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ds FROM DoctorSlot ds WHERE ds.id = :id")
    Optional<DoctorSlot> findByIdWithLock(@Param("id") Long id);

    Optional<DoctorSlot> findByDoctorAndDateAndStartTime(Doctor doctor, LocalDate date, LocalTime startTime);

    @Query("SELECT ds FROM DoctorSlot ds WHERE ds.doctor.id = :doctorId AND ds.date = :date " +
           "AND ds.status = 'ACTIVE' AND ds.currentCount < ds.maxCapacity " +
           "ORDER BY ds.startTime")
    List<DoctorSlot> findAvailableSlots(@Param("doctorId") Long doctorId, @Param("date") LocalDate date);

    @Query("SELECT ds FROM DoctorSlot ds WHERE ds.doctor.id = :doctorId AND ds.date = :date " +
           "AND ds.status = 'ACTIVE' AND ds.startTime <= :currentTime AND ds.endTime > :currentTime")
    Optional<DoctorSlot> findCurrentActiveSlot(@Param("doctorId") Long doctorId,
                                                @Param("date") LocalDate date,
                                                @Param("currentTime") LocalTime currentTime);
}

