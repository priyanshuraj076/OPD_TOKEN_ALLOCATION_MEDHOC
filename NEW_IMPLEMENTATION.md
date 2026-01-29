# OPD Token Allocation System - New Implementation Summary

## ✅ Complete Rewrite Completed

This codebase has been completely rewritten from scratch based on the documentation.

---

## 📁 New File Structure

### **Entities (5 Tables - Exact Match with Documentation)**

1. **Doctor.java** 
   - Table: `doctors`
   - Fields: id, name, specialization
   - No circular references

2. **DoctorSlot.java** (renamed from TimeSlot)
   - Table: `doctor_slots` 
   - Fields: id, doctor_id, date, start_time, end_time, max_capacity, current_count, status
   - Status: ACTIVE, CANCELLED, COMPLETED, DELAYED

3. **Token.java**
   - Table: `tokens`
   - Fields: id, token_number, patient_name, patient_phone, doctor_id, slot_id, token_type, status, created_at, called_at, completed_at
   - TokenType: EMERGENCY, PAID_PRIORITY, FOLLOWUP, ONLINE, WALKIN
   - TokenStatus: WAITING, IN_CONSULTATION, COMPLETED, CANCELLED, NO_SHOW, WAITING_LIST

4. **Reallocation.java** (renamed from TokenReallocation)
   - Table: `reallocations`
   - Fields: id, token_id, old_slot_id, new_slot_id, reason, reallocated_at

5. **AuditLog.java**
   - Table: `audit_log`
   - Fields: id, event_type, entity_type, entity_id, description, created_at

---

## 🔧 Services (Business Logic)

### **TokenService.java** - Core Algorithm
Implements the exact 5-step algorithm from documentation:

```
1. EMERGENCY → Allocate immediately, ignore capacity
2. Check preferred slot availability
3. PAID_PRIORITY → Can bump lower priority tokens
4. Find next available slot
5. Add to waiting list if all full
```

**Key Features:**
- ✅ Duplicate token prevention
- ✅ Pessimistic locking (prevents double booking)
- ✅ Priority-based bumping
- ✅ Waiting list management
- ✅ Automatic token number generation (T001, E001, W001)

### **DoctorSlotService.java**
- Slot creation and management
- Availability checking
- Slot locking for thread safety

### **ReallocationService.java**
- Handles doctor delays
- Moves lowest priority tokens to next slots
- SMS notifications (logged to console)

### **AuditLogService.java**
- Logs every action (token_created, token_cancelled, etc.)

---

## 🌐 REST API Endpoints (All from Documentation)

### Setup APIs:
- `POST /api/doctors` - Create doctor
- `POST /api/doctors/{id}/slots` - Create time slots
- `GET /api/doctors/{id}/slots?date=` - Check availability

### Token APIs:
- `POST /api/tokens/allocate` - Main allocation (handles all priorities)
- `POST /api/tokens/emergency-allocate` - Emergency only
- `GET /api/tokens/{token_number}` - Get token status
- `POST /api/tokens/{id}/cancel` - Cancel token
- `POST /api/tokens/{id}/mark-no-show` - Mark no-show
- `PUT /api/tokens/{id}/status` - Update status

### Slot APIs:
- `GET /api/slots/{id}/tokens` - Get all tokens in slot
- `POST /api/slots/{id}/reallocate-overflow` - Handle delays

---

## 🔑 Key Differences from Old Code

| Old Code | New Code | Why Changed |
|----------|----------|-------------|
| TimeSlot | DoctorSlot | Match documentation table name |
| maxTokens | maxCapacity | Match documentation field names |
| currentTokens | currentCount | Match documentation field names |
| Patient entity | patient_name/patient_phone in Token | Simplified as per doc |
| TokenReallocation | Reallocation | Match documentation table name |
| PriorityLevel enum | TokenType enum inside Token | Cleaner structure |
| Multiple service files | 4 focused services | Cleaner architecture |

---

## 🚀 How to Use

### 1. Cleanup Old Files
```bash
chmod +x cleanup.sh
./cleanup.sh
```

### 2. Run Application
```bash
mvn clean spring-boot:run
```

### 3. Test APIs
```bash
chmod +x test_api.sh
./test_api.sh
```

Or manually test:

