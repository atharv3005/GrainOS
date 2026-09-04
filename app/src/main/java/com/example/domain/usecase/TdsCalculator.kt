package com.example.domain.usecase

/**
 * Result data class for Section 194Q Income Tax TDS calculations.
 */
data class TdsResult(
    val isTdsApplicable: Boolean,
    val tdsRate: Double,            // 0.1% with PAN, 5.0% without PAN
    val tdsDeductedAmount: Double,
    val isTcsExempt: Boolean        // 206C(1H) TCS exemption
)

/**
 * Statutory Income Tax Section 194Q calculation engine.
 * Under Indian Income Tax laws, buyers with annual turnover exceeding ₹10 Crore must deduct
 * 0.1% TDS on aggregate purchase values exceeding ₹50,00,000 (₹50 Lakhs) from a seller in a FY.
 */
class TdsCalculator {

    companion object {
        const val FY_THRESHOLD_AMOUNT = 5000000.0 // ₹50 Lakhs
        const val STANDARD_TDS_RATE_WITH_PAN = 0.001 // 0.1%
        const val HIGHER_TDS_RATE_WITHOUT_PAN = 0.050 // 5.0%
    }

    /**
     * Evaluates TDS deduction under Section 194Q.
     */
    fun calculate(
        currentBillAmount: Double,
        cumulativePurchasesInFy: Double,
        enableTds194q: Boolean,
        hasValidPan: Boolean
    ): TdsResult {
        if (!enableTds194q || currentBillAmount <= 0.0) {
            return TdsResult(
                isTdsApplicable = false,
                tdsRate = 0.0,
                tdsDeductedAmount = 0.0,
                isTcsExempt = false
            )
        }

        val totalPurchases = cumulativePurchasesInFy + currentBillAmount
        if (totalPurchases <= FY_THRESHOLD_AMOUNT) {
            return TdsResult(
                isTdsApplicable = false,
                tdsRate = 0.0,
                tdsDeductedAmount = 0.0,
                isTcsExempt = false
            )
        }

        val taxablePortion = if (cumulativePurchasesInFy >= FY_THRESHOLD_AMOUNT) {
            currentBillAmount
        } else {
            totalPurchases - FY_THRESHOLD_AMOUNT
        }

        val rate = if (hasValidPan) STANDARD_TDS_RATE_WITH_PAN else HIGHER_TDS_RATE_WITHOUT_PAN
        val tdsAmount = taxablePortion * rate

        return TdsResult(
            isTdsApplicable = true,
            tdsRate = rate,
            tdsDeductedAmount = roundToTwoDecimals(tdsAmount),
            isTcsExempt = true
        )
    }

    private fun roundToTwoDecimals(value: Double): Double {
        return Math.round(value * 100.0) / 100.0
    }
}
