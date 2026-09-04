package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.domain.managers.OrganizationContext
import java.util.UUID

/**
 * Types of physical and accounting stock movements.
 */
enum class InventoryMovementType {
    RECEIPT,      // Inbound farmer grain received
    DISPATCH,     // Outbound sales dispatch
    TRANSFER_IN,  // Inter-godown transfer in
    TRANSFER_OUT, // Inter-godown transfer out
    RETURN,       // Truck rejection returned to stock
    REVERSAL,     // Compensatory error cancellation
    ADJUSTMENT,   // Periodic physical count adjustment
    LOSS,         // Moisture shrinkage or physical damage
    SHRINKAGE,    // Moisture shrinkage (alias for LOSS)
    GAIN,         // Scale or moisture gain
    BLEND,        // Combining multiple lots
    SPLIT,        // Splitting into sub-batches
    REPACK,       // Re-bagging operation
    QUARANTINE,   // High-moisture / infested hold
    RELEASE       // Released from quarantine
}

/**
 * Explicit basis on which quantity is calculated.
 */
enum class QuantityBasis {
    PHYSICAL,   // Raw weighbridge gross - tare
    ACCEPTED,   // Weight after physical dockage / foreign matter cut
    PAYABLE,    // Commercial weight for farmer payout
    INVENTORY,  // Stocked weight placed into godown/silo
    SETTLEMENT  // Final weight agreed at corporate buyer mill
}

/**
 * Append-only immutable inventory movement ledger entity.
 * All warehouse balances are rebuildable by summing movements.
 */
@Entity(
    tableName = "inventory_movements",
    indices = [
        Index(value = ["uuid"], unique = true),
        Index(value = ["facility_id"]),
        Index(value = ["batch_id"]),
        Index(value = ["movement_type"]),
        Index(value = ["timestamp"]),
        Index(value = ["source_entity_uuid"]),
        Index(value = ["org_code", "timestamp"])
    ]
)
data class InventoryMovementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "uuid")
    val uuid: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "org_code")
    val orgCode: String = OrganizationContext.getCurrentOrgCode(),

    @ColumnInfo(name = "movement_type")
    val movementType: String, // InventoryMovementType enum name

    @ColumnInfo(name = "source_entity_type")
    val sourceEntityType: String, // PROCUREMENT, DISPATCH, TRANSFER, etc.

    @ColumnInfo(name = "source_entity_uuid")
    val sourceEntityUuid: String,

    @ColumnInfo(name = "facility_id")
    val facilityId: String, // Godown / Silo ID

    @ColumnInfo(name = "batch_id")
    val batchId: String = "LOT_GEN",

    @ColumnInfo(name = "crop_type")
    val cropType: String = "MAIZE",

    @ColumnInfo(name = "quantity_kg")
    val quantityKg: Double, // Positive for inward, negative for outward

    @ColumnInfo(name = "quantity_grams")
    val quantityGrams: Long = (quantityKg * 1000.0).toLong(),

    @ColumnInfo(name = "quantity_basis")
    val quantityBasis: String = QuantityBasis.INVENTORY.name,

    @ColumnInfo(name = "cost_per_quintal_paise")
    val costPerQuintalPaise: Long = 0L,

    @ColumnInfo(name = "total_value_paise")
    val totalValuePaise: Long = 0L,

    @ColumnInfo(name = "user_id")
    val userId: String = "operator",

    @ColumnInfo(name = "device_id")
    val deviceId: String = "local_device",

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "reason")
    val reason: String = "",

    @ColumnInfo(name = "related_movement_uuids")
    val relatedMovementUuids: String? = null,

    @ColumnInfo(name = "sync_status")
    val syncStatus: String = SyncStatus.PENDING.name,

    @ColumnInfo(name = "synced_at")
    val syncedAt: Long? = null,

    @ColumnInfo(name = "idempotency_key")
    val idempotencyKey: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "organization_id")
    val organizationId: String = "default",

    @ColumnInfo(name = "schema_version")
    val schemaVersion: Int = 1,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
