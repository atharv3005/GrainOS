package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.InventoryReconciliationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryReconciliationDao {
    @Query("SELECT * FROM inventory_reconciliations ORDER BY timestamp DESC")
    fun getAllReconciliations(): Flow<List<InventoryReconciliationEntity>>

    @Query("SELECT * FROM inventory_reconciliations WHERE godownId = :godownId ORDER BY timestamp DESC")
    fun getReconciliationsForGodown(godownId: String): Flow<List<InventoryReconciliationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReconciliation(reconciliation: InventoryReconciliationEntity): Long

    @Query("DELETE FROM inventory_reconciliations WHERE id = :id")
    suspend fun deleteReconciliation(id: Long)
}
