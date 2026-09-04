package com.example.domain.managers

import com.example.data.local.CashDrawerDao
import com.example.data.local.ExpenseDao
import com.example.data.local.ProcurementDao
import com.example.data.local.VendorLedgerDao
import com.example.data.model.CashDrawerCountEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Enterprise Cash Drawer & Physical Currency Reconciliation Manager.
 * Computes system opening/closing cash ledger balance and compares against operator physical count.
 */
class CashDrawerReconciliationManager(
    private val cashDrawerDao: CashDrawerDao,
    private val procurementDao: ProcurementDao,
    private val expenseDao: ExpenseDao,
    private val vendorLedgerDao: VendorLedgerDao
) {

    data class CashReconciliationSummary(
        val openingBalance: Double,
        val totalCashInflows: Double,
        val totalCashOutflows: Double,
        val totalCashExpenses: Double,
        val totalCashProcurements: Double,
        val calculatedSystemCash: Double,
        val physicalCashCounted: Double,
        val variance: Double,
        val isBalanced: Boolean
    )

    suspend fun reconcileDailyCash(
        notes500: Int,
        notes200: Int,
        notes100: Int,
        notes50: Int,
        notes20: Int,
        notes10: Int,
        coinsTotal: Double,
        countedBy: String = "operator",
        notes: String = ""
    ): Result<CashDrawerCountEntity> = withContext(Dispatchers.IO) {
        try {
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date())

            // 1. Dynamic Opening Cash Balance (from previous count or default to 0.0 if initial day)
            val previousCount = cashDrawerDao.getLatestCount()
            val openingBalance = previousCount?.physicalCashTotal ?: 0.0

            // 2. Inflows and Outflows
            val procurements = procurementDao.getAllProcurementsDirect()
            val totalCashProcurements = procurements.filter { it.paymentMode == "CASH" }.sumOf { it.totalAmount }

            val ledgers = vendorLedgerDao.getAllLedgersDirect()
            val cashInflows = ledgers.filter { it.transactionType == "PAYMENT_RECEIVED" && it.paymentMode == "CASH" }.sumOf { it.amount }
            val cashOutflows = ledgers.filter { it.transactionType == "PAYMENT_DEBIT" && it.paymentMode == "CASH" }.sumOf { it.amount }

            val expenses = expenseDao.getAllExpensesDirect()
            val cashExpenses = expenses.filter { it.paymentMode == "CASH" }.sumOf { it.totalExpense }

            // Dynamic system cash formula - BUG-003 Fix
            val systemCash = (openingBalance + cashInflows) - (cashOutflows + totalCashProcurements + cashExpenses)

            val countEntity = CashDrawerCountEntity(
                countDate = todayStr,
                notes500 = notes500,
                notes200 = notes200,
                notes100 = notes100,
                notes50 = notes50,
                notes20 = notes20,
                notes10 = notes10,
                coinsTotal = coinsTotal,
                systemCashBalance = systemCash,
                varianceReason = notes,
                countedBy = countedBy
            )

            val id = cashDrawerDao.insert(countEntity)
            Result.success(countEntity.copy(id = id))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
