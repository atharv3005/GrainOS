package com.example.ui.components

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.CropType
import com.example.data.model.FirmProfile

@Composable
fun FirmLoginDialog(
    currentProfile: FirmProfile,
    activeCrop: CropType,
    onSaveProfile: (FirmProfile) -> Unit,
    onSaveFacilities: (List<Pair<String, Double>>) -> Unit,
    onDismiss: () -> Unit
) {
    var firmName by remember { mutableStateOf(currentProfile.firmName) }
    var regNumber by remember { mutableStateOf(currentProfile.registrationNumber) }
    var location by remember { mutableStateOf(currentProfile.location) }
    var operatorName by remember { mutableStateOf(currentProfile.operatorName) }
    var tagLine by remember { mutableStateOf(currentProfile.tagLine) }
    var contactNumber by remember { mutableStateOf(currentProfile.contactNumber) }
    val facilities = remember {
        mutableStateListOf(
            DynamicFacilityItem(1L, "Godown 1 (Main Silo)", "2500")
        )
    }

    val presetProfiles = remember {
        listOf(
            FirmProfile(
                firmName = "Bijasani Mata Agro FPC Ltd.",
                registrationNumber = "U01110MH2021PTC",
                location = "Dhule, Maharashtra",
                operatorName = "Operator A. Patil",
                contactNumber = "+91 98220 12345",
                tagLine = "Digital Weighbridge & Warehouse Division"
            ),
            FirmProfile(
                firmName = "Kisan Samriddhi Agri Logistics",
                registrationNumber = "KS-AGRO-2024-MH",
                location = "Nashik Mandi Yard, MH",
                operatorName = "Manager S. Deshmukh",
                contactNumber = "+91 98810 54321",
                tagLine = "Multi-Commodity Silo & Weighbridge Network"
            ),
            FirmProfile(
                firmName = "Shree Ganesh Grain Warehousing",
                registrationNumber = "SG-WMS-9981-IND",
                location = "Indore APMC Yard, MP",
                operatorName = "Incharge Vikram Sharma",
                contactNumber = "+91 97550 67890",
                tagLine = "Certified Cold & Dry Grain Storage"
            )
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(20.dp)),
            color = Color(0xFF0F172A)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Modal Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E293B))
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(activeCrop.primaryColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Apartment,
                                contentDescription = null,
                                tint = activeCrop.primaryColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Firm Profile & Login Setup",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                ),
                                color = Color.White
                            )
                            Text(
                                text = "Customize firm branding for slips & receipts",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Quick Preset Switcher
                    Text(
                        text = "QUICK PRESETS / SWITCH FIRM",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp
                        ),
                        color = Color(0xFF94A3B8)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presetProfiles.forEach { preset ->
                            val isSelected = firmName.equals(preset.firmName, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) activeCrop.primaryColor.copy(alpha = 0.25f) else Color(0xFF1E293B))
                                    .border(
                                        1.dp,
                                        if (isSelected) activeCrop.primaryColor else Color(0xFF334155),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        firmName = preset.firmName
                                        regNumber = preset.registrationNumber
                                        location = preset.location
                                        operatorName = preset.operatorName
                                        contactNumber = preset.contactNumber
                                        tagLine = preset.tagLine
                                    }
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = preset.firmName.split(" ").take(2).joinToString(" "),
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) activeCrop.primaryColor else Color.White,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Divider(color = Color(0xFF334155), thickness = 0.5.dp)

                    // Firm Name Input Field
                    OutlinedTextField(
                        value = firmName,
                        onValueChange = { firmName = it },
                        label = { Text("Firm / Company / FPC Name") },
                        leadingIcon = {
                            Icon(Icons.Default.Business, contentDescription = null, tint = activeCrop.primaryColor)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_firm_name"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = activeCrop.primaryColor,
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = activeCrop.primaryColor,
                            unfocusedLabelColor = Color(0xFF94A3B8)
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Registration / License No
                    OutlinedTextField(
                        value = regNumber,
                        onValueChange = { regNumber = it },
                        label = { Text("Registration / License / APMC Code") },
                        leadingIcon = {
                            Icon(Icons.Default.Badge, contentDescription = null, tint = activeCrop.primaryColor)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_firm_reg"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = activeCrop.primaryColor,
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = activeCrop.primaryColor,
                            unfocusedLabelColor = Color(0xFF94A3B8)
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Location / Yard
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Location / District / APMC Yard") },
                        leadingIcon = {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = activeCrop.primaryColor)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_firm_location"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = activeCrop.primaryColor,
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = activeCrop.primaryColor,
                            unfocusedLabelColor = Color(0xFF94A3B8)
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Operator / Incharge Name
                    OutlinedTextField(
                        value = operatorName,
                        onValueChange = { operatorName = it },
                        label = { Text("Weighbridge Operator / Incharge Name") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = activeCrop.primaryColor)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_firm_operator"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = activeCrop.primaryColor,
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = activeCrop.primaryColor,
                            unfocusedLabelColor = Color(0xFF94A3B8)
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Tagline / Subtitle for Receipts
                    OutlinedTextField(
                        value = tagLine,
                        onValueChange = { tagLine = it },
                        label = { Text("Receipt Subtitle / Unit Division") },
                        leadingIcon = {
                            Icon(Icons.Default.Tag, contentDescription = null, tint = activeCrop.primaryColor)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_firm_tagline"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = activeCrop.primaryColor,
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = activeCrop.primaryColor,
                            unfocusedLabelColor = Color(0xFF94A3B8)
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Divider(color = Color(0xFF334155), thickness = 0.5.dp)
                    Text(
                        text = "STORAGE FACILITIES",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp
                        ),
                        color = Color(0xFF94A3B8)
                    )
                    facilities.forEachIndexed { index, facility ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = facility.name,
                                onValueChange = { newName ->
                                    facilities[index] = facility.copy(name = newName)
                                },
                                label = { Text("Facility Name") },
                                modifier = Modifier.weight(1.5f),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = activeCrop.primaryColor,
                                    unfocusedBorderColor = Color(0xFF334155),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                )
                            )
                            OutlinedTextField(
                                value = facility.capacityStr,
                                onValueChange = { newCap ->
                                    facilities[index] = facility.copy(capacityStr = newCap.filter { it.isDigit() || it == '.' })
                                },
                                label = { Text("Capacity (MT)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = activeCrop.primaryColor,
                                    unfocusedBorderColor = Color(0xFF334155),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                )
                            )
                            IconButton(
                                onClick = { facilities.removeAt(index) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                            }
                        }
                    }
                    OutlinedButton(
                        onClick = {
                            facilities.add(DynamicFacilityItem(System.currentTimeMillis(), "", "500"))
                        },
                        modifier = Modifier.fillMaxWidth().height(40.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("+ Add Another Facility")
                    }

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF94A3B8)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                val updated = FirmProfile(
                                    firmName = firmName.trim().ifEmpty { "Agro Warehouse WMS" },
                                    registrationNumber = regNumber.trim(),
                                    location = location.trim(),
                                    operatorName = operatorName.trim().ifEmpty { "Operator" },
                                    contactNumber = contactNumber.trim(),
                                    tagLine = tagLine.trim()
                                )
                                onSaveProfile(updated)
                                val facPairs = facilities.map { Pair(it.name.ifBlank { "Storage" }, it.capacityStr.toDoubleOrNull() ?: 500.0) }
                                onSaveFacilities(facPairs)
                                onDismiss()
                            },
                            modifier = Modifier
                                .weight(1.5f)
                                .testTag("btn_save_firm_login"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = activeCrop.primaryColor,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save & Log In", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
