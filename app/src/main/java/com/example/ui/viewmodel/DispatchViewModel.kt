package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.GodownEntity
import com.example.data.model.OutboundDispatchEntity
import com.example.data.model.PartyEntity
import com.example.data.repository.GrainRepository
import com.example.domain.usecase.CreateDispatchRequest
import com.example.domain.usecase.InsufficientStockException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DispatchViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = GrainRepository(db)

    val allDispatches: StateFlow<List<OutboundDispatchEntity>> = repository.allDispatches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allGodowns: StateFlow<List<GodownEntity>> = repository.allGodowns
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allParties: StateFlow<List<PartyEntity>> = repository.allParties
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    private val _selectedDispatchForSettlement = MutableStateFlow<OutboundDispatchEntity?>(null)
    val selectedDispatchForSettlement: StateFlow<OutboundDispatchEntity?> = _selectedDispatchForSettlement.asStateFlow()

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }

    fun openSettlement(dispatch: OutboundDispatchEntity) {
        _selectedDispatchForSettlement.value = dispatch
    }

    fun closeSettlement() {
        _selectedDispatchForSettlement.value = null
    }

    fun createDispatch(
        request: CreateDispatchRequest,
        onSuccess: ((OutboundDispatchEntity) -> Unit)? = null
    ) {
        viewModelScope.launch {
            _isSubmitting.value = true
            _errorMessage.value = null
            try {
                val result = repository.createDispatchUseCase(request)
                result.onSuccess { saved ->
                    _successMessage.value = "Dispatch ${saved.dispatchNo} authorized & inventory deducted!"
                    onSuccess?.invoke(saved)
                }.onFailure { ex ->
                    if (ex is InsufficientStockException) {
                        _errorMessage.value = "⚠️ STOCK BLOCKED: ${ex.message}"
                    } else {
                        _errorMessage.value = ex.message ?: "Failed to authorize dispatch."
                    }
                }
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun settleUnloadedDispatch(
        dispatchId: Long,
        companyUnloadedWeightKg: Double,
        qualityPenalty: Double,
        freightCost: Double,
        laborCost: Double,
        bagCost: Double,
        miscCost: Double,
        brokerName: String,
        brokerageRatePerQtl: Double
    ) {
        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                repository.settleUnloadedDispatch(
                    dispatchId = dispatchId,
                    companyUnloadedWeightKg = companyUnloadedWeightKg,
                    qualityPenalty = qualityPenalty,
                    freightCost = freightCost,
                    laborCost = laborCost,
                    bagCost = bagCost,
                    miscCost = miscCost,
                    brokerName = brokerName,
                    brokerageRatePerQtl = brokerageRatePerQtl
                )
                closeSettlement()
                _successMessage.value = "Dispatch settled & Net P&L posted!"
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to settle dispatch."
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun deleteDispatch(id: Long, reason: String = "User requested cancellation") {
        viewModelScope.launch {
            repository.deleteDispatch(id, reason)
            _successMessage.value = "Dispatch record cancelled & audited."
        }
    }
}
