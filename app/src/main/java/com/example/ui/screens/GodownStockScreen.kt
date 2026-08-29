package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CropType
import com.example.data.model.GodownEntity

import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf

@Composable
fun GodownStockScreen(
    godowns: List<GodownEntity>,
    activeCrop: CropType,
    liveGodownStockLedger: Map<String, Double>,
    getEstimatedPhysicalStock: (String) -> Double,
    onEndOfSeasonAudit: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalCapacity = godowns.sumOf { it.capacityMt }
    val totalBookStock = godowns.sumOf { liveGodownStockLedger[it.godownId] ?: 0.0 }
    val totalEstimatedStock = godowns.sumOf { getEstimatedPhysicalStock(it.godownId) }
    val availableSpace = (totalCapacity - totalEstimatedStock).coerceAtLeast(0.0)

    val modelProducer = remember { ChartEntryModelProducer() }
    LaunchedEffect(godowns, liveGodownStockLedger) {
        val entries = godowns.mapIndexed { index, it ->
            val est = getEstimatedPhysicalStock(it.godownId)
            val pct = if (it.capacityMt > 0) (est / it.capacityMt) * 100.0 else 0.0
            entryOf(index.toFloat(), pct.toFloat())
        }
        modelProducer.setEntries(entries)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Overview Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            border = androidx.compose.foundation.BorderStroke(1.dp, activeCrop.primaryColor.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Total Warehouse Inventory", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                        Text("Real-time telemetry across all 4 storage structures", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF10B981).copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("HEALTHY", color = Color(0xFF34D399), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }

                Divider(color = Color(0xFF334155))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    MetricSmall("Physical Stock", "${(totalEstimatedStock * 10).toLong() / 10.0} MT", activeCrop.primaryColor)
                    MetricSmall("Book Stock", "${(totalBookStock * 10).toLong() / 10.0} MT", Color(0xFF94A3B8))
                    MetricSmall("Free Space", "${(availableSpace * 10).toLong() / 10.0} MT", Color(0xFF38BDF8))
                }
            }
        }

        Text(
            text = "CAPACITY VISUALIZATION (%)",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
            color = Color(0xFF94A3B8)
        )

        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
        ) {
            Chart(
                chart = columnChart(),
                chartModelProducer = modelProducer,
                startAxis = rememberStartAxis(),
                bottomAxis = rememberBottomAxis(),
                modifier = Modifier.padding(16.dp).fillMaxWidth().height(200.dp)
            )
        }

        Text(
            text = "SILOS & STORAGE UNITS",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
            color = Color(0xFF94A3B8)
        )

        godowns.forEach { g ->
            val bookStock = liveGodownStockLedger[g.godownId] ?: 0.0
            val estimatedStock = getEstimatedPhysicalStock(g.godownId)
            
            GodownDetailCard(
                godown = g,
                bookStock = bookStock,
                estimatedStock = estimatedStock,
                onAudit = { onEndOfSeasonAudit(g.godownId) },
                accentColor = when (g.godownId) {
                    "GODOWN_A" -> Color(0xFFF59E0B)
                    "GODOWN_B" -> Color(0xFFD97706)
                    "GODOWN_C" -> Color(0xFF10B981)
                    else -> Color(0xFFEAB308)
                }
            )
        }
    }
}

@Composable
private fun GodownDetailCard(
    godown: GodownEntity,
    bookStock: Double,
    estimatedStock: Double,
    onAudit: () -> Unit,
    accentColor: Color
) {
    val fillRatio = if (godown.capacityMt > 0) (estimatedStock / godown.capacityMt).toFloat().coerceIn(0f, 1f) else 0f

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(36.dp).clip(CircleShape).background(accentColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Warehouse, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(godown.displayName, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
                        Text(godown.activeCrop, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = accentColor)
                    }
                }

                val warningColor = if (fillRatio >= 0.9f) Color(0xFFEF4444) else accentColor
                if (fillRatio >= 0.9f) {
                    Text("CAPACITY FULL WARNING", color = warningColor, fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.background(Color(0xFFEF4444).copy(alpha = 0.2f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
                }
                Text(
                    text = "${(fillRatio * 100).toInt()}% FULL",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                    color = warningColor
                )
            }

            // Progress Fill Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0F172A))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fillRatio)
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )
            }

            // Stock Numbers
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Physical (Shrink Adj): ${"%.2f".format(estimatedStock)} MT", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Book Stock (Ledger): ${"%.2f".format(bookStock)} MT", fontSize = 11.sp, color = Color(0xFF94A3B8))
                }
                Text("Capacity: ${godown.capacityMt.toInt()} MT", fontSize = 12.sp, color = Color(0xFF94A3B8))
            }

            Divider(color = Color(0xFF334155), thickness = 0.5.dp)

            // Environmental Telemetry Sensors
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SensorPill(Icons.Default.DeviceThermostat, "Temp", "${godown.temperatureCelsius}°C", Color(0xFFF59E0B))
                SensorPill(Icons.Default.Opacity, "Moisture", "${godown.averageMoisture}%", Color(0xFF38BDF8))
                SensorPill(Icons.Default.Air, "Ventilation", godown.ventilationStatus, Color(0xFF10B981))
                SensorPill(Icons.Default.Security, "Health", "NORMAL", Color(0xFF34D399))
            }
            
            // End of Season Audit Button
            Button(
                onClick = onAudit,
                modifier = Modifier.fillMaxWidth().height(36.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEF4444).copy(alpha = 0.15f),
                    contentColor = Color(0xFFEF4444)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("END OF SEASON AUDIT (ZERO OUT)", fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
        }
    }
}

@Composable
private fun SensorPill(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, tint: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(13.dp))
        Spacer(modifier = Modifier.width(3.dp))
        Column {
            Text(label, fontSize = 9.sp, color = Color(0xFF94A3B8))
            Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
private fun MetricSmall(label: String, value: String, color: Color) {
    Column {
        Text(label, fontSize = 11.sp, color = Color(0xFF94A3B8))
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = color)
    }
}
