package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun KpiWidget(
    title: String,
    value: String,
    unit: String = "",
    subtitle: String? = null,
    icon: ImageVector,
    accentColor: Color,
    trendText: String? = null,
    isPositiveTrend: Boolean = true,
    progress: Float? = null,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress ?: 0f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "kpi_progress"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E293B).copy(alpha = 0.85f),
                        Color(0xFF0F172A).copy(alpha = 0.95f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.5f),
                        Color.White.copy(alpha = 0.08f)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(14.dp)
    ) {
        Column {
            // Header Row: Icon + Title + Trend Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(accentColor.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.8.sp
                        ),
                        color = Color(0xFF94A3B8)
                    )
                }

                if (trendText != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isPositiveTrend) Color(0xFF10B981).copy(alpha = 0.15f)
                                else Color(0xFFEF4444).copy(alpha = 0.15f)
                            )
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = trendText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (isPositiveTrend) Color(0xFF34D399) else Color(0xFFF87171)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Value + Unit
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.SansSerif
                    ),
                    color = Color(0xFFF8FAFC)
                )
                if (unit.isNotBlank()) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = accentColor,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }

            // Progress Bar if applicable
            if (progress != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF334155))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress.coerceIn(0f, 1f))
                            .height(5.dp)
                            .clip(CircleShape)
                            .background(accentColor)
                    )
                }
            }

            // Subtitle
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = Color(0xFF94A3B8)
                )
            }
        }
    }
}
