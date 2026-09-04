package com.example.domain.usecase

/**
 * Result data class for Maharashtra APMC Mandi Cess calculations.
 */
data class ApmcCessResult(
    val marketFee: Double,          // 1.0% Market Fee (बाजार शुल्क)
    val supervisoryCharge: Double,  // 0.5% Supervisory Charge (देखरेख शुल्क)
    val totalCess: Double           // 1.5% Total Mandi Cess
)

/**
 * Statutory calculation engine for Agricultural Produce Market Committee (APMC) cess.
 * Standard statutory Maharashtra APMC rate is 1.0% Market Fee + 0.5% Supervisory Charge = 1.5%.
 */
class ApmcTaxCalculator {

    companion object {
        const val STANDARD_MARKET_FEE_RATE = 0.010       // 1.0%
        const val STANDARD_SUPERVISORY_RATE = 0.005       // 0.5%
        const val STANDARD_TOTAL_CESS_RATE = 0.015        // 1.5%
    }

    /**
     * Calculates the statutory APMC cess breakdown on a gross commodity purchase value.
     */
    fun calculate(grossAmount: Double, applyMandiCess: Boolean): ApmcCessResult {
        if (!applyMandiCess || grossAmount <= 0.0) {
            return ApmcCessResult(0.0, 0.0, 0.0)
        }
        val marketFee = grossAmount * STANDARD_MARKET_FEE_RATE
        val supervisoryCharge = grossAmount * STANDARD_SUPERVISORY_RATE
        val totalCess = marketFee + supervisoryCharge
        return ApmcCessResult(
            marketFee = roundToTwoDecimals(marketFee),
            supervisoryCharge = roundToTwoDecimals(supervisoryCharge),
            totalCess = roundToTwoDecimals(totalCess)
        )
    }

    private fun roundToTwoDecimals(value: Double): Double {
        return Math.round(value * 100.0) / 100.0
    }
}
