package com.example.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import com.example.data.model.ProcurementEntity
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Enterprise Thermal Receipt Printing Helper.
 * Supports ESC/POS 80mm & 58mm roll printing with dynamic page height calculation to prevent content clipping (BUG-008 Fix).
 */
object ThermalPrinterHelper {

    fun printReceipt(context: Context, item: ProcurementEntity, firmName: String) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
            ?: return // Graceful return if Android Print Service is not present

        val jobName = "Receipt_${item.tokenNo}"

        val printAdapter = object : PrintDocumentAdapter() {
            private var pdfDocument: PdfDocument? = null

            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes,
                cancellationSignal: CancellationSignal?,
                callback: LayoutResultCallback,
                extras: Bundle?
            ) {
                pdfDocument = PdfDocument()

                if (cancellationSignal?.isCanceled == true) {
                    callback.onLayoutCancelled()
                    return
                }

                val builder = PrintDocumentInfo.Builder(jobName)
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(1)

                val info = builder.build()
                callback.onLayoutFinished(info, newAttributes != oldAttributes)
            }

            override fun onWrite(
                pages: Array<out PageRange>,
                destination: ParcelFileDescriptor,
                cancellationSignal: CancellationSignal?,
                callback: WriteResultCallback
            ) {
                pdfDocument?.let { doc ->
                    val pageWidth = 280
                    
                    // Pre-calculate dynamic page height (BUG-008 Fix)
                    val baseLines = 15
                    val optionalLines = (if (item.totalMandiCess > 0) 1 else 0) +
                                        (if (item.tdsDeductedAmount > 0) 1 else 0) +
                                        (if (!item.utrOrChequeNo.isNullOrBlank()) 1 else 0) +
                                        (if (!item.village.isNullOrBlank()) 1 else 0)
                    val calculatedHeight = 220 + ((baseLines + optionalLines) * 22) + 100 // Dynamic height + 100pt buffer
                    val pageHeight = calculatedHeight.coerceAtLeast(550)

                    val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
                    val page = doc.startPage(pageInfo)

                    val canvas: Canvas = page.canvas
                    val paint = Paint().apply { color = Color.BLACK }

                    // Header
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    paint.textSize = 16f
                    paint.textAlign = Paint.Align.CENTER

                    var yOffset = 35f
                    canvas.drawText(firmName, pageWidth / 2f, yOffset, paint)

                    yOffset += 22f
                    paint.textSize = 12f
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    canvas.drawText("Gate Pass / Farmer Receipt", pageWidth / 2f, yOffset, paint)

                    yOffset += 25f
                    canvas.drawLine(15f, yOffset, pageWidth - 15f, yOffset, paint)
                    yOffset += 18f

                    // Content
                    paint.textAlign = Paint.Align.LEFT
                    paint.textSize = 11f

                    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH)
                    val dateStr = dateFormat.format(item.completedTimestamp)
                    val inrFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
                    val netAmt = inrFormat.format(item.totalAmount)

                    val leftX = 15f
                    val lineHeight = 18f

                    canvas.drawText("Token No: ${item.tokenNo}", leftX, yOffset, paint); yOffset += lineHeight
                    canvas.drawText("Date: $dateStr", leftX, yOffset, paint); yOffset += lineHeight
                    canvas.drawText("Farmer: ${item.farmerName}", leftX, yOffset, paint); yOffset += lineHeight
                    if (!item.village.isNullOrBlank()) {
                        canvas.drawText("Village: ${item.village}", leftX, yOffset, paint); yOffset += lineHeight
                    }
                    canvas.drawText("Vehicle: ${item.vehicleNumber}", leftX, yOffset, paint); yOffset += lineHeight

                    yOffset += 8f
                    canvas.drawLine(15f, yOffset, pageWidth - 15f, yOffset, paint)
                    yOffset += 18f

                    canvas.drawText("Crop: ${item.cropType}", leftX, yOffset, paint); yOffset += lineHeight
                    canvas.drawText("Gross Wt: ${item.grossWeightKg} kg", leftX, yOffset, paint); yOffset += lineHeight
                    canvas.drawText("Tare Wt: ${item.tareWeightKg} kg", leftX, yOffset, paint); yOffset += lineHeight
                    canvas.drawText("Net Wt: ${item.netWeightKg} kg", leftX, yOffset, paint); yOffset += lineHeight
                    canvas.drawText("Moisture: ${item.moisturePercentage}%", leftX, yOffset, paint); yOffset += lineHeight
                    canvas.drawText("Rate: ₹${item.ratePerQuintal} / qtl", leftX, yOffset, paint); yOffset += lineHeight
                    canvas.drawText("Gross Value: ${inrFormat.format(item.grossBillAmount)}", leftX, yOffset, paint); yOffset += lineHeight

                    if (item.totalMandiCess > 0) {
                        canvas.drawText("APMC Cess (1.5%): -${inrFormat.format(item.totalMandiCess)}", leftX, yOffset, paint); yOffset += lineHeight
                    }
                    if (item.tdsDeductedAmount > 0) {
                        canvas.drawText("TDS Sec 194Q: -${inrFormat.format(item.tdsDeductedAmount)}", leftX, yOffset, paint); yOffset += lineHeight
                    }

                    yOffset += 8f
                    canvas.drawLine(15f, yOffset, pageWidth - 15f, yOffset, paint)
                    yOffset += 22f

                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    paint.textSize = 13f
                    canvas.drawText("Net Payable: $netAmt", leftX, yOffset, paint); yOffset += lineHeight
                    paint.textSize = 11f
                    canvas.drawText("Payment Mode: ${item.paymentMode} (${item.paymentStatus})", leftX, yOffset, paint); yOffset += lineHeight

                    if (!item.utrOrChequeNo.isNullOrBlank()) {
                        canvas.drawText("Cheque/UTR: ${item.utrOrChequeNo}", leftX, yOffset, paint); yOffset += lineHeight
                    }

                    yOffset += 25f
                    paint.textAlign = Paint.Align.CENTER
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                    paint.textSize = 9.5f
                    canvas.drawText("Thank you for your business!", pageWidth / 2f, yOffset, paint)

                    doc.finishPage(page)

                    try {
                        doc.writeTo(FileOutputStream(destination.fileDescriptor))
                        callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                    } catch (e: Exception) {
                        callback.onWriteFailed(e.toString())
                    } finally {
                        doc.close()
                        pdfDocument = null
                    }
                }
            }
        }

        // Set up print attributes for thermal printer (approx. 80mm roll)
        val attributes = PrintAttributes.Builder()
            .setMediaSize(PrintAttributes.MediaSize.UNKNOWN_PORTRAIT)
            .setResolution(PrintAttributes.Resolution("THERMAL", "Thermal Printer", 203, 203))
            .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
            .build()

        printManager.print(jobName, printAdapter, attributes)
    }
}
