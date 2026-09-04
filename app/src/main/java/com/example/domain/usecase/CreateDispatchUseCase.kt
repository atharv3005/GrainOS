package com.example.domain.usecase

import androidx.room.withTransaction
import com.example.data.local.AppDatabase
import com.example.data.local.DispatchDao
import com.example.data.local.GodownDao
import com.example.data.local.VendorLedgerDao
import com.example.data.model.CropType
import com.example.data.model.DispatchStatus
import com.example.data.model.DocumentType
import com.example.data.model.InventoryMovementType
import com.example.data.model.OutboundDispatchEntity
import com.example.data.model.QuantityBasis
import com.example.data.model.VendorLedgerEntity
import com.example.data.model.VendorType
import com.example.data.repository.AuditTrailRepository
import com.example.data.repository.PartyRepository
import com.example.domain.managers.OrganizationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class InsufficientStockException(message: String) : Exception(message)

data class CreateDispatchRequest(
    val buyerPartyId: Long? = null,
    val buyerNameQuick: String? = null,
    val destination: String,
    val vehicleNumber: String,
    val cropType: CropType = CropType.MAIZE,
    val godownSource: String = "GODOWN_A",
    val tareWeightKg: Double,
    val grossWeightKg: Double,
    val ratePerQuintal: Double,
    val tradeBookingId: Long? = null,
    val transporterPartyId: Long? = null,
    val freightCost: Double = 0.0,
    val loadingLaborCost: Double = 0.0,
    val bagCost: Double = 0.0,
    val miscCost: Double = 0.0,
    val financialYear: String = "26-27",
    val facilityId: String = "MAIN",
    val orgCode: String = OrganizationContext.getCurrentOrgCode(),
    val userId: String = "operator",
    val deviceId: String = "local_device"
)

/**
 * Domain Use Case for outbound dispatch authorization, stock availability verification,
 * atomic guarded godown deduction, inventory movement posting, and receivable registration.
 * Atomically wrapped in Room database transactions to prevent partial writes (BUG-002 Fix).
 */
