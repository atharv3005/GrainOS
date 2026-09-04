package com.example.data.repository

import com.example.data.local.PaymentAllocationDao
import com.example.data.model.PaymentAllocationEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

open class PaymentAllocationRepository(private val allocationDao: PaymentAllocationDao) {

    val allAllocationsFlow: Flow<List<PaymentAllocationEntity>> = allocationDao.getAllAllocationsFlow()

    suspend fun allocate(allocation: PaymentAllocationEntity): Long = withContext(Dispatchers.IO) {
        allocationDao.insert(allocation)
    }

    suspend fun allocateAll(allocations: List<PaymentAllocationEntity>) = withContext(Dispatchers.IO) {
        allocationDao.insertAll(allocations)
    }

    suspend fun getAllocationsForPayment(paymentUuid: String): List<PaymentAllocationEntity> = withContext(Dispatchers.IO) {
        allocationDao.getAllocationsForPayment(paymentUuid)
    }

    suspend fun getAllocationsForPayable(payableUuid: String): List<PaymentAllocationEntity> = withContext(Dispatchers.IO) {
        allocationDao.getAllocationsForPayable(payableUuid)
    }

    fun getAllocationsForPayableFlow(payableUuid: String): Flow<List<PaymentAllocationEntity>> {
        return allocationDao.getAllocationsForPayableFlow(payableUuid)
    }

    suspend fun getTotalAllocatedRupees(payableUuid: String): Double = withContext(Dispatchers.IO) {
        allocationDao.getTotalAllocatedRupees(payableUuid)
    }
}
