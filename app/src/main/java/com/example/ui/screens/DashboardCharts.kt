package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProcurementEntity
import com.example.data.model.ProcurementStatus
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.entry.FloatEntry
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProcurementVolumeChart(procurements: List<ProcurementEntity>) {
    val dailyVolumes = remember(procurements) {
        val format = SimpleDateFormat("MM/dd", Locale.getDefault())
        
        // Filter and group by start of day
        val groups = procurements
            .filter { it.status == ProcurementStatus.COMPLETED.name || it.status == ProcurementStatus.UNLOADED.name }
            .groupBy { 
                val cal = Calendar.getInstance()
                cal.timeInMillis = it.createdAt
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            .mapValues { (_, list) -> 
                (list.sumOf { it.netWeightKg } / 1000.0).toFloat()
            }
            .toSortedMap()
            
        // Get last 7 entries
        val keys = groups.keys.toList().takeLast(7)
        val entries = keys.mapIndexed { index, time ->
            FloatEntry(x = index.toFloat(), y = groups[time] ?: 0f)
        }
        
        Pair(entries, keys.map { format.format(Date(it)) })
    }

    if (dailyVolumes.first.isEmpty()) {
        Text("No procurement data yet for charts.", color = Color.Gray, modifier = Modifier.padding(16.dp))
        return
    }

    val model = entryModelOf(dailyVolumes.first)

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Daily Procurement Volume (MT)", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Chart(
                chart = columnChart(),
                model = model,
                startAxis = rememberStartAxis(),
                bottomAxis = rememberBottomAxis(
                    valueFormatter = { value, _ -> 
                        val idx = value.toInt()
                        if (idx >= 0 && idx < dailyVolumes.second.size) dailyVolumes.second[idx] else ""
                    }
                ),
                modifier = Modifier.height(200.dp)
            )
        }
    }
}

@Composable
fun MoistureTrendChart(procurements: List<ProcurementEntity>) {
    val dailyMoisture = remember(procurements) {
        val format = SimpleDateFormat("MM/dd", Locale.getDefault())
        
        val groups = procurements
            .filter { it.status == ProcurementStatus.COMPLETED.name || it.status == ProcurementStatus.UNLOADED.name }
            .groupBy { 
                val cal = Calendar.getInstance()
                cal.timeInMillis = it.createdAt
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            .mapValues { (_, list) -> 
                val validMoisture = list.filter { it.moisturePercentage > 0 }
                if (validMoisture.isNotEmpty()) {
                    (validMoisture.sumOf { it.moisturePercentage } / validMoisture.size).toFloat()
                } else {
                    0f
                }
            }
            .filterValues { it > 0f }
            .toSortedMap()
            
        val keys = groups.keys.toList().takeLast(7)
        val entries = keys.mapIndexed { index, time ->
            FloatEntry(x = index.toFloat(), y = groups[time] ?: 0f)
        }
        
        Pair(entries, keys.map { format.format(Date(it)) })
    }

    if (dailyMoisture.first.isEmpty()) {
        return
    }

    val model = entryModelOf(dailyMoisture.first)

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Average Moisture Trend (%)", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Chart(
                chart = lineChart(),
                model = model,
                startAxis = rememberStartAxis(),
                bottomAxis = rememberBottomAxis(
                    valueFormatter = { value, _ -> 
                        val idx = value.toInt()
                        if (idx >= 0 && idx < dailyMoisture.second.size) dailyMoisture.second[idx] else ""
                    }
                ),
                modifier = Modifier.height(200.dp)
            )
        }
    }
}
