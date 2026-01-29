# OPD Token Allocation System

A complete hospital OPD token management system built with Spring Boot.

## Features

✅ 5-level priority system (Emergency, Paid Priority, Follow-up, Online, Walk-in)
✅ Smart token allocation algorithm
✅ Slot capacity management
✅ Token reallocation on delays/cancellations
✅ Duplicate booking prevention
✅ Emergency override capability
✅ Complete audit logging
✅ No circular reference issues

## Quick Start

### 1. Setup Database

```sql
CREATE DATABASE opd_token_system;
```

Update `application.properties` with your MySQL credentials.

### 2. Run Application

```bash
mvn spring-boot:run
```

The application will start at `http://localhost:8080`

## API Testing Guide

### Step 1: Create Doctors

```bash
# Create Dr. Sharma (Cardiology)
curl -X POST http://localhost:8080/api/doctors \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Dr. Sharma",
    "specialization": "Cardiology"
  }'

# Create Dr. Patel (General Medicine)
curl -X POST http://localhost:8080/api/doctors \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Dr. Patel",
    "specialization": "General Medicine"
  }'

# Create Dr. Kumar (Orthopedics)
curl -X POST http://localhost:8080/api/doctors \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Dr. Kumar",
    "specialization": "Orthopedics"
  }'
```

### Step 2: Create Time Slots

```bash
# Create slots for Dr. Sharma (doctor_id = 1)
curl -X POST http://localhost:8080/api/doctors/1/slots \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2026-01-30",
    "slots": [
      {"start_time": "09:00", "end_time": "10:00", "max_capacity": 10},
      {"start_time": "10:00", "end_time": "11:00", "max_capacity": 10},
      {"start_time": "11:00", "end_time": "12:00", "max_capacity": 8}
    ]
  }'
```

### Step 3: Check Availability

```bash
curl http://localhost:8080/api/doctors/1/slots?date=2026-01-30
```

### Step 4: Allocate Tokens

**Online Booking:**
```bash
curl -X POST http://localhost:8080/api/tokens/allocate \
  -H "Content-Type: application/json" \
  -d '{
    "patient_name": "Raj Kumar",
    "patient_phone": "9876543210",
    "doctor_id": 1,
    "preferred_date": "2026-01-30",
    "preferred_time": "09:00",
    "token_type": "online"
  }'
```

**Walk-in:**
```bash
curl -X POST http://localhost:8080/api/tokens/allocate \
  -H "Content-Type: application/json" \
  -d '{
    "patient_name": "Walk-in Patient",
    "patient_phone": "9876543211",
    "doctor_id": 1,
    "preferred_date": "2026-01-30",
    "preferred_time": "09:00",
    "token_type": "walkin"
  }'
```

**Emergency:**
```bash
curl -X POST http://localhost:8080/api/tokens/emergency-allocate \
  -H "Content-Type: application/json" \
  -d '{
    "patient_name": "Emergency Patient",
    "patient_phone": "9999999999",
    "doctor_id": 1,
    "date": "2026-01-30"
  }'
```

**Paid Priority:**
```bash
curl -X POST http://localhost:8080/api/tokens/allocate \
  -H "Content-Type: application/json" \
  -d '{
    "patient_name": "VIP Patient",
    "patient_phone": "9876543212",
    "doctor_id": 1,
    "preferred_date": "2026-01-30",
    "preferred_time": "09:00",
    "token_type": "paid_priority"
  }'
```

### Step 5: Query Token Status

```bash
curl http://localhost:8080/api/tokens/T001
```

### Step 6: Cancel Token

```bash
curl -X POST http://localhost:8080/api/tokens/1/cancel \
  -H "Content-Type: application/json" \
  -d '{
    "reason": "Patient unable to come"
  }'
```

### Step 7: Update Token Status

```bash
curl -X PUT http://localhost:8080/api/tokens/1/status \
  -H "Content-Type: application/json" \
  -d '{
    "status": "in_consultation"
  }'
```

### Step 8: Handle Doctor Delay (Reallocation)

```bash
curl -X POST http://localhost:8080/api/slots/1/reallocate-overflow \
  -H "Content-Type: application/json" \
  -d '{
    "reason": "Doctor running 30 minutes late",
    "strategy": "move_to_next_slot"
  }'
```

## Database Schema

### Tables Created:
1. `doctors` - Doctor information
2. `doctor_slots` - Time slot management
3. `tokens` - Patient tokens
4. `reallocations` - Token reallocation history
5. `audit_log` - Event tracking

## Priority System

1. **EMERGENCY** (Highest) - Overrides capacity
2. **PAID_PRIORITY** - Can bump lower priorities
3. **FOLLOWUP** - Returning patients
4. **ONLINE** - Pre-booked
5. **WALKIN** (Lowest) - First-come-first-served

## Token Allocation Algorithm

The system follows this logic:
1. Emergency → Allocate immediately, ignore capacity
2. Check preferred slot availability
3. Paid priority can bump walk-in/online tokens
4. Find next available slot if preferred is full
5. Add to waiting list if all slots full

## Edge Cases Handled

✅ Double booking prevention (pessimistic locking)
✅ Same patient multiple tokens blocked
✅ Emergency during full slot
✅ Doctor delays with auto-reallocation
✅ No-show handling
✅ Waiting list promotion

## Project Structure

```
src/main/java/com/example/OPD_allocation/
├── entity/          # JPA entities (5 tables)
├── repository/      # Data access layer
├── service/         # Business logic
├── controller/      # REST APIs
├── dto/             # Request/Response objects
└── exception/       # Error handling
```

## Technologies

- Spring Boot 4.0.0
- Spring Data JPA
- MySQL 8.0
- Lombok
- Java 21

## License

MIT

