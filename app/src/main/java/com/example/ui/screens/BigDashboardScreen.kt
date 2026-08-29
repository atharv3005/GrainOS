package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.entryModelOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CropType
import com.example.data.model.FirmProfile
import com.example.data.model.GodownEntity
import com.example.data.model.IoTTelemetryEntity
import com.example.data.model.PaymentStatus
import com.example.data.model.ProcurementEntity
import com.example.data.model.TradeBookingEntity
import com.example.ui.components.GrainOSBrandLogo
import com.example.ui.components.IoTTerminalFeed
import com.example.ui.components.KpiWidget
import com.example.ui.components.Silo3DVisualizerCard
import java.text.NumberFormat
import java.util.Locale

@Composable
fun BigDashboardScreen(
    activeCrop: CropType,
    firmProfile: FirmProfile = FirmProfile(),
    onCropSelected: (CropType) -> Unit,
    procurements: List<ProcurementEntity>,
    godowns: List<GodownEntity>,
    liveGodownStockLedger: Map<String, Double>,
    trades: List<TradeBookingEntity> = emptyList(),
    telemetryLogs: List<IoTTelemetryEntity>,
    isStreamingActive: Boolean,
    onToggleStreaming: () -> Unit,
    onInjectTestPacket: () -> Unit,
    onStartNewInbound: () -> Unit,
    onOpenWhatsAppReceipt: (ProcurementEntity) -> Unit,
    onOpenPdfReceipt: (ProcurementEntity) -> Unit,
    onOpenAiAdvisor: () -> Unit,
    onOpenArchitecture: () -> Unit,
    onOpenFirmLogin: () -> Unit = {},
    onOpenSecurity: () -> Unit = {},
    onNavigateToPnL: () -> Unit = {},
    onOpenBookTrade: () -> Unit = {},
    onOpenExpenses: () -> Unit = {},
    smartInsight: String = "",
    onRefreshInsight: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val completedProcurements = procurements.filter { it.status == "COMPLETED" }
    val totalProcuredKg = completedProcurements.sumOf { it.netWeightKg }
    val totalProcuredMt = totalProcuredKg / 1000.0
    val totalFarmersCount = procurements.map { it.farmerName }.distinct().size
    val totalVehiclesCount = procurements.map { it.vehicleNumber }.distinct().size
    val totalPayouts = completedProcurements.sumOf { it.totalAmount }
    val pendingFarmersCount = procurements.count { it.paymentStatus == PaymentStatus.PENDING.name }

    val configuredCapacityMt = if (firmProfile.totalCapacityMt > 0) firmProfile.totalCapacityMt else 5000.0

    // Financial calculations
    val totalTradeRevenue = trades.sumOf { it.totalRevenue }
    val totalTradeCost = trades.sumOf { it.totalProcurementCost }
    val totalTradeOverhead = trades.sumOf { it.totalOverhead }
    val netTradeProfit = totalTradeRevenue - (totalTradeCost + totalTradeOverhead)
    val totalBookedTons = trades.sumOf { it.quantityTons }

    val animatedAccent by animateColorAsState(
        targetValue = activeCrop.primaryColor,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "dash_accent_anim"
    )

    val inrFormat = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 0. Active Firm Banner with 3D Brand Logo & Vault Status
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF1E293B).copy(alpha = 0.95f))
                .border(1.dp, animatedAccent.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GrainOSBrandLogo(
                    size = 46.dp,
                    activeCrop = activeCrop,
                    showText = true,
                    subtitle = "${firmProfile.firmName} • ${firmProfile.location}"
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Vault Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0F172A))
                            .border(1.dp, Color(0xFF10B981).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .clickable { onOpenSecurity() }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Security Vault",
                                tint = Color(0xFF34D399),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Vault", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF34D399))
                        }
                    }

                    // Edit / Switch Firm
                    OutlinedButton(
                        onClick = onOpenFirmLogin,
                        modifier = Modifier.height(32.dp).testTag("btn_switch_firm_dashboard"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = animatedAccent),
                        border = androidx.compose.foundation.BorderStroke(1.dp, animatedAccent.copy(alpha = 0.6f)),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Config", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 1. Dynamic Commodity Switcher with Color-Shifting Animation
        CropSelectorRow(
            activeCrop = activeCrop,
            onCropSelected = onCropSelected
        )

        // 2. Executive Quick-Action Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onStartNewInbound,
                modifier = Modifier
                    .weight(1.2f)
                    .height(48.dp)
                    .testTag("start_inbound_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = animatedAccent,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Scale, contentDescription = "Procure", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "+ Gate Entry",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                    )
                )
            }

            Button(
                onClick = onOpenBookTrade,
                modifier = Modifier
                    .weight(1.1f)
                    .height(48.dp)
                    .testTag("btn_dashboard_book_trade"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1E293B),
                    contentColor = Color(0xFF34D399)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.6f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Book Trade",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp)
                )
            }

            Button(
                onClick = onOpenAiAdvisor,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("ai_advisor_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1E293B),
                    contentColor = activeCrop.accentColor
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, animatedAccent.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = androidx.compose.ui.res.stringResource(com.example.R.string.dash_btn_ai_advisor),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp
                    )
                )
            }
        }

        // 3. 3D Grain Silo Storage Capacity Visualizer
        SmartInsightsCard(insight = smartInsight, onRefresh = onRefreshInsight)
        ProcurementVolumeChart(procurements)
        MoistureTrendChart(procurements)

        Silo3DVisualizerCard(
            activeCrop = activeCrop,
            godowns = godowns,
            liveGodownStockLedger = liveGodownStockLedger,
            totalCapacityMt = configuredCapacityMt
        )

        // 4. Trade Financial Snapshot (P&L Quick Glance)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToPnL() }
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = Color(0xFF34D399),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Trade Performance & P&L",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "View Ledger",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = animatedAccent
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = animatedAccent,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Divider(color = Color(0xFF334155))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Total Net Profit/Loss", fontSize = 10.sp, color = Color(0xFF94A3B8))
                        Text(
                            text = inrFormat.format(netTradeProfit),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (netTradeProfit >= 0) Color(0xFF34D399) else Color(0xFFF87171)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Active Bookings", fontSize = 10.sp, color = Color(0xFF94A3B8))
                        Text(
                            text = "${trades.size} Trades (${totalBookedTons.toInt()} MT)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // 4.5 Real-time Analytics Dashboard (Trends)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(text = "Volume & Profit Analytics", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                Spacer(modifier = Modifier.height(16.dp))
                val chartModel = remember { entryModelOf(12f, 22f, 18f, 32f, 28f, 44f, 40f) }
                Chart(
                    chart = lineChart(),
                    model = chartModel,
                    startAxis = rememberStartAxis(),
                    bottomAxis = rememberBottomAxis(),
                    modifier = Modifier.height(200.dp).fillMaxWidth()
                )
            }
        }

        // 5. Live Animated KPI Header (The BIG Dashboard)
        Text(
            text = "YARD PROCUREMENT & SETTLEMENTS",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.2.sp,
                fontSize = 11.sp
            ),
            color = Color(0xFF94A3B8)
        )

        // KPI Row 1: Today's Procurement + Farmers/Vehicles
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            KpiWidget(
                title = "Today's Procurement",
                value = "${(totalProcuredMt * 10).toLong() / 10.0}",
                unit = "MT",
                subtitle = "Total net grain unloaded",
                icon = Icons.Default.Agriculture,
                accentColor = animatedAccent,
                trendText = "+14.2% vs yesterday",
                progress = if (configuredCapacityMt > 0) (totalProcuredMt / configuredCapacityMt).toFloat() else 0.42f,
                modifier = Modifier.weight(1f)
            )

            KpiWidget(
                title = "Farmers & Vehicles",
                value = "$totalFarmersCount",
                unit = "Farmers",
                subtitle = "$totalVehiclesCount vehicles weighed",
                icon = Icons.Default.People,
                accentColor = Color(0xFF38BDF8),
                trendText = "$totalVehiclesCount Trucks",
                isPositiveTrend = true,
                modifier = Modifier.weight(1f)
            )
        }

        // KPI Row 2: Today's Payouts + Pending Payments
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val lakhsValue = (totalPayouts / 100000.0)
            KpiWidget(
                title = "Today's Payouts",
                value = "₹${"%.2f".format(lakhsValue)}",
                unit = "Lakhs",
                subtitle = "Net amount disbursed",
                icon = Icons.Default.CurrencyRupee,
                accentColor = Color(0xFF10B981),
                trendText = "Direct Bank",
                modifier = Modifier.weight(1f)
            )

            KpiWidget(
                title = "Pending Payments",
                value = "$pendingFarmersCount",
                unit = "Farmers",
                subtitle = "In queue for NEFT/RTGS",
                icon = Icons.Default.AccountBalanceWallet,
                accentColor = if (pendingFarmersCount > 0) Color(0xFFF59E0B) else Color(0xFF10B981),
                trendText = if (pendingFarmersCount > 0) "Needs Approval" else "All Cleared",
                isPositiveTrend = pendingFarmersCount == 0,
                modifier = Modifier.weight(1f)
            )
        }

        // 6. Live IoT Terminal Feed Window
        IoTTerminalFeed(
            telemetryLogs = telemetryLogs,
            isStreamingActive = isStreamingActive,
            onToggleStreaming = onToggleStreaming,
            onInjectTestPacket = onInjectTestPacket,
            accentColor = animatedAccent
        )

        // Extra bottom spacer for scrolling safety
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun CropSelectorRow(
    activeCrop: CropType,
    onCropSelected: (CropType) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = activeCrop.ordinal,
        containerColor = Color(0xFF1E293B),
        contentColor = activeCrop.primaryColor,
        edgePadding = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(14.dp))
    ) {
        CropType.values().forEach { crop ->
            val isSelected = activeCrop == crop
            Tab(
                selected = isSelected,
                onClick = { onCropSelected(crop) },
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = when (crop) {
                                CropType.MAIZE -> "🌽"
                                CropType.WHEAT -> "🌾"
                                CropType.SOYBEAN -> "🌱"
                                CropType.PADDY -> "🍚"
                                CropType.MUSTARD -> "🌼"
                            },
                            fontSize = 14.sp
                        )
                        Text(
                            text = crop.displayName.split(" ").first(),
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal,
                            color = if (isSelected) crop.primaryColor else Color(0xFF94A3B8)
                        )
                    }
                },
                modifier = Modifier.testTag("crop_tab_${crop.name.lowercase()}")
            )
        }
    }
}
