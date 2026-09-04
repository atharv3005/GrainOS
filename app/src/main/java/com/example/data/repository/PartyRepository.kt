package com.example.data.repository

import com.example.data.local.PartyDao
import com.example.data.model.PartyEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

open class PartyRepository(private val partyDao: PartyDao) {

    val allPartiesFlow: Flow<List<PartyEntity>> = partyDao.getAllPartiesFlow()

    fun getPartiesByTypeFlow(type: String): Flow<List<PartyEntity>> {
        return partyDao.getByTypeFlow(type)
    }

    suspend fun create(party: PartyEntity): Long = withContext(Dispatchers.IO) {
        partyDao.insert(party)
    }

    suspend fun update(party: PartyEntity) = withContext(Dispatchers.IO) {
        partyDao.update(party)
    }

    suspend fun getById(id: Long): PartyEntity? = withContext(Dispatchers.IO) {
        partyDao.getById(id)
    }

    suspend fun getByUuid(uuid: String): PartyEntity? = withContext(Dispatchers.IO) {
        partyDao.getByUuid(uuid)
    }

    suspend fun getByMobile(mobile: String): PartyEntity? = withContext(Dispatchers.IO) {
        partyDao.getByMobile(mobile)
    }

    suspend fun getByPan(pan: String): PartyEntity? = withContext(Dispatchers.IO) {
        partyDao.getByPan(pan)
    }

    suspend fun search(query: String?): List<PartyEntity> = withContext(Dispatchers.IO) {
        partyDao.search(query?.trim())
    }

    suspend fun updateCumulativePurchases(partyId: Long, amount: Double) = withContext(Dispatchers.IO) {
        partyDao.incrementCumulativePurchases(partyId, amount)
    }

    suspend fun updateRunningBalance(partyId: Long, deltaAmount: Double) = withContext(Dispatchers.IO) {
        partyDao.updateRunningBalance(partyId, deltaAmount)
    }
}
