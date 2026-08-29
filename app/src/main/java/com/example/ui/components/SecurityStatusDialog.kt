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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.security.DeviceSecurityReport

@Composable
fun SecurityStatusDialog(
    report: DeviceSecurityReport,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(22.dp))
                .border(1.dp, Color(0xFF10B981).copy(alpha = 0.5f), RoundedCornerShape(22.dp)),
            color = Color(0xFF0F172A)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF10B981).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = Color(0xFF34D399),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "GrainOS Security & Data Vault",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = report.securityLevel,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            ),
                            color = if (report.isRootDetected) Color(0xFFF87171) else Color(0xFF34D399)
                        )
                    }
                }

                // Status Banner
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (report.isRootDetected) Color(0xFF7F1D1D).copy(alpha = 0.4f)
                        else Color(0xFF064E3B).copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (report.isRootDetected) Icons.Default.Warning else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (report.isRootDetected) Color(0xFFF87171) else Color(0xFF34D399),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = report.statusMessage,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                }

                // Security Features Active
                Text(
                    text = "ACTIVE DEFENSES & DATA ENCRYPTION",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8),
                    letterSpacing = 1.sp
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E293B))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SecurityRowItem(
                        title = "Hardware Keystore (AES-256 GCM)",
                        description = "All financial trades & firm credentials encrypted at rest."
                    )
                    Divider(color = Color(0xFF334155))
                    SecurityRowItem(
                        title = "Root & Su Binary Scanner",
                        description = if (report.isRootDetected) "Warning: Root binary detected" else "No root/tamper binaries found."
                    )
                    Divider(color = Color(0xFF334155))
                    SecurityRowItem(
                        title = "Code Shrinking & R8 Obfuscation",
                        description = "ProGuard bytecode optimization active."
                    )
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(46.dp).testTag("btn_close_security"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155))
                ) {
                    Text("Close Security Report", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun SecurityRowItem(title: String, description: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = Color(0xFF34D399),
            modifier = Modifier.size(16.dp).padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(text = description, fontSize = 10.sp, color = Color(0xFF94A3B8))
        }
    }
}
