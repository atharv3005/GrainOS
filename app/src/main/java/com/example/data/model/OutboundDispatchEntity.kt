package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class DispatchStatus(val label: String) {
    IN_TRANSIT("In Transit (वाहतुकीत)"),
    UNLOADED("Unloaded & Weight Verified (अनलोड झाले)"),
    REJECTED("Rejected at Mill (रद्द / परत)"),
    SETTLED("Settled (हिशोब पूर्ण)")
}

@Entity(tableName = "outbound_dispatches")
data class OutboundDispatchEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dispatchNo: String,
    val buyerName: String,
    val destination: String,
    val vehicleNumber: String,
    val cropType: String,
    val godownSource: String,
    val tareWeightKg: Double,
    val grossWeightKg: Double,
    val netLoadedWeightKg: Double, // Gate Net Weight
    val ratePerQuintal: Double, // Booked contract rate
    val totalInvoiceAmount: Double, // Initial invoice based on gate weight
    
    // Post-Unloading & Actual P&L settlement fields
    val companyUnloadedWeightKg: Double = 0.0, // Final Mill Unloaded Weight
    val weightShortageKg: Double = 0.0, // Gate Net - Company Unloaded
    val companyRateDeductionPenalty: Double = 0.0, // Mill quality/moisture price cut
    val brokerName: String = "",
    val brokerageRatePerQtl: Double = 0.0,
    val finalBrokerageFee: Double = 0.0,
    val loadingLaborCost: Double = 0.0,
    val freightCost: Double = 0.0,
    val bagCost: Double = 0.0,
    val miscCost: Double = 0.0,
    val fifoProcurementCost: Double = 0.0, // Realized FIFO purchase cost from godown batches
    val actualNetRevenue: Double = 0.0, // (Company Unloaded Weight / 100 * Rate) - Penalty
    val actualNetProfit: Double = 0.0, // Actual Net Revenue - (FIFO Cost + Labor + Freight + Bags + Misc + Brokerage)
    
    val status: String = DispatchStatus.IN_TRANSIT.name,
    val timestamp: Long = System.currentTimeMillis(),
    val unloadedTimestamp: Long = 0L,
    val notes: String = ""
)
