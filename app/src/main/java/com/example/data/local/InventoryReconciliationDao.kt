package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.InventoryReconciliationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryReconciliationDao {
    @Query("SELECT * FROM inventory_reconciliations ORDER BY timestamp DESC")
    fun getAllReconciliations(): Flow<List<InventoryReconciliationEntity>>

    @Query("SELECT * FROM inventory_reconciliations WHERE id = :id LIMIT 1")
    suspend fun getReconciliationById(id: Long): InventoryReconciliationEntity?

    @Query("SELECT * FROM inventory_reconciliations WHERE uuid = :uuid LIMIT 1")
    suspend fun getReconciliationByUuid(uuid: String): InventoryReconciliationEntity?

    @Query("SELECT * FROM inventory_reconciliations WHERE godownId = :godownId ORDER BY timestamp DESC")
    fun getReconciliationsForGodown(godownId: String): Flow<List<InventoryReconciliationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReconciliation(reconciliation: InventoryReconciliationEntity): Long

    @Update
    suspend fun updateReconciliation(reconciliation: InventoryReconciliationEntity)

    @Deprecated("Prefer logging audit trail and using reversal transactions rather than hard deletion.")
    @Query("DELETE FROM inventory_reconciliations WHERE id = :id")
    suspend fun deleteReconciliation(id: Long)
}
