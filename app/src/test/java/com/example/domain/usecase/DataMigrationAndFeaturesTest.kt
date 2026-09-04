package com.example.domain.usecase

import com.example.data.export.EWayBillGenerator
import com.example.data.local.GeneralLedgerDao
import com.example.data.model.CropType
import com.example.data.model.FirmProfile
import com.example.data.model.GeneralLedgerEntity
import com.example.data.model.OutboundDispatchEntity
import com.example.data.model.PartyEntity
import com.example.data.model.PartyType
import com.example.data.model.ProcurementEntity
import com.example.domain.managers.FifoCostingEngine
import com.example.domain.managers.PartialTransactionManager
import com.example.security.RbacManager
import com.example.security.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DataMigrationAndFeaturesTest {

    @Test
    fun testRbacPinHashingAndVerification() {
        val pin = "1234"
        val salt = RbacManager.generateSalt()
        val hash = RbacManager.hashPin(pin, salt)
        assertTrue(RbacManager.verifyPin("1234", hash, salt))
        assertFalse(RbacManager.verifyPin("0000", hash, salt))

        // Ensure different salts generate different hashes
        val salt2 = RbacManager.generateSalt()
        val hash2 = RbacManager.hashPin(pin, salt2)
        assertNotEquals(hash, hash2)

        assertTrue(RbacManager.hasPermission(UserRole.OWNER, com.example.security.Permission.APPROVE_OVERRIDE))
        assertFalse(RbacManager.hasPermission(UserRole.OPERATOR, com.example.security.Permission.APPROVE_OVERRIDE))
        assertTrue(RbacManager.hasPermission(UserRole.ACCOUNTANT, com.example.security.Permission.CLOSE_DAY_END))
    }

    @Test
    fun testJournalPostingWithApmcCessBalancedDoubleEntry() = runBlocking {
        val fakeEntries = mutableListOf<GeneralLedgerEntity>()
        val fakeDao = object : GeneralLedgerDao {
            override fun getAllEntriesFlow(): Flow<List<GeneralLedgerEntity>> = flowOf(fakeEntries)
            override suspend fun getVoucherEntries(voucherNo: String): List<GeneralLedgerEntity> = fakeEntries.filter { it.voucherNo == voucherNo }
            override fun getEntriesByAccountFlow(accountCode: String): Flow<List<GeneralLedgerEntity>> = flowOf(fakeEntries.filter { it.accountCode == accountCode })
            override fun getEntriesByPartyFlow(partyId: Long): Flow<List<GeneralLedgerEntity>> = flowOf(fakeEntries.filter { it.partyId == partyId })
            override suspend fun getAccountNetDebitBalance(accountCode: String): Double? = null
            override suspend fun getTotalDebits(): Double? = fakeEntries.sumOf { it.debitAmount }
            override suspend fun getTotalCredits(): Double? = fakeEntries.sumOf { it.creditAmount }
            override suspend fun insert(entry: GeneralLedgerEntity): Long {
                fakeEntries.add(entry)
                return entry.id
            }
            override suspend fun insertAll(entries: List<GeneralLedgerEntity>) {
                fakeEntries.addAll(entries)
            }
        }

        val useCase = PostJournalEntryUseCase(fakeDao)
        val proc = ProcurementEntity(
            tokenNo = "GIN/26-27/00142",
            farmerName = "Suresh Patil",
            mobileNumber = "9822011223",
            village = "Dhule",
            vehicleNumber = "MH 18 AB 1234",
            cropType = "MAIZE",
            grossWeightKg = 13500.0,
            tareWeightKg = 3500.0,
            netWeightKg = 10000.0, // 100 Qtl
            ratePerQuintal = 2200.0,
            grossBillAmount = 220000.0,
            applyMandiCess = true,
            totalMandiCess = 3300.0, // 1.5%
            enableTds194q = false,
            tdsDeductedAmount = 0.0,
            totalAmount = 216700.0 // 220000 - 3300
        )

        val result = useCase.postProcurementJournal(proc)
        assertTrue(result.isSuccess)
        assertEquals(3, fakeEntries.size) // Grain Inventory (Dr), Mandi Cess (Cr), Farmer Payable (Cr)

        val totalDebits = fakeEntries.sumOf { it.debitAmount }
        val totalCredits = fakeEntries.sumOf { it.creditAmount }
        assertEquals(220000.0, totalDebits, 0.01)
        assertEquals(220000.0, totalCredits, 0.01)
    }

    @Test
    fun testPartialTransactionSplitting() {
        val baseProc = ProcurementEntity(
            tokenNo = "GIN/26-27/00142",
            farmerName = "Suresh Patil",
            mobileNumber = "9822011223",
            village = "Dhule Village",
            vehicleNumber = "MH 18 AB 1234",
            cropType = "MAIZE",
            grossWeightKg = 13500.0,
            tareWeightKg = 3500.0,
            netWeightKg = 10000.0,
            ratePerQuintal = 2200.0,
            grossBillAmount = 220000.0,
            totalAmount = 220000.0
        )

        // Split: 7,000 kg Grade A at ₹2200/Qtl, 3,000 kg Grade B Salvage at ₹1950/Qtl
        val splitResult = PartialTransactionManager.splitProcurementTransaction(
            baseProcurement = baseProc,
            acceptedWeightKg = 7000.0,
            acceptedRatePerQuintal = 2200.0,
            salvageWeightKg = 3000.0,
            salvageRatePerQuintal = 1950.0,
            rejectedWeightKg = 0.0
        )

        assertEquals("GIN/26-27/00142-A", splitResult.primaryAcceptedProcurement.tokenNo)
        assertEquals(7000.0, splitResult.primaryAcceptedProcurement.netWeightKg, 0.01)
        assertEquals(154000.0, splitResult.primaryAcceptedProcurement.grossBillAmount, 0.01)

        assertNotNull(splitResult.secondarySalvageProcurement)
        assertEquals("GIN/26-27/00142-B", splitResult.secondarySalvageProcurement?.tokenNo)
        assertEquals(3000.0, splitResult.secondarySalvageProcurement?.netWeightKg ?: 0.0, 0.01)
        assertEquals(58500.0, splitResult.secondarySalvageProcurement?.grossBillAmount ?: 0.0, 0.01)

        assertEquals(212500.0, splitResult.totalPayoutAmount, 0.01)
    }

    @Test
    fun testFifoCostingEngineWithShortageLotHandling() {
        val procs = listOf(
            ProcurementEntity(
                tokenNo = "GIN-01",
                farmerName = "Farmer 1",
                mobileNumber = "9800000001",
                village = "Village 1",
                vehicleNumber = "MH18-0001",
                cropType = "MAIZE",
                netWeightKg = 10000.0, // 100 Qtl @ 2000 = 2,00,000
                ratePerQuintal = 2000.0,
                grossBillAmount = 200000.0,
                createdAt = 1000L
            )
        )

        // Dispatch 15,000 kg when only 10,000 kg lot available (5,000 kg shortage)
        val dispatches = listOf(
            OutboundDispatchEntity(
                dispatchNo = "DSP-01",
                buyerName = "Cargill",
                vehicleNumber = "MH18-1234",
                cropType = "MAIZE",
                godownSource = "GODOWN_A",
                tareWeightKg = 3500.0,
                grossWeightKg = 18500.0,
                netLoadedWeightKg = 15000.0,
                ratePerQuintal = 2500.0,
                totalInvoiceAmount = 375000.0,
                destination = "Pune",
                timestamp = 3000L
            )
        )

        val fifoResult = FifoCostingEngine.calculateFifoCosting(procs, dispatches, "MAIZE")

        // 10,000 kg @ 2000/Qtl (2,00,000) + 5,000 kg shortage @ avg rate 2000/Qtl (1,00,000) = 3,00,000
        assertEquals(375000.0, fifoResult.totalRevenue, 0.01)
        assertEquals(300000.0, fifoResult.totalCogs, 0.01)
        assertEquals(75000.0, fifoResult.realizedGrossMargin, 0.01)
        assertEquals(5000.0, fifoResult.unmatchedShortageKg, 0.01)
    }

    @Test
    fun testEWayBillGeneratorWithEscaping() {
        val dispatch = OutboundDispatchEntity(
            dispatchNo = "DSP/26-27/00088",
            buyerName = "Godrej \"Agro\" Ltd",
            vehicleNumber = "MH 18 AB 9876",
            cropType = "MAIZE",
            godownSource = "GODOWN_A",
            tareWeightKg = 3500.0,
            grossWeightKg = 27500.0,
            netLoadedWeightKg = 24000.0,
            ratePerQuintal = 2450.0,
            totalInvoiceAmount = 588000.0,
            destination = "Pune Plant, Maharashtra"
        )
        val firm = FirmProfile(
            firmName = "GrainOS \"Enterprise\" Hub",
            gstNumber = "27AABCB1234F1Z5",
            location = "Dhule, Maharashtra"
        )
        val buyerParty = PartyEntity(
            partyType = PartyType.BUYER.name,
            legalName = "Godrej Agrovet Ltd",
            mobile = "9922001122",
            gstin = "27XYZPA9876Q1Z9"
        )

        val ewbJson = EWayBillGenerator.generateEWayBillJson(dispatch, firm, buyerParty)
        assertTrue(ewbJson.contains("10059000")) // HSN for Maize
        assertTrue(ewbJson.contains("DSP/26-27/00088"))
        assertTrue(ewbJson.contains("MH18AB9876"))
        assertTrue(ewbJson.contains("27AABCB1234F1Z5"))
        assertTrue(ewbJson.contains("\\\"Enterprise\\\"")) // Escaped quotes

        val eInvJson = EWayBillGenerator.generateEInvoicePayload(dispatch, firm, buyerParty)
        assertTrue(eInvJson.contains("SignedQRCode"))
        assertTrue(eInvJson.contains("Irn"))
    }
}
