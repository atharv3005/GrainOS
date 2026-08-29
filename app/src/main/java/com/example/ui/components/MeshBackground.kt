package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun MeshBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)) // Deep Navy Base
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Deep purple gradient sphere
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF312E81).copy(alpha = 0.5f), // Subtle deep purple
                        Color.Transparent
                    ),
                    center = Offset(width * 0.2f, height * 0.3f),
                    radius = width * 0.8f
                ),
                radius = width * 0.8f,
                center = Offset(width * 0.2f, height * 0.3f)
            )

            // Dark teal gradient sphere
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF064E3B).copy(alpha = 0.4f), // Faint dark teal
                        Color.Transparent
                    ),
                    center = Offset(width * 0.8f, height * 0.7f),
                    radius = width * 0.9f
                ),
                radius = width * 0.9f,
                center = Offset(width * 0.8f, height * 0.7f)
            )
        }
        content()
    }
}
