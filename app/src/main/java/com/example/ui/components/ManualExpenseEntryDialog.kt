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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Receipt
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
import androidx.compose.ui.platform.testTag
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
 * Manual Expense Entry Dialog.
 * Captures live fluctuating transaction costs:
 * a) Labor (₹)
 * b) Bags (Calculated & entered PER TRUCK LOADING, not per quintal)
 * c) Transport / Freight (₹)
 * d) Miscellaneous / Handling Expenses (₹)
 */
@Composable
fun ManualExpenseEntryDialog(
    activeCrop: CropType,
    initialTruckRef: String = "",
    onSaveExpense: (
        truckOrBatchRef: String,
        cropType: CropType,
        laborCost: Double,
        bagsCostPerTruck: Double,
        transportCost: Double,
        miscCost: Double,
        paidToOrParty: String,
        notes: String
    ) -> Unit,
    onDismiss: () -> Unit
) {
    var truckRef by remember { mutableStateOf(initialTruckRef) }
    var selectedCrop by remember { mutableStateOf(activeCrop) }
    var laborCost by remember { mutableDoubleStateOf(1500.0) }
    var bagsCostPerTruck by remember { mutableDoubleStateOf(3200.0) } // Per truck loading
    var transportCost by remember { mutableDoubleStateOf(4200.0) }
    var miscCost by remember { mutableDoubleStateOf(500.0) }
    var paidTo by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val animatedAccent by animateColorAsState(
        targetValue = selectedCrop.primaryColor,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "crop_accent_expense"
    )

    val inrFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    val scrollState = rememberScrollState()

    val totalExpense = laborCost + bagsCostPerTruck + transportCost + miscCost

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .border(1.5.dp, animatedAccent.copy(alpha = 0.6f), RoundedCornerShape(24.dp)),
            color = Color(0xFF0F172A),
            tonalElevation = 10.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
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
                                .background(animatedAccent.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CurrencyRupee,
                                contentDescription = null,
                                tint = animatedAccent,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Manual Expense Entry",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 17.sp
                                ),
                                color = Color.White
                            )
                            Text(
                                text = "Live Transaction & Loading Costs",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    // Crop Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(animatedAccent.copy(alpha = 0.15f))
                            .border(1.dp, animatedAccent.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = selectedCrop.displayName.split(" ").first(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = animatedAccent
                        )
                    }
                }

                // Reference Details
                OutlinedTextField(
                    value = truckRef,
                    onValueChange = { truckRef = it },
                    label = { Text("Truck / Vehicle / Lot Reference") },
                    placeholder = { Text("e.g. MH 18 Q 4589 (Lot #1082)") },
                    leadingIcon = {
                        Icon(Icons.Default.LocalShipping, contentDescription = null, tint = animatedAccent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color(0xFFE2E8F0),
                        focusedBorderColor = animatedAccent,
                        unfocusedBorderColor = Color(0xFF334155)
                    )
                )

                OutlinedTextField(
                    value = paidTo,
                    onValueChange = { paidTo = it },
                    label = { Text("Paid To / Hamal Mandali / Transporter") },
                    placeholder = { Text("e.g. Kisan Hamal Group / Shri Logistics") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color(0xFFE2E8F0),
                        focusedBorderColor = animatedAccent,
                        unfocusedBorderColor = Color(0xFF334155)
                    )
                )

                // 4 Core Expense Inputs
                Text(
                    text = "EXPENSE BREAKDOWN (MANUAL ENTRY)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = Color(0xFF94A3B8)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ManualExpenseInputField(
                        label = "a) Labor Cost (₹)",
                        value = laborCost,
                        onValueChange = { laborCost = it },
                        modifier = Modifier.weight(1f),
                        accentColor = animatedAccent
                    )
                    ManualExpenseInputField(
                        label = "b) Bags (₹ / Truck)",
                        subtitle = "Per truck loading",
                        value = bagsCostPerTruck,
                        onValueChange = { bagsCostPerTruck = it },
                        modifier = Modifier.weight(1f),
                        accentColor = Color(0xFF38BDF8)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ManualExpenseInputField(
                        label = "c) Transport / Freight (₹)",
                        value = transportCost,
                        onValueChange = { transportCost = it },
                        modifier = Modifier.weight(1f),
                        accentColor = Color(0xFFF59E0B)
                    )
                    ManualExpenseInputField(
                        label = "d) Misc / Handling (₹)",
                        value = miscCost,
                        onValueChange = { miscCost = it },
                        modifier = Modifier.weight(1f),
                        accentColor = Color(0xFFA855F7)
                    )
                }

                // Total Summary Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                    border = androidx.compose.foundation.BorderStroke(1.dp, animatedAccent.copy(alpha = 0.3f))
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
                                text = "Total Out-of-Pocket Expense",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = Color(0xFF94A3B8)
                            )
                            Text(
                                text = "Labor + Bags/Truck + Freight + Misc",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = Color(0xFF64748B)
                            )
                        }
                        Text(
                            text = inrFormat.format(totalExpense),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            ),
                            color = animatedAccent
                        )
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Receipt Voucher No.") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color(0xFFE2E8F0),
                        focusedBorderColor = animatedAccent,
                        unfocusedBorderColor = Color(0xFF334155)
                    )
                )

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
                            onSaveExpense(
                                truckRef.ifEmpty { "Truck Ref #${(1000..9999).random()}" },
                                selectedCrop,
                                laborCost,
                                bagsCostPerTruck,
                                transportCost,
                                miscCost,
                                paidTo,
                                notes
                            )
                        },
                        modifier = Modifier.weight(2f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = animatedAccent,
                            contentColor = Color.Black
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Save Expense",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ManualExpenseInputField(
    label: String,
    subtitle: String? = null,
    value: Double,
    onValueChange: (Double) -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    var textValue by remember(value) { mutableStateOf(value.toInt().toString()) }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = textValue,
            onValueChange = {
                textValue = it
                it.toDoubleOrNull()?.let(onValueChange)
            },
            label = { Text(label, fontSize = 11.sp) },
            supportingText = subtitle?.let { { Text(it, fontSize = 9.sp, color = Color(0xFF38BDF8)) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color(0xFFE2E8F0),
                focusedBorderColor = accentColor,
                unfocusedBorderColor = Color(0xFF334155),
                focusedLabelColor = accentColor
            )
        )
    }
}
