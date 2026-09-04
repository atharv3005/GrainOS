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

    @Query("SELECT * FROM procurements ORDER BY id ASC")
    suspend fun getAllProcurementsDirect(): List<ProcurementEntity>

    @Query("SELECT * FROM procurements WHERE isArchived = 0 ORDER BY id DESC")
    fun getNonArchivedProcurements(): Flow<List<ProcurementEntity>>

    @Query("SELECT * FROM procurements WHERE isArchived = 1 ORDER BY id DESC")
    fun getArchivedProcurements(): Flow<List<ProcurementEntity>>

    @Query("SELECT * FROM procurements WHERE status != 'COMPLETED' AND isArchived = 0 ORDER BY id DESC")
    fun getActiveProcurements(): Flow<List<ProcurementEntity>>

    @Query("SELECT * FROM procurements WHERE id = :id LIMIT 1")
    suspend fun getProcurementById(id: Long): ProcurementEntity?

    @Query("SELECT * FROM procurements WHERE uuid = :uuid LIMIT 1")
    suspend fun getProcurementByUuid(uuid: String): ProcurementEntity?

    @Query("SELECT * FROM procurements WHERE tokenNo = :tokenNo LIMIT 1")
    suspend fun getProcurementByToken(tokenNo: String): ProcurementEntity?

    @Query("SELECT * FROM procurements WHERE party_id = :partyId ORDER BY id DESC")
    suspend fun getProcurementsByParty(partyId: Long): List<ProcurementEntity>

    @Query("SELECT * FROM procurements WHERE party_id = :partyId ORDER BY id DESC")
    fun getProcurementsByPartyFlow(partyId: Long): Flow<List<ProcurementEntity>>

    @Query("SELECT SUM(grossBillAmount) FROM procurements WHERE farmerName = :farmerName")
    suspend fun getCumulativeFarmerGross(farmerName: String): Double?

    @Query("SELECT SUM(grossBillAmount) FROM procurements WHERE party_id = :partyId")
    suspend fun getCumulativePartyGross(partyId: Long): Double?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProcurement(procurement: ProcurementEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProcurements(procurements: List<ProcurementEntity>)

    @Update
    suspend fun updateProcurement(procurement: ProcurementEntity)

    @Query("UPDATE procurements SET isArchived = :isArchived, updated_at = :timestamp WHERE id = :id")
    suspend fun updateArchiveStatus(id: Long, isArchived: Boolean, timestamp: Long = System.currentTimeMillis())

    @Deprecated("Prefer logging audit trail and using reversal transactions rather than hard deletion.")
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
