package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.GodownEntity
import com.example.data.model.InventoryMovementEntity
import com.example.data.model.InventoryReconciliationEntity
import com.example.data.model.StorageFacilityIntakeEntity
import com.example.data.repository.GrainRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GodownViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = GrainRepository(db)

    val allGodowns: StateFlow<List<GodownEntity>> = repository.allGodowns
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allStorageIntakes: StateFlow<List<StorageFacilityIntakeEntity>> = repository.allStorageIntakes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMovements: StateFlow<List<InventoryMovementEntity>> = repository.allInventoryMovements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allReconciliations: StateFlow<List<InventoryReconciliationEntity>> = repository.allReconciliations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun clearMessages() {
        _successMessage.value = null
        _errorMessage.value = null
    }

    fun reconcileShrinkage(
        godownId: String,
        cropType: String,
        initialStockKg: Double,
        auditedStockKg: Double,
        initialMoisturePct: Double,
        currentMoisturePct: Double,
        baseCostPerQuintal: Double,
        notes: String = ""
    ) {
        viewModelScope.launch {
            try {
                repository.reconcileInventoryShrinkage(
                    godownId = godownId,
                    cropType = cropType,
                    initialStockKg = initialStockKg,
                    auditedStockKg = auditedStockKg,
                    initialMoisturePct = initialMoisturePct,
                    currentMoisturePct = currentMoisturePct,
                    baseCostPerQuintal = baseCostPerQuintal,
                    notes = notes
                )
                _successMessage.value = "Moisture shrinkage reconciled & capitalized in $godownId."
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to reconcile shrinkage."
            }
        }
    }
}
