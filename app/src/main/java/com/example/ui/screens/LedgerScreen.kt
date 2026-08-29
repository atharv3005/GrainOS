package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OutboundDispatchEntity
import com.example.data.model.ProcurementEntity
import com.example.ui.components.GrainGlassCard
import com.example.ui.components.MeshBackground
import com.example.ui.viewmodel.GrainWmsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class LedgerEntry(
    val id: String,
    val date: Long,
    val type: TransactionType,
    val cropType: String,
    val partyName: String,
    val quantityKg: Double,
    val amount: Double
)

enum class TransactionType {
    INCOME, // Procurement (Grain In, Cash Out, wait, green for income? Procurement is Grain In. Usually "Income" in warehouse context is grain incoming.)
    EXPENSE // Dispatch (Grain Out)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerScreen(
    viewModel: GrainWmsViewModel,
    modifier: Modifier = Modifier
) {
    val procurements by viewModel.allProcurements.collectAsState()
    val dispatches by viewModel.allDispatches.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    
    // Combine and map
    val ledgerEntries = remember(procurements, dispatches) {
        val mappedProcs = procurements.map {
            LedgerEntry(
                id = it.tokenNo,
                date = it.createdAt,
                type = TransactionType.INCOME, // Grain coming in
                cropType = it.cropType,
                partyName = it.farmerName,
                quantityKg = it.netWeightKg,
                amount = it.totalAmount
            )
        }
        val mappedDisps = dispatches.map {
            LedgerEntry(
                id = it.dispatchNo,
                date = it.timestamp,
                type = TransactionType.EXPENSE, // Grain going out
                cropType = it.cropType,
                partyName = it.buyerName,
                quantityKg = it.netLoadedWeightKg,
                amount = it.totalInvoiceAmount
            )
        }
        (mappedProcs + mappedDisps).sortedByDescending { it.date }
    }

    val filteredEntries = ledgerEntries.filter {
        it.cropType.contains(searchQuery, ignoreCase = true) ||
        it.partyName.contains(searchQuery, ignoreCase = true) ||
        it.id.contains(searchQuery, ignoreCase = true)
    }

    MeshBackground(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Transaction Ledger",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            // Search Filter
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search by Crop, Party, or ID", color = Color(0xFF94A3B8)) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF94A3B8))
                },
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

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(filteredEntries, key = { it.id }) { entry ->
                    LedgerCard(entry)
                }
            }
        }
    }
}

@Composable
fun LedgerCard(entry: LedgerEntry) {
    val isIncome = entry.type == TransactionType.INCOME
    val icon = if (isIncome) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward
    val iconBgColor = if (isIncome) Color(0xFF4CAF50).copy(alpha = 0.2f) else Color(0xFFFF9800).copy(alpha = 0.2f)
    val iconColor = if (isIncome) Color(0xFF4CAF50) else Color(0xFFFF9800)
    
    val dateFormatter = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())

    GrainGlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconColor)
            }

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.partyName,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "${entry.cropType} • ${dateFormatter.format(Date(entry.date))}",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp
                )
            }

            // Amount / Quantity
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹${String.format("%.2f", entry.amount)}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "${String.format("%.2f", entry.quantityKg)} Kg",
                    color = iconColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}
