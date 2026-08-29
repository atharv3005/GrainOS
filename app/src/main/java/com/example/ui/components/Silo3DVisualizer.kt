package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CropType
import com.example.data.model.GodownEntity
import java.text.NumberFormat
import java.util.Locale

/**
 * 3D Grain Silo Storage Capacity Visualizer.
 * Renders a rich 3D-styled metallic silo filling with grain based on total warehouse stock vs capacity,
 * with subtle crop textures, dynamic color shifts, and real-time occupancy stats.
 */
@Composable
fun Silo3DVisualizerCard(
    activeCrop: CropType,
    godowns: List<GodownEntity>,
    liveGodownStockLedger: Map<String, Double> = emptyMap(),
    totalCapacityMt: Double = 5000.0,
    modifier: Modifier = Modifier
) {
    val totalStockMt = if (liveGodownStockLedger.isNotEmpty()) {
        godowns.sumOf { liveGodownStockLedger[it.godownId] ?: 0.0 }
    } else {
        godowns.sumOf { it.currentStockMt }
    }
    val effectiveCapacity = if (totalCapacityMt > 0) totalCapacityMt else 5000.0
    val fillRatio = (totalStockMt / effectiveCapacity).coerceIn(0.0, 1.0).toFloat()

    val animatedFillRatio by animateFloatAsState(
        targetValue = fillRatio,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "silo_fill_ratio"
    )

    val animatedCropColor by animateColorAsState(
        targetValue = activeCrop.primaryColor,
        animationSpec = tween(650, easing = FastOutSlowInEasing),
        label = "crop_color_anim"
    )

    val animatedContainerBorder by animateColorAsState(
        targetValue = activeCrop.primaryColor.copy(alpha = 0.35f),
        animationSpec = tween(650, easing = FastOutSlowInEasing),
        label = "crop_border_anim"
    )

    val inrFormat = NumberFormat.getNumberInstance(Locale("en", "IN"))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(20.dp), spotColor = animatedCropColor.copy(alpha = 0.25f)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = androidx.compose.foundation.BorderStroke(1.dp, animatedContainerBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(animatedCropColor.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warehouse,
                            contentDescription = "Warehouse Silo",
                            tint = animatedCropColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "3D Storage Silo Capacity",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            ),
                            color = Color.White
                        )
                        Text(
                            text = "Live Vault Utilization • ${activeCrop.displayName}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                // Percentage Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(animatedCropColor.copy(alpha = 0.2f))
                        .border(1.dp, animatedCropColor.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${(animatedFillRatio * 100).toInt()}% Full",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp
                        ),
                        color = animatedCropColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3D Silo & Metric Breakdown Layout
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 3D Animated Silo Canvas (Left)
                Box(
                    modifier = Modifier
                        .width(110.dp)
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(width = 100.dp, height = 155.dp)) {
                        draw3DSilo(
                            fillRatio = animatedFillRatio,
                            grainColor = animatedCropColor,
                            cropType = activeCrop
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Breakdown Statistics (Right)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Metric 1: Current Stock
                    SiloMetricRow(
                        label = "Current Stock in Godowns",
                        value = "${inrFormat.format(totalStockMt.toInt())} MT",
                        subvalue = "(${inrFormat.format((totalStockMt * 10).toInt())} Quintals)",
                        accentColor = animatedCropColor
                    )

                    // Metric 2: Available Empty Space
                    val emptySpace = (effectiveCapacity - totalStockMt).coerceAtLeast(0.0)
                    SiloMetricRow(
                        label = "Vacant Holding Capacity",
                        value = "${inrFormat.format(emptySpace.toInt())} MT",
                        subvalue = "Ready for Inbound Lots",
                        accentColor = Color(0xFF38BDF8)
                    )

                    // Metric 3: Active Godowns Count
                    SiloMetricRow(
                        label = "Active Godowns / Yards",
                        value = "${godowns.size} Storage Bays",
                        subvalue = "Main Silos + Drying Beds",
                        accentColor = Color(0xFF10B981)
                    )
                }
            }
        }
    }
}

@Composable
private fun SiloMetricRow(
    label: String,
    value: String,
    subvalue: String,
    accentColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF0F172A).copy(alpha = 0.6f))
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = Color(0xFF94A3B8)
                )
                Text(
                    text = subvalue,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = Color(0xFF64748B)
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                ),
                color = accentColor
            )
        }
    }
}

/**
 * Custom 3D Silo Drawing Logic on Canvas
 */
