package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.IoTTelemetryEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun IoTTerminalFeed(
    telemetryLogs: List<IoTTelemetryEntity>,
    isStreamingActive: Boolean,
    onToggleStreaming: () -> Unit,
    onInjectTestPacket: () -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    var expandedPayloadId by remember { mutableStateOf<Long?>(null) }
    val timeFormat = remember { SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()) }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF070B10))
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        // Terminal Window Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Traffic light indicator buttons
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(Color(0xFFEF4444)))
                    Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(Color(0xFFF59E0B)))
                    Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(Color(0xFF10B981)))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Icon(
                    imageVector = Icons.Default.Sensors,
                    contentDescription = "IoT Stream",
                    tint = if (isStreamingActive) Color(0xFF10B981) else Color(0xFF94A3B8),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "LIVE IoT TELEMETRY FEED",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    ),
                    color = if (isStreamingActive) Color(0xFF34D399) else Color(0xFF94A3B8)
                )
            }

            // Controls
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(
                    onClick = onInjectTestPacket,
                    modifier = Modifier.height(28.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = accentColor),
                    border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "+ Pulse",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = onToggleStreaming,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (isStreamingActive) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = "Toggle Stream",
                        tint = if (isStreamingActive) Color(0xFFEF4444) else Color(0xFF10B981),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Sub-millisecond Hardware Stream
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF03070C))
                .border(1.dp, Color(0xFF131D2A), RoundedCornerShape(10.dp))
                .padding(8.dp)
        ) {
            if (telemetryLogs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Waiting for hardware packets (Weighbridge / Moisture)...",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        ),
                        color = Color(0xFF64748B)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(telemetryLogs, key = { it.id }) { item ->
                        val isExpanded = expandedPayloadId == item.id
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isExpanded) Color(0xFF0E1A29) else Color.Transparent)
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = timeFormat.format(Date(item.timestamp)),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 9.sp
                                        ),
                                        color = Color(0xFF64748B)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    DeviceBadge(item.deviceType)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${item.readingValue} ${item.unit}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        ),
                                        color = Color(0xFFF1F5F9)
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${item.latencyMs}ms",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 9.sp
                                        ),
                                        color = Color(0xFF38BDF8)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    StatusBadge(item.status)
                                }
                            }

                            // Raw JSON payload snippet
                            Text(
                                text = item.rawPayloadJson,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp
                                ),
                                color = Color(0xFF64748B),
                                maxLines = 1,
                                modifier = Modifier.padding(top = 1.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceBadge(deviceType: String) {
    val (bg, fg) = when (deviceType) {
        "WEIGHBRIDGE" -> Pair(Color(0xFF3B82F6).copy(alpha = 0.2f), Color(0xFF60A5FA))
        "MOISTURE_METER" -> Pair(Color(0xFF10B981).copy(alpha = 0.2f), Color(0xFF34D399))
        "BARRIER_GATE" -> Pair(Color(0xFFF59E0B).copy(alpha = 0.2f), Color(0xFFFBBF24))
        else -> Pair(Color(0xFF8B5CF6).copy(alpha = 0.2f), Color(0xFFA78BFA))
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .padding(horizontal = 5.dp, vertical = 1.dp)
    ) {
        val short = when (deviceType) {
            "WEIGHBRIDGE" -> "WB-01"
            "MOISTURE_METER" -> "MOIST"
            "BARRIER_GATE" -> "GATE"
            else -> "SILO"
        }
        Text(
            text = short,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            ),
            color = fg
        )
    }
}

@Composable
private fun StatusBadge(status: String) {
    val (bg, fg) = when {
        status.contains("OK") || status.contains("ACCEPTED") || status.contains("COMPLETE") || status.contains("HEALTHY") ->
            Pair(Color(0xFF10B981).copy(alpha = 0.2f), Color(0xFF34D399))
        status.contains("REJECTED") || status.contains("ALERT") ->
            Pair(Color(0xFFEF4444).copy(alpha = 0.2f), Color(0xFFF87171))
        else -> Pair(Color(0xFF38BDF8).copy(alpha = 0.2f), Color(0xFF7DD3FC))
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .padding(horizontal = 4.dp, vertical = 1.dp)
    ) {
        Text(
            text = status.take(8),
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            ),
            color = fg
        )
    }
}
