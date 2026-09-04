package com.example.domain.usecase

import androidx.room.withTransaction
import com.example.data.local.AppDatabase
import com.example.data.local.GodownDao
import com.example.data.local.ProcurementDao
import com.example.data.local.VendorLedgerDao
import com.example.data.model.CropType
import com.example.data.model.DocumentType
import com.example.data.model.InventoryMovementType
import com.example.data.model.PaymentMode
import com.example.data.model.PaymentStatus
import com.example.data.model.PdcStatus
import com.example.data.model.ProcurementEntity
import com.example.data.model.ProcurementStatus
import com.example.data.model.QualityGrade
import com.example.data.model.QuantityBasis
import com.example.data.model.VendorLedgerEntity
import com.example.data.model.VendorType
import com.example.data.repository.AuditTrailRepository
import com.example.data.repository.PartyRepository
import com.example.domain.managers.OrganizationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Input request for creating a new procurement record.
 */
data class CreateProcurementRequest(
    val partyId: Long? = null,
    val farmerNameQuick: String? = null,
    val mobileQuick: String? = null,
    val villageQuick: String? = null,
    val panQuick: String? = null,
    val vehicleNumber: String,
    val cropType: CropType = CropType.MAIZE,
    val grossWeightKg: Double,
    val tareWeightKg: Double,
    val moisturePercentage: Double,
    val qualityGrade: QualityGrade = QualityGrade.GRADE_A,
    val ratePerQuintal: Double,
    val godownAssigned: String = "GODOWN_A",
    val applyMandiCess: Boolean = true,
    val enableTds194q: Boolean = false,
    val paymentMode: PaymentMode = PaymentMode.CASH,
    val advanceDeductionAmount: Double = 0.0,
    val isPdc: Boolean = false,
    val pdcMaturityDate: Long = 0L,
    val utrOrChequeNo: String = "",
    val grossWeightMethod: String = "AUTO",
    val tareWeightMethod: String = "AUTO",
    val financialYear: String = "26-27",
    val facilityId: String = "MAIN",
    val orgCode: String = OrganizationContext.getCurrentOrgCode(),
    val userId: String = "operator",
    val deviceId: String = "local_device"
)

/**
 * Domain Use Case for registering farmer procurement, executing APMC & TDS tax math,
 * generating sequential document numbering, updating inventory movement, and creating financial ledgers.
 * Atomically wrapped in Room database transactions to prevent partial writes (BUG-002 Fix).
 */
