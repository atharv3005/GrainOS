package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.FirmProfile
import com.example.data.model.ProcurementEntity
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PdfReceiptDialog(
    procurement: ProcurementEntity,
    firmProfile: FirmProfile = FirmProfile(),
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val inrFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    val dateStr = dateFormat.format(Date(procurement.completedTimestamp.takeIf { it > 0 } ?: procurement.createdAt))

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(16.dp)),
            color = Color(0xFF1E293B)
        ) {
            Column {
                // Modal Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F172A))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "PDF",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Procurement Slip (PDF Report)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = Color.White
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(30.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                // Printable A4 Invoice Sheet Container
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            // Header of FPC
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = firmProfile.firmName.uppercase(),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 15.sp
                                        ),
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = "Reg No: ${firmProfile.registrationNumber} • ${firmProfile.location}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                        color = Color(0xFF64748B)
                                    )
                                    Text(
                                        text = firmProfile.tagLine,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = Color(0xFF10B981)
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Default.QrCode2,
                                    contentDescription = "QR Code Verification",
                                    tint = Color(0xFF0F172A),
                                    modifier = Modifier.size(54.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Divider(color = Color(0xFFE2E8F0), thickness = 1.dp)
                            Spacer(modifier = Modifier.height(10.dp))

                            // Meta row: Token + Date
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "RECEIPT / BILL OF LADING",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = "Slip No: ${procurement.tokenNo}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        ),
                                        color = Color(0xFF2563EB)
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "DATE & TIME",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = Color(0xFF64748B)
                                    )
                                    Text(
                                        text = dateStr,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                        color = Color(0xFF0F172A)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Farmer & Vehicle details card
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFF8FAFC))
                                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
                                    .padding(10.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    PdfInfoRow("Farmer Name:", procurement.farmerName)
                                    PdfInfoRow("Mobile No:", procurement.mobileNumber)
                                    PdfInfoRow("Village / Region:", procurement.village)
                                    PdfInfoRow("Vehicle Number:", procurement.vehicleNumber)
                                    PdfInfoRow("Crop Commodity:", "${procurement.cropType} (${procurement.qualityGrade})")
                                    PdfInfoRow("Assigned Silo:", procurement.godownAssigned)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Weighbridge & Moisture Grid
                            Text(
                                text = "DIGITAL WEIGHBRIDGE & LAB LOG",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFF1F5F9))
                                    .padding(8.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Gross Weight (Loaded)", fontSize = 11.sp, color = Color(0xFF475569))
                                        Text("${procurement.grossWeightKg.toInt()} kg", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Tare Weight (Empty)", fontSize = 11.sp, color = Color(0xFF475569))
                                        Text("${procurement.tareWeightKg.toInt()} kg", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                    }
                                    Divider(color = Color(0xFFCBD5E1), thickness = 0.5.dp)
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Net Grain Procured", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF059669))
                                        Text("${procurement.netWeightKg.toInt()} kg (${(procurement.netWeightKg / 1000.0 * 100).toLong() / 100.0} MT)", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF059669))
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Lab Moisture %", fontSize = 11.sp, color = Color(0xFF475569))
                                        Text("${procurement.moisturePercentage}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Payout calculation box
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFECFDF5))
                                    .border(1.dp, Color(0xFFA7F3D0), RoundedCornerShape(6.dp))
                                    .padding(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Rate per Quintal", fontSize = 10.sp, color = Color(0xFF065F46))
                                        Text("₹${procurement.ratePerQuintal.toInt()} / qtl", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF065F46))
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Total Payout Amount", fontSize = 10.sp, color = Color(0xFF065F46))
                                        Text(
                                            inrFormat.format(procurement.totalAmount),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF047857)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Digital Seal and Signatures
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column {
                                    Divider(modifier = Modifier.width(100.dp), color = Color(0xFF94A3B8), thickness = 1.dp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Authorized Signatory", fontSize = 10.sp, color = Color(0xFF64748B))
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Icon(Icons.Default.Verified, contentDescription = "Seal", tint = Color(0xFF059669), modifier = Modifier.size(32.dp))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Digitally Verified", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF059669))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                generateAndSharePdf(context, procurement, firmProfile, inrFormat)
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Icon(imageVector = Icons.Default.Print, contentDescription = "Print", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Print Slip", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                generateAndSharePdf(context, procurement, firmProfile, inrFormat, share = true)
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981), contentColor = Color.White)
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PdfInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = Color(0xFF64748B)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
            ),
            color = Color(0xFF0F172A)
        )
    }
}

private fun generateAndSharePdf(context: Context, procurement: ProcurementEntity, firmProfile: FirmProfile, inrFormat: java.text.NumberFormat, share: Boolean = false) {
    val pdfDocument = android.graphics.pdf.PdfDocument()
    val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas
    val paint = android.graphics.Paint()

    paint.color = android.graphics.Color.BLACK
    paint.textSize = 24f
    paint.isFakeBoldText = true
    canvas.drawText("${firmProfile.firmName} - Receipt", 50f, 80f, paint)
    
    paint.textSize = 14f
    paint.isFakeBoldText = false
    var y = 140f
    val lineSpacing = 30f
    
    canvas.drawText("Token No: ${procurement.tokenNo}", 50f, y, paint); y += lineSpacing
    canvas.drawText("Farmer: ${procurement.farmerName}", 50f, y, paint); y += lineSpacing
    canvas.drawText("Vehicle: ${procurement.vehicleNumber}", 50f, y, paint); y += lineSpacing
    canvas.drawText("Crop: ${procurement.cropType} (${procurement.qualityGrade})", 50f, y, paint); y += lineSpacing
    canvas.drawText("Net Weight: ${procurement.netWeightKg} kg", 50f, y, paint); y += lineSpacing
    canvas.drawText("Rate: Rs. ${procurement.ratePerQuintal} / Qtl", 50f, y, paint); y += lineSpacing
    
    paint.isFakeBoldText = true
    canvas.drawText("Total Payout: ${inrFormat.format(procurement.totalAmount)}", 50f, y + 20f, paint)

    pdfDocument.finishPage(page)

    try {
        val file = java.io.File(context.cacheDir, "Receipt_${procurement.tokenNo}.pdf")
        java.io.FileOutputStream(file).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        val uri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        
        if (share) {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "application/pdf"
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context.startActivity(Intent.createChooser(intent, "Share PDF Slip"))
        } else {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(uri, "application/pdf")
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context.startActivity(intent)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error generating PDF", Toast.LENGTH_SHORT).show()
    }
}
