package com.example.domain.managers

import com.example.data.model.ProcurementEntity
import java.util.UUID

/**
 * Enterprise Partial Transaction & Salvage Manager.
 * Supports split unloading when a single truck contains variable quality grain:
 * - Part A: Accepted at standard grade & agreed rate
 * - Part B: Discounted / Salvaged secondary grade or rejected back to vehicle
 */
object PartialTransactionManager {

    data class SplitProcurementResult(
        val primaryAcceptedProcurement: ProcurementEntity,
        val secondarySalvageProcurement: ProcurementEntity?,
        val rejectedWeightKg: Double,
        val totalNetWeightKg: Double,
        val totalPayoutAmount: Double
    )

    fun splitProcurementTransaction(
        baseProcurement: ProcurementEntity,
        acceptedWeightKg: Double,
        acceptedRatePerQuintal: Double,
        salvageWeightKg: Double = 0.0,
        salvageRatePerQuintal: Double = 0.0,
        rejectedWeightKg: Double = 0.0,
        salvageGodown: String = "GODOWN_B"
    ): SplitProcurementResult {
        require(acceptedWeightKg + salvageWeightKg + rejectedWeightKg == baseProcurement.netWeightKg) {
            "Total split weights ($acceptedWeightKg + $salvageWeightKg + $rejectedWeightKg) must equal total net weight ${baseProcurement.netWeightKg} kg."
        }

        // Primary Accepted Voucher
        val primaryBillAmount = (acceptedWeightKg / 100.0) * acceptedRatePerQuintal
        val primaryVoucher = baseProcurement.copy(
            tokenNo = "${baseProcurement.tokenNo}-A",
            netWeightKg = acceptedWeightKg,
            ratePerQuintal = acceptedRatePerQuintal,
            grossBillAmount = primaryBillAmount,
            totalAmount = primaryBillAmount - baseProcurement.tdsDeductedAmount
        )

        // Secondary Salvage Voucher (if any)
        val secondaryVoucher = if (salvageWeightKg > 0) {
            val salvageBillAmount = (salvageWeightKg / 100.0) * salvageRatePerQuintal
            baseProcurement.copy(
                id = 0L,
                uuid = UUID.randomUUID().toString(),
                tokenNo = "${baseProcurement.tokenNo}-B",
                netWeightKg = salvageWeightKg,
                ratePerQuintal = salvageRatePerQuintal,
                grossBillAmount = salvageBillAmount,
                totalAmount = salvageBillAmount,
                godownAssigned = salvageGodown,
                totalMandiCess = 0.0,
                tdsDeductedAmount = 0.0
            )
        } else null

        val totalPayout = primaryVoucher.totalAmount + (secondaryVoucher?.totalAmount ?: 0.0)

        return SplitProcurementResult(
            primaryAcceptedProcurement = primaryVoucher,
            secondarySalvageProcurement = secondaryVoucher,
            rejectedWeightKg = rejectedWeightKg,
            totalNetWeightKg = baseProcurement.netWeightKg,
            totalPayoutAmount = totalPayout
        )
    }
}
