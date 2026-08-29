package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DonutLarge
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CropType
import com.example.data.model.TradeBookingEntity
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ChartViewMode {
    COMPARATIVE_BARS,
    COMMODITY_BREAKDOWN,
    EXPENSE_STACK
}

data class FinancialDataPoint(
    val label: String,
    val sublabel: String,
    val revenue: Double,
    val procurementCost: Double,
    val overheadCost: Double,
    val netProfit: Double,
    val quantityTons: Double,
    val cropType: String,
    val laborCost: Double = 0.0,
    val bagCost: Double = 0.0,
    val freightCost: Double = 0.0,
    val brokerageCost: Double = 0.0
)

/**
 * Recharts-inspired Financial Visualization Component for P&L Dashboard.
 * Plots Revenue versus Farmer Procurement and Overhead Expenses with interactive
 * touch scrub, multi-series bar comparators, expense breakdown, and trend lines.
 */
@Composable
fun FinancialPnLChartCard(
    activeCrop: CropType,
    trades: List<TradeBookingEntity>,
    modifier: Modifier = Modifier
) {
    var viewMode by remember { mutableStateOf(ChartViewMode.COMPARATIVE_BARS) }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var showRevenueSeries by remember { mutableStateOf(true) }
    var showProcurementSeries by remember { mutableStateOf(true) }
    var showOverheadSeries by remember { mutableStateOf(true) }
    var showProfitTrend by remember { mutableStateOf(true) }

    val inrFormat = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }
    val dateFormat = remember { SimpleDateFormat("dd MMM", Locale.getDefault()) }

    // Map trades into chart data points
    val chartDataPoints = remember(trades, viewMode) {
        when (viewMode) {
            ChartViewMode.COMPARATIVE_BARS -> {
                if (trades.isEmpty()) {
                    generateSampleTradePoints()
                } else {
                    trades.takeLast(8).mapIndexed { idx, trade ->
                        val dateStr = try {
                            dateFormat.format(Date(trade.tradeTimestamp))
                        } catch (_: Exception) {
                            "Lot #${trade.id}"
                        }
                        FinancialDataPoint(
                            label = if (trade.tradeNo.isNotBlank()) trade.tradeNo.takeLast(5) else "T-${trade.id.toString().takeLast(3)}",
                            sublabel = "${trade.brokerOrBuyerName.take(8)}.. ($dateStr)",
                            revenue = trade.totalRevenue,
                            procurementCost = trade.totalProcurementCost,
                            overheadCost = trade.totalOverhead,
                            netProfit = trade.netProfit,
                            quantityTons = trade.quantityTons,
                            cropType = trade.cropType,
                            laborCost = trade.laborPerQuintal * trade.totalQuintals,
                            bagCost = trade.bagCostPerQuintal * trade.totalQuintals,
                            freightCost = trade.transportPerQuintal * trade.totalQuintals,
                            brokerageCost = trade.brokeragePerQuintal * trade.totalQuintals
                        )
                    }
                }
            }
            ChartViewMode.COMMODITY_BREAKDOWN -> {
                CropType.values().mapNotNull { crop ->
                    val cropTrades = trades.filter { it.cropType == crop.name }
                    if (cropTrades.isNotEmpty()) {
                        FinancialDataPoint(
                            label = crop.displayName.split(" ").first(),
                            sublabel = "${cropTrades.size} trade(s)",
                            revenue = cropTrades.sumOf { it.totalRevenue },
                            procurementCost = cropTrades.sumOf { it.totalProcurementCost },
                            overheadCost = cropTrades.sumOf { it.totalOverhead },
                            netProfit = cropTrades.sumOf { it.netProfit },
                            quantityTons = cropTrades.sumOf { it.quantityTons },
                            cropType = crop.name,
                            laborCost = cropTrades.sumOf { it.laborPerQuintal * it.totalQuintals },
                            bagCost = cropTrades.sumOf { it.bagCostPerQuintal * it.totalQuintals },
                            freightCost = cropTrades.sumOf { it.transportPerQuintal * it.totalQuintals },
                            brokerageCost = cropTrades.sumOf { it.brokeragePerQuintal * it.totalQuintals }
                        )
                    } else null
                }.ifEmpty { generateSampleCommodityPoints() }
            }
            ChartViewMode.EXPENSE_STACK -> {
                if (trades.isEmpty()) {
                    generateSampleTradePoints()
                } else {
                    trades.takeLast(7).map { trade ->
                        FinancialDataPoint(
                            label = "T-${trade.id.toString().takeLast(3)}",
                            sublabel = trade.cropType,
                            revenue = trade.totalRevenue,
                            procurementCost = trade.totalProcurementCost,
                            overheadCost = trade.totalOverhead,
                            netProfit = trade.netProfit,
                            quantityTons = trade.quantityTons,
                            cropType = trade.cropType,
                            laborCost = trade.laborPerQuintal * trade.totalQuintals,
                            bagCost = trade.bagCostPerQuintal * trade.totalQuintals,
                            freightCost = trade.transportPerQuintal * trade.totalQuintals,
                            brokerageCost = trade.brokeragePerQuintal * trade.totalQuintals
                        )
                    }
                }
            }
        }
    }

    val maxVal = remember(chartDataPoints) {
        val highest = chartDataPoints.maxOfOrNull {
            maxOf(it.revenue, it.procurementCost + it.overheadCost, it.procurementCost, it.overheadCost)
        } ?: 100000.0
        if (highest <= 0.0) 100000.0 else highest * 1.18
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        shape = RoundedCornerShape(18.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(18.dp))
            .testTag("recharts_financial_visualization_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Header with Mode Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0284C7).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShowChart,
                            contentDescription = "Financial Visualizer",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Revenue vs Expense Analytics",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "Recharts-Engine: Procurement & Overhead vs Broker Revenue",
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                // Mode Pills
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E293B))
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    ChartModePill(
                        selected = viewMode == ChartViewMode.COMPARATIVE_BARS,
                        title = "Trades",
                        icon = Icons.Default.BarChart,
                        onClick = {
                            viewMode = ChartViewMode.COMPARATIVE_BARS
                            selectedIndex = null
                        }
                    )
                    ChartModePill(
                        selected = viewMode == ChartViewMode.COMMODITY_BREAKDOWN,
                        title = "Crops",
                        icon = Icons.Default.DonutLarge,
                        onClick = {
                            viewMode = ChartViewMode.COMMODITY_BREAKDOWN
                            selectedIndex = null
                        }
                    )
                    ChartModePill(
                        selected = viewMode == ChartViewMode.EXPENSE_STACK,
                        title = "Stack",
                        icon = Icons.Default.TrendingUp,
                        onClick = {
                            viewMode = ChartViewMode.EXPENSE_STACK
                            selectedIndex = null
                        }
                    )
                }
            }

            // 2. Interactive Series Legends (Togglable)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                InteractiveLegendItem(
                    label = "Revenue",
                    color = Color(0xFF10B981),
                    isActive = showRevenueSeries,
                    onClick = { showRevenueSeries = !showRevenueSeries }
                )
                InteractiveLegendItem(
                    label = "Procurement",
                    color = Color(0xFFF59E0B),
                    isActive = showProcurementSeries,
                    onClick = { showProcurementSeries = !showProcurementSeries }
                )
                InteractiveLegendItem(
                    label = "Overheads",
                    color = Color(0xFF38BDF8),
                    isActive = showOverheadSeries,
                    onClick = { showOverheadSeries = !showOverheadSeries }
                )
                InteractiveLegendItem(
                    label = "Net Margin",
                    color = Color(0xFFA855F7),
                    isActive = showProfitTrend,
                    onClick = { showProfitTrend = !showProfitTrend }
                )
            }

            // 3. Interactive Recharts Canvas Drawing Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0B1329))
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
                    .padding(top = 16.dp, bottom = 8.dp, start = 8.dp, end = 8.dp)
            ) {
                RechartsCanvas(
                    dataPoints = chartDataPoints,
                    maxVal = maxVal,
                    viewMode = viewMode,
                    showRevenue = showRevenueSeries,
                    showProcurement = showProcurementSeries,
                    showOverhead = showOverheadSeries,
                    showProfit = showProfitTrend,
                    selectedIndex = selectedIndex,
                    onSelectIndex = { selectedIndex = it }
                )
            }

            // 4. Detailed Selected Point Card / Tooltip
            val currentSelected = selectedIndex?.let { chartDataPoints.getOrNull(it) }
                ?: chartDataPoints.lastOrNull()

            if (currentSelected != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E293B),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${currentSelected.label}: ${currentSelected.sublabel}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF334155))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${currentSelected.quantityTons.toInt()} MT (${(currentSelected.quantityTons * 10).toInt()} Qtl)",
                                        fontSize = 9.sp,
                                        color = Color(0xFF38BDF8),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            val marginPct = if (currentSelected.revenue > 0) {
                                (currentSelected.netProfit / currentSelected.revenue) * 100.0
                            } else 0.0

                            Text(
                                text = "Margin: ${String.format(Locale.getDefault(), "%.1f", marginPct)}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (currentSelected.netProfit >= 0) Color(0xFF34D399) else Color(0xFFF87171)
                            )
                        }

                        Divider(color = Color(0xFF334155))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Revenue", fontSize = 10.sp, color = Color(0xFF94A3B8))
                                Text(
                                    text = inrFormat.format(currentSelected.revenue),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF10B981)
                                )
                            }
                            Column {
                                Text("Procurement (Farmers)", fontSize = 10.sp, color = Color(0xFF94A3B8))
                                Text(
                                    text = inrFormat.format(currentSelected.procurementCost),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF59E0B)
                                )
                            }
                            Column {
                                Text("Overhead Expenses", fontSize = 10.sp, color = Color(0xFF94A3B8))
                                Text(
                                    text = inrFormat.format(currentSelected.overheadCost),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF38BDF8)
                                )
                            }
                            Column {
                                Text("Net Margin", fontSize = 10.sp, color = Color(0xFF94A3B8))
                                Text(
                                    text = inrFormat.format(currentSelected.netProfit),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (currentSelected.netProfit >= 0) Color(0xFF34D399) else Color(0xFFF87171)
                                )
                            }
                        }

                        // Detailed Overhead Breakdown bar if available
                        if (currentSelected.overheadCost > 0) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF0F172A))
                                    .padding(horizontal = 6.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Labor: ₹${currentSelected.laborCost.toInt()}",
                                    fontSize = 9.sp,
                                    color = Color(0xFFF59E0B)
                                )
                                Text(
                                    text = "Bags: ₹${currentSelected.bagCost.toInt()}",
                                    fontSize = 9.sp,
                                    color = Color(0xFF38BDF8)
                                )
                                Text(
                                    text = "Freight: ₹${currentSelected.freightCost.toInt()}",
                                    fontSize = 9.sp,
                                    color = Color(0xFF10B981)
                                )
                                Text(
                                    text = "Brokerage: ₹${currentSelected.brokerageCost.toInt()}",
                                    fontSize = 9.sp,
                                    color = Color(0xFFEC4899)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChartModePill(
    selected: Boolean,
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (selected) Color(0xFF0284C7) else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (selected) Color.White else Color(0xFF94A3B8),
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) Color.White else Color(0xFF94A3B8)
            )
        }
    }
}

