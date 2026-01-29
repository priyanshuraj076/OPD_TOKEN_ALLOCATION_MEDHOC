package com.example.OPD_allocation.service;

import com.example.OPD_allocation.dto.CreateSlotsRequest;
import com.example.OPD_allocation.dto.SlotAvailabilityResponse;
import com.example.OPD_allocation.entity.Doctor;
import com.example.OPD_allocation.entity.DoctorSlot;
import com.example.OPD_allocation.repository.DoctorSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorSlotService {

    private final DoctorSlotRepository slotRepository;
    private final DoctorService doctorService;
    private final AuditLogService auditLogService;

    @Transactional
    public List<Long> createSlots(Long doctorId, CreateSlotsRequest request) {
        Doctor doctor = doctorService.getDoctorById(doctorId);
        List<Long> slotIds = new ArrayList<>();

        for (CreateSlotsRequest.SlotInfo slotInfo : request.getSlots()) {
            DoctorSlot slot = new DoctorSlot();
            slot.setDoctor(doctor);
            slot.setDate(request.getDate());
            slot.setStartTime(LocalTime.parse(slotInfo.getStartTime()));
            slot.setEndTime(LocalTime.parse(slotInfo.getEndTime()));
            slot.setMaxCapacity(slotInfo.getMaxCapacity());
            slot.setCurrentCount(0);
            slot.setStatus(DoctorSlot.SlotStatus.ACTIVE);

            DoctorSlot saved = slotRepository.save(slot);
            slotIds.add(saved.getId());

            auditLogService.log("SLOT_CREATED", "slot", saved.getId(),
                "Slot created for Dr. " + doctor.getName() + " on " + request.getDate() +
                " from " + slotInfo.getStartTime() + " to " + slotInfo.getEndTime());
        }

        return slotIds;
    }

    public SlotAvailabilityResponse getSlotAvailability(Long doctorId, LocalDate date) {
        Doctor doctor = doctorService.getDoctorById(doctorId);
        List<DoctorSlot> slots = slotRepository.findByDoctorAndDateOrderByStartTime(doctor, date);

        SlotAvailabilityResponse.DoctorInfo doctorInfo = new SlotAvailabilityResponse.DoctorInfo(
            doctor.getId(), doctor.getName(), doctor.getSpecialization()
        );

        List<SlotAvailabilityResponse.SlotInfo> slotInfos = slots.stream()
            .map(slot -> new SlotAvailabilityResponse.SlotInfo(
                slot.getId(),
                slot.getTimeRange(),
                slot.getMaxCapacity(),
                slot.getCurrentCount(),
                slot.getMaxCapacity() - slot.getCurrentCount(),
                slot.getStatus().name().toLowerCase()
            ))
            .collect(Collectors.toList());

        return new SlotAvailabilityResponse(doctorInfo, date.toString(), slotInfos);
    }

    public DoctorSlot getSlotById(Long slotId) {
        return slotRepository.findById(slotId)
            .orElseThrow(() -> new RuntimeException("Slot not found with id: " + slotId));
    }

    public DoctorSlot getSlotByIdWithLock(Long slotId) {
        return slotRepository.findByIdWithLock(slotId)
            .orElseThrow(() -> new RuntimeException("Slot not found with id: " + slotId));
    }

    public List<DoctorSlot> findAvailableSlots(Long doctorId, LocalDate date) {
        return slotRepository.findAvailableSlots(doctorId, date);
    }

    public DoctorSlot findCurrentActiveSlot(Long doctorId, LocalDate date, LocalTime currentTime) {
        return slotRepository.findCurrentActiveSlot(doctorId, date, currentTime)
            .orElse(null);
    }
}

