package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CropType

/**
 * Premium Agricultural Brand Logo for GrainOS.
 * Conceptual design: Stylized golden grain stalks intertwined with a modern geometric "O" and glowing emerald leaf accents.
 */
@Composable
fun GrainOSBrandLogo(
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    activeCrop: CropType = CropType.MAIZE,
    showText: Boolean = false,
    subtitle: String? = null
) {
    val animatedAccent by animateColorAsState(
        targetValue = activeCrop.primaryColor,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "brand_accent"
    )

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .shadow(8.dp, RoundedCornerShape(12.dp), spotColor = animatedAccent)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF1E293B),
                            Color(0xFF0F172A)
                        )
                    )
                )
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFBBF24),
                            animatedAccent,
                            Color(0xFF10B981).copy(alpha = 0.6f)
                        )
                    ),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(size * 0.85f)) {
                drawGoldenGrainStalksWithModernO(animatedAccent)
            }
        }

        if (showText) {
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Grain",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp,
                            letterSpacing = 0.5.sp
                        ),
                        color = Color.White
                    )
                    Text(
                        text = "OS",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp,
                            letterSpacing = 0.5.sp
                        ),
                        color = Color(0xFFF59E0B)
                    )
                }
                Text(
                    text = subtitle ?: "Enterprise Maharashtra Agri ERP",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = Color(0xFF94A3B8),
                    maxLines = 1
                )
            }
        }
    }
}

private fun DrawScope.drawGoldenGrainStalksWithModernO(accentColor: Color) {
    val w = size.width
    val h = size.height
    val center = Offset(w / 2f, h / 2f)

    // 1. Central Modern "O" Geometric Ring (3D Chamfered Torus Ring)
    val ringRadius = w * 0.28f
    val ringStroke = w * 0.12f

    // Outer glow aura
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFF59E0B).copy(alpha = 0.35f), Color.Transparent),
            center = center,
            radius = ringRadius * 1.5f
        ),
        radius = ringRadius * 1.5f,
        center = center
    )

    // Modern "O" Ring
    drawCircle(
        brush = Brush.sweepGradient(
            colors = listOf(
                Color(0xFFFDE047),
                Color(0xFFF59E0B),
                Color(0xFFD97706),
                Color(0xFFFDE047)
            ),
            center = center
        ),
        radius = ringRadius,
        center = center,
        style = Stroke(width = ringStroke, cap = StrokeCap.Round)
    )

    // 2. Stylized Golden Grain Stalk Arcing Upwards through the "O"
    val leftStalk = Path().apply {
        moveTo(w * 0.24f, h * 0.88f)
        cubicTo(
            w * 0.15f, h * 0.55f,
            w * 0.30f, h * 0.25f,
            w * 0.50f, h * 0.12f
        )
    }
    drawPath(
        path = leftStalk,
        color = Color(0xFFF59E0B),
        style = Stroke(width = 2.5f, cap = StrokeCap.Round)
    )

    // 3. Golden Wheat Kernels along the arc
    val kernels = listOf(
        Pair(Offset(w * 0.50f, h * 0.12f), 0f),
        Pair(Offset(w * 0.40f, h * 0.20f), -30f),
        Pair(Offset(w * 0.58f, h * 0.22f), 30f),
        Pair(Offset(w * 0.32f, h * 0.34f), -35f),
        Pair(Offset(w * 0.52f, h * 0.36f), 35f),
        Pair(Offset(w * 0.26f, h * 0.50f), -40f),
        Pair(Offset(w * 0.44f, h * 0.52f), 40f)
    )

    kernels.forEachIndexed { idx, (pos, _) ->
        val kSize = if (idx == 0) w * 0.13f else w * 0.11f
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFEF08A),
                    Color(0xFFF59E0B),
                    Color(0xFFB45309)
                ),
                center = pos,
                radius = kSize
            ),
            topLeft = Offset(pos.x - kSize / 2f, pos.y - kSize / 1.6f),
            size = Size(kSize, kSize * 1.35f)
        )
    }

    // 4. Base Sprout Leaves (Green Emerald accents)
    val leafPath = Path().apply {
        moveTo(w * 0.35f, h * 0.85f)
        quadraticTo(w * 0.50f, h * 0.72f, w * 0.65f, h * 0.78f)
        quadraticTo(w * 0.52f, h * 0.90f, w * 0.35f, h * 0.85f)
        close()
    }
    drawPath(
        path = leafPath,
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFF34D399), Color(0xFF059669))
        )
    )
}
