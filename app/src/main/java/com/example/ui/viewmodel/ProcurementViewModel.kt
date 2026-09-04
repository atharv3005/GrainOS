package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.CropType
import com.example.data.model.PartyEntity
import com.example.data.model.PaymentMode
import com.example.data.model.ProcurementEntity
import com.example.data.model.QualityGrade
import com.example.data.repository.GrainRepository
import com.example.domain.usecase.CreateProcurementRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProcurementViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = GrainRepository(db)

    val allProcurements: StateFlow<List<ProcurementEntity>> = repository.allProcurements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allParties: StateFlow<List<PartyEntity>> = repository.allParties
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    private val _selectedProcurementForReceipt = MutableStateFlow<ProcurementEntity?>(null)
    val selectedProcurementForReceipt: StateFlow<ProcurementEntity?> = _selectedProcurementForReceipt.asStateFlow()

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }

    fun openReceipt(procurement: ProcurementEntity) {
        _selectedProcurementForReceipt.value = procurement
    }

    fun closeReceipt() {
        _selectedProcurementForReceipt.value = null
    }

    fun createProcurement(
        request: CreateProcurementRequest,
        onSuccess: ((ProcurementEntity) -> Unit)? = null
    ) {
        viewModelScope.launch {
            _isSubmitting.value = true
            _errorMessage.value = null
            try {
                val result = repository.createProcurementUseCase(request)
                result.onSuccess { saved ->
                    _successMessage.value = "Procurement ${saved.tokenNo} registered successfully!"
                    onSuccess?.invoke(saved)
                }.onFailure { ex ->
                    _errorMessage.value = ex.message ?: "Failed to create procurement."
                }
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun deleteProcurement(id: Long, reason: String = "User requested cancellation") {
        viewModelScope.launch {
            repository.deleteProcurement(id, reason)
            _successMessage.value = "Procurement record cancelled and audited."
        }
    }
}
