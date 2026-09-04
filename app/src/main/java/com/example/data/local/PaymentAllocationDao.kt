package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.PaymentAllocationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentAllocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(allocation: PaymentAllocationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(allocations: List<PaymentAllocationEntity>)

    @Query("SELECT * FROM payment_allocations WHERE payment_uuid = :paymentUuid ORDER BY allocation_timestamp DESC")
    suspend fun getAllocationsForPayment(paymentUuid: String): List<PaymentAllocationEntity>

    @Query("SELECT * FROM payment_allocations WHERE payable_uuid = :payableUuid ORDER BY allocation_timestamp DESC")
    suspend fun getAllocationsForPayable(payableUuid: String): List<PaymentAllocationEntity>

    @Query("SELECT * FROM payment_allocations WHERE payable_uuid = :payableUuid ORDER BY allocation_timestamp DESC")
    fun getAllocationsForPayableFlow(payableUuid: String): Flow<List<PaymentAllocationEntity>>

    @Query("SELECT COALESCE(SUM(allocated_amount_paise), 0) FROM payment_allocations WHERE payable_uuid = :payableUuid")
    suspend fun getTotalAllocatedPaise(payableUuid: String): Long

    @Query("SELECT COALESCE(SUM(allocated_amount_rupees), 0.0) FROM payment_allocations WHERE payable_uuid = :payableUuid")
    suspend fun getTotalAllocatedRupees(payableUuid: String): Double

    @Query("SELECT * FROM payment_allocations ORDER BY allocation_timestamp DESC")
    fun getAllAllocationsFlow(): Flow<List<PaymentAllocationEntity>>
}
