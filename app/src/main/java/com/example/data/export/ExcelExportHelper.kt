package com.example.data.export

import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import com.example.data.model.VendorLedgerEntity
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

object ExcelExportHelper {

    fun exportAnnualLedgerToExcel(context: Context, ledgers: List<VendorLedgerEntity>): Boolean {
        try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Annual Ledger")

            // Header row
            val headerRow = sheet.createRow(0)
            val headers = listOf("Date", "Entity/Party", "Transaction Type", "Amount (₹)", "Status", "Notes")
            headers.forEachIndexed { index, header ->
                val cell = headerRow.createCell(index)
                cell.setCellValue(header)
                // Add simple styling if needed
            }

            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

            // Data rows
            ledgers.forEachIndexed { index, ledger ->
                val row = sheet.createRow(index + 1)
                row.createCell(0).setCellValue(dateFormat.format(Date(ledger.timestamp)))
                row.createCell(1).setCellValue("${ledger.vendorName} (${ledger.vendorType})")
                row.createCell(2).setCellValue(ledger.transactionType)
                row.createCell(3).setCellValue(ledger.amount)
                row.createCell(4).setCellValue(ledger.pdcStatus)
                row.createCell(5).setCellValue(ledger.notes)
            }

            // Save to Downloads using MediaStore
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "GrainOS_CA_Report_${System.currentTimeMillis()}.xlsx")
                put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    workbook.write(outputStream)
                }
                workbook.close()
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return false
    }
}
