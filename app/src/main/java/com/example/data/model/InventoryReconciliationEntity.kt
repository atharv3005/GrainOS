package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Inventory Reconciliation & Moisture Shrinkage Capitalization Entity.
 * When physical grain weight drops due to moisture evaporation during warehousing,
 * logs the lost tonnage and capitalizes its acquisition cost across the remaining stock,
 * adjusting the weighted average Cost of Goods Sold (COGS).
 */
@Entity(tableName = "inventory_reconciliations")
data class InventoryReconciliationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val reconciliationNo: String,
    val godownId: String,
    val cropType: String,
    val initialStockKg: Double,
    val auditedStockKg: Double,
    val lostWeightKg: Double, // initialStockKg - auditedStockKg
    val shrinkagePercentage: Double,
    val initialMoisturePct: Double,
    val currentMoisturePct: Double,
    val originalCostPerKg: Double,
    val capitalizedCostPerRemainingKg: Double,
    val originalCostPerQuintal: Double,
    val adjustedCostPerQuintal: Double,
    val totalLossAmountCapitalized: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
)
