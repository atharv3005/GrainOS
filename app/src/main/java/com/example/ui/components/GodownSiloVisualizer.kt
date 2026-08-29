package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GodownEntity

@Composable
fun GodownSiloVisualizer(
    godowns: List<GodownEntity>,
    activeCropColor: Color,
    onGodownSelected: (GodownEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val totalCapacity = godowns.sumOf { it.capacityMt }
    val totalStock = godowns.sumOf { it.currentStockMt }
    val totalAvailable = (totalCapacity - totalStock).coerceAtLeast(0.0)
    val overallFillPct = if (totalCapacity > 0) (totalStock / totalCapacity).toFloat() else 0f

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E293B).copy(alpha = 0.9f),
                        Color(0xFF0F172A).copy(alpha = 0.98f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = activeCropColor.copy(alpha = 0.35f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(18.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(activeCropColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warehouse,
                        contentDescription = "Warehouse",
                        tint = activeCropColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Warehouse & Silos Visualizer",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = Color(0xFFF8FAFC)
                    )
                    Text(
                        text = "Live telemetry & grain capacity distribution",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            // Overall Fill Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(activeCropColor.copy(alpha = 0.15f))
                    .border(1.dp, activeCropColor.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${(overallFillPct * 100).toInt()}% UTILIZED",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp
                    ),
                    color = activeCropColor
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Summary metric pills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StockStatPill(
                label = "Total In-Store",
                value = "${(totalStock * 10).toLong() / 10.0} MT",
                color = activeCropColor,
                modifier = Modifier.weight(1f)
            )
            StockStatPill(
                label = "Available Space",
                value = "${(totalAvailable * 10).toLong() / 10.0} MT",
                color = Color(0xFF38BDF8),
                modifier = Modifier.weight(1f)
            )
            StockStatPill(
                label = "Total Capacity",
                value = "${totalCapacity.toInt()} MT",
                color = Color(0xFF94A3B8),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Interactive Silos Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            godowns.forEach { godown ->
                SingleSiloColumn(
                    godown = godown,
                    accentColor = when (godown.godownId) {
                        "GODOWN_A" -> Color(0xFFF59E0B)
                        "GODOWN_B" -> Color(0xFFD97706)
                        "GODOWN_C" -> Color(0xFF10B981)
                        else -> Color(0xFFEAB308)
                    },
                    onClicked = { onGodownSelected(godown) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StockStatPill(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF0F172A).copy(alpha = 0.8f))
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = Color(0xFF94A3B8)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                ),
                color = color
            )
        }
    }
}

@Composable
private fun SingleSiloColumn(
    godown: GodownEntity,
    accentColor: Color,
    onClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fillRatio = if (godown.capacityMt > 0) {
        (godown.currentStockMt / godown.capacityMt).toFloat().coerceIn(0f, 1f)
    } else 0f

    val animatedHeight by animateFloatAsState(
        targetValue = fillRatio,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "silo_fill"
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF0B1017).copy(alpha = 0.7f))
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(14.dp))
            .clickable { onClicked() }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Silo Dome & Tank Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 8.dp, bottomEnd = 8.dp))
                .background(Color(0xFF1E293B))
                .border(
                    width = 1.dp,
                    color = accentColor.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
                ),
            contentAlignment = Alignment.BottomCenter
        ) {
            // Background Grid Lines on Silo
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                repeat(4) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color.White.copy(alpha = 0.07f))
                    )
                }
            }

            // Animated Grain Level Fill
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(animatedHeight)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                accentColor,
                                accentColor.copy(alpha = 0.75f)
                            )
                        )
                    )
            )

            // Percentage Label inside Silo
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${(fillRatio * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp
                    ),
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Title
        val shortName = when (godown.godownId) {
            "GODOWN_A" -> "Godown A"
            "GODOWN_B" -> "Godown B"
            "GODOWN_C" -> "Godown C"
            else -> "Drying Yard"
        }
        Text(
            text = shortName,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            ),
            color = Color(0xFFF8FAFC)
        )

        // Weight
        Text(
            text = "${godown.currentStockMt.toInt()} / ${godown.capacityMt.toInt()} MT",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = accentColor
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Mini telemetry chips
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Opacity,
                contentDescription = "Moisture",
                tint = Color(0xFF38BDF8),
                modifier = Modifier.size(11.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = "${godown.averageMoisture}%",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = Color(0xFF94A3B8)
            )
        }
    }
}
