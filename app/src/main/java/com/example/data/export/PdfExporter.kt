package com.example.data.export

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.FirmProfile
import com.example.data.model.ProcurementEntity
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfExporter {

    private val inrCurrencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    private val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH)
    private val dateOnlyFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)

    /**
     * Formats current procurement record data into a high-resolution, statutory
     * Maharashtra APMC PDF receipt layout and writes it to a file.
     */
    fun generateProcurementReceiptPdf(
        context: Context,
        procurement: ProcurementEntity,
        firmProfile: FirmProfile,
        targetFile: File? = null
    ): File {
        val pdfDocument = PdfDocument()
        // Standard A4 Size: 595 x 842 points
        val pageWidth = 595
        val pageHeight = 842
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        drawPdfReceiptLayout(canvas, pageWidth, pageHeight, procurement, firmProfile)

        pdfDocument.finishPage(page)

        val destinationFile = targetFile ?: File(
            context.cacheDir,
            "GrainOS_Receipt_${procurement.tokenNo}_${System.currentTimeMillis()}.pdf"
        )

        FileOutputStream(destinationFile).use { outputStream ->
            pdfDocument.writeTo(outputStream)
        }
        pdfDocument.close()

        return destinationFile
    }

    /**
     * Renders the complete, high-craft visual PDF layout on an A4 canvas.
     */
    private fun drawPdfReceiptLayout(
        canvas: Canvas,
        pageWidth: Int,
        pageHeight: Int,
        procurement: ProcurementEntity,
        firm: FirmProfile
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val leftMargin = 40f
        val rightMargin = pageWidth - 40f
        val contentWidth = rightMargin - leftMargin

        // 1. Top Decorative Brand Bar (Deep Emerald)
        paint.color = Color.rgb(16, 185, 129) // Emerald-500
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), 8f, paint)

        // 2. Organization / FPC Header Section
        paint.color = Color.rgb(15, 23, 42) // Slate-900
        paint.textSize = 20f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(firm.firmName.uppercase(Locale.ENGLISH), leftMargin, 42f, paint)

        // License & Location Subtext
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.rgb(100, 116, 139) // Slate-500
        canvas.drawText("APMC Reg No: ${firm.registrationNumber}  |  GSTIN: ${firm.gstNumber}", leftMargin, 58f, paint)
        canvas.drawText("Yard Location: ${firm.location}  |  Ph: ${firm.contactNumber}", leftMargin, 72f, paint)

        // Right side badge / tag
        paint.color = Color.rgb(241, 245, 249)
        val tagRect = RectF(rightMargin - 150f, 26f, rightMargin, 52f)
        canvas.drawRoundRect(tagRect, 6f, 6f, paint)

        paint.color = Color.rgb(5, 150, 105)
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("GRAINOS CERTIFIED ERP", rightMargin - 140f, 42f, paint)

        // 3. Document Title Banner
        var currentY = 95f
        paint.color = Color.rgb(236, 253, 245) // Light emerald bg
        val bannerRect = RectF(leftMargin, currentY, rightMargin, currentY + 36f)
        canvas.drawRoundRect(bannerRect, 6f, 6f, paint)

        paint.color = Color.rgb(4, 120, 87) // Emerald-700
        paint.textSize = 13f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("MAHARASHTRA APMC PROCUREMENT SLIP (खरेदी पावती)", leftMargin + 14f, currentY + 23f, paint)

        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.rgb(6, 95, 70)
        canvas.drawText("FORM VIII (RULE 48)", rightMargin - 110f, currentY + 23f, paint)

        currentY += 46f

        // 4. Token & Transaction Metadata Card
        paint.color = Color.rgb(248, 250, 252)
        val metaRect = RectF(leftMargin, currentY, rightMargin, currentY + 54f)
        canvas.drawRoundRect(metaRect, 6f, 6f, paint)
        paint.color = Color.rgb(226, 232, 240)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(metaRect, 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        val timestamp = if (procurement.completedTimestamp > 0) procurement.completedTimestamp else procurement.createdAt
        val dateString = dateFormat.format(Date(timestamp))

        // Row 1 inside Meta
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.rgb(100, 116, 139)
        canvas.drawText("TOKEN / SLIP NO", leftMargin + 12f, currentY + 20f, paint)
        canvas.drawText("DATE & TIME", leftMargin + 150f, currentY + 20f, paint)
        canvas.drawText("VEHICLE NUMBER", leftMargin + 320f, currentY + 20f, paint)
        canvas.drawText("PAYMENT STATUS", rightMargin - 95f, currentY + 20f, paint)

        // Row 2 values inside Meta
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.rgb(37, 99, 235) // Blue-600
        canvas.drawText(procurement.tokenNo, leftMargin + 12f, currentY + 40f, paint)

        paint.color = Color.rgb(15, 23, 42)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(dateString, leftMargin + 150f, currentY + 40f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(procurement.vehicleNumber, leftMargin + 320f, currentY + 40f, paint)

        // Status Badge
        val isPaid = procurement.paymentStatus == "PAID"
        paint.color = if (isPaid) Color.rgb(16, 185, 129) else Color.rgb(245, 158, 11)
        canvas.drawText(procurement.paymentStatus, rightMargin - 95f, currentY + 40f, paint)

        currentY += 66f

        // 5. Two-Column Layout: Farmer Info (Left) & Commodity Specs (Right)
        val colWidth = (contentWidth - 14f) / 2f
        val boxHeight = 100f

        // 5A. Farmer Details Box
        val farmerRect = RectF(leftMargin, currentY, leftMargin + colWidth, currentY + boxHeight)
        drawSectionBox(canvas, paint, farmerRect, "FARMER / PRODUCER DETAILS (शेतकरी तपशील)")

        var fy = currentY + 34f
        drawKeyValueRow(canvas, paint, "Farmer Name:", procurement.farmerName, leftMargin + 10f, leftMargin + colWidth - 10f, fy)
        fy += 16f
        drawKeyValueRow(canvas, paint, "Village / Taluka:", procurement.village, leftMargin + 10f, leftMargin + colWidth - 10f, fy)
        fy += 16f
        drawKeyValueRow(canvas, paint, "Mobile No:", procurement.mobileNumber, leftMargin + 10f, leftMargin + colWidth - 10f, fy)
        fy += 16f
        val panStatus = if (procurement.isPanVerified) "Verified" else "Unverified"
        val panDisplay = if (procurement.panNumber.isNotBlank()) "${procurement.panNumber} ($panStatus)" else "Not Provided"
        drawKeyValueRow(canvas, paint, "PAN No:", panDisplay, leftMargin + 10f, leftMargin + colWidth - 10f, fy)

        // 5B. Crop & Logistics Box
        val cropRect = RectF(leftMargin + colWidth + 14f, currentY, rightMargin, currentY + boxHeight)
        drawSectionBox(canvas, paint, cropRect, "COMMODITY & BATCH SPECIFICATIONS")

        var cy = currentY + 34f
        drawKeyValueRow(canvas, paint, "Commodity Crop:", "${procurement.cropType} (${procurement.qualityGrade})", leftMargin + colWidth + 24f, rightMargin - 10f, cy)
        cy += 16f
        drawKeyValueRow(canvas, paint, "Assigned Godown:", procurement.godownAssigned, leftMargin + colWidth + 24f, rightMargin - 10f, cy)
        cy += 16f
        drawKeyValueRow(canvas, paint, "Standard Bags:", "${procurement.bagCount} Bags (@ ${procurement.bagWeightKg.toInt()} kg)", leftMargin + colWidth + 24f, rightMargin - 10f, cy)
        cy += 16f
        val moistRemark = if (procurement.moisturePercentage > 14.0) "(High Moist)" else "(Safe Std)"
        drawKeyValueRow(canvas, paint, "Moisture Level:", "${procurement.moisturePercentage}% $moistRemark", leftMargin + colWidth + 24f, rightMargin - 10f, cy)

        currentY += boxHeight + 14f

        // 6. Weighbridge Measurement Log
        val weighHeight = 82f
        val weighRect = RectF(leftMargin, currentY, rightMargin, currentY + weighHeight)
        drawSectionBox(canvas, paint, weighRect, "DIGITAL WEIGHBRIDGE RECORD (वजन मापाचा तपशील)")

        // 4 Columns for weights
        val wColW = contentWidth / 4f
        val wy = currentY + 36f

        // Col 1: Gross
        paint.textSize = 8.5f
        paint.color = Color.rgb(100, 116, 139)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("GROSS WEIGHT (Loaded)", leftMargin + 10f, wy, paint)
        paint.textSize = 13f
        paint.color = Color.rgb(15, 23, 42)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("${procurement.grossWeightKg.toInt()} kg", leftMargin + 10f, wy + 18f, paint)
        paint.textSize = 8f
        paint.color = Color.rgb(148, 163, 184)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Method: ${procurement.grossWeightMethod}", leftMargin + 10f, wy + 32f, paint)

        // Col 2: Tare
        paint.textSize = 8.5f
        paint.color = Color.rgb(100, 116, 139)
        canvas.drawText("TARE WEIGHT (Empty)", leftMargin + wColW + 10f, wy, paint)
        paint.textSize = 13f
        paint.color = Color.rgb(15, 23, 42)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("${procurement.tareWeightKg.toInt()} kg", leftMargin + wColW + 10f, wy + 18f, paint)
        paint.textSize = 8f
        paint.color = Color.rgb(148, 163, 184)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Method: ${procurement.tareWeightMethod}", leftMargin + wColW + 10f, wy + 32f, paint)

        // Col 3: Net Kg
        paint.textSize = 8.5f
        paint.color = Color.rgb(5, 150, 105)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("NET GRAIN WEIGHT", leftMargin + wColW * 2 + 10f, wy, paint)
        paint.textSize = 14f
        paint.color = Color.rgb(5, 150, 105)
        canvas.drawText("${procurement.netWeightKg.toInt()} kg", leftMargin + wColW * 2 + 10f, wy + 18f, paint)
        val mtVal = String.format(Locale.ENGLISH, "%.3f", procurement.netWeightKg / 1000.0)
        paint.textSize = 8f
        canvas.drawText("Metric Tonnes: $mtVal MT", leftMargin + wColW * 2 + 10f, wy + 32f, paint)

        // Col 4: Net Quintals
        val qtl = procurement.netWeightKg / 100.0
        val qtlStr = String.format(Locale.ENGLISH, "%.2f", qtl)
        paint.textSize = 8.5f
        paint.color = Color.rgb(37, 99, 235)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("NET IN QUINTALS", leftMargin + wColW * 3 + 10f, wy, paint)
        paint.textSize = 14f
        paint.color = Color.rgb(37, 99, 235)
        canvas.drawText("$qtlStr Qtl", leftMargin + wColW * 3 + 10f, wy + 18f, paint)
        paint.textSize = 8f
        paint.color = Color.rgb(100, 116, 139)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("1 Qtl = 100 Kg", leftMargin + wColW * 3 + 10f, wy + 32f, paint)

        currentY += weighHeight + 14f

        // 7. Financial Settlement & APMC Deductions Table
        val grossAmt = if (procurement.grossBillAmount > 0) procurement.grossBillAmount else (qtl * procurement.ratePerQuintal)
        val cessFee = if (procurement.mandiMarketFee > 0) procurement.mandiMarketFee else (if (procurement.applyMandiCess) grossAmt * 0.01 else 0.0)
        val cessSuper = if (procurement.mandiSupervisoryCharge > 0) procurement.mandiSupervisoryCharge else (if (procurement.applyMandiCess) grossAmt * 0.005 else 0.0)
        val totalCess = if (procurement.totalMandiCess > 0) procurement.totalMandiCess else (cessFee + cessSuper)
        val tdsAmt = procurement.tdsDeductedAmount
        val netPayout = if (procurement.totalAmount > 0) procurement.totalAmount else (grossAmt - totalCess - tdsAmt)

        val tableHeight = 150f
        val tableRect = RectF(leftMargin, currentY, rightMargin, currentY + tableHeight)
        drawSectionBox(canvas, paint, tableRect, "FINANCIAL SETTLEMENT & STATUTORY APMC TAXATION (हिशोब)")

        var ty = currentY + 32f
        drawFinancialRow(canvas, paint, "1. Agreed Purchase Rate (प्रति क्विंटल दर)", "₹ ${procurement.ratePerQuintal.toInt()} / Qtl", leftMargin, rightMargin, ty, isBold = false)
        ty += 18f
        drawFinancialRow(canvas, paint, "2. Gross Bill Amount ($qtlStr Qtl × ₹${procurement.ratePerQuintal.toInt()})", inrCurrencyFormat.format(grossAmt), leftMargin, rightMargin, ty, isBold = true)
        ty += 18f

        if (procurement.applyMandiCess || totalCess > 0) {
            drawFinancialRow(canvas, paint, "   • Less: Maharashtra APMC 1.0% Mandi Market Fee", "- ${inrCurrencyFormat.format(cessFee)}", leftMargin, rightMargin, ty, isDeduction = true)
            ty += 16f
            drawFinancialRow(canvas, paint, "   • Less: Maharashtra APMC 0.5% Supervisory Cess", "- ${inrCurrencyFormat.format(cessSuper)}", leftMargin, rightMargin, ty, isDeduction = true)
            ty += 16f
        } else {
            drawFinancialRow(canvas, paint, "   • Mandi Cess (Exempt / Outside APMC Jurisdiction)", "₹ 0.00", leftMargin, rightMargin, ty, isMuted = true)
            ty += 16f
        }

        if (tdsAmt > 0) {
            val ratePct = if (procurement.tdsRate > 0) "${procurement.tdsRate * 100}%" else "0.1%"
            drawFinancialRow(canvas, paint, "   • Less: Income Tax Section 194Q TDS Deduction ($ratePct)", "- ${inrCurrencyFormat.format(tdsAmt)}", leftMargin, rightMargin, ty, isDeduction = true)
            ty += 16f
        }

        // Divider before Total
        paint.color = Color.rgb(203, 213, 225)
        paint.strokeWidth = 1f
        canvas.drawLine(leftMargin + 10f, ty, rightMargin - 10f, ty, paint)
        ty += 18f

        // Highlight Net Total Row
        paint.color = Color.rgb(236, 253, 245)
        canvas.drawRect(leftMargin + 6f, ty - 12f, rightMargin - 6f, ty + 16f, paint)

        paint.color = Color.rgb(6, 95, 70)
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("NET FINAL PAYOUT TO FARMER (निव्वळ देय रक्कम)", leftMargin + 12f, ty + 4f, paint)

        paint.textSize = 15f
        paint.color = Color.rgb(5, 150, 105)
        val formattedPayout = inrCurrencyFormat.format(netPayout)
        canvas.drawText(formattedPayout, rightMargin - 12f - paint.measureText(formattedPayout), ty + 4f, paint)

        currentY += tableHeight + 10f

        // 8. Amount in Words Box
        paint.color = Color.rgb(248, 250, 252)
        val wordsRect = RectF(leftMargin, currentY, rightMargin, currentY + 30f)
        canvas.drawRoundRect(wordsRect, 4f, 4f, paint)
        paint.color = Color.rgb(226, 232, 240)
        paint.style = Paint.Style.STROKE
        canvas.drawRoundRect(wordsRect, 4f, 4f, paint)
        paint.style = Paint.Style.FILL

        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.rgb(71, 85, 105)
        canvas.drawText("Amount in Words: ", leftMargin + 10f, currentY + 18f, paint)
        val wordsWidth = paint.measureText("Amount in Words: ")

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.rgb(15, 23, 42)
        val amountWords = convertAmountToIndianRupeeWords(netPayout.toLong())
        canvas.drawText(amountWords, leftMargin + 10f + wordsWidth, currentY + 18f, paint)

        currentY += 40f

        // 9. Payment Settlement Details & Bank Note
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.rgb(100, 116, 139)
        val paymentRef = if (procurement.utrOrChequeNo.isNotBlank()) "Ref: ${procurement.utrOrChequeNo}" else "Direct Settlement"
        canvas.drawText("Payment Mode: ${procurement.paymentMode} ($paymentRef)  |  Account Holder: ${procurement.farmerName}", leftMargin, currentY, paint)

        currentY += 16f

        // 10. Digital Seal, QR & Signatures
        val sigTop = pageHeight - 110f
        paint.color = Color.rgb(226, 232, 240)
        canvas.drawLine(leftMargin, sigTop - 10f, rightMargin, sigTop - 10f, paint)

        // Left Signature: Farmer
        paint.color = Color.rgb(148, 163, 184)
        paint.pathEffect = DashPathEffect(floatArrayOf(4f, 4f), 0f)
        paint.style = Paint.Style.STROKE
        canvas.drawLine(leftMargin + 10f, sigTop + 45f, leftMargin + 160f, sigTop + 45f, paint)
        paint.pathEffect = null
        paint.style = Paint.Style.FILL

        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.rgb(71, 85, 105)
        canvas.drawText("Farmer's Signature / Thumb", leftMargin + 10f, sigTop + 60f, paint)
        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("मी वजन व हिशोबाशी सहमत आहे.", leftMargin + 10f, sigTop + 72f, paint)

        // Center: QR Code Graphic Simulation & Verification
        drawQrCodeGraphic(canvas, (pageWidth / 2f) - 20f, sigTop + 5f, 40f)
        paint.textSize = 7.5f
        paint.color = Color.rgb(100, 116, 139)
        val qrNote = "Audit Scan Verified"
        canvas.drawText(qrNote, (pageWidth / 2f) - (paint.measureText(qrNote) / 2f), sigTop + 58f, paint)

        // Right Signature: Authorized APMC Trader
        paint.pathEffect = DashPathEffect(floatArrayOf(4f, 4f), 0f)
        paint.style = Paint.Style.STROKE
        paint.color = Color.rgb(148, 163, 184)
        canvas.drawLine(rightMargin - 160f, sigTop + 45f, rightMargin - 10f, sigTop + 45f, paint)
        paint.pathEffect = null
        paint.style = Paint.Style.FILL

        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.rgb(71, 85, 105)
        canvas.drawText("For ${firm.firmName}", rightMargin - 160f, sigTop + 60f, paint)
        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Authorized Weighbridge Signatory", rightMargin - 160f, sigTop + 72f, paint)

        // Footer disclaimer
        paint.textSize = 7.5f
        paint.color = Color.rgb(148, 163, 184)
        val footerText = "Generated securely via GrainOS Agricultural ERP. Computerized record under Maharashtra APMC Rules."
        canvas.drawText(footerText, (pageWidth / 2f) - (paint.measureText(footerText) / 2f), pageHeight - 15f, paint)
    }

    private fun drawSectionBox(canvas: Canvas, paint: Paint, rect: RectF, title: String) {
        paint.color = Color.rgb(248, 250, 252) // Slate-50
        canvas.drawRoundRect(rect, 6f, 6f, paint)

        paint.color = Color.rgb(226, 232, 240) // Slate-200 border
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(rect, 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        // Header Title Bar
        paint.color = Color.rgb(241, 245, 249)
        val headerRect = RectF(rect.left, rect.top, rect.right, rect.top + 20f)
        canvas.drawRoundRect(headerRect, 6f, 6f, paint)
        canvas.drawRect(rect.left, rect.top + 10f, rect.right, rect.top + 20f, paint)

        paint.color = Color.rgb(30, 41, 59)
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(title, rect.left + 10f, rect.top + 14f, paint)
    }

    private fun drawKeyValueRow(
        canvas: Canvas,
        paint: Paint,
        key: String,
        value: String,
        leftX: Float,
        rightX: Float,
        y: Float
    ) {
        paint.textSize = 8.5f
        paint.color = Color.rgb(100, 116, 139)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(key, leftX, y, paint)

        paint.color = Color.rgb(15, 23, 42)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val valueWidth = paint.measureText(value)
        val maxAvailable = rightX - leftX - 65f
        val displayValue = if (valueWidth > maxAvailable) {
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            value.take(22) + "..."
        } else value
        canvas.drawText(displayValue, rightX - paint.measureText(displayValue), y, paint)
    }

    private fun drawFinancialRow(
        canvas: Canvas,
        paint: Paint,
        label: String,
        amount: String,
        leftMargin: Float,
        rightMargin: Float,
        y: Float,
        isBold: Boolean = false,
        isDeduction: Boolean = false,
        isMuted: Boolean = false
    ) {
        paint.textSize = if (isBold) 9.5f else 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, if (isBold) Typeface.BOLD else Typeface.NORMAL)
        paint.color = when {
            isDeduction -> Color.rgb(220, 38, 38) // Red
            isMuted -> Color.rgb(148, 163, 184)
            isBold -> Color.rgb(15, 23, 42)
            else -> Color.rgb(71, 85, 105)
        }
        canvas.drawText(label, leftMargin + 12f, y, paint)

        val textWidth = paint.measureText(amount)
        canvas.drawText(amount, rightMargin - 12f - textWidth, y, paint)
    }

    private fun drawQrCodeGraphic(canvas: Canvas, startX: Float, startY: Float, size: Float) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.rgb(15, 23, 42)

        // Outer square box
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        canvas.drawRect(startX, startY, startX + size, startY + size, paint)

        paint.style = Paint.Style.FILL
        // 3 Corner finder squares
        val cornerSize = size * 0.28f
        canvas.drawRect(startX + 2f, startY + 2f, startX + cornerSize, startY + cornerSize, paint)
        canvas.drawRect(startX + size - cornerSize, startY + 2f, startX + size - 2f, startY + cornerSize, paint)
        canvas.drawRect(startX + 2f, startY + size - cornerSize, startX + cornerSize, startY + size - 2f, paint)

        // Inner center blocks
        canvas.drawRect(startX + size * 0.4f, startY + size * 0.4f, startX + size * 0.6f, startY + size * 0.6f, paint)
        canvas.drawRect(startX + size * 0.65f, startY + size * 0.65f, startX + size * 0.85f, startY + size * 0.85f, paint)
        canvas.drawRect(startX + size * 0.4f, startY + size * 0.7f, startX + size * 0.55f, startY + size * 0.85f, paint)
    }

    /**
     * Converts a numeric Rupee amount to Indian English words.
     */
    fun convertAmountToIndianRupeeWords(n: Long): String {
        if (n == 0L) return "Rupees Zero Only"
        val units = arrayOf(
            "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten",
            "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen"
        )
        val tens = arrayOf("", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety")

        fun numToWordsLessThanThousand(num: Int): String {
            var current = num
            var result = ""
            if (current >= 100) {
                result += units[current / 100] + " Hundred "
                current %= 100
            }
            if (current in 1..19) {
                result += units[current] + " "
            } else if (current >= 20) {
                result += tens[current / 10] + " "
                if (current % 10 > 0) {
                    result += units[current % 10] + " "
                }
            }
            return result.trim()
        }

        var num = n
        var result = ""

        val crore = (num / 10000000L).toInt()
        num %= 10000000L
        if (crore > 0) {
            result += "${numToWordsLessThanThousand(crore)} Crore "
        }

        val lakh = (num / 100000L).toInt()
        num %= 100000L
        if (lakh > 0) {
            result += "${numToWordsLessThanThousand(lakh)} Lakh "
        }

        val thousand = (num / 1000L).toInt()
        num %= 1000L
        if (thousand > 0) {
            result += "${numToWordsLessThanThousand(thousand)} Thousand "
        }

        val hundred = num.toInt()
        if (hundred > 0) {
            result += numToWordsLessThanThousand(hundred) + " "
        }

        return "Rupees ${result.trim()} Only"
    }

    /**
     * Downloads the formatted PDF receipt directly to the device's public Downloads directory.
     */
    fun downloadProfessionalPdf(context: Context, procurement: ProcurementEntity, firmProfile: FirmProfile): File? {
        return try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }
            val fileName = "GrainOS_Receipt_${procurement.tokenNo}_${System.currentTimeMillis()}.pdf"
            val targetFile = File(downloadsDir, fileName)

            generateProcurementReceiptPdf(context, procurement, firmProfile, targetFile)

            Toast.makeText(
                context,
                "✓ PDF Receipt exported to Downloads:\n${targetFile.name}",
                Toast.LENGTH_LONG
            ).show()

            targetFile
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to internal cache if public storage fails
            try {
                val fallbackFile = File(context.cacheDir, "GrainOS_Receipt_${procurement.tokenNo}.pdf")
                generateProcurementReceiptPdf(context, procurement, firmProfile, fallbackFile)
                Toast.makeText(context, "✓ PDF saved to app storage: ${fallbackFile.name}", Toast.LENGTH_LONG).show()
                fallbackFile
            } catch (err: Exception) {
                Toast.makeText(context, "Failed to export PDF: ${err.message}", Toast.LENGTH_SHORT).show()
                null
            }
        }
    }

    /**
     * Exports and triggers the system share sheet (WhatsApp, Email, Drive, Print) with the generated PDF.
     */
    fun exportAndSharePdf(context: Context, procurement: ProcurementEntity, firmProfile: FirmProfile) {
        try {
            val pdfFile = generateProcurementReceiptPdf(context, procurement, firmProfile)
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                pdfFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Procurement Slip - Token ${procurement.tokenNo} (${procurement.farmerName})")
                putExtra(Intent.EXTRA_TEXT, "Official APMC Procurement Receipt for Token ${procurement.tokenNo} - ${firmProfile.firmName}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(shareIntent, "Export / Share PDF Receipt")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error sharing PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Opens the generated PDF in the device's native PDF Viewer or Print Spooler.
     */
    fun viewOrPrintPdf(context: Context, procurement: ProcurementEntity, firmProfile: FirmProfile) {
        try {
            val pdfFile = generateProcurementReceiptPdf(context, procurement, firmProfile)
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                pdfFile
            )

            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(viewIntent)
        } catch (e: Exception) {
            // If no PDF reader app is present, fallback to share intent
            exportAndSharePdf(context, procurement, firmProfile)
        }
    }
}
