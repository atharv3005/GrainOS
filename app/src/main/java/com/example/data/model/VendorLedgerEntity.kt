package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.domain.managers.OrganizationContext
import java.util.UUID

enum class VendorType(val label: String) {
    FARMER("Farmer (शेतकरी)"),
    TRANSPORTER("Transporter (वाहतूकदार)"),
    LABOR("Hamal / Labor (हमाली)"),
    BROKER("Broker / Dalal (दलाल / आडत्या)"),
    CORPORATE("Corporate Buyer")
}

enum class TransactionType(val label: String) {
    BILL_CREDIT("Bill Credit (खरेदी पावती जमा)"),
    PAYMENT_DEBIT("Payment Debit (पेमेंट दिले)"),
    ADVANCE_DEBIT("Advance Payment (अ‍ॅडव्हान्स दिले)"),
    PDC_ISSUED("Post-Dated Cheque (PDC धनादेश)"),
    PENALTY_DEDUCTION("Penalty / Deduction (कपात)"),
    PAYMENT_RECEIVED("Payment Received")
}

enum class PdcStatus(val label: String) {
    NONE("N/A"),
    ISSUED("Issued (धनादेश दिला)"),
    PENDING_MATURITY("Pending Maturity (प्रलंबित)"),
    DEPOSITED("Deposited in Bank (बँकेत जमा केला)"),
    PRESENTED("Presented for Clearing (क्लिअरिंगसाठी सादर)"),
    CLEARED("Cleared (पास झाले)"),
    BOUNCED("Bounced / Dishonored (बाउन्स / रद्द)")
}

@Entity(
    tableName = "vendor_ledgers",
    indices = [
        Index(value = ["uuid"], unique = true),
        Index(value = ["party_id"]),
        Index(value = ["vendorType"]),
        Index(value = ["transactionType"]),
        Index(value = ["pdcStatus"]),
        Index(value = ["timestamp"]),
        Index(value = ["org_code", "timestamp"])
    ]
)
data class VendorLedgerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "uuid")
    val uuid: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "org_code")
    val orgCode: String = OrganizationContext.getCurrentOrgCode(),

    @ColumnInfo(name = "party_id")
    val partyId: Long? = null,

    val vendorType: String, // FARMER, TRANSPORTER, LABOR, BROKER, CORPORATE
    val vendorName: String,
    val contactNumber: String = "",
    val panNumber: String = "",
    val transactionType: String, // BILL_CREDIT, PAYMENT_DEBIT, ADVANCE_DEBIT, PDC_ISSUED, PENALTY_DEDUCTION
    val amount: Double,
    val paymentMode: String = "CASH", // CASH, RTGS, CHEQUE
    val utrOrChequeNo: String = "",
    val chequeMaturityDate: Long = 0L,
    val pdcStatus: String = PdcStatus.NONE.name,
    val referenceDocNo: String = "", // e.g., "TK-1081", "EXP-301", "TRD-8821"
    val runningBalance: Double = 0.0,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "deposited_at")
    val depositedAt: Long? = null,

    @ColumnInfo(name = "presented_at")
    val presentedAt: Long? = null,

    @ColumnInfo(name = "cleared_at")
    val clearedAt: Long? = null,

    @ColumnInfo(name = "bounced_at")
    val bouncedAt: Long? = null,

    @ColumnInfo(name = "bounce_reason")
    val bounceReason: String? = null,

    @ColumnInfo(name = "reopened_payable_id")
    val reopenedPayableId: String? = null,

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
