package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.CropType
import com.example.data.model.DispatchStatus
import com.example.data.model.ExpenseEntryEntity
import com.example.data.model.GodownEntity
import com.example.data.model.InventoryReconciliationEntity
import com.example.data.model.IoTTelemetryEntity
import com.example.data.model.OutboundDispatchEntity
import com.example.data.model.PdcStatus
import com.example.data.model.PaymentMode
import com.example.data.model.PaymentStatus
import com.example.data.model.ProcurementEntity
import com.example.data.model.ProcurementStatus
import com.example.data.model.QualityGrade
import com.example.data.model.StorageFacilityIntakeEntity
import com.example.data.model.TradeBookingEntity
import com.example.data.model.TruckRejectionEntity
import com.example.data.model.VendorLedgerEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import kotlin.random.Random

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

    val allProcurements: Flow<List<ProcurementEntity>> = procurementDao.getAllProcurements()
    val allGodowns: Flow<List<GodownEntity>> = godownDao.getAllGodowns()
    val allStorageIntakes: Flow<List<StorageFacilityIntakeEntity>> = storageIntakeDao.getAllIntakes()
    val allDispatches: Flow<List<OutboundDispatchEntity>> = dispatchDao.getAllDispatches()
    val recentTelemetry: Flow<List<IoTTelemetryEntity>> = telemetryDao.getRecentTelemetry()
    val allTrades: Flow<List<TradeBookingEntity>> = tradeDao.getAllTrades()
    val allExpenses: Flow<List<ExpenseEntryEntity>> = expenseDao.getAllExpenses()
    val allRejections: Flow<List<TruckRejectionEntity>> = truckRejectionDao.getAllRejections()
    val allLedgerEntries: Flow<List<VendorLedgerEntity>> = vendorLedgerDao.getAllLedgerEntries()
    val allReconciliations: Flow<List<InventoryReconciliationEntity>> = inventoryReconciliationDao.getAllReconciliations()

    fun getLedgerEntriesByType(vendorType: String): Flow<List<VendorLedgerEntity>> =
        vendorLedgerDao.getLedgerEntriesByType(vendorType)

    fun getPendingPdcs(): Flow<List<VendorLedgerEntity>> = vendorLedgerDao.getPendingPdcs()

    // 1. Inbound Procurement Registration
    suspend fun insertProcurement(procurement: ProcurementEntity) = withContext(Dispatchers.IO) {
        procurementDao.insertProcurement(procurement)
    }

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
        godownAssigned: String = "Godown A"
    ): Long = withContext(Dispatchers.IO) {
        val token = "TK-${Random.nextInt(1000, 9999)}"
        
        // Calculate cumulative purchases for TDS 194Q compliance check
        val cumulativePurchases = if (enableTds194q) {
            procurementDao.getCumulativeFarmerGross(farmerName.trim()) ?: 0.0
        } else {
            0.0
        }

        val procurement = ProcurementEntity(
            tokenNo = token,
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

    // 3. Moisture Testing & Grading with Manual Negotiated Farmer Rate Support
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

    // 5. Tare Weight & Complete Procurement (with Maharashtra APMC Cess + TDS 194Q Math & Immediate Godown Stock Increment)
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

        // Maharashtra APMC Cess: 1.0% Market Fee + 0.5% Supervisory Charge = 1.5%
        var marketFee = 0.0
        var supervisoryCharge = 0.0
        var totalCess = 0.0
        if (item.applyMandiCess) {
            marketFee = grossBill * 0.01 // 1.0%
            supervisoryCharge = grossBill * 0.005 // 0.5%
            totalCess = marketFee + supervisoryCharge // 1.5%
        }

        // Section 194Q TDS: 0.1% on excess over ₹50 Lakhs (or 5.0% if no verified PAN)
        var tdsApplicable = false
        var tdsRate = 0.0
        var tdsAmount = 0.0
        var tcsExempt = false

        if (item.enableTds194q) {
            val prevPurchases = procurementDao.getCumulativeFarmerGross(item.farmerName) ?: 0.0
            val newTotal = prevPurchases + grossBill
            val threshold = 5000000.0 // ₹50 Lakhs

            if (newTotal > threshold) {
                tdsApplicable = true
                tcsExempt = true // Flags deduction so seller doesn't charge TCS 206C(1H)
                val hasPan = item.isPanVerified || (item.panNumber.isNotBlank() && item.panNumber.length == 10)
                tdsRate = if (hasPan) 0.001 else 0.05 // 0.1% with PAN, 5.0% without

                val taxableExcess = if (prevPurchases >= threshold) {
                    grossBill
                } else {
                    newTotal - threshold
                }
                tdsAmount = (taxableExcess * tdsRate).coerceAtLeast(0.0)
            }
        }

        // Final Net Payable to Farmer
        val netPayable = (grossBill - totalCess - tdsAmount).coerceAtLeast(0.0)

        val updated = item.copy(
            tareWeightKg = tareWeightKg,
            netWeightKg = netKg,
            bagCount = if (bagCount > 0) bagCount else (netKg / bagWeightKg).toInt().coerceAtLeast(1),
            bagWeightKg = bagWeightKg,
            ratePerQuintal = effectiveRate,
            grossBillAmount = grossBill,
            mandiMarketFee = marketFee,
            mandiSupervisoryCharge = supervisoryCharge,
            totalMandiCess = totalCess,
            isTdsApplicable = tdsApplicable,
            tdsRate = tdsRate,
            tdsDeductedAmount = tdsAmount,
            isTcsExempt = tcsExempt,
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

        // Increment Godown Stock and Store Grain Lot in Storage Facility Database
        recordStorageIntake(
            storageFacilityNameOrId = item.godownAssigned,
            procurement = updated
        )

        // Add to Farmer Vendor Ledger
        vendorLedgerDao.insertLedgerEntry(
            VendorLedgerEntity(
                vendorType = "FARMER",
                vendorName = item.farmerName,
                contactNumber = item.mobileNumber,
                panNumber = item.panNumber,
                transactionType = if (isPdc) "PDC_ISSUED" else "BILL_CREDIT",
                amount = netPayable,
                paymentMode = paymentMode,
                utrOrChequeNo = utrOrChequeNo,
                chequeMaturityDate = if (isPdc) chequeMaturityDate else 0L,
                pdcStatus = if (isPdc) PdcStatus.PENDING_MATURITY.name else PdcStatus.CLEARED.name,
                referenceDocNo = item.tokenNo,
                notes = "Procurement: Net ${netKg}kg (${quintals}Q) of ${item.cropType} @ ₹$effectiveRate/Qtl. TDS: ₹$tdsAmount, Cess: ₹$totalCess"
            )
        )
        updated
    }

    // 6. Inventory Reconciliation & Moisture Shrinkage Capitalization
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

        // Redistribute total loss over remaining audited stock
        val capitalizedPerRemainingKg = if (auditedStockKg > 0) totalLossCapitalized / auditedStockKg else 0.0
        val newAdjustedCostPerKg = baseCostPerKg + capitalizedPerRemainingKg
        val newAdjustedCostPerQuintal = newAdjustedCostPerKg * 100.0

        val rec = InventoryReconciliationEntity(
            reconciliationNo = "REC-${Random.nextInt(1000, 9999)}",
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

        // Update Godown Entity with new Stock, Shrinkage & Adjusted Cost
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

    // 7. Outbound Dispatch & FIFO Stock Depletion
    suspend fun createOutboundDispatch(
        buyerName: String,
        destination: String,
        vehicleNumber: String,
        cropType: String,
        godownSource: String,
        tareWeightKg: Double,
        grossWeightKg: Double,
        ratePerQuintal: Double,
        onComplete: (Long) -> Unit = {}
    ): Long = withContext(Dispatchers.IO) {
        val netKg = (grossWeightKg - tareWeightKg).coerceAtLeast(0.0)
        val invoiceAmt = (netKg / 100.0) * ratePerQuintal
        
        // FIFO Cost calculation from source Godown adjusted average cost
        val godown = godownDao.findGodown(godownSource)
            ?: godownDao.getGodownById(godownSource)
            ?: godownDao.getFirstGodown()
        val fifoUnitCost = godown?.adjustedAvgCostPerQuintal ?: (ratePerQuintal * 0.94)
        val fifoCost = (netKg / 100.0) * fifoUnitCost

        val dispatch = OutboundDispatchEntity(
            dispatchNo = "DSP-${Random.nextInt(1000, 9999)}",
            buyerName = buyerName.trim(),
            destination = destination.trim(),
            vehicleNumber = vehicleNumber.trim().uppercase(),
            cropType = cropType,
            godownSource = godownSource,
            tareWeightKg = tareWeightKg,
            grossWeightKg = grossWeightKg,
            netLoadedWeightKg = netKg,
            ratePerQuintal = ratePerQuintal,
            totalInvoiceAmount = invoiceAmt,
            fifoProcurementCost = fifoCost,
            status = DispatchStatus.IN_TRANSIT.name,
            timestamp = System.currentTimeMillis()
        )
        val id = dispatchDao.insertDispatch(dispatch)

        // Deduct from Godown Stock immediately in real time
        if (godown != null) {
            val newStock = (godown.currentStockMt - (netKg / 1000.0)).coerceAtLeast(0.0)
            godownDao.updateGodown(godown.copy(currentStockMt = newStock, lastUpdated = System.currentTimeMillis()))
        }
        onComplete(id)
        id
    }

    suspend fun getDispatchById(id: Long): OutboundDispatchEntity? = dispatchDao.getDispatchById(id)

    // 8. Settle Unloaded Dispatch & Calculate Actual Net Profit
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

    // 9. Truck Rejection & 50% Extra Labor Rule
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
        // Strict Rule: Return bag shifting labor is calculated at exactly 50% of original loading labor
        val returnBagShiftingLabor = originalLoadingLaborCost * 0.50
        val totalLoss = transportLoss + penaltiesDemurrage + qualitySalvageDeduction + returnBagShiftingLabor

        val rejection = TruckRejectionEntity(
            rejectionNo = "REJ-${Random.nextInt(1000, 9999)}",
            truckNumber = truckNumber.trim().uppercase(),
            buyerOrCompany = buyerOrCompany.trim(),
            cropType = cropType.name,
            dispatchedWeightKg = dispatchedWeightKg,
            rejectionReason = rejectionReason.trim(),
            transportLoss = transportLoss,
            penaltiesDemurrage = penaltiesDemurrage,
            originalLoadingLaborCost = originalLoadingLaborCost,
            returnBagShiftingLaborCost = returnBagShiftingLabor, // 50% extra labor
            qualitySalvageDeduction = qualitySalvageDeduction,
            totalRejectionLoss = totalLoss,
            salvageAction = salvageAction.trim(),
            salvageRealizedRatePerQtl = salvageRealizedRatePerQtl,
            notes = notes.trim()
        )
        val id = truckRejectionDao.insertRejection(rejection)

        // Add the grain weight back into the selected Silo
        val godown = godownDao.getFirstGodown() // Or find by truck
        // Actually, we can just find the dispatch
        val dispatch = dispatchDao.getDispatchByTruck(truckNumber)
        val godownSource = dispatch?.godownSource
        if (godownSource != null) {
            val godownFound = godownDao.findGodown(godownSource) ?: godownDao.getGodownById(godownSource)
            if (godownFound != null) {
                val newStock = godownFound.currentStockMt + (dispatchedWeightKg / 1000.0)
                godownDao.updateGodown(godownFound.copy(currentStockMt = newStock, lastUpdated = System.currentTimeMillis()))
            }
            dispatchDao.updateDispatch(dispatch.copy(status = com.example.data.model.DispatchStatus.REJECTED.name))
        }

        // Log rejection loss in ledger
        vendorLedgerDao.insertLedgerEntry(
            VendorLedgerEntity(
                vendorType = "TRANSPORTER",
                vendorName = "Truck $truckNumber Rejection Claim",
                transactionType = "PENALTY_DEDUCTION",
                amount = -totalLoss, // Negative expense
                paymentMode = "CASH",
                referenceDocNo = rejection.rejectionNo,
                notes = "Rejection Loss: Transport ₹$transportLoss + 50% Shifting Labor ₹$returnBagShiftingLabor + Penalties ₹$penaltiesDemurrage"
            )
        )
        
        val expense = ExpenseEntryEntity(
            expenseNo = "EXP-${kotlin.random.Random.nextInt(1000, 9999)}",
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
        notes: String = ""
    ): Long = withContext(Dispatchers.IO) {
        val expense = ExpenseEntryEntity(
            expenseNo = "EXP-${Random.nextInt(1000, 9999)}",
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

        // Add to Vendor Ledger
        vendorLedgerDao.insertLedgerEntry(
            VendorLedgerEntity(
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

    // 11. Automated PDC Background Check & Clearing
    suspend fun checkAndClearMaturedPdcs(): List<VendorLedgerEntity> = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val matured = vendorLedgerDao.getMaturedPdcs(now)
        for (pdc in matured) {
            val cleared = pdc.copy(
                pdcStatus = PdcStatus.CLEARED.name,
                runningBalance = 0.0,
                notes = "${pdc.notes} • AUTO-CLEARED on maturity"
            )
            vendorLedgerDao.updateLedgerEntry(cleared)
        }
        matured
    }

    // Simulated IoT pulse generator
    suspend fun emitSimulatedIoTPulse() = withContext(Dispatchers.IO) {
        val currentKg = Random.nextInt(7800, 32000).toDouble()
        telemetryDao.insertTelemetry(
            IoTTelemetryEntity(
                deviceType = "WEIGHBRIDGE",
                deviceId = "WB-DIGI-01",
                readingValue = currentKg,
                unit = "kg",
                status = "LIVE_STREAM",
                rawPayloadJson = "{\"gross_kg\":$currentKg,\"stability\":\"OK\"}",
                latencyMs = 1
            )
        )
    }

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
        notes: String = ""
    ): Long = withContext(Dispatchers.IO) {
        val trade = TradeBookingEntity(
            tradeNo = "TRD-${Random.nextInt(1000, 9999)}",
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

    suspend fun deleteTrade(tradeId: Long) = withContext(Dispatchers.IO) {
        tradeDao.deleteTradeById(tradeId)
    }

    suspend fun deleteExpense(expense: ExpenseEntryEntity) = withContext(Dispatchers.IO) {
        expenseDao.deleteExpense(expense.id)
    }

    suspend fun deleteExpenseById(expenseId: Long) = withContext(Dispatchers.IO) {
        expenseDao.deleteExpense(expenseId)
    }

    suspend fun deleteTruckRejection(rejectionId: Long) = withContext(Dispatchers.IO) {
        truckRejectionDao.deleteRejection(rejectionId)
    }

    suspend fun setupDynamicGodowns(godowns: List<GodownEntity>) = withContext(Dispatchers.IO) {
        if (godowns.isNotEmpty()) {
            godownDao.deleteAllGodowns()
            godownDao.insertGodowns(godowns)
        }
    }

    suspend fun updatePaymentStatus(procurementId: Long, newStatus: PaymentStatus) = withContext(Dispatchers.IO) {
        val item = procurementDao.getProcurementById(procurementId) ?: return@withContext
        procurementDao.updateProcurement(item.copy(paymentStatus = newStatus.name))
    }

    suspend fun setProcurementArchived(procurementId: Long, isArchived: Boolean) = withContext(Dispatchers.IO) {
        procurementDao.updateArchiveStatus(procurementId, isArchived)
    }

    suspend fun deleteProcurement(procurementId: Long) = withContext(Dispatchers.IO) {
        procurementDao.deleteProcurement(procurementId)
    }

    suspend fun deleteDispatch(dispatchId: Long) = withContext(Dispatchers.IO) {
        dispatchDao.deleteDispatch(dispatchId)
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

        // Insert into storage_facility_intakes table
        storageIntakeDao.insertIntake(intakeEntity)

        // Update target Godown Entity (Stock, Dynamic Weighted Avg Moisture, Active Crop, Last Updated)
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

    suspend fun deleteStorageIntake(id: Long) = withContext(Dispatchers.IO) {
        storageIntakeDao.deleteIntake(id)
    }

    fun getIntakesForFacility(facilityId: String): Flow<List<StorageFacilityIntakeEntity>> =
        storageIntakeDao.getIntakesForFacility(facilityId)
}
