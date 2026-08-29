package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ProcurementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProcurementDao {
    @Query("SELECT * FROM procurements ORDER BY id DESC")
    fun getAllProcurements(): Flow<List<ProcurementEntity>>

    @Query("SELECT * FROM procurements WHERE status != 'COMPLETED' ORDER BY id DESC")
    fun getActiveProcurements(): Flow<List<ProcurementEntity>>

    @Query("SELECT * FROM procurements WHERE id = :id")
    suspend fun getProcurementById(id: Long): ProcurementEntity?

    @Query("SELECT SUM(grossBillAmount) FROM procurements WHERE farmerName = :farmerName")
    suspend fun getCumulativeFarmerGross(farmerName: String): Double?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProcurement(procurement: ProcurementEntity): Long

    @Update
    suspend fun updateProcurement(procurement: ProcurementEntity)

    @Query("DELETE FROM procurements WHERE id = :id")
    suspend fun deleteProcurement(id: Long)

    @Query("SELECT COUNT(*) FROM procurements")
    fun getTotalCount(): Flow<Int>

    @Query("SELECT SUM(netWeightKg) FROM procurements WHERE status = 'COMPLETED'")
    fun getTotalProcuredKg(): Flow<Double?>

    @Query("SELECT SUM(totalAmount) FROM procurements WHERE status = 'COMPLETED'")
    fun getTotalPayoutAmount(): Flow<Double?>

    @Query("SELECT SUM(totalMandiCess) FROM procurements WHERE applyMandiCess = 1 AND status = 'COMPLETED'")
    fun getTotalMandiCessCollected(): Flow<Double?>

    @Query("SELECT SUM(tdsDeductedAmount) FROM procurements WHERE enableTds194q = 1 AND status = 'COMPLETED'")
    fun getTotalTdsDeducted(): Flow<Double?>
}
