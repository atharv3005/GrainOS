package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.domain.managers.OrganizationContext
import java.util.UUID

/**
 * Supported statutory document types for sequential numbering.
 */
enum class DocumentType(val prefix: String) {
    GIN("GIN"), // Gate Inbound / Procurement Receipt
    DSP("DSP"), // Outbound Dispatch / Gate Pass
    REC("REC"), // Inventory Reconciliation
    PAY("PAY"), // Payment Voucher
    PDC("PDC"), // Post-Dated Cheque Voucher
    EXP("EXP"), // Operational Expense Voucher
    TRD("TRD"), // Trade Booking Contract
    REJ("REJ")  // Truck Rejection Memo
}

/**
 * Sequential document numbering state table.
 * Ensures continuous, gapless, financial-year based sequence numbers without collisions.
 */
@Entity(
    tableName = "document_sequences",
    indices = [
        Index(value = ["financial_year", "facility_id", "document_type"], unique = true),
        Index(value = ["uuid"], unique = true),
        Index(value = ["org_code", "financial_year"])
    ]
)
data class DocumentSequenceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "uuid")
    val uuid: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "org_code")
    val orgCode: String = OrganizationContext.getCurrentOrgCode(),

    @ColumnInfo(name = "financial_year")
    val financialYear: String, // e.g. "26-27"

    @ColumnInfo(name = "facility_id")
    val facilityId: String = "MAIN",

    @ColumnInfo(name = "document_type")
    val documentType: String, // GIN, DSP, etc.

    @ColumnInfo(name = "series_code")
    val seriesCode: String = "GEN",

    @ColumnInfo(name = "current_sequence")
    val currentSequence: Long = 0L,

    @ColumnInfo(name = "last_used_timestamp")
    val lastUsedTimestamp: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "is_locked")
    val isLocked: Boolean = false,

    @ColumnInfo(name = "locked_at")
    val lockedAt: Long? = null,

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
) {
    fun formatDocumentNumber(seqNumber: Long = currentSequence): String {
        val padded = seqNumber.toString().padStart(5, '0')
        return "$documentType/$financialYear/$padded"
    }
}
