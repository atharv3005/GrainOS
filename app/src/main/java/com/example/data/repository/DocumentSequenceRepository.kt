package com.example.data.repository

import com.example.data.local.DocumentSequenceDao
import com.example.data.model.DocumentSequenceEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

open class DocumentSequenceRepository(private val sequenceDao: DocumentSequenceDao) {

    val allSequencesFlow: Flow<List<DocumentSequenceEntity>> = sequenceDao.getAllSequencesFlow()

    fun getSequencesByFyFlow(fy: String): Flow<List<DocumentSequenceEntity>> {
        return sequenceDao.getSequencesByFyFlow(fy)
    }

    suspend fun getNextNumber(fy: String, facilityId: String, docType: String): String = withContext(Dispatchers.IO) {
        sequenceDao.getNextDocumentNumber(fy, facilityId, docType)
    }

    suspend fun lockSequence(fy: String, facilityId: String) = withContext(Dispatchers.IO) {
        sequenceDao.setFacilityLock(fy, facilityId, true)
    }

    suspend fun unlockSequence(fy: String, facilityId: String) = withContext(Dispatchers.IO) {
        sequenceDao.setFacilityLock(fy, facilityId, false)
    }

    suspend fun getOrCreateSequence(fy: String, facilityId: String, docType: String): DocumentSequenceEntity = withContext(Dispatchers.IO) {
        sequenceDao.getOrCreateSequence(fy, facilityId, docType)
    }
}
