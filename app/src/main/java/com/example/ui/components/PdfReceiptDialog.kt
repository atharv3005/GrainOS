package com.example.ui.components

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Download
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.export.PdfExporter
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
    val inrFormat = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH) }
    val dateStr = remember(procurement) {
        val ts = if (procurement.completedTimestamp > 0) procurement.completedTimestamp else procurement.createdAt
        dateFormat.format(Date(ts))
    }

    val qtl = procurement.netWeightKg / 100.0
    val grossAmt = if (procurement.grossBillAmount > 0) procurement.grossBillAmount else (qtl * procurement.ratePerQuintal)
    val cessFee = if (procurement.mandiMarketFee > 0) procurement.mandiMarketFee else (if (procurement.applyMandiCess) grossAmt * 0.01 else 0.0)
    val cessSuper = if (procurement.mandiSupervisoryCharge > 0) procurement.mandiSupervisoryCharge else (if (procurement.applyMandiCess) grossAmt * 0.005 else 0.0)
    val totalCess = if (procurement.totalMandiCess > 0) procurement.totalMandiCess else (cessFee + cessSuper)
    val tdsAmt = procurement.tdsDeductedAmount
    val netPayout = if (procurement.totalAmount > 0) procurement.totalAmount else (grossAmt - totalCess - tdsAmt)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(16.dp))
                .testTag("pdf_receipt_dialog"),
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
                        Column {
                            Text(
                                text = "Procurement PDF Receipt Layout",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                ),
                                color = Color.White
                            )
                            Text(
                                text = "Token: ${procurement.tokenNo} • Maharashtra APMC Form VIII",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(30.dp)
                            .testTag("close_pdf_dialog_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                // Scrollable Document Layout
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Printable A4 Invoice Container Simulation
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            // Header of FPC & Verification Code
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = firmProfile.firmName.uppercase(Locale.ENGLISH),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 15.sp
                                        ),
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = "APMC Reg: ${firmProfile.registrationNumber} • GSTIN: ${firmProfile.gstNumber}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                        color = Color(0xFF64748B)
                                    )
                                    Text(
                                        text = "Mandi Yard: ${firmProfile.location} • Ph: ${firmProfile.contactNumber}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Medium
                                        ),
                                        color = Color(0xFF059669)
                                    )
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.QrCode2,
                                        contentDescription = "QR Code Verification",
                                        tint = Color(0xFF0F172A),
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Text(
                                        text = "Audit Verified",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Divider(color = Color(0xFFE2E8F0), thickness = 1.dp)
                            Spacer(modifier = Modifier.height(10.dp))

                            // Document Title & Tag
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFECFDF5))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "MAHARASHTRA APMC PROCUREMENT SLIP (खरेदी पावती)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF065F46)
                                    )
                                    Text(
                                        text = "RULE 48 COMPLIANT",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF047857)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Meta row: Token + Date + Vehicle + Payment Status
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "SLIP / TOKEN NO",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                        color = Color(0xFF64748B)
                                    )
                                    Text(
                                        text = procurement.tokenNo,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 13.sp),
                                        color = Color(0xFF2563EB)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "VEHICLE NO",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                        color = Color(0xFF64748B)
                                    )
                                    Text(
                                        text = procurement.vehicleNumber,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp),
                                        color = Color(0xFF0F172A)
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "DATE & STATUS",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                        color = Color(0xFF64748B)
                                    )
                                    Text(
                                        text = "${procurement.paymentStatus} • $dateStr",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        ),
                                        color = if (procurement.paymentStatus == "PAID") Color(0xFF10B981) else Color(0xFFF59E0B)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Farmer & Crop Details Box
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFF8FAFC))
                                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
                                    .padding(10.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    PdfInfoRow("Farmer / Producer:", "${procurement.farmerName} (${procurement.village})")
                                    PdfInfoRow("Mobile / PAN:", "${procurement.mobileNumber} • ${if (procurement.panNumber.isNotBlank()) procurement.panNumber else "PAN NOT GIVEN"}")
                                    PdfInfoRow("Commodity & Grade:", "${procurement.cropType} (${procurement.qualityGrade})")
                                    PdfInfoRow("Godown & Packing:", "${procurement.godownAssigned} • ${procurement.bagCount} Bags (@ ${procurement.bagWeightKg.toInt()} kg)")
                                    PdfInfoRow("Moisture Level:", "${procurement.moisturePercentage}% ${if (procurement.moisturePercentage > 14.0) "(High)" else "(Safe Std)"}")
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Weighbridge Log
                            Text(
                                text = "DIGITAL WEIGHBRIDGE & LAB LOG",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
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
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Gross Loaded Weight", fontSize = 11.sp, color = Color(0xFF475569))
                                        Text("${procurement.grossWeightKg.toInt()} kg (${procurement.grossWeightMethod})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Tare Empty Weight", fontSize = 11.sp, color = Color(0xFF475569))
                                        Text("${procurement.tareWeightKg.toInt()} kg (${procurement.tareWeightMethod})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                    }
                                    Divider(color = Color(0xFFCBD5E1), thickness = 0.5.dp)
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Net Weight Procured", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF059669))
                                        Text("${procurement.netWeightKg.toInt()} kg (${String.format(Locale.ENGLISH, "%.2f", qtl)} Qtl)", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF059669))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Financial Settlement & Deductions
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFECFDF5))
                                    .border(1.dp, Color(0xFFA7F3D0), RoundedCornerShape(6.dp))
                                    .padding(10.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Agreed Rate / Qtl", fontSize = 11.sp, color = Color(0xFF065F46))
                                        Text("₹${procurement.ratePerQuintal.toInt()} / Qtl", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF065F46))
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Gross Bill Amount", fontSize = 11.sp, color = Color(0xFF065F46))
                                        Text(inrFormat.format(grossAmt), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF065F46))
                                    }

                                    if (totalCess > 0) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Less: APMC Mandi Cess (1.5%)", fontSize = 10.sp, color = Color(0xFFDC2626))
                                            Text("- ${inrFormat.format(totalCess)}", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFDC2626))
                                        }
                                    }
                                    if (tdsAmt > 0) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Less: TDS u/s 194Q", fontSize = 10.sp, color = Color(0xFFDC2626))
                                            Text("- ${inrFormat.format(tdsAmt)}", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFDC2626))
                                        }
                                    }

                                    Divider(color = Color(0xFFA7F3D0), thickness = 1.dp)

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("TOTAL FINAL PAYOUT", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF065F46))
                                        Text(
                                            inrFormat.format(netPayout),
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFF047857)
                                        )
                                    }

                                    Text(
                                        text = "In Words: ${PdfExporter.convertAmountToIndianRupeeWords(netPayout.toLong())}",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF065F46)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Signatures Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column {
                                    Divider(modifier = Modifier.width(90.dp), color = Color(0xFF94A3B8), thickness = 1.dp)
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text("Farmer's Signature", fontSize = 9.sp, color = Color(0xFF64748B))
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Icon(Icons.Default.Verified, contentDescription = "Seal", tint = Color(0xFF059669), modifier = Modifier.size(28.dp))
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text("Authorized Signatory", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF059669))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 3 Action Buttons for Export, Download, and Printing
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                PdfExporter.downloadProfessionalPdf(context, procurement, firmProfile)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("download_pdf_receipt_btn"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFF475569))
                        ) {
                            Icon(imageVector = Icons.Default.Download, contentDescription = "Download", modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Download", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                PdfExporter.viewOrPrintPdf(context, procurement, firmProfile)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("print_pdf_receipt_btn"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFF475569))
                        ) {
                            Icon(imageVector = Icons.Default.Print, contentDescription = "Print", modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Print / View", fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                PdfExporter.exportAndSharePdf(context, procurement, firmProfile)
                            },
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("export_share_pdf_receipt_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981), contentColor = Color.Black)
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Export PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
