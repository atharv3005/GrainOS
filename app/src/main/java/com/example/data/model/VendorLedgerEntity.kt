package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

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
    PENDING_MATURITY("Pending Maturity (प्रलंबित)"),
    CLEARED("Cleared (पास झाले)"),
    BOUNCED("Bounced / Cancelled (रद्द)")
}

@Entity(tableName = "vendor_ledgers")
data class VendorLedgerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val vendorType: String, // FARMER, TRANSPORTER, LABOR, BROKER
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
    val timestamp: Long = System.currentTimeMillis()
)
