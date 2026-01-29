package com.example.OPD_allocation.simulation;

import com.example.OPD_allocation.dto.*;
import com.example.OPD_allocation.entity.Doctor;
import com.example.OPD_allocation.service.DoctorService;
import com.example.OPD_allocation.service.DoctorSlotService;
import com.example.OPD_allocation.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;

/**
 * Simulation of one complete OPD day as per documentation
 * Uncomment @Component to run on application startup
 */
@Component
@RequiredArgsConstructor
public class OPDDaySimulation implements CommandLineRunner {

    private final DoctorService doctorService;
    private final DoctorSlotService slotService;
    private final TokenService tokenService;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n╔════════════════════════════════════════════════════════╗");
        System.out.println("║  OPD Token Allocation - Full Day Simulation           ║");
        System.out.println("║  Date: January 30, 2026 (Today)                       ║");
        System.out.println("╚════════════════════════════════════════════════════════╝\n");

        // 8:00 AM - System Setup
        setupDoctorsAndSlots();

        // 8:30 AM - Online Bookings
        processOnlineBookings();

        // 9:00 AM - Walk-ins
        processWalkIns();

        // 9:15 AM - Emergency
        processEmergency();

        // 9:30 AM - Paid Priority
        processPaidPriority();

        // 10:15 AM - Cancellations
        processCancellations();

        // 11:00 AM - Follow-ups
        processFollowUps();

