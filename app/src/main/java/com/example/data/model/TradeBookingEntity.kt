package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trade_bookings")
data class TradeBookingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tradeNo: String,
    val cropType: String,
    val brokerOrBuyerName: String,
    val quantityTons: Double,
    val bookedPricePerQuintal: Double,      // Locked-in broker selling price in ₹/qtl
    val farmerPurchasePricePerQuintal: Double, // Procurement cost given to farmer in ₹/qtl
    val laborPerQuintal: Double = 18.0,
    val bagCostPerQuintal: Double = 25.0,
    val transportPerQuintal: Double = 35.0,
    val brokeragePerQuintal: Double = 12.0,
    val tradeStatus: String = "ACTIVE",     // ACTIVE, EXECUTED, SETTLED, UNLOADED_AT_COMPANY
    val tradeTimestamp: Long = System.currentTimeMillis(),
    val notes: String = "",
    val finalCompanyUnloadedWeightKg: Double = 0.0,
    val gateWeightKg: Double = 0.0,
    val actualLoggedExpenses: Double = 0.0
) {
    // 1 Metric Ton = 10 Quintals
    val totalQuintals: Double get() = quantityTons * 10.0
    val finalCompanyUnloadedQuintals: Double get() = if (finalCompanyUnloadedWeightKg > 0) finalCompanyUnloadedWeightKg / 100.0 else totalQuintals
    val gateWeightQuintals: Double get() = if (gateWeightKg > 0) gateWeightKg / 100.0 else totalQuintals

    // Financial Calculation Engine
    val totalRevenue: Double get() = bookedPricePerQuintal * finalCompanyUnloadedQuintals
    val totalProcurementCost: Double get() = farmerPurchasePricePerQuintal * gateWeightQuintals
    val overheadPerQuintal: Double get() = laborPerQuintal + bagCostPerQuintal + transportPerQuintal + brokeragePerQuintal
    val totalOverhead: Double get() = (overheadPerQuintal * gateWeightQuintals) + actualLoggedExpenses
    val netProfit: Double get() = totalRevenue - (totalProcurementCost + totalOverhead)
    val netMarginPerQuintal: Double get() = if (finalCompanyUnloadedQuintals > 0) netProfit / finalCompanyUnloadedQuintals else 0.0

    val roiPercentage: Double get() {
        val totalCost = totalProcurementCost + totalOverhead
        return if (totalCost > 0) (netProfit / totalCost) * 100.0 else 0.0
    }
}
