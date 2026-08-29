package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.CropType
import com.example.data.model.FirmProfile
import com.example.data.model.ProcurementEntity
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WhatsAppReceiptDialog(
    procurement: ProcurementEntity,
    firmProfile: FirmProfile = FirmProfile(),
    isEntryOnly: Boolean = false,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(if (isEntryOnly) procurement.grossTimestamp.takeIf { it > 0 } ?: procurement.createdAt else procurement.completedTimestamp.takeIf { it > 0 } ?: System.currentTimeMillis()))
    val inrFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    val effectiveGross = procurement.grossWeightKg
    val effectiveTare = if (procurement.tareWeightKg > 0) procurement.tareWeightKg else 0.0
    val effectiveNet = if (procurement.netWeightKg > 0) procurement.netWeightKg else (effectiveGross - effectiveTare).coerceAtLeast(0.0)
    val crop = CropType.entries.find { it.name == procurement.cropType } ?: CropType.MAIZE
    val effectiveRate = if (procurement.ratePerQuintal > 0) procurement.ratePerQuintal else crop.standardMsp
    val calculatedGrossBill = (effectiveNet / 100.0) * effectiveRate
    val effectiveTotal = if (procurement.totalAmount > 0) procurement.totalAmount else calculatedGrossBill
    val netQuintals = effectiveNet / 100.0

    val formattedMsg = if (isEntryOnly) {
        """
        🌾 *${firmProfile.firmName}* 🌾
        _${firmProfile.tagLine}_
        
        Dear *${procurement.farmerName}*,
        
        ✅ *Vehicle Entry Successful*
        
        • *Vehicle:* ${procurement.vehicleNumber}
        • *Gross Weight:* ${effectiveGross.toInt()} kg
        • *Crop:* ${procurement.cropType}
        • *Time:* $timeStr
        • *Token ID:* ${procurement.tokenNo}
        • *Operator:* ${firmProfile.operatorName}

        _Please proceed to Quality Lab & Unloading Bay._
        """.trimIndent()
    } else {
        """
        🌾 *${firmProfile.firmName}* 🌾
        _Official Procurement Receipt & Payout_
        
        *Farmer:* ${procurement.farmerName}
        *Mobile:* ${procurement.mobileNumber}
        *Vehicle:* ${procurement.vehicleNumber}
        *Crop:* ${procurement.cropType}
        *Moisture:* ${procurement.moisturePercentage}% (${procurement.qualityGrade})
        
        ⚖️ *Weight Breakdown:*
        • *Gross Weight:* ${effectiveGross.toInt()} kg
        • *Tare Weight:* ${effectiveTare.toInt()} kg
        ----------------------------
        ✨ *Net Grain:* ${effectiveNet.toInt()} kg (${"%.2f".format(netQuintals)} Qtl)
        
        💰 *Rate:* ₹${effectiveRate.toInt()}/Qtl
        💵 *Total Amount:* ${inrFormat.format(effectiveTotal)}
        
        📍 *Stored at:* ${procurement.godownAssigned}
        💳 *Payment Status:* ${procurement.paymentStatus}
        
        *Thank You for Partnering with ${firmProfile.firmName}!*
        """.trimIndent()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(20.dp)),
            color = Color(0xFFECE5DD) // Classic WhatsApp wallpaper color
        ) {
            Column {
                // WhatsApp Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF075E54)) // WhatsApp dark green
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF25D366)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🌾",
                                fontSize = 18.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = firmProfile.firmName,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                ),
                                color = Color.White
                            )
                            Text(
                                text = "WhatsApp Business API • Verified",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                color = Color(0xFFD1FAE5)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                // Chat Area with Bubble
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Date pill
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFD1D7DB))
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "TODAY",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFF54656F)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Green WhatsApp Sent Bubble
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .align(Alignment.End)
                            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 0.dp, bottomStart = 12.dp, bottomEnd = 12.dp))
                            .background(Color(0xFFDCF8C6))
                            .border(0.5.dp, Color(0xFFC3E6CB), RoundedCornerShape(topStart = 12.dp, topEnd = 0.dp, bottomStart = 12.dp, bottomEnd = 12.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = formattedMsg,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 13.sp,
                                    lineHeight = 19.sp,
                                    fontFamily = FontFamily.SansSerif
                                ),
                                color = Color(0xFF111B21)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = timeStr,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = Color(0xFF667781)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.DoneAll,
                                    contentDescription = "Read",
                                    tint = Color(0xFF53BDEB), // Double blue ticks!
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }

                // Actions Footer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            shareToWhatsApp(context, procurement.mobileNumber, formattedMsg)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF25D366),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Send via WhatsApp to ${procurement.mobileNumber}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}

private fun shareToWhatsApp(context: Context, mobile: String, message: String) {
    try {
        val cleanPhone = mobile.replace("+", "").replace(" ", "").replace("-", "").trim()
        val encodedText = java.net.URLEncoder.encode(message, "UTF-8")
        val uri = android.net.Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=$encodedText")
        val whatsappIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.whatsapp")
        }
        try {
            context.startActivity(whatsappIntent)
        } catch (e: Exception) {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, message)
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, "Share Grain Receipt via WhatsApp")
            context.startActivity(shareIntent)
        }
    } catch (e: Exception) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, message)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Grain Receipt via WhatsApp")
        context.startActivity(shareIntent)
    }
}
