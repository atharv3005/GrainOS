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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
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
import java.text.NumberFormat
import java.util.Locale

@Composable
fun TradeBookingDialog(
    firmProfile: FirmProfile,
    activeCrop: CropType,
    onBookTrade: (
        cropType: CropType,
        brokerName: String,
        quantityTons: Double,
        bookedPricePerQuintal: Double,
        farmerPurchasePricePerQuintal: Double,
        notes: String
    ) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedCrop by remember { mutableStateOf(activeCrop) }
    var brokerName by remember { mutableStateOf("") }
    var quantityTonsStr by remember { mutableStateOf("50") }
    var bookedPriceStr by remember { mutableStateOf((selectedCrop.standardMsp + 120.0).toInt().toString()) }
    var laborCostStr by remember { mutableStateOf("18") }
    var transportCostStr by remember { mutableStateOf("35") }
    var bagsCostStr by remember { mutableStateOf("25") }
    var notes by remember { mutableStateOf("") }

    val inrFormat = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }
    val scrollState = rememberScrollState()

    val quantityTons = quantityTonsStr.toDoubleOrNull() ?: 0.0
    val bookedPrice = bookedPriceStr.toDoubleOrNull() ?: 0.0
    
    val laborCost = laborCostStr.toDoubleOrNull() ?: 0.0
    val transportCost = transportCostStr.toDoubleOrNull() ?: 0.0
    val bagsCost = bagsCostStr.toDoubleOrNull() ?: 0.0
    
    val totalOverheadPerQuintal = laborCost + transportCost + bagsCost
    val breakEvenPrice = bookedPrice - totalOverheadPerQuintal

    // Ton-to-Quintal Conversions: 1 MT = 10 Quintals
    val totalQuintals = quantityTons * 10.0
    val totalRevenue = bookedPrice * totalQuintals
    val totalOverhead = totalOverheadPerQuintal * totalQuintals

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, selectedCrop.primaryColor.copy(alpha = 0.5f), RoundedCornerShape(24.dp)),
            color = Color(0xFF0F172A)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Title
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(selectedCrop.primaryColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddShoppingCart,
                            contentDescription = null,
                            tint = selectedCrop.primaryColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Book Daily Market Trade",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "Predictive Engine: Find Safe Buying Range",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                // 1. Crop Selection
                Text(
                    text = "SELECT COMMODITY",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8),
                    letterSpacing = 1.sp
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    CropType.values().forEach { crop ->
                        val isSelected = selectedCrop == crop
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) crop.primaryColor.copy(alpha = 0.2f) else Color(0xFF1E293B))
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) crop.primaryColor else Color(0xFF334155),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    selectedCrop = crop
                                    bookedPriceStr = (crop.standardMsp + 120.0).toInt().toString()
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = crop.displayName.split(" ").first(),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else Color(0xFF94A3B8)
                            )
                        }
                    }
                }

                // 2. Broker / Buyer Name
                OutlinedTextField(
                    value = brokerName,
                    onValueChange = { brokerName = it },
                    label = { Text("Broker / Buyer Name & Mandi") },
                    placeholder = { Text("e.g., Cargill Agro / Broker M. Kulkarni") },
                    leadingIcon = {
                        Icon(Icons.Default.Business, contentDescription = null, tint = selectedCrop.primaryColor)
                    },
                    modifier = Modifier.fillMaxWidth().testTag("input_trade_broker_name"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color(0xFFE2E8F0),
                        focusedBorderColor = selectedCrop.primaryColor,
                        unfocusedBorderColor = Color(0xFF334155)
                    )
                )

                // 3. Trade Metrics Inputs
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = quantityTonsStr,
                        onValueChange = { quantityTonsStr = it },
                        label = { Text("Quantity (Tons)") },
                        suffix = { Text("MT", color = selectedCrop.primaryColor, fontSize = 11.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("input_trade_quantity_tons"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color(0xFFE2E8F0),
                            focusedBorderColor = selectedCrop.primaryColor,
                            unfocusedBorderColor = Color(0xFF334155)
                        )
                    )

                    OutlinedTextField(
                        value = bookedPriceStr,
                        onValueChange = { bookedPriceStr = it },
                        label = { Text("Broker Locked Rate") },
                        prefix = { Text("₹", color = Color(0xFF34D399), fontSize = 12.sp) },
                        suffix = { Text("/q", color = Color(0xFF64748B), fontSize = 10.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("input_trade_booked_price"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color(0xFFE2E8F0),
                            focusedBorderColor = Color(0xFF34D399),
                            unfocusedBorderColor = Color(0xFF334155)
                        )
                    )
                }

                Text(
                    text = "GRANULAR OVERHEAD COSTS (₹/QTL)",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8),
                    letterSpacing = 1.sp
                )
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = laborCostStr,
                        onValueChange = { laborCostStr = it },
                        label = { Text("Labor") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color(0xFFE2E8F0),
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color(0xFF334155)
                        )
                    )
                    OutlinedTextField(
                        value = transportCostStr,
                        onValueChange = { transportCostStr = it },
                        label = { Text("Transport") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color(0xFFE2E8F0),
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color(0xFF334155)
                        )
                    )
                    OutlinedTextField(
                        value = bagsCostStr,
                        onValueChange = { bagsCostStr = it },
                        label = { Text("Bags") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color(0xFFE2E8F0),
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color(0xFF334155)
                        )
                    )
                }

                // 4. Safe Buying Range Predictive Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF064E3B)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "TARGET BUYING PRICE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF6EE7B7),
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "₹${(breakEvenPrice - 100).toInt()} to ₹${breakEvenPrice.toInt()}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "Buy at or below ₹${breakEvenPrice.toInt()} per quintal to ensure zero loss.",
                            fontSize = 11.sp,
                            color = Color(0xFFD1FAE5)
                        )
                    }
                }

                // 5. Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Delivery Terms & Contract Notes") },
                    placeholder = { Text("e.g., Moisture spec < 12.5%, delivery to godown bay") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color(0xFFE2E8F0),
                        focusedBorderColor = selectedCrop.primaryColor,
                        unfocusedBorderColor = Color(0xFF334155)
                    )
                )

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(46.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF94A3B8)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (quantityTons > 0 && bookedPrice > 0) {
                                onBookTrade(
                                    selectedCrop,
                                    brokerName.ifEmpty { "Registered APMC Buyer" },
                                    quantityTons,
                                    bookedPrice,
                                    breakEvenPrice, // Use breakEvenPrice as safe purchase price
                                    notes
                                )
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1.5f).height(46.dp).testTag("btn_confirm_book_trade"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = selectedCrop.primaryColor,
                            contentColor = Color.Black
                        )
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Confirm Trade", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun CalcRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 11.sp, color = Color(0xFFCBD5E1))
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = valueColor)
    }
}
