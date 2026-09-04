package com.example.domain.usecase

import com.example.data.model.AuditTrailEntity
import com.example.data.model.CropType
import com.example.data.model.DocumentSequenceEntity
import com.example.data.model.GodownEntity
import com.example.data.model.InventoryMovementEntity
import com.example.data.model.PartyEntity
import com.example.data.model.ProcurementEntity
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

class CreateProcurementUseCaseTest {

    private val godownStorage = mutableMapOf<String, GodownEntity>(
        "GODOWN_A" to GodownEntity(
            godownId = "GODOWN_A",
            displayName = "Godown A",
            capacityMt = 200.0,
            currentStockMt = 50.0,
            activeCrop = "MAIZE"
        )
    )
    private val procurementStorage = mutableListOf<ProcurementEntity>()
    private val movementStorage = mutableListOf<InventoryMovementEntity>()
    private val auditStorage = mutableListOf<AuditTrailEntity>()
    private val ledgerStorage = mutableListOf<VendorLedgerEntity>()
    private val partyStorage = mutableMapOf<Long, PartyEntity>(
        1L to PartyEntity(
            id = 1L,
            partyType = "FARMER",
            legalName = "Ramesh Patil",
            mobile = "9822451230",
            pan = "ABCDE1234F",
            cumulativePurchasesInFy = 100000.0
        )
    )

    private val fakeGodownDao = object : com.example.data.local.GodownDao {
        override fun getAllGodowns(): Flow<List<GodownEntity>> = flowOf(godownStorage.values.toList())
        override suspend fun getGodownById(id: String): GodownEntity? = godownStorage[id]
        override suspend fun getGodownByUuid(uuid: String): GodownEntity? = null
        override suspend fun findGodown(query: String): GodownEntity? = godownStorage[query]
        override suspend fun getFirstGodown(): GodownEntity? = godownStorage.values.firstOrNull()
        override suspend fun getAllGodownsDirect(): List<GodownEntity> = godownStorage.values.toList()
        override suspend fun insertGodown(godown: GodownEntity): Long = 1L
        override suspend fun insertGodowns(godowns: List<GodownEntity>) {}
        override suspend fun updateGodown(godown: GodownEntity) { godownStorage[godown.godownId] = godown }
        override suspend fun addStock(godownId: String, weightMt: Double, timestamp: Long) {
            val g = godownStorage[godownId]
            if (g != null) {
                godownStorage[godownId] = g.copy(currentStockMt = g.currentStockMt + weightMt)
            }
        }
        override suspend fun reduceStock(godownId: String, weightMt: Double, timestamp: Long): Int = 1
    }

