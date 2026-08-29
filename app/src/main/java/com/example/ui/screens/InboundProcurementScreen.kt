package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CropType
import com.example.data.model.GodownEntity
import com.example.data.model.ProcurementEntity
import com.example.data.model.QualityGrade
import java.text.NumberFormat
import java.util.Locale

enum class WeightEntryMode {
    AUTO_SENSOR,
    MANUAL_INPUT
}

@Composable
fun InboundProcurementScreen(
    activeCrop: CropType,
    godowns: List<GodownEntity>,
    onRegisterFarmer: (String, String, String, String, CropType, (Long) -> Unit) -> Unit,
    onRecordGrossWeight: (Long, Double) -> Unit,
    onRecordMoisture: (Long, Double, String, Double?) -> Unit,
    onConfirmUnload: (Long) -> Unit,
    onRecordTareWeight: (Long, Double, Double?) -> Unit,
    onOpenWhatsApp: (ProcurementEntity, Boolean) -> Unit,
    onOpenPdf: (ProcurementEntity) -> Unit,
    onFinishPipeline: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeStep by remember { mutableIntStateOf(1) }
    var activeProcurementId by remember { mutableStateOf<Long?>(null) }

    // Step 1 State
    var farmerName by remember { mutableStateOf("Ramesh Patil") }
    var mobileNumber by remember { mutableStateOf("+91 98224 51230") }
    var village by remember { mutableStateOf("Pimpalner, Dhule") }
    var vehicleNumber by remember { mutableStateOf("MH 15 AB 1234") }
    var selectedCrop by remember { mutableStateOf(activeCrop) }

    // Step 2 Gross Weight State
    var grossWeightInput by remember { mutableStateOf("8420") }

    // Step 3 Moisture & Grading State + Manual Negotiated Rate
    var moistureSlider by remember { mutableStateOf(12.8f) }
    var selectedGodown by remember {
        mutableStateOf(godowns.firstOrNull()?.displayName ?: "Godown 1 (Main Silo)")
    }
    var manualFarmerRateInput by remember { mutableStateOf("") }

    // Step 5 Tare Weight State
    var tareWeightInput by remember { mutableStateOf("3260") }

    // Completed Summary State
    var completedProcurementSnapshot by remember { mutableStateOf<ProcurementEntity?>(null) }

    val scrollState = rememberScrollState()

    val animatedCropColor by animateColorAsState(
        targetValue = selectedCrop.primaryColor,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "crop_accent_inbound"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Stepper Progress Header
        PipelineProgressHeader(
            activeStep = activeStep,
            accentColor = animatedCropColor
        )

        // Step Content
        when (activeStep) {
            1 -> {
                Step1FarmerRegistration(
                    farmerName = farmerName,
                    onFarmerNameChange = { farmerName = it },
                    mobileNumber = mobileNumber,
                    onMobileNumberChange = { mobileNumber = it },
                    village = village,
                    onVillageChange = { village = it },
                    vehicleNumber = vehicleNumber,
                    onVehicleNumberChange = { vehicleNumber = it },
                    selectedCrop = selectedCrop,
                    onCropSelected = { selectedCrop = it },
                    onProceed = {
                        onRegisterFarmer(farmerName, mobileNumber, village, vehicleNumber, selectedCrop) { id ->
                            activeProcurementId = id
                            activeStep = 2
                        }
                    },
                    accentColor = animatedCropColor
                )
            }
            2 -> {
                Step2WeighbridgeGross(
                    farmerName = farmerName,
                    vehicleNumber = vehicleNumber,
                    crop = selectedCrop,
                    grossWeight = grossWeightInput,
                    onGrossWeightChange = { grossWeightInput = it },
                    onAutoCaptureIoT = { grossWeightInput = "8420" },
                    onProceed = {
                        activeProcurementId?.let { id ->
                            val grossKg = grossWeightInput.toDoubleOrNull() ?: 8420.0
                            onRecordGrossWeight(id, grossKg)
                            activeStep = 3
                        }
                    },
                    onBack = { activeStep = 1 },
                    accentColor = animatedCropColor
                )
            }
            3 -> {
                Step3MoistureAndGrading(
                    crop = selectedCrop,
                    moisture = moistureSlider,
                    onMoistureChange = { moistureSlider = it },
                    selectedGodown = selectedGodown,
                    godowns = godowns,
                    onGodownSelected = { selectedGodown = it },
                    manualFarmerRate = manualFarmerRateInput,
                    onManualFarmerRateChange = { manualFarmerRateInput = it },
                    onProceed = {
                        activeProcurementId?.let { id ->
                            val manualRateVal = manualFarmerRateInput.toDoubleOrNull()
                            onRecordMoisture(id, (moistureSlider * 10).toLong() / 10.0, selectedGodown, manualRateVal)
                            activeStep = 4
                        }
                    },
                    onBack = { activeStep = 2 },
                    accentColor = animatedCropColor
                )
            }
            4 -> {
                Step4UnloadingConfirmation(
                    farmerName = farmerName,
                    vehicleNumber = vehicleNumber,
                    godownAssigned = selectedGodown,
                    crop = selectedCrop,
                    onProceed = {
                        activeProcurementId?.let { id ->
                            onConfirmUnload(id)
                            activeStep = 5
                        }
                    },
                    onBack = { activeStep = 3 },
                    accentColor = animatedCropColor
                )
            }
            5 -> {
                Step5TareAndNetCalculation(
                    grossWeight = grossWeightInput.toDoubleOrNull() ?: 8420.0,
                    tareWeight = tareWeightInput,
                    onTareWeightChange = { tareWeightInput = it },
                    onAutoCaptureTare = { tareWeightInput = "3260" },
                    crop = selectedCrop,
                    onProceed = {
                        activeProcurementId?.let { id ->
                            val tareKg = tareWeightInput.toDoubleOrNull() ?: 3260.0
                            val grossKg = grossWeightInput.toDoubleOrNull() ?: 8420.0
                            val netKg = (grossKg - tareKg).coerceAtLeast(0.0)
                            val negotiatedRate = manualFarmerRateInput.toDoubleOrNull()
                            val rate = negotiatedRate ?: selectedCrop.standardMsp
                            val totalAmt = (netKg / 100.0) * rate

                            val snapshot = ProcurementEntity(
                                id = id,
                                tokenNo = "TK-${id + 1080}",
                                farmerName = farmerName,
                                mobileNumber = mobileNumber,
                                village = village,
                                vehicleNumber = vehicleNumber,
                                cropType = selectedCrop.name,
                                grossWeightKg = grossKg,
                                tareWeightKg = tareKg,
                                netWeightKg = netKg,
                                moisturePercentage = (moistureSlider * 10).toLong() / 10.0,
                                ratePerQuintal = rate,
                                totalAmount = totalAmt,
                                godownAssigned = selectedGodown,
                                status = "COMPLETED",
                                paymentStatus = "PAID"
                            )
                            completedProcurementSnapshot = snapshot
                            onRecordTareWeight(id, tareKg, negotiatedRate)
                            activeStep = 6
                        }
                    },
                    onBack = { activeStep = 4 },
                    accentColor = animatedCropColor
                )
            }
            6 -> {
                Step6StockUpdated(
                    snapshot = completedProcurementSnapshot,
                    onProceed = { activeStep = 7 },
                    accentColor = animatedCropColor
                )
            }
            7 -> {
                Step7ReceiptAndActions(
                    snapshot = completedProcurementSnapshot,
                    onOpenWhatsApp = { snapshot ->
                        snapshot?.let { onOpenWhatsApp(it, false) }
                    },
                    onOpenPdf = { snapshot ->
                        snapshot?.let { onOpenPdf(it) }
                    },
                    onStartNew = {
                        activeStep = 1
                        activeProcurementId = null
                        completedProcurementSnapshot = null
                        onFinishPipeline()
                    },
                    accentColor = animatedCropColor
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
private fun PipelineProgressHeader(
    activeStep: Int,
    accentColor: Color
) {
    val stepTitles = listOf(
        "Gate Entry",
        "Gross Wt",
        "Moisture Lab",
        "Unload Bay",
        "Tare Wt & Net",
        "Stock Update",
        "Receipt"
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PROCUREMENT STAGE: ${stepTitles.getOrElse(activeStep - 1) { "" }.uppercase()}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    ),
                    color = accentColor
                )

                Text(
                    text = "$activeStep / 7 Steps (${(activeStep.toFloat() / 7f * 100).toInt()}%)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    ),
                    color = Color(0xFF94A3B8)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 1..7) {
                    val isCompleted = i < activeStep
                    val isCurrent = i == activeStep
                    val dotColor = when {
                        isCompleted -> Color(0xFF10B981)
                        isCurrent -> accentColor
                        else -> Color(0xFF334155)
                    }

                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(dotColor),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompleted) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = "Done", tint = Color.White, modifier = Modifier.size(14.dp))
                        } else {
                            Text(
                                text = "$i",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                color = if (isCurrent) Color.Black else Color(0xFF94A3B8)
                            )
                        }
                    }

                    if (i < 7) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(3.dp)
                                .padding(horizontal = 2.dp)
                                .background(if (i < activeStep) Color(0xFF10B981) else Color(0xFF334155))
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Step1FarmerRegistration(
    farmerName: String,
    onFarmerNameChange: (String) -> Unit,
    mobileNumber: String,
    onMobileNumberChange: (String) -> Unit,
    village: String,
    onVillageChange: (String) -> Unit,
    vehicleNumber: String,
    onVehicleNumberChange: (String) -> Unit,
    selectedCrop: CropType,
    onCropSelected: (CropType) -> Unit,
    onProceed: () -> Unit,
    accentColor: Color
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Step 1 — Gate Entry & Farmer Registration", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                    Text("Farmer arrives at APMC security gate with grain vehicle", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                }
            }

            HorizontalDivider(color = Color(0xFF334155))

            OutlinedTextField(
                value = farmerName,
                onValueChange = onFarmerNameChange,
                label = { Text("Farmer Name") },
                placeholder = { Text("e.g. Ramesh Patil") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = accentColor) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_farmer_name"),
                colors = outlinedColors(accentColor),
                singleLine = true
            )

            OutlinedTextField(
                value = mobileNumber,
                onValueChange = onMobileNumberChange,
                label = { Text("Mobile Number (WhatsApp Receipt & Alert)") },
                placeholder = { Text("+91 98224 51230") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = accentColor) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_mobile_number"),
                colors = outlinedColors(accentColor),
                singleLine = true
            )

            OutlinedTextField(
                value = village,
                onValueChange = onVillageChange,
                label = { Text("Village / Tahsil") },
                placeholder = { Text("Pimpalner, Dhule") },
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = accentColor) },
                modifier = Modifier.fillMaxWidth(),
                colors = outlinedColors(accentColor),
                singleLine = true
            )

            OutlinedTextField(
                value = vehicleNumber,
                onValueChange = onVehicleNumberChange,
                label = { Text("Vehicle Number (Tractor / Truck)") },
                placeholder = { Text("MH 15 AB 1234") },
                leadingIcon = { Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = accentColor) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_vehicle_number"),
                colors = outlinedColors(accentColor),
                singleLine = true
            )

            Text(
                text = "Select Inbound Commodity:",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFFCBD5E1)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CropType.entries.take(4).forEach { crop ->
                    val isSel = crop == selectedCrop
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSel) crop.primaryColor else Color(0xFF0F172A))
                            .border(1.dp, if (isSel) crop.primaryColor else Color(0xFF334155), RoundedCornerShape(10.dp))
                            .clickable { onCropSelected(crop) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = crop.displayName.split(" ")[0],
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isSel) Color.Black else Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = onProceed,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("step1_submit_button"),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Proceed to Digital Weighbridge", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold))
                Spacer(modifier = Modifier.width(8.dp))
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }
        }
    }
}

