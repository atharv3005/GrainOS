package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.ProcurementEntity

@Composable
fun EditProcurementDialog(
    procurement: ProcurementEntity,
    onDismiss: () -> Unit,
    onConfirm: (ProcurementEntity) -> Unit
) {
    var farmerName by remember { mutableStateOf(procurement.farmerName) }
    var vehicleNumber by remember { mutableStateOf(procurement.vehicleNumber) }
    var grossWeight by remember { mutableStateOf(procurement.grossWeightKg.toString()) }
    var tareWeight by remember { mutableStateOf(procurement.tareWeightKg.toString()) }
    var moisture by remember { mutableStateOf(procurement.moisturePercentage.toString()) }
    var rate by remember { mutableStateOf(procurement.ratePerQuintal.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Procurement Record") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = farmerName,
                    onValueChange = { farmerName = it },
                    label = { Text("Farmer Name") }
                )
                OutlinedTextField(
                    value = vehicleNumber,
                    onValueChange = { vehicleNumber = it },
                    label = { Text("Vehicle Number") }
                )
                OutlinedTextField(
                    value = grossWeight,
                    onValueChange = { grossWeight = it },
                    label = { Text("Gross Weight (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = tareWeight,
                    onValueChange = { tareWeight = it },
                    label = { Text("Tare Weight (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = moisture,
                    onValueChange = { moisture = it },
                    label = { Text("Moisture %") },
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
                val gWt = grossWeight.toDoubleOrNull() ?: procurement.grossWeightKg
                val tWt = tareWeight.toDoubleOrNull() ?: procurement.tareWeightKg
                val nWt = (gWt - tWt).coerceAtLeast(0.0)
                val rateVal = rate.toDoubleOrNull() ?: procurement.ratePerQuintal
                val newGrossBill = (nWt / 100.0) * rateVal
                val newTotalAmount = newGrossBill - procurement.totalMandiCess - procurement.tdsDeductedAmount
                
                onConfirm(
                    procurement.copy(
                        farmerName = farmerName,
                        vehicleNumber = vehicleNumber,
                        grossWeightKg = gWt,
                        tareWeightKg = tWt,
                        netWeightKg = nWt,
                        moisturePercentage = moisture.toDoubleOrNull() ?: procurement.moisturePercentage,
                        ratePerQuintal = rateVal,
                        grossBillAmount = newGrossBill,
                        totalAmount = newTotalAmount.coerceAtLeast(0.0)
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