    private val fakeProcurementDao = object : com.example.data.local.ProcurementDao {
        override fun getAllProcurements(): Flow<List<ProcurementEntity>> = flowOf(procurementStorage)
        override suspend fun getAllProcurementsDirect(): List<ProcurementEntity> = procurementStorage
        override fun getNonArchivedProcurements(): Flow<List<ProcurementEntity>> = flowOf(procurementStorage)
        override fun getArchivedProcurements(): Flow<List<ProcurementEntity>> = flowOf(emptyList())
        override fun getActiveProcurements(): Flow<List<ProcurementEntity>> = flowOf(emptyList())
        override suspend fun getProcurementById(id: Long): ProcurementEntity? = procurementStorage.find { it.id == id }
        override suspend fun getProcurementByUuid(uuid: String): ProcurementEntity? = procurementStorage.find { it.uuid == uuid }
        override suspend fun getProcurementByToken(tokenNo: String): ProcurementEntity? = procurementStorage.find { it.tokenNo == tokenNo }
        override suspend fun getProcurementsByParty(partyId: Long): List<ProcurementEntity> = procurementStorage.filter { it.partyId == partyId }
        override fun getProcurementsByPartyFlow(partyId: Long): Flow<List<ProcurementEntity>> = flowOf(procurementStorage.filter { it.partyId == partyId })
        override suspend fun getCumulativeFarmerGross(farmerName: String): Double? = 0.0
        override suspend fun getCumulativePartyGross(partyId: Long): Double? = 0.0
        override suspend fun insertProcurement(procurement: ProcurementEntity): Long {
            val saved = procurement.copy(id = procurementStorage.size + 1L)
            procurementStorage.add(saved)
            return saved.id
        }
        override suspend fun insertProcurements(procurements: List<ProcurementEntity>) {
            procurementStorage.addAll(procurements)
        }
        override suspend fun updateProcurement(procurement: ProcurementEntity) {}
        override suspend fun updateArchiveStatus(id: Long, isArchived: Boolean, timestamp: Long) {}
        override suspend fun deleteProcurement(id: Long) {}
        override fun getTotalCount(): Flow<Int> = flowOf(procurementStorage.size)
        override fun getTotalProcuredKg(): Flow<Double?> = flowOf(0.0)
        override fun getTotalPayoutAmount(): Flow<Double?> = flowOf(0.0)
        override fun getTotalMandiCessCollected(): Flow<Double?> = flowOf(0.0)
        override fun getTotalTdsDeducted(): Flow<Double?> = flowOf(0.0)
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
            override suspend fun update(party: PartyEntity) { partyStorage[party.id] = party }
            override fun getAllPartiesFlow(): Flow<List<PartyEntity>> = flowOf(partyStorage.values.toList())
            override suspend fun getAllParties(): List<PartyEntity> = partyStorage.values.toList()
            override suspend fun getByType(type: String): List<PartyEntity> = emptyList()
            override fun getByTypeFlow(type: String): Flow<List<PartyEntity>> = flowOf(emptyList())
            override suspend fun getByMobile(mobile: String): PartyEntity? = null
            override suspend fun getByPan(pan: String): PartyEntity? = null
            override suspend fun getById(id: Long): PartyEntity? = partyStorage[id]
            override suspend fun getByUuid(uuid: String): PartyEntity? = null
            override suspend fun search(searchQuery: String?): List<PartyEntity> = emptyList()
            override suspend fun incrementCumulativePurchases(partyId: Long, amount: Double, timestamp: Long) {
                val p = partyStorage[partyId]
                if (p != null) {
                    partyStorage[partyId] = p.copy(cumulativePurchasesInFy = p.cumulativePurchasesInFy + amount)
                }
            }
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
            override suspend fun getNextDocumentNumber(fy: String, facilityId: String, docType: String): String = "GIN/26-27/00001"
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
    private val createProcurementUseCase = CreateProcurementUseCase(
        fakeProcurementDao, fakeGodownDao, fakeVendorLedgerDao, fakePartyRepo,
        generateDocNumber, postMovement, fakeAuditRepo
    )

    @Test
    fun testCreateProcurement_calculatesApmcCessAndAddsStock() = runBlocking {
        val request = CreateProcurementRequest(
            partyId = 1L,
            vehicleNumber = "MH 18 AB 4321",
            cropType = CropType.MAIZE,
            grossWeightKg = 15000.0,
            tareWeightKg = 5000.0, // Net = 10,000 kg = 100 Qtl = 10 MT
            moisturePercentage = 12.0,
            ratePerQuintal = 2400.0, // Gross Bill = ₹2,40,000
            godownAssigned = "GODOWN_A",
            applyMandiCess = true, // 1.5% = ₹3,600
            enableTds194q = false
        )

        val result = createProcurementUseCase(request)
        assertTrue(result.isSuccess)

        val saved = result.getOrNull()!!
        assertEquals("GIN/26-27/00001", saved.tokenNo)
        assertEquals(10000.0, saved.netWeightKg, 0.01)
        assertEquals(240000.0, saved.grossBillAmount, 0.01)
        assertEquals(3600.0, saved.totalMandiCess, 0.01)
        assertEquals(236400.0, saved.totalAmount, 0.01) // 240,000 - 3,600

        // Verify Godown stock increased by 10 MT (50 MT -> 60 MT)
        assertEquals(60.0, godownStorage["GODOWN_A"]?.currentStockMt ?: 0.0, 0.01)

        // Verify Inbound Inventory Movement posted
        assertEquals(1, movementStorage.size)
        assertEquals(10000.0, movementStorage[0].quantityKg, 0.01)

        // Verify Party Cumulative Purchases incremented
        assertEquals(340000.0, partyStorage[1L]?.cumulativePurchasesInFy ?: 0.0, 0.01)
    }
}