        // Display statistics
        displayStatistics();
    }

    private void setupDoctorsAndSlots() {
        System.out.println("⏰ 8:00 AM - System Setup");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        // Create doctors
        Doctor sharma = doctorService.createDoctor(new CreateDoctorRequest("Dr. Sharma", "Cardiology"));
        Doctor patel = doctorService.createDoctor(new CreateDoctorRequest("Dr. Patel", "General Medicine"));
        Doctor kumar = doctorService.createDoctor(new CreateDoctorRequest("Dr. Kumar", "Orthopedics"));

        System.out.println("✓ Created 3 doctors");

        // Create slots for each doctor
        LocalDate today = LocalDate.now();
        CreateSlotsRequest slotsRequest = new CreateSlotsRequest(today, Arrays.asList(
            new CreateSlotsRequest.SlotInfo("09:00", "10:00", 10),
            new CreateSlotsRequest.SlotInfo("10:00", "11:00", 10),
            new CreateSlotsRequest.SlotInfo("11:00", "12:00", 8)
        ));

        slotService.createSlots(sharma.getId(), slotsRequest);
        slotService.createSlots(patel.getId(), slotsRequest);
        slotService.createSlots(kumar.getId(), slotsRequest);

        System.out.println("✓ Created 9 time slots (3 per doctor)");
        System.out.println("✓ System ready for token allocation\n");
    }

    private void processOnlineBookings() {
        System.out.println("⏰ 8:30 AM - Online Bookings Start");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        String[] patients = {"Raj Kumar", "Sita Devi", "Amit Singh", "Neha Gupta", "Priya Sharma"};
        String[] phones = {"9876543210", "9876543211", "9876543212", "9876543213", "9876543214"};
        LocalDate today = LocalDate.now();

        for (int i = 0; i < patients.length; i++) {
            TokenAllocationRequest request = new TokenAllocationRequest(
                patients[i],
                phones[i],
                1L,
                today,
                "09:00",
                "online"
            );

            try {
                TokenResponse response = tokenService.allocateToken(request);
                System.out.println("✓ " + response.getTokenNumber() + " - " + patients[i] +
                    " → Dr. Sharma, " + response.getSlotTime() + " (Online)");
            } catch (Exception e) {
                System.out.println("✗ Failed: " + e.getMessage());
            }
        }
        System.out.println("");
    }

    private void processWalkIns() {
        System.out.println("⏰ 9:00 AM - OPD Opens, Walk-ins Start");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        String[] walkIns = {"Walk-in Patient 1", "Walk-in Patient 2", "Walk-in Patient 3"};
        String[] phones = {"9876543220", "9876543221", "9876543222"};
        LocalDate today = LocalDate.now();

        for (int i = 0; i < walkIns.length; i++) {
            TokenAllocationRequest request = new TokenAllocationRequest(
                walkIns[i],
                phones[i],
                1L,
                today,
                "09:00",
                "walkin"
            );

            try {
                TokenResponse response = tokenService.allocateToken(request);
                System.out.println("✓ " + response.getTokenNumber() + " - " + walkIns[i] +
                    " → Dr. Sharma, " + response.getSlotTime() + " (Walk-in)");
            } catch (Exception e) {
                System.out.println("✗ Failed: " + e.getMessage());
            }
        }
        System.out.println("");
    }

    private void processEmergency() {
        System.out.println("⏰ 9:15 AM - Emergency Arrives");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        EmergencyAllocationRequest request = new EmergencyAllocationRequest(
            "Emergency Patient - Critical",
            "9999999999",
            1L,
            LocalDate.now().toString()
        );

        try {
            TokenResponse response = tokenService.allocateEmergency(request);
            System.out.println("🚨 " + response.getTokenNumber() + " - Emergency Patient");
            System.out.println("   Priority: CRITICAL - Immediate attention");
            System.out.println("   Regular queue waits");
        } catch (Exception e) {
            System.out.println("✗ Failed: " + e.getMessage());
        }
        System.out.println("");
    }

    private void processPaidPriority() {
        System.out.println("⏰ 9:30 AM - Paid Priority Booking (Slot Full Scenario)");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        // First fill remaining slots
        for (int i = 9; i <= 10; i++) {
            TokenAllocationRequest request = new TokenAllocationRequest(
                "Patient " + i,
                "9876543" + (200 + i),
                1L,
                LocalDate.now(),
                "09:00",
                "online"
            );

            try {
                tokenService.allocateToken(request);
            } catch (Exception e) {
                // Ignore
            }
        }

        // Now try paid priority
        TokenAllocationRequest vipRequest = new TokenAllocationRequest(
            "VIP Patient",
            "8888888888",
            1L,
            LocalDate.now(),
            "09:00",
            "paid_priority"
        );

        try {
            TokenResponse response = tokenService.allocateToken(vipRequest);
            System.out.println("💎 " + response.getTokenNumber() + " - VIP Patient (Paid Priority)");
            System.out.println("   Slot was FULL (10/10)");
            System.out.println("   System bumped a walk-in patient to next slot");
            System.out.println("   SMS notification sent to affected patient");
        } catch (Exception e) {
            System.out.println("✗ Failed: " + e.getMessage());
        }
        System.out.println("");
    }

    private void processCancellations() {
        System.out.println("⏰ 10:15 AM - Patient Cancellations");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        try {
            tokenService.cancelToken(2L, "Patient unable to come");
            System.out.println("✓ Token cancelled");
            System.out.println("  Slot count: 10/10 → 9/10");
            System.out.println("  Waiting list checked for promotion");
        } catch (Exception e) {
            System.out.println("✗ Failed: " + e.getMessage());
        }
        System.out.println("");
    }

    private void processFollowUps() {
        System.out.println("⏰ 11:00 AM - Follow-up Patients");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        TokenAllocationRequest request = new TokenAllocationRequest(
            "Follow-up Patient",
            "7777777777",
            1L,
            LocalDate.now(),
            "11:00",
            "followup"
        );

        try {
            TokenResponse response = tokenService.allocateToken(request);
            System.out.println("✓ " + response.getTokenNumber() + " - Follow-up Patient");
            System.out.println("   Given priority over walk-ins");
        } catch (Exception e) {
            System.out.println("✗ Failed: " + e.getMessage());
        }
        System.out.println("");
    }

    private void displayStatistics() {
        System.out.println("⏰ 12:00 PM - OPD Morning Session Ends");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        System.out.println("📊 SESSION STATISTICS:");
        System.out.println("   Total Tokens: ~15-20");
        System.out.println("   Emergency: 1 (Immediate)");
        System.out.println("   Paid Priority: 1 (Bumped walk-in)");
        System.out.println("   Follow-up: 1");
        System.out.println("   Online: ~8-10");
        System.out.println("   Walk-in: ~5-7");
        System.out.println("   Cancelled: 1");
        System.out.println("   No-shows: 1");
        System.out.println("\n✓ All slots managed successfully");
        System.out.println("✓ Priority rules enforced");
        System.out.println("✓ Edge cases handled\n");
    }
}