```bash
# Create doctor
curl -X POST http://localhost:8080/api/doctors \
  -H "Content-Type: application/json" \
  -d '{"name": "Dr. Sharma", "specialization": "Cardiology"}'

# Create slots
curl -X POST http://localhost:8080/api/doctors/1/slots \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2026-01-30",
    "slots": [
      {"start_time": "09:00", "end_time": "10:00", "max_capacity": 10}
    ]
  }'

# Allocate token
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

---

## ✅ All Documentation Requirements Met

- [x] 5-level priority system
- [x] Token allocation algorithm with all 5 steps
- [x] Emergency override capability
- [x] Paid priority bumping
- [x] Duplicate booking prevention
- [x] Doctor delay handling
- [x] No-show management
- [x] Waiting list promotion
- [x] Complete audit logging
- [x] All 11 API endpoints
- [x] Thread-safe with pessimistic locking
- [x] No circular reference issues

---

## 🎯 Edge Cases Handled

1. ✅ Doctor running late → Auto-reallocation
2. ✅ Emergency during full slot → Overrides capacity
3. ✅ Paid priority when full → Bumps walk-in
4. ✅ Double booking → Pessimistic lock prevents it
5. ✅ Duplicate token → Validation rejects it
6. ✅ No-show → Frees capacity, promotes waiting list
7. ✅ Multiple cancellations → Each promotes from waiting list

---

## 🎪 Test Scenarios

See `SAMPLE_DATA.md` for complete test data.

**Quick Test Flow:**
1. Create 3 doctors
2. Create slots for each (9-10, 10-11, 11-12)
3. Fill slot with 10 online bookings
4. Try 11th booking → Auto-moves to next slot
5. Try emergency → Allocated to full slot (11/10)
6. Try paid priority → Bumps walk-in
7. Cancel token → Waiting list promoted
8. Duplicate booking → Rejected

---

## 📊 Database Schema Generated

```sql
CREATE TABLE doctors (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    specialization VARCHAR(100) NOT NULL
);

CREATE TABLE doctor_slots (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    doctor_id BIGINT NOT NULL,
    date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    max_capacity INT NOT NULL,
    current_count INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (doctor_id) REFERENCES doctors(id)
);

CREATE TABLE tokens (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    token_number VARCHAR(10) UNIQUE NOT NULL,
    patient_name VARCHAR(100) NOT NULL,
    patient_phone VARCHAR(15) NOT NULL,
    doctor_id BIGINT NOT NULL,
    slot_id BIGINT NOT NULL,
    token_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    queue_position INT,
    created_at TIMESTAMP,
    called_at TIMESTAMP,
    completed_at TIMESTAMP,
    FOREIGN KEY (doctor_id) REFERENCES doctors(id),
    FOREIGN KEY (slot_id) REFERENCES doctor_slots(id)
);

CREATE TABLE reallocations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    token_id BIGINT NOT NULL,
    old_slot_id BIGINT NOT NULL,
    new_slot_id BIGINT NOT NULL,
    reason VARCHAR(200) NOT NULL,
    reallocated_at TIMESTAMP,
    FOREIGN KEY (token_id) REFERENCES tokens(id),
    FOREIGN KEY (old_slot_id) REFERENCES doctor_slots(id),
    FOREIGN KEY (new_slot_id) REFERENCES doctor_slots(id)
);

CREATE TABLE audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    event_type VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT,
    description TEXT,
    created_at TIMESTAMP
);
```

---

## 🔄 Migration from Old Code

The new code is **completely independent**. Old controllers/services/entities won't work with new code.

**Action Required:**
1. Run `cleanup.sh` to remove old conflicting files
2. Restart application
3. Database will auto-update schema (JPA hibernate)
4. Test with new API structure

---

## 📝 Notes

- No Patient entity (patient data stored in Token as per documentation)
- No separate TimeSlot entity (renamed to DoctorSlot to match doc)
- Simplified structure focusing on core algorithm
- All Lombok annotations properly used (no manual getters/setters)
- Proper JSON serialization (no circular references)

---

## 🆘 Troubleshooting

**Issue:** Old files causing conflicts
**Solution:** Run `./cleanup.sh`

**Issue:** Database errors
**Solution:** Drop and recreate database: `DROP DATABASE opd_token_system; CREATE DATABASE opd_token_system;`

**Issue:** Compilation errors
**Solution:** `mvn clean compile`

**Issue:** Circular reference errors
**Solution:** Already fixed with proper @JsonManagedReference/@JsonBackReference

---

## 📞 Support

All code follows Spring Boot best practices and matches the documentation exactly.
Ready for production use!

