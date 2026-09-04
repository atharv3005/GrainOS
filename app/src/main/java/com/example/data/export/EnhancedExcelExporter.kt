package com.example.data.export

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.GodownEntity
import com.example.data.model.OutboundDispatchEntity
import com.example.data.model.PartyEntity
import com.example.data.model.ProcurementEntity
import com.example.data.model.VendorLedgerEntity
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Enterprise Multi-Sheet Apache POI Excel Exporter.
 * Generates an audit-ready CA & Bank submission workbook with 6 sheets:
 * 1. Executive Summary & P&L
 * 2. Inbound Gate Procurements
 * 3. Outward Dispatches & Deliveries
 * 4. Silo / Godown Stock Balances
 * 5. Party Directory & Balances
 * 6. Statutory Tax & TDS 194Q Schedule
 */
object EnhancedExcelExporter {

    fun generateAuditWorkbook(
        context: Context,
        procurements: List<ProcurementEntity>,
        dispatches: List<OutboundDispatchEntity>,
        godowns: List<GodownEntity>,
        parties: List<PartyEntity>,
        ledgers: List<VendorLedgerEntity>,
        firmName: String = "GrainOS Enterprise Hub"
    ): Uri {
        val workbook = XSSFWorkbook()
        val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.ENGLISH)

        // Styles
        val headerFont = workbook.createFont().apply {
            bold = true
            color = IndexedColors.WHITE.index
        }
        val headerStyle = workbook.createCellStyle().apply {
            setFont(headerFont)
            fillForegroundColor = IndexedColors.DARK_TEAL.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            alignment = HorizontalAlignment.CENTER
        }
        val currencyStyle = workbook.createCellStyle().apply {
            dataFormat = workbook.createDataFormat().getFormat("₹#,##0.00")
        }
        val numberStyle = workbook.createCellStyle().apply {
            dataFormat = workbook.createDataFormat().getFormat("#,##0.00")
        }

        // --- SHEET 1: Executive Summary & P&L ---
        val sheetSummary = workbook.createSheet("1. Summary & PnL")
        val sumHeaderRow = sheetSummary.createRow(0)
        sumHeaderRow.createCell(0).apply { setCellValue("Metric"); cellStyle = headerStyle }
        sumHeaderRow.createCell(1).apply { setCellValue("Value"); cellStyle = headerStyle }

        val totalProcuredKg = procurements.sumOf { it.netWeightKg }
        val totalProcuredCost = procurements.sumOf { it.grossBillAmount }
        val totalDispatchedKg = dispatches.sumOf { it.netLoadedWeightKg }
        val totalRevenue = dispatches.sumOf { it.totalInvoiceAmount }
        val totalApmcCess = procurements.sumOf { it.totalMandiCess }
        val totalTds = procurements.sumOf { it.tdsDeductedAmount }
        val totalStockMt = godowns.sumOf { it.currentStockMt }
        val estGrossProfit = totalRevenue - (totalProcuredCost * (if (totalProcuredKg > 0) (totalDispatchedKg / totalProcuredKg).coerceAtMost(1.0) else 0.0))

        val summaryMetrics = listOf(
            "Firm Name" to firmName,
            "Report Generation Date" to dateFormat.format(Date()),
            "Total Inward Volume (MT)" to "%.2f MT".format(totalProcuredKg / 1000.0),
            "Total Procurement Value (₹)" to "₹%,.2f".format(totalProcuredCost),
            "Total Outward Volume (MT)" to "%.2f MT".format(totalDispatchedKg / 1000.0),
            "Total Sales Revenue (₹)" to "₹%,.2f".format(totalRevenue),
            "Estimated Realized Gross Margin (₹)" to "₹%,.2f".format(estGrossProfit),
            "Current Godown Stock on Hand (MT)" to "%.2f MT".format(totalStockMt),
            "Total APMC Cess Incurred (₹)" to "₹%,.2f".format(totalApmcCess),
            "Total TDS 194Q Deducted (₹)" to "₹%,.2f".format(totalTds),
            "Active Registered Parties" to "${parties.size} entities"
        )
        summaryMetrics.forEachIndexed { idx, (k, v) ->
            val row = sheetSummary.createRow(idx + 1)
            row.createCell(0).setCellValue(k)
            row.createCell(1).setCellValue(v)
        }
        sheetSummary.autoSizeColumn(0)
        sheetSummary.autoSizeColumn(1)

