package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.CropType
import java.text.NumberFormat
import java.util.Locale

/**
 * Truck Rejection & Manual Loss Dialog.
 * Triggered when a buyer/mill rejects a dispatched truck.
 * Captures user-entered manual losses:
 * - Transport Loss (₹)
 * - Penalties & Demurrage (₹)
 * - Quality / Salvage Deduction (₹)
 * - Salvage / Reroute Action
 */
@Composable
fun TruckRejectionDialog(
    activeCrop: CropType,
    initialTruckNo: String = "",
    initialBuyer: String = "",
    initialWeightKg: Double = 20000.0,
    onRecordRejection: (
        truckNumber: String,
        buyerOrCompany: String,
        cropType: CropType,
        dispatchedWeightKg: Double,
        rejectionReason: String,
        transportLoss: Double,
        penaltiesDemurrage: Double,
        qualitySalvageDeduction: Double,
        salvageAction: String,
        notes: String
    ) -> Unit,
    onDismiss: () -> Unit
) {
    var truckNumber by remember { mutableStateOf(initialTruckNo.ifEmpty { "MH 18 B 9912" }) }
    var buyerOrCompany by remember { mutableStateOf(initialBuyer.ifEmpty { "Patanjali Feed Mill / ITC" }) }
    var dispatchedWeightKg by remember { mutableDoubleStateOf(if (initialWeightKg > 0) initialWeightKg else 20000.0) }
    var rejectionReason by remember { mutableStateOf("Moisture > 14.5% & Black grains") }
    var transportLoss by remember { mutableDoubleStateOf(12500.0) }
    var penaltiesDemurrage by remember { mutableDoubleStateOf(3500.0) }
    var qualitySalvageDeduction by remember { mutableDoubleStateOf(7000.0) }
    var salvageAction by remember { mutableStateOf("Diverted to local Poultry Mill at discount") }
    var notes by remember { mutableStateOf("") }

    val totalRejectionLoss = transportLoss + penaltiesDemurrage + qualitySalvageDeduction
    val inrFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .border(1.5.dp, Color(0xFFEF4444).copy(alpha = 0.7f), RoundedCornerShape(24.dp)),
            color = Color(0xFF0F172A),
            tonalElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Banner
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFEF4444).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ReportProblem,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Truck Rejection Audit",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 17.sp
                                ),
                                color = Color.White
                            )
                            Text(
                                text = "Record Manual Loss & Operational Deductions",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = Color(0xFFFCA5A5)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFEF4444).copy(alpha = 0.2f))
                            .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "REJECTED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFEF4444)
                        )
                    }
                }

                // Truck Info
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = truckNumber,
                        onValueChange = { truckNumber = it },
                        label = { Text("Truck Number") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color(0xFFE2E8F0),
                            focusedBorderColor = Color(0xFFEF4444),
                            unfocusedBorderColor = Color(0xFF334155)
                        )
                    )
                    OutlinedTextField(
                        value = buyerOrCompany,
                        onValueChange = { buyerOrCompany = it },
                        label = { Text("Rejecting Buyer / Mill") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color(0xFFE2E8F0),
                            focusedBorderColor = Color(0xFFEF4444),
                            unfocusedBorderColor = Color(0xFF334155)
                        )
                    )
                }

                OutlinedTextField(
                    value = rejectionReason,
                    onValueChange = { rejectionReason = it },
                    label = { Text("Rejection Reason / Quality Failure") },
                    placeholder = { Text("e.g. Moisture > 14.5%, Fungus, High Foreign Matter") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color(0xFFE2E8F0),
                        focusedBorderColor = Color(0xFFEF4444),
                        unfocusedBorderColor = Color(0xFF334155)
                    )
                )

                // Manual Loss Entry Inputs
                Text(
                    text = "MANUAL LOSS & PENALTY FIELDS (₹)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = Color(0xFF94A3B8)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    LossInputField(
                        label = "1) Transport Loss (₹)",
                        value = transportLoss,
                        onValueChange = { transportLoss = it },
                        modifier = Modifier.weight(1f)
                    )
                    LossInputField(
                        label = "2) Demurrage / Penalty (₹)",
                        value = penaltiesDemurrage,
                        onValueChange = { penaltiesDemurrage = it },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    LossInputField(
                        label = "3) Quality Deduction (₹)",
                        value = qualitySalvageDeduction,
                        onValueChange = { qualitySalvageDeduction = it },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = salvageAction,
                        onValueChange = { salvageAction = it },
                        label = { Text("Salvage / Reroute Action", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color(0xFFE2E8F0),
                            focusedBorderColor = Color(0xFFF59E0B),
                            unfocusedBorderColor = Color(0xFF334155)
                        )
                    )
                }

                // Total Loss Calculation Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Total Net Rejection Loss",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = Color(0xFFFCA5A5)
                            )
                            Text(
                                text = "Will be deducted directly from P&L operations",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = Color(0xFF94A3B8)
                            )
                        }
                        Text(
                            text = "- ${inrFormat.format(totalRejectionLoss)}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            ),
                            color = Color(0xFFEF4444)
                        )
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF94A3B8)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            onRecordRejection(
                                truckNumber,
                                buyerOrCompany,
                                activeCrop,
                                dispatchedWeightKg,
                                rejectionReason,
                                transportLoss,
                                penaltiesDemurrage,
                                qualitySalvageDeduction,
                                salvageAction,
                                notes
                            )
                        },
                        modifier = Modifier.weight(2f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEF4444),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Record Rejection Loss",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LossInputField(
    label: String,
    value: Double,
    onValueChange: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var textValue by remember(value) { mutableStateOf(value.toInt().toString()) }

    OutlinedTextField(
        value = textValue,
        onValueChange = {
            textValue = it
            it.toDoubleOrNull()?.let(onValueChange)
        },
        label = { Text(label, fontSize = 11.sp) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = modifier,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color(0xFFE2E8F0),
            focusedBorderColor = Color(0xFFEF4444),
            unfocusedBorderColor = Color(0xFF334155),
            focusedLabelColor = Color(0xFFEF4444)
        )
    )
}
