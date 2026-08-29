package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import com.example.data.model.CropType
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class GrainParticle(
    val id: Int,
    val xRatio: Float,
    val yRatio: Float,
    val size: Float,
    val speed: Float,
    val rotationSpeed: Float,
    val initialAngle: Float,
    val opacity: Float,
    val seedVal: Float
)

@Composable
fun Dynamic3DGrainBackground(
    activeCrop: CropType,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "grain_bg_animation")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time_ticker"
    )

    // Smooth color-shifting animation when user switches crop configuration
    val animatedBaseColor by animateColorAsState(
        targetValue = activeCrop.darkBackground,
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "base_color_anim"
    )
    val animatedPrimaryColor by animateColorAsState(
        targetValue = activeCrop.primaryColor,
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "primary_color_anim"
    )
    val animatedSecondaryColor by animateColorAsState(
        targetValue = activeCrop.secondaryColor,
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "secondary_color_anim"
    )
    val animatedAccentColor by animateColorAsState(
        targetValue = activeCrop.accentColor,
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "accent_color_anim"
    )

    val particles = remember {
        List(30) { i ->
            val rand = Random(i * 997 + 13)
            GrainParticle(
                id = i,
                xRatio = rand.nextFloat(),
                yRatio = rand.nextFloat(),
                size = rand.nextFloat() * 12f + 8f,
                speed = rand.nextFloat() * 0.4f + 0.2f,
                rotationSpeed = (rand.nextFloat() - 0.5f) * 1.5f,
                initialAngle = rand.nextFloat() * 360f,
                opacity = rand.nextFloat() * 0.35f + 0.12f,
                seedVal = rand.nextFloat() * 100f
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // 1. Subtle Radial Ambient Lighting
        val ambientBrush = Brush.radialGradient(
            colors = listOf(
                animatedPrimaryColor.copy(alpha = 0.12f),
                animatedSecondaryColor.copy(alpha = 0.05f),
                animatedBaseColor
            ),
            center = Offset(width * 0.7f, height * 0.2f),
            radius = width * 1.2f
        )
        drawRect(brush = ambientBrush, size = size)

        // 2. High-Tech Grid overlay
        drawSubtleGrid(width, height, animatedPrimaryColor)

        // 3. Render 3D Crop-specific Animated Elements efficiently
        when (activeCrop) {
            CropType.MAIZE -> {
                drawMaizeCornElements(particles, time, animatedPrimaryColor, animatedAccentColor, width, height)
            }
            CropType.WHEAT -> {
                drawWheatSpikeElements(particles, time, animatedPrimaryColor, animatedAccentColor, width, height)
            }
            CropType.SOYBEAN -> {
                drawSoybeanElements(particles, time, animatedPrimaryColor, animatedAccentColor, width, height)
            }
            CropType.PADDY -> {
                drawPaddyRiceElements(particles, time, animatedPrimaryColor, animatedAccentColor, width, height)
            }
            CropType.MUSTARD -> {
                drawMustardSeedElements(particles, time, animatedPrimaryColor, animatedAccentColor, width, height)
            }
        }
    }
}

private fun DrawScope.drawSubtleGrid(width: Float, height: Float, color: Color) {
    val step = 80f
    var x = 0f
    while (x < width) {
        drawLine(
            color = color.copy(alpha = 0.02f),
            start = Offset(x, 0f),
            end = Offset(x, height),
            strokeWidth = 1f
        )
        x += step
    }
    var y = 0f
    while (y < height) {
        drawLine(
            color = color.copy(alpha = 0.02f),
            start = Offset(0f, y),
            end = Offset(width, y),
            strokeWidth = 1f
        )
        y += step
    }
}

private fun DrawScope.drawMaizeCornElements(
    particles: List<GrainParticle>,
    time: Float,
    primaryColor: Color,
    accentColor: Color,
    width: Float,
    height: Float
) {
    particles.forEach { p ->
        val yOffset = ((p.yRatio * height + (time * p.speed * 20f)) % height)
        val xOffset = p.xRatio * width + sin((time + p.seedVal) * 0.02f) * 20f
        val angle = p.initialAngle + time * p.rotationSpeed

        rotate(degrees = angle, pivot = Offset(xOffset, yOffset)) {
            // Stylized 3D Golden Maize Kernel
            drawRoundRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFEF08A).copy(alpha = p.opacity),
                        primaryColor.copy(alpha = p.opacity),
                        Color(0xFFB45309).copy(alpha = p.opacity * 0.7f)
                    ),
                    center = Offset(xOffset, yOffset),
                    radius = p.size
                ),
                topLeft = Offset(xOffset - p.size / 2, yOffset - p.size / 1.5f),
                size = Size(p.size, p.size * 1.3f),
                cornerRadius = CornerRadius(p.size * 0.4f, p.size * 0.4f)
            )
        }
    }
}

