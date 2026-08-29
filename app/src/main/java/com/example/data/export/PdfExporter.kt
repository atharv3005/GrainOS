package com.example.data.export

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import com.example.data.model.FirmProfile
import com.example.data.model.ProcurementEntity
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfExporter {
    fun downloadProfessionalPdf(context: Context, procurement: ProcurementEntity, firmProfile: FirmProfile) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()

        // 1. Header (Firm Name)
        paint.color = Color.rgb(15, 23, 42) // Dark blue/slate
        paint.textSize = 28f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(firmProfile.firmName, 50f, 60f, paint)

        // Firm Address / GST
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.rgb(100, 116, 139)
        canvas.drawText(firmProfile.location, 50f, 80f, paint)
        canvas.drawText("GSTIN: ${firmProfile.gstNumber}", 50f, 95f, paint)

        // 2. Receipt Title
        paint.color = Color.rgb(16, 185, 129) // Emerald green
        paint.textSize = 22f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("PROCUREMENT RECEIPT", 50f, 140f, paint)

        // 3. Divider
        paint.color = Color.rgb(203, 213, 225)
        paint.strokeWidth = 2f
        canvas.drawLine(50f, 155f, 545f, 155f, paint)

        // 4. Metadata (Token, Date, Status)
        paint.color = Color.BLACK
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        
        val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val dateString = dateFormat.format(Date(procurement.completedTimestamp.takeIf { it > 0 } ?: procurement.createdAt))

        canvas.drawText("Token No:", 50f, 180f, paint)
        canvas.drawText("Date:", 300f, 180f, paint)
        canvas.drawText("Payment Status:", 50f, 205f, paint)
        canvas.drawText("Vehicle No:", 300f, 205f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(procurement.tokenNo, 150f, 180f, paint)
        canvas.drawText(dateString, 340f, 180f, paint)
        
        // Status color
        if (procurement.paymentStatus == "PAID") paint.color = Color.rgb(16, 185, 129) else paint.color = Color.rgb(245, 158, 11)
        canvas.drawText(procurement.paymentStatus, 150f, 205f, paint)
        
        paint.color = Color.BLACK
        canvas.drawText(procurement.vehicleNumber, 375f, 205f, paint)

        // 5. Farmer Details Box
        paint.color = Color.rgb(241, 245, 249)
        canvas.drawRoundRect(50f, 230f, 545f, 290f, 8f, 8f, paint)
        
        paint.color = Color.BLACK
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Farmer Details", 60f, 250f, paint)
        
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Name: ${procurement.farmerName}", 60f, 275f, paint)
        canvas.drawText("Village: ${procurement.village}", 300f, 275f, paint)

        // 6. Procurement Table
        var y = 330f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Item / Crop", 50f, y, paint)
        canvas.drawText("Weight (Kg)", 250f, y, paint)
        canvas.drawText("Rate (/Qtl)", 370f, y, paint)
        canvas.drawText("Amount", 470f, y, paint)
        
        y += 10f
        paint.color = Color.rgb(203, 213, 225)
        canvas.drawLine(50f, y, 545f, y, paint)
        
        y += 25f
        paint.color = Color.BLACK
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        
        val inrFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
        
        canvas.drawText("${procurement.cropType} (${procurement.qualityGrade})", 50f, y, paint)
        canvas.drawText("Net: ${procurement.netWeightKg}", 250f, y, paint)
        canvas.drawText(inrFormat.format(procurement.ratePerQuintal), 370f, y, paint)
        canvas.drawText(inrFormat.format(procurement.grossBillAmount), 470f, y, paint)

        y += 20f
        paint.color = Color.rgb(100, 116, 139)
        paint.textSize = 10f
        canvas.drawText("Gross: ${procurement.grossWeightKg} | Tare: ${procurement.tareWeightKg} | Moisture: ${procurement.moisturePercentage}%", 50f, y, paint)

        // Deductions
        y += 40f
        paint.color = Color.BLACK
        paint.textSize = 12f
        if (procurement.totalMandiCess > 0) {
            canvas.drawText("Mandi Cess / Tax Deducted", 50f, y, paint)
            canvas.drawText("- ${inrFormat.format(procurement.totalMandiCess)}", 470f, y, paint)
            y += 20f
        }
        if (procurement.tdsDeductedAmount > 0) {
            canvas.drawText("TDS (194Q) Deducted", 50f, y, paint)
            canvas.drawText("- ${inrFormat.format(procurement.tdsDeductedAmount)}", 470f, y, paint)
            y += 20f
        }

        // Total
        y += 10f
        paint.color = Color.rgb(203, 213, 225)
        canvas.drawLine(50f, y, 545f, y, paint)
        
        y += 25f
        paint.color = Color.rgb(16, 185, 129)
        paint.textSize = 16f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("TOTAL PAYOUT", 50f, y, paint)
        canvas.drawText(inrFormat.format(procurement.totalAmount), 440f, y, paint)

        // Digital Seal
        y += 80f
        paint.color = Color.rgb(100, 116, 139)
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("This is a computer-generated receipt. No signature is required.", 50f, y, paint)
        
        pdfDocument.finishPage(page)

        // Save to Downloads
        try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }
            val file = File(downloadsDir, "Receipt_${procurement.tokenNo}_${System.currentTimeMillis()}.pdf")
            FileOutputStream(file).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()
            Toast.makeText(context, "PDF saved to Downloads", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to save PDF", Toast.LENGTH_SHORT).show()
        }
    }
}