@Composable
private fun Step2WeighbridgeGross(
    farmerName: String,
    vehicleNumber: String,
    crop: CropType,
    grossWeight: String,
    onGrossWeightChange: (String) -> Unit,
    onAutoCaptureIoT: () -> Unit,
    onProceed: () -> Unit,
    onBack: () -> Unit,
    accentColor: Color
) {
    var entryMode by remember { mutableStateOf(WeightEntryMode.AUTO_SENSOR) }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Scale, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Step 2 — Gross Weight Capture", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                    Text("Dual Weight Entry: Live Sensor or Manual Keypad", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                }
            }

            HorizontalDivider(color = Color(0xFF334155))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0F172A))
                    .padding(12.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("VEHICLE NUMBER", fontSize = 10.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                        Text(vehicleNumber, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("FARMER", fontSize = 10.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                        Text(farmerName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = accentColor)
                    }
                }
            }

            // Mode Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0F172A))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (entryMode == WeightEntryMode.AUTO_SENSOR) accentColor.copy(alpha = 0.25f)
                            else Color.Transparent
                        )
                        .clickable { entryMode = WeightEntryMode.AUTO_SENSOR }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Sensors,
                            contentDescription = null,
                            tint = if (entryMode == WeightEntryMode.AUTO_SENSOR) accentColor else Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "⚡ Sensor Auto-Capture",
                            fontSize = 11.sp,
                            fontWeight = if (entryMode == WeightEntryMode.AUTO_SENSOR) FontWeight.Bold else FontWeight.Normal,
                            color = if (entryMode == WeightEntryMode.AUTO_SENSOR) Color.White else Color(0xFF94A3B8)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (entryMode == WeightEntryMode.MANUAL_INPUT) accentColor.copy(alpha = 0.25f)
                            else Color.Transparent
                        )
                        .clickable { entryMode = WeightEntryMode.MANUAL_INPUT }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Keyboard,
                            contentDescription = null,
                            tint = if (entryMode == WeightEntryMode.MANUAL_INPUT) accentColor else Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "✍️ Manual Keypad",
                            fontSize = 11.sp,
                            fontWeight = if (entryMode == WeightEntryMode.MANUAL_INPUT) FontWeight.Bold else FontWeight.Normal,
                            color = if (entryMode == WeightEntryMode.MANUAL_INPUT) Color.White else Color(0xFF94A3B8)
                        )
                    }
                }
            }

            if (entryMode == WeightEntryMode.AUTO_SENSOR) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF070B10))
                        .border(2.dp, accentColor, RoundedCornerShape(12.dp))
                        .padding(18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "LIVE WEIGHBRIDGE GROSS READING",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = grossWeight.ifEmpty { "0" },
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontSize = 38.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = accentColor
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "KG",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = Color(0xFF34D399),
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }
                        Text("✅ Scale Stable • Dual Optical Beam Aligned", fontSize = 11.sp, color = Color(0xFF10B981))
                    }
                }

                Button(
                    onClick = onAutoCaptureIoT,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("btn_capture_gross_iot"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A), contentColor = accentColor),
                    border = androidx.compose.foundation.BorderStroke(1.dp, accentColor)
                ) {
                    Icon(Icons.Default.Sensors, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("⚡ Re-Sync Sensor Scale Weight", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "ENTER GROSS LOADED WEIGHT (KG):",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFCBD5E1)
                    )

                    OutlinedTextField(
                        value = grossWeight,
                        onValueChange = { newVal ->
                            onGrossWeightChange(newVal.filter { it.isDigit() })
                        },
                        label = { Text("Gross Weight (KG)") },
                        placeholder = { Text("e.g. 8420") },
                        leadingIcon = {
                            Icon(Icons.Default.Scale, contentDescription = null, tint = accentColor)
                        },
                        trailingIcon = {
                            Text("KG", fontWeight = FontWeight.Black, color = Color(0xFF34D399), modifier = Modifier.padding(end = 12.dp))
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_gross_weight"),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White
                        ),
                        colors = outlinedColors(accentColor)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("+100", "+500", "+1000").forEach { addStr ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF0F172A))
                                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                                    .clickable {
                                        val cur = grossWeight.toDoubleOrNull() ?: 0.0
                                        val added = addStr.removePrefix("+").toDouble()
                                        onGrossWeightChange((cur + added).toInt().toString())
                                    }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("$addStr kg", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = accentColor)
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF064E3B).copy(alpha = 0.4f))
                    .border(1.dp, Color(0xFF059669), RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color(0xFF25D366), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Instant WhatsApp alert: 'Vehicle Entry Successful: $grossWeight kg' will trigger to $farmerName.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = Color(0xFFA7F3D0)
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF94A3B8)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Back")
                }

                Button(
                    onClick = onProceed,
                    modifier = Modifier
                        .weight(2f)
                        .height(48.dp)
                        .testTag("step2_proceed_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Confirm Gross Weight", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold))
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                }
            }
        }
    }
}

