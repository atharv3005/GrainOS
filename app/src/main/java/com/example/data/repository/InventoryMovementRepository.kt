package com.example.data.repository

import com.example.data.local.InventoryMovementDao
import com.example.data.model.InventoryMovementEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

open class InventoryMovementRepository(private val movementDao: InventoryMovementDao) {

    val allMovementsFlow: Flow<List<InventoryMovementEntity>> = movementDao.getAllMovementsFlow()

    fun getMovementsByFacilityFlow(facilityId: String): Flow<List<InventoryMovementEntity>> {
        return movementDao.getMovementsByFacilityFlow(facilityId)
    }

    suspend fun post(movement: InventoryMovementEntity): Long = withContext(Dispatchers.IO) {
        movementDao.insert(movement)
    }

    suspend fun postAll(movements: List<InventoryMovementEntity>) = withContext(Dispatchers.IO) {
        movementDao.insertAll(movements)
    }

    suspend fun getMovementsByFacility(facilityId: String): List<InventoryMovementEntity> = withContext(Dispatchers.IO) {
        movementDao.getMovementsByFacility(facilityId)
    }

    suspend fun getMovementsByBatch(facilityId: String, batchId: String): List<InventoryMovementEntity> = withContext(Dispatchers.IO) {
        movementDao.getMovementsByBatch(facilityId, batchId)
    }

    suspend fun calculateStockKg(facilityId: String): Double = withContext(Dispatchers.IO) {
        movementDao.calculateFacilityStockKg(facilityId)
    }

    suspend fun calculateStockMt(facilityId: String): Double = withContext(Dispatchers.IO) {
        movementDao.calculateFacilityStockKg(facilityId) / 1000.0
    }

    suspend fun getBySourceEntity(uuid: String): InventoryMovementEntity? = withContext(Dispatchers.IO) {
        movementDao.getBySourceEntity(uuid)
    }
}