class CreateDispatchUseCase(
    private val dispatchDao: DispatchDao,
    private val godownDao: GodownDao,
    private val vendorLedgerDao: VendorLedgerDao,
    private val partyRepository: PartyRepository,
    private val generateDocNumber: GenerateDocumentNumberUseCase,
    private val postMovement: PostInventoryMovementUseCase,
    private val auditTrailRepository: AuditTrailRepository,
    private val appDatabase: AppDatabase? = null
) {
    suspend operator fun invoke(request: CreateDispatchRequest): Result<OutboundDispatchEntity> = withContext(Dispatchers.IO) {
        try {
            require(request.grossWeightKg > 0.0) { "Gross weight must be greater than zero." }
            require(request.tareWeightKg >= 0.0) { "Tare weight cannot be negative." }
            require(request.grossWeightKg > request.tareWeightKg) { "Gross weight must be greater than tare weight." }
            require(request.ratePerQuintal > 0.0) { "Rate per quintal must be positive." }

            // 1. Resolve Buyer Party
            val buyerParty = request.buyerPartyId?.let { partyRepository.getById(it) }
            val effectiveBuyerName = buyerParty?.legalName ?: request.buyerNameQuick?.trim()
            require(!effectiveBuyerName.isNullOrBlank()) { "Buyer name must be provided." }

            val netLoadedWeightKg = request.grossWeightKg - request.tareWeightKg
            val netLoadedWeightMt = netLoadedWeightKg / 1000.0

            // 2. Fetch Godown to check cost & available balance
            val godown = godownDao.getGodownById(request.godownSource)
                ?: throw IllegalArgumentException("Godown facility ${request.godownSource} not found.")

            val mutateBlock = suspend {
                // 3. Atomically attempt stock reduction. If rowsUpdated == 0, stock is insufficient!
                val rowsUpdated = godownDao.reduceStock(request.godownSource, netLoadedWeightMt)
                if (rowsUpdated == 0) {
                    throw InsufficientStockException(
                        "Insufficient stock in ${godown.displayName}. Required: ${"%.2f".format(netLoadedWeightMt)} MT, Available: ${"%.2f".format(godown.currentStockMt)} MT."
                    )
                }

                // 4. Generate Sequential DSP Number
                val dispatchNo = generateDocNumber(
                    financialYear = request.financialYear,
                    facilityId = request.facilityId,
                    documentType = DocumentType.DSP
                )

                // 5. Calculate Invoicing & FIFO Cost
                val totalInvoiceAmount = (netLoadedWeightKg / 100.0) * request.ratePerQuintal
                val fifoProcurementCost = (netLoadedWeightKg / 100.0) * godown.adjustedAvgCostPerQuintal

                // 6. Build and Persist Dispatch Entity
                val dispatch = OutboundDispatchEntity(
                    dispatchNo = dispatchNo,
                    buyerPartyId = buyerParty?.id,
                    tradeBookingId = request.tradeBookingId,
                    transporterPartyId = request.transporterPartyId,
                    orgCode = request.orgCode,
                    buyerName = effectiveBuyerName,
                    destination = request.destination.trim(),
                    vehicleNumber = request.vehicleNumber.trim(),
                    cropType = request.cropType.name,
                    godownSource = request.godownSource,
                    tareWeightKg = request.tareWeightKg,
                    grossWeightKg = request.grossWeightKg,
                    netLoadedWeightKg = netLoadedWeightKg,
                    ratePerQuintal = request.ratePerQuintal,
                    totalInvoiceAmount = totalInvoiceAmount,
                    loadingLaborCost = request.loadingLaborCost,
                    freightCost = request.freightCost,
                    bagCost = request.bagCost,
                    miscCost = request.miscCost,
                    fifoProcurementCost = fifoProcurementCost,
                    status = DispatchStatus.IN_TRANSIT.name,
                    timestamp = System.currentTimeMillis()
                )

                val insertedId = dispatchDao.insertDispatch(dispatch)
                val savedDispatch = dispatch.copy(id = insertedId)

                // 7. Post Immutable Outbound Inventory Movement (Negative Quantity)
                postMovement(
                    movementType = InventoryMovementType.DISPATCH,
                    sourceEntityType = "DISPATCH",
                    sourceEntityUuid = savedDispatch.uuid,
                    facilityId = request.godownSource,
                    cropType = savedDispatch.cropType,
                    quantityKg = -netLoadedWeightKg,
                    quantityBasis = QuantityBasis.INVENTORY,
                    costPerQuintalPaise = (request.ratePerQuintal * 100).toLong(),
                    totalValuePaise = (totalInvoiceAmount * 100).toLong(),
                    userId = request.userId,
                    deviceId = request.deviceId,
                    reason = "Outbound Dispatch $dispatchNo to $effectiveBuyerName"
                )

                // 8. Create Corporate Receivable Entry in Ledger
                vendorLedgerDao.insertLedgerEntry(
                    VendorLedgerEntity(
                        partyId = buyerParty?.id,
                        orgCode = request.orgCode,
                        vendorType = VendorType.CORPORATE.name,
                        vendorName = effectiveBuyerName,
                        transactionType = "BILL_CREDIT",
                        amount = totalInvoiceAmount,
                        referenceDocNo = dispatchNo,
                        runningBalance = totalInvoiceAmount,
                        notes = "Receivable created for Dispatch #$dispatchNo ($netLoadedWeightKg kg @ ₹${request.ratePerQuintal}/Qtl)"
                    )
                )

                // 9. Log Audit Trail
                auditTrailRepository.logCreate(
                    entityType = "DISPATCH",
                    entityId = savedDispatch.dispatchNo,
                    newStateJson = "{\"dispatchNo\":\"$dispatchNo\",\"buyer\":\"$effectiveBuyerName\",\"netKg\":$netLoadedWeightKg,\"invoice\":$totalInvoiceAmount}",
                    userId = request.userId,
                    deviceId = request.deviceId,
                    reason = "Created outbound dispatch $dispatchNo"
                )

                savedDispatch
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
