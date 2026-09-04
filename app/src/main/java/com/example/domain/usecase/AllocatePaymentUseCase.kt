package com.example.domain.usecase

import com.example.data.model.AllocationType
import com.example.data.model.PaymentAllocationEntity
import com.example.data.model.PaymentMode
import com.example.data.repository.AuditTrailRepository
import com.example.data.repository.PaymentAllocationRepository

data class PayableAllocationItem(
    val payableUuid: String,
    val allocatedAmountRupees: Double,
    val payableTotalAmountRupees: Double
)

data class AllocatePaymentRequest(
    val paymentUuid: String,
    val partyId: Long? = null,
    val totalPaymentAmountRupees: Double,
    val paymentMode: PaymentMode = PaymentMode.RTGS,
    val allocations: List<PayableAllocationItem>,
    val userId: String = "operator",
    val deviceId: String = "local_device",
    val notes: String = ""
)

/**
 * Domain Use Case to split and allocate lump-sum payments across multiple specific payable bills.
 */
class AllocatePaymentUseCase(
    private val paymentAllocationRepository: PaymentAllocationRepository,
    private val auditTrailRepository: AuditTrailRepository
) {
    suspend operator fun invoke(request: AllocatePaymentRequest): Result<List<PaymentAllocationEntity>> {
        return try {
            require(request.totalPaymentAmountRupees > 0.0) { "Payment amount must be positive." }

            val totalAllocatedRupees = request.allocations.sumOf { it.allocatedAmountRupees }
            require(totalAllocatedRupees <= request.totalPaymentAmountRupees + 0.01) {
                "Total allocated amount (₹$totalAllocatedRupees) exceeds total payment amount (₹${request.totalPaymentAmountRupees})."
            }

            val savedAllocations = mutableListOf<PaymentAllocationEntity>()

            // 1. Process specific payable allocations
            for (item in request.allocations) {
                if (item.allocatedAmountRupees <= 0.0) continue

                val previouslyAllocated = paymentAllocationRepository.getTotalAllocatedRupees(item.payableUuid)
                val remainingBefore = (item.payableTotalAmountRupees - previouslyAllocated).coerceAtLeast(0.0)
                val remainingAfter = (remainingBefore - item.allocatedAmountRupees).coerceAtLeast(0.0)

                val allocationType = if (remainingAfter <= 0.01) AllocationType.FULL else AllocationType.PARTIAL

                val allocation = PaymentAllocationEntity(
                    paymentUuid = request.paymentUuid,
                    payableUuid = item.payableUuid,
                    allocatedAmountPaise = (item.allocatedAmountRupees * 100.0).toLong(),
                    allocatedAmountRupees = item.allocatedAmountRupees,
                    allocationType = allocationType.name,
                    remainingPayableBalancePaise = (remainingAfter * 100.0).toLong(),
                    notes = request.notes
                )
                val id = paymentAllocationRepository.allocate(allocation)
                savedAllocations.add(allocation.copy(id = id))
            }

            // 2. Handle unallocated remainder as an Advance
            val unallocatedResidual = request.totalPaymentAmountRupees - totalAllocatedRupees
            if (unallocatedResidual > 0.01) {
                val advanceAllocation = PaymentAllocationEntity(
                    paymentUuid = request.paymentUuid,
                    payableUuid = "UNALLOCATED_ADVANCE_${request.partyId ?: "GEN"}",
                    allocatedAmountPaise = (unallocatedResidual * 100.0).toLong(),
                    allocatedAmountRupees = unallocatedResidual,
                    allocationType = AllocationType.ADVANCE.name,
                    remainingPayableBalancePaise = 0L,
                    notes = "Unallocated Advance Payment: ₹$unallocatedResidual"
                )
                val advId = paymentAllocationRepository.allocate(advanceAllocation)
                savedAllocations.add(advanceAllocation.copy(id = advId))
            }

            // 3. Log Audit Trail
            auditTrailRepository.logCreate(
                entityType = "PAYMENT_ALLOCATION",
                entityId = request.paymentUuid,
                newStateJson = "{\"paymentUuid\":\"${request.paymentUuid}\",\"allocated\":$totalAllocatedRupees,\"advance\":$unallocatedResidual}",
                userId = request.userId,
                deviceId = request.deviceId,
                reason = "Allocated payment ${request.paymentUuid} across ${savedAllocations.size} entries"
            )

            Result.success(savedAllocations)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
