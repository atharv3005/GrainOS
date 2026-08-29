package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CropType
import com.example.data.model.OutboundDispatchEntity
import com.example.data.model.ProcurementEntity
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FinanceTrackerScreen(
    activeCrop: CropType,
    procurements: List<ProcurementEntity>,
    dispatches: List<OutboundDispatchEntity>,
    onPayFarmer: (Long) -> Unit,
    onSettleDispatch: (OutboundDispatchEntity) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val inrFormat = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }
    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }
    val now = System.currentTimeMillis()
    val fortyEightHoursMs = 48 * 60 * 60 * 1000L

    // Calculate Payables (Money to Give)
    val pendingPayables = procurements.filter { it.paymentStatus == "PENDING" && it.status == "COMPLETED" }
    
    // Notifications for deadlines within 48 hours
    val upcomingDeadlines = pendingPayables.filter { p ->
        val elapsed = now - (p.completedTimestamp.takeIf { it > 0 } ?: p.createdAt)
        val timeRemaining = fortyEightHoursMs - elapsed
        timeRemaining in 0..fortyEightHoursMs
    }
    
    val overduePayments = pendingPayables.filter { p ->
        val elapsed = now - (p.completedTimestamp.takeIf { it > 0 } ?: p.createdAt)
        elapsed > fortyEightHoursMs
    }

    // Calculate Receivables (Money to Receive)
    val pendingReceivables = dispatches.filter { it.status == "UNLOADED" || it.status == "IN_TRANSIT" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Finance & Payments Tracker", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)

        // Notification Alerts Section
        if (overduePayments.isNotEmpty() || upcomingDeadlines.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF450a0a)), // Dark red bg
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFf87171), RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = "Alert", tint = Color(0xFFf87171))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Payment Alerts", color = Color(0xFFf87171), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    if (overduePayments.isNotEmpty()) {
                        Text("OVERDUE: ${overduePayments.size} farmers passed 48-hour deadline! Total: ${inrFormat.format(overduePayments.sumOf { it.totalAmount })}", color = Color.White, fontSize = 13.sp)
                    }
                    if (upcomingDeadlines.isNotEmpty()) {
                        Text("UPCOMING: ${upcomingDeadlines.size} payments due within 48h. Total: ${inrFormat.format(upcomingDeadlines.sumOf { it.totalAmount })}", color = Color(0xFFfca5a5), fontSize = 13.sp)
                    }
                }
            }
        }

        // Summary Cards
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryCard(
                title = "Total Payables",
                amount = inrFormat.format(pendingPayables.sumOf { it.totalAmount }),
                icon = Icons.Default.MoneyOff,
                color = Color(0xFFef4444),
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                title = "Total Receivables",
                amount = inrFormat.format(pendingReceivables.sumOf { it.totalInvoiceAmount }),
                icon = Icons.Default.AttachMoney,
                color = Color(0xFF22c55e),
                modifier = Modifier.weight(1f)
            )
        }

        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = activeCrop.primaryColor,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = activeCrop.primaryColor
                )
            }
        ) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Payables (Farmers)") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Receivables (Buyers)") })
        }

        // List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (selectedTab == 0) {
                if (pendingPayables.isEmpty()) {
                    item { Text("No pending payments.", color = Color.Gray, modifier = Modifier.padding(16.dp)) }
                }
                items(pendingPayables.sortedBy { it.completedTimestamp }) { p ->
                    val elapsed = now - (p.completedTimestamp.takeIf { it > 0 } ?: p.createdAt)
                    val isOverdue = elapsed > fortyEightHoursMs
                    val deadlineStr = if (isOverdue) "OVERDUE" else "Due in ${(fortyEightHoursMs - elapsed) / (1000 * 60 * 60)}h"
                    
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        modifier = Modifier.fillMaxWidth().border(1.dp, if(isOverdue) Color.Red else Color(0xFF334155), RoundedCornerShape(12.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(p.farmerName, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                                Text(inrFormat.format(p.totalAmount), fontWeight = FontWeight.Bold, color = Color(0xFFef4444))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Token: ${p.tokenNo} | ${p.netWeightKg} Kg ${p.cropType}", color = Color.LightGray, fontSize = 13.sp)
                            Text("Deadline: $deadlineStr", color = if (isOverdue) Color(0xFFf87171) else Color(0xFFfbbf24), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { onPayFarmer(p.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = activeCrop.primaryColor),
                                modifier = Modifier.fillMaxWidth().height(40.dp)
                            ) {
                                Text("Mark as Paid", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                if (pendingReceivables.isEmpty()) {
                    item { Text("No pending receivables.", color = Color.Gray, modifier = Modifier.padding(16.dp)) }
                }
                items(pendingReceivables.sortedByDescending { it.timestamp }) { d ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(d.buyerName, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                                Text(inrFormat.format(d.totalInvoiceAmount), fontWeight = FontWeight.Bold, color = Color(0xFF22c55e))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Dispatch: ${d.dispatchNo} | ${d.status}", color = Color.LightGray, fontSize = 13.sp)
                            Text(dateFormat.format(Date(d.timestamp)), color = Color.Gray, fontSize = 12.sp)
                            
                            if (d.status == "UNLOADED") {
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedButton(
                                    onClick = { onSettleDispatch(d) },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = activeCrop.primaryColor),
                                    modifier = Modifier.fillMaxWidth().height(40.dp)
                                ) {
                                    Text("Settle Payment (P&L)")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryCard(title: String, amount: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, color = Color.LightGray, fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(amount, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
    }
}
