package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CropType
import com.example.data.model.ExpenseEntryEntity
import com.example.data.model.FirmProfile
import com.example.data.model.OutboundDispatchEntity
import com.example.data.model.TradeBookingEntity
import com.example.data.model.TruckRejectionEntity
import com.example.ui.components.FinancialPnLChartCard
import com.example.ui.components.ProfitLossDashboard
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Enterprise Financial P&L Screen.
 * Calculates Net Profit / Loss by aggregating:
 * - Trade Revenues (Locked Broker / Buyer Contracts)
 * - Raw Grain Procurement Costs (Farmer Purchases)
 * - Manual Logged Expenses (Labor + Bags Per Truck + Transport + Misc)
 * - Truck Rejection Losses (Transport Losses + Penalties + Quality Deductions)
 */
@Composable
fun FinancialPnLScreen(
    activeCrop: CropType,
    firmProfile: FirmProfile,
    trades: List<TradeBookingEntity>,
    expenses: List<ExpenseEntryEntity> = emptyList(),
    rejections: List<TruckRejectionEntity> = emptyList(),
    dispatches: List<OutboundDispatchEntity> = emptyList(),
    onOpenBookTrade: () -> Unit,
    onOpenAddExpense: () -> Unit = {},
    onOpenRecordRejection: () -> Unit = {},
    onUpdateTradeStatus: (Long, String) -> Unit,
    onDeleteTrade: (Long) -> Unit,
    onDeleteRejection: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedFilterCrop by remember { mutableStateOf<CropType?>(null) }
    val inrFormat = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }

    val animatedAccent by animateColorAsState(
        targetValue = activeCrop.primaryColor,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "pnl_accent_anim"
    )

    val filteredTrades = if (selectedFilterCrop == null) {
        trades
    } else {
        trades.filter { it.cropType == selectedFilterCrop?.name }
    }

    val filteredDispatches = if (selectedFilterCrop == null) {
        dispatches
    } else {
        dispatches.filter { it.cropType == selectedFilterCrop?.name }
    }

    // P&L Aggregate Calculations using Actual Realized Data
    // Filter UNLOADED trades internally if they were passed, though DAO handles it too
    val totalBookedTons = filteredDispatches.sumOf { it.companyUnloadedWeightKg } / 1000.0
    val totalBookedQuintals = filteredDispatches.sumOf { it.companyUnloadedWeightKg } / 100.0
    val totalTradeRevenue = filteredDispatches.sumOf { it.actualNetRevenue }
    val totalTradeProcurementCost = filteredDispatches.sumOf { it.fifoProcurementCost }

    // Direct manual operational expenses
    val manualLaborExpense = expenses.sumOf { it.laborCost }
    val manualBagsExpense = expenses.sumOf { it.bagsCost }
    val manualTransportExpense = expenses.sumOf { it.transportCost }
    val manualMiscExpense = expenses.sumOf { it.miscCost }
    val totalManualExpenses = manualLaborExpense + manualBagsExpense + manualTransportExpense + manualMiscExpense

    // Fallback to trade-embedded overhead if no manual expenses entered yet
    val effectiveOperationalOverhead = if (totalManualExpenses > 0) totalManualExpenses else filteredDispatches.sumOf { it.loadingLaborCost + it.freightCost + it.bagCost + it.miscCost + it.finalBrokerageFee }

    // Direct truck rejection manual losses
    val totalTransportLoss = rejections.sumOf { it.transportLoss }
    val totalPenaltiesDemurrage = rejections.sumOf { it.penaltiesDemurrage }
    val totalQualityDeductions = rejections.sumOf { it.qualitySalvageDeduction }
    val totalRejectionLosses = totalTransportLoss + totalPenaltiesDemurrage + totalQualityDeductions

    // Final Net P&L = Gross Revenue - Procurement - Operational Expenses - Rejection Losses
    val totalOutflow = totalTradeProcurementCost + effectiveOperationalOverhead + totalRejectionLosses
    val netProfit = totalTradeRevenue - totalOutflow
    val roiPercentage = if (totalOutflow > 0) (netProfit / totalOutflow) * 100.0 else 0.0
    val avgNetMarginPerQtl = if (totalBookedQuintals > 0) netProfit / totalBookedQuintals else 0.0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Header & Quick Actions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Trade & P&L Analysis",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp
                        ),
                        color = Color.White
                    )
                    Text(
                        text = "Showing Realized Net Profit (Unloaded Trades Only)",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                        color = Color(0xFF34D399) // Badge visual
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = onOpenRecordRejection,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.ReportProblem, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Rejection Loss", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onOpenBookTrade,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = animatedAccent,
                            contentColor = Color.Black
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("btn_book_trade_action")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Book Trade", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 2. Crop Filter Tabs
        item {
            ScrollableTabRow(
                selectedTabIndex = if (selectedFilterCrop == null) 0 else selectedFilterCrop!!.ordinal + 1,
                containerColor = Color(0xFF1E293B),
                contentColor = animatedAccent,
                edgePadding = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedFilterCrop == null,
                    onClick = { selectedFilterCrop = null },
                    text = {
                        Text(
                            text = "All Commodities (${trades.size})",
                            fontSize = 12.sp,
                            fontWeight = if (selectedFilterCrop == null) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedFilterCrop == null) animatedAccent else Color(0xFF94A3B8)
                        )
                    }
                )

                CropType.values().forEach { crop ->
                    val isSelected = selectedFilterCrop == crop
                    Tab(
                        selected = isSelected,
                        onClick = { selectedFilterCrop = crop },
                        text = {
                            Text(
                                text = crop.displayName.split(" ").first(),
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) crop.primaryColor else Color(0xFF94A3B8)
                            )
                        }
                    )
                }
            }
        }

        // 3. Primary Executive P&L Hero Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (netProfit >= 0) Color(0xFF10B981).copy(alpha = 0.4f) else Color(0xFFEF4444).copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (netProfit >= 0) Color(0xFF10B981).copy(alpha = 0.2f)
                                        else Color(0xFFEF4444).copy(alpha = 0.2f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (netProfit >= 0) Icons.Default.TrendingUp else Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    tint = if (netProfit >= 0) Color(0xFF34D399) else Color(0xFFF87171),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (netProfit >= 0) "NET OPERATING PROFIT" else "NET OPERATING LOSS",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 1.sp
                                    ),
                                    color = if (netProfit >= 0) Color(0xFF34D399) else Color(0xFFF87171)
                                )
                                Text(
                                    text = "${totalBookedTons.toInt()} MT • ${totalBookedQuintals.toInt()} Quintals Total Volume",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        // ROI Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (roiPercentage >= 0) Color(0xFF064E3B) else Color(0xFF7F1D1D)
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = String.format(java.util.Locale.US, "%+.1f%% ROI", roiPercentage),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (roiPercentage >= 0) Color(0xFF34D399) else Color(0xFFF87171)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF3B82F6).copy(alpha = 0.15f))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "Showing Realized Net Profit (Unloaded Trades Only).",
                            color = Color(0xFF60A5FA),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = inrFormat.format(netProfit),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 32.sp
                        ),
                        color = if (netProfit >= 0) Color(0xFF34D399) else Color(0xFFF87171)
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Divider(color = Color(0xFF334155))
                    Spacer(modifier = Modifier.height(14.dp))

                    // 4-Column Metric Breakdown
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        PnlMiniStat(
                            title = "Gross Revenue",
                            value = inrFormat.format(totalTradeRevenue),
                            color = Color(0xFF38BDF8)
                        )
                        PnlMiniStat(
                            title = "Procurement",
                            value = "- ${inrFormat.format(totalTradeProcurementCost)}",
                            color = Color(0xFFFBBF24)
                        )
                        PnlMiniStat(
                            title = "Operations",
                            value = "- ${inrFormat.format(effectiveOperationalOverhead)}",
                            color = Color(0xFFA855F7)
                        )
                        PnlMiniStat(
                            title = "Rejections",
                            value = "- ${inrFormat.format(totalRejectionLosses)}",
                            color = Color(0xFFEF4444)
                        )
                    }
                }
            }
        }

        // 4. Financial Recharts / Visualization Chart
        item {
            FinancialPnLChartCard(
                activeCrop = activeCrop,
                trades = filteredTrades
            )
        }

        // 5. Per-Quintal Profit Margin & Daily Volume Dashboard
        item {
            ProfitLossDashboard(
                activeCrop = activeCrop,
                trades = filteredTrades
            )
        }

        // 5. Truck Rejection Deductions Summary (if any)
        if (rejections.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ReportProblem, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "REJECTED TRUCK LOSSES (${rejections.size})",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                                    color = Color(0xFFEF4444)
                                )
                            }
                            Text(
                                text = "- ${inrFormat.format(totalRejectionLosses)}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFEF4444)
                            )
                        }

                        rejections.forEach { rej ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF0F172A))
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${rej.truckNumber} • ${rej.buyerOrCompany}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Loss: Freight ₹${rej.transportLoss.toInt()} + Penalty ₹${rej.penaltiesDemurrage.toInt()} + Deduction ₹${rej.qualitySalvageDeduction.toInt()}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "- ${inrFormat.format(rej.totalRejectionLoss)}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFFEF4444)
                                    )
                                    IconButton(onClick = { onDeleteRejection(rej.id) }, modifier = Modifier.size(28.dp)) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFF64748B), modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 6. Trades Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ACTIVE CONTRACTS & TRADES (${filteredTrades.size})",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = Color(0xFF94A3B8)
                )

                Text(
                    text = "Avg Margin: ₹${avgNetMarginPerQtl.toInt()}/Qtl",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = animatedAccent
                )
            }
        }

        // 7. Trade List
        if (filteredTrades.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1E293B))
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No trades booked for this commodity",
                            color = Color(0xFF94A3B8),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Tap 'Book Trade' to lock in broker rates and track margins.",
                            color = Color(0xFF64748B),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        } else {
            items(filteredTrades, key = { it.id }) { trade ->
                TradeBookingItemCard(
                    trade = trade,
                    activeCrop = activeCrop,
                    onUpdateStatus = { onUpdateTradeStatus(trade.id, it) },
                    onDelete = { onDeleteTrade(trade.id) },
                    dateFormat = dateFormat,
                    inrFormat = inrFormat
                )
            }
        }
    }
}

