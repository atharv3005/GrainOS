package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.domain.managers.OrganizationContext
import java.util.UUID

/**
 * Inventory Reconciliation & Moisture Shrinkage Capitalization Entity.
 * When physical grain weight drops due to moisture evaporation during warehousing,
 * logs the lost tonnage and capitalizes its acquisition cost across the remaining stock,
 * adjusting the weighted average Cost of Goods Sold (COGS).
 */
@Entity(
    tableName = "inventory_reconciliations",
    indices = [
        Index(value = ["uuid"], unique = true),
        Index(value = ["reconciliationNo"], unique = true),
        Index(value = ["godownId"]),
        Index(value = ["timestamp"]),
        Index(value = ["org_code", "timestamp"])
    ]
)
data class InventoryReconciliationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "uuid")
    val uuid: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "org_code")
    val orgCode: String = OrganizationContext.getCurrentOrgCode(),

    val reconciliationNo: String,
    val godownId: String,
    val cropType: String,
    val initialStockKg: Double,
    val auditedStockKg: Double,
    val lostWeightKg: Double, // initialStockKg - auditedStockKg
    val shrinkagePercentage: Double,
    val initialMoisturePct: Double,
    val currentMoisturePct: Double,
    val originalCostPerKg: Double,
    val capitalizedCostPerRemainingKg: Double,
    val originalCostPerQuintal: Double,
    val adjustedCostPerQuintal: Double,
    val totalLossAmountCapitalized: Double,
    val timestamp: Long = System.currentTimeMillis(),
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
