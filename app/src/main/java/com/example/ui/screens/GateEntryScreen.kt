package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CropType
import com.example.data.model.ProcurementEntity
import com.example.data.model.ProcurementStatus
import com.example.ui.components.DarkGlassTextField
import com.example.ui.components.GrainGlassCard
import com.example.ui.components.MeshBackground
import com.example.ui.viewmodel.GrainWmsViewModel
import java.util.UUID

@Composable
fun GateEntryScreen(
    viewModel: GrainWmsViewModel,
    activeCrop: CropType,
    modifier: Modifier = Modifier
) {
    var farmerName by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var vehicleNumber by remember { mutableStateOf("") }
    var godownAssigned by remember { mutableStateOf("Godown A") }

    // Weight Math
    var grossWeightString by remember { mutableStateOf("") }
    var tareWeightString by remember { mutableStateOf("") }
    
    val grossWeight = grossWeightString.toDoubleOrNull() ?: 0.0
    val tareWeight = tareWeightString.toDoubleOrNull() ?: 0.0
    val netWeight = (grossWeight - tareWeight).coerceAtLeast(0.0)

    // APMC Tax Toggle
    var showWeighbridgeDialog by remember { mutableStateOf(false) }
    var targetWeightType by remember { mutableStateOf("GROSS") }
    var ratePerQuintalString by remember { mutableStateOf(activeCrop.standardMsp.toString()) }
    var applyApmc by remember { mutableStateOf(false) }
    
    val ratePerQuintal = ratePerQuintalString.toDoubleOrNull() ?: 0.0
    val grossPayable = (netWeight / 100.0) * ratePerQuintal
    
    val mandiMarketFee = if (applyApmc) grossPayable * 0.01 else 0.0
    val mandiSupervisoryCharge = if (applyApmc) grossPayable * 0.005 else 0.0
    val totalMandiCess = mandiMarketFee + mandiSupervisoryCharge
    
    val totalBill = grossPayable - totalMandiCess

    // Split Payment Engine
    var advanceDeductionString by remember { mutableStateOf("") }
    var pdcAmountString by remember { mutableStateOf("") }
    
    val advanceDeduction = advanceDeductionString.toDoubleOrNull() ?: 0.0
    val pdcAmount = pdcAmountString.toDoubleOrNull() ?: 0.0
    val remainingBalance = (totalBill - advanceDeduction - pdcAmount).coerceAtLeast(0.0)

    MeshBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Advanced Gate Entry", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)

            // General Info
            GrainGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Farmer & Vehicle", color = Color.White, fontWeight = FontWeight.Bold)
                    DarkGlassTextField(value = farmerName, onValueChange = { farmerName = it }, label = "Farmer Name", icon = Icons.Default.Person)
                    DarkGlassTextField(value = mobileNumber, onValueChange = { mobileNumber = it }, label = "Mobile Number", icon = Icons.Default.Phone, keyboardType = KeyboardType.Phone)
                    DarkGlassTextField(value = vehicleNumber, onValueChange = { vehicleNumber = it }, label = "Vehicle Number", icon = Icons.Default.DirectionsCar)
                }
            }

            // Weight Math
            GrainGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Weight Math", color = Color.White, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            DarkGlassTextField(value = grossWeightString, onValueChange = { grossWeightString = it }, label = "Gross (Kg)", icon = Icons.Default.Scale, keyboardType = KeyboardType.Decimal)
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            DarkGlassTextField(value = tareWeightString, onValueChange = { tareWeightString = it }, label = "Tare (Kg)", icon = Icons.Default.Scale, keyboardType = KeyboardType.Decimal)
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { 
                                targetWeightType = "GROSS"
                                showWeighbridgeDialog = true 
                            },
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Sensors, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Live Gross", fontSize = 12.sp)
                        }
                        Button(
                            onClick = { 
                                targetWeightType = "TARE"
                                showWeighbridgeDialog = true 
                            },
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Sensors, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Live Tare", fontSize = 12.sp)
                        }
                    }
                    // Live Green Calculation Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50).copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("CALCULATED NET GRAIN WEIGHT", color = Color(0xFF4CAF50), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("${String.format("%.2f", netWeight)} Kg", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // APMC Tax Toggle
            GrainGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Pricing & Tax", color = Color.White, fontWeight = FontWeight.Bold)
                    DarkGlassTextField(value = ratePerQuintalString, onValueChange = { ratePerQuintalString = it }, label = "Negotiated Rate / Quintal (₹)", icon = Icons.Default.AttachMoney, keyboardType = KeyboardType.Decimal)
                    
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text("Apply APMC Mandi Charges (1.5%)", color = Color.White, modifier = Modifier.weight(1f))
                        Switch(
                            checked = applyApmc,
                            onCheckedChange = { applyApmc = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFFF9800), checkedTrackColor = Color(0xFFFF9800).copy(alpha = 0.5f))
                        )
                    }
                    if (applyApmc) {
                        Text("Market Fee (1.0%): -₹${String.format("%.2f", mandiMarketFee)}", color = Color(0xFFFF9800), fontSize = 14.sp)
                        Text("Supervisory (0.5%): -₹${String.format("%.2f", mandiSupervisoryCharge)}", color = Color(0xFFFF9800), fontSize = 14.sp)
                    }
                }
            }

            // Split Payment Engine
            GrainGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Payment Settlement", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Total Bill: ₹${String.format("%.2f", totalBill)}", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    
                    DarkGlassTextField(value = advanceDeductionString, onValueChange = { advanceDeductionString = it }, label = "Advance Deduction (₹)", icon = Icons.Default.MoneyOff, keyboardType = KeyboardType.Decimal)
                    DarkGlassTextField(value = pdcAmountString, onValueChange = { pdcAmountString = it }, label = "PDC Amount (₹)", icon = Icons.Default.Event, keyboardType = KeyboardType.Decimal)
                    
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Remaining Balance (Cash/NEFT)", color = Color(0xFF94A3B8), fontSize = 12.sp)
                            Text("₹${String.format("%.2f", remainingBalance)}", color = Color(0xFFFF9800), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Generate Inbound Slip
            Button(
                onClick = {
                    val procurement = ProcurementEntity(
                        tokenNo = "GP-${UUID.randomUUID().toString().take(6).uppercase()}",
                        farmerName = farmerName,
                        mobileNumber = mobileNumber,
                        vehicleNumber = vehicleNumber,
                        village = "",
                        cropType = activeCrop.name,
                        grossWeightKg = grossWeight,
                        tareWeightKg = tareWeight,
                        netWeightKg = netWeight,
                        ratePerQuintal = ratePerQuintal,
                        grossBillAmount = grossPayable,
                        applyMandiCess = applyApmc,
                        mandiMarketFee = mandiMarketFee,
                        mandiSupervisoryCharge = mandiSupervisoryCharge,
                        totalMandiCess = totalMandiCess,
                        totalAmount = totalBill,
                        godownAssigned = godownAssigned,
                        status = ProcurementStatus.COMPLETED.name,
                        paymentStatus = if (remainingBalance <= 0) "PAID" else "PENDING",
                        paymentMode = if (pdcAmount > 0) "CHEQUE" else "CASH",
                        isPdc = pdcAmount > 0,
                        createdAt = System.currentTimeMillis(),
                        completedTimestamp = System.currentTimeMillis()
                    )
                    viewModel.submitAdvancedGateEntry(procurement)
                    
                    // Reset fields
                    farmerName = ""
                    mobileNumber = ""
                    vehicleNumber = ""
                    grossWeightString = ""
                    tareWeightString = ""
                    advanceDeductionString = ""
                    pdcAmountString = ""
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF9800),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = "Save", modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("\u2713 Save Entry & Generate Slip", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(100.dp))
        }
    }

    if (showWeighbridgeDialog) {
        com.example.ui.components.WeighbridgeLiveDialog(
            targetType = targetWeightType,
            onDismiss = { showWeighbridgeDialog = false },
            onWeightCaptured = { weight ->
                if (targetWeightType == "GROSS") {
                    grossWeightString = String.format("%.2f", weight)
                } else {
                    tareWeightString = String.format("%.2f", weight)
                }
            }
        )
    }
}