private fun DrawScope.drawWheatSpikeElements(
    particles: List<GrainParticle>,
    time: Float,
    primaryColor: Color,
    accentColor: Color,
    width: Float,
    height: Float
) {
    particles.forEach { p ->
        val yOffset = ((p.yRatio * height + (time * p.speed * 15f)) % height)
        val xOffset = p.xRatio * width + cos((time + p.seedVal) * 0.02f) * 15f
        val angle = p.initialAngle + time * p.rotationSpeed

        rotate(degrees = angle, pivot = Offset(xOffset, yOffset)) {
            // Sleek golden wheat grain
            drawOval(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFDE68A).copy(alpha = p.opacity),
                        primaryColor.copy(alpha = p.opacity),
                        Color(0xFF92400E).copy(alpha = p.opacity * 0.5f)
                    )
                ),
                topLeft = Offset(xOffset - p.size * 0.4f, yOffset - p.size),
                size = Size(p.size * 0.8f, p.size * 2f)
            )
            // Center grain groove
            drawLine(
                color = Color(0xFF78350F).copy(alpha = p.opacity * 0.6f),
                start = Offset(xOffset, yOffset - p.size * 0.8f),
                end = Offset(xOffset, yOffset + p.size * 0.8f),
                strokeWidth = 1.5f
            )
        }
    }
}

private fun DrawScope.drawSoybeanElements(
    particles: List<GrainParticle>,
    time: Float,
    primaryColor: Color,
    accentColor: Color,
    width: Float,
    height: Float
) {
    particles.forEach { p ->
        val yOffset = ((p.yRatio * height + (time * p.speed * 18f)) % height)
        val xOffset = p.xRatio * width + sin((time + p.seedVal) * 0.03f) * 25f
        val angle = p.initialAngle + time * p.rotationSpeed

        rotate(degrees = angle, pivot = Offset(xOffset, yOffset)) {
            // Smooth spherical / oval soybean
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFA7F3D0).copy(alpha = p.opacity),
                        primaryColor.copy(alpha = p.opacity),
                        Color(0xFF065F46).copy(alpha = p.opacity * 0.6f)
                    ),
                    center = Offset(xOffset - p.size * 0.2f, yOffset - p.size * 0.2f),
                    radius = p.size
                ),
                radius = p.size * 0.7f,
                center = Offset(xOffset, yOffset)
            )
        }
    }
}

private fun DrawScope.drawPaddyRiceElements(
    particles: List<GrainParticle>,
    time: Float,
    primaryColor: Color,
    accentColor: Color,
    width: Float,
    height: Float
) {
    particles.forEach { p ->
        val yOffset = ((p.yRatio * height + (time * p.speed * 16f)) % height)
        val xOffset = p.xRatio * width + cos((time + p.seedVal) * 0.02f) * 18f
        val angle = p.initialAngle + time * p.rotationSpeed

        rotate(degrees = angle, pivot = Offset(xOffset, yOffset)) {
            // Slender curved paddy grain
            drawOval(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFEF3C7).copy(alpha = p.opacity),
                        primaryColor.copy(alpha = p.opacity),
                        Color(0xFFB45309).copy(alpha = p.opacity * 0.5f)
                    )
                ),
                topLeft = Offset(xOffset - p.size * 0.3f, yOffset - p.size * 1.2f),
                size = Size(p.size * 0.6f, p.size * 2.4f)
            )
        }
    }
}

private fun DrawScope.drawMustardSeedElements(
    particles: List<GrainParticle>,
    time: Float,
    primaryColor: Color,
    accentColor: Color,
    width: Float,
    height: Float
) {
    particles.forEach { p ->
        val yOffset = ((p.yRatio * height + (time * p.speed * 22f)) % height)
        val xOffset = p.xRatio * width + sin((time + p.seedVal) * 0.04f) * 12f

        // Tiny glowing mustard seed
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFEF08A).copy(alpha = p.opacity),
                    primaryColor.copy(alpha = p.opacity),
                    Color(0xFF713F12).copy(alpha = p.opacity * 0.8f)
                ),
                center = Offset(xOffset, yOffset),
                radius = p.size * 0.5f
            ),
            radius = p.size * 0.45f,
            center = Offset(xOffset, yOffset)
        )
    }
}
