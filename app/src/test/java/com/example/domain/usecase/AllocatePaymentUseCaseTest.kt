package com.example.domain.usecase

import com.example.data.model.AllocationType
import com.example.data.model.AuditTrailEntity
import com.example.data.model.PaymentAllocationEntity
import com.example.data.repository.AuditTrailRepository
import com.example.data.repository.PaymentAllocationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AllocatePaymentUseCaseTest {

    // In-memory fake implementation of repositories
    private val allocationStorage = mutableListOf<PaymentAllocationEntity>()
    private val auditStorage = mutableListOf<AuditTrailEntity>()

    private val fakeAllocationRepo = object : PaymentAllocationRepository(
        allocationDao = object : com.example.data.local.PaymentAllocationDao {
            override suspend fun insert(allocation: PaymentAllocationEntity): Long {
                allocationStorage.add(allocation)
                return allocationStorage.size.toLong()
            }
            override suspend fun insertAll(allocations: List<PaymentAllocationEntity>) {
                allocationStorage.addAll(allocations)
            }
            override suspend fun getAllocationsForPayment(paymentUuid: String): List<PaymentAllocationEntity> =
                allocationStorage.filter { it.paymentUuid == paymentUuid }
            override suspend fun getAllocationsForPayable(payableUuid: String): List<PaymentAllocationEntity> =
                allocationStorage.filter { it.payableUuid == payableUuid }
            override fun getAllocationsForPayableFlow(payableUuid: String): Flow<List<PaymentAllocationEntity>> =
                flowOf(allocationStorage.filter { it.payableUuid == payableUuid })
            override suspend fun getTotalAllocatedPaise(payableUuid: String): Long =
                allocationStorage.filter { it.payableUuid == payableUuid }.sumOf { it.allocatedAmountPaise }
            override suspend fun getTotalAllocatedRupees(payableUuid: String): Double =
                allocationStorage.filter { it.payableUuid == payableUuid }.sumOf { it.allocatedAmountRupees }
            override fun getAllAllocationsFlow(): Flow<List<PaymentAllocationEntity>> =
                flowOf(allocationStorage)
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
            override suspend fun getAuditHistory(type: String, entityId: String): List<AuditTrailEntity> =
                auditStorage.filter { it.entityType == type && it.entityId == entityId }
            override fun getAuditHistoryFlow(type: String, entityId: String): Flow<List<AuditTrailEntity>> =
                flowOf(auditStorage.filter { it.entityType == type && it.entityId == entityId })
            override suspend fun getAllDeletions(): List<AuditTrailEntity> =
                auditStorage.filter { it.action == "DELETE" }
            override fun getAllDeletionsFlow(): Flow<List<AuditTrailEntity>> =
                flowOf(auditStorage.filter { it.action == "DELETE" })
            override suspend fun filterAudit(action: String?, entityType: String?): List<AuditTrailEntity> =
                auditStorage.filter { (action == null || it.action == action) && (entityType == null || it.entityType == entityType) }
        }
    ) {}

    private val useCase = AllocatePaymentUseCase(fakeAllocationRepo, fakeAuditRepo)

    @Test
    fun testAllocatePayment_splitAcrossTwoPayablesAndAdvance() = runBlocking {
        val request = AllocatePaymentRequest(
            paymentUuid = "PAY_TEST_001",
            partyId = 1L,
            totalPaymentAmountRupees = 100000.0, // ₹1 Lakh
            allocations = listOf(
                PayableAllocationItem(
                    payableUuid = "PAYABLE_01",
                    allocatedAmountRupees = 40000.0,
                    payableTotalAmountRupees = 40000.0 // Fully cleared
                ),
                PayableAllocationItem(
                    payableUuid = "PAYABLE_02",
                    allocatedAmountRupees = 35000.0,
                    payableTotalAmountRupees = 50000.0 // Partially cleared (₹15,000 remaining)
                )
            )
        )

        val result = useCase(request)
        assertTrue(result.isSuccess)

        val allocations = result.getOrNull()!!
        assertEquals(3, allocations.size) // 2 specific + 1 advance for remaining ₹25,000

        // Check Payable 1
        val a1 = allocations.find { it.payableUuid == "PAYABLE_01" }!!
        assertEquals(40000.0, a1.allocatedAmountRupees, 0.01)
        assertEquals(AllocationType.FULL.name, a1.allocationType)
        assertEquals(0L, a1.remainingPayableBalancePaise)

        // Check Payable 2
        val a2 = allocations.find { it.payableUuid == "PAYABLE_02" }!!
        assertEquals(35000.0, a2.allocatedAmountRupees, 0.01)
        assertEquals(AllocationType.PARTIAL.name, a2.allocationType)
        assertEquals(1500000L, a2.remainingPayableBalancePaise) // ₹15,000 in paise

        // Check Advance
        val a3 = allocations.find { it.allocationType == AllocationType.ADVANCE.name }!!
        assertEquals(25000.0, a3.allocatedAmountRupees, 0.01)
    }
}
