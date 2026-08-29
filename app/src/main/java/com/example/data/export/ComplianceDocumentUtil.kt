package com.example.data.export

import android.content.Context
import android.content.Intent
import com.example.data.model.ExpenseEntryEntity
import com.example.data.model.FirmProfile
import com.example.data.model.InventoryReconciliationEntity
import com.example.data.model.OutboundDispatchEntity
import com.example.data.model.ProcurementEntity
import com.example.data.model.TradeBookingEntity
import com.example.data.model.TruckRejectionEntity
import com.example.data.model.VendorLedgerEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ComplianceDocumentUtil {
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ENGLISH)
    private val dateOnlyFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)

    /**
     * Generates a formal, statutory Maharashtra APMC Sauda Patti (खरेदी पावती)
     * compliant with Maharashtra Agricultural Produce Marketing (Development and Regulation) Act.
     */
    fun generateSaudaPattiText(
        procurement: ProcurementEntity,
        firm: FirmProfile
    ): String {
        val qtl = procurement.netWeightKg / 100.0
        val grossAmt = procurement.grossBillAmount.ifZero(qtl * procurement.ratePerQuintal)
        val cessMarketFee = procurement.mandiMarketFee.ifZero(if (procurement.applyMandiCess) grossAmt * 0.01 else 0.0)
        val cessSupervisory = procurement.mandiSupervisoryCharge.ifZero(if (procurement.applyMandiCess) grossAmt * 0.005 else 0.0)
        val totalCess = procurement.totalMandiCess.ifZero(cessMarketFee + cessSupervisory)
        val tdsAmt = procurement.tdsDeductedAmount
        val netPayable = procurement.totalAmount.ifZero(grossAmt - totalCess - tdsAmt)

        return """
============================================================
              MAHARASHTRA APMC SAUDA PATTI
                   (कृषी उत्पन्न खरेदी पावती)
============================================================
FIRM: ${firm.firmName}
APMC License No: ${firm.registrationNumber} | GSTIN: ${firm.gstNumber}
Yard / Location: ${firm.location}
Contact: ${firm.contactNumber}

------------------------------------------------------------
PURCHASE MEMO DETAILS (पावती तपशील)
------------------------------------------------------------
Sauda Patti No   : ${procurement.tokenNo}
Date & Time      : ${dateFormat.format(Date(if (procurement.completedTimestamp > 0) procurement.completedTimestamp else procurement.createdAt))}
Farmer Name      : ${procurement.farmerName}
Village / Taluka : ${procurement.village}
Mobile No        : ${procurement.mobileNumber}
PAN No           : ${if (procurement.panNumber.isNotBlank()) procurement.panNumber else "NOT PROVIDED"} (${if (procurement.isPanVerified) "Verified" else "Unverified"})
Vehicle No       : ${procurement.vehicleNumber}
Crop Commodity   : ${procurement.cropType} (${procurement.qualityGrade})
Godown / Bay     : ${procurement.godownAssigned}

------------------------------------------------------------
WEIGHBRIDGE AUDIT (वजन मापाचा तपशील)
------------------------------------------------------------
Gross Weight     : ${String.format(Locale.ENGLISH, "%,.2f", procurement.grossWeightKg)} kg (${procurement.grossWeightMethod})
Tare Weight      : ${String.format(Locale.ENGLISH, "%,.2f", procurement.tareWeightKg)} kg (${procurement.tareWeightMethod})
Net Weight       : ${String.format(Locale.ENGLISH, "%,.2f", procurement.netWeightKg)} kg (${String.format(Locale.ENGLISH, "%.2f", qtl)} Quintals)
Bag Breakdown    : ${procurement.bagCount} Bags (~${procurement.bagWeightKg.toInt()} kg standard packing)
Moisture Level   : ${procurement.moisturePercentage}% (Safe standard <14%)

------------------------------------------------------------
FINANCIAL SETTLEMENT (हिशोब व कर आकारणी)
------------------------------------------------------------
Agreed Rate / Qtl: ₹${String.format(Locale.ENGLISH, "%,.2f", procurement.ratePerQuintal)}
Gross Purchase Val: ₹${String.format(Locale.ENGLISH, "%,.2f", grossAmt)}

[MAHARASHTRA APMC STATUTORY DEDUCTIONS]
• Mandi Cess Applied  : ${if (procurement.applyMandiCess) "YES (Statutory APMC)" else "NO (Exempt/Outside)"}
• 1.0% Market Fee     : ₹${String.format(Locale.ENGLISH, "%,.2f", cessMarketFee)}
• 0.5% Supervisory Chg: ₹${String.format(Locale.ENGLISH, "%,.2f", cessSupervisory)}
• Total APMC Cess     : ₹${String.format(Locale.ENGLISH, "%,.2f", totalCess)}

[INCOME TAX COMPLIANCE]
• TDS u/s 194Q Tracking: ${if (procurement.enableTds194q) "ENABLED" else "DISABLED"}
• TDS Deduction (${if (procurement.tdsRate > 0) "${procurement.tdsRate * 100}%" else "0%"}) : ₹${String.format(Locale.ENGLISH, "%,.2f", tdsAmt)}
• TCS 206C(1H) Note   : ${if (procurement.isTcsExempt) "EXEMPT (TDS 194Q deducted; no TCS applicable)" else "N/A"}

------------------------------------------------------------
NET PAYABLE TO FARMER : ₹${String.format(Locale.ENGLISH, "%,.2f", netPayable)}
Payment Mode          : ${procurement.paymentMode} ${if (procurement.utrOrChequeNo.isNotBlank()) "(Ref: ${procurement.utrOrChequeNo})" else ""}
Payment Status        : ${procurement.paymentStatus} ${if (procurement.isPdc) "(PDC Due: ${dateOnlyFormat.format(Date(procurement.chequeDate))})" else ""}
------------------------------------------------------------
Certified that the produce was weighed on computerized electronic
weighbridge and statutory APMC market fee accounted for.

Farmer Signature / अंगठा               Authorized Signatory (${firm.firmName})
============================================================
        """.trimIndent()
    }

    /**
     * Generates statutory APMC Mandi Arrival & Cess Register for local market committee audits.
     */
    fun generateMandiRegisterText(
        procurements: List<ProcurementEntity>,
        firm: FirmProfile
    ): String {
        val sb = StringBuilder()
        sb.append("========================================================================================\n")
        sb.append("         MAHARASHTRA APMC STATUTORY ARRIVAL & MARKET CESS REGISTER\n")
        sb.append("                 (कृषी उत्पन्न बाजार समिती आवक व शुल्क नोंदवही)\n")
        sb.append("========================================================================================\n")
        sb.append("Market Committee: APMC Dhule / Maharashtra | Licensee: ${firm.firmName} (${firm.registrationNumber})\n")
        sb.append("Generated On: ${dateFormat.format(Date())} | Report Period: FY 2026-27\n")
        sb.append("----------------------------------------------------------------------------------------\n")
        sb.append(String.format(Locale.ENGLISH, "%-8s %-16s %-10s %-12s %-8s %-10s %-12s %-10s\n",
            "Token", "Farmer", "Vehicle", "Crop", "Net Qtl", "Rate/Qtl", "Gross ₹", "Cess 1.5% ₹"))
        sb.append("----------------------------------------------------------------------------------------\n")

        var totalQtl = 0.0
        var totalGross = 0.0
        var totalCess = 0.0

        for (p in procurements) {
            val qtl = p.netWeightKg / 100.0
            val gross = if (p.grossBillAmount > 0) p.grossBillAmount else (qtl * p.ratePerQuintal)
            val cess = if (p.totalMandiCess > 0) p.totalMandiCess else (if (p.applyMandiCess) gross * 0.015 else 0.0)
            totalQtl += qtl
            totalGross += gross
            totalCess += cess

            sb.append(String.format(Locale.ENGLISH, "%-8s %-16s %-10s %-12s %-8.2f %-10.2f %-12.2f %-10.2f\n",
                p.tokenNo,
                p.farmerName.take(15),
                p.vehicleNumber.take(10),
                p.cropType,
                qtl,
                p.ratePerQuintal,
                gross,
                cess
            ))
        }

        sb.append("----------------------------------------------------------------------------------------\n")
        sb.append(String.format(Locale.ENGLISH, "%-48s %-8.2f %-10s %-12.2f %-10.2f\n",
            "TOTAL STATUTORY APMC SUMMARY:", totalQtl, "", totalGross, totalCess))
        sb.append("========================================================================================\n")
        return sb.toString()
    }

    /**
     * Generates TDS Section 194Q Quarterly Compliance Sheet for TRACES 26Q return filing.
     */
    fun generateTds194qReportText(
        procurements: List<ProcurementEntity>,
        firm: FirmProfile
    ): String {
        val sb = StringBuilder()
        sb.append("========================================================================================\n")
        sb.append("           INCOME TAX SECTION 194Q COMPLIANCE & VENDOR AUDIT REPORT\n")
        sb.append("      (TDS on Purchase of Goods exceeding ₹50 Lakhs • FY 2026-27 / AY 2027-28)\n")
        sb.append("========================================================================================\n")
        sb.append("Deductor: ${firm.firmName} | TAN/PAN: ${firm.gstNumber.take(10)} | GSTIN: ${firm.gstNumber}\n")
        sb.append("Generated On: ${dateFormat.format(Date())}\n")
        sb.append("Statutory Rule: 0.1% TDS on aggregate purchase exceeding ₹50L (5.0% if PAN unverified)\n")
        sb.append("----------------------------------------------------------------------------------------\n")
        sb.append(String.format(Locale.ENGLISH, "%-16s %-12s %-10s %-14s %-10s %-10s %-12s\n",
            "Vendor/Farmer", "PAN No", "PAN Status", "Cumul. Gross ₹", "Threshold", "TDS Rate", "TDS Deducted ₹"))
        sb.append("----------------------------------------------------------------------------------------\n")

        val grouped = procurements.groupBy { it.farmerName }
        var totalTdsDeducted = 0.0

        for ((farmer, list) in grouped) {
            val totalFarmerGross = list.sumOf { if (it.grossBillAmount > 0) it.grossBillAmount else (it.netWeightKg / 100.0 * it.ratePerQuintal) }
            val first = list.first()
            val hasPan = first.isPanVerified || (first.panNumber.length == 10)
            val thresholdCrossed = totalFarmerGross > 5000000.0
            val tdsDeducted = list.sumOf { it.tdsDeductedAmount }
            totalTdsDeducted += tdsDeducted

            sb.append(String.format(Locale.ENGLISH, "%-16s %-12s %-10s %-14.2f %-10s %-10s %-12.2f\n",
                farmer.take(15),
                if (first.panNumber.isNotBlank()) first.panNumber else "PAN_NOT_GIVEN",
                if (first.isPanVerified) "VERIFIED" else "UNVERIFIED",
                totalFarmerGross,
                if (thresholdCrossed) "> ₹50L" else "< ₹50L",
                if (thresholdCrossed) (if (hasPan) "0.1%" else "5.0%") else "N/A",
                tdsDeducted
            ))
        }

        sb.append("----------------------------------------------------------------------------------------\n")
        sb.append(String.format(Locale.ENGLISH, "%-70s %-12.2f\n",
            "TOTAL TDS u/s 194Q DEPOSIT LIABILITY (Form 26Q Challan 281):", totalTdsDeducted))
        sb.append("========================================================================================\n")
        return sb.toString()
    }

    /**
     * Generates a comprehensive, downloadable CA Tax Filing Pack (CSV / Excel format)
     * containing Inbound Procurements, Vendor Ledgers, Expenses, Dispatches, Rejections, and Moisture Shrinkage.
     */
    fun generateCaTaxPackCsv(
        firm: FirmProfile,
        procurements: List<ProcurementEntity>,
        dispatches: List<OutboundDispatchEntity>,
        expenses: List<ExpenseEntryEntity>,
        rejections: List<TruckRejectionEntity>,
        ledgers: List<VendorLedgerEntity>,
        reconciliations: List<InventoryReconciliationEntity>
    ): String {
        val sb = StringBuilder()
        sb.append("--- GRAINOS COMPREHENSIVE CA TAX & FINANCIAL AUDIT PACK ---\n")
        sb.append("Firm Name,${firm.firmName}\n")
        sb.append("GSTIN,${firm.gstNumber}\n")
        sb.append("APMC License,${firm.registrationNumber}\n")
        sb.append("Generated At,${dateFormat.format(Date())}\n\n")

        // 1. Inbound Procurements (Sauda Patti Ledger)
        sb.append("=== INBOUND PROCUREMENT & SAUDA PATTI REGISTER ===\n")
        sb.append("Token,Date,Farmer Name,Mobile,Village,Vehicle,PAN,Crop,Gross Wt (kg),Tare Wt (kg),Net Wt (kg),Net Qtl,Rate/Qtl,Gross Bill (₹),Apply Mandi Cess,1% Market Fee (₹),0.5% Supervisory (₹),Total Cess (₹),TDS 194Q (₹),Net Payable (₹),Payment Mode,UTR/Cheque,Status\n")
        for (p in procurements) {
            val qtl = p.netWeightKg / 100.0
            val gross = if (p.grossBillAmount > 0) p.grossBillAmount else (qtl * p.ratePerQuintal)
            sb.append("${p.tokenNo},${dateOnlyFormat.format(Date(if (p.completedTimestamp > 0) p.completedTimestamp else p.createdAt))},\"${p.farmerName}\",${p.mobileNumber},\"${p.village}\",${p.vehicleNumber},${p.panNumber},${p.cropType},${p.grossWeightKg},${p.tareWeightKg},${p.netWeightKg},$qtl,${p.ratePerQuintal},$gross,${p.applyMandiCess},${p.mandiMarketFee},${p.mandiSupervisoryCharge},${p.totalMandiCess},${p.tdsDeductedAmount},${p.totalAmount},${p.paymentMode},${p.utrOrChequeNo},${p.status}\n")
        }
        sb.append("\n")

        // 2. Outbound Dispatches & Actual P&L
        sb.append("=== OUTBOUND SALES & ACTUAL P&L DISPATCH REGISTER ===\n")
        sb.append("Dispatch No,Date,Buyer Name,Destination,Truck No,Crop,Godown,Gate Net (kg),Booked Rate/Qtl,Company Unloaded Wt (kg),Weight Shortage (kg),Company Penalty (₹),FIFO Procurement Cost (₹),Freight (₹),Labor (₹),Bags (₹),Brokerage (₹),Net Revenue (₹),Actual Net Profit (₹),Status\n")
        for (d in dispatches) {
            sb.append("${d.dispatchNo},${dateOnlyFormat.format(Date(d.timestamp))},\"${d.buyerName}\",\"${d.destination}\",${d.vehicleNumber},${d.cropType},${d.godownSource},${d.netLoadedWeightKg},${d.ratePerQuintal},${d.companyUnloadedWeightKg},${d.weightShortageKg},${d.companyRateDeductionPenalty},${d.fifoProcurementCost},${d.freightCost},${d.loadingLaborCost},${d.bagCost},${d.finalBrokerageFee},${d.actualNetRevenue},${d.actualNetProfit},${d.status}\n")
        }
        sb.append("\n")

        // 3. Operational Expenses
        sb.append("=== TRANSACTION EXPENSES (HAMALI, FREIGHT, BAGS, MISC) ===\n")
        sb.append("Expense No,Date,Truck/Batch Ref,Crop,Labor (₹),Bags (₹),Freight (₹),Misc (₹),Misc Description,Total (₹),Paid To,Payment Mode,UTR/Cheque\n")
        for (e in expenses) {
            sb.append("${e.expenseNo},${dateOnlyFormat.format(Date(e.timestamp))},\"${e.truckOrBatchRef}\",${e.cropType},${e.laborCost},${e.bagsCost},${e.transportCost},${e.miscCost},\"${e.miscDescription}\",${e.totalExpense},\"${e.paidToOrParty}\",${e.paymentMode},${e.utrOrChequeNo}\n")
        }
        sb.append("\n")

        // 4. Rejection Claims & 50% Shifting Labor
        sb.append("=== TRUCK REJECTIONS & DISPUTE RESOLUTION ===\n")
        sb.append("Rejection No,Date,Truck No,Buyer/Company,Crop,Dispatched Wt (kg),Reason,Transport Loss (₹),Demurrage/Penalties (₹),Original Labor (₹),50% Return Labor (₹),Quality Deduction (₹),Total Loss (₹),Salvage Action\n")
        for (r in rejections) {
            sb.append("${r.rejectionNo},${dateOnlyFormat.format(Date(r.timestamp))},${r.truckNumber},\"${r.buyerOrCompany}\",${r.cropType},${r.dispatchedWeightKg},\"${r.rejectionReason}\",${r.transportLoss},${r.penaltiesDemurrage},${r.originalLoadingLaborCost},${r.returnBagShiftingLaborCost},${r.qualitySalvageDeduction},${r.totalRejectionLoss},\"${r.salvageAction}\"\n")
        }
        sb.append("\n")

        // 5. Vendor Ledgers & PDCs
        sb.append("=== VENDOR LEDGERS (FARMER, TRANSPORTER, HAMALI, BROKER) ===\n")
        sb.append("ID,Date,Vendor Type,Vendor Name,Transaction Type,Amount (₹),Payment Mode,UTR/Cheque,PDC Maturity Date,PDC Status,Ref Doc No,Running Balance\n")
        for (l in ledgers) {
            sb.append("${l.id},${dateOnlyFormat.format(Date(l.timestamp))},${l.vendorType},\"${l.vendorName}\",${l.transactionType},${l.amount},${l.paymentMode},${l.utrOrChequeNo},${if (l.chequeMaturityDate > 0) dateOnlyFormat.format(Date(l.chequeMaturityDate)) else "N/A"},${l.pdcStatus},${l.referenceDocNo},${l.runningBalance}\n")
        }
        sb.append("\n")

        // 6. Moisture Shrinkage Capitalization Logs
        sb.append("=== INVENTORY MOISTURE SHRINKAGE RECONCILIATION ===\n")
        sb.append("Reconciliation No,Date,Godown,Crop,Initial Wt (kg),Audited Wt (kg),Shrinkage Loss (kg),Shrinkage %,Initial Moisture %,Current Moisture %,Base Cost/Qtl (₹),Adjusted Capitalized Cost/Qtl (₹),Total Capitalized Loss (₹)\n")
        for (rc in reconciliations) {
            sb.append("${rc.reconciliationNo},${dateOnlyFormat.format(Date(rc.timestamp))},${rc.godownId},${rc.cropType},${rc.initialStockKg},${rc.auditedStockKg},${rc.lostWeightKg},${rc.shrinkagePercentage},${rc.initialMoisturePct},${rc.currentMoisturePct},${rc.originalCostPerQuintal},${rc.adjustedCostPerQuintal},${rc.totalLossAmountCapitalized}\n")
        }

        return sb.toString()
    }

    /**
     * Triggers standard Android share intent for WhatsApp, Gmail, or Saving to device.
     */
    fun shareDocument(context: Context, title: String, content: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TITLE, title)
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, content)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share $title via")
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }

    private fun Double.ifZero(fallback: Double): Double = if (this == 0.0) fallback else this
}
