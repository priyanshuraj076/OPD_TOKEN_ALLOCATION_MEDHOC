#!/bin/bash

# OPD Token Allocation System - Complete Test Script
# This script demonstrates all APIs and scenarios from the documentation

BASE_URL="http://localhost:8080/api"

echo "=========================================="
echo "OPD Token Allocation System - Test Script"
echo "=========================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Step 1: Create Doctors
echo -e "${BLUE}Step 1: Creating Doctors...${NC}"
echo ""

echo "Creating Dr. Sharma (Cardiology)..."
curl -X POST "$BASE_URL/doctors" \
  -H "Content-Type: application/json" \
  -d '{"name": "Dr. Sharma", "specialization": "Cardiology"}' \
  | jq '.'
echo ""

echo "Creating Dr. Patel (General Medicine)..."
curl -X POST "$BASE_URL/doctors" \
  -H "Content-Type: application/json" \
  -d '{"name": "Dr. Patel", "specialization": "General Medicine"}' \
  | jq '.'
echo ""

echo "Creating Dr. Kumar (Orthopedics)..."
curl -X POST "$BASE_URL/doctors" \
  -H "Content-Type: application/json" \
  -d '{"name": "Dr. Kumar", "specialization": "Orthopedics"}' \
  | jq '.'
echo ""

# Step 2: Create Slots
echo -e "${BLUE}Step 2: Creating Time Slots for Dr. Sharma...${NC}"
echo ""

curl -X POST "$BASE_URL/doctors/1/slots" \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2026-01-30",
    "slots": [
      {"start_time": "09:00", "end_time": "10:00", "max_capacity": 10},
      {"start_time": "10:00", "end_time": "11:00", "max_capacity": 10},
      {"start_time": "11:00", "end_time": "12:00", "max_capacity": 8}
    ]
  }' | jq '.'
echo ""

# Step 3: Check Availability
echo -e "${BLUE}Step 3: Checking Slot Availability...${NC}"
echo ""

curl "$BASE_URL/doctors/1/slots?date=2026-01-30" | jq '.'
echo ""

# Step 4: Allocate Online Tokens
echo -e "${BLUE}Step 4: Allocating Online Tokens...${NC}"
echo ""

for i in {1..5}; do
  echo "Allocating token $i..."
  curl -X POST "$BASE_URL/tokens/allocate" \
    -H "Content-Type: application/json" \
    -d "{
      \"patient_name\": \"Patient $i\",
      \"patient_phone\": \"987654321$i\",
      \"doctor_id\": 1,
      \"preferred_date\": \"2026-01-30\",
      \"preferred_time\": \"09:00\",
      \"token_type\": \"online\"
    }" | jq '.'
  echo ""
done

# Step 5: Allocate Walk-in
echo -e "${BLUE}Step 5: Allocating Walk-in Tokens...${NC}"
echo ""

for i in {6..8}; do
  echo "Allocating walk-in $i..."
  curl -X POST "$BASE_URL/tokens/allocate" \
    -H "Content-Type: application/json" \
    -d "{
      \"patient_name\": \"Walk-in Patient $i\",
      \"patient_phone\": \"987654321$i\",
      \"doctor_id\": 1,
      \"preferred_date\": \"2026-01-30\",
      \"preferred_time\": \"09:00\",
      \"token_type\": \"walkin\"
    }" | jq '.'
  echo ""
done

# Step 6: Emergency Allocation
echo -e "${BLUE}Step 6: Emergency Allocation (Should Override Capacity)...${NC}"
echo ""

curl -X POST "$BASE_URL/tokens/emergency-allocate" \
  -H "Content-Type: application/json" \
  -d '{
    "patient_name": "Emergency Patient",
    "patient_phone": "9999999999",
    "doctor_id": 1,
    "date": "2026-01-30"
  }' | jq '.'
echo ""

# Step 7: Paid Priority (Should Bump Walk-in)
echo -e "${BLUE}Step 7: Paid Priority Allocation (Should Bump Walk-in)...${NC}"
echo ""

curl -X POST "$BASE_URL/tokens/allocate" \
  -H "Content-Type: application/json" \
  -d '{
    "patient_name": "VIP Patient",
    "patient_phone": "8888888888",
    "doctor_id": 1,
    "preferred_date": "2026-01-30",
    "preferred_time": "09:00",
    "token_type": "paid_priority"
  }' | jq '.'
echo ""

# Step 8: Check Token Status
echo -e "${BLUE}Step 8: Checking Token Status...${NC}"
echo ""

curl "$BASE_URL/tokens/T001" | jq '.'
echo ""

# Step 9: Get Slot Tokens
echo -e "${BLUE}Step 9: Getting All Tokens for Slot 1...${NC}"
echo ""

curl "$BASE_URL/slots/1/tokens" | jq '.'
echo ""

# Step 10: Update Token Status
echo -e "${BLUE}Step 10: Updating Token Status to In Consultation...${NC}"
echo ""

curl -X PUT "$BASE_URL/tokens/1/status" \
  -H "Content-Type: application/json" \
  -d '{"status": "in_consultation"}' | jq '.'
echo ""

# Step 11: Cancel a Token
echo -e "${BLUE}Step 11: Cancelling a Token...${NC}"
echo ""

curl -X POST "$BASE_URL/tokens/2/cancel" \
  -H "Content-Type: application/json" \
  -d '{"reason": "Patient unable to come"}' | jq '.'
echo ""

# Step 12: Mark No-Show
echo -e "${BLUE}Step 12: Marking Token as No-Show...${NC}"
echo ""

curl -X POST "$BASE_URL/tokens/3/mark-no-show" | jq '.'
echo ""

# Step 13: Reallocate Overflow
echo -e "${BLUE}Step 13: Reallocating Overflow due to Doctor Delay...${NC}"
echo ""

curl -X POST "$BASE_URL/slots/1/reallocate-overflow" \
  -H "Content-Type: application/json" \
  -d '{
    "reason": "Doctor running 30 minutes late",
    "strategy": "move_to_next_slot"
  }' | jq '.'
echo ""

# Step 14: Try Duplicate Booking (Should Fail)
echo -e "${BLUE}Step 14: Testing Duplicate Booking Prevention...${NC}"
echo ""

curl -X POST "$BASE_URL/tokens/allocate" \
  -H "Content-Type: application/json" \
  -d '{
    "patient_name": "Patient 1",
    "patient_phone": "9876543211",
    "doctor_id": 1,
    "preferred_date": "2026-01-30",
    "preferred_time": "09:00",
    "token_type": "online"
  }' | jq '.'
echo ""

echo -e "${GREEN}=========================================="
echo "Test Script Completed!"
echo "==========================================${NC}"

