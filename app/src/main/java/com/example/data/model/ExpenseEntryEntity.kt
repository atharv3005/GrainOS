package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.domain.managers.OrganizationContext
import java.util.UUID

/**
 * Manual Expense Entry Entity.
 * Allows traders to manually enter exact fluctuating expenses at the time of transaction:
 * - Labor cost (₹) (Hamali)
 * - Bags cost (Calculated & entered PER TRUCK LOADING, e.g. ~60kg/50kg bags)
 * - Transport / Freight cost (₹)
 * - Miscellaneous / Weighment & Handling costs (₹) with custom description (e.g., Toll Taxes, Quality Penalty)
 */
@Entity(
    tableName = "manual_expenses",
    indices = [
        Index(value = ["uuid"], unique = true),
        Index(value = ["expenseNo"], unique = true),
        Index(value = ["party_id"]),
        Index(value = ["timestamp"]),
        Index(value = ["org_code", "timestamp"])
    ]
)
data class ExpenseEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "uuid")
    val uuid: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "org_code")
    val orgCode: String = OrganizationContext.getCurrentOrgCode(),

    @ColumnInfo(name = "party_id")
    val partyId: Long? = null,

    val expenseNo: String,
    val truckOrBatchRef: String,
    val cropType: String,
    val laborCost: Double,
    val bagsCost: Double, // Entered per truck loading
    val transportCost: Double,
    val miscCost: Double,
    val miscDescription: String = "", // e.g. "Toll Taxes", "Mandi Weighment Slip", "Quality Rate Cut"
    val totalExpense: Double = laborCost + bagsCost + transportCost + miscCost,
    val paidToOrParty: String,
    val paymentMode: String = "CASH", // CASH, RTGS, CHEQUE
    val utrOrChequeNo: String = "",
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
