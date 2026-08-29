package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.OutboundDispatchEntity

@Composable
fun SettleDispatchDialog(
    dispatch: OutboundDispatchEntity,
    onSettle: (Double, Double, Double, Double, Double, Double, String, Double) -> Unit,
    onDismiss: () -> Unit
) {
    var companyUnloadedWeightStr by remember { mutableStateOf("") }
    var qualityPenaltyStr by remember { mutableStateOf("0") }
    var freightCostStr by remember { mutableStateOf("0") }
    var laborCostStr by remember { mutableStateOf("0") }
    var bagCostStr by remember { mutableStateOf("0") }
    var miscCostStr by remember { mutableStateOf("0") }
    var brokerName by remember { mutableStateOf(dispatch.brokerName) }
    var brokerageRateStr by remember { mutableStateOf("0") }

    val companyUnloadedWeightKg = companyUnloadedWeightStr.toDoubleOrNull() ?: 0.0
    val loadedNetWeightKg = dispatch.netLoadedWeightKg
    val transitLossKg = (loadedNetWeightKg - companyUnloadedWeightKg).coerceAtLeast(0.0)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Settle Dispatch & Transit Loss",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = Color.White
                )

                Text(
                    text = "Gate Loaded Weight: ${loadedNetWeightKg} kg",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )

                OutlinedTextField(
                    value = companyUnloadedWeightStr,
                    onValueChange = { companyUnloadedWeightStr = it },
                    label = { Text("Final Company Unloaded Wt (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                if (companyUnloadedWeightKg > 0) {
                    Text(
                        text = "Transit Loss: ${transitLossKg} kg",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEF4444)
                    )
                }
                
                Text(
                    text = "This transit loss will be recorded against the trade's P&L, but will NOT deduct further from godown stock since it already left the gate.",
                    fontSize = 10.sp,
                    color = Color(0xFF94A3B8)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155))
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            if (companyUnloadedWeightKg > 0) {
                                onSettle(
                                    companyUnloadedWeightKg,
                                    qualityPenaltyStr.toDoubleOrNull() ?: 0.0,
                                    freightCostStr.toDoubleOrNull() ?: 0.0,
                                    laborCostStr.toDoubleOrNull() ?: 0.0,
                                    bagCostStr.toDoubleOrNull() ?: 0.0,
                                    miscCostStr.toDoubleOrNull() ?: 0.0,
                                    brokerName,
                                    brokerageRateStr.toDoubleOrNull() ?: 0.0
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        Text("Settle Loss")
                    }
                }
            }
        }
    }
}
