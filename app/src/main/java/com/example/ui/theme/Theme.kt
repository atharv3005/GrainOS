package com.example.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.data.model.CropType

tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

private val DarkColorScheme = darkColorScheme(
    primary = GrainGreenLight,
    onPrimary = Color.Black,
    primaryContainer = GrainGreenMedium,
    onPrimaryContainer = TextPrimary,
    secondary = MaizeGold,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF374151),
    onSecondaryContainer = TextPrimary,
    tertiary = GrainGreenAccent,
    background = DarkSurface,
    onBackground = TextPrimary,
    surface = DarkSurfaceElevated,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceCard,
    onSurfaceVariant = TextSecondary,
    outline = DarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = GrainGreenDark,
    onPrimary = Color.White,
    primaryContainer = GrainGreenLight.copy(alpha = 0.2f),
    onPrimaryContainer = GrainGreenDark,
    secondary = MaizeAmber,
    onSecondary = Color.White,
    tertiary = GrainGreenAccent,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1)
)

@Composable
fun GrainWmsTheme(
    darkTheme: Boolean = true, // default to sleek high-tech dark theme
    activeCrop: CropType = CropType.MAIZE,
    content: @Composable () -> Unit
) {
    val dynamicColors = if (darkTheme) {
        DarkColorScheme.copy(
            primary = activeCrop.primaryColor,
            secondary = activeCrop.secondaryColor,
            tertiary = activeCrop.accentColor,
            background = activeCrop.darkBackground,
            surface = Color(0xFF131E18)
        )
    } else {
        LightColorScheme.copy(
            primary = activeCrop.primaryColor,
            secondary = activeCrop.secondaryColor,
            tertiary = activeCrop.accentColor
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = view.context.findActivity()?.window
            if (window != null) {
                window.statusBarColor = dynamicColors.background.toArgb()
                window.navigationBarColor = dynamicColors.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = dynamicColors,
        typography = Typography,
        content = content
    )
}

// Backward compatibility with template
@Composable
fun MyApplicationTheme(content: @Composable () -> Unit) {
    GrainWmsTheme(content = content)
}
