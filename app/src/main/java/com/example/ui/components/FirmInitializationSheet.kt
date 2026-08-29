package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FirmInitializationSheet(
    onDismissRequest: () -> Unit,
    onSave: (firmName: String, apmcCode: String, location: String, initialCapital: Double, facilities: List<Pair<String, Double>>) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    var firmName by remember { mutableStateOf("") }
    var apmcCode by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var capitalString by remember { mutableStateOf("") }
    
    // Storage Facilities State
    val facilities = remember { mutableStateListOf(Pair("Godown 1", "")) }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color(0xFF121826), // Deep Dark Navy
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 32.dp, top = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Firm Setup",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Enter your enterprise details to initialize GrainOS.",
                color = Color(0xFF94A3B8), // Slate 400
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))

            // Firm Name
            DarkGlassTextField(
                value = firmName,
                onValueChange = { firmName = it },
                label = "Firm Name",
                icon = Icons.Default.Business
            )

            // APMC Code
            DarkGlassTextField(
                value = apmcCode,
                onValueChange = { apmcCode = it },
                label = "APMC Code",
                icon = Icons.Default.QrCode
            )

            // Location
            DarkGlassTextField(
                value = location,
                onValueChange = { location = it },
                label = "Location",
                icon = Icons.Default.LocationOn
            )

            // Initial Capital (Numeric Validation)
            DarkGlassTextField(
                value = capitalString,
                onValueChange = { newValue ->
                    // Real-time numeric validation (only digits and one optional decimal point)
                    if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                        capitalString = newValue
                    }
                },
                label = "Initial Business Capital (₹)",
                icon = Icons.Default.AccountBalanceWallet,
                keyboardType = KeyboardType.Decimal
            )

            Spacer(modifier = Modifier.height(16.dp))
            
            Divider(color = Color(0xFF334155))
            
            Text(
                text = "Storage Facilities",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            facilities.forEachIndexed { index, facility ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        DarkGlassTextField(
                            value = facility.first,
                            onValueChange = { facilities[index] = facility.copy(first = it) },
                            label = "Facility Name",
                            icon = Icons.Default.Business
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        DarkGlassTextField(
                            value = facility.second,
                            onValueChange = { facilities[index] = facility.copy(second = it) },
                            label = "Capacity (MT)",
                            icon = Icons.Default.Assessment,
                            keyboardType = KeyboardType.Decimal
                        )
                    }
                }
            }
            
            TextButton(onClick = { facilities.add(Pair("", "")) }) {
                Text("+ Add Another Facility", color = Color(0xFFFF9800), fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(8.dp))

            // Neon Orange Save Button
            Button(
                onClick = {
                    val capital = capitalString.toDoubleOrNull() ?: 0.0
                    val parsedFacilities = facilities.filter { it.first.isNotBlank() && it.second.toDoubleOrNull() != null }
                        .map { Pair(it.first, it.second.toDouble()) }
                    onSave(firmName, apmcCode, location, capital, parsedFacilities)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF9800), // Neon Orange
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Save",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Save & Log In",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun DarkGlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color(0xFF64748B)) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color(0xFF94A3B8)
            )
        },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedContainerColor = Color.White.copy(alpha = 0.05f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
            focusedBorderColor = Color(0xFFFF9800), // Neon Orange focus
            unfocusedBorderColor = Color(0xFF334155),
            cursorColor = Color(0xFFFF9800)
        ),
        shape = RoundedCornerShape(12.dp)
    )
}
