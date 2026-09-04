package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.domain.managers.OrganizationContext
import java.util.UUID

/**
 * Allocation type for payment splits.
 */
enum class AllocationType {
    FULL,
    PARTIAL,
    ADVANCE
}

/**
 * Explicit payment-to-payable matching entity.
 * Records how payments are allocated across specific procurement bills, freight invoices, or operational vouchers.
 */
@Entity(
    tableName = "payment_allocations",
    indices = [
        Index(value = ["uuid"], unique = true),
        Index(value = ["payment_uuid"]),
        Index(value = ["payable_uuid"]),
        Index(value = ["allocation_timestamp"]),
        Index(value = ["org_code", "allocation_timestamp"])
    ]
)
data class PaymentAllocationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "uuid")
    val uuid: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "org_code")
    val orgCode: String = OrganizationContext.getCurrentOrgCode(),

    @ColumnInfo(name = "payment_uuid")
    val paymentUuid: String,

    @ColumnInfo(name = "payable_uuid")
    val payableUuid: String, // References procurement UUID or expense UUID

    @ColumnInfo(name = "allocated_amount_paise")
    val allocatedAmountPaise: Long,

    @ColumnInfo(name = "allocated_amount_rupees")
    val allocatedAmountRupees: Double = allocatedAmountPaise / 100.0,

    @ColumnInfo(name = "allocation_timestamp")
    val allocationTimestamp: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "allocation_type")
    val allocationType: String = AllocationType.FULL.name,

    @ColumnInfo(name = "remaining_payable_balance_paise")
    val remainingPayableBalancePaise: Long = 0L,

    @ColumnInfo(name = "notes")
    val notes: String = "",

    @ColumnInfo(name = "allocated_by_user_id")
    val allocatedByUserId: String = "operator",

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
