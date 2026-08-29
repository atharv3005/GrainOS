package com.example.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun WeighbridgeLiveDialog(
    targetType: String,
    onDismiss: () -> Unit,
    onWeightCaptured: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    onDismiss()
}
