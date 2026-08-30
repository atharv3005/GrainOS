package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ProcurementStatus {
    REGISTERED,
    GROSS_WEIGHED,
    MOISTURE_TESTED,
    UNLOADED,
    COMPLETED
}

enum class QualityGrade(val label: String, val rateFactor: Double, val maxMoisture: Double) {
    GRADE_A("Grade A (Export / Prime)", 1.0, 12.0),
    GRADE_B("Grade B (Standard)", 0.96, 14.0),
    GRADE_C("Grade C (High Moisture - Drying Yard)", 0.88, 17.0),
    REJECTED("Rejected (> 17% Moisture / High Foreign Matter)", 0.0, 99.0)
}

enum class PaymentStatus {
    PAID,
    PENDING,
    PROCESSING
}

enum class PaymentMode(val label: String) {
    CASH("Cash (रोकड)"),
    RTGS("RTGS / NEFT (बँक ट्रान्सफर)"),
    CHEQUE("Cheque / PDC (धनादेश)")
}

@Entity(tableName = "procurements")
data class ProcurementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tokenNo: String,
    val farmerName: String,
    val mobileNumber: String,
    val village: String,
    val vehicleNumber: String,
    val panNumber: String = "",
    val isPanVerified: Boolean = false,
    val cropType: String = CropType.MAIZE.name,
    val grossWeightKg: Double = 0.0,
    val tareWeightKg: Double = 0.0,
    val netWeightKg: Double = 0.0,
    val bagCount: Int = 0,
    val bagWeightKg: Double = 50.0,
    val moisturePercentage: Double = 0.0,
    val qualityGrade: String = QualityGrade.GRADE_A.name,
    val ratePerQuintal: Double = 0.0,
    val grossBillAmount: Double = 0.0,
    
    // Maharashtra APMC Compliance Engine
    val applyMandiCess: Boolean = false,
    val mandiMarketFee: Double = 0.0, // 1.0% Mandi Shulk
    val mandiSupervisoryCharge: Double = 0.0, // 0.5% Supervisory Charge
    val totalMandiCess: Double = 0.0, // 1.5% Total Cess
    
    // Taxation Engine: TDS 194Q vs TCS 206C(1H)
    val enableTds194q: Boolean = false,
    val cumulativePurchasesInFy: Double = 0.0,
    val isTdsApplicable: Boolean = false,
    val tdsRate: Double = 0.0, // 0.1% or 5.0%
    val tdsDeductedAmount: Double = 0.0,
    val isTcsExempt: Boolean = false, // Flag to prevent double taxation under 206C(1H)
    
    // Final Net Payable
    val totalAmount: Double = 0.0, // Net payable to farmer
    val godownAssigned: String = "Godown A",
    val status: String = ProcurementStatus.REGISTERED.name,
    val paymentStatus: String = PaymentStatus.PENDING.name,
    val paymentMode: String = PaymentMode.CASH.name,
    val utrOrChequeNo: String = "",
    val chequeDate: Long = 0L,
    val isPdc: Boolean = false,
    val pdcCleared: Boolean = true,
    
    // Dual Weighbridge Audit
    val grossWeightMethod: String = "AUTO", // AUTO / MANUAL
    val tareWeightMethod: String = "AUTO", // AUTO / MANUAL
    
    val grossTimestamp: Long = 0L,
    val tareTimestamp: Long = 0L,
    val completedTimestamp: Long = 0L,
    val whatsappEntrySent: Boolean = false,
    val whatsappReceiptSent: Boolean = false,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
