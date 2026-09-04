package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.PdcStatus
import com.example.data.model.VendorLedgerEntity
import com.example.ui.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PdcManagementScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val allPdcs by viewModel.allPdcs.collectAsState()
    var selectedStatusFilter by remember { mutableStateOf("ALL") }

    var showBounceDialog by remember { mutableStateOf(false) }
    var selectedPdcToBounce by remember { mutableStateOf<VendorLedgerEntity?>(null) }
    var bounceReasonText by remember { mutableStateOf("") }

    val filteredPdcs = remember(allPdcs, selectedStatusFilter) {
        allPdcs.filter { pdc ->
            selectedStatusFilter == "ALL" || pdc.pdcStatus == selectedStatusFilter
        }
    }

    val totalActiveAmount = remember(allPdcs) {
        allPdcs.filter { it.pdcStatus in listOf(PdcStatus.ISSUED.name, PdcStatus.DEPOSITED.name, PdcStatus.PRESENTED.name) }
            .sumOf { it.amount }
    }

    val totalClearedAmount = remember(allPdcs) {
        allPdcs.filter { it.pdcStatus == PdcStatus.CLEARED.name }.sumOf { it.amount }
    }

    val totalBouncedCount = remember(allPdcs) {
        allPdcs.count { it.pdcStatus == PdcStatus.BOUNCED.name }
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
            // Header
            Text(
                text = "PDC Management (धनादेश व्यवस्थापन)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF9FAFB)
            )
            Text(
                text = "Track Post-Dated Cheques through ISSUED → DEPOSITED → PRESENTED → CLEARED / BOUNCED",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF9CA3AF)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Summary Metrics Row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricCard(
                    title = "Active PDCs",
                    value = "₹${"%,.0f".format(totalActiveAmount)}",
                    color = Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Cleared",
                    value = "₹${"%,.0f".format(totalClearedAmount)}",
                    color = Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Bounced",
                    value = "$totalBouncedCount Cheques",
                    color = Color(0xFFEF4444),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Status Filter Chips
            val statusFilters = listOf(
                "ALL" to "All PDCs",
                PdcStatus.ISSUED.name to "Issued",
                PdcStatus.DEPOSITED.name to "Deposited",
                PdcStatus.PRESENTED.name to "Presented",
                PdcStatus.CLEARED.name to "Cleared",
                PdcStatus.BOUNCED.name to "Bounced"
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                items(statusFilters) { (key, label) ->
                    val isSelected = selectedStatusFilter == key
                    Surface(
                        modifier = Modifier.clickable { selectedStatusFilter = key },
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

            // List of PDCs
            if (filteredPdcs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No Post-Dated Cheques in this status.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF6B7280)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredPdcs, key = { it.uuid }) { pdc ->
                        PdcCard(
                            pdc = pdc,
                            onDeposit = { viewModel.depositPdc(pdc.uuid) },
                            onPresent = { viewModel.presentPdc(pdc.uuid) },
                            onClear = { viewModel.clearPdc(pdc.uuid) },
                            onBounce = {
                                selectedPdcToBounce = pdc
                                bounceReasonText = ""
                                showBounceDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    // Bounce Confirmation Dialog
    if (showBounceDialog && selectedPdcToBounce != null) {
        AlertDialog(
            onDismissRequest = { showBounceDialog = false },
            title = { Text("Report Dishonored / Bounced PDC", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Dishonoring Cheque #${selectedPdcToBounce?.utrOrChequeNo} for ₹${"%,.2f".format(selectedPdcToBounce?.amount ?: 0.0)} will automatically reopen the payable liability.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFD1D5DB)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = bounceReasonText,
                        onValueChange = { bounceReasonText = it },
                        label = { Text("Reason for Dishonor (कारण)") },
                        placeholder = { Text("e.g., Insufficient Funds, Signature Mismatch") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFEF4444),
                            unfocusedBorderColor = Color(0xFF374151)
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (bounceReasonText.isNotBlank()) {
                            selectedPdcToBounce?.let { viewModel.bouncePdc(it.uuid, bounceReasonText.trim()) }
                            showBounceDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Confirm Bounce & Reopen Debt", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBounceDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = Color(0xFF9CA3AF))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
private fun PdcCard(
    pdc: VendorLedgerEntity,
    onDeposit: () -> Unit,
    onPresent: () -> Unit,
    onClear: () -> Unit,
    onBounce: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val maturityDateStr = if (pdc.chequeMaturityDate > 0) dateFormat.format(Date(pdc.chequeMaturityDate)) else "N/A"

    val statusColor = when (pdc.pdcStatus) {
        PdcStatus.CLEARED.name -> Color(0xFF10B981)
        PdcStatus.BOUNCED.name -> Color(0xFFEF4444)
        PdcStatus.PRESENTED.name -> Color(0xFF38BDF8)
        PdcStatus.DEPOSITED.name -> Color(0xFF818CF8)
        else -> Color(0xFFF59E0B)
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
                Column {
                    Text(
                        text = pdc.vendorName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF9FAFB)
                    )
                    Text(
                        text = "Cheque #${pdc.utrOrChequeNo.ifEmpty { "PDC-GEN" }} • Ref: ${pdc.referenceDocNo}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF9CA3AF)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹${"%,.2f".format(pdc.amount)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = statusColor.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = pdc.pdcStatus,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Schedule, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.padding(end = 4.dp))
                Text(
                    text = "Maturity Date: $maturityDateStr",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFD1D5DB)
                )
            }

            if (pdc.bounceReason != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "⚠️ Bounce Reason: ${pdc.bounceReason}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFEF4444),
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Lifecycle Action Buttons
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (pdc.pdcStatus) {
                    PdcStatus.ISSUED.name, PdcStatus.PENDING_MATURITY.name -> {
                        Button(
                            onClick = onDeposit,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF818CF8))
                        ) {
                            Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                            Text("Deposit in Bank", color = Color.White)
                        }
                    }
                    PdcStatus.DEPOSITED.name -> {
                        OutlinedButton(onClick = onPresent) {
                            Text("Present for Clearing", color = Color(0xFF38BDF8))
                        }
                    }
                    PdcStatus.PRESENTED.name -> {
                        OutlinedButton(
                            onClick = onBounce,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))
                        ) {
                            Icon(Icons.Default.Error, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                            Text("Report Bounce")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onClear,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                            Text("Mark Cleared", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
