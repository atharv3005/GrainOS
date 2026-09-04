package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.domain.managers.OrganizationContext
import java.util.UUID

enum class AccountType {
    ASSET,
    LIABILITY,
    EQUITY,
    REVENUE,
    EXPENSE
}

/**
 * Standard Chart of Accounts for Grain Trading Operations.
 */
object ChartOfAccounts {
    // Assets (1000s)
    const val ACC_CASH_ON_HAND = "1010_CASH_ON_HAND"
    const val ACC_BANK_CURRENT_ACCOUNT = "1020_BANK_CURRENT_ACC"
    const val ACC_GRAIN_INVENTORY = "1030_GRAIN_INVENTORY"
    const val ACC_ACCOUNTS_RECEIVABLE = "1040_ACCOUNTS_RECEIVABLE" // Corporate Buyers
    const val ACC_CHEQUES_IN_HAND = "1050_CHEQUES_IN_HAND" // Inward PDCs

    // Liabilities (2000s)
    const val ACC_ACCOUNTS_PAYABLE = "2010_ACCOUNTS_PAYABLE" // Farmers / Vendors
    const val ACC_PDC_PAYABLE = "2020_PDC_PAYABLE" // Issued PDCs
    const val ACC_MANDI_CESS_PAYABLE = "2030_MANDI_CESS_PAYABLE"
    const val ACC_APMC_CESS_PAYABLE = "2030_MANDI_CESS_PAYABLE" // Alias for APMC Cess
    const val ACC_TDS_194Q_PAYABLE = "2040_TDS_194Q_PAYABLE"
    const val ACC_BANK_OD_CC_LIMIT = "2050_BANK_OD_CC_LIMIT"

    // Equity (3000s)
    const val ACC_PROPRIETOR_CAPITAL = "3010_PROPRIETOR_CAPITAL"
    const val ACC_RETAINED_EARNINGS = "3020_RETAINED_EARNINGS"

    // Revenue (4000s)
    const val ACC_GRAIN_SALES_REVENUE = "4010_GRAIN_SALES_REVENUE"
    const val ACC_MISC_TRADING_INCOME = "4020_MISC_INCOME"

    // Expenses (5000s)
    const val ACC_COST_OF_GOODS_SOLD = "5010_COGS_PURCHASES"
    const val ACC_HAMALI_LABOR_EXPENSE = "5020_HAMALI_LABOR"
    const val ACC_GUNNY_BAGS_EXPENSE = "5030_GUNNY_BAGS"
    const val ACC_FREIGHT_TRANSPORT_EXPENSE = "5040_FREIGHT_TRANSPORT"
    const val ACC_MANDI_FEES_EXPENSE = "5050_MANDI_FEES"
    const val ACC_BANK_INTEREST_EXPENSE = "5060_BANK_INTEREST"
    const val ACC_SHRINKAGE_STORAGE_LOSS = "5070_SHRINKAGE_LOSS"
}

@Entity(
    tableName = "general_ledger",
    indices = [
        Index(value = ["uuid"], unique = true),
        Index(value = ["voucher_no"]),
        Index(value = ["account_code"]),
        Index(value = ["party_id"]),
        Index(value = ["timestamp"]),
        Index(value = ["org_code", "timestamp"])
    ]
)
data class GeneralLedgerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "uuid")
    val uuid: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "org_code")
    val orgCode: String = OrganizationContext.getCurrentOrgCode(),

    @ColumnInfo(name = "voucher_no")
    val voucherNo: String, // e.g. JV/26-27/00128, GIN/26-27/00142

    @ColumnInfo(name = "account_code")
    val accountCode: String,

    @ColumnInfo(name = "account_name")
    val accountName: String,

    @ColumnInfo(name = "account_type")
    val accountType: String,

    @ColumnInfo(name = "debit_amount")
    val debitAmount: Double = 0.0,

    @ColumnInfo(name = "credit_amount")
    val creditAmount: Double = 0.0,

    @ColumnInfo(name = "party_id")
    val partyId: Long? = null,

    @ColumnInfo(name = "narration")
    val narration: String,

    @ColumnInfo(name = "reference_doc_no")
    val referenceDocNo: String = "",

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis()
)
