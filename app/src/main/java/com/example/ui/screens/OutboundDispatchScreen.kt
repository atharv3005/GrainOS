package com.example.ui.screens
import androidx.compose.foundation.layout.PaddingValues

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.data.model.CropType
import com.example.data.model.GodownEntity
import com.example.data.model.OutboundDispatchEntity
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OutboundDispatchScreen(
    activeCrop: CropType,
    godowns: List<GodownEntity>,
    dispatches: List<OutboundDispatchEntity>,
    liveGodownStockLedger: Map<String, Double>,
    isSubmitting: Boolean = false,
    onCreateDispatch: (String, String, String, CropType, String, Double, Double, Double, () -> Unit) -> Unit,
    onOpenRecordRejectionForTruck: (truckNo: String, buyer: String, weightKg: Double) -> Unit = { _, _, _ -> },
    onOpenSettleDispatch: (OutboundDispatchEntity) -> Unit = {},
    onEdit: (OutboundDispatchEntity) -> Unit = {},
    onDelete: (OutboundDispatchEntity) -> Unit = {},
    firmName: String = "GrainOS Enterprise",
    modifier: Modifier = Modifier
) {
    var buyerName by remember { mutableStateOf("Adani Agri Logistics Ltd.") }
    var destination by remember { mutableStateOf("JNPT Port / Navi Mumbai Silo") }
    var vehicleNumber by remember { mutableStateOf("MH 04 FK 8819") }
    var driverMobile by remember { mutableStateOf("") }
    var selectedGodown by remember { mutableStateOf(godowns.firstOrNull()?.godownId ?: "GODOWN_A") }
    var tareWeightKg by remember { mutableStateOf("11400") }
    var grossWeightKg by remember { mutableStateOf("31400") }
    var ratePerQuintal by remember { mutableStateOf("2650") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var editingDispatch by remember { mutableStateOf<OutboundDispatchEntity?>(null) }
    val context = LocalContext.current

    val tare by remember { androidx.compose.runtime.derivedStateOf { tareWeightKg.toDoubleOrNull() ?: 0.0 } }
    val gross by remember { androidx.compose.runtime.derivedStateOf { grossWeightKg.toDoubleOrNull() ?: 0.0 } }
    val netKg by remember { androidx.compose.runtime.derivedStateOf { (gross - tare).coerceAtLeast(0.0) } }
    val netMt by remember { androidx.compose.runtime.derivedStateOf { netKg / 1000.0 } }
    val rate by remember { androidx.compose.runtime.derivedStateOf { ratePerQuintal.toDoubleOrNull() ?: 0.0 } }
    val totalInvoice by remember { androidx.compose.runtime.derivedStateOf { (netKg / 100.0) * rate } }

    val animatedAccent by animateColorAsState(
        targetValue = activeCrop.primaryColor,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "dispatch_accent_anim"
    )

    val inrFormat = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }
    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFF38BDF8).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.LocalShipping, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Outbound Grain Dispatch", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                            Text("Sales to Millers, Processors & Exporters", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                        }
                    }

                    OutlinedButton(
                        onClick = { onOpenRecordRejectionForTruck(vehicleNumber, buyerName, netKg) },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.ReportProblem, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Log Rejection", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Divider(color = Color(0xFF334155))

                OutlinedTextField(
                    value = buyerName,
                    onValueChange = { buyerName = it },
                    label = { Text("Buyer / Corporate Miller Name") },
                    leadingIcon = { Icon(Icons.Default.Business, contentDescription = null, tint = Color(0xFF38BDF8)) },
                    modifier = Modifier.fillMaxWidth().testTag("input_buyer_name"),
                    colors = dispatchFieldColors(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = destination,
                    onValueChange = { destination = it },
                    label = { Text("Destination Delivery Point") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF38BDF8)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = dispatchFieldColors(),
                    singleLine = true
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = driverMobile,
                    onValueChange = { driverMobile = it },
                    label = { Text("Driver Mobile (WhatsApp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.weight(1f),
                    colors = dispatchFieldColors(),
                    singleLine = true
                )
                    OutlinedTextField(
                        value = vehicleNumber,
                        onValueChange = { vehicleNumber = it },
                        label = { Text("Truck Number") },
                        modifier = Modifier.weight(1f),
                        colors = dispatchFieldColors(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = ratePerQuintal,
                        onValueChange = { ratePerQuintal = it },
                        label = { Text("Sale Rate ₹/Qtl") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = dispatchFieldColors(),
                        singleLine = true
                    )
                }

                // Silo Source selector
                Text("Dispatch Silo Source:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFCBD5E1))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    godowns.forEach { g ->
                        val isSel = g.godownId == selectedGodown
                        val currentStockMt = liveGodownStockLedger[g.godownId] ?: 0.0
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSel) Color(0xFF38BDF8).copy(alpha = 0.2f) else Color(0xFF0F172A))
                                .border(1.dp, if (isSel) Color(0xFF38BDF8) else Color(0xFF334155), RoundedCornerShape(10.dp))
                                .clickable { selectedGodown = g.godownId }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(g.displayName.take(8), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSel) Color(0xFF38BDF8) else Color.White)
                                Text("${currentStockMt.toInt()} MT", fontSize = 9.sp, color = Color(0xFF94A3B8))
                            }
                        }
                    }
                }

                // Weights Grid
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = tareWeightKg,
                        onValueChange = { tareWeightKg = it },
                        label = { Text("Tare Wt (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = dispatchFieldColors(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = grossWeightKg,
                        onValueChange = { grossWeightKg = it },
                        label = { Text("Gross Wt (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = dispatchFieldColors(),
                        singleLine = true
                    )
                }

                // Calculation Summary
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF0F172A))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Net Dispatch Weight:", fontSize = 12.sp, color = Color(0xFF94A3B8))
                            Text("${netKg.toInt()} kg (${"%.2f".format(netMt)} MT)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (netKg > 0) Color(0xFF10B981) else Color(0xFF38BDF8))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Invoice Amount:", fontSize = 12.sp, color = Color(0xFF94A3B8))
                            Text(inrFormat.format(totalInvoice), fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = if (totalInvoice > 0) Color(0xFF10B981) else Color(0xFF94A3B8))
                        }
                    }
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = Color(0xFFEF4444),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = {
                        if (tare > gross) {
                            errorMessage = "Error: Tare weight cannot be greater than Gross weight."
                            return@Button
                        }
                        val currentStockMt = liveGodownStockLedger[selectedGodown] ?: 0.0
                        if (netMt > currentStockMt) {
                            errorMessage = "Error: Cannot dispatch ${netMt.toInt()} MT. Only ${currentStockMt.toInt()} MT available in this storage facility."
                            return@Button
                        }
                        errorMessage = null
                        
                        val capturedDest = destination
                        val capturedTruck = vehicleNumber
                        val capturedGross = gross
                        val capturedTare = tare
                        val capturedNet = netKg
                        
                        onCreateDispatch(buyerName, destination, vehicleNumber, activeCrop, selectedGodown, tare, gross, rate) {
                            buyerName = ""
                            destination = ""
                            vehicleNumber = ""
                            tareWeightKg = ""
                            grossWeightKg = ""
                            ratePerQuintal = ""
                        val firmName = "GrainOS Enterprise"
                        val msg = """*GATE PASS*
$firmName
-----------------------
Destination: $capturedDest
Truck No: $capturedTruck
Gross: ${capturedGross.toInt()} kg
Tare: ${capturedTare.toInt()} kg
Net: ${capturedNet.toInt()} kg
""".trimIndent()
                        val intent = Intent(Intent.ACTION_VIEW)
                        val numStr = driverMobile.filter { it.isDigit() }
                        if (numStr.isNotEmpty()) {
                            intent.data = Uri.parse("https://api.whatsapp.com/send?phone=91$numStr&text=${Uri.encode(msg)}")
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "WhatsApp not found.", Toast.LENGTH_SHORT).show()
                            }
                        }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("submit_dispatch_button"),
                    enabled = !isSubmitting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9800), // Neon Orange
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFFFF9800).copy(alpha = 0.5f),
                        disabledContentColor = Color.White.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Processing...", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold))
                    } else {
                        Icon(imageVector = Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Authorize Gate Pass & Save Dispatch", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold))
                    }
                }
            }
        }

        // Recent Dispatches History
        Text(
            text = "RECENT OUTBOUND DISPATCHES",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
            color = Color(0xFF94A3B8)
        )

        if (dispatches.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFF1E293B)).padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No outbound dispatches recorded today.", color = Color(0xFF64748B), fontSize = 12.sp)
            }
        } else {
            dispatches.forEach { d ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF1E293B))
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(d.buyerName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("${(d.netLoadedWeightKg / 1000.0 * 10).toLong() / 10.0} MT", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF38BDF8))
                                OutlinedButton(
                                    onClick = { onOpenRecordRejectionForTruck(d.vehicleNumber, d.buyerName, d.netLoadedWeightKg) },
                                    modifier = Modifier.height(26.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444)),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("Mark as Rejected", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Text("Destination: ${d.destination} • Truck: ${d.vehicleNumber}", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Invoice: ${inrFormat.format(d.totalInvoiceAmount)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                            
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                if (d.status == com.example.data.model.DispatchStatus.IN_TRANSIT.name) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF10B981).copy(alpha = 0.15f))
                                            .clickable { onOpenSettleDispatch(d) }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("Settle", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF34D399))
                                    }
                                } else {
                                    Text(d.status, fontSize = 10.sp, color = Color(0xFF64748B))
                                }
                                
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    tint = Color(0xFF60A5FA),
                                    modifier = Modifier.size(16.dp).clickable { editingDispatch = d }
                                )
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Color(0xFFF87171),
                                    modifier = Modifier.size(16.dp).clickable { onDelete(d) }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    editingDispatch?.let { disp ->
        com.example.ui.components.EditDispatchDialog(
            dispatch = disp,
            onDismiss = { editingDispatch = null },
            onConfirm = { updatedDisp ->
                onEdit(updatedDisp)
                editingDispatch = null
            }
        )
    }
}

@Composable
private fun dispatchFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Color(0xFF38BDF8),
    unfocusedBorderColor = Color(0xFF334155),
    focusedLabelColor = Color(0xFF38BDF8),
    unfocusedLabelColor = Color(0xFF94A3B8),
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    cursorColor = Color(0xFF38BDF8)
)
