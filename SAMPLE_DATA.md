### Sample Test Data for OPD Token Allocation System

## 1. Create Doctors

### POST /api/doctors

```json
{"name": "Dr. Sharma", "specialization": "Cardiology"}
```

```json
{"name": "Dr. Patel", "specialization": "General Medicine"}
```

```json
{"name": "Dr. Kumar", "specialization": "Orthopedics"}
```

---

## 2. Create Time Slots

### POST /api/doctors/1/slots

```json
{
  "date": "2026-01-30",
  "slots": [
    {"start_time": "09:00", "end_time": "10:00", "max_capacity": 10},
    {"start_time": "10:00", "end_time": "11:00", "max_capacity": 10},
    {"start_time": "11:00", "end_time": "12:00", "max_capacity": 8}
  ]
}
```

---

## 3. Allocate Tokens

### POST /api/tokens/allocate

**Online Booking:**
```json
{
  "patient_name": "Raj Kumar",
  "patient_phone": "9876543210",
  "doctor_id": 1,
  "preferred_date": "2026-01-30",
  "preferred_time": "09:00",
  "token_type": "online"
}
```

**Walk-in:**
```json
{
  "patient_name": "Amit Sharma",
  "patient_phone": "9876543211",
  "doctor_id": 1,
  "preferred_date": "2026-01-30",
  "preferred_time": "09:00",
  "token_type": "walkin"
}
```

**Follow-up:**
```json
{
  "patient_name": "Priya Patel",
  "patient_phone": "9876543212",
  "doctor_id": 1,
  "preferred_date": "2026-01-30",
  "preferred_time": "10:00",
  "token_type": "followup"
}
```

**Paid Priority:**
```json
{
  "patient_name": "VIP Patient",
  "patient_phone": "9876543213",
  "doctor_id": 1,
  "preferred_date": "2026-01-30",
  "preferred_time": "09:00",
  "token_type": "paid_priority"
}
```

---

## 4. Emergency Allocation

### POST /api/tokens/emergency-allocate

```json
{
  "patient_name": "Emergency Patient",
  "patient_phone": "9999999999",
  "doctor_id": 1,
  "date": "2026-01-30"
}
```

---

## 5. Cancel Token

### POST /api/tokens/{token_id}/cancel

```json
{
  "reason": "Patient unable to come"
}
```

---

## 6. Update Status

### PUT /api/tokens/{token_id}/status

```json
{
  "status": "in_consultation"
}
```

---

## 7. Reallocate Overflow

### POST /api/slots/{slot_id}/reallocate-overflow

```json
{
  "reason": "Doctor running 30 minutes late",
  "strategy": "move_to_next_slot"
}
```

---

## Complete Test Scenario

Fill all 10 slots for 9-10 AM, then:

1. Try adding 11th online booking → Should move to 10-11 AM
2. Try paid_priority when full → Should bump a walk-in patient
3. Try emergency when full → Should allocate anyway (11/10)
4. Cancel 2 tokens → Check waiting list promotion
5. Mark no-show → Free capacity
6. Duplicate booking with same phone → Should reject

---

## Expected Responses

### Success Response:
```json
{
  "success": true,
  "message": null,
  "data": {
    "token_number": "T001",
    "doctor_name": "Dr. Sharma",
    "date": "2026-01-30",
    "slot_time": "09:00 - 10:00",
    "estimated_wait_time": "30 minutes",
    "position_in_queue": 5,
    "status": "waiting"
  }
}
```

### Error Response:
```json
{
  "success": false,
  "message": "You already have a token for this doctor today",
  "data": null
}
```

