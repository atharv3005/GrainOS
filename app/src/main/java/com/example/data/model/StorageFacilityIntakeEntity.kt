package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "storage_facility_intakes")
data class StorageFacilityIntakeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val storageFacilityId: String,       // e.g. "GODOWN_A", "GODOWN_B", "SILO_BAY_1"
    val storageFacilityName: String,     // e.g. "Godown A (Main Silo)"
    val tokenNo: String,                 // e.g. "GP-84201" or "TK-1081"
    val farmerName: String,
    val mobileNumber: String = "",
    val village: String = "",
    val vehicleNumber: String,
    val cropType: String = CropType.MAIZE.name,
    val qualityGrade: String = "GRADE_A",
    val grossWeightKg: Double = 0.0,
    val tareWeightKg: Double = 0.0,
    val netWeightKg: Double,
    val netWeightMt: Double = netWeightKg / 1000.0,
    val bagCount: Int = (netWeightKg / 50.0).toInt().coerceAtLeast(1),
    val bagWeightKg: Double = 50.0,
    val moisturePercentage: Double = 12.0,
    val temperatureCelsius: Double = 24.0,
    val ratePerQuintal: Double = 2400.0,
    val grossBillAmount: Double = 0.0,
    val totalAmount: Double = 0.0,
    val paymentStatus: String = "PAID",
    val paymentMode: String = "CASH",
    val intakeTimestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
)
