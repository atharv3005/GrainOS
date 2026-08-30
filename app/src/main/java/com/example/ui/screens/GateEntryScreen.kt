package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.CropType
import com.example.data.model.GodownEntity
import com.example.data.model.ProcurementEntity
import com.example.data.model.ProcurementStatus
import com.example.data.model.QualityGrade
import com.example.ui.components.DarkGlassTextField
import com.example.ui.components.GrainGlassCard
import com.example.ui.components.MeshBackground
import com.example.ui.viewmodel.GrainWmsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Composable
fun GateEntryScreen(
    viewModel: GrainWmsViewModel,
    activeCrop: CropType,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val godowns by viewModel.allGodowns.collectAsState()
    val firmProfile by viewModel.firmProfile.collectAsState()

    var farmerName by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var village by remember { mutableStateOf("Dhule, MH") }
    var vehicleNumber by remember { mutableStateOf("") }
    
    // Selected Storage Facility (Godown/Silo)
    var selectedGodown by remember(godowns) { 
        mutableStateOf(godowns.firstOrNull()?.displayName ?: "Godown A (Main Silo)") 
    }

    // Moisture & Quality Testing
    var moistureString by remember { mutableStateOf(activeCrop.idealMoisture.toString()) }
    var bagWeightKg by remember { mutableStateOf(50.0) } // 50kg std, 60kg std, or 1.0 (bulk)

    // Weight Math
    var grossWeightString by remember { mutableStateOf("") }
    var tareWeightString by remember { mutableStateOf("") }
    
    val grossWeight = grossWeightString.toDoubleOrNull() ?: 0.0
    val tareWeight = tareWeightString.toDoubleOrNull() ?: 0.0
    val netWeight = (grossWeight - tareWeight).coerceAtLeast(0.0)
    val moistureValue = moistureString.toDoubleOrNull() ?: activeCrop.idealMoisture
    val bagCount = if (bagWeightKg > 0) (netWeight / bagWeightKg).toInt().coerceAtLeast(0) else 0

    // Quality Grade determination
    val calculatedGrade = when {
        moistureValue <= 12.0 -> QualityGrade.GRADE_A
        moistureValue <= 14.0 -> QualityGrade.GRADE_B
        moistureValue <= 17.0 -> QualityGrade.GRADE_C
        else -> QualityGrade.REJECTED
    }

    // Pricing & Taxes
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

    // Generated Inbound Slip Dialog State
    var generatedSlipProcurement by remember { mutableStateOf<ProcurementEntity?>(null) }
    var showGeneratedSlipDialog by remember { mutableStateOf(false) }

    MeshBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Gate Entry & Storage Intake", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("Weigh, test moisture, and store directly into facility", color = Color(0xFF94A3B8), fontSize = 12.sp)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(activeCrop.primaryColor.copy(alpha = 0.2f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(activeCrop.name, color = activeCrop.primaryColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            // 1. Farmer & Vehicle Info
            GrainGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = activeCrop.primaryColor, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Farmer & Logistics Information", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    DarkGlassTextField(value = farmerName, onValueChange = { farmerName = it }, label = "Farmer Name *", icon = Icons.Default.Person)
                    DarkGlassTextField(value = mobileNumber, onValueChange = { mobileNumber = it }, label = "Mobile Number *", icon = Icons.Default.Phone, keyboardType = KeyboardType.Phone)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            DarkGlassTextField(value = vehicleNumber, onValueChange = { vehicleNumber = it }, label = "Vehicle Number *", icon = Icons.Default.DirectionsCar)
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            DarkGlassTextField(value = village, onValueChange = { village = it }, label = "Village / Tehsil", icon = Icons.Default.LocationOn)
                        }
                    }
                }
            }

            // 2. Target Storage Facility Selector (Unloading Destination)
            GrainGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warehouse, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Select Target Storage Facility / Silo", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Text("Select where this inward grain lot will be unloaded and added to the database:", color = Color(0xFF94A3B8), fontSize = 12.sp)

                    // Facility Cards Grid / List
                    val availableFacilities = if (godowns.isNotEmpty()) godowns else listOf(
                        GodownEntity(godownId = "GODOWN_A", displayName = "Godown A (Main Silo)", capacityMt = 2000.0, currentStockMt = 240.0, activeCrop = activeCrop.name, averageMoisture = 12.0),
                        GodownEntity(godownId = "GODOWN_B", displayName = "Godown B (Secondary Silo)", capacityMt = 1500.0, currentStockMt = 120.0, activeCrop = activeCrop.name, averageMoisture = 12.5),
                        GodownEntity(godownId = "GODOWN_C", displayName = "Godown C (Paved Shed)", capacityMt = 1000.0, currentStockMt = 0.0, activeCrop = activeCrop.name, averageMoisture = 13.0)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        availableFacilities.forEach { g ->
                            val isSelected = selectedGodown == g.displayName || selectedGodown == g.godownId
                            val freeSpace = (g.capacityMt - g.currentStockMt).coerceAtLeast(0.0)
                            
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Color(0xFF38BDF8).copy(alpha = 0.18f) else Color(0xFF1E293B)
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    if (isSelected) 1.5.dp else 1.dp,
                                    if (isSelected) Color(0xFF38BDF8) else Color(0xFF334155)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedGodown = g.displayName }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { selectedGodown = g.displayName },
                                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF38BDF8))
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Column {
                                            Text(g.displayName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text("Stock: ${g.currentStockMt.toInt()}/${g.capacityMt.toInt()} MT • Avg Moisture: ${g.averageMoisture}%", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                        }
                                    }
                                    
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (freeSpace > 50) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFFF59E0B).copy(alpha = 0.2f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("${freeSpace.toInt()} MT Free", color = if (freeSpace > 50) Color(0xFF34D399) else Color(0xFFFBBF24), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. Moisture & Grain Quality Testing
            GrainGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Opacity, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Moisture & Quality Testing", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        // Quality Grade Pill
                        val gradeColor = when (calculatedGrade) {
                            QualityGrade.GRADE_A -> Color(0xFF10B981)
                            QualityGrade.GRADE_B -> Color(0xFFF59E0B)
                            QualityGrade.GRADE_C -> Color(0xFFEAB308)
                            QualityGrade.REJECTED -> Color(0xFFEF4444)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(gradeColor.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(calculatedGrade.name.replace("_", " "), color = gradeColor, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.weight(1f)) {
                            DarkGlassTextField(
                                value = moistureString,
                                onValueChange = { moistureString = it },
                                label = "Moisture % (Std: ${activeCrop.idealMoisture}%)",
                                icon = Icons.Default.Opacity,
                                keyboardType = KeyboardType.Decimal
                            )
                        }
                        Button(
                            onClick = {
                                val simulatedProbe = (11.8 + (Math.random() * 1.8))
                                moistureString = String.format(Locale.US, "%.1f", simulatedProbe)
                            },
                            modifier = Modifier.height(52.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Sensors, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Probe IoT", fontSize = 12.sp, color = Color.White)
                        }
                    }

                    // Quick Moisture Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(11.5, 12.0, 12.5, 13.5, 14.5).forEach { mVal ->
                            val isSelected = moistureString == mVal.toString()
                            FilterChip(
                                selected = isSelected,
                                onClick = { moistureString = mVal.toString() },
                                label = { Text("$mVal%", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF38BDF8).copy(alpha = 0.3f),
                                    selectedLabelColor = Color(0xFF38BDF8)
                                )
                            )
                        }
                    }

                    Divider(color = Color(0xFF334155), thickness = 0.5.dp)

                    // Bag Packing Selection
                    Text("Bag Packing Standard", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(Pair(50.0, "50 kg Standard"), Pair(60.0, "60 kg Jute"), Pair(1.0, "Loose Bulk")).forEach { (weight, label) ->
                            val isSelected = bagWeightKg == weight
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) activeCrop.primaryColor.copy(alpha = 0.25f) else Color(0xFF1E293B))
                                    .border(1.dp, if (isSelected) activeCrop.primaryColor else Color(0xFF334155), RoundedCornerShape(8.dp))
                                    .clickable { bagWeightKg = weight }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(label, color = if (isSelected) activeCrop.primaryColor else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    if (bagWeightKg > 1.0 && netWeight > 0) {
                        Text("Calculated: $bagCount Bags @ ${bagWeightKg.toInt()}kg each", color = activeCrop.primaryColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            // 4. Weight Math
            GrainGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Scale, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Weighbridge Gross & Tare (Dual Entry)", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            DarkGlassTextField(value = grossWeightString, onValueChange = { grossWeightString = it }, label = "Gross (Kg) *", icon = Icons.Default.Scale, keyboardType = KeyboardType.Decimal)
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            DarkGlassTextField(value = tareWeightString, onValueChange = { tareWeightString = it }, label = "Tare (Kg) *", icon = Icons.Default.Scale, keyboardType = KeyboardType.Decimal)
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { 
                                targetWeightType = "GROSS"
                                showWeighbridgeDialog = true 
                            },
                            modifier = Modifier.weight(1f).height(46.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Sensors, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF10B981))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Live Gross WB", fontSize = 12.sp)
                        }
                        Button(
                            onClick = { 
                                targetWeightType = "TARE"
                                showWeighbridgeDialog = true 
                            },
                            modifier = Modifier.weight(1f).height(46.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Sensors, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF10B981))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Live Tare WB", fontSize = 12.sp)
                        }
                    }
                    // Live Green Calculation Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF10B981).copy(alpha = 0.18f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("CALCULATED NET GRAIN WEIGHT", color = Color(0xFF34D399), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            Text("${String.format(Locale.US, "%,.2f", netWeight)} Kg (${String.format(Locale.US, "%.3f", netWeight / 1000.0)} MT)", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                            Text("Will be unloaded & added to $selectedGodown database", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        }
                    }
                }
            }

            // 5. APMC Tax & Pricing
            GrainGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Pricing & Mandi Charges", color = Color.White, fontWeight = FontWeight.Bold)
                    DarkGlassTextField(value = ratePerQuintalString, onValueChange = { ratePerQuintalString = it }, label = "Negotiated Rate / Quintal (₹)", icon = Icons.Default.AttachMoney, keyboardType = KeyboardType.Decimal)
                    
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text("Apply APMC Mandi Charges (1.5%)", color = Color.White, modifier = Modifier.weight(1f), fontSize = 14.sp)
                        Switch(
                            checked = applyApmc,
                            onCheckedChange = { applyApmc = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFFF9800), checkedTrackColor = Color(0xFFFF9800).copy(alpha = 0.5f))
                        )
                    }
                    if (applyApmc) {
                        Text("Market Fee (1.0%): -₹${String.format(Locale.US, "%.2f", mandiMarketFee)}", color = Color(0xFFFF9800), fontSize = 13.sp)
                        Text("Supervisory (0.5%): -₹${String.format(Locale.US, "%.2f", mandiSupervisoryCharge)}", color = Color(0xFFFF9800), fontSize = 13.sp)
                    }
                }
            }

            // 6. Split Payment Engine
            GrainGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Payment Settlement", color = Color.White, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Gross Bill Amount:", color = Color(0xFF94A3B8), fontSize = 14.sp)
                        Text("₹${String.format(Locale.US, "%,.2f", totalBill)}", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    
                    DarkGlassTextField(value = advanceDeductionString, onValueChange = { advanceDeductionString = it }, label = "Advance Cash Deduction (₹)", icon = Icons.Default.MoneyOff, keyboardType = KeyboardType.Decimal)
                    DarkGlassTextField(value = pdcAmountString, onValueChange = { pdcAmountString = it }, label = "PDC Cheque Amount (₹)", icon = Icons.Default.Event, keyboardType = KeyboardType.Decimal)
                    
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Remaining Instant Balance Payable", color = Color(0xFF94A3B8), fontSize = 12.sp)
                            Text("₹${String.format(Locale.US, "%,.2f", remainingBalance)}", color = Color(0xFFFF9800), fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 7. Save Entry, Store in Facility Database & Generate Slip
            Button(
                onClick = {
                    if (farmerName.isBlank()) {
                        farmerName = "Walk-in Farmer"
                    }
                    if (vehicleNumber.isBlank()) {
                        vehicleNumber = "MH 15 TR 9901"
                    }

                    val token = "GP-${UUID.randomUUID().toString().take(6).uppercase()}"
                    val procurement = ProcurementEntity(
                        tokenNo = token,
                        farmerName = farmerName,
                        mobileNumber = mobileNumber.ifBlank { "+91 98220 00000" },
                        vehicleNumber = vehicleNumber,
                        village = village,
                        cropType = activeCrop.name,
                        qualityGrade = calculatedGrade.name,
                        grossWeightKg = if (grossWeight > 0) grossWeight else (netWeight + 3200.0),
                        tareWeightKg = if (tareWeight > 0) tareWeight else 3200.0,
                        netWeightKg = if (netWeight > 0) netWeight else 4500.0,
                        bagCount = bagCount.coerceAtLeast(1),
                        bagWeightKg = bagWeightKg,
                        moisturePercentage = moistureValue,
                        ratePerQuintal = if (ratePerQuintal > 0) ratePerQuintal else activeCrop.standardMsp,
                        grossBillAmount = grossPayable,
                        applyMandiCess = applyApmc,
                        mandiMarketFee = mandiMarketFee,
                        mandiSupervisoryCharge = mandiSupervisoryCharge,
                        totalMandiCess = totalMandiCess,
                        totalAmount = totalBill,
                        godownAssigned = selectedGodown,
                        status = ProcurementStatus.COMPLETED.name,
                        paymentStatus = if (remainingBalance <= 0) "PAID" else "PENDING",
                        paymentMode = if (pdcAmount > 0) "CHEQUE" else "CASH",
                        isPdc = pdcAmount > 0,
                        createdAt = System.currentTimeMillis(),
                        completedTimestamp = System.currentTimeMillis()
                    )

                    viewModel.submitAdvancedGateEntry(
                        procurement = procurement,
                        targetGodownIdOrName = selectedGodown
                    ) { savedProc ->
                        generatedSlipProcurement = savedProc
                        showGeneratedSlipDialog = true
                    }
                    
                    // Reset inputs
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
                Text("✓ Save Entry & Generate Slip", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(100.dp))
        }
    }

    // Weighbridge Capture Dialog
    if (showWeighbridgeDialog) {
        com.example.ui.components.WeighbridgeLiveDialog(
            targetType = targetWeightType,
            onDismiss = { showWeighbridgeDialog = false },
            onWeightCaptured = { weight ->
                if (targetWeightType == "GROSS") {
                    grossWeightString = String.format(Locale.US, "%.2f", weight)
                } else {
                    tareWeightString = String.format(Locale.US, "%.2f", weight)
                }
            }
        )
    }

    // Generated Gate Entry Slip Dialog Preview
    if (showGeneratedSlipDialog && generatedSlipProcurement != null) {
        val slip = generatedSlipProcurement!!
        GateEntrySlipDialog(
            procurement = slip,
            firmProfile = firmProfile,
            onDismiss = { showGeneratedSlipDialog = false },
            onShareWhatsApp = {
                showGeneratedSlipDialog = false
                viewModel.openWhatsAppReceipt(slip, false)
            },
            onViewPdf = {
                showGeneratedSlipDialog = false
                viewModel.openPdfReceipt(slip)
            }
        )
    }
}

@Composable
fun GateEntrySlipDialog(
    procurement: ProcurementEntity,
    firmProfile: com.example.data.model.FirmProfile,
    onDismiss: () -> Unit,
    onShareWhatsApp: () -> Unit,
    onViewPdf: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFF9800)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(firmProfile.firmName, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                        Text("INBOUND GATE ENTRY & STORAGE SLIP", color = Color(0xFFFF9800), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF10B981).copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("STORED", color = Color(0xFF34D399), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }

                Divider(color = Color(0xFF334155))

                // Storage Facility Destination Badge
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF38BDF8).copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warehouse, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("UNLOADED & STORED IN FACILITY", color = Color(0xFF38BDF8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(procurement.godownAssigned, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }

                // Key Slip Metadata
                SlipRow("Token / Slip No:", procurement.tokenNo, Color.White, isBold = true)
                SlipRow("Date & Time:", dateFormat.format(Date(if (procurement.completedTimestamp > 0) procurement.completedTimestamp else System.currentTimeMillis())), Color(0xFF94A3B8))
                SlipRow("Farmer Name:", procurement.farmerName, Color.White, isBold = true)
                SlipRow("Vehicle Number:", procurement.vehicleNumber, Color.White)
                SlipRow("Crop & Grade:", "${procurement.cropType} (${procurement.qualityGrade.replace("_", " ")})", Color(0xFFFF9800), isBold = true)

                Divider(color = Color(0xFF334155))

                // Grain Weights & Moisture
                Text("GRAIN PARAMETERS & WEIGHTS", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                SlipRow("Gross Weight:", "${String.format(Locale.US, "%,.2f", procurement.grossWeightKg)} kg", Color.White)
                SlipRow("Tare Weight:", "${String.format(Locale.US, "%,.2f", procurement.tareWeightKg)} kg", Color.White)
                SlipRow("Net Grain Weight:", "${String.format(Locale.US, "%,.2f", procurement.netWeightKg)} kg (${String.format(Locale.US, "%.3f", procurement.netWeightKg / 1000.0)} MT)", Color(0xFF34D399), isBold = true)
                SlipRow("Moisture Tested:", "${procurement.moisturePercentage}%", Color(0xFF38BDF8), isBold = true)
                SlipRow("Bags Count:", "${procurement.bagCount} Bags", Color.White)
                SlipRow("Rate / Quintal:", "₹${procurement.ratePerQuintal}", Color.White)
                SlipRow("Total Bill Amount:", "₹${String.format(Locale.US, "%,.2f", procurement.totalAmount)}", Color(0xFFFF9800), isBold = true)

                Divider(color = Color(0xFF334155))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onShareWhatsApp,
                        modifier = Modifier.weight(1f).height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onViewPdf,
                        modifier = Modifier.weight(1f).height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PDF Slip", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Done / New Gate Entry", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun SlipRow(label: String, value: String, valueColor: Color = Color.White, isBold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color(0xFF94A3B8), fontSize = 12.sp)
        Text(value, color = valueColor, fontSize = 13.sp, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)
    }
}
