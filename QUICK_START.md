# 🚨 IMPORTANT: Action Required to Fix Conflicts

## Problem
The old codebase files are conflicting with the new implementation. You have:
- Old: `TimeSlot.java`, `Patient.java`, `TokenReallocation.java`
- New: `DoctorSlot.java`, `Token.java` (with patient data), `Reallocation.java`

## Solution - Run These Commands in Order:

### Step 1: Delete Old Conflicting Files

```bash
cd /Users/sujalsharma/Downloads/OPD_allocation

# Delete old entities
rm src/main/java/com/example/OPD_allocation/entity/TimeSlot.java
rm src/main/java/com/example/OPD_allocation/entity/Patient.java
rm src/main/java/com/example/OPD_allocation/entity/TokenReallocation.java
rm src/main/java/com/example/OPD_allocation/entity/PriorityLevel.java
rm src/main/java/com/example/OPD_allocation/entity/TokenStatus.java

# Delete old services
rm src/main/java/com/example/OPD_allocation/service/TimeSlotService.java
rm src/main/java/com/example/OPD_allocation/service/PatientService.java
rm src/main/java/com/example/OPD_allocation/service/TokenReallocationService.java

# Delete old controllers
rm src/main/java/com/example/OPD_allocation/controller/TimeSlotController.java
rm src/main/java/com/example/OPD_allocation/controller/PatientController.java
rm src/main/java/com/example/OPD_allocation/controller/TokenReallocationController.java

# Delete old repositories
rm src/main/java/com/example/OPD_allocation/repository/TimeSlotRepository.java
rm src/main/java/com/example/OPD_allocation/repository/PatientRepository.java
rm src/main/java/com/example/OPD_allocation/repository/TokenReallocationRepository.java

# Delete old DTOs
rm src/main/java/com/example/OPD_allocation/dto/DoctorDto.java
rm src/main/java/com/example/OPD_allocation/dto/PatientDto.java
rm src/main/java/com/example/OPD_allocation/dto/TimeSlotDto.java
rm src/main/java/com/example/OPD_allocation/dto/QueueStatusResponse.java

# Delete old exceptions
rm src/main/java/com/example/OPD_allocation/exception/InvalidOperationException.java
rm src/main/java/com/example/OPD_allocation/exception/ResourceNotFoundException.java
rm src/main/java/com/example/OPD_allocation/exception/SlotFullException.java
```

### Step 2: Clean and Compile

```bash
mvn clean compile
```

### Step 3: Run Application

```bash
mvn spring-boot:run
```

---

## 📋 New Implementation Files (Keep These)

### Entities ✅
- `Doctor.java` - Doctor profiles
- `DoctorSlot.java` - Time slots (replaces TimeSlot)
- `Token.java` - Patient tokens (includes patient data, no separate Patient entity)
- `Reallocation.java` - Reallocation tracking (replaces TokenReallocation)
- `AuditLog.java` - Audit logging

### Services ✅
- `DoctorService.java`
- `DoctorSlotService.java`
- `TokenService.java` (Core algorithm)
- `ReallocationService.java`
- `AuditLogService.java`

### Controllers ✅
- `DoctorController.java`
- `TokenController.java`
- `SlotController.java`

### Repositories ✅
- `DoctorRepository.java`
- `DoctorSlotRepository.java`
- `TokenRepository.java`
- `ReallocationRepository.java`
- `AuditLogRepository.java`

---

## 🎯 Quick Test After Cleanup

```bash
# 1. Create doctor
curl -X POST http://localhost:8080/api/doctors \
  -H "Content-Type: application/json" \
  -d '{"name":"Dr. Sharma","specialization":"Cardiology"}'

# 2. Create slots
curl -X POST http://localhost:8080/api/doctors/1/slots \
  -H "Content-Type: application/json" \
  -d '{
    "date":"2026-01-30",
    "slots":[
      {"start_time":"09:00","end_time":"10:00","max_capacity":10}
    ]
  }'

# 3. Allocate token
curl -X POST http://localhost:8080/api/tokens/allocate \
  -H "Content-Type: application/json" \
  -d '{
    "patient_name":"Raj Kumar",
    "patient_phone":"9876543210",
    "doctor_id":1,
    "preferred_date":"2026-01-30",
    "preferred_time":"09:00",
    "token_type":"online"
  }'
```

Expected response:
```json
{
  "success": true,
  "message": null,
  "data": {
    "tokenNumber": "T001",
    "doctorName": "Dr. Sharma",
    "date": "2026-01-30",
    "slotTime": "09:00 - 10:00",
    "estimatedWaitTime": "10 minutes",
    "positionInQueue": 1,
    "status": "waiting"
  }
}
```

---

## ✅ What Changed

| Old | New | Why |
|-----|-----|-----|
| TimeSlot | DoctorSlot | Matches documentation table name `doctor_slots` |
| Patient entity | Data in Token | Documentation stores patient_name/phone in tokens table |
| TokenReallocation | Reallocation | Matches documentation table name `reallocations` |
| Separate enum files | Nested enums | Cleaner, follows documentation |
| maxTokens | maxCapacity | Exact match with documentation |
| currentTokens | currentCount | Exact match with documentation |

---

## 🔧 If You Still Get Errors

1. **Drop and recreate database:**
   ```sql
   DROP DATABASE opd_token_system;
   CREATE DATABASE opd_token_system;
   ```

2. **Clean everything:**
   ```bash
   mvn clean
   rm -rf target/
   ```

3. **Recompile:**
   ```bash
   mvn compile
   ```

---

## 📖 Full Documentation

- See `README.md` for complete API guide
- See `SAMPLE_DATA.md` for all test data
- See `NEW_IMPLEMENTATION.md` for architecture details
- Run `./test_api.sh` for automated testing

The new implementation is **100% based on the documentation** with no circular references and proper token allocation algorithm!