@Composable
private fun PnlMiniStat(
    title: String,
    value: String,
    color: Color
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = Color(0xFF94A3B8)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 11.5.sp
            ),
            color = color
        )
    }
}

@Composable
private fun TradeBookingItemCard(
    trade: TradeBookingEntity,
    activeCrop: CropType,
    onUpdateStatus: (String) -> Unit,
    onDelete: () -> Unit,
    dateFormat: SimpleDateFormat,
    inrFormat: NumberFormat
) {
    val crop = CropType.values().find { it.name == trade.cropType } ?: activeCrop
    val isProfitable = trade.netProfit >= 0

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(crop.primaryColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (crop) {
                                CropType.MAIZE -> "🌽"
                                CropType.WHEAT -> "🌾"
                                CropType.SOYBEAN -> "🌱"
                                CropType.PADDY -> "🍚"
                                CropType.MUSTARD -> "🌼"
                            },
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = trade.brokerOrBuyerName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            ),
                            color = Color.White
                        )
                        Text(
                            text = "${trade.tradeNo} • ${dateFormat.format(Date(trade.tradeTimestamp))}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                // Profit / Loss Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isProfitable) Color(0xFF064E3B) else Color(0xFF7F1D1D)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = inrFormat.format(trade.netProfit),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp
                        ),
                        color = if (isProfitable) Color(0xFF34D399) else Color(0xFFF87171)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Trade Metrics Grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0F172A))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TradeMetricCol("Quantity", "${trade.quantityTons.toInt()} MT (${trade.totalQuintals.toInt()} Q)")
                TradeMetricCol("Broker Rate", "₹${trade.bookedPricePerQuintal.toInt()}/Q")
                TradeMetricCol("Farmer Rate", "₹${trade.farmerPurchasePricePerQuintal.toInt()}/Q")
                TradeMetricCol("Margin/Q", "₹${trade.netMarginPerQuintal.toInt()}/Q", if (isProfitable) Color(0xFF34D399) else Color(0xFFF87171))
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Actions & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("ACTIVE", "EXECUTED", "SETTLED").forEach { status ->
                        val isCurrent = trade.tradeStatus == status
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isCurrent) crop.primaryColor else Color(0xFF334155))
                                .clickable { onUpdateStatus(status) }
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = status,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrent) Color.Black else Color(0xFF94A3B8)
                            )
                        }
                    }
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFEF4444).copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TradeMetricCol(title: String, value: String, valueColor: Color = Color(0xFFE2E8F0)) {
    Column {
        Text(text = title, fontSize = 9.5.sp, color = Color(0xFF94A3B8))
        Text(text = value, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}
