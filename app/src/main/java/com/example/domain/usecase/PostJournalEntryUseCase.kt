package com.example.domain.usecase

import com.example.data.local.GeneralLedgerDao
import com.example.data.model.AccountType
import com.example.data.model.ChartOfAccounts
import com.example.data.model.GeneralLedgerEntity
import com.example.data.model.OutboundDispatchEntity
import com.example.data.model.ProcurementEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs

/**
 * Enterprise Double-Entry Journal Posting Use Case.
 * Enforces fundamental accounting equation: Total Debits == Total Credits.
 */
class PostJournalEntryUseCase(private val generalLedgerDao: GeneralLedgerDao) {

    suspend fun postJournalVoucher(
        voucherNo: String,
        narration: String,
        referenceDocNo: String,
        entries: List<GeneralLedgerEntity>
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val totalDebits = entries.sumOf { it.debitAmount }
            val totalCredits = entries.sumOf { it.creditAmount }

            if (abs(totalDebits - totalCredits) > 0.01) {
                return@withContext Result.failure(
                    IllegalStateException("Double-entry imbalance! Total Debits (₹$totalDebits) != Total Credits (₹$totalCredits) for voucher $voucherNo.")
                )
            }

            generalLedgerDao.insertAll(entries)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun postProcurementJournal(procurement: ProcurementEntity): Result<Unit> {
        val voucherNo = "JV-${procurement.tokenNo}"
        val entries = mutableListOf<GeneralLedgerEntity>()

        // 1. Dr. Grain Inventory (Gross Value)
        entries.add(
            GeneralLedgerEntity(
                voucherNo = voucherNo,
                accountCode = ChartOfAccounts.ACC_GRAIN_INVENTORY,
                accountName = "Grain Stock Inventory",
                accountType = AccountType.ASSET.name,
                debitAmount = procurement.grossBillAmount,
                creditAmount = 0.0,
                partyId = procurement.partyId,
                narration = "Inward Grain Intake [${procurement.cropType} ${procurement.netWeightKg / 100.0} Qtl]",
                referenceDocNo = procurement.tokenNo,
                orgCode = procurement.orgCode
            )
        )

        // 2. Cr. APMC Mandi Cess Payable (if applied) - BUG-001 Fix
        if (procurement.totalMandiCess > 0) {
            entries.add(
                GeneralLedgerEntity(
                    voucherNo = voucherNo,
                    accountCode = ChartOfAccounts.ACC_APMC_CESS_PAYABLE,
                    accountName = "APMC Mandi Cess Statutory Payable",
                    accountType = AccountType.LIABILITY.name,
                    debitAmount = 0.0,
                    creditAmount = procurement.totalMandiCess,
                    partyId = procurement.partyId,
                    narration = "APMC Mandi Cess on Procurement ${procurement.tokenNo}",
                    referenceDocNo = procurement.tokenNo,
                    orgCode = procurement.orgCode
                )
            )
        }

        // 3. Cr. TDS 194Q Payable (if any)
        if (procurement.tdsDeductedAmount > 0) {
            entries.add(
                GeneralLedgerEntity(
                    voucherNo = voucherNo,
                    accountCode = ChartOfAccounts.ACC_TDS_194Q_PAYABLE,
                    accountName = "TDS 194Q Statutory Payable",
                    accountType = AccountType.LIABILITY.name,
                    debitAmount = 0.0,
                    creditAmount = procurement.tdsDeductedAmount,
                    partyId = procurement.partyId,
                    narration = "TDS 194Q Deducted",
                    referenceDocNo = procurement.tokenNo,
                    orgCode = procurement.orgCode
                )
            )
        }

        // 4. Cr. Farmer Accounts Payable (Net Balance)
        val remainingPayable = procurement.totalAmount
        entries.add(
            GeneralLedgerEntity(
                voucherNo = voucherNo,
                accountCode = ChartOfAccounts.ACC_ACCOUNTS_PAYABLE,
                accountName = "Farmer Payable Ledger",
                accountType = AccountType.LIABILITY.name,
                debitAmount = 0.0,
                creditAmount = remainingPayable,
                partyId = procurement.partyId,
                narration = "Net Payable to ${procurement.farmerName}",
                referenceDocNo = procurement.tokenNo,
                orgCode = procurement.orgCode
            )
        )

        return postJournalVoucher(voucherNo, "Procurement Intake JV", procurement.tokenNo, entries)
    }

    suspend fun postDispatchJournal(dispatch: OutboundDispatchEntity): Result<Unit> {
        val voucherNo = "JV-${dispatch.dispatchNo}"
        val entries = mutableListOf<GeneralLedgerEntity>()

        // 1. Dr. Accounts Receivable (Corporate Buyer)
        entries.add(
            GeneralLedgerEntity(
                voucherNo = voucherNo,
                accountCode = ChartOfAccounts.ACC_ACCOUNTS_RECEIVABLE,
                accountName = "Corporate Buyer Receivable",
                accountType = AccountType.ASSET.name,
                debitAmount = dispatch.totalInvoiceAmount,
                creditAmount = 0.0,
                partyId = dispatch.buyerPartyId,
                narration = "Sales Delivery to ${dispatch.buyerName} [${dispatch.netLoadedWeightKg / 1000.0} MT]",
                referenceDocNo = dispatch.dispatchNo,
                orgCode = dispatch.orgCode
            )
        )

        // 2. Cr. Grain Sales Revenue
        entries.add(
            GeneralLedgerEntity(
                voucherNo = voucherNo,
                accountCode = ChartOfAccounts.ACC_GRAIN_SALES_REVENUE,
                accountName = "Grain Sales Revenue",
                accountType = AccountType.REVENUE.name,
                debitAmount = 0.0,
                creditAmount = dispatch.totalInvoiceAmount,
                partyId = dispatch.buyerPartyId,
                narration = "Outward Dispatch Sales Invoice",
                referenceDocNo = dispatch.dispatchNo,
                orgCode = dispatch.orgCode
            )
        )

        return postJournalVoucher(voucherNo, "Sales Dispatch JV", dispatch.dispatchNo, entries)
    }
}