@Composable
private fun Step3MoistureAndGrading(
    crop: CropType,
    moisture: Float,
    onMoistureChange: (Float) -> Unit,
    selectedGodown: String,
    godowns: List<GodownEntity>,
    onGodownSelected: (String) -> Unit,
    manualFarmerRate: String,
    onManualFarmerRateChange: (String) -> Unit,
    onProceed: () -> Unit,
    onBack: () -> Unit,
    accentColor: Color
) {
    val grade = when {
        moisture <= crop.idealMoisture -> QualityGrade.GRADE_A
        moisture <= crop.maxSafeMoisture -> QualityGrade.GRADE_B
        moisture <= crop.maxSafeMoisture + 3.0 -> QualityGrade.GRADE_C
        else -> QualityGrade.REJECTED
    }
    val autoRate = crop.standardMsp * grade.rateFactor
    val effectiveRate = manualFarmerRate.toDoubleOrNull() ?: autoRate

    val availableBays = remember(godowns) {
        if (godowns.isNotEmpty()) {
            godowns.map { it.displayName }
        } else {
            listOf("Godown 1 (Main Silo)", "Godown 2 (Secondary)", "Main Drying Yard")
        }
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Opacity, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Step 3 — Digital Moisture & Grading", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                    Text("Auto lab test reading & MSP price determination", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                }
            }

            HorizontalDivider(color = Color(0xFF334155))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0F172A))
                    .padding(14.dp)
            ) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("LAB MOISTURE READING", fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                        Text(
                            text = "${(moisture * 10).toInt() / 10.0}%",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (grade == QualityGrade.GRADE_A) Color(0xFF10B981) else if (grade == QualityGrade.GRADE_B) Color(0xFFF59E0B) else Color(0xFFEF4444)
                        )
                    }

                    Slider(
                        value = moisture,
                        onValueChange = onMoistureChange,
                        valueRange = 8f..20f,
                        steps = 24,
                        colors = SliderDefaults.colors(thumbColor = accentColor, activeTrackColor = accentColor),
                        modifier = Modifier.testTag("moisture_slider")
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("8% (Dry)", fontSize = 10.sp, color = Color(0xFF64748B))
                        Text("12% (Ideal)", fontSize = 10.sp, color = Color(0xFF10B981))
                        Text("14% (Safe Limit)", fontSize = 10.sp, color = Color(0xFFF59E0B))
                        Text("20% (Wet)", fontSize = 10.sp, color = Color(0xFFEF4444))
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (grade == QualityGrade.GRADE_A) Color(0xFF064E3B).copy(alpha = 0.4f)
                        else if (grade == QualityGrade.GRADE_B) Color(0xFF78350F).copy(alpha = 0.4f)
                        else Color(0xFF7F1D1D).copy(alpha = 0.4f)
                    )
                    .border(
                        1.dp,
                        if (grade == QualityGrade.GRADE_A) Color(0xFF10B981)
                        else if (grade == QualityGrade.GRADE_B) Color(0xFFF59E0B)
                        else Color(0xFFEF4444),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("AUTOMATIC GRADE CLASSIFICATION", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFCBD5E1))
                        Text(grade.name.replace("_", " "), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    }
                    Text("Auto MSP Baseline Rate: ₹${autoRate.toInt()} / quintal", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFCBD5E1))
                }
            }

            // Negotiated / Final Farmer Rate Input Field
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "FINAL FARMER RATE (₹ / QUINTAL)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = accentColor
                    )
                    Text(
                        text = if (manualFarmerRate.isNotBlank()) "★ Custom Rate Applied" else "Defaulting to MSP",
                        fontSize = 10.sp,
                        color = if (manualFarmerRate.isNotBlank()) Color(0xFF34D399) else Color(0xFF94A3B8)
                    )
                }

                OutlinedTextField(
                    value = manualFarmerRate,
                    onValueChange = { input ->
                        onManualFarmerRateChange(input.filter { it.isDigit() || it == '.' })
                    },
                    label = { Text("Negotiated Rate / Quintal") },
                    placeholder = { Text("e.g. ${autoRate.toInt()}") },
                    leadingIcon = {
                        Text("₹", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = accentColor, modifier = Modifier.padding(start = 12.dp, end = 4.dp))
                    },
                    trailingIcon = {
                        Text("/ Qtl", fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8), modifier = Modifier.padding(end = 12.dp))
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_manual_farmer_rate"),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White
                    ),
                    colors = outlinedColors(accentColor)
                )

                Text(
                    text = "Effective Rate: ₹${effectiveRate.toInt()} / Qtl (Overrides MSP in WhatsApp receipt & financial ledger)",
                    fontSize = 11.sp,
                    color = Color(0xFFA7F3D0)
                )
            }

            Text("Assign Storage Bay (Dynamic Facilities):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFCBD5E1))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                availableBays.forEach { bay ->
                    val isSel = bay == selectedGodown
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSel) accentColor.copy(alpha = 0.2f) else Color(0xFF0F172A))
                            .border(1.dp, if (isSel) accentColor else Color(0xFF334155), RoundedCornerShape(10.dp))
                            .clickable { onGodownSelected(bay) }
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Warehouse,
                                    contentDescription = null,
                                    tint = if (isSel) accentColor else Color(0xFF94A3B8),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = bay,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) Color.White else Color(0xFFCBD5E1)
                                )
                            }
                            if (isSel) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF94A3B8)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Back")
                }

                Button(
                    onClick = onProceed,
                    modifier = Modifier
                        .weight(2f)
                        .height(48.dp)
                        .testTag("step3_proceed_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Proceed to Unload Bay", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold))
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                }
            }
        }
    }
}

