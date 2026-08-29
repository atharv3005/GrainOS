package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun ArchitectureSpecDialog(
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(20.dp)),
            color = Color(0xFF0B1017)
        ) {
            Column {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF131D2A))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccountTree,
                            contentDescription = "Architecture",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "GrainOS Architecture & Security Spec",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            ),
                            color = Color.White
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(30.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                // Body Scroll
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Security Architecture
                    ArchCard(
                        title = "1. Enterprise Hardware Security Vault & R8 Obfuscation",
                        accentColor = Color(0xFF10B981),
                        icon = Icons.Default.Lock
                    ) {
                        Text(
                            text = "• Hardware KeyStore (AES-256 GCM): All sensitive APMC firm credentials, GST identifiers, and trade ledgers are encrypted at rest with non-extractable master keys.\n" +
                                    "• Root & Sandbox Integrity Checks: Scans `/system/bin/su`, test-keys signatures, and virtualization environments before decrypting high-value financial data.\n" +
                                    "• ProGuard / R8 Bytecode Optimization: Strips debug symbols, enforces tree-shaking, and renames internal class identifiers to prevent reverse-engineering.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            ),
                            color = Color(0xFFCBD5E1)
                        )
                    }

                    // Trading & P&L Engine
                    ArchCard(
                        title = "2. Real-Time Trading & Quintal Conversion Engine",
                        accentColor = Color(0xFFFBBF24),
                        icon = Icons.Default.Payments
                    ) {
                        Text(
                            text = "• Quintal-Metric Ton Conversion: 1 Metric Ton (MT) = 10 Quintals = 1,000 Kilograms.\n" +
                                    "• Financial Equation: Net Profit/Loss = Total Revenue (Broker Lock Rate × Qtl) - [Procurement Outflow (Farmer Base Rate × Qtl) + Total Overheads (Labor + Bags + Transport + Brokerage × Qtl)].\n" +
                                    "• Real-Time ROI & Margin Calculation: Sub-millisecond reactive StateFlow streams allow live profit projections prior to executing contract bookings.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            ),
                            color = Color(0xFFCBD5E1)
                        )
                    }

                    // Pipeline Overview
                    ArchCard(
                        title = "3. End-to-End Inbound IoT Telemetry Pipeline",
                        accentColor = Color(0xFF38BDF8),
                        icon = Icons.Default.Bolt
                    ) {
                        Text(
                            text = "• Inputs: RS-485 / Modbus Load-cell Weighbridge + NIR Dielectric Moisture Sensor + ANPR Gate.\n" +
                                    "• Edge Gateway: Continuous 50Hz load vector streaming with atomic double-entry Godown stock accounting.\n" +
                                    "• Persistence: Android Room with reactive Kotlin Coroutines & Flow.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            ),
                            color = Color(0xFFCBD5E1)
                        )
                    }

                    // Stock Ledger Double-Entry
                    ArchCard(
                        title = "4. Stock Ledger Double-Entry Consistency",
                        accentColor = Color(0xFFEC4899),
                        icon = Icons.Default.Timeline
                    ) {
                        Text(
                            text = "• ACID Transactional safety ensures that Gross - Tare net calculation atomically increments Godown inventory and generates payable ledger in a single atomic commit.\n" +
                                    "• Outbound dispatches deduct exact MT with optimistic locking to prevent overselling beyond silo capacities.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            ),
                            color = Color(0xFFCBD5E1)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ArchCard(
    title: String,
    accentColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF131D2A))
            .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = title, tint = accentColor, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    ),
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Divider(color = Color(0xFF1E293B), thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(6.dp))
            content()
        }
    }
}
