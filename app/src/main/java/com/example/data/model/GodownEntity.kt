package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.domain.managers.OrganizationContext
import java.util.UUID

@Entity(
    tableName = "godowns",
    indices = [
        Index(value = ["uuid"], unique = true),
        Index(value = ["activeCrop"]),
        Index(value = ["org_code"])
    ]
)
data class GodownEntity(
    @PrimaryKey
    val godownId: String, // "GODOWN_A", "GODOWN_B", "GODOWN_C", "SILO_BAY_1", "SILO_BAY_2", "DRYING_YARD"
    val displayName: String,
    val capacityMt: Double,
    val currentStockMt: Double,
    val activeCrop: String = CropType.MAIZE.name,
    val averageMoisture: Double = 12.4,
    val temperatureCelsius: Double = 24.5,
    val baseCostPerQuintal: Double = 2400.0,
    val cumulativeShrinkageKg: Double = 0.0,
    val shrinkageCapitalizedCost: Double = 0.0,
    val adjustedAvgCostPerQuintal: Double = 2400.0,
    val ventilationStatus: String = "OPTIMAL",
    val lastUpdated: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "uuid")
    val uuid: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "org_code")
    val orgCode: String = OrganizationContext.getCurrentOrgCode(),

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
