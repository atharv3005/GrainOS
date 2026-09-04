package com.example.domain.usecase

import com.example.data.model.AuditTrailEntity
import com.example.data.model.PaymentStatus
import com.example.data.model.PdcStatus
import com.example.data.model.ProcurementEntity
import com.example.data.model.VendorLedgerEntity
import com.example.data.repository.AuditTrailRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkPdcLifecycleTest {

    private val ledgerStorage = mutableMapOf<String, VendorLedgerEntity>()
    private val procurementStorage = mutableMapOf<String, ProcurementEntity>()
    private val auditStorage = mutableListOf<AuditTrailEntity>()

    private val fakeVendorLedgerDao = object : com.example.data.local.VendorLedgerDao {
        override fun getAllLedgerEntries(): Flow<List<VendorLedgerEntity>> = flowOf(ledgerStorage.values.toList())
        override suspend fun getAllLedgersDirect(): List<VendorLedgerEntity> = ledgerStorage.values.toList()
        override suspend fun getById(id: Long): VendorLedgerEntity? = ledgerStorage.values.find { it.id == id }
        override suspend fun getByUuid(uuid: String): VendorLedgerEntity? = ledgerStorage[uuid]
        override suspend fun getLedgerEntriesByParty(partyId: Long): List<VendorLedgerEntity> = ledgerStorage.values.filter { it.partyId == partyId }
        override fun getLedgerEntriesByPartyFlow(partyId: Long): Flow<List<VendorLedgerEntity>> = flowOf(ledgerStorage.values.filter { it.partyId == partyId })
        override fun getLedgerEntriesByType(vendorType: String): Flow<List<VendorLedgerEntity>> = flowOf(ledgerStorage.values.filter { it.vendorType == vendorType })
        override fun getLedgerEntriesForVendor(vendorName: String): Flow<List<VendorLedgerEntity>> = flowOf(ledgerStorage.values.filter { it.vendorName == vendorName })
        override fun getAllPdcsFlow(): Flow<List<VendorLedgerEntity>> = flowOf(ledgerStorage.values.filter { it.pdcStatus != PdcStatus.NONE.name })
        override fun getPdcsByStatusFlow(status: String): Flow<List<VendorLedgerEntity>> = flowOf(ledgerStorage.values.filter { it.pdcStatus == status })
        override fun getActivePdcsFlow(): Flow<List<VendorLedgerEntity>> = flowOf(ledgerStorage.values.filter { it.pdcStatus in listOf(PdcStatus.ISSUED.name, PdcStatus.DEPOSITED.name, PdcStatus.PRESENTED.name) })
        override suspend fun getPdcsDueBetween(startTs: Long, endTs: Long): List<VendorLedgerEntity> = emptyList()
        override suspend fun getCumulativeFarmerPurchases(vendorName: String): Double? = 0.0
        override suspend fun getCumulativePartyPurchases(partyId: Long): Double? = 0.0
        override suspend fun insertLedgerEntry(entry: VendorLedgerEntity): Long {
            ledgerStorage[entry.uuid] = entry
            return ledgerStorage.size.toLong()
        }
        override suspend fun insertLedgerEntries(entries: List<VendorLedgerEntity>) {
            entries.forEach { ledgerStorage[it.uuid] = it }
        }
        override suspend fun updateLedgerEntry(entry: VendorLedgerEntity) {
            ledgerStorage[entry.uuid] = entry
        }
        override suspend fun deleteLedgerEntry(id: Long) {}
    }

    private val fakeProcurementDao = object : com.example.data.local.ProcurementDao {
        override fun getAllProcurements(): Flow<List<ProcurementEntity>> = flowOf(procurementStorage.values.toList())
        override suspend fun getAllProcurementsDirect(): List<ProcurementEntity> = procurementStorage.values.toList()
        override fun getNonArchivedProcurements(): Flow<List<ProcurementEntity>> = flowOf(procurementStorage.values.filter { !it.isArchived })
        override fun getArchivedProcurements(): Flow<List<ProcurementEntity>> = flowOf(procurementStorage.values.filter { it.isArchived })
        override fun getActiveProcurements(): Flow<List<ProcurementEntity>> = flowOf(procurementStorage.values.filter { it.status != "COMPLETED" })
        override suspend fun getProcurementById(id: Long): ProcurementEntity? = procurementStorage.values.find { it.id == id }
        override suspend fun getProcurementByUuid(uuid: String): ProcurementEntity? = procurementStorage[uuid]
        override suspend fun getProcurementByToken(tokenNo: String): ProcurementEntity? = procurementStorage.values.find { it.tokenNo == tokenNo }
        override suspend fun getProcurementsByParty(partyId: Long): List<ProcurementEntity> = procurementStorage.values.filter { it.partyId == partyId }
        override fun getProcurementsByPartyFlow(partyId: Long): Flow<List<ProcurementEntity>> = flowOf(procurementStorage.values.filter { it.partyId == partyId })
        override suspend fun getCumulativeFarmerGross(farmerName: String): Double? = 0.0
        override suspend fun getCumulativePartyGross(partyId: Long): Double? = 0.0
        override suspend fun insertProcurement(procurement: ProcurementEntity): Long {
            procurementStorage[procurement.uuid] = procurement
            return procurementStorage.size.toLong()
        }
        override suspend fun insertProcurements(procurements: List<ProcurementEntity>) {
            procurements.forEach { procurementStorage[it.uuid] = it }
        }
        override suspend fun updateProcurement(procurement: ProcurementEntity) {
            procurementStorage[procurement.uuid] = procurement
        }
        override suspend fun updateArchiveStatus(id: Long, isArchived: Boolean, timestamp: Long) {}
        override suspend fun deleteProcurement(id: Long) {}
        override fun getTotalCount(): Flow<Int> = flowOf(procurementStorage.size)
        override fun getTotalProcuredKg(): Flow<Double?> = flowOf(0.0)
        override fun getTotalPayoutAmount(): Flow<Double?> = flowOf(0.0)
        override fun getTotalMandiCessCollected(): Flow<Double?> = flowOf(0.0)
        override fun getTotalTdsDeducted(): Flow<Double?> = flowOf(0.0)
    }

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

    private val depositUseCase = MarkPdcAsDepositedUseCase(fakeVendorLedgerDao, fakeAuditRepo)
    private val presentUseCase = MarkPdcAsPresentedUseCase(fakeVendorLedgerDao, fakeAuditRepo)
    private val clearUseCase = MarkPdcAsClearedUseCase(fakeVendorLedgerDao, fakeProcurementDao, fakeAuditRepo)
    private val bounceUseCase = MarkPdcAsBouncedUseCase(fakeVendorLedgerDao, fakeProcurementDao, fakeAuditRepo)

    @Test
    fun testPdcStateTransition_IssuedToDepositedToPresentedToCleared() = runBlocking {
        val proc = ProcurementEntity(
            tokenNo = "GIN/26-27/00101",
            farmerName = "Shantaram Patil",
            mobileNumber = "9822000001",
            village = "Dhule Village",
            vehicleNumber = "MH 18 AA 1122",
            ratePerQuintal = 2400.0,
            netWeightKg = 5000.0,
            totalAmount = 120000.0,
            paymentStatus = PaymentStatus.PENDING.name,
            isPdc = true,
            pdcCleared = false
        )
        fakeProcurementDao.insertProcurement(proc)

        val pdc = VendorLedgerEntity(
            vendorType = "FARMER",
            vendorName = "Shantaram Patil",
            transactionType = "PDC_ISSUED",
            amount = 120000.0,
            utrOrChequeNo = "CHQ-99001",
            chequeMaturityDate = System.currentTimeMillis() + 86400000L,
            pdcStatus = PdcStatus.ISSUED.name,
            referenceDocNo = proc.tokenNo,
            runningBalance = 120000.0
        )
        fakeVendorLedgerDao.insertLedgerEntry(pdc)

        // 1. Deposit PDC
        val depRes = depositUseCase(pdc.uuid)
        assertTrue(depRes.isSuccess)
        assertEquals(PdcStatus.DEPOSITED.name, fakeVendorLedgerDao.getByUuid(pdc.uuid)?.pdcStatus)

        // 2. Present PDC
        val presRes = presentUseCase(pdc.uuid)
        assertTrue(presRes.isSuccess)
        assertEquals(PdcStatus.PRESENTED.name, fakeVendorLedgerDao.getByUuid(pdc.uuid)?.pdcStatus)

        // 3. Clear PDC
        val clrRes = clearUseCase(pdc.uuid)
        assertTrue(clrRes.isSuccess)
        val clearedEntry = fakeVendorLedgerDao.getByUuid(pdc.uuid)!!
        assertEquals(PdcStatus.CLEARED.name, clearedEntry.pdcStatus)
        assertEquals(0.0, clearedEntry.runningBalance, 0.01)

        // Verify Procurement marked PAID
        val updatedProc = fakeProcurementDao.getProcurementByToken(proc.tokenNo)!!
        assertEquals(PaymentStatus.PAID.name, updatedProc.paymentStatus)
        assertTrue(updatedProc.pdcCleared)
    }

    @Test
    fun testPdcBounced_reopensDebtToUnpaid() = runBlocking {
        val proc = ProcurementEntity(
            tokenNo = "GIN/26-27/00102",
            farmerName = "Kishor Wagh",
            mobileNumber = "9822000002",
            village = "Dhule Village",
            vehicleNumber = "MH 18 Q 3344",
            ratePerQuintal = 2400.0,
            netWeightKg = 5000.0,
            totalAmount = 120000.0,
            paymentStatus = PaymentStatus.PENDING.name,
            isPdc = true,
            pdcCleared = false
        )
        fakeProcurementDao.insertProcurement(proc)

        val pdc = VendorLedgerEntity(
            vendorType = "FARMER",
            vendorName = "Kishor Wagh",
            transactionType = "PDC_ISSUED",
            amount = 120000.0,
            utrOrChequeNo = "CHQ-99002",
            chequeMaturityDate = System.currentTimeMillis() + 86400000L,
            pdcStatus = PdcStatus.PRESENTED.name,
            referenceDocNo = proc.tokenNo,
            runningBalance = 0.0
        )
        fakeVendorLedgerDao.insertLedgerEntry(pdc)

        // Bounce PDC
        val bounceRes = bounceUseCase(pdc.uuid, bounceReason = "Insufficient Funds in Account")
        assertTrue(bounceRes.isSuccess)

        val bouncedEntry = fakeVendorLedgerDao.getByUuid(pdc.uuid)!!
        assertEquals(PdcStatus.BOUNCED.name, bouncedEntry.pdcStatus)
        assertEquals("Insufficient Funds in Account", bouncedEntry.bounceReason)
        assertEquals(120000.0, bouncedEntry.runningBalance, 0.01) // Liability restored

        // Verify original Procurement reopened as UNPAID
        val updatedProc = fakeProcurementDao.getProcurementByToken(proc.tokenNo)!!
        assertEquals(PaymentStatus.UNPAID.name, updatedProc.paymentStatus)
        assertEquals(false, updatedProc.pdcCleared)
    }
}