private fun DrawScope.draw3DSilo(
    fillRatio: Float,
    grainColor: Color,
    cropType: CropType
) {
    val w = size.width
    val h = size.height

    val siloLeft = w * 0.15f
    val siloWidth = w * 0.70f
    val siloRight = siloLeft + siloWidth
    val siloTop = h * 0.22f
    val siloHeight = h * 0.68f
    val siloBottom = siloTop + siloHeight

    // 1. Silo Base Shadow / Ground Reflection
    drawOval(
        color = Color(0xFF000000).copy(alpha = 0.5f),
        topLeft = Offset(siloLeft - w * 0.05f, siloBottom - h * 0.04f),
        size = Size(siloWidth * 1.1f, h * 0.08f)
    )

    // 2. Silo Outer Metal Cylinder Background (Empty hollow container)
    drawRoundRect(
        brush = Brush.horizontalGradient(
            colors = listOf(
                Color(0xFF1E293B),
                Color(0xFF334155),
                Color(0xFF475569),
                Color(0xFF1E293B)
            ),
            startX = siloLeft,
            endX = siloRight
        ),
        topLeft = Offset(siloLeft, siloTop),
        size = Size(siloWidth, siloHeight),
        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
    )

    // Horizontal Steel Ribs / Corrugation Rings
    val ribCount = 6
    for (i in 1..ribCount) {
        val ribY = siloTop + (siloHeight / (ribCount + 1)) * i
        drawLine(
            color = Color(0xFF64748B).copy(alpha = 0.4f),
            start = Offset(siloLeft, ribY),
            end = Offset(siloRight, ribY),
            strokeWidth = 2f
        )
    }

    // 3. Grain Filling Level (3D Liquid / Granular Fill)
    if (fillRatio > 0.02f) {
        val fillHeight = siloHeight * fillRatio
        val fillTop = siloBottom - fillHeight

        val grainPath = Path().apply {
            moveTo(siloLeft + 3f, siloBottom - 3f)
            lineTo(siloLeft + 3f, fillTop + 6f)
            // Curved meniscus on top
            quadraticTo(
                siloLeft + siloWidth / 2f,
                fillTop - 4f,
                siloRight - 3f,
                fillTop + 6f
            )
            lineTo(siloRight - 3f, siloBottom - 3f)
            close()
        }

        // Grain Body with 3D cylindrical lighting
        drawPath(
            path = grainPath,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    grainColor.copy(alpha = 0.75f),
                    grainColor,
                    grainColor.copy(alpha = 0.95f),
                    grainColor.copy(alpha = 0.65f)
                ),
                startX = siloLeft,
                endX = siloRight
            )
        )

        // Top surface oval / grain mound highlight
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.4f),
                    grainColor,
                    grainColor.copy(alpha = 0.8f)
                ),
                center = Offset(siloLeft + siloWidth / 2f, fillTop + 4f),
                radius = siloWidth / 2f
            ),
            topLeft = Offset(siloLeft + 4f, fillTop - 2f),
            size = Size(siloWidth - 8f, 12.dp.toPx())
        )

        // Subtle Grain Granular Speckles
        val speckleColor = Color.White.copy(alpha = 0.3f)
        val speckleCount = (12 * fillRatio).toInt()
        for (i in 0 until speckleCount) {
            val sx = siloLeft + 8f + ((i * 19) % (siloWidth - 16f).toInt())
            val sy = fillTop + 10f + ((i * 23) % (fillHeight - 14f).toInt().coerceAtLeast(1))
            drawCircle(
                color = speckleColor,
                radius = 1.5f,
                center = Offset(sx, sy)
            )
        }
    }

    // 4. Silo Dome Roof (Conical Roof in 3D)
    val domePath = Path().apply {
        moveTo(siloLeft, siloTop)
        quadraticTo(
            siloLeft + siloWidth / 2f,
            siloTop - h * 0.18f,
            siloRight,
            siloTop
        )
        close()
    }
    drawPath(
        path = domePath,
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFFE2E8F0), Color(0xFF94A3B8), Color(0xFF475569)),
            startY = siloTop - h * 0.18f,
            endY = siloTop
        )
    )

    // Silo Roof Edge Highlight Ring
    drawPath(
        path = domePath,
        color = Color(0xFFCBD5E1),
        style = Stroke(width = 1.5f)
    )

    // Silo Top Aeration Vent / Cap
    drawRect(
        color = Color(0xFF10B981),
        topLeft = Offset(siloLeft + siloWidth * 0.44f, siloTop - h * 0.20f),
        size = Size(siloWidth * 0.12f, h * 0.05f)
    )

    // 5. Vertical Level Indicator Gauge Ladder (Glass Window Tube on right)
    val gaugeLeft = siloRight - 10.dp.toPx()
    val gaugeWidth = 4.dp.toPx()
    drawRoundRect(
        color = Color(0xFF0F172A).copy(alpha = 0.8f),
        topLeft = Offset(gaugeLeft, siloTop + 8f),
        size = Size(gaugeWidth, siloHeight - 16f),
        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
    )
    if (fillRatio > 0.05f) {
        val gFillH = (siloHeight - 16f) * fillRatio
        drawRoundRect(
            color = grainColor,
            topLeft = Offset(gaugeLeft, siloBottom - 8f - gFillH),
            size = Size(gaugeWidth, gFillH),
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
        )
    }

    // 6. Cylindrical Specular Glare (Vertical light reflection strip across cylinder)
    drawLine(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.35f),
                Color.White.copy(alpha = 0.15f),
                Color.Transparent
            )
        ),
        start = Offset(siloLeft + siloWidth * 0.32f, siloTop + 4f),
        end = Offset(siloLeft + siloWidth * 0.32f, siloBottom - 4f),
        strokeWidth = 3f
    )
}
