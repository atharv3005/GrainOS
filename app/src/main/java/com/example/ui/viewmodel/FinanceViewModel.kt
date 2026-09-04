package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.AuditTrailEntity
import com.example.data.model.DocumentSequenceEntity
import com.example.data.model.PartyEntity
import com.example.data.model.PaymentAllocationEntity
import com.example.data.model.VendorLedgerEntity
import com.example.data.repository.GrainRepository
import com.example.domain.usecase.AllocatePaymentRequest
import com.example.domain.usecase.DayEndReport
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FinanceViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = GrainRepository(db)

    val allLedgers: StateFlow<List<VendorLedgerEntity>> = repository.allLedgerEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPdcs: StateFlow<List<VendorLedgerEntity>> = repository.allPdcs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allParties: StateFlow<List<PartyEntity>> = repository.allParties
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSequences: StateFlow<List<DocumentSequenceEntity>> = repository.allDocumentSequences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAllocations: StateFlow<List<PaymentAllocationEntity>> = repository.allPaymentAllocations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAuditTrails: StateFlow<List<AuditTrailEntity>> = repository.auditTrailRepository.allAuditTrailsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    private val _dayEndReport = MutableStateFlow<DayEndReport?>(null)
    val dayEndReport: StateFlow<DayEndReport?> = _dayEndReport.asStateFlow()

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }

    // --- PDC Lifecycle Operations ---
    fun depositPdc(uuid: String) {
        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                val result = repository.markPdcAsDepositedUseCase(uuid)
                result.onSuccess {
                    _successMessage.value = "PDC marked as DEPOSITED in bank."
                }.onFailure { ex ->
                    _errorMessage.value = ex.message ?: "Failed to deposit PDC."
                }
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun presentPdc(uuid: String) {
        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                val result = repository.markPdcAsPresentedUseCase(uuid)
                result.onSuccess {
                    _successMessage.value = "PDC presented for clearing."
                }.onFailure { ex ->
                    _errorMessage.value = ex.message ?: "Failed to present PDC."
                }
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun clearPdc(uuid: String) {
        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                val result = repository.markPdcAsClearedUseCase(uuid)
                result.onSuccess {
                    _successMessage.value = "PDC CLEARED! Payable liability marked as PAID."
                }.onFailure { ex ->
                    _errorMessage.value = ex.message ?: "Failed to clear PDC."
                }
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun bouncePdc(uuid: String, bounceReason: String) {
        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                val result = repository.markPdcAsBouncedUseCase(uuid, bounceReason)
                result.onSuccess {
                    _successMessage.value = "PDC BOUNCED. Original payable reopened as UNPAID."
                }.onFailure { ex ->
                    _errorMessage.value = ex.message ?: "Failed to bounce PDC."
                }
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    // --- Split Payment Allocation ---
    fun allocatePayment(request: AllocatePaymentRequest) {
        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                val result = repository.allocatePaymentUseCase(request)
                result.onSuccess { allocations ->
                    _successMessage.value = "Allocated payment across ${allocations.size} entries."
                }.onFailure { ex ->
                    _errorMessage.value = ex.message ?: "Failed to allocate payment."
                }
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    // --- Day-End Closing ---
    fun closeDayEnd(financialYear: String = "26-27", facilityId: String = "MAIN") {
        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                val result = repository.closeDayEndUseCase(financialYear, facilityId)
                result.onSuccess { report ->
                    _dayEndReport.value = report
                    _successMessage.value = "Day-End Closing complete! Sequence numbers frozen."
                }.onFailure { ex ->
                    _errorMessage.value = ex.message ?: "Failed to execute Day-End Closing."
                }
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    // --- Party Master Management ---
    fun createParty(party: PartyEntity, onComplete: ((Long) -> Unit)? = null) {
        viewModelScope.launch {
            try {
                val id = repository.partyRepository.create(party)
                _successMessage.value = "Party ${party.legalName} created."
                onComplete?.invoke(id)
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to create party."
            }
        }
    }

    fun updateParty(party: PartyEntity) {
        viewModelScope.launch {
            try {
                repository.partyRepository.update(party)
                _successMessage.value = "Party ${party.legalName} updated."
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to update party."
            }
        }
    }
}
