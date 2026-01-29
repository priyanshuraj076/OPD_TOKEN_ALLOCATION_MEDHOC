package com.example.OPD_allocation.service;

import com.example.OPD_allocation.entity.Doctor;
import com.example.OPD_allocation.entity.TimeSlot;
import com.example.OPD_allocation.repository.TimeSlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TimeSlotService {
    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private DoctorService doctorService;

    public TimeSlot createTimeSlot(TimeSlot timeSlot){
        return timeSlotRepository.save(timeSlot);
    }
    public List<TimeSlot> createSlotsForDoctor(Long doctorId, LocalDate date,
                                               LocalTime startTime, LocalTime endTime,
                                               Integer maxTokensPerSlot) {
        Doctor doctor = doctorService.getDoctorById(doctorId);
        List<TimeSlot> slots = new ArrayList<>();
        LocalTime currentTime=startTime;
        while(currentTime.isBefore(endTime)){
            TimeSlot slot=new TimeSlot();
            slot.setDoctor(doctor);
            slot.setSlotDate(date);
            slot.setStartTime(currentTime);
            slot.setEndTime(currentTime.plusMinutes(15));
            slot.setMaxTokens(maxTokensPerSlot);
            slot.setCurrentTokens(0);
            slot.setAvailable(true);
            slots.add(timeSlotRepository.save(slot));
            currentTime=currentTime.plusMinutes(10);
        }
        return slots;
    }
    public List<TimeSlot> getAvailableSlotsForDoctor(Long doctorId, LocalDate date) {
        return timeSlotRepository.findAvailableSlotsForDoctor(doctorId, date);
    }
    public TimeSlot getSlotById(Long slotId) {
        return timeSlotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Time slot not found with id: " + slotId));
    }
    public List<TimeSlot> getAllAvailableSlots() {
        return timeSlotRepository.findAllAvailableSlots();
    }
}