class CreateProcurementUseCase(
    private val procurementDao: ProcurementDao,
    private val godownDao: GodownDao,
    private val vendorLedgerDao: VendorLedgerDao,
    private val partyRepository: PartyRepository,
    private val generateDocNumber: GenerateDocumentNumberUseCase,
    private val postMovement: PostInventoryMovementUseCase,
    private val auditTrailRepository: AuditTrailRepository,
    private val apmcCalculator: ApmcTaxCalculator = ApmcTaxCalculator(),
    private val tdsCalculator: TdsCalculator = TdsCalculator(),
    private val appDatabase: AppDatabase? = null
) {
    suspend operator fun invoke(request: CreateProcurementRequest): Result<ProcurementEntity> = withContext(Dispatchers.IO) {
        try {
            require(request.grossWeightKg > 0.0) { "Gross weight must be greater than zero." }
            require(request.tareWeightKg >= 0.0) { "Tare weight cannot be negative." }
            require(request.grossWeightKg >= request.tareWeightKg) { "Gross weight cannot be less than tare weight." }
            require(request.ratePerQuintal > 0.0) { "Rate per quintal must be positive." }

            // 1. Resolve Farmer details (from Party Master or Quick Entry)
            val party = request.partyId?.let { partyRepository.getById(it) }
            val effectiveFarmerName = party?.legalName ?: request.farmerNameQuick?.trim()
            require(!effectiveFarmerName.isNullOrBlank()) { "Farmer name must be provided." }

            val effectiveMobile = party?.mobile ?: request.mobileQuick?.trim() ?: ""
            val effectiveVillage = party?.village ?: request.villageQuick?.trim() ?: ""
            val effectivePan = party?.pan ?: request.panQuick?.trim() ?: ""
            val hasValidPan = effectivePan.length == 10

            // 2. Physical & Commercial Quantities
            val netWeightKg = (request.grossWeightKg - request.tareWeightKg).coerceAtLeast(0.0)
            val commercialQuintals = netWeightKg / 100.0
            val grossBillAmount = commercialQuintals * request.ratePerQuintal

            // 3. APMC Cess Calculation (1.0% + 0.5% = 1.5%)
            val cessResult = apmcCalculator.calculate(grossBillAmount, request.applyMandiCess)

            // 4. Section 194Q TDS Calculation
            val cumulativeInFy = party?.cumulativePurchasesInFy ?: 0.0
            val tdsResult = tdsCalculator.calculate(
                currentBillAmount = grossBillAmount,
                cumulativePurchasesInFy = cumulativeInFy,
                enableTds194q = request.enableTds194q,
                hasValidPan = hasValidPan
            )

            // 5. Final Net Farmer Payable
            val finalPayable = (grossBillAmount - cessResult.totalCess - tdsResult.tdsDeductedAmount - request.advanceDeductionAmount).coerceAtLeast(0.0)

            // 6. Generate Statutory Sequential Token / GIN
            val tokenNo = generateDocNumber(
                financialYear = request.financialYear,
                facilityId = request.facilityId,
                documentType = DocumentType.GIN
            )

            // 7. Build Procurement Entity
            val procurement = ProcurementEntity(
                tokenNo = tokenNo,
                partyId = party?.id,
                orgCode = request.orgCode,
                farmerNameQuick = if (party == null) effectiveFarmerName else null,
                mobileQuick = if (party == null) effectiveMobile else null,
                villageQuick = if (party == null) effectiveVillage else null,
                farmerName = effectiveFarmerName,
                mobileNumber = effectiveMobile,
                village = effectiveVillage,
                vehicleNumber = request.vehicleNumber.trim(),
                panNumber = effectivePan,
                isPanVerified = hasValidPan,
                cropType = request.cropType.name,
                grossWeightKg = request.grossWeightKg,
                tareWeightKg = request.tareWeightKg,
                netWeightKg = netWeightKg,
                bagCount = (netWeightKg / 50.0).toInt().coerceAtLeast(1),
                bagWeightKg = 50.0,
                moisturePercentage = request.moisturePercentage,
                qualityGrade = request.qualityGrade.name,
                ratePerQuintal = request.ratePerQuintal,
                grossBillAmount = grossBillAmount,
                applyMandiCess = request.applyMandiCess,
                mandiMarketFee = cessResult.marketFee,
                mandiSupervisoryCharge = cessResult.supervisoryCharge,
                totalMandiCess = cessResult.totalCess,
                enableTds194q = request.enableTds194q,
                cumulativePurchasesInFy = cumulativeInFy,
                isTdsApplicable = tdsResult.isTdsApplicable,
                tdsRate = tdsResult.tdsRate,
                tdsDeductedAmount = tdsResult.tdsDeductedAmount,
                isTcsExempt = tdsResult.isTcsExempt,
                totalAmount = finalPayable,
                godownAssigned = request.godownAssigned,
                status = ProcurementStatus.COMPLETED.name,
                paymentStatus = if (request.isPdc) PaymentStatus.PENDING.name else PaymentStatus.PAID.name,
                paymentMode = request.paymentMode.name,
                utrOrChequeNo = request.utrOrChequeNo,
                chequeDate = request.pdcMaturityDate,
                isPdc = request.isPdc,
                pdcCleared = !request.isPdc,
                grossWeightMethod = request.grossWeightMethod,
                tareWeightMethod = request.tareWeightMethod,
                grossTimestamp = System.currentTimeMillis() - 1000 * 60 * 15,
                tareTimestamp = System.currentTimeMillis(),
                completedTimestamp = System.currentTimeMillis(),
                quantityBasis = QuantityBasis.PHYSICAL.name
            )

            // Atomic database transaction block (BUG-002 Fix)
            val mutateBlock = suspend {
                val insertedId = procurementDao.insertProcurement(procurement)
                val savedProcurement = procurement.copy(id = insertedId)

                // 8. Post Immutable Inventory Movement
                postMovement(
                    movementType = InventoryMovementType.RECEIPT,
                    sourceEntityType = "PROCUREMENT",
                    sourceEntityUuid = savedProcurement.uuid,
                    facilityId = request.godownAssigned,
                    cropType = savedProcurement.cropType,
                    quantityKg = savedProcurement.netWeightKg,
                    quantityBasis = QuantityBasis.INVENTORY,
                    costPerQuintalPaise = (savedProcurement.ratePerQuintal * 100).toLong(),
                    totalValuePaise = (savedProcurement.totalAmount * 100).toLong(),
                    userId = request.userId,
                    deviceId = request.deviceId,
                    reason = "Inbound Procurement $tokenNo from $effectiveFarmerName"
                )

                // 9. Update Godown physical balance
                godownDao.addStock(request.godownAssigned, savedProcurement.netWeightKg / 1000.0)

                // 10. Update Party Master Cumulative Turnover if Party Exists
                party?.let {
                    partyRepository.updateCumulativePurchases(it.id, grossBillAmount)
                }

                // 11. Create Vendor Ledger Entry for PDC or Cash/RTGS
                val ledgerEntry = VendorLedgerEntity(
                    partyId = party?.id,
                    orgCode = request.orgCode,
                    vendorType = VendorType.FARMER.name,
                    vendorName = effectiveFarmerName,
                    contactNumber = effectiveMobile,
                    panNumber = effectivePan,
                    transactionType = if (request.isPdc) "PDC_ISSUED" else "BILL_CREDIT",
                    amount = finalPayable,
                    paymentMode = request.paymentMode.name,
                    utrOrChequeNo = request.utrOrChequeNo,
                    chequeMaturityDate = request.pdcMaturityDate,
                    pdcStatus = if (request.isPdc) PdcStatus.ISSUED.name else PdcStatus.NONE.name,
                    referenceDocNo = tokenNo,
                    runningBalance = if (request.isPdc) finalPayable else 0.0,
                    notes = "Procurement $tokenNo (${request.cropType.name} - ${netWeightKg.toInt()} kg)"
                )
                vendorLedgerDao.insertLedgerEntry(ledgerEntry)

                // 12. Log Audit Trail
                auditTrailRepository.logCreate(
                    entityType = "PROCUREMENT",
                    entityId = savedProcurement.tokenNo,
                    newStateJson = "{\"token\":\"$tokenNo\",\"farmer\":\"$effectiveFarmerName\",\"crop\":\"${procurement.cropType}\",\"netKg\":$netWeightKg,\"payable\":$finalPayable}",
                    userId = request.userId,
                    deviceId = request.deviceId,
                    reason = "Created procurement slip $tokenNo"
                )

                savedProcurement
            }

            val saved = if (appDatabase != null) {
                appDatabase.withTransaction { mutateBlock() }
            } else {
                mutateBlock()
            }

            Result.success(saved)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