@Composable
private fun Step4UnloadingConfirmation(
    farmerName: String,
    vehicleNumber: String,
    godownAssigned: String,
    crop: CropType,
    onProceed: () -> Unit,
    onBack: () -> Unit,
    accentColor: Color
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Warehouse, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Step 4 — Unloading in $godownAssigned", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                    Text("Supervisor physically verifies pit discharge", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                }
            }

            HorizontalDivider(color = Color(0xFF334155))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0F172A))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("UNLOADING DIRECTIVE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                    Text("Vehicle $vehicleNumber is currently at Hopper Inlet #2 in $godownAssigned.", fontSize = 13.sp, color = Color.White)
                    Text("Farmer: $farmerName • Commodity: ${crop.displayName}", fontSize = 12.sp, color = accentColor)
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF94A3B8)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Text("Back")
                }

                Button(
                    onClick = onProceed,
                    modifier = Modifier
                        .weight(2f)
                        .height(52.dp)
                        .testTag("step4_confirm_unload"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981), contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("CONFIRM UNLOAD DONE", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 13.sp))
                }
            }
        }
    }
}

@Composable
private fun Step5TareAndNetCalculation(
    grossWeight: Double,
    tareWeight: String,
    onTareWeightChange: (String) -> Unit,
    onAutoCaptureTare: () -> Unit,
    crop: CropType,
    onProceed: () -> Unit,
    onBack: () -> Unit,
    accentColor: Color
) {
    var tareMode by remember { mutableStateOf(WeightEntryMode.AUTO_SENSOR) }
    val tareKg = tareWeight.toDoubleOrNull() ?: 3260.0
    val netKg = (grossWeight - tareKg).coerceAtLeast(0.0)
    val rate = crop.standardMsp
    val totalAmt = (netKg / 100.0) * rate
    val inrFormat = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Scale, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Step 5 — Tare Weight & Net Calculation", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                    Text("Empty vehicle tare capture (Gross - Tare = Net)", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                }
            }

            HorizontalDivider(color = Color(0xFF334155))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0F172A))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (tareMode == WeightEntryMode.AUTO_SENSOR) accentColor.copy(alpha = 0.25f)
                            else Color.Transparent
                        )
                        .clickable { tareMode = WeightEntryMode.AUTO_SENSOR }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Sensors,
                            contentDescription = null,
                            tint = if (tareMode == WeightEntryMode.AUTO_SENSOR) accentColor else Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "⚡ Tare Auto-Capture",
                            fontSize = 11.sp,
                            fontWeight = if (tareMode == WeightEntryMode.AUTO_SENSOR) FontWeight.Bold else FontWeight.Normal,
                            color = if (tareMode == WeightEntryMode.AUTO_SENSOR) Color.White else Color(0xFF94A3B8)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (tareMode == WeightEntryMode.MANUAL_INPUT) accentColor.copy(alpha = 0.25f)
                            else Color.Transparent
                        )
                        .clickable { tareMode = WeightEntryMode.MANUAL_INPUT }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Keyboard,
                            contentDescription = null,
                            tint = if (tareMode == WeightEntryMode.MANUAL_INPUT) accentColor else Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "✍️ Manual Tare Entry",
                            fontSize = 11.sp,
                            fontWeight = if (tareMode == WeightEntryMode.MANUAL_INPUT) FontWeight.Bold else FontWeight.Normal,
                            color = if (tareMode == WeightEntryMode.MANUAL_INPUT) Color.White else Color(0xFF94A3B8)
                        )
                    }
                }
            }

            if (tareMode == WeightEntryMode.AUTO_SENSOR) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF070B10))
                        .border(1.5.dp, accentColor, RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("LIVE EMPTY TARE WEIGHT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = tareWeight.ifEmpty { "0" },
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace,
                                color = accentColor
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("KG", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF34D399))
                        }
                        Text("✅ Empty Vehicle On Scale • Stable", fontSize = 11.sp, color = Color(0xFF10B981))
                    }
                }

                Button(
                    onClick = onAutoCaptureTare,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .testTag("btn_capture_tare_iot"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A), contentColor = accentColor),
                    border = androidx.compose.foundation.BorderStroke(1.dp, accentColor)
                ) {
                    Icon(Icons.Default.Sensors, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("⚡ Re-Capture Empty Tare", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("ENTER EMPTY VEHICLE TARE WEIGHT (KG):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFCBD5E1))

                    OutlinedTextField(
                        value = tareWeight,
                        onValueChange = { newVal ->
                            onTareWeightChange(newVal.filter { it.isDigit() })
                        },
                        label = { Text("Tare Weight (KG)") },
                        placeholder = { Text("e.g. 3260") },
                        leadingIcon = {
                            Icon(Icons.Default.Scale, contentDescription = null, tint = accentColor)
                        },
                        trailingIcon = {
                            Text("KG", fontWeight = FontWeight.Black, color = Color(0xFF34D399), modifier = Modifier.padding(end = 12.dp))
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_tare_weight"),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White
                        ),
                        colors = outlinedColors(accentColor)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            Pair("Tractor", "3200"),
                            Pair("6-Tyre", "5400"),
                            Pair("10-Tyre", "8800")
                        ).forEach { pair ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF0F172A))
                                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                                    .clickable { onTareWeightChange(pair.second) }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("${pair.first} (${pair.second}k)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = accentColor)
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF070B10))
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Gross Loaded Weight", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        Text("${grossWeight.toInt()} kg", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Tare Empty Weight", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        Text("${tareKg.toInt()} kg", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    HorizontalDivider(color = Color(0xFF334155), thickness = 1.dp)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Net ${crop.displayName.split(" ")[0]} Grain", color = accentColor, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                        Text(
                            "${grossWeight.toInt()} - ${tareKg.toInt()} = ${netKg.toInt()} kg",
                            color = Color(0xFF34D399),
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0F172A))
                    .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("TOTAL FARMER PAYOUT AMOUNT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                        Text(inrFormat.format(totalAmt), fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF10B981))
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("RATE PER QTL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                        Text("₹${rate.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF94A3B8)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Back")
                }

                Button(
                    onClick = onProceed,
                    modifier = Modifier
                        .weight(2f)
                        .height(48.dp)
                        .testTag("step5_complete_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Complete & Update Stock", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold))
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                }
            }
        }
    }
}