@Composable
private fun InteractiveLegendItem(
    label: String,
    color: Color,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(if (isActive) color else color.copy(alpha = 0.25f))
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isActive) Color(0xFFE2E8F0) else Color(0xFF64748B)
        )
    }
}

@Composable
private fun RechartsCanvas(
    dataPoints: List<FinancialDataPoint>,
    maxVal: Double,
    viewMode: ChartViewMode,
    showRevenue: Boolean,
    showProcurement: Boolean,
    showOverhead: Boolean,
    showProfit: Boolean,
    selectedIndex: Int?,
    onSelectIndex: (Int) -> Unit
) {
    val count = dataPoints.size
    if (count == 0) return

    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
        label = "recharts_anim"
    )

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .pointerInput(dataPoints) {
                detectTapGestures { offset ->
                    val availableWidth = size.width - 60f
                    val slotWidth = availableWidth / count
                    val clickedIndex = ((offset.x - 50f) / slotWidth).toInt().coerceIn(0, count - 1)
                    onSelectIndex(clickedIndex)
                }
            }
    ) {
        val width = size.width
        val height = size.height
        val leftPadding = 55f
        val bottomPadding = 30f
        val chartHeight = height - bottomPadding
        val chartWidth = width - leftPadding

        // 1. Gridlines and Y-axis intervals (4 horizontal grid levels)
        val gridLevels = 4
        for (i in 0..gridLevels) {
            val y = chartHeight - (chartHeight / gridLevels) * i
            drawLine(
                color = Color(0xFF1E293B).copy(alpha = 0.7f),
                start = Offset(leftPadding, y),
                end = Offset(width, y),
                strokeWidth = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
            )

            // Y-Axis currency labels
            val levelValue = (maxVal / gridLevels) * i
            val formatted = when {
                levelValue >= 10000000 -> "₹${String.format(Locale.getDefault(), "%.1f", levelValue / 10000000)}Cr"
                levelValue >= 100000 -> "₹${String.format(Locale.getDefault(), "%.1f", levelValue / 100000)}L"
                levelValue >= 1000 -> "₹${String.format(Locale.getDefault(), "%.0f", levelValue / 1000)}k"
                else -> "₹${levelValue.toInt()}"
            }
            // Draw axis guideline indicators
            drawCircle(
                color = Color(0xFF475569),
                radius = 2f,
                center = Offset(leftPadding - 4f, y)
            )
        }

        val slotWidth = chartWidth / count
        val profitPoints = mutableListOf<Offset>()

        dataPoints.forEachIndexed { index, dp ->
            val slotCenter = leftPadding + (index * slotWidth) + (slotWidth / 2f)
            val isSelected = selectedIndex == index

            // Selection highlight background strip
            if (isSelected) {
                drawRoundRect(
                    color = Color(0xFF38BDF8).copy(alpha = 0.12f),
                    topLeft = Offset(leftPadding + (index * slotWidth), 0f),
                    size = Size(slotWidth, chartHeight),
                    cornerRadius = CornerRadius(4f, 4f)
                )
            }

            when (viewMode) {
                ChartViewMode.COMPARATIVE_BARS, ChartViewMode.COMMODITY_BREAKDOWN -> {
                    val barWidth = (slotWidth * 0.22f).coerceAtMost(16f)
                    val spacing = 3f

                    // 1. Revenue Bar (Green)
                    if (showRevenue) {
                        val revHeight = ((dp.revenue / maxVal) * chartHeight * animatedProgress).toFloat()
                        val revX = slotCenter - barWidth - spacing
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF34D399), Color(0xFF059669))
                            ),
                            topLeft = Offset(revX, chartHeight - revHeight),
                            size = Size(barWidth, revHeight),
                            cornerRadius = CornerRadius(3f, 3f)
                        )
                    }

                    // 2. Procurement Cost Bar (Amber)
                    if (showProcurement) {
                        val procHeight = ((dp.procurementCost / maxVal) * chartHeight * animatedProgress).toFloat()
                        val procX = slotCenter
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFFFBBF24), Color(0xFFD97706))
                            ),
                            topLeft = Offset(procX, chartHeight - procHeight),
                            size = Size(barWidth, procHeight),
                            cornerRadius = CornerRadius(3f, 3f)
                        )
                    }

                    // 3. Overhead Cost Bar (Sky Blue)
                    if (showOverhead) {
                        val overHeight = ((dp.overheadCost / maxVal) * chartHeight * animatedProgress).toFloat()
                        val overX = slotCenter + barWidth + spacing
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF38BDF8), Color(0xFF0284C7))
                            ),
                            topLeft = Offset(overX, chartHeight - overHeight),
                            size = Size(barWidth, overHeight),
                            cornerRadius = CornerRadius(3f, 3f)
                        )
                    }
                }
                ChartViewMode.EXPENSE_STACK -> {
                    // Stacked bar: Total Procurement at base + Overhead stacked on top vs Revenue
                    val barWidth = (slotWidth * 0.38f).coerceAtMost(24f)
                    val barX = slotCenter - (barWidth / 2f)

                    val procHeight = ((dp.procurementCost / maxVal) * chartHeight * animatedProgress).toFloat()
                    val overHeight = ((dp.overheadCost / maxVal) * chartHeight * animatedProgress).toFloat()

                    // Procurement base
                    if (showProcurement) {
                        drawRoundRect(
                            brush = Brush.verticalGradient(listOf(Color(0xFFFBBF24), Color(0xFFD97706))),
                            topLeft = Offset(barX, chartHeight - procHeight),
                            size = Size(barWidth, procHeight),
                            cornerRadius = CornerRadius(2f, 2f)
                        )
                    }

                    // Overhead top stack
                    if (showOverhead) {
                        drawRoundRect(
                            brush = Brush.verticalGradient(listOf(Color(0xFF38BDF8), Color(0xFF0284C7))),
                            topLeft = Offset(barX, chartHeight - procHeight - overHeight),
                            size = Size(barWidth, overHeight),
                            cornerRadius = CornerRadius(2f, 2f)
                        )
                    }

                    // Revenue baseline marker
                    if (showRevenue) {
                        val revY = chartHeight - ((dp.revenue / maxVal) * chartHeight * animatedProgress).toFloat()
                        drawLine(
                            color = Color(0xFF10B981),
                            start = Offset(barX - 4f, revY),
                            end = Offset(barX + barWidth + 4f, revY),
                            strokeWidth = 3f,
                            cap = StrokeCap.Round
                        )
                    }
                }
            }

            // Record Profit Trend point for spline/line overlay
            val profitY = chartHeight - (((dp.netProfit.coerceAtLeast(0.0)) / maxVal) * chartHeight * animatedProgress).toFloat()
            profitPoints.add(Offset(slotCenter, profitY))
        }

        // 4. Net Profit Line & Glow Points (Recharts Line Series)
        if (showProfit && profitPoints.size > 1) {
            val linePath = Path()
            profitPoints.forEachIndexed { i, pt ->
                if (i == 0) linePath.moveTo(pt.x, pt.y)
                else linePath.lineTo(pt.x, pt.y)
            }

            // Draw connecting line
            drawPath(
                path = linePath,
                color = Color(0xFFA855F7),
                style = Stroke(width = 2.5f, cap = StrokeCap.Round)
            )

            // Draw node circles
            profitPoints.forEachIndexed { i, pt ->
                val isSelected = selectedIndex == i
                drawCircle(
                    color = if (isSelected) Color.White else Color(0xFFA855F7),
                    radius = if (isSelected) 5f else 3.5f,
                    center = pt
                )
                drawCircle(
                    color = Color(0xFF0B1329),
                    radius = if (isSelected) 2.5f else 1.8f,
                    center = pt
                )
            }
        }

        // 5. Baseline X-Axis
        drawLine(
            color = Color(0xFF334155),
            start = Offset(leftPadding, chartHeight),
            end = Offset(width, chartHeight),
            strokeWidth = 1.5f
        )
    }
}

