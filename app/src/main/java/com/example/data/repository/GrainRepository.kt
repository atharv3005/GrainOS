package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.CropType
import com.example.data.model.DispatchStatus
import com.example.data.model.DocumentSequenceEntity
import com.example.data.model.DocumentType
import com.example.data.model.ExpenseEntryEntity
import com.example.data.model.GodownEntity
import com.example.data.model.InventoryMovementEntity
import com.example.data.model.InventoryReconciliationEntity
import com.example.data.model.IoTTelemetryEntity
import com.example.data.model.OutboundDispatchEntity
import com.example.data.model.PartyEntity
import com.example.data.model.PaymentAllocationEntity
import com.example.data.model.PaymentMode
import com.example.data.model.PaymentStatus
import com.example.data.model.PdcStatus
import com.example.data.model.ProcurementEntity
import com.example.data.model.ProcurementStatus
import com.example.data.model.QualityGrade
import com.example.data.model.StorageFacilityIntakeEntity
import com.example.data.model.TradeBookingEntity
import com.example.data.model.TruckRejectionEntity
import com.example.data.model.VendorLedgerEntity
import com.example.domain.usecase.AllocatePaymentRequest
import com.example.domain.usecase.AllocatePaymentUseCase
import com.example.domain.usecase.ApmcTaxCalculator
import com.example.domain.usecase.CloseDayEndUseCase
import com.example.domain.usecase.CreateDispatchRequest
import com.example.domain.usecase.CreateDispatchUseCase
import com.example.domain.usecase.CreateProcurementRequest
import com.example.domain.usecase.CreateProcurementUseCase
import com.example.domain.usecase.DayEndReport
import com.example.domain.usecase.GenerateDocumentNumberUseCase
import com.example.domain.usecase.MarkPdcAsBouncedUseCase
import com.example.domain.usecase.MarkPdcAsClearedUseCase
import com.example.domain.usecase.MarkPdcAsDepositedUseCase
import com.example.domain.usecase.MarkPdcAsPresentedUseCase
import com.example.domain.usecase.PostInventoryMovementUseCase
import com.example.domain.usecase.TdsCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class GrainRepository(private val db: AppDatabase) {
    private val procurementDao = db.procurementDao()
    private val godownDao = db.godownDao()
    private val storageIntakeDao = db.storageIntakeDao()
    private val dispatchDao = db.dispatchDao()
    private val telemetryDao = db.iotTelemetryDao()
    private val tradeDao = db.tradeDao()
    private val expenseDao = db.expenseDao()
    private val truckRejectionDao = db.truckRejectionDao()
    private val vendorLedgerDao = db.vendorLedgerDao()
    private val inventoryReconciliationDao = db.inventoryReconciliationDao()
    private val partyDao = db.partyDao()
    private val documentSequenceDao = db.documentSequenceDao()
    private val auditTrailDao = db.auditTrailDao()
    private val inventoryMovementDao = db.inventoryMovementDao()
    private val paymentAllocationDao = db.paymentAllocationDao()

    // Sub-repositories
    val partyRepository = PartyRepository(partyDao)
    val documentSequenceRepository = DocumentSequenceRepository(documentSequenceDao)
    val auditTrailRepository = AuditTrailRepository(auditTrailDao)
    val inventoryMovementRepository = InventoryMovementRepository(inventoryMovementDao)
    val paymentAllocationRepository = PaymentAllocationRepository(paymentAllocationDao)

    // Domain Use Cases
    val apmcCalculator = ApmcTaxCalculator()
    val tdsCalculator = TdsCalculator()
    val generateDocNumber = GenerateDocumentNumberUseCase(documentSequenceRepository)
    val postMovement = PostInventoryMovementUseCase(inventoryMovementRepository, auditTrailRepository)
    val createProcurementUseCase = CreateProcurementUseCase(
        procurementDao, godownDao, vendorLedgerDao, partyRepository,
        generateDocNumber, postMovement, auditTrailRepository, apmcCalculator, tdsCalculator, db
    )
    val createDispatchUseCase = CreateDispatchUseCase(
        dispatchDao, godownDao, vendorLedgerDao, partyRepository,
        generateDocNumber, postMovement, auditTrailRepository, db
    )
    val allocatePaymentUseCase = AllocatePaymentUseCase(paymentAllocationRepository, auditTrailRepository)
    val markPdcAsDepositedUseCase = MarkPdcAsDepositedUseCase(vendorLedgerDao, auditTrailRepository)
    val markPdcAsPresentedUseCase = MarkPdcAsPresentedUseCase(vendorLedgerDao, auditTrailRepository)
    val markPdcAsClearedUseCase = MarkPdcAsClearedUseCase(vendorLedgerDao, procurementDao, auditTrailRepository)
    val markPdcAsBouncedUseCase = MarkPdcAsBouncedUseCase(vendorLedgerDao, procurementDao, auditTrailRepository)
    val closeDayEndUseCase = CloseDayEndUseCase(procurementDao, dispatchDao, godownDao, documentSequenceRepository, auditTrailRepository)

    // Phase 2 & Phase 3 Managers & Engines
    val dataMigrationManager = com.example.data.local.DataMigrationManager(db)
    val approvalWorkflowEngine = com.example.domain.usecase.ApprovalWorkflowEngine(db.approvalDao(), db.userDao())
    val cashDrawerManager = com.example.domain.managers.CashDrawerReconciliationManager(db.cashDrawerDao(), db.procurementDao(), db.expenseDao(), db.vendorLedgerDao())
    val postJournalEntryUseCase = com.example.domain.usecase.PostJournalEntryUseCase(db.generalLedgerDao())
    val stressTestRunner = com.example.domain.managers.StressTestRunner(db)

    // Flow Streams
    val allProcurements: Flow<List<ProcurementEntity>> = procurementDao.getAllProcurements()
    val allGodowns: Flow<List<GodownEntity>> = godownDao.getAllGodowns()
    val allStorageIntakes: Flow<List<StorageFacilityIntakeEntity>> = storageIntakeDao.getAllIntakes()
    val allDispatches: Flow<List<OutboundDispatchEntity>> = dispatchDao.getAllDispatches()
    val recentTelemetry: Flow<List<IoTTelemetryEntity>> = telemetryDao.getRecentTelemetry()
    val allTrades: Flow<List<TradeBookingEntity>> = tradeDao.getAllTrades()
    val allExpenses: Flow<List<ExpenseEntryEntity>> = expenseDao.getAllExpenses()
    val allRejections: Flow<List<TruckRejectionEntity>> = truckRejectionDao.getAllRejections()
    val allLedgerEntries: Flow<List<VendorLedgerEntity>> = vendorLedgerDao.getAllLedgerEntries()
    val allPdcs: Flow<List<VendorLedgerEntity>> = vendorLedgerDao.getAllPdcsFlow()
    val allReconciliations: Flow<List<InventoryReconciliationEntity>> = inventoryReconciliationDao.getAllReconciliations()
    val allParties: Flow<List<PartyEntity>> = partyRepository.allPartiesFlow
    val allDocumentSequences: Flow<List<DocumentSequenceEntity>> = documentSequenceRepository.allSequencesFlow
    val allInventoryMovements: Flow<List<InventoryMovementEntity>> = inventoryMovementRepository.allMovementsFlow
    val allPaymentAllocations: Flow<List<PaymentAllocationEntity>> = paymentAllocationRepository.allAllocationsFlow
    val allUsers: Flow<List<com.example.security.UserEntity>> = db.userDao().getAllUsersFlow()
    val allApprovals: Flow<List<com.example.data.model.ApprovalRequestEntity>> = db.approvalDao().getAllApprovalsFlow()
    val allGeneralLedger: Flow<List<com.example.data.model.GeneralLedgerEntity>> = db.generalLedgerDao().getAllEntriesFlow()
    val allOrganizations: Flow<List<com.example.data.model.OrganizationEntity>> = db.organizationDao().getAllOrganizationsFlow()

    fun getLedgerEntriesByType(vendorType: String): Flow<List<VendorLedgerEntity>> =
        vendorLedgerDao.getLedgerEntriesByType(vendorType)

    fun getPendingPdcs(): Flow<List<VendorLedgerEntity>> = vendorLedgerDao.getActivePdcsFlow()

    // 1. Inbound Procurement Registration (Sequential Document Numbering)
    suspend fun registerInboundLot(
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
        partyId: Long? = null
    ): Long = withContext(Dispatchers.IO) {
        val token = generateDocNumber(
            financialYear = "26-27",
            facilityId = "MAIN",
            documentType = DocumentType.GIN
        )

        val cumulativePurchases = if (enableTds194q) {
            if (partyId != null) {
                partyRepository.getById(partyId)?.cumulativePurchasesInFy ?: 0.0
            } else {
                procurementDao.getCumulativeFarmerGross(farmerName.trim()) ?: 0.0
            }
        } else {
            0.0
        }

        val procurement = ProcurementEntity(
            tokenNo = token,
            partyId = partyId,
            farmerNameQuick = if (partyId == null) farmerName.trim() else null,
            mobileQuick = if (partyId == null) mobileNumber.trim() else null,
            villageQuick = if (partyId == null) village.trim() else null,
            farmerName = farmerName.trim(),
            mobileNumber = mobileNumber.trim(),
            village = village.trim(),
            vehicleNumber = vehicleNumber.trim().uppercase(),
            panNumber = panNumber.trim().uppercase(),
            isPanVerified = isPanVerified,
            cropType = cropType.name,
            ratePerQuintal = ratePerQuintal,
            applyMandiCess = applyMandiCess,
            enableTds194q = enableTds194q,
            cumulativePurchasesInFy = cumulativePurchases,
            godownAssigned = godownAssigned,
            status = ProcurementStatus.REGISTERED.name,
            paymentStatus = PaymentStatus.PENDING.name,
            createdAt = System.currentTimeMillis()
        )
        val id = procurementDao.insertProcurement(procurement)

        // Audit Trail
        auditTrailRepository.logCreate(
            entityType = "PROCUREMENT",
            entityId = token,
            newStateJson = "{\"token\":\"$token\",\"farmer\":\"${procurement.farmerName}\",\"vehicle\":\"${procurement.vehicleNumber}\"}",
            reason = "Registered inbound lot at gate"
        )

        // Telemetry Event
        telemetryDao.insertTelemetry(
            IoTTelemetryEntity(
                deviceType = "BARRIER_GATE",
                deviceId = "OPT-GATE-01",
                readingValue = 1.0,
                unit = "ANPR_OK",
                status = "OPEN",
                rawPayloadJson = "{\"token\":\"$token\",\"vehicle\":\"${vehicleNumber.trim().uppercase()}\",\"farmer\":\"${farmerName.trim()}\"}",
                latencyMs = 1
            )
        )
        id
    }

    // 2. Gross Weight Recording (Dual Entry: Auto IoT / Manual)
    suspend fun recordGrossWeight(
        procurementId: Long,
        grossWeightKg: Double,
        weightMethod: String = "AUTO"
    ): ProcurementEntity? = withContext(Dispatchers.IO) {
        val item = procurementDao.getProcurementById(procurementId) ?: return@withContext null
        val updated = item.copy(
            grossWeightKg = grossWeightKg,
            grossWeightMethod = weightMethod,
            grossTimestamp = System.currentTimeMillis(),
            status = ProcurementStatus.GROSS_WEIGHED.name,
            whatsappEntrySent = true
        )
        procurementDao.updateProcurement(updated)

        telemetryDao.insertTelemetry(
            IoTTelemetryEntity(
                deviceType = "WEIGHBRIDGE",
                deviceId = "WB-DIGI-01",
                readingValue = grossWeightKg,
                unit = "kg",
                status = "GROSS_CAPTURED",
                rawPayloadJson = "{\"token\":\"${item.tokenNo}\",\"gross_kg\":$grossWeightKg,\"method\":\"$weightMethod\"}",
                latencyMs = 1
            )
        )
        updated
    }

    // 3. Moisture Testing & Grading
    suspend fun recordMoistureAndGrading(
        procurementId: Long,
        moisturePct: Double,
        godownAssigned: String,
        manualFarmerRate: Double? = null
    ): ProcurementEntity? = withContext(Dispatchers.IO) {
        val item = procurementDao.getProcurementById(procurementId) ?: return@withContext null
        val crop = CropType.entries.find { it.name == item.cropType } ?: CropType.MAIZE

        val grade = when {
            moisturePct <= crop.idealMoisture -> QualityGrade.GRADE_A
            moisturePct <= crop.maxSafeMoisture -> QualityGrade.GRADE_B
            moisturePct <= 18.0 -> QualityGrade.GRADE_C
            else -> QualityGrade.REJECTED
        }

        val finalRate = if (manualFarmerRate != null && manualFarmerRate > 0) {
            manualFarmerRate
        } else if (item.ratePerQuintal > 0) {
            item.ratePerQuintal
        } else {
            crop.standardMsp * grade.rateFactor
        }

        val updated = item.copy(
            moisturePercentage = moisturePct,
            qualityGrade = grade.name,
            ratePerQuintal = finalRate,
            godownAssigned = godownAssigned,
            status = ProcurementStatus.MOISTURE_TESTED.name
        )
        procurementDao.updateProcurement(updated)

        telemetryDao.insertTelemetry(
            IoTTelemetryEntity(
                deviceType = "MOISTURE_METER",
                deviceId = "MM-GRAIN-PRO",
                readingValue = moisturePct,
                unit = "%",
                status = "MOISTURE_LOGGED",
                rawPayloadJson = "{\"token\":\"${item.tokenNo}\",\"moisture\":$moisturePct,\"grade\":\"${grade.name}\",\"rate\":$finalRate}",
                latencyMs = 2
            )
        )
        updated
    }

    // 4. Confirm Unloading
    suspend fun confirmUnloading(procurementId: Long): ProcurementEntity? = withContext(Dispatchers.IO) {
        val item = procurementDao.getProcurementById(procurementId) ?: return@withContext null
        val updated = item.copy(status = ProcurementStatus.UNLOADED.name)
        procurementDao.updateProcurement(updated)
        updated
    }

    // 5. Tare Weight & Complete Procurement (with APMC Cess + TDS 194Q + Inventory Movement)
    suspend fun recordTareWeightAndComplete(
        procurementId: Long,
        tareWeightKg: Double,
        bagCount: Int = 0,
        bagWeightKg: Double = 50.0,
        paymentMode: String = PaymentMode.CASH.name,
        utrOrChequeNo: String = "",
        isPdc: Boolean = false,
        chequeMaturityDate: Long = 0L,
        weightMethod: String = "AUTO",
        overrideFarmerRate: Double? = null
    ): ProcurementEntity? = withContext(Dispatchers.IO) {
        val item = procurementDao.getProcurementById(procurementId) ?: return@withContext null
        val netKg = (item.grossWeightKg - tareWeightKg).coerceAtLeast(0.0)
        val quintals = netKg / 100.0
        val effectiveRate = if (overrideFarmerRate != null && overrideFarmerRate > 0) {
            overrideFarmerRate
        } else if (item.ratePerQuintal > 0) {
            item.ratePerQuintal
        } else {
            val crop = CropType.entries.find { it.name == item.cropType } ?: CropType.MAIZE
            crop.standardMsp
        }
        val grossBill = quintals * effectiveRate

        // APMC Cess
        val cessResult = apmcCalculator.calculate(grossBill, item.applyMandiCess)

        // TDS 194Q
        val prevPurchases = if (item.partyId != null) {
            partyRepository.getById(item.partyId)?.cumulativePurchasesInFy ?: 0.0
        } else {
            procurementDao.getCumulativeFarmerGross(item.farmerName) ?: 0.0
        }
        val hasPan = item.isPanVerified || (item.panNumber.isNotBlank() && item.panNumber.length == 10)
        val tdsResult = tdsCalculator.calculate(
            currentBillAmount = grossBill,
            cumulativePurchasesInFy = prevPurchases,
            enableTds194q = item.enableTds194q,
            hasValidPan = hasPan
        )

        // Final Net Payable
        val netPayable = (grossBill - cessResult.totalCess - tdsResult.tdsDeductedAmount).coerceAtLeast(0.0)

        val updated = item.copy(
            tareWeightKg = tareWeightKg,
            netWeightKg = netKg,
            bagCount = if (bagCount > 0) bagCount else (netKg / bagWeightKg).toInt().coerceAtLeast(1),
            bagWeightKg = bagWeightKg,
            ratePerQuintal = effectiveRate,
            grossBillAmount = grossBill,
            mandiMarketFee = cessResult.marketFee,
            mandiSupervisoryCharge = cessResult.supervisoryCharge,
            totalMandiCess = cessResult.totalCess,
            isTdsApplicable = tdsResult.isTdsApplicable,
            tdsRate = tdsResult.tdsRate,
            tdsDeductedAmount = tdsResult.tdsDeductedAmount,
            isTcsExempt = tdsResult.isTcsExempt,
            totalAmount = netPayable,
            paymentMode = paymentMode,
            utrOrChequeNo = utrOrChequeNo,
            chequeDate = chequeMaturityDate,
            isPdc = isPdc,
            pdcCleared = !isPdc,
            paymentStatus = if (isPdc) PaymentStatus.PENDING.name else PaymentStatus.PAID.name,
            tareWeightMethod = weightMethod,
            tareTimestamp = System.currentTimeMillis(),
            completedTimestamp = System.currentTimeMillis(),
            status = ProcurementStatus.COMPLETED.name,
            whatsappReceiptSent = true
        )
        procurementDao.updateProcurement(updated)

        // Post Inventory Movement
        postMovement(
            movementType = com.example.data.model.InventoryMovementType.RECEIPT,
            sourceEntityType = "PROCUREMENT",
            sourceEntityUuid = updated.uuid,
            facilityId = updated.godownAssigned,
            cropType = updated.cropType,
            quantityKg = updated.netWeightKg,
            costPerQuintalPaise = (effectiveRate * 100).toLong(),
            totalValuePaise = (netPayable * 100).toLong(),
            reason = "Procurement ${updated.tokenNo}"
        )

        // Update Party Cumulative Purchases
        item.partyId?.let { pId ->
            partyRepository.updateCumulativePurchases(pId, grossBill)
        }

        // Storage Intake Entry
        recordStorageIntake(
            storageFacilityNameOrId = item.godownAssigned,
            procurement = updated
        )

        // Vendor Ledger Entry
        vendorLedgerDao.insertLedgerEntry(
            VendorLedgerEntity(
                partyId = item.partyId,
                vendorType = "FARMER",
                vendorName = item.farmerName,
                contactNumber = item.mobileNumber,
                panNumber = item.panNumber,
                transactionType = if (isPdc) "PDC_ISSUED" else "BILL_CREDIT",
                amount = netPayable,
                paymentMode = paymentMode,
                utrOrChequeNo = utrOrChequeNo,
                chequeMaturityDate = if (isPdc) chequeMaturityDate else 0L,
                pdcStatus = if (isPdc) PdcStatus.ISSUED.name else PdcStatus.NONE.name,
                referenceDocNo = item.tokenNo,
                runningBalance = if (isPdc) netPayable else 0.0,
                notes = "Procurement: Net ${netKg}kg (${quintals}Q) of ${item.cropType} @ ₹$effectiveRate/Qtl. TDS: ₹${tdsResult.tdsDeductedAmount}, Cess: ₹${cessResult.totalCess}"
            )
        )
        updated
    }

    // 6. Inventory Reconciliation & Moisture Shrinkage
    suspend fun reconcileInventoryShrinkage(
        godownId: String,
        cropType: String,
        initialStockKg: Double,
        auditedStockKg: Double,
        initialMoisturePct: Double,
        currentMoisturePct: Double,
        baseCostPerQuintal: Double,
        notes: String = ""
    ): Long = withContext(Dispatchers.IO) {
        val lostKg = (initialStockKg - auditedStockKg).coerceAtLeast(0.0)
        val shrinkagePct = if (initialStockKg > 0) (lostKg / initialStockKg) * 100.0 else 0.0
        val baseCostPerKg = baseCostPerQuintal / 100.0
        val totalLossCapitalized = lostKg * baseCostPerKg

        val capitalizedPerRemainingKg = if (auditedStockKg > 0) totalLossCapitalized / auditedStockKg else 0.0
        val newAdjustedCostPerKg = baseCostPerKg + capitalizedPerRemainingKg
        val newAdjustedCostPerQuintal = newAdjustedCostPerKg * 100.0

        val recNo = generateDocNumber(
            financialYear = "26-27",
            facilityId = godownId,
            documentType = DocumentType.REC
        )

        val rec = InventoryReconciliationEntity(
            reconciliationNo = recNo,
            godownId = godownId,
            cropType = cropType,
            initialStockKg = initialStockKg,
            auditedStockKg = auditedStockKg,
            lostWeightKg = lostKg,
            shrinkagePercentage = shrinkagePct,
            initialMoisturePct = initialMoisturePct,
            currentMoisturePct = currentMoisturePct,
            originalCostPerKg = baseCostPerKg,
            capitalizedCostPerRemainingKg = capitalizedPerRemainingKg,
            originalCostPerQuintal = baseCostPerQuintal,
            adjustedCostPerQuintal = newAdjustedCostPerQuintal,
            totalLossAmountCapitalized = totalLossCapitalized,
            notes = notes.trim()
        )
        val recId = inventoryReconciliationDao.insertReconciliation(rec)

        // Post Inventory Loss Adjustment Movement
        if (lostKg > 0) {
            postMovement(
                movementType = com.example.data.model.InventoryMovementType.SHRINKAGE,
                sourceEntityType = "RECONCILIATION",
                sourceEntityUuid = rec.uuid,
                facilityId = godownId,
                cropType = cropType,
                quantityKg = -lostKg,
                costPerQuintalPaise = (baseCostPerQuintal * 100).toLong(),
                totalValuePaise = (totalLossCapitalized * 100).toLong(),
                reason = "Moisture Shrinkage loss reconciled in $godownId"
            )
        }

        // Update Godown
        val godown = godownDao.getGodownById(godownId)
        if (godown != null) {
            val updated = godown.copy(
                currentStockMt = auditedStockKg / 1000.0,
                averageMoisture = currentMoisturePct,
                cumulativeShrinkageKg = godown.cumulativeShrinkageKg + lostKg,
                shrinkageCapitalizedCost = godown.shrinkageCapitalizedCost + totalLossCapitalized,
                adjustedAvgCostPerQuintal = newAdjustedCostPerQuintal,
                lastUpdated = System.currentTimeMillis()
            )
            godownDao.updateGodown(updated)
        }
        recId
    }

    // 7. Outbound Dispatch via Use Case
    suspend fun createOutboundDispatch(
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
    ): Long = withContext(Dispatchers.IO) {
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
        val result = createDispatchUseCase(request)
        val saved = result.getOrThrow()
        onComplete(saved.id)
        saved.id
    }

    suspend fun getDispatchById(id: Long): OutboundDispatchEntity? = dispatchDao.getDispatchById(id)

    // 8. Settle Unloaded Dispatch
    suspend fun settleUnloadedDispatch(
        dispatchId: Long,
        companyUnloadedWeightKg: Double,
        qualityPenalty: Double,
        freightCost: Double,
        laborCost: Double,
        bagCost: Double,
        miscCost: Double,
        brokerName: String,
        brokerageRatePerQtl: Double
    ) = withContext(Dispatchers.IO) {
        val dispatch = dispatchDao.getDispatchById(dispatchId) ?: return@withContext
        val unloadedQuintals = companyUnloadedWeightKg / 100.0
        val shortageKg = (dispatch.netLoadedWeightKg - companyUnloadedWeightKg).coerceAtLeast(0.0)

        val netRevenue = (unloadedQuintals * dispatch.ratePerQuintal) - qualityPenalty
        val finalBrokerage = unloadedQuintals * brokerageRatePerQtl
        val totalOverheads = freightCost + laborCost + bagCost + miscCost + finalBrokerage
        val netProfit = netRevenue - (dispatch.fifoProcurementCost + totalOverheads)

        val updated = dispatch.copy(
            companyUnloadedWeightKg = companyUnloadedWeightKg,
            weightShortageKg = shortageKg,
            companyRateDeductionPenalty = qualityPenalty,
            freightCost = freightCost,
            loadingLaborCost = laborCost,
            bagCost = bagCost,
            miscCost = miscCost,
            brokerName = brokerName,
            brokerageRatePerQtl = brokerageRatePerQtl,
            finalBrokerageFee = finalBrokerage,
            actualNetRevenue = netRevenue,
            actualNetProfit = netProfit,
            status = DispatchStatus.UNLOADED.name,
            unloadedTimestamp = System.currentTimeMillis()
        )
        dispatchDao.updateDispatch(updated)

        // Log Expenses in Vendor Ledger
        if (freightCost > 0) {
            vendorLedgerDao.insertLedgerEntry(
                VendorLedgerEntity(
                    partyId = dispatch.transporterPartyId,
                    vendorType = "TRANSPORTER",
                    vendorName = "Truck ${dispatch.vehicleNumber}",
                    transactionType = "BILL_CREDIT",
                    amount = freightCost,
                    paymentMode = "CASH",
                    referenceDocNo = dispatch.dispatchNo,
                    notes = "Freight for dispatch to ${dispatch.destination}"
                )
            )
        }
        if (laborCost > 0) {
            vendorLedgerDao.insertLedgerEntry(
                VendorLedgerEntity(
                    vendorType = "LABOR",
                    vendorName = "Hamali Gang",
                    transactionType = "BILL_CREDIT",
                    amount = laborCost,
                    paymentMode = "CASH",
                    referenceDocNo = dispatch.dispatchNo,
                    notes = "Loading labor for ${dispatch.dispatchNo}"
                )
            )
        }
        if (finalBrokerage > 0 && brokerName.isNotBlank()) {
            vendorLedgerDao.insertLedgerEntry(
                VendorLedgerEntity(
                    vendorType = "BROKER",
                    vendorName = brokerName,
                    transactionType = "BILL_CREDIT",
                    amount = finalBrokerage,
                    paymentMode = "CASH",
                    referenceDocNo = dispatch.dispatchNo,
                    notes = "Brokerage at ₹$brokerageRatePerQtl/Qtl"
                )
            )
        }
    }

    // 9. Truck Rejection & 50% Labor Rule with Document Sequencing
    suspend fun recordTruckRejection(
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
    ): Long = withContext(Dispatchers.IO) {
        val returnBagShiftingLabor = originalLoadingLaborCost * 0.50
        val totalLoss = transportLoss + penaltiesDemurrage + qualitySalvageDeduction + returnBagShiftingLabor

        val rejectionNo = generateDocNumber(
            financialYear = "26-27",
            facilityId = "MAIN",
            documentType = DocumentType.REJ
        )

        val rejection = TruckRejectionEntity(
            rejectionNo = rejectionNo,
            truckNumber = truckNumber.trim().uppercase(),
            buyerOrCompany = buyerOrCompany.trim(),
            cropType = cropType.name,
            dispatchedWeightKg = dispatchedWeightKg,
            rejectionReason = rejectionReason.trim(),
            transportLoss = transportLoss,
            penaltiesDemurrage = penaltiesDemurrage,
            originalLoadingLaborCost = originalLoadingLaborCost,
            returnBagShiftingLaborCost = returnBagShiftingLabor,
            qualitySalvageDeduction = qualitySalvageDeduction,
            totalRejectionLoss = totalLoss,
            salvageAction = salvageAction.trim(),
            salvageRealizedRatePerQtl = salvageRealizedRatePerQtl,
            notes = notes.trim()
        )
        val id = truckRejectionDao.insertRejection(rejection)

        // Find dispatch to restore stock
        val dispatch = dispatchDao.getDispatchByTruck(truckNumber)
        val godownSource = dispatch?.godownSource ?: "GODOWN_A"
        godownDao.addStock(godownSource, dispatchedWeightKg / 1000.0)

        // Post Inventory Return Movement
        postMovement(
            movementType = com.example.data.model.InventoryMovementType.RECEIPT,
            sourceEntityType = "REJECTION",
            sourceEntityUuid = rejection.uuid,
            facilityId = godownSource,
            cropType = cropType.name,
            quantityKg = dispatchedWeightKg,
            reason = "Restored stock from rejected truck $truckNumber"
        )

        if (dispatch != null) {
            dispatchDao.updateDispatch(dispatch.copy(status = com.example.data.model.DispatchStatus.REJECTED.name))
        }

        // Ledger Entry
        vendorLedgerDao.insertLedgerEntry(
            VendorLedgerEntity(
                vendorType = "TRANSPORTER",
                vendorName = "Truck $truckNumber Rejection Claim",
                transactionType = "PENALTY_DEDUCTION",
                amount = -totalLoss,
                paymentMode = "CASH",
                referenceDocNo = rejection.rejectionNo,
                notes = "Rejection Loss: Transport ₹$transportLoss + 50% Shifting Labor ₹$returnBagShiftingLabor + Penalties ₹$penaltiesDemurrage"
            )
        )

        val expNo = generateDocNumber(
            financialYear = "26-27",
            facilityId = "MAIN",
            documentType = DocumentType.EXP
        )

        val expense = ExpenseEntryEntity(
            expenseNo = expNo,
            truckOrBatchRef = truckNumber,
            cropType = cropType.name,
            laborCost = returnBagShiftingLabor,
            bagsCost = 0.0,
            transportCost = transportLoss,
            miscCost = penaltiesDemurrage + qualitySalvageDeduction,
            miscDescription = "Rejection Penalties",
            totalExpense = totalLoss,
            paidToOrParty = buyerOrCompany,
            paymentMode = "CASH",
            utrOrChequeNo = "",
            timestamp = System.currentTimeMillis()
        )
        expenseDao.insertExpense(expense)
        id
    }

    // 10. Manual Transaction Expense Entry
    suspend fun recordManualExpense(
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
    ): Long = withContext(Dispatchers.IO) {
        val expNo = generateDocNumber(
            financialYear = "26-27",
            facilityId = "MAIN",
            documentType = DocumentType.EXP
        )

        val expense = ExpenseEntryEntity(
            expenseNo = expNo,
            partyId = partyId,
            truckOrBatchRef = truckOrBatchRef.trim(),
            cropType = cropType.name,
            laborCost = laborCost,
            bagsCost = bagsCost,
            transportCost = transportCost,
            miscCost = miscCost,
            miscDescription = miscDescription.trim(),
            totalExpense = laborCost + bagsCost + transportCost + miscCost,
            paidToOrParty = paidToOrParty.trim(),
            paymentMode = paymentMode,
            utrOrChequeNo = utrOrChequeNo.trim(),
            notes = notes.trim()
        )
        val id = expenseDao.insertExpense(expense)

        vendorLedgerDao.insertLedgerEntry(
            VendorLedgerEntity(
                partyId = partyId,
                vendorType = if (laborCost > 0) "LABOR" else "TRANSPORTER",
                vendorName = paidToOrParty.trim().ifEmpty { "Cash Party" },
                transactionType = "PAYMENT_DEBIT",
                amount = expense.totalExpense,
                paymentMode = paymentMode,
                utrOrChequeNo = utrOrChequeNo,
                referenceDocNo = expense.expenseNo,
                notes = "Expense: Labor ₹$laborCost, Bags ₹$bagsCost, Freight ₹$transportCost, Misc (${miscDescription}) ₹$miscCost"
            )
        )
        id
    }

    // 11. Trade Booking with Document Sequencing
    suspend fun bookTrade(
        cropType: CropType,
        brokerOrBuyerName: String,
        quantityTons: Double,
        bookedPricePerQuintal: Double,
        farmerPurchasePricePerQuintal: Double,
        laborPerQuintal: Double,
        bagCostPerQuintal: Double,
        transportPerQuintal: Double,
        brokeragePerQuintal: Double,
        notes: String = "",
        buyerPartyId: Long? = null
    ): Long = withContext(Dispatchers.IO) {
        val tradeNo = generateDocNumber(
            financialYear = "26-27",
            facilityId = "MAIN",
            documentType = DocumentType.TRD
        )

        val trade = TradeBookingEntity(
            tradeNo = tradeNo,
            buyerPartyId = buyerPartyId,
            cropType = cropType.name,
            brokerOrBuyerName = brokerOrBuyerName.trim(),
            quantityTons = quantityTons,
            bookedPricePerQuintal = bookedPricePerQuintal,
            farmerPurchasePricePerQuintal = farmerPurchasePricePerQuintal,
            laborPerQuintal = laborPerQuintal,
            bagCostPerQuintal = bagCostPerQuintal,
            transportPerQuintal = transportPerQuintal,
            brokeragePerQuintal = brokeragePerQuintal,
            tradeStatus = "ACTIVE",
            tradeTimestamp = System.currentTimeMillis(),
            notes = notes.trim()
        )
        tradeDao.insertTrade(trade)
    }

    suspend fun updateTradeStatus(tradeId: Long, newStatus: String) = withContext(Dispatchers.IO) {
        val trade = tradeDao.getTradeById(tradeId) ?: return@withContext
        tradeDao.updateTrade(trade.copy(tradeStatus = newStatus))
    }

    suspend fun deleteTrade(tradeId: Long, reason: String = "Deleted trade") = withContext(Dispatchers.IO) {
        val trade = tradeDao.getTradeById(tradeId)
        if (trade != null) {
            auditTrailRepository.logDelete(
                entityType = "TRADE_BOOKING",
                entityId = trade.tradeNo,
                previousStateJson = "{\"tradeNo\":\"${trade.tradeNo}\",\"tons\":${trade.quantityTons}}",
                reason = reason
            )
            tradeDao.deleteTradeById(tradeId)
        }
    }

    suspend fun deleteExpenseById(expenseId: Long, reason: String = "Deleted expense") = withContext(Dispatchers.IO) {
        val exp = expenseDao.getExpenseById(expenseId)
        if (exp != null) {
            auditTrailRepository.logDelete(
                entityType = "MANUAL_EXPENSE",
                entityId = exp.expenseNo,
                previousStateJson = "{\"expenseNo\":\"${exp.expenseNo}\",\"total\":${exp.totalExpense}}",
                reason = reason
            )
            expenseDao.deleteExpense(expenseId)
        }
    }

    suspend fun deleteTruckRejection(rejectionId: Long, reason: String = "Deleted rejection") = withContext(Dispatchers.IO) {
        val rej = truckRejectionDao.getRejectionById(rejectionId)
        if (rej != null) {
            auditTrailRepository.logDelete(
                entityType = "TRUCK_REJECTION",
                entityId = rej.rejectionNo,
                previousStateJson = "{\"rejectionNo\":\"${rej.rejectionNo}\",\"loss\":${rej.totalRejectionLoss}}",
                reason = reason
            )
            truckRejectionDao.deleteRejection(rejectionId)
        }
    }

    suspend fun updatePaymentStatus(procurementId: Long, newStatus: PaymentStatus) = withContext(Dispatchers.IO) {
        val item = procurementDao.getProcurementById(procurementId) ?: return@withContext
        procurementDao.updateProcurement(item.copy(paymentStatus = newStatus.name))
    }

    suspend fun setProcurementArchived(procurementId: Long, isArchived: Boolean) = withContext(Dispatchers.IO) {
        procurementDao.updateArchiveStatus(procurementId, isArchived)
    }

    suspend fun deleteProcurement(procurementId: Long, reason: String = "Deleted procurement") = withContext(Dispatchers.IO) {
        val item = procurementDao.getProcurementById(procurementId)
        if (item != null) {
            auditTrailRepository.logDelete(
                entityType = "PROCUREMENT",
                entityId = item.tokenNo,
                previousStateJson = "{\"token\":\"${item.tokenNo}\",\"farmer\":\"${item.farmerName}\"}",
                reason = reason
            )
            procurementDao.deleteProcurement(procurementId)
        }
    }

    suspend fun deleteDispatch(dispatchId: Long, reason: String = "Deleted dispatch") = withContext(Dispatchers.IO) {
        val item = dispatchDao.getDispatchById(dispatchId)
        if (item != null) {
            auditTrailRepository.logDelete(
                entityType = "DISPATCH",
                entityId = item.dispatchNo,
                previousStateJson = "{\"dispatchNo\":\"${item.dispatchNo}\",\"buyer\":\"${item.buyerName}\"}",
                reason = reason
            )
            dispatchDao.deleteDispatch(dispatchId)
        }
    }

    suspend fun updateProcurement(procurement: ProcurementEntity) = withContext(Dispatchers.IO) {
        procurementDao.updateProcurement(procurement)
    }

    suspend fun updateDispatch(dispatch: OutboundDispatchEntity) = withContext(Dispatchers.IO) {
        dispatchDao.updateDispatch(dispatch)
    }

    suspend fun insertLedgerEntry(entry: VendorLedgerEntity): Long = withContext(Dispatchers.IO) {
        vendorLedgerDao.insertLedgerEntry(entry)
    }

    suspend fun insertExpenseEntry(entry: ExpenseEntryEntity): Long = withContext(Dispatchers.IO) {
        expenseDao.insertExpense(entry)
    }

    suspend fun insertGodowns(godowns: List<GodownEntity>) = withContext(Dispatchers.IO) {
        godownDao.insertGodowns(godowns)
    }

    suspend fun getGodownsList(): List<GodownEntity> = withContext(Dispatchers.IO) {
        godownDao.getAllGodowns().firstOrNull() ?: emptyList()
    }

    // Storage Facility Lot Intake & Grain Details Storage Persistence
    suspend fun recordStorageIntake(
        storageFacilityNameOrId: String,
        procurement: ProcurementEntity,
        customMoisture: Double? = null
    ): StorageFacilityIntakeEntity = withContext(Dispatchers.IO) {
        val godown = godownDao.findGodown(storageFacilityNameOrId)
            ?: godownDao.getGodownById(storageFacilityNameOrId)
            ?: godownDao.getFirstGodown()

        val facilityId = godown?.godownId ?: "GODOWN_A"
        val facilityName = godown?.displayName ?: storageFacilityNameOrId.ifBlank { "Storage Facility A" }

        val netKg = procurement.netWeightKg
        val incomingMt = netKg / 1000.0
        val incomingMoisture = customMoisture
            ?: (if (procurement.moisturePercentage > 0.0) procurement.moisturePercentage else 12.4)

        val intakeEntity = StorageFacilityIntakeEntity(
            storageFacilityId = facilityId,
            storageFacilityName = facilityName,
            tokenNo = procurement.tokenNo,
            farmerName = procurement.farmerName,
            mobileNumber = procurement.mobileNumber,
            village = procurement.village,
            vehicleNumber = procurement.vehicleNumber,
            cropType = procurement.cropType,
            qualityGrade = procurement.qualityGrade.ifBlank { "GRADE_A" },
            grossWeightKg = procurement.grossWeightKg,
            tareWeightKg = procurement.tareWeightKg,
            netWeightKg = netKg,
            netWeightMt = incomingMt,
            bagCount = if (procurement.bagCount > 0) procurement.bagCount else (netKg / 50.0).toInt().coerceAtLeast(1),
            bagWeightKg = if (procurement.bagWeightKg > 0) procurement.bagWeightKg else 50.0,
            moisturePercentage = incomingMoisture,
            temperatureCelsius = godown?.temperatureCelsius ?: 24.0,
            ratePerQuintal = procurement.ratePerQuintal,
            grossBillAmount = procurement.grossBillAmount,
            totalAmount = procurement.totalAmount,
            paymentStatus = procurement.paymentStatus,
            paymentMode = procurement.paymentMode,
            intakeTimestamp = if (procurement.completedTimestamp > 0) procurement.completedTimestamp else System.currentTimeMillis(),
            notes = "Stored into $facilityName • Moisture: $incomingMoisture% • Net: ${netKg.toInt()} kg"
        )

        storageIntakeDao.insertIntake(intakeEntity)

        if (godown != null) {
            val prevStockMt = godown.currentStockMt
            val newStockMt = (prevStockMt + incomingMt).coerceAtMost(godown.capacityMt)
            val newAvgMoisture = if (newStockMt > 0.0) {
                ((prevStockMt * godown.averageMoisture) + (incomingMt * incomingMoisture)) / newStockMt
            } else {
                incomingMoisture
            }
            val roundedMoisture = (Math.round(newAvgMoisture * 10.0) / 10.0)

            val updatedGodown = godown.copy(
                currentStockMt = newStockMt,
                averageMoisture = roundedMoisture,
                activeCrop = procurement.cropType,
                lastUpdated = System.currentTimeMillis()
            )
            godownDao.updateGodown(updatedGodown)
        }

        intakeEntity
    }

    fun getIntakesForFacility(facilityId: String): Flow<List<StorageFacilityIntakeEntity>> =
        storageIntakeDao.getIntakesForFacility(facilityId)

    suspend fun insertProcurement(procurement: ProcurementEntity): Long = withContext(Dispatchers.IO) {
        procurementDao.insertProcurement(procurement)
    }

    suspend fun deleteStorageIntake(id: Long) = withContext(Dispatchers.IO) {
        val intake = storageIntakeDao.getIntakeById(id)
        if (intake != null) {
            auditTrailRepository.logDelete(
                entityType = "STORAGE_INTAKE",
                entityId = intake.tokenNo,
                previousStateJson = "{\"token\":\"${intake.tokenNo}\",\"facility\":\"${intake.storageFacilityName}\"}",
                reason = "Deleted storage facility intake"
            )
            storageIntakeDao.deleteIntake(id)
        }
    }

    suspend fun deleteStorageIntake(intake: StorageFacilityIntakeEntity) = deleteStorageIntake(intake.id)

    suspend fun emitSimulatedIoTPulse() = withContext(Dispatchers.IO) {
        val godowns = godownDao.getAllGodownsDirect()
        val randomGodown = godowns.randomOrNull() ?: return@withContext
        val moist = (randomGodown.averageMoisture + (Math.random() * 0.8 - 0.4)).coerceIn(10.0, 18.0)
        val telemetry = IoTTelemetryEntity(
            deviceType = "SILO_SENSOR",
            deviceId = "IOT-${randomGodown.godownId}",
            readingValue = Math.round(moist * 10.0) / 10.0,
            unit = "%",
            status = if (moist > 14.0) "ACTIVE_AERATION" else "OPTIMAL",
            rawPayloadJson = "{\"facility\":\"${randomGodown.displayName}\",\"moisture\":$moist}",
            timestamp = System.currentTimeMillis()
        )
        telemetryDao.insertTelemetry(telemetry)
    }

    suspend fun endOfSeasonZeroOut(godownId: String) = withContext(Dispatchers.IO) {
        val godown = godownDao.getGodownById(godownId) ?: return@withContext
        auditTrailRepository.logAction(
            entityType = "GODOWN_STOCK",
            entityId = godownId,
            action = com.example.data.model.AuditAction.LOCK.name,
            reason = "End of Season Audit Zero-Out for $godownId",
            previousStateJson = "{\"currentStockMt\":${godown.currentStockMt}}"
        )
        godownDao.updateGodown(godown.copy(currentStockMt = 0.0, lastUpdated = System.currentTimeMillis()))
    }

    suspend fun receiveCorporatePayment(amount: Double, source: String, notes: String) = withContext(Dispatchers.IO) {
        val entry = VendorLedgerEntity(
            vendorType = "CORPORATE_BUYER",
            vendorName = source,
            transactionType = "PAYMENT_RECEIVED",
            amount = amount,
            utrOrChequeNo = "BANK-REC-${System.currentTimeMillis() % 100000}",
            referenceDocNo = "CORP-PAY",
            notes = notes,
            runningBalance = 0.0
        )
        vendorLedgerDao.insertLedgerEntry(entry)
    }

    suspend fun logInterestExpense(amount: Double, notes: String) = withContext(Dispatchers.IO) {
        val entry = ExpenseEntryEntity(
            expenseNo = "INT-${System.currentTimeMillis() % 100000}",
            truckOrBatchRef = "BANK-INTEREST",
            cropType = "ALL",
            laborCost = 0.0,
            bagsCost = 0.0,
            transportCost = 0.0,
            miscCost = amount,
            miscDescription = "Bank Interest / CC Charges",
            paidToOrParty = "Bank CC / OD Interest",
            notes = notes
        )
        expenseDao.insertExpense(entry)
    }
}
