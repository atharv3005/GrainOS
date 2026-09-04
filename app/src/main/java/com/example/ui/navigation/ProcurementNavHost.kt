package com.example.ui.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.data.model.CropType
import com.example.ui.screens.GateEntryScreen
import com.example.ui.viewmodel.GrainWmsViewModel

sealed class ProcurementStage {
    data object GateRegistration : ProcurementStage()
    data class GrossWeighment(val procurementId: Long) : ProcurementStage()
    data class MoistureGrading(val procurementId: Long) : ProcurementStage()
    data class UnloadingConfirm(val procurementId: Long) : ProcurementStage()
    data class TareSettlement(val procurementId: Long) : ProcurementStage()
}

/**
 * Multi-stage navigation host for the inbound procurement intake lifecycle.
 */
@Composable
fun ProcurementNavHost(
    viewModel: GrainWmsViewModel,
    activeCrop: CropType,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentStage by remember { mutableStateOf<ProcurementStage>(ProcurementStage.GateRegistration) }

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = currentStage,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "ProcurementNavAnimation"
        ) { stage ->
            when (stage) {
                is ProcurementStage.GateRegistration,
                is ProcurementStage.GrossWeighment,
                is ProcurementStage.MoistureGrading,
                is ProcurementStage.UnloadingConfirm,
                is ProcurementStage.TareSettlement -> {
                    GateEntryScreen(
                        viewModel = viewModel,
                        activeCrop = activeCrop
                    )
                }
            }
        }
    }
}