private fun generateSampleTradePoints(): List<FinancialDataPoint> {
    return listOf(
        FinancialDataPoint("T-101", "Garg Agro (Maize)", 525000.0, 437500.0, 22500.0, 65000.0, 25.0, "MAIZE", 4500.0, 6250.0, 8750.0, 3000.0),
        FinancialDataPoint("T-102", "Patanjali (Wheat)", 840000.0, 717500.0, 31500.0, 91000.0, 35.0, "WHEAT", 6300.0, 8750.0, 12250.0, 4200.0),
        FinancialDataPoint("T-103", "Adani Wilmar (Soy)", 1260000.0, 1080000.0, 40500.0, 139500.0, 45.0, "SOYBEAN", 8100.0, 11250.0, 15750.0, 5400.0),
        FinancialDataPoint("T-104", "ITC Foods (Paddy)", 680000.0, 580000.0, 27000.0, 73000.0, 30.0, "PADDY", 5400.0, 7500.0, 10500.0, 3600.0),
        FinancialDataPoint("T-105", "Marico Oil (Mustard)", 960000.0, 816000.0, 36000.0, 108000.0, 40.0, "MUSTARD", 7200.0, 10000.0, 14000.0, 4800.0)
    )
}

private fun generateSampleCommodityPoints(): List<FinancialDataPoint> {
    return listOf(
        FinancialDataPoint("Maize", "3 trades", 1450000.0, 1220000.0, 65000.0, 165000.0, 70.0, "MAIZE"),
        FinancialDataPoint("Wheat", "4 trades", 2100000.0, 1780000.0, 92000.0, 228000.0, 100.0, "WHEAT"),
        FinancialDataPoint("Soybean", "5 trades", 3200000.0, 2720000.0, 138000.0, 342000.0, 150.0, "SOYBEAN"),
        FinancialDataPoint("Paddy", "2 trades", 1200000.0, 1020000.0, 54000.0, 126000.0, 60.0, "PADDY"),
        FinancialDataPoint("Mustard", "2 trades", 1600000.0, 1360000.0, 68000.0, 172000.0, 80.0, "MUSTARD")
    )
}
