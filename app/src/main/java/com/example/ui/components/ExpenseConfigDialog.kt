package com.example.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Payments
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.CropType
import com.example.data.model.FirmProfile

@Composable
fun ExpenseConfigDialog(
    firmProfile: FirmProfile,
    activeCrop: CropType,
    onSaveExpenses: (labor: Double, bag: Double, transport: Double, brokerage: Double) -> Unit,
    onDismiss: () -> Unit
) {
    var labor by remember { mutableDoubleStateOf(firmProfile.laborPerQuintal) }
    var bag by remember { mutableDoubleStateOf(firmProfile.bagCostPerQuintal) }
    var transport by remember { mutableDoubleStateOf(firmProfile.transportPerQuintal) }
    var brokerage by remember { mutableDoubleStateOf(firmProfile.brokeragePerQuintal) }

    val totalOverhead = labor + bag + transport + brokerage

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(22.dp))
                .border(1.dp, activeCrop.primaryColor.copy(alpha = 0.5f), RoundedCornerShape(22.dp)),
            color = Color(0xFF0F172A)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(activeCrop.primaryColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null,
                            tint = activeCrop.primaryColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Per-Quintal Expense Ledger",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "Universal operational variables applied to all trades",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                // Expense Fields
                ExpenseItemCard(
                    icon = Icons.Default.Engineering,
                    title = "Labor Charges (Hamali)",
                    subtitle = "Unloading, stacking & grading labor",
                    currentValue = labor,
                    onValueChange = { labor = it },
                    accentColor = Color(0xFFF59E0B)
                )

                ExpenseItemCard(
                    icon = Icons.Default.Inventory,
                    title = "Bag / Gunny Packaging Cost",
                    subtitle = "50kg Jute/HDPE bag unit cost",
                    currentValue = bag,
                    onValueChange = { bag = it },
                    accentColor = Color(0xFF38BDF8)
                )

                ExpenseItemCard(
                    icon = Icons.Default.DirectionsBus,
                    title = "Transport & Freight Charges",
                    subtitle = "Average yard-to-depot logistics",
                    currentValue = transport,
                    onValueChange = { transport = it },
                    accentColor = Color(0xFF10B981)
                )

                ExpenseItemCard(
                    icon = Icons.Default.AccountBalance,
                    title = "Brokerage & Mandi Cess",
                    subtitle = "APMC market cess & commission",
                    currentValue = brokerage,
                    onValueChange = { brokerage = it },
                    accentColor = Color(0xFFEC4899)
                )

                // Total Overhead Summary Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
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
                                text = "Total Operational Overhead",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF94A3B8)
                            )
                            Text(
                                text = "Applied automatically to 1 MT = 10 Qtl",
                                fontSize = 10.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                        Text(
                            text = "₹${totalOverhead.toInt()} / Qtl",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = activeCrop.primaryColor
                        )
                    }
                }

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(46.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF94A3B8)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            onSaveExpenses(labor, bag, transport, brokerage)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1.5f).height(46.dp).testTag("btn_save_expenses"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = activeCrop.primaryColor,
                            contentColor = Color.Black
                        )
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Expenses", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpenseItemCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    currentValue: Double,
    onValueChange: (Double) -> Unit,
    accentColor: Color
) {
    var textValue by remember(currentValue) { mutableStateOf(currentValue.toInt().toString()) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.7f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(text = subtitle, fontSize = 10.sp, color = Color(0xFF94A3B8))
                }
            }

            OutlinedTextField(
                value = textValue,
                onValueChange = {
                    textValue = it
                    it.toDoubleOrNull()?.let(onValueChange)
                },
                prefix = { Text("₹", fontSize = 12.sp, color = accentColor) },
                suffix = { Text("/q", fontSize = 10.sp, color = Color(0xFF64748B)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.width(105.dp).height(50.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color(0xFFE2E8F0),
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = Color(0xFF334155)
                )
            )
        }
    }
}
