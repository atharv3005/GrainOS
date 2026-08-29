package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.OutboundDispatchEntity

@Composable
fun EditDispatchDialog(
    dispatch: OutboundDispatchEntity,
    onDismiss: () -> Unit,
    onConfirm: (OutboundDispatchEntity) -> Unit
) {
    var buyerName by remember { mutableStateOf(dispatch.buyerName) }
    var vehicleNumber by remember { mutableStateOf(dispatch.vehicleNumber) }
    var grossWeight by remember { mutableStateOf(dispatch.grossWeightKg.toString()) }
    var tareWeight by remember { mutableStateOf(dispatch.tareWeightKg.toString()) }
    var rate by remember { mutableStateOf(dispatch.ratePerQuintal.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Dispatch Record") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = buyerName,
                    onValueChange = { buyerName = it },
                    label = { Text("Buyer Name") }
                )
                OutlinedTextField(
                    value = vehicleNumber,
                    onValueChange = { vehicleNumber = it },
                    label = { Text("Vehicle Number") }
                )
                OutlinedTextField(
                    value = tareWeight,
                    onValueChange = { tareWeight = it },
                    label = { Text("Tare Weight (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = grossWeight,
                    onValueChange = { grossWeight = it },
                    label = { Text("Gross Weight (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = rate,
                    onValueChange = { rate = it },
                    label = { Text("Rate per Quintal (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val gWt = grossWeight.toDoubleOrNull() ?: dispatch.grossWeightKg
                val tWt = tareWeight.toDoubleOrNull() ?: dispatch.tareWeightKg
                val nWt = (gWt - tWt).coerceAtLeast(0.0)
                val rateVal = rate.toDoubleOrNull() ?: dispatch.ratePerQuintal
                val newInvoice = (nWt / 100.0) * rateVal
                
                onConfirm(
                    dispatch.copy(
                        buyerName = buyerName,
                        vehicleNumber = vehicleNumber,
                        grossWeightKg = gWt,
                        tareWeightKg = tWt,
                        netLoadedWeightKg = nWt,
                        ratePerQuintal = rateVal,
                        totalInvoiceAmount = newInvoice
                    )
                )
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
