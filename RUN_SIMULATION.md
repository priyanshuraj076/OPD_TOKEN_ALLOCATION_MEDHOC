# 🏥 How to Run OPD Simulation

## ✅ FIXED - Ready to Run!

The simulation has been fixed and is ready to run. Issues resolved:
- ✅ Fixed duplicate token number generation (now database-aware)
- ✅ Fixed duplicate patient booking errors (unique phone numbers)
- ✅ Fixed repository method name (queuePosition → createdAt)
- ✅ **Switched to H2 in-memory database (auto-resets on restart)**

## ⚠️ IMPORTANT: Database Changed

The application is now using **H2 in-memory database** instead of MySQL. This means:
- ✅ Database resets automatically on each restart (perfect for simulation)
- ✅ No need to manually clear old data
- ✅ Fresh start every time you run the application

## Quick Start

### Start the Application with Simulation

```bash
# Navigate to project directory
cd /Users/sujalsharma/Downloads/OPD_allocation

# Stop any running instance first
# Press Ctrl+C if it's running

# Start fresh (H2 database will be created new)
mvn spring-boot:run
```

The simulation will automatically run on startup and demonstrate:
- ✅ Create 3 doctors (Dr. Sharma, Dr. Patel, Dr. Kumar)
- ✅ Create time slots (9:00-10:00, 10:00-11:00, 11:00-12:00)
- ✅ Process online bookings (5 patients)
- ✅ Process walk-ins (3 patients)
- ✅ Handle emergency case (priority allocation)
- ✅ Handle paid priority booking (with slot full scenario)
- ✅ Process cancellations
- ✅ Handle follow-up patients
- ✅ Display statistics

---

## What the Simulation Demonstrates

### Timeline of Events:

**8:00 AM** - System Setup
- Creates 3 doctors with different specializations
- Creates 9 time slots (3 per doctor)
- Each slot has 10 capacity

**8:30 AM** - Online Bookings
- 5 patients book online appointments
- Token allocation with priority rules

**9:00 AM** - Walk-ins Arrive
- 3 walk-in patients register
- Lower priority than online bookings

**9:15 AM** - Emergency Case
- Critical emergency patient arrives
- Gets immediate priority (bumps all others)
- Regular patients wait

**9:30 AM** - Paid Priority Booking
- VIP patient books when slot is FULL
- System automatically bumps a walk-in to next slot
- SMS notification sent to affected patient

**10:15 AM** - Cancellation
- Patient cancels appointment
- Waiting list patients get promoted automatically

**11:00 AM** - Follow-up Patient
- Follow-up gets priority over walk-ins
- System maintains priority rules

**12:00 PM** - Session Ends
- Shows complete statistics
- Demonstrates all edge cases

---

## Manual API Testing (Without Simulation)

If you disable the simulation, you can test manually:

### 1. Create a Doctor
```bash
curl -X POST http://localhost:8080/api/doctors \
  -H "Content-Type: application/json" \
  -d '{"name":"Dr. Sharma","specialization":"Cardiology"}'
```

### 2. Create Slots
```bash
curl -X POST http://localhost:8080/api/slots/1 \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2026-01-30",
    "slots": [
      {"startTime":"09:00","endTime":"10:00","capacity":10},
      {"startTime":"10:00","endTime":"11:00","capacity":10}
    ]
  }'
```

### 3. Allocate Token
```bash
curl -X POST http://localhost:8080/api/tokens/allocate \
  -H "Content-Type: application/json" \
  -d '{
    "patientName": "John Doe",
    "patientPhone": "9876543210",
    "doctorId": 1,
    "date": "2026-01-30",
    "preferredTime": "09:00",
    "tokenType": "online"
  }'
```

### 4. Allocate Emergency
```bash
curl -X POST http://localhost:8080/api/tokens/emergency \
  -H "Content-Type: application/json" \
  -d '{
    "patientName": "Emergency Patient",
    "patientPhone": "9999999999",
    "doctorId": 1,
    "date": "2026-01-30"
  }'
```

### 5. View Slot Availability
```bash
curl http://localhost:8080/api/slots/availability/1/2026-01-30
```

---

## Database Access

The application uses H2 in-memory database:

**H2 Console**: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:opddb`
- Username: `sa`
- Password: (leave blank)

---

## Troubleshooting

### Simulation runs too fast
The simulation runs sequentially without delays. Check the console output for detailed logs.

### Want to run simulation again
Restart the application - the H2 database is in-memory and resets on each restart.

### Errors during simulation
Check the console for detailed error messages. Most common issues:
- Database constraints
- Slot capacity issues
- Token allocation conflicts

---

## Next Steps

After running the simulation:
1. Check H2 console to see the data
2. Test the REST APIs manually
3. Review the audit logs in the database
4. Examine reallocation records
