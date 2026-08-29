package com.example.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CropType

@Composable
fun AiAdvisorScreen(
    activeCrop: CropType,
    aiResult: String?,
    isLoading: Boolean,
    onRunAnalysis: (CropType, Double, Double, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var farmerName by remember { mutableStateOf("Ramesh Patil") }
    var moistureSlider by remember { mutableStateOf(13.5f) }
    var ambientTempSlider by remember { mutableStateOf(29f) }
    var targetGodown by remember { mutableStateOf("Godown A") }
    var selectedCrop by remember { mutableStateOf(activeCrop) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // AI Header
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            border = androidx.compose.foundation.BorderStroke(1.dp, selectedCrop.primaryColor.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(38.dp).clip(CircleShape).background(selectedCrop.primaryColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = selectedCrop.primaryColor, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Gemini Agronomist & Storage Advisor", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                        Text("AI lot quality, aeration hours & drying recommendations", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                    }
                }

                Divider(color = Color(0xFF334155))

                // Crop Select
                Text("Select Crop Lot:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFCBD5E1))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(CropType.MAIZE, CropType.WHEAT, CropType.SOYBEAN).forEach { crop ->
                        val isSel = crop == selectedCrop
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) crop.primaryColor else Color(0xFF0F172A))
                                .clickable { selectedCrop = crop }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(crop.displayName.split(" ")[0], fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSel) Color.Black else Color.White)
                        }
                    }
                }

                // Moisture Slider
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Measured Grain Moisture %", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        Text("${"%.1f".format(moistureSlider)}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = selectedCrop.primaryColor)
                    }
                    Slider(
                        value = moistureSlider,
                        onValueChange = { moistureSlider = it },
                        valueRange = 8f..22f,
                        colors = SliderDefaults.colors(thumbColor = selectedCrop.primaryColor, activeTrackColor = selectedCrop.primaryColor)
                    )
                }

                // Ambient Temp Slider
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Ambient Warehouse Temp °C", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        Text("${ambientTempSlider.toInt()}°C", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                    }
                    Slider(
                        value = ambientTempSlider,
                        onValueChange = { ambientTempSlider = it },
                        valueRange = 15f..45f,
                        colors = SliderDefaults.colors(thumbColor = Color(0xFFF59E0B), activeTrackColor = Color(0xFFF59E0B))
                    )
                }

                OutlinedTextField(
                    value = farmerName,
                    onValueChange = { farmerName = it },
                    label = { Text("Farmer Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = selectedCrop.primaryColor,
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                Button(
                    onClick = {
                        onRunAnalysis(selectedCrop, (moistureSlider * 10).toLong() / 10.0, ambientTempSlider.toDouble(), targetGodown, farmerName)
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("run_ai_analysis_button"),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = selectedCrop.primaryColor, contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Analyzing with Gemini AI...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(imageVector = Icons.Default.Psychology, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generate AI Storage & Quality Advisory", fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }

        // Analysis Output Card
        if (aiResult != null) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = androidx.compose.foundation.BorderStroke(1.dp, selectedCrop.primaryColor)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = selectedCrop.primaryColor, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AI Agronomist Verdict", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
                    }
                    Divider(color = Color(0xFF1E293B))
                    Text(
                        text = aiResult,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        ),
                        color = Color(0xFFF1F5F9)
                    )
                }
            }
        }
    }
}
