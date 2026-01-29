#!/bin/bash

# Quick validation script to check if new implementation is ready

echo "🔍 Validating New Implementation..."
echo ""

# Check if new entity files exist
echo "Checking Entity Files:"
files=(
    "src/main/java/com/example/OPD_allocation/entity/Doctor.java"
    "src/main/java/com/example/OPD_allocation/entity/DoctorSlot.java"
    "src/main/java/com/example/OPD_allocation/entity/Token.java"
    "src/main/java/com/example/OPD_allocation/entity/Reallocation.java"
    "src/main/java/com/example/OPD_allocation/entity/AuditLog.java"
)

for file in "${files[@]}"; do
    if [ -f "$file" ]; then
        echo "✓ $file"
    else
        echo "✗ MISSING: $file"
    fi
done

echo ""
echo "Checking Service Files:"
services=(
    "src/main/java/com/example/OPD_allocation/service/DoctorService.java"
    "src/main/java/com/example/OPD_allocation/service/DoctorSlotService.java"
    "src/main/java/com/example/OPD_allocation/service/TokenService.java"
    "src/main/java/com/example/OPD_allocation/service/ReallocationService.java"
    "src/main/java/com/example/OPD_allocation/service/AuditLogService.java"
)

for file in "${services[@]}"; do
    if [ -f "$file" ]; then
        echo "✓ $file"
    else
        echo "✗ MISSING: $file"
    fi
done

echo ""
echo "Checking Controller Files:"
controllers=(
    "src/main/java/com/example/OPD_allocation/controller/DoctorController.java"
    "src/main/java/com/example/OPD_allocation/controller/TokenController.java"
    "src/main/java/com/example/OPD_allocation/controller/SlotController.java"
)

for file in "${controllers[@]}"; do
    if [ -f "$file" ]; then
        echo "✓ $file"
    else
        echo "✗ MISSING: $file"
    fi
done

echo ""
echo "Checking for OLD conflicting files (should not exist):"
old_files=(
    "src/main/java/com/example/OPD_allocation/entity/TimeSlot.java"
    "src/main/java/com/example/OPD_allocation/entity/Patient.java"
    "src/main/java/com/example/OPD_allocation/service/TimeSlotService.java"
    "src/main/java/com/example/OPD_allocation/service/PatientService.java"
    "src/main/java/com/example/OPD_allocation/service/TokenReallocationService.java"
)

conflict_found=false
for file in "${old_files[@]}"; do
    if [ -f "$file" ]; then
        echo "⚠️  CONFLICT: $file (should be removed)"
        conflict_found=true
    fi
done

if [ "$conflict_found" = false ]; then
    echo "✓ No conflicting old files found"
fi

echo ""
echo "=========================================="
if [ "$conflict_found" = true ]; then
    echo "⚠️  Action Required: Run ./cleanup_all.sh"
else
    echo "✅ Implementation is clean and ready!"
    echo "Run: mvn clean spring-boot:run"
fi
echo "=========================================="

