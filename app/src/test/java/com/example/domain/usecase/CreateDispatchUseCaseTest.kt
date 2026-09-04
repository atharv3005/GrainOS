package com.example.domain.usecase

import com.example.data.model.AuditTrailEntity
import com.example.data.model.CropType
import com.example.data.model.DocumentSequenceEntity
import com.example.data.model.GodownEntity
import com.example.data.model.InventoryMovementEntity
import com.example.data.model.OutboundDispatchEntity
import com.example.data.model.PartyEntity
import com.example.data.model.VendorLedgerEntity
import com.example.data.repository.AuditTrailRepository
import com.example.data.repository.DocumentSequenceRepository
import com.example.data.repository.InventoryMovementRepository
import com.example.data.repository.PartyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CreateDispatchUseCaseTest {

    private val godownStorage = mutableMapOf<String, GodownEntity>(
        "GODOWN_A" to GodownEntity(
            godownId = "GODOWN_A",
            displayName = "Godown A (Main Silo)",
            capacityMt = 200.0,
            currentStockMt = 10.0, // Only 10 MT available
            activeCrop = "MAIZE",
            baseCostPerQuintal = 2400.0,
            adjustedAvgCostPerQuintal = 2400.0
        )
    )
    private val dispatchStorage = mutableListOf<OutboundDispatchEntity>()
    private val movementStorage = mutableListOf<InventoryMovementEntity>()
    private val auditStorage = mutableListOf<AuditTrailEntity>()
    private val ledgerStorage = mutableListOf<VendorLedgerEntity>()

    private val fakeGodownDao = object : com.example.data.local.GodownDao {
        override fun getAllGodowns(): Flow<List<GodownEntity>> = flowOf(godownStorage.values.toList())
        override suspend fun getGodownById(id: String): GodownEntity? = godownStorage[id]
        override suspend fun getGodownByUuid(uuid: String): GodownEntity? = godownStorage.values.find { it.uuid == uuid }
        override suspend fun findGodown(query: String): GodownEntity? = godownStorage.values.find { it.godownId == query || it.displayName.contains(query) }
        override suspend fun getFirstGodown(): GodownEntity? = godownStorage.values.firstOrNull()
        override suspend fun getAllGodownsDirect(): List<GodownEntity> = godownStorage.values.toList()
        override suspend fun insertGodown(godown: GodownEntity): Long = 1L
        override suspend fun insertGodowns(godowns: List<GodownEntity>) {}
        override suspend fun updateGodown(godown: GodownEntity) { godownStorage[godown.godownId] = godown }
        override suspend fun addStock(godownId: String, weightMt: Double, timestamp: Long) {}
        override suspend fun reduceStock(godownId: String, weightMt: Double, timestamp: Long): Int {
            val g = godownStorage[godownId] ?: return 0
            if (g.currentStockMt >= weightMt) {
                godownStorage[godownId] = g.copy(currentStockMt = g.currentStockMt - weightMt)
                return 1
            }
            return 0 // Insufficient stock!
        }
    }

    private val fakeDispatchDao = object : com.example.data.local.DispatchDao {
        override fun getAllDispatches(): Flow<List<OutboundDispatchEntity>> = flowOf(dispatchStorage)
        override suspend fun getAllDispatchesDirect(): List<OutboundDispatchEntity> = dispatchStorage
        override suspend fun getDispatchById(id: Long): OutboundDispatchEntity? = dispatchStorage.find { it.id == id }
        override suspend fun getDispatchByUuid(uuid: String): OutboundDispatchEntity? = dispatchStorage.find { it.uuid == uuid }
        override suspend fun getDispatchByNo(dispatchNo: String): OutboundDispatchEntity? = dispatchStorage.find { it.dispatchNo == dispatchNo }
        override suspend fun getDispatchesByBuyerParty(partyId: Long): List<OutboundDispatchEntity> = dispatchStorage.filter { it.buyerPartyId == partyId }
        override fun getDispatchesByStatusFlow(status: String): Flow<List<OutboundDispatchEntity>> = flowOf(dispatchStorage.filter { it.status == status })
        override suspend fun getDispatchByTruck(truckNo: String): OutboundDispatchEntity? = dispatchStorage.find { it.vehicleNumber == truckNo }
        override suspend fun insertDispatch(dispatch: OutboundDispatchEntity): Long {
            val saved = dispatch.copy(id = dispatchStorage.size + 1L)
            dispatchStorage.add(saved)
            return saved.id
        }
        override suspend fun insertDispatches(dispatches: List<OutboundDispatchEntity>) {
            dispatchStorage.addAll(dispatches)
        }
        override suspend fun updateDispatch(dispatch: OutboundDispatchEntity) {}
        override suspend fun deleteDispatch(id: Long) {}
        override fun getTotalDispatchedKg(): Flow<Double?> = flowOf(dispatchStorage.sumOf { it.netLoadedWeightKg })
    }

    private val fakeVendorLedgerDao = object : com.example.data.local.VendorLedgerDao {
        override fun getAllLedgerEntries(): Flow<List<VendorLedgerEntity>> = flowOf(ledgerStorage)
        override suspend fun getAllLedgersDirect(): List<VendorLedgerEntity> = ledgerStorage
        override suspend fun getById(id: Long): VendorLedgerEntity? = ledgerStorage.find { it.id == id }
        override suspend fun getByUuid(uuid: String): VendorLedgerEntity? = ledgerStorage.find { it.uuid == uuid }
        override suspend fun getLedgerEntriesByParty(partyId: Long): List<VendorLedgerEntity> = emptyList()
        override fun getLedgerEntriesByPartyFlow(partyId: Long): Flow<List<VendorLedgerEntity>> = flowOf(emptyList())
        override fun getLedgerEntriesByType(vendorType: String): Flow<List<VendorLedgerEntity>> = flowOf(emptyList())
        override fun getLedgerEntriesForVendor(vendorName: String): Flow<List<VendorLedgerEntity>> = flowOf(emptyList())
        override fun getAllPdcsFlow(): Flow<List<VendorLedgerEntity>> = flowOf(emptyList())
        override fun getPdcsByStatusFlow(status: String): Flow<List<VendorLedgerEntity>> = flowOf(emptyList())
        override fun getActivePdcsFlow(): Flow<List<VendorLedgerEntity>> = flowOf(emptyList())
        override suspend fun getPdcsDueBetween(startTs: Long, endTs: Long): List<VendorLedgerEntity> = emptyList()
        override suspend fun getCumulativeFarmerPurchases(vendorName: String): Double? = 0.0
        override suspend fun getCumulativePartyPurchases(partyId: Long): Double? = 0.0
        override suspend fun insertLedgerEntry(entry: VendorLedgerEntity): Long {
            ledgerStorage.add(entry)
            return ledgerStorage.size.toLong()
        }
        override suspend fun insertLedgerEntries(entries: List<VendorLedgerEntity>) {}
        override suspend fun updateLedgerEntry(entry: VendorLedgerEntity) {}
        override suspend fun deleteLedgerEntry(id: Long) {}
    }

    private val fakePartyRepo = object : PartyRepository(
        partyDao = object : com.example.data.local.PartyDao {
            override suspend fun insert(party: PartyEntity): Long = 1L
            override suspend fun insertAll(parties: List<PartyEntity>) {}
            override suspend fun update(party: PartyEntity) {}
            override fun getAllPartiesFlow(): Flow<List<PartyEntity>> = flowOf(emptyList())
            override suspend fun getAllParties(): List<PartyEntity> = emptyList()
            override suspend fun getByType(type: String): List<PartyEntity> = emptyList()
            override fun getByTypeFlow(type: String): Flow<List<PartyEntity>> = flowOf(emptyList())
            override suspend fun getByMobile(mobile: String): PartyEntity? = null
            override suspend fun getByPan(pan: String): PartyEntity? = null
            override suspend fun getById(id: Long): PartyEntity? = null
            override suspend fun getByUuid(uuid: String): PartyEntity? = null
            override suspend fun search(searchQuery: String?): List<PartyEntity> = emptyList()
            override suspend fun incrementCumulativePurchases(partyId: Long, amount: Double, timestamp: Long) {}
            override suspend fun updateRunningBalance(partyId: Long, deltaAmount: Double, timestamp: Long) {}
        }
    ) {}

    private val fakeDocSequenceRepo = object : DocumentSequenceRepository(
        sequenceDao = object : com.example.data.local.DocumentSequenceDao {
            override suspend fun insert(sequence: DocumentSequenceEntity): Long = 1L
            override suspend fun update(sequence: DocumentSequenceEntity) {}
            override suspend fun getSequence(fy: String, facilityId: String, docType: String): DocumentSequenceEntity? = null
            override fun getSequencesByFyFlow(fy: String): Flow<List<DocumentSequenceEntity>> = flowOf(emptyList())
            override fun getAllSequencesFlow(): Flow<List<DocumentSequenceEntity>> = flowOf(emptyList())
            override suspend fun setFacilityLock(fy: String, facilityId: String, isLocked: Boolean, timestamp: Long) {}
            override suspend fun getNextDocumentNumber(fy: String, facilityId: String, docType: String): String = "DSP/26-27/00001"
        }
    ) {}

    private val fakeMovementRepo = object : InventoryMovementRepository(
        movementDao = object : com.example.data.local.InventoryMovementDao {
            override suspend fun insert(movement: InventoryMovementEntity): Long {
                movementStorage.add(movement)
                return movementStorage.size.toLong()
            }
            override suspend fun insertAll(movements: List<InventoryMovementEntity>) {}
            override fun getAllMovementsFlow(): Flow<List<InventoryMovementEntity>> = flowOf(movementStorage)
            override suspend fun getAllMovements(): List<InventoryMovementEntity> = movementStorage
            override fun getMovementsByFacilityFlow(facilityId: String): Flow<List<InventoryMovementEntity>> = flowOf(movementStorage)
            override suspend fun getMovementsByFacility(facilityId: String): List<InventoryMovementEntity> = movementStorage
            override suspend fun getMovementsByBatch(facilityId: String, batchId: String): List<InventoryMovementEntity> = emptyList()
            override suspend fun calculateFacilityStockKg(facilityId: String): Double = 0.0
            override suspend fun calculateBatchStockKg(facilityId: String, batchId: String): Double = 0.0
            override suspend fun getBySourceEntity(uuid: String): InventoryMovementEntity? = null
            override suspend fun getByUuid(uuid: String): InventoryMovementEntity? = null
        }
    ) {}

    private val fakeAuditRepo = object : AuditTrailRepository(
        auditTrailDao = object : com.example.data.local.AuditTrailDao {
            override suspend fun insert(audit: AuditTrailEntity): Long {
                auditStorage.add(audit)
                return auditStorage.size.toLong()
            }
            override fun getAllAuditTrailsFlow(): Flow<List<AuditTrailEntity>> = flowOf(auditStorage)
            override suspend fun getAllAuditTrails(): List<AuditTrailEntity> = auditStorage
            override suspend fun getAuditHistory(type: String, entityId: String): List<AuditTrailEntity> = emptyList()
            override fun getAuditHistoryFlow(type: String, entityId: String): Flow<List<AuditTrailEntity>> = flowOf(emptyList())
            override suspend fun getAllDeletions(): List<AuditTrailEntity> = emptyList()
            override fun getAllDeletionsFlow(): Flow<List<AuditTrailEntity>> = flowOf(emptyList())
            override suspend fun filterAudit(action: String?, entityType: String?): List<AuditTrailEntity> = emptyList()
        }
    ) {}

    private val generateDocNumber = GenerateDocumentNumberUseCase(fakeDocSequenceRepo)
    private val postMovement = PostInventoryMovementUseCase(fakeMovementRepo, fakeAuditRepo)
    private val createDispatchUseCase = CreateDispatchUseCase(
        fakeDispatchDao, fakeGodownDao, fakeVendorLedgerDao, fakePartyRepo,
        generateDocNumber, postMovement, fakeAuditRepo
    )

    @Test
    fun testCreateDispatch_insufficientStock_blocksDispatch() = runBlocking {
        // Godown A has 10.0 MT available. Request 25.0 MT -> Must fail with InsufficientStockException!
        val request = CreateDispatchRequest(
            buyerNameQuick = "Cargill Agro",
            destination = "Pune Feed Mill",
            vehicleNumber = "MH 12 AB 9988",
            cropType = CropType.MAIZE,
            godownSource = "GODOWN_A",
            tareWeightKg = 10000.0,
            grossWeightKg = 35000.0, // 25,000 kg = 25 MT
            ratePerQuintal = 2600.0
        )

        val result = createDispatchUseCase(request)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is InsufficientStockException)

        // Ensure stock was NOT reduced
        assertEquals(10.0, godownStorage["GODOWN_A"]?.currentStockMt ?: 0.0, 0.01)
    }

    @Test
    fun testCreateDispatch_sufficientStock_succeedsAndDeductsInventory() = runBlocking {
        // Request 5.0 MT out of 10.0 MT available
        val request = CreateDispatchRequest(
            buyerNameQuick = "ITC Agri",
            destination = "Indore Plant",
            vehicleNumber = "MH 18 Q 1122",
            cropType = CropType.MAIZE,
            godownSource = "GODOWN_A",
            tareWeightKg = 5000.0,
            grossWeightKg = 10000.0, // 5,000 kg = 5 MT
            ratePerQuintal = 2500.0
        )

        val result = createDispatchUseCase(request)
        assertTrue(result.isSuccess)

        val saved = result.getOrNull()!!
        assertEquals("DSP/26-27/00001", saved.dispatchNo)
        assertEquals(5000.0, saved.netLoadedWeightKg, 0.01)

        // Ensure godown stock deducted by exactly 5 MT
        assertEquals(5.0, godownStorage["GODOWN_A"]?.currentStockMt ?: 0.0, 0.01)

        // Ensure outbound inventory movement was posted
        assertEquals(1, movementStorage.size)
        assertEquals(-5000.0, movementStorage[0].quantityKg, 0.01)
    }
}
