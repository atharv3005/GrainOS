package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.InventoryMovementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryMovementDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(movement: InventoryMovementEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(movements: List<InventoryMovementEntity>)

    @Query("SELECT * FROM inventory_movements ORDER BY timestamp DESC")
    fun getAllMovementsFlow(): Flow<List<InventoryMovementEntity>>

    @Query("SELECT * FROM inventory_movements ORDER BY timestamp DESC")
    suspend fun getAllMovements(): List<InventoryMovementEntity>

    @Query("SELECT * FROM inventory_movements WHERE facility_id = :facilityId ORDER BY timestamp DESC")
    fun getMovementsByFacilityFlow(facilityId: String): Flow<List<InventoryMovementEntity>>

    @Query("SELECT * FROM inventory_movements WHERE facility_id = :facilityId ORDER BY timestamp ASC")
    suspend fun getMovementsByFacility(facilityId: String): List<InventoryMovementEntity>

    @Query("SELECT * FROM inventory_movements WHERE facility_id = :facilityId AND batch_id = :batchId ORDER BY timestamp ASC")
    suspend fun getMovementsByBatch(facilityId: String, batchId: String): List<InventoryMovementEntity>

    @Query("SELECT COALESCE(SUM(quantity_kg), 0.0) FROM inventory_movements WHERE facility_id = :facilityId")
    suspend fun calculateFacilityStockKg(facilityId: String): Double

    @Query("SELECT COALESCE(SUM(quantity_kg), 0.0) FROM inventory_movements WHERE facility_id = :facilityId AND batch_id = :batchId")
    suspend fun calculateBatchStockKg(facilityId: String, batchId: String): Double

    @Query("SELECT * FROM inventory_movements WHERE source_entity_uuid = :uuid LIMIT 1")
    suspend fun getBySourceEntity(uuid: String): InventoryMovementEntity?

    @Query("SELECT * FROM inventory_movements WHERE uuid = :uuid LIMIT 1")
    suspend fun getByUuid(uuid: String): InventoryMovementEntity?
}
