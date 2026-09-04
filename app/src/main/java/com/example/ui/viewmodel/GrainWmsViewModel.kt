package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.gemini.GeminiAdvisorService
import com.example.data.local.AppDatabase
import com.example.data.model.AuditTrailEntity
import com.example.data.model.CropType
import com.example.data.model.DocumentSequenceEntity
import com.example.data.model.ExpenseEntryEntity
import com.example.data.model.FirmProfile
import com.example.data.model.GodownEntity
import com.example.data.model.InventoryMovementEntity
import com.example.data.model.InventoryReconciliationEntity
import com.example.data.model.IoTTelemetryEntity
import com.example.data.model.OutboundDispatchEntity
import com.example.data.model.PartyEntity
import com.example.data.model.PaymentAllocationEntity
import com.example.data.model.PaymentMode
import com.example.data.model.PaymentStatus
import com.example.data.model.ProcurementEntity
import com.example.data.model.ProcurementStatus
import com.example.data.model.StorageFacilityIntakeEntity
import com.example.data.model.TradeBookingEntity
import com.example.data.model.TruckRejectionEntity
import com.example.data.model.VendorLedgerEntity
import com.example.data.model.VendorType
import com.example.data.model.TransactionType
import com.example.data.repository.GrainRepository
import com.example.security.DeviceSecurityReport
import com.example.security.SecureStorageManager
import com.example.security.SecurityCheckUtil
import com.example.domain.managers.InventoryManager
import com.example.domain.managers.TransactionLedgerManager
import com.example.domain.usecase.CreateDispatchRequest
import com.example.domain.usecase.CreateProcurementRequest
import com.example.domain.usecase.InsufficientStockException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class GrainWmsViewModel(application: Application) : AndroidViewModel(application) {
    private val secureStorage = SecureStorageManager(application)
    private val database = AppDatabase.getDatabase(application, viewModelScope)
    val repository = GrainRepository(database)
    private val inventoryManager = InventoryManager(database.godownDao())
    val transactionManager = TransactionLedgerManager(repository, inventoryManager)
    private val geminiService = GeminiAdvisorService()

    // 1. Device Security Status
    private val _securityReport = MutableStateFlow(SecurityCheckUtil.evaluateDeviceSecurity())
    val securityReport: StateFlow<DeviceSecurityReport> = _securityReport.asStateFlow()

    private val _showSecurityDialog = MutableStateFlow(false)
    val showSecurityDialog: StateFlow<Boolean> = _showSecurityDialog.asStateFlow()

    // 2. Firm Profile & Dynamic Onboarding
    private val _firmProfile = MutableStateFlow(secureStorage.loadFirmProfile())
    val firmProfile: StateFlow<FirmProfile> = _firmProfile.asStateFlow()

    private val _showOnboardingDialog = MutableStateFlow(!secureStorage.isUserOnboarded())
    val showOnboardingDialog: StateFlow<Boolean> = _showOnboardingDialog.asStateFlow()

    private val _showFirmLoginDialog = MutableStateFlow(false)
    val showFirmLoginDialog: StateFlow<Boolean> = _showFirmLoginDialog.asStateFlow()

    private val _showExpenseConfigDialog = MutableStateFlow(false)
    val showExpenseConfigDialog: StateFlow<Boolean> = _showExpenseConfigDialog.asStateFlow()

    private val _showTradeBookingDialog = MutableStateFlow(false)
    val showTradeBookingDialog: StateFlow<Boolean> = _showTradeBookingDialog.asStateFlow()

    // 3. Manual Expense, Rejections, Reconciliation & Ledgers Dialog States
    private val _showExpenseEntryDialog = MutableStateFlow(false)
    val showExpenseEntryDialog: StateFlow<Boolean> = _showExpenseEntryDialog.asStateFlow()

    private val _showTruckRejectionDialog = MutableStateFlow(false)
    val showTruckRejectionDialog: StateFlow<Boolean> = _showTruckRejectionDialog.asStateFlow()

    private val _showReconciliationDialog = MutableStateFlow(false)
    val showReconciliationDialog: StateFlow<Boolean> = _showReconciliationDialog.asStateFlow()

    private val _showVendorLedgerDialog = MutableStateFlow(false)
    val showVendorLedgerDialog: StateFlow<Boolean> = _showVendorLedgerDialog.asStateFlow()

    private val _showCaExportDialog = MutableStateFlow(false)
    val showCaExportDialog: StateFlow<Boolean> = _showCaExportDialog.asStateFlow()

    private val _showSettleDispatchDialog = MutableStateFlow(false)
    val showSettleDispatchDialog: StateFlow<Boolean> = _showSettleDispatchDialog.asStateFlow()
    private val _selectedDispatchForSettlement = MutableStateFlow<OutboundDispatchEntity?>(null)
    val selectedDispatchForSettlement: StateFlow<OutboundDispatchEntity?> = _selectedDispatchForSettlement.asStateFlow()

    // Rejection prep state
    private val _rejectionTruckNumber = MutableStateFlow("")
    val rejectionTruckNumber: StateFlow<String> = _rejectionTruckNumber.asStateFlow()

    private val _rejectionBuyerName = MutableStateFlow("")
    val rejectionBuyerName: StateFlow<String> = _rejectionBuyerName.asStateFlow()

    private val _rejectionWeightKg = MutableStateFlow(0.0)
    val rejectionWeightKg: StateFlow<Double> = _rejectionWeightKg.asStateFlow()

    // 4. Active Crop Selection
    private val _activeCrop = MutableStateFlow(_firmProfile.value.mainTargetCrop)
    val activeCrop: StateFlow<CropType> = _activeCrop.asStateFlow()

    // 5. Telemetry streaming toggle
    private val _isStreamingActive = MutableStateFlow(true)
    val isStreamingActive: StateFlow<Boolean> = _isStreamingActive.asStateFlow()

    // 6. Gemini AI Advisor State
    private val _aiAnalysisResult = MutableStateFlow<String?>(null)
    val aiAnalysisResult: StateFlow<String?> = _aiAnalysisResult.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    // 7. Receipts & Document Sharing
    private val _selectedWhatsAppReceipt = MutableStateFlow<ProcurementEntity?>(null)
    val selectedWhatsAppReceipt: StateFlow<ProcurementEntity?> = _selectedWhatsAppReceipt.asStateFlow()

    private val _isWhatsAppEntryOnly = MutableStateFlow(false)
    val isWhatsAppEntryOnly: StateFlow<Boolean> = _isWhatsAppEntryOnly.asStateFlow()

    private val _selectedPdfReceipt = MutableStateFlow<ProcurementEntity?>(null)
    val selectedPdfReceipt: StateFlow<ProcurementEntity?> = _selectedPdfReceipt.asStateFlow()

    private val _showArchitectureDialog = MutableStateFlow(false)
    val showArchitectureDialog: StateFlow<Boolean> = _showArchitectureDialog.asStateFlow()

    // User Notification Snackbar message
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()
    private val _smartInsight = MutableStateFlow("Loading market insights...")
    val smartInsight: StateFlow<String> = _smartInsight.asStateFlow()

    // 8. Reactive DB States
    val allProcurements: StateFlow<List<ProcurementEntity>> = repository.allProcurements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allGodowns: StateFlow<List<GodownEntity>> = repository.allGodowns
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allStorageIntakes: StateFlow<List<StorageFacilityIntakeEntity>> = repository.allStorageIntakes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDispatches: StateFlow<List<OutboundDispatchEntity>> = repository.allDispatches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentTelemetry: StateFlow<List<IoTTelemetryEntity>> = repository.recentTelemetry
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTrades: StateFlow<List<TradeBookingEntity>> = repository.allTrades
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allExpenses: StateFlow<List<ExpenseEntryEntity>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRejections: StateFlow<List<TruckRejectionEntity>> = repository.allRejections
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLedgers: StateFlow<List<VendorLedgerEntity>> = repository.allLedgerEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPdcs: StateFlow<List<VendorLedgerEntity>> = repository.allPdcs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingPdcs: StateFlow<List<VendorLedgerEntity>> = repository.getPendingPdcs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allReconciliations: StateFlow<List<InventoryReconciliationEntity>> = repository.allReconciliations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allParties: StateFlow<List<PartyEntity>> = repository.allParties
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDocumentSequences: StateFlow<List<DocumentSequenceEntity>> = repository.allDocumentSequences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allInventoryMovements: StateFlow<List<InventoryMovementEntity>> = repository.allInventoryMovements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPaymentAllocations: StateFlow<List<PaymentAllocationEntity>> = repository.allPaymentAllocations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAuditTrails: StateFlow<List<AuditTrailEntity>> = repository.auditTrailRepository.allAuditTrailsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- REAL-TIME LIVE INVENTORY SYNC ---
    val liveGodownStockLedger: StateFlow<Map<String, Double>> = kotlinx.coroutines.flow.combine(allProcurements, allDispatches, allGodowns) { procs, disps, godowns ->
        val stockMap = mutableMapOf<String, Double>()
        
        fun getGodownId(ref: String): String {
            val exact = godowns.find { it.godownId == ref || it.displayName == ref }
            if (exact != null) return exact.godownId
            val fuzzy = godowns.find { it.displayName.contains(ref, ignoreCase = true) || ref.contains(it.displayName, ignoreCase = true) }
            if (fuzzy != null) return fuzzy.godownId
            return ref
        }

        procs.filter { it.status == ProcurementStatus.UNLOADED.name || it.status == ProcurementStatus.COMPLETED.name }
            .forEach { p ->
                val gId = getGodownId(p.godownAssigned)
                val current = stockMap.getOrDefault(gId, 0.0)
                stockMap[gId] = current + (p.netWeightKg / 1000.0)
            }
            
        disps.filter { it.status != com.example.data.model.DispatchStatus.REJECTED.name }
            .forEach { d ->
                val gId = getGodownId(d.godownSource)
                val current = stockMap.getOrDefault(gId, 0.0)
                stockMap[gId] = current - (d.netLoadedWeightKg / 1000.0)
            }
            
        stockMap
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    private var telemetryJob: Job? = null

    init {
        startSimulatedTelemetry()
        _activeCrop.value = _firmProfile.value.mainTargetCrop
    }

    fun setCrop(crop: CropType) {
        _activeCrop.value = crop
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun toggleStreaming() {
        _isStreamingActive.value = !_isStreamingActive.value
        if (_isStreamingActive.value) {
            startSimulatedTelemetry()
        } else {
            telemetryJob?.cancel()
        }
    }

    fun injectTestTelemetryPulse() {
        viewModelScope.launch {
            repository.emitSimulatedIoTPulse()
        }
    }

    private fun startSimulatedTelemetry() {
        telemetryJob?.cancel()
        telemetryJob = viewModelScope.launch {
            while (isActive) {
                delay(3500)
                if (_isStreamingActive.value) {
                    repository.emitSimulatedIoTPulse()
                }
            }
        }
    }

    fun checkGoogleDriveBackupStatus() {
        viewModelScope.launch {
            val lastSyncTime = secureStorage.getLong("last_drive_sync_time", 0L)
            val currentTime = System.currentTimeMillis()
            val hours48 = 48 * 60 * 60 * 1000L
            if (currentTime - lastSyncTime > hours48) {
                _snackbarMessage.value = "⚠️ Backup Recommended: Local database hasn't been synced to Google Drive in over 48 hours."
            }
        }
    }
    
    fun simulateGoogleDriveBackup() {
        viewModelScope.launch {
            secureStorage.saveLong("last_drive_sync_time", System.currentTimeMillis())
            _snackbarMessage.value = "✅ Database successfully backed up to Google Drive."
        }
    }

    fun submitAdvancedGateEntry(
        procurement: ProcurementEntity,
        targetGodownIdOrName: String? = null,
        onComplete: ((ProcurementEntity) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val assignedGodown = targetGodownIdOrName?.ifBlank { null } ?: procurement.godownAssigned
            val updatedProc = procurement.copy(godownAssigned = assignedGodown)
            repository.updateProcurement(updatedProc)
            repository.recordStorageIntake(
                storageFacilityNameOrId = assignedGodown,
                procurement = updatedProc,
                customMoisture = updatedProc.moisturePercentage
            )
            _snackbarMessage.value = "Gate Entry Saved & Stored in $assignedGodown!"
            onComplete?.invoke(updatedProc)
        }
    }

    fun deleteStorageIntake(id: Long) {
        viewModelScope.launch {
            repository.deleteStorageIntake(id)
            _snackbarMessage.value = "Storage facility intake record deleted."
        }
    }

    fun completeOnboarding(
        firmName: String,
        capacityMt: Double,
        mainCrop: CropType,
        facilities: List<Pair<String, Double>> = emptyList(),
        operatorName: String = "Chief Operator",
        location: String = "Maharashtra",
        contactNumber: String = "+91 98220 12345",
        gstNumber: String = "27AABCB1234F1Z5",
        laborExp: Double = 12.0,
        bagExp: Double = 45.0,
        transportExp: Double = 25.0,
        brokerExp: Double = 8.0
    ) {
        val totalCap = if (facilities.isNotEmpty()) facilities.sumOf { it.second } else if (capacityMt > 0) capacityMt else 5000.0
        val newProfile = FirmProfile(
            firmName = firmName.trim().ifEmpty { "GrainOS Enterprise Agri Hub" },
            registrationNumber = "APMC/MH/2026/088",
            location = location.trim().ifEmpty { "Dhule, Maharashtra" },
            operatorName = operatorName.trim().ifEmpty { "Chief Yard Operator" },
            contactNumber = contactNumber.trim().ifEmpty { "+91 98220 12345" },
            gstNumber = gstNumber.trim().ifEmpty { "27AABCB1234F1Z5" },
            tagLine = "Enterprise Agri-Commodity Trading & Warehouse OS",
            totalCapacityMt = totalCap,
            mainTargetCrop = mainCrop,
            isOnboarded = true,
            laborPerQuintal = laborExp,
            bagCostPerQuintal = bagExp,
            transportPerQuintal = transportExp,
            brokeragePerQuintal = brokerExp
        )
        _firmProfile.value = newProfile
        _activeCrop.value = mainCrop
        secureStorage.saveFirmProfile(newProfile)

        if (facilities.isNotEmpty()) {
            val godownEntities = facilities.mapIndexed { idx, fac ->
                val idStr = "GODOWN_${(idx + 65).toChar()}"
                GodownEntity(
                    godownId = idStr,
                    displayName = fac.first.ifBlank { "Storage Facility ${idx + 1}" },
                    capacityMt = fac.second.coerceAtLeast(50.0),
                    currentStockMt = 0.0,
                    activeCrop = mainCrop.name,
                    averageMoisture = mainCrop.idealMoisture,
                    temperatureCelsius = 24.0,
                    baseCostPerQuintal = mainCrop.standardMsp,
                    adjustedAvgCostPerQuintal = mainCrop.standardMsp
                )
            }
            viewModelScope.launch {
                repository.insertGodowns(godownEntities)
            }
        }

        _showOnboardingDialog.value = false
    }

    fun updateFirmProfile(profile: FirmProfile) {
        _firmProfile.value = profile
        secureStorage.saveFirmProfile(profile)
    }

    fun updateExpenseDefaults(labor: Double, bag: Double, transport: Double, brokerage: Double) {
        val updated = _firmProfile.value.copy(
            laborPerQuintal = labor,
            bagCostPerQuintal = bag,
            transportPerQuintal = transport,
            brokeragePerQuintal = brokerage
        )
        _firmProfile.value = updated
        secureStorage.saveFirmProfile(updated)
    }

    // --- INBOUND REGISTRATION & COMPLIANCE PIPELINE ---
    fun registerFarmer(
        farmerName: String,
        mobileNumber: String,
        village: String,
        vehicleNumber: String,
        panNumber: String = "",
        isPanVerified: Boolean = false,
        cropType: CropType,
        applyMandiCess: Boolean = false,
        enableTds194q: Boolean = false,
        ratePerQuintal: Double = cropType.standardMsp,
        godownAssigned: String = "Godown A",
        partyId: Long? = null,
        onComplete: (Long) -> Unit = {}
    ) {
        viewModelScope.launch {
            val id = repository.registerInboundLot(
                farmerName = farmerName,
                mobileNumber = mobileNumber,
                village = village,
                vehicleNumber = vehicleNumber,
                panNumber = panNumber,
                isPanVerified = isPanVerified,
                cropType = cropType,
                applyMandiCess = applyMandiCess,
                enableTds194q = enableTds194q,
                ratePerQuintal = ratePerQuintal,
                godownAssigned = godownAssigned,
                partyId = partyId
            )
            _snackbarMessage.value = "Inbound token generated!"
            onComplete(id)
        }
    }

    fun recordGrossWeight(
        procurementId: Long,
        grossWeightKg: Double,
        weightMethod: String = "AUTO",
        showEntryAlert: Boolean = false
    ) {
        viewModelScope.launch {
            val updated = repository.recordGrossWeight(procurementId, grossWeightKg, weightMethod)
            if (showEntryAlert && updated != null) {
                _selectedWhatsAppReceipt.value = updated
                _isWhatsAppEntryOnly.value = true
            }
            _snackbarMessage.value = "Gross weight logged (${grossWeightKg.toInt()} kg)"
        }
    }

    fun recordMoistureAndGrading(
        procurementId: Long,
        moisturePercentage: Double,
        godownAssigned: String,
        manualFarmerRate: Double? = null
    ) {
        viewModelScope.launch {
            repository.recordMoistureAndGrading(procurementId, moisturePercentage, godownAssigned, manualFarmerRate)
            _snackbarMessage.value = "Moisture logged ($moisturePercentage%) & Silo assigned."
        }
    }

    fun confirmUnloading(procurementId: Long) {
        viewModelScope.launch {
            repository.confirmUnloading(procurementId)
            _snackbarMessage.value = "Unloading confirmed at silo bay."
        }
    }

    fun recordTareWeightAndComplete(
        procurementId: Long,
        tareWeightKg: Double,
        bagCount: Int = 0,
        bagWeightKg: Double = 50.0,
        paymentMode: String = PaymentMode.CASH.name,
        utrOrChequeNo: String = "",
        isPdc: Boolean = false,
        chequeMaturityDate: Long = 0L,
        weightMethod: String = "AUTO",
        overrideFarmerRate: Double? = null,
        showReceiptAlert: Boolean = true
    ) {
        viewModelScope.launch {
            val updated = repository.recordTareWeightAndComplete(
                procurementId = procurementId,
                tareWeightKg = tareWeightKg,
                bagCount = bagCount,
                bagWeightKg = bagWeightKg,
                paymentMode = paymentMode,
                utrOrChequeNo = utrOrChequeNo,
                isPdc = isPdc,
                chequeMaturityDate = chequeMaturityDate,
                weightMethod = weightMethod,
                overrideFarmerRate = overrideFarmerRate
            )
            if (showReceiptAlert && updated != null) {
                _selectedWhatsAppReceipt.value = updated
                _isWhatsAppEntryOnly.value = false
            }
            _snackbarMessage.value = "Procurement complete! Bill & inventory posted."
        }
    }

    // --- INVENTORY RECONCILIATION & MOISTURE SHRINKAGE ---
    fun reconcileInventoryShrinkage(
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
            _showReconciliationDialog.value = false
            _snackbarMessage.value = "Moisture shrinkage reconciled & capitalized across remaining stock."
        }
    }

    // --- OUTBOUND DISPATCH & ACTUAL P&L SETTLEMENT ---
    private val _isSubmittingDispatch = MutableStateFlow(false)
    val isSubmittingDispatch: StateFlow<Boolean> = _isSubmittingDispatch.asStateFlow()

    fun createOutboundDispatch(
        buyerName: String,
        destination: String,
        vehicleNumber: String,
        cropType: String,
        godownSource: String,
        tareWeightKg: Double,
        grossWeightKg: Double,
        ratePerQuintal: Double,
        buyerPartyId: Long? = null,
        onComplete: (Long) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isSubmittingDispatch.value = true
            try {
                val crop = CropType.entries.find { it.name == cropType } ?: CropType.MAIZE
                val request = CreateDispatchRequest(
                    buyerPartyId = buyerPartyId,
                    buyerNameQuick = buyerName,
                    destination = destination,
                    vehicleNumber = vehicleNumber,
                    cropType = crop,
                    godownSource = godownSource,
                    tareWeightKg = tareWeightKg,
                    grossWeightKg = grossWeightKg,
                    ratePerQuintal = ratePerQuintal
                )
                val result = repository.createDispatchUseCase(request)
                result.onSuccess { saved ->
                    _snackbarMessage.value = "Truck dispatched! Stock depleted via FIFO."
                    onComplete(saved.id)
                }.onFailure { ex ->
                    if (ex is InsufficientStockException) {
                        _snackbarMessage.value = "⚠️ STOCK BLOCKED: ${ex.message}"
                    } else {
                        _snackbarMessage.value = "Dispatch Error: ${ex.message}"
                    }
                }
            } finally {
                _isSubmittingDispatch.value = false
            }
        }
    }

    fun openSettleDispatchDialog(dispatch: OutboundDispatchEntity) {
        _selectedDispatchForSettlement.value = dispatch
        _showSettleDispatchDialog.value = true
    }

    fun closeSettleDispatchDialog() {
        _selectedDispatchForSettlement.value = null
        _showSettleDispatchDialog.value = false
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
            closeSettleDispatchDialog()
            _snackbarMessage.value = "Dispatch settled! Actual Net Profit calculated."
        }
    }

    // --- MANUAL EXPENSES ---
    fun recordManualExpense(
        truckOrBatchRef: String,
        cropType: CropType,
        laborCost: Double,
        bagsCost: Double,
        transportCost: Double,
        miscCost: Double,
        miscDescription: String = "",
        paidToOrParty: String,
        paymentMode: String = "CASH",
        utrOrChequeNo: String = "",
        notes: String = "",
        partyId: Long? = null
    ) {
        viewModelScope.launch {
            repository.recordManualExpense(
                truckOrBatchRef = truckOrBatchRef,
                cropType = cropType,
                laborCost = laborCost,
                bagsCost = bagsCost,
                transportCost = transportCost,
                miscCost = miscCost,
                miscDescription = miscDescription,
                paidToOrParty = paidToOrParty,
                paymentMode = paymentMode,
                utrOrChequeNo = utrOrChequeNo,
                notes = notes,
                partyId = partyId
            )
            _showExpenseEntryDialog.value = false
            _snackbarMessage.value = "Transaction expense recorded & posted to vendor ledger."
        }
    }

    fun deleteExpense(expenseId: Long, reason: String = "User deletion") {
        viewModelScope.launch {
            repository.deleteExpenseById(expenseId, reason)
            _snackbarMessage.value = "Expense record deleted & audited."
        }
    }

    // --- TRUCK REJECTION & 50% SHIFTING LABOR ---
    fun openTruckRejectionDialog(
        truckNumber: String = "",
        buyerName: String = "",
        dispatchedWeightKg: Double = 0.0
    ) {
        _rejectionTruckNumber.value = truckNumber
        _rejectionBuyerName.value = buyerName
        _rejectionWeightKg.value = dispatchedWeightKg
        _showTruckRejectionDialog.value = true
    }

    fun closeTruckRejectionDialog() {
        _showTruckRejectionDialog.value = false
        _rejectionTruckNumber.value = ""
        _rejectionBuyerName.value = ""
        _rejectionWeightKg.value = 0.0
    }

    fun recordTruckRejection(
        truckNumber: String,
        buyerOrCompany: String,
        cropType: CropType,
        dispatchedWeightKg: Double,
        rejectionReason: String,
        transportLoss: Double,
        penaltiesDemurrage: Double,
        originalLoadingLaborCost: Double,
        qualitySalvageDeduction: Double,
        salvageAction: String,
        salvageRealizedRatePerQtl: Double = 0.0,
        notes: String = ""
    ) {
        viewModelScope.launch {
            repository.recordTruckRejection(
                truckNumber = truckNumber,
                buyerOrCompany = buyerOrCompany,
                cropType = cropType,
                dispatchedWeightKg = dispatchedWeightKg,
                rejectionReason = rejectionReason,
                transportLoss = transportLoss,
                penaltiesDemurrage = penaltiesDemurrage,
                originalLoadingLaborCost = originalLoadingLaborCost,
                qualitySalvageDeduction = qualitySalvageDeduction,
                salvageAction = salvageAction,
                salvageRealizedRatePerQtl = salvageRealizedRatePerQtl,
                notes = notes
            )
            closeTruckRejectionDialog()
            _snackbarMessage.value = "Rejection recorded with 50% Return Labor adjustment."
        }
    }

    fun deleteTruckRejection(id: Long, reason: String = "User deletion") {
        viewModelScope.launch {
            repository.deleteTruckRejection(id, reason)
            _snackbarMessage.value = "Rejection record deleted & audited."
        }
    }

    // --- PREDICTIVE MOISTURE SHRINKAGE AI ENGINE ---
    fun getEstimatedPhysicalStock(godownId: String): Double {
        val bookStockMt = liveGodownStockLedger.value[godownId] ?: return 0.0
        if (bookStockMt <= 0) return 0.0

        val procs = allProcurements.value
            .filter { (it.status == ProcurementStatus.UNLOADED.name || it.status == ProcurementStatus.COMPLETED.name) && it.godownAssigned == godownId }
            .sortedBy { it.completedTimestamp.takeIf { t -> t > 0 } ?: it.createdAt }
            
        val disps = allDispatches.value
            .filter { it.godownSource == godownId && it.status != com.example.data.model.DispatchStatus.REJECTED.name }
        
        var totalDispatched = disps.sumOf { it.netLoadedWeightKg / 1000.0 }
        var totalEstimatedShrinkMt = 0.0
        val now = System.currentTimeMillis()

        for (p in procs) {
            val inwardMt = p.netWeightKg / 1000.0
            if (totalDispatched >= inwardMt) {
                totalDispatched -= inwardMt
                continue
            }
            
            val remainingMt = inwardMt - totalDispatched
            totalDispatched = 0.0
            
            val inwardDate = p.completedTimestamp.takeIf { it > 0 } ?: p.createdAt
            val weeksStored = ((now - inwardDate) / (1000.0 * 60 * 60 * 24 * 7)).coerceAtLeast(0.0)
            
            val initialMoisture = p.moisturePercentage.takeIf { it > 0.0 } ?: 14.5
            var currentEstimatedMoisture = initialMoisture - (0.5 * weeksStored)
            if (currentEstimatedMoisture < 14.5) currentEstimatedMoisture = 14.5
            
            var shrinkPct = 0.0
            if (initialMoisture > currentEstimatedMoisture) {
                shrinkPct = ((initialMoisture - currentEstimatedMoisture) / (100.0 - currentEstimatedMoisture)) * 100.0
            }
            
            val totalLossPct = shrinkPct + 0.5
            val lossMt = remainingMt * (totalLossPct / 100.0)
            totalEstimatedShrinkMt += lossMt
        }
        
        return (bookStockMt - totalEstimatedShrinkMt).coerceAtLeast(0.0)
    }

    fun exportCaReportToExcel(context: android.content.Context) {
        viewModelScope.launch {
            try {
                val ledgers = allLedgers.value
                val success = com.example.data.export.ExcelExportHelper.exportAnnualLedgerToExcel(context, ledgers)
                if (success) {
                    _snackbarMessage.value = "Editable Excel Report saved to Downloads"
                } else {
                    _snackbarMessage.value = "Failed to export report"
                }
            } catch (e: Exception) {
                _snackbarMessage.value = "Export error: ${e.message}"
            }
        }
    }

    // --- TRADES ---
    fun bookTrade(
        cropType: CropType,
        brokerOrBuyerName: String,
        quantityTons: Double,
        bookedPricePerQuintal: Double,
        farmerPurchasePricePerQuintal: Double,
        laborPerQuintal: Double = _firmProfile.value.laborPerQuintal,
        bagCostPerQuintal: Double = _firmProfile.value.bagCostPerQuintal,
        transportPerQuintal: Double = _firmProfile.value.transportPerQuintal,
        brokeragePerQuintal: Double = _firmProfile.value.brokeragePerQuintal,
        notes: String = "",
        buyerPartyId: Long? = null
    ) {
        viewModelScope.launch {
            repository.bookTrade(
                cropType = cropType,
                brokerOrBuyerName = brokerOrBuyerName,
                quantityTons = quantityTons,
                bookedPricePerQuintal = bookedPricePerQuintal,
                farmerPurchasePricePerQuintal = farmerPurchasePricePerQuintal,
                laborPerQuintal = laborPerQuintal,
                bagCostPerQuintal = bagCostPerQuintal,
                transportPerQuintal = transportPerQuintal,
                brokeragePerQuintal = brokeragePerQuintal,
                notes = notes,
                buyerPartyId = buyerPartyId
            )
            _showTradeBookingDialog.value = false
            _snackbarMessage.value = "Trade contract locked."
        }
    }

    fun updateTradeStatus(tradeId: Long, newStatus: String) {
        viewModelScope.launch {
            repository.updateTradeStatus(tradeId, newStatus)
        }
    }

    fun deleteTrade(tradeId: Long, reason: String = "User deletion") {
        viewModelScope.launch {
            repository.deleteTrade(tradeId, reason)
        }
    }

    fun updatePaymentStatus(procurementId: Long, newStatus: PaymentStatus) {
        viewModelScope.launch {
            repository.updatePaymentStatus(procurementId, newStatus)
        }
    }

    // --- GEMINI PRO ADVISOR ---
    fun runGeminiAnalysis(
        crop: CropType,
        moisturePct: Double,
        ambientTempC: Double,
        targetGodown: String,
        farmerName: String
    ) {
        viewModelScope.launch {
            _isAiLoading.value = true
            val response = geminiService.analyzeGrainLot(
                crop = crop,
                moisturePct = moisturePct,
                ambientTempC = ambientTempC,
                targetGodown = targetGodown,
                farmerName = farmerName
            )
            _aiAnalysisResult.value = response
            _isAiLoading.value = false
        }
    }

    // Dialog state toggles
    fun setShowOnboardingDialog(show: Boolean) { _showOnboardingDialog.value = show }
    fun setShowFirmLoginDialog(show: Boolean) { _showFirmLoginDialog.value = show }
    fun setShowExpenseConfigDialog(show: Boolean) { _showExpenseConfigDialog.value = show }
    fun setShowTradeBookingDialog(show: Boolean) { _showTradeBookingDialog.value = show }
    fun setShowSecurityDialog(show: Boolean) { _showSecurityDialog.value = show }
    fun setShowArchitectureDialog(show: Boolean) { _showArchitectureDialog.value = show }
    fun setShowExpenseEntryDialog(show: Boolean) { _showExpenseEntryDialog.value = show }
    fun setShowReconciliationDialog(show: Boolean) { _showReconciliationDialog.value = show }
    fun setShowVendorLedgerDialog(show: Boolean) { _showVendorLedgerDialog.value = show }
    fun setShowCaExportDialog(show: Boolean) { _showCaExportDialog.value = show }

    fun openWhatsAppReceipt(procurement: ProcurementEntity, isEntryOnly: Boolean) {
        val sanitized = if (!isEntryOnly && procurement.netWeightKg <= 0.0 && procurement.grossWeightKg > 0) {
            val gross = procurement.grossWeightKg
            val tare = if (procurement.tareWeightKg > 0) procurement.tareWeightKg else 0.0
            val net = (gross - tare).coerceAtLeast(0.0)
            val crop = CropType.entries.find { it.name == procurement.cropType } ?: CropType.MAIZE
            val rate = if (procurement.ratePerQuintal > 0) procurement.ratePerQuintal else crop.standardMsp
            val grossBill = (net / 100.0) * rate
            val total = if (procurement.totalAmount > 0) procurement.totalAmount else grossBill
            procurement.copy(
                tareWeightKg = tare,
                netWeightKg = net,
                ratePerQuintal = rate,
                grossBillAmount = grossBill,
                totalAmount = total
            )
        } else {
            procurement
        }
        _selectedWhatsAppReceipt.value = sanitized
        _isWhatsAppEntryOnly.value = isEntryOnly
    }
    fun closeWhatsAppReceipt() { _selectedWhatsAppReceipt.value = null }

    fun openPdfReceipt(procurement: ProcurementEntity) { _selectedPdfReceipt.value = procurement }
    fun closePdfReceipt() { _selectedPdfReceipt.value = null }

    fun deleteProcurement(id: Long, reason: String = "User deletion") {
        viewModelScope.launch {
            repository.deleteProcurement(id, reason)
            _snackbarMessage.value = "Procurement record deleted & audited."
        }
    }

    fun setProcurementArchived(id: Long, isArchived: Boolean) {
        viewModelScope.launch {
            repository.setProcurementArchived(id, isArchived)
            _snackbarMessage.value = if (isArchived) "Procurement record archived" else "Procurement record unarchived"
        }
    }

    fun toggleArchive(procurement: ProcurementEntity) {
        setProcurementArchived(procurement.id, !procurement.isArchived)
    }

    fun updateProcurement(procurement: ProcurementEntity) {
        viewModelScope.launch {
            repository.updateProcurement(procurement)
        }
    }

    fun deleteDispatch(id: Long, reason: String = "User deletion") {
        viewModelScope.launch {
            repository.deleteDispatch(id, reason)
            _snackbarMessage.value = "Dispatch record deleted & audited."
        }
    }

    fun updateDispatch(dispatch: OutboundDispatchEntity) {
        viewModelScope.launch {
            repository.updateDispatch(dispatch)
        }
    }

    fun depositPdc(uuid: String) {
        viewModelScope.launch {
            repository.markPdcAsDepositedUseCase(uuid)
                .onSuccess { _snackbarMessage.value = "PDC marked as DEPOSITED." }
                .onFailure { _snackbarMessage.value = "Error: ${it.message}" }
        }
    }

    fun presentPdc(uuid: String) {
        viewModelScope.launch {
            repository.markPdcAsPresentedUseCase(uuid)
                .onSuccess { _snackbarMessage.value = "PDC presented for clearing." }
                .onFailure { _snackbarMessage.value = "Error: ${it.message}" }
        }
    }

    fun clearPdc(uuid: String) {
        viewModelScope.launch {
            repository.markPdcAsClearedUseCase(uuid)
                .onSuccess { _snackbarMessage.value = "PDC cleared & liability settled!" }
                .onFailure { _snackbarMessage.value = "Error: ${it.message}" }
        }
    }

    fun bouncePdc(uuid: String, reason: String) {
        viewModelScope.launch {
            repository.markPdcAsBouncedUseCase(uuid, reason)
                .onSuccess { _snackbarMessage.value = "PDC marked as BOUNCED. Payable debt reopened." }
                .onFailure { _snackbarMessage.value = "Error: ${it.message}" }
        }
    }

    fun closeDayEnd(financialYear: String = "26-27", facilityId: String = "MAIN") {
        viewModelScope.launch {
            repository.closeDayEndUseCase(financialYear, facilityId)
                .onSuccess { _snackbarMessage.value = "Day-End Closing complete! Sequence numbers frozen." }
                .onFailure { _snackbarMessage.value = "Day-End Error: ${it.message}" }
        }
    }

    fun refreshSmartInsight(procurements: List<ProcurementEntity>, godowns: List<GodownEntity>) {
        viewModelScope.launch {
            val totalProcured = procurements.sumOf { it.netWeightKg } / 1000.0
            val totalCapacity = godowns.sumOf { it.capacityMt }
            val occupancy = if (totalCapacity > 0) (totalProcured / totalCapacity) * 100 else 0.0
            _smartInsight.value = "Storage Occupancy: ${"%.1f".format(occupancy)}% • Total Stock: ${"%.1f".format(totalProcured)} MT across ${godowns.size} silos."
        }
    }

    fun receiveCorporatePayment(amount: Double, source: String, notes: String) {
        viewModelScope.launch {
            repository.receiveCorporatePayment(amount, source, notes)
            _snackbarMessage.value = "Corporate payment of ₹${"%,.0f".format(amount)} received from $source."
        }
    }

    fun logInterestExpense(amount: Double, notes: String) {
        viewModelScope.launch {
            repository.logInterestExpense(amount, notes)
            _snackbarMessage.value = "Bank interest expense of ₹${"%,.0f".format(amount)} recorded."
        }
    }

    fun endOfSeasonZeroOut(godownId: String) {
        viewModelScope.launch {
            repository.endOfSeasonZeroOut(godownId)
            _snackbarMessage.value = "End of season zero-out audit recorded for $godownId."
        }
    }

    fun addStorageFacilities(facilities: List<Pair<String, Double>>) {
        viewModelScope.launch {
            val entities = facilities.mapIndexed { idx, fac ->
                val idStr = "GODOWN_${(idx + 65).toChar()}"
                GodownEntity(
                    godownId = idStr,
                    displayName = fac.first.ifBlank { "Storage Facility ${idx + 1}" },
                    capacityMt = fac.second.coerceAtLeast(50.0),
                    currentStockMt = 0.0,
                    activeCrop = _activeCrop.value.name,
                    averageMoisture = _activeCrop.value.idealMoisture,
                    temperatureCelsius = 24.0,
                    baseCostPerQuintal = _activeCrop.value.standardMsp,
                    adjustedAvgCostPerQuintal = _activeCrop.value.standardMsp
                )
            }
            repository.insertGodowns(entities)
            _snackbarMessage.value = "${facilities.size} Storage facilities added."
        }
    }

    fun deleteExpense(id: Long) {
        viewModelScope.launch {
            repository.deleteExpenseById(id)
            _snackbarMessage.value = "Expense record deleted."
        }
    }
}
