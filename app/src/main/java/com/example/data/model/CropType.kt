package com.example.data.model

import androidx.compose.ui.graphics.Color

enum class CropType(
    val displayName: String,
    val hindiName: String,
    val standardMsp: Double, // in ₹/quintal
    val idealMoisture: Double, // %
    val maxSafeMoisture: Double, // %
    val primaryColorHex: Long,
    val secondaryColorHex: Long,
    val accentColorHex: Long,
    val darkBgHex: Long
) {
    MAIZE(
        displayName = "Maize (Corn)",
        hindiName = "मक्का",
        standardMsp = 2450.0,
        idealMoisture = 12.0,
        maxSafeMoisture = 14.0,
        primaryColorHex = 0xFFF59E0B, // Golden Yellow
        secondaryColorHex = 0xFF10B981, // Vivid Green
        accentColorHex = 0xFFFBBF24, // Amber
        darkBgHex = 0xFF121B13
    ),
    WHEAT(
        displayName = "Wheat",
        hindiName = "गेहूं",
        standardMsp = 2375.0,
        idealMoisture = 11.5,
        maxSafeMoisture = 13.0,
        primaryColorHex = 0xFFD97706, // Amber Gold
        secondaryColorHex = 0xFFB45309, // Rust Gold
        accentColorHex = 0xFFFDE68A, // Light Straw
        darkBgHex = 0xFF1C1408
    ),
    SOYBEAN(
        displayName = "Soybean",
        hindiName = "सोयाबीन",
        standardMsp = 4892.0,
        idealMoisture = 10.0,
        maxSafeMoisture = 12.0,
        primaryColorHex = 0xFF059669, // Emerald Green
        secondaryColorHex = 0xFF84CC16, // Lime
        accentColorHex = 0xFF34D399, // Mint
        darkBgHex = 0xFF0B1B14
    ),
    PADDY(
        displayName = "Paddy (Rice)",
        hindiName = "धान",
        standardMsp = 2300.0,
        idealMoisture = 13.0,
        maxSafeMoisture = 15.0,
        primaryColorHex = 0xFF16A34A, // Rich Forest Green
        secondaryColorHex = 0xFFEAB308, // Golden Paddy
        accentColorHex = 0xFF86EFAC, // Pale Green
        darkBgHex = 0xFF091F11
    ),
    MUSTARD(
        displayName = "Mustard",
        hindiName = "सरसों",
        standardMsp = 5650.0,
        idealMoisture = 8.0,
        maxSafeMoisture = 9.5,
        primaryColorHex = 0xFFEAB308, // Bright Mustard Yellow
        secondaryColorHex = 0xFFCA8A04, // Deep Mustard
        accentColorHex = 0xFFFEF08A, // Light Blossom
        darkBgHex = 0xFF1E1B05
    );

    val primaryColor get() = Color(primaryColorHex)
    val secondaryColor get() = Color(secondaryColorHex)
    val accentColor get() = Color(accentColorHex)
    val darkBackground get() = Color(darkBgHex)
}
