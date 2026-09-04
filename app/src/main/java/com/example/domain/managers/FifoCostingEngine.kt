package com.example.domain.managers

import com.example.data.model.OutboundDispatchEntity
import com.example.data.model.ProcurementEntity

/**
 * Enterprise FIFO (First-In, First-Out) Inventory Costing Engine.
 * Matches sales shipments against chronologically ordered intake lots to derive exact COGS and realized margins.
 * Handles unmatched shortage lots using weighted average cost rather than inflating margins with ₹0 COGS (BUG-007 Fix).
 */
object FifoCostingEngine {

    data class InventoryLot(
        val tokenNo: String,
        val cropType: String,
        val timestamp: Long,
        var remainingWeightKg: Double,
        val costPerQuintal: Double
    )

    data class FifoCostResult(
        val totalRevenue: Double,
        val totalCogs: Double,
        val realizedGrossMargin: Double,
        val grossMarginPercentage: Double,
        val unmatchedShortageKg: Double = 0.0,
        val remainingStockLots: List<InventoryLot>
    )

    fun calculateFifoCosting(
        procurements: List<ProcurementEntity>,
        dispatches: List<OutboundDispatchEntity>,
        targetCrop: String = "MAIZE"
    ): FifoCostResult {
        // Sort procurements chronologically (oldest first)
        val cropProcurements = procurements.filter { it.cropType == targetCrop }
        val lots = cropProcurements
            .sortedBy { it.createdAt }
            .map {
                InventoryLot(
                    tokenNo = it.tokenNo,
                    cropType = it.cropType,
                    timestamp = it.createdAt,
                    remainingWeightKg = it.netWeightKg,
                    costPerQuintal = it.ratePerQuintal
                )
            }
            .toMutableList()

        val avgRatePerQuintal = if (cropProcurements.isNotEmpty()) {
            val totalWeight = cropProcurements.sumOf { it.netWeightKg }
            if (totalWeight > 0) cropProcurements.sumOf { it.grossBillAmount } / (totalWeight / 100.0) else 2400.0
        } else {
            2400.0
        }

        var totalCogs = 0.0
        var totalUnmatchedShortageKg = 0.0
        val cropDispatches = dispatches.filter { it.cropType == targetCrop }.sortedBy { it.timestamp }
        val totalRevenue: Double = cropDispatches.sumOf { it.totalInvoiceAmount }

        for (dispatch in cropDispatches) {
            var neededKg: Double = dispatch.netLoadedWeightKg

            for (lot in lots) {
                if (neededKg <= 0.0) break
                if (lot.remainingWeightKg <= 0.0) continue

                val consumedKg = neededKg.coerceAtMost(lot.remainingWeightKg)
                val lotCost = (consumedKg / 100.0) * lot.costPerQuintal
                totalCogs += lotCost
                lot.remainingWeightKg -= consumedKg
                neededKg -= consumedKg
            }

            // BUG-007 Fix: Unmatched shortage assigns average cost rather than ₹0 COGS
            if (neededKg > 0.0) {
                val shortageCost = (neededKg / 100.0) * avgRatePerQuintal
                totalCogs += shortageCost
                totalUnmatchedShortageKg += neededKg
            }
        }

        val grossMargin = totalRevenue - totalCogs
        val marginPct = if (totalRevenue > 0) (grossMargin / totalRevenue) * 100.0 else 0.0

        return FifoCostResult(
            totalRevenue = totalRevenue,
            totalCogs = totalCogs,
            realizedGrossMargin = grossMargin,
            grossMarginPercentage = marginPct,
            unmatchedShortageKg = totalUnmatchedShortageKg,
            remainingStockLots = lots.filter { it.remainingWeightKg > 0 }
        )
    }
}
