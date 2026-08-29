package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Truck Rejection & Manual Loss Entity.
 * When purchasing corporate mills reject a loaded truck, captures:
 * - Transport Loss (₹ manually entered freight loss)
 * - Demurrage / Penalties (₹ manually entered holding charges)
 * - Original Loading Labor Cost (₹)
 * - Return Bag Shifting Labor Cost (₹ automatically calculated at EXACTLY 50% of original loading labor)
 * - Quality / Salvage deduction (₹ manually entered discount/price cut)
 * - Final Net Rejection Loss to be deducted from P&L operations.
 */
@Entity(tableName = "truck_rejections")
data class TruckRejectionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val rejectionNo: String,
    val truckNumber: String,
    val buyerOrCompany: String,
    val cropType: String,
    val dispatchedWeightKg: Double,
    val rejectionReason: String, // e.g., "High Moisture >14.2%", "Weevil / Infestation", "Foreign Matter >2%"
    val transportLoss: Double, // User manually entered freight loss
    val penaltiesDemurrage: Double, // User manually entered penalty/holding charges
    val originalLoadingLaborCost: Double = 0.0, // Original loading labor
    val returnBagShiftingLaborCost: Double = 0.0, // EXACTLY 50% of original labor cost
    val qualitySalvageDeduction: Double, // User manually entered discount/price cut
    val totalRejectionLoss: Double = transportLoss + penaltiesDemurrage + qualitySalvageDeduction + returnBagShiftingLaborCost,
    val salvageAction: String, // e.g., "Returned to Yard for Aeration", "Diverted to Local Poultry Mill", "Reprocessed"
    val salvageRealizedRatePerQtl: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
)
