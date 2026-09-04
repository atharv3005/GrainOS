package com.example.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AuditAction
import com.example.data.model.AuditTrailEntity
import com.example.ui.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AuditTrailViewerScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val auditTrails by viewModel.allAuditTrails.collectAsState()
    var selectedActionFilter by remember { mutableStateOf("ALL") }

    val filteredTrails = remember(auditTrails, selectedActionFilter) {
        auditTrails.filter { trail ->
            selectedActionFilter == "ALL" || trail.action == selectedActionFilter
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFF0F172A)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "Immutable Audit Trail (अपरिवर्तनीय बदल नोंदणी)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF9FAFB)
            )
            Text(
                text = "Full compliance change log tracking creates, updates, and hard deletion attempts",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF9CA3AF)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action Filter Chips
            val actionFilters = listOf("ALL" to "All Actions") + AuditAction.entries.map { it.name to it.label }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                items(actionFilters) { (key, label) ->
                    val isSelected = selectedActionFilter == key
                    Surface(
                        modifier = Modifier.clickable { selectedActionFilter = key },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) Color(0xFF10B981) else Color(0xFF1E293B)
                    ) {
                        Text(
                            text = label,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSelected) Color.White else Color(0xFF9CA3AF),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredTrails.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No audit log entries recorded.", color = Color(0xFF6B7280))
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(filteredTrails, key = { it.uuid }) { trail ->
                        AuditTrailCard(trail = trail)
                    }
                }
            }
        }
    }
}

@Composable
private fun AuditTrailCard(trail: AuditTrailEntity) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault()) }
    val timeStr = dateFormat.format(Date(trail.timestamp))

    val actionColor = when (trail.action) {
        AuditAction.CREATE.name -> Color(0xFF10B981)
        AuditAction.UPDATE.name -> Color(0xFF38BDF8)
        AuditAction.DELETE.name -> Color(0xFFEF4444)
        AuditAction.REVERSE.name -> Color(0xFFF59E0B)
        else -> Color(0xFF9CA3AF)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = actionColor.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = trail.action,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = actionColor
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${trail.entityType}: ${trail.entityId}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF9FAFB)
                    )
                }
                Text(
                    text = timeStr,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF9CA3AF)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Reason: ${trail.reason}",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFD1D5DB)
            )

            Text(
                text = "User: ${trail.userId} • Device: ${trail.deviceId}",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF6B7280)
            )

            if (trail.previousStateJson != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF0F172A)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "PREVIOUS STATE:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444)
                        )
                        Text(
                            text = trail.previousStateJson,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF9CA3AF)
                        )
                    }
                }
            }
        }
    }
}
