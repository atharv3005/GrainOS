package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.domain.managers.OrganizationContext
import java.util.UUID

@Entity(
    tableName = "storage_facility_intakes",
    indices = [
        Index(value = ["uuid"], unique = true),
        Index(value = ["storageFacilityId"]),
        Index(value = ["tokenNo"]),
        Index(value = ["intakeTimestamp"]),
        Index(value = ["org_code", "intakeTimestamp"])
    ]
)
data class StorageFacilityIntakeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "uuid")
    val uuid: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "org_code")
    val orgCode: String = OrganizationContext.getCurrentOrgCode(),

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
    val notes: String = "",

    @ColumnInfo(name = "sync_status")
    val syncStatus: String = SyncStatus.PENDING.name,

    @ColumnInfo(name = "synced_at")
    val syncedAt: Long? = null,

    @ColumnInfo(name = "idempotency_key")
    val idempotencyKey: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "device_id")
    val deviceId: String = "local_device",

    @ColumnInfo(name = "organization_id")
    val organizationId: String = "default",

    @ColumnInfo(name = "schema_version")
    val schemaVersion: Int = 1,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