@Composable
private fun Step6StockUpdated(
    snapshot: ProcurementEntity?,
    onProceed: () -> Unit,
    accentColor: Color
) {
    val netMt = (snapshot?.netWeightKg ?: 5160.0) / 1000.0

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF10B981).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(32.dp))
            }

            Text("Step 6 — Stock Ledger Automatically Updated", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
            Text("Double-entry accounting executed. Storage facility stock incremented.", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0F172A))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Target Storage Facility", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        Text(snapshot?.godownAssigned ?: "Godown 1", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Earlier Stock", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        Text("148.20 MT", color = Color(0xFF94A3B8), fontSize = 13.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Inbound Net Added", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("+${(netMt * 100).toLong() / 100.0} MT", color = Color(0xFF10B981), fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                    }
                    HorizontalDivider(color = Color(0xFF334155))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Now Updated Stock", color = accentColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("${(148.20 + netMt).toString().take(6)} MT", color = accentColor, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    }
                }
            }

            Button(
                onClick = onProceed,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("step6_proceed_receipt"),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Generate Receipts & WhatsApp Alert", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.width(6.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }
        }
    }
}

@Composable
private fun Step7ReceiptAndActions(
    snapshot: ProcurementEntity?,
    onOpenWhatsApp: (ProcurementEntity?) -> Unit,
    onOpenPdf: (ProcurementEntity?) -> Unit,
    onStartNew: () -> Unit,
    accentColor: Color
) {
    val inrFormat = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF25D366).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color(0xFF25D366), modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Step 7 — WhatsApp Alert & Official PDF", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                    Text("Automated communication dispatched to farmer", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                }
            }

            HorizontalDivider(color = Color(0xFF334155))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0F172A))
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("TOKEN #", fontSize = 10.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                        Text(snapshot?.tokenNo ?: "TK-1081", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = accentColor)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Farmer", fontSize = 12.sp, color = Color(0xFF94A3B8))
                        Text(snapshot?.farmerName ?: "Ramesh Patil", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Net Grain", fontSize = 12.sp, color = Color(0xFF94A3B8))
                        Text("${snapshot?.netWeightKg?.toInt() ?: 5160} KG (${((snapshot?.netWeightKg ?: 5160.0) / 100.0).toInt()} Qtl)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF34D399))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Amount", fontSize = 12.sp, color = Color(0xFF94A3B8))
                        Text(inrFormat.format(snapshot?.totalAmount ?: 110940.0), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF10B981))
                    }
                }
            }

            Button(
                onClick = { onOpenWhatsApp(snapshot) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_send_whatsapp_receipt"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366), contentColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Send WhatsApp Receipt to Farmer", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = { onOpenPdf(snapshot) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_view_pdf_receipt"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, tint = Color(0xFFEF4444))
                Spacer(modifier = Modifier.width(8.dp))
                Text("View Official PDF Weighment Slip")
            }

            Button(
                onClick = onStartNew,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_start_new_inbound"),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Agriculture, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Register Next Vehicle at Gate", fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun outlinedColors(accentColor: Color) = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color(0xFFE2E8F0),
    focusedBorderColor = accentColor,
    unfocusedBorderColor = Color(0xFF334155),
    focusedLabelColor = accentColor,
    unfocusedLabelColor = Color(0xFF94A3B8),
    cursorColor = accentColor
)
