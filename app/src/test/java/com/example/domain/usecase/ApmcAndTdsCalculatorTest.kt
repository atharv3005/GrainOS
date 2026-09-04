package com.example.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ApmcAndTdsCalculatorTest {

    private val apmcCalculator = ApmcTaxCalculator()
    private val tdsCalculator = TdsCalculator()

    @Test
    fun testApmcCessCalculation_standardMaharashtraRate() {
        val grossAmount = 100000.0 // ₹1 Lakh
        val result = apmcCalculator.calculate(grossAmount, applyMandiCess = true)

        assertEquals(1000.0, result.marketFee, 0.01) // 1.0%
        assertEquals(500.0, result.supervisoryCharge, 0.01) // 0.5%
        assertEquals(1500.0, result.totalCess, 0.01) // 1.5% Total
    }

    @Test
    fun testApmcCessCalculation_whenDisabled_returnsZero() {
        val grossAmount = 100000.0
        val result = apmcCalculator.calculate(grossAmount, applyMandiCess = false)

        assertEquals(0.0, result.marketFee, 0.01)
        assertEquals(0.0, result.supervisoryCharge, 0.01)
        assertEquals(0.0, result.totalCess, 0.01)
    }

    @Test
    fun testTds194q_underThreshold_notApplicable() {
        val currentBill = 200000.0
        val cumulativeBefore = 3000000.0 // ₹30 Lakhs (under ₹50L threshold)

        val result = tdsCalculator.calculate(
            currentBillAmount = currentBill,
            cumulativePurchasesInFy = cumulativeBefore,
            enableTds194q = true,
            hasValidPan = true
        )

        assertFalse(result.isTdsApplicable)
        assertEquals(0.0, result.tdsDeductedAmount, 0.01)
    }

    @Test
    fun testTds194q_crossingThreshold_calculatesOnExcessOnly() {
        val currentBill = 500000.0 // ₹5 Lakhs
        val cumulativeBefore = 4800000.0 // ₹48 Lakhs (Total ₹53L -> ₹3L excess)

        val result = tdsCalculator.calculate(
            currentBillAmount = currentBill,
            cumulativePurchasesInFy = cumulativeBefore,
            enableTds194q = true,
            hasValidPan = true
        )

        assertTrue(result.isTdsApplicable)
        assertEquals(0.001, result.tdsRate, 0.0001) // 0.1%
        assertEquals(300.0, result.tdsDeductedAmount, 0.01) // 0.1% of ₹300,000 = ₹300
        assertTrue(result.isTcsExempt)
    }

    @Test
    fun testTds194q_withoutValidPan_deductsHigherRate() {
        val currentBill = 100000.0
        val cumulativeBefore = 6000000.0 // Already exceeded ₹50L

        val result = tdsCalculator.calculate(
            currentBillAmount = currentBill,
            cumulativePurchasesInFy = cumulativeBefore,
            enableTds194q = true,
            hasValidPan = false
        )

        assertTrue(result.isTdsApplicable)
        assertEquals(0.050, result.tdsRate, 0.0001) // 5.0%
        assertEquals(5000.0, result.tdsDeductedAmount, 0.01) // 5% of ₹100,000 = ₹5,000
    }
}
