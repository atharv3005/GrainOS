package com.example.ui.components

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.example.data.model.FirmProfile

data class DynamicFacilityItem(
    val id: Long,
    var name: String,
    var capacityStr: String
)

/**
 * Dynamic Multi-Storage Onboarding & Startup Setup Dialog.
 * Captures:
 * 1) Firm Trading Identity
 * 2) Dynamic Multi-Storage Facilities (e.g. Godown 1, Main Silo, Drying Yard) with MT capacity
 * 3) Primary Commodity / Target Crop
 */
@Composable
fun OnboardingSetupDialog(
    currentProfile: FirmProfile,
    onCompleteSetup: (
        firmName: String,
        capacityMt: Double,
        mainCrop: CropType,
        facilities: List<Pair<String, Double>>
    ) -> Unit,
    onDismiss: () -> Unit
) {
    var firmName by remember { mutableStateOf(currentProfile.firmName) }
    var selectedCrop by remember { mutableStateOf(currentProfile.mainTargetCrop) }

    // Dynamic list of storage facilities
    val facilities = remember {
        mutableStateListOf(
            DynamicFacilityItem(1L, "Godown 1 (Main Silo)", "2500"),
            DynamicFacilityItem(2L, "Godown 2 (Secondary)", "1500"),
            DynamicFacilityItem(3L, "Main Drying Yard", "1000")
        )
    }

    // Dynamic calculated total capacity in MT
    val totalCapacityCalculated = facilities.sumOf { it.capacityStr.toDoubleOrNull() ?: 0.0 }

    val animatedCropColor by animateColorAsState(
        targetValue = selectedCrop.primaryColor,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "crop_color_onboarding"
    )

    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(24.dp))
                .border(1.5.dp, animatedCropColor.copy(alpha = 0.6f), RoundedCornerShape(24.dp)),
            color = Color(0xFF0F172A),
            tonalElevation = 10.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Header with 3D Brand Logo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    GrainOSBrandLogo(
                        size = 44.dp,
                        activeCrop = selectedCrop,
                        showText = true,
                        subtitle = "Enterprise Multi-Storage Setup"
                    )
                }

                Text(
                    text = "Configure your trading identity, add distinct storage facilities with MT capacities, and choose your primary commodity.",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = Color(0xFF94A3B8)
                )

                // Step A: Firm Identity
                Text(
                    text = "A. FIRM IDENTITY",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    ),
                    color = animatedCropColor
                )

                OutlinedTextField(
                    value = firmName,
                    onValueChange = { firmName = it },
                    label = { Text("Trading Firm / FPC Name") },
                    placeholder = { Text("e.g., Vinayak Traders / Bijasani Mata Agro") },
                    leadingIcon = {
                        Icon(Icons.Default.Business, contentDescription = null, tint = animatedCropColor)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_onboard_firm_name"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color(0xFFE2E8F0),
                        focusedBorderColor = animatedCropColor,
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedLabelColor = animatedCropColor,
                        unfocusedLabelColor = Color(0xFF94A3B8)
                    )
                )

                // Step B: Dynamic Storage Facilities
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "B. STORAGE FACILITIES & CAPACITIES",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        ),
                        color = animatedCropColor
                    )
                    Text(
                        text = "Total: ${totalCapacityCalculated.toInt()} MT",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = Color(0xFF34D399)
                    )
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Add multiple distinct storage areas (Godowns, Drying Yards, Silos):",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = Color(0xFF94A3B8)
                        )

                        facilities.forEachIndexed { index, facility ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF0F172A))
                                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(10.dp))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(animatedCropColor.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = animatedCropColor
                                    )
                                }

                                OutlinedTextField(
                                    value = facility.name,
                                    onValueChange = { newName ->
                                        facilities[index] = facility.copy(name = newName)
                                    },
                                    label = { Text("Facility Name", fontSize = 10.sp) },
                                    placeholder = { Text("e.g. Silo A / Yard 1", fontSize = 11.sp) },
                                    modifier = Modifier.weight(1.4f),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = animatedCropColor,
                                        unfocusedBorderColor = Color(0xFF334155)
                                    )
                                )

                                OutlinedTextField(
                                    value = facility.capacityStr,
                                    onValueChange = { newCap ->
                                        facilities[index] = facility.copy(capacityStr = newCap.filter { it.isDigit() || it == '.' })
                                    },
                                    label = { Text("Cap (MT)", fontSize = 10.sp) },
                                    placeholder = { Text("MT", fontSize = 11.sp) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(0.9f),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color(0xFF34D399),
                                        unfocusedTextColor = Color(0xFF34D399),
                                        focusedBorderColor = animatedCropColor,
                                        unfocusedBorderColor = Color(0xFF334155)
                                    )
                                )

                                if (facilities.size > 1) {
                                    IconButton(
                                        onClick = { facilities.removeAt(index) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Remove Facility",
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Add Facility Button
                        OutlinedButton(
                            onClick = {
                                val nextNum = facilities.size + 1
                                facilities.add(
                                    DynamicFacilityItem(
                                        id = System.currentTimeMillis(),
                                        name = "Godown $nextNum",
                                        capacityStr = "1000"
                                    )
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .testTag("btn_add_storage_facility"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = animatedCropColor),
                            border = androidx.compose.foundation.BorderStroke(1.dp, animatedCropColor.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("+ Add Storage Facility", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        HorizontalDivider(color = Color(0xFF334155), thickness = 1.dp)

                        // Live Calculated Total Summary
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Storage,
                                    contentDescription = null,
                                    tint = animatedCropColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Total Storage Sum (${facilities.size} Facilities):",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFCBD5E1)
                                )
                            }
                            Text(
                                text = "${totalCapacityCalculated.toInt()} MT",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = animatedCropColor
                            )
                        }
                    }
                }

                // Step C: Primary Commodity / Target Crop
                Text(
                    text = "C. PRIMARY COMMODITY / TARGET CROP",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    ),
                    color = animatedCropColor
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CropType.entries.forEach { crop ->
                        val isSelected = selectedCrop == crop
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) crop.primaryColor.copy(alpha = 0.25f)
                                    else Color(0xFF1E293B)
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) crop.primaryColor else Color(0xFF334155),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedCrop = crop }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = when (crop) {
                                        CropType.MAIZE -> "🌽 Maize"
                                        CropType.WHEAT -> "🌾 Wheat"
                                        CropType.SOYBEAN -> "🌱 Soybean"
                                        CropType.PADDY -> "🍚 Paddy"
                                        CropType.MUSTARD -> "🌼 Mustard"
                                    }.split(" ").first(),
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFF94A3B8)
                                )
                                Text(
                                    text = crop.displayName.split(" ").first(),
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) crop.accentColor else Color(0xFF64748B)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF94A3B8)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Text("Dismiss")
                    }

                    Button(
                        onClick = {
                            val facilityPairs = facilities.map {
                                Pair(
                                    it.name.ifBlank { "Storage Facility" },
                                    it.capacityStr.toDoubleOrNull() ?: 500.0
                                )
                            }
                            onCompleteSetup(
                                firmName.trim().ifEmpty { "Vinayak Traders" },
                                totalCapacityCalculated.coerceAtLeast(100.0),
                                selectedCrop,
                                facilityPairs
                            )
                        },
                        modifier = Modifier
                            .weight(2f)
                            .height(48.dp)
                            .testTag("btn_save_onboarding"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = animatedCropColor,
                            contentColor = Color.Black
                        )
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Launch GrainOS",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
