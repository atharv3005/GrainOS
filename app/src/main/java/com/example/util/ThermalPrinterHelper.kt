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

object ThermalPrinterHelper {

    fun printReceipt(context: Context, item: ProcurementEntity, firmName: String) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
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
                    // 80mm Thermal Printer Width ~ 3 inches ~ 216 pts (72 pts per inch)
                    // Let's use 250x500 for thermal slip
                    val pageWidth = 280
                    val pageHeight = 500
                    
                    val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
                    val page = doc.startPage(pageInfo)
                    
                    val canvas: Canvas = page.canvas
                    val paint = Paint()
                    paint.color = Color.BLACK
                    
                    // Header
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    paint.textSize = 18f
                    paint.textAlign = Paint.Align.CENTER
                    
                    var yOffset = 40f
                    canvas.drawText(firmName, pageWidth / 2f, yOffset, paint)
                    
                    yOffset += 25f
                    paint.textSize = 14f
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    canvas.drawText("Gate Pass / Farmer Receipt", pageWidth / 2f, yOffset, paint)
                    
                    yOffset += 30f
                    
                    // Divider
                    canvas.drawLine(20f, yOffset, pageWidth - 20f, yOffset, paint)
                    yOffset += 20f
                    
                    // Content
                    paint.textAlign = Paint.Align.LEFT
                    paint.textSize = 12f
                    
                    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH)
                    val dateStr = dateFormat.format(item.completedTimestamp)
                    val inrFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
                    val netAmt = inrFormat.format(item.totalAmount)
                    
                    val leftX = 20f
                    val lineHeight = 20f
                    
                    canvas.drawText("Token No: ${item.tokenNo}", leftX, yOffset, paint)
                    yOffset += lineHeight
                    canvas.drawText("Date: $dateStr", leftX, yOffset, paint)
                    yOffset += lineHeight
                    canvas.drawText("Farmer: ${item.farmerName}", leftX, yOffset, paint)
                    yOffset += lineHeight
                    canvas.drawText("Vehicle: ${item.vehicleNumber}", leftX, yOffset, paint)
                    yOffset += lineHeight
                    
                    yOffset += 10f
                    canvas.drawLine(20f, yOffset, pageWidth - 20f, yOffset, paint)
                    yOffset += 20f
                    
                    canvas.drawText("Gross Wt: ${item.grossWeightKg} kg", leftX, yOffset, paint)
                    yOffset += lineHeight
                    canvas.drawText("Tare Wt: ${item.tareWeightKg} kg", leftX, yOffset, paint)
                    yOffset += lineHeight
                    canvas.drawText("Net Wt: ${item.netWeightKg} kg", leftX, yOffset, paint)
                    yOffset += lineHeight
                    canvas.drawText("Moisture: ${item.moisturePercentage}%", leftX, yOffset, paint)
                    yOffset += lineHeight
                    canvas.drawText("Deduction: ${(item.grossWeightKg - item.tareWeightKg - item.netWeightKg)} kg", leftX, yOffset, paint)
                    yOffset += lineHeight
                    canvas.drawText("Rate: ₹${item.ratePerQuintal} / qtl", leftX, yOffset, paint)
                    
                    yOffset += 10f
                    canvas.drawLine(20f, yOffset, pageWidth - 20f, yOffset, paint)
                    yOffset += 25f
                    
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    paint.textSize = 14f
                    canvas.drawText("Net Pay: $netAmt", leftX, yOffset, paint)
                    
                    yOffset += 10f
                    paint.textSize = 12f
                    canvas.drawText("Status: ${item.paymentStatus}", leftX, yOffset, paint)
                    
                    yOffset += 40f
                    paint.textAlign = Paint.Align.CENTER
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                    paint.textSize = 10f
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