        // --- SHEET 2: Inbound Gate Procurements ---
        val sheetProc = workbook.createSheet("2. Inbound Procurements")
        val procHeaders = listOf("Token No", "Date", "Farmer Name", "Mobile", "Crop", "Net Qtl", "Rate/Qtl", "Gross Bill (₹)", "APMC Cess (₹)", "TDS (₹)", "Net Payable (₹)", "Godown")
        val procHeadRow = sheetProc.createRow(0)
        procHeaders.forEachIndexed { i, h ->
            procHeadRow.createCell(i).apply { setCellValue(h); cellStyle = headerStyle }
        }
        procurements.forEachIndexed { i, p ->
            val row = sheetProc.createRow(i + 1)
            row.createCell(0).setCellValue(p.tokenNo)
            row.createCell(1).setCellValue(dateFormat.format(Date(p.createdAt)))
            row.createCell(2).setCellValue(p.farmerName)
            row.createCell(3).setCellValue(p.mobileNumber)
            row.createCell(4).setCellValue(p.cropType)
            row.createCell(5).apply { setCellValue(p.netWeightKg / 100.0); cellStyle = numberStyle }
            row.createCell(6).apply { setCellValue(p.ratePerQuintal); cellStyle = currencyStyle }
            row.createCell(7).apply { setCellValue(p.grossBillAmount); cellStyle = currencyStyle }
            row.createCell(8).apply { setCellValue(p.totalMandiCess); cellStyle = currencyStyle }
            row.createCell(9).apply { setCellValue(p.tdsDeductedAmount); cellStyle = currencyStyle }
            row.createCell(10).apply { setCellValue(p.totalAmount); cellStyle = currencyStyle }
            row.createCell(11).setCellValue(p.godownAssigned)
        }
        procHeaders.indices.forEach { sheetProc.autoSizeColumn(it) }

        // --- SHEET 3: Outward Dispatches ---
        val sheetDisp = workbook.createSheet("3. Outward Dispatches")
        val dispHeaders = listOf("Dispatch No", "Date", "Buyer Name", "Vehicle No", "Crop", "Loaded MT", "Rate/Qtl", "Total Bill (₹)", "Destination", "Status")
        val dispHeadRow = sheetDisp.createRow(0)
        dispHeaders.forEachIndexed { i, h ->
            dispHeadRow.createCell(i).apply { setCellValue(h); cellStyle = headerStyle }
        }
        dispatches.forEachIndexed { i, d ->
            val row = sheetDisp.createRow(i + 1)
            row.createCell(0).setCellValue(d.dispatchNo)
            row.createCell(1).setCellValue(dateFormat.format(Date(d.timestamp)))
            row.createCell(2).setCellValue(d.buyerName)
            row.createCell(3).setCellValue(d.vehicleNumber)
            row.createCell(4).setCellValue(d.cropType)
            row.createCell(5).apply { setCellValue(d.netLoadedWeightKg / 1000.0); cellStyle = numberStyle }
            row.createCell(6).apply { setCellValue(d.ratePerQuintal); cellStyle = currencyStyle }
            row.createCell(7).apply { setCellValue(d.totalInvoiceAmount); cellStyle = currencyStyle }
            row.createCell(8).setCellValue(d.destination)
            row.createCell(9).setCellValue(d.status)
        }
        dispHeaders.indices.forEach { sheetDisp.autoSizeColumn(it) }

        // --- SHEET 4: Godown Stock Balances ---
        val sheetGodown = workbook.createSheet("4. Godown Stock")
        val godownHeaders = listOf("Godown ID", "Display Name", "Active Crop", "Capacity (MT)", "Current Stock (MT)", "Avg Moisture (%)", "Occupancy (%)", "Avg Cost/Qtl (₹)")
        val godownHeadRow = sheetGodown.createRow(0)
        godownHeaders.forEachIndexed { i, h ->
            godownHeadRow.createCell(i).apply { setCellValue(h); cellStyle = headerStyle }
        }
        godowns.forEachIndexed { i, g ->
            val row = sheetGodown.createRow(i + 1)
            row.createCell(0).setCellValue(g.godownId)
            row.createCell(1).setCellValue(g.displayName)
            row.createCell(2).setCellValue(g.activeCrop)
            row.createCell(3).apply { setCellValue(g.capacityMt); cellStyle = numberStyle }
            row.createCell(4).apply { setCellValue(g.currentStockMt); cellStyle = numberStyle }
            row.createCell(5).apply { setCellValue(g.averageMoisture); cellStyle = numberStyle }
            row.createCell(6).apply {
                val occ = if (g.capacityMt > 0) (g.currentStockMt / g.capacityMt) * 100 else 0.0
                setCellValue(occ)
                cellStyle = numberStyle
            }
            row.createCell(7).apply { setCellValue(g.adjustedAvgCostPerQuintal); cellStyle = currencyStyle }
        }
        godownHeaders.indices.forEach { sheetGodown.autoSizeColumn(it) }

