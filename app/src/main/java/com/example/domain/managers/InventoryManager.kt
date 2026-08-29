package com.example.domain.managers

import com.example.data.local.GodownDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class InventoryManager(
    private val godownDao: GodownDao
) {
    suspend fun addWeightToSilo(godownId: String, netWeightKg: Double) {
        withContext(Dispatchers.IO) {
            val weightMt = netWeightKg / 1000.0
            val godown = godownDao.getGodownById(godownId)
            if (godown != null) {
                godownDao.addStock(godownId, weightMt)
            }
        }
    }

    suspend fun subtractWeightFromSilo(godownId: String, dispatchWeightKg: Double) {
        withContext(Dispatchers.IO) {
            val weightMt = dispatchWeightKg / 1000.0
            godownDao.reduceStock(godownId, weightMt)
        }
    }
}
