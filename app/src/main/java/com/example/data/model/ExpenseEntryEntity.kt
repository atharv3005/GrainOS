package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Manual Expense Entry Entity.
 * Allows traders to manually enter exact fluctuating expenses at the time of transaction:
 * - Labor cost (₹) (Hamali)
 * - Bags cost (Calculated & entered PER TRUCK LOADING, e.g. ~60kg/50kg bags)
 * - Transport / Freight cost (₹)
 * - Miscellaneous / Weighment & Handling costs (₹) with custom description (e.g., Toll Taxes, Quality Penalty)
 */
@Entity(tableName = "manual_expenses")
data class ExpenseEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val expenseNo: String,
    val truckOrBatchRef: String,
    val cropType: String,
    val laborCost: Double,
    val bagsCost: Double, // Entered per truck loading
    val transportCost: Double,
    val miscCost: Double,
    val miscDescription: String = "", // e.g. "Toll Taxes", "Mandi Weighment Slip", "Quality Rate Cut"
    val totalExpense: Double = laborCost + bagsCost + transportCost + miscCost,
    val paidToOrParty: String,
    val paymentMode: String = "CASH", // CASH, RTGS, CHEQUE
    val utrOrChequeNo: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
)