        // --- SHEET 5: Party Directory & Balances ---
        val sheetParty = workbook.createSheet("5. Party Directory")
        val partyHeaders = listOf("Party Type", "Legal Name", "Trade Name", "Mobile", "Village", "PAN", "GSTIN", "Cumulative FY Purchases (₹)", "Running Balance (₹)")
        val partyHeadRow = sheetParty.createRow(0)
        partyHeaders.forEachIndexed { i, h ->
            partyHeadRow.createCell(i).apply { setCellValue(h); cellStyle = headerStyle }
        }
        parties.forEachIndexed { i, p ->
            val row = sheetParty.createRow(i + 1)
            row.createCell(0).setCellValue(p.partyType)
            row.createCell(1).setCellValue(p.legalName)
            row.createCell(2).setCellValue(p.tradeName ?: "-")
            row.createCell(3).setCellValue(p.mobile)
            row.createCell(4).setCellValue(p.village)
            row.createCell(5).setCellValue(p.pan ?: "-")
            row.createCell(6).setCellValue(p.gstin ?: "-")
            row.createCell(7).apply { setCellValue(p.cumulativePurchasesInFy); cellStyle = currencyStyle }
            row.createCell(8).apply { setCellValue(p.runningBalance); cellStyle = currencyStyle }
        }
        partyHeaders.indices.forEach { sheetParty.autoSizeColumn(it) }

        // --- SHEET 6: Statutory Tax & TDS 194Q Schedule ---
        val sheetTax = workbook.createSheet("6. Tax & TDS 194Q Schedule")
        val taxHeaders = listOf("Party Name", "PAN", "Section", "Total FY Turnover (₹)", "Threshold (₹)", "TDS Rate (%)", "TDS Deducted (₹)", "APMC Cess Rate (%)", "APMC Cess Incurred (₹)")
        val taxHeadRow = sheetTax.createRow(0)
        taxHeaders.forEachIndexed { i, h ->
            taxHeadRow.createCell(i).apply { setCellValue(h); cellStyle = headerStyle }
        }
        parties.filter { it.cumulativePurchasesInFy > 0 }.forEachIndexed { i, p ->
            val row = sheetTax.createRow(i + 1)
            row.createCell(0).setCellValue(p.legalName)
            row.createCell(1).setCellValue(p.pan ?: "NOT_PROVIDED")
            row.createCell(2).setCellValue("194Q")
            row.createCell(3).apply { setCellValue(p.cumulativePurchasesInFy); cellStyle = currencyStyle }
            row.createCell(4).apply { setCellValue(5000000.0); cellStyle = currencyStyle }
            val tdsRate = if (p.pan.isNullOrBlank()) 5.0 else 0.1
            row.createCell(5).apply { setCellValue(tdsRate); cellStyle = numberStyle }
            val tdsAmt = if (p.cumulativePurchasesInFy > 5000000.0) ((p.cumulativePurchasesInFy - 5000000.0) * (tdsRate / 100.0)) else 0.0
            row.createCell(6).apply { setCellValue(tdsAmt); cellStyle = currencyStyle }
            row.createCell(7).apply { setCellValue(1.0); cellStyle = numberStyle } // 1% Mandi Cess
            row.createCell(8).apply { setCellValue(p.cumulativePurchasesInFy * 0.01); cellStyle = currencyStyle }
        }
        taxHeaders.indices.forEach { sheetTax.autoSizeColumn(it) }

        // Write to Cache / Storage Directory
        val reportsDir = File(context.cacheDir, "ca_reports").apply { if (!exists()) mkdirs() }
        val reportFile = File(reportsDir, "GrainOS_Audit_Report_${System.currentTimeMillis()}.xlsx")
        FileOutputStream(reportFile).use { fos ->
            workbook.write(fos)
        }
        workbook.close()

        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", reportFile)
    }
}
