package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.InventoryMovementEntity
import com.example.data.model.InventoryMovementType
import com.example.ui.viewmodel.GodownViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun InventoryMovementViewerScreen(
    viewModel: GodownViewModel,
    modifier: Modifier = Modifier
) {
    val movements by viewModel.allMovements.collectAsState()

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
                text = "Inventory Movements (अक्षय स्टॉक नोंदवही)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF9FAFB)
            )
            Text(
                text = "Append-only immutable stock ledger tracking every grain grain receipt, dispatch, transfer, and shrinkage",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF9CA3AF)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (movements.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No inventory movements logged.", color = Color(0xFF6B7280))
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(movements, key = { it.uuid }) { movement ->
                        MovementCard(movement = movement)
                    }
                }
            }
        }
    }
}

@Composable
private fun MovementCard(movement: InventoryMovementEntity) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }
    val timeStr = dateFormat.format(Date(movement.timestamp))

    val isPositive = movement.quantityKg >= 0
    val badgeColor = when (movement.movementType) {
        InventoryMovementType.RECEIPT.name -> Color(0xFF10B981)
        InventoryMovementType.DISPATCH.name -> Color(0xFF38BDF8)
        InventoryMovementType.SHRINKAGE.name -> Color(0xFFEF4444)
        InventoryMovementType.TRANSFER_IN.name -> Color(0xFF818CF8)
        InventoryMovementType.TRANSFER_OUT.name -> Color(0xFFF59E0B)
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
                        color = badgeColor.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = movement.movementType,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor
                        )
                    }
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(
                        text = "Facility: ${movement.facilityId}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF9FAFB)
                    )
                }

                Text(
                    text = "${if (isPositive) "+" else ""}${"%,.1f".format(movement.quantityKg)} kg",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isPositive) Color(0xFF10B981) else Color(0xFFEF4444)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Crop: ${movement.cropType} • Basis: ${movement.quantityBasis} • Ref: ${movement.sourceEntityType}",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF9CA3AF)
            )

            if (movement.reason.isNotBlank()) {
                Text(
                    text = movement.reason,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFD1D5DB)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = timeStr,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF6B7280)
            )
        }
    }
}
