package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.DocumentSequenceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentSequenceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sequence: DocumentSequenceEntity): Long

    @Update
    suspend fun update(sequence: DocumentSequenceEntity)

    @Query("SELECT * FROM document_sequences WHERE financial_year = :fy AND facility_id = :facilityId AND document_type = :docType LIMIT 1")
    suspend fun getSequence(fy: String, facilityId: String, docType: String): DocumentSequenceEntity?

    @Query("SELECT * FROM document_sequences WHERE financial_year = :fy ORDER BY document_type ASC")
    fun getSequencesByFyFlow(fy: String): Flow<List<DocumentSequenceEntity>>

    @Query("SELECT * FROM document_sequences ORDER BY financial_year DESC, document_type ASC")
    fun getAllSequencesFlow(): Flow<List<DocumentSequenceEntity>>

    @Query("UPDATE document_sequences SET is_locked = :isLocked, updated_at = :timestamp WHERE financial_year = :fy AND facility_id = :facilityId")
    suspend fun setFacilityLock(fy: String, facilityId: String, isLocked: Boolean, timestamp: Long = System.currentTimeMillis())

    @Transaction
    suspend fun getOrCreateSequence(fy: String, facilityId: String, docType: String): DocumentSequenceEntity {
        val existing = getSequence(fy, facilityId, docType)
        if (existing != null) {
            return existing
        }
        val initial = DocumentSequenceEntity(
            financialYear = fy,
            facilityId = facilityId,
            documentType = docType,
            currentSequence = 0L,
            lastUsedTimestamp = System.currentTimeMillis()
        )
        insert(initial)
        return getSequence(fy, facilityId, docType) ?: initial
    }

    @Transaction
    suspend fun getNextDocumentNumber(fy: String, facilityId: String, docType: String): String {
        val seq = getOrCreateSequence(fy, facilityId, docType)
        if (seq.isLocked) {
            throw IllegalStateException("Document sequence for $docType in FY $fy (Facility $facilityId) is LOCKED for Day-End Closing.")
        }
        val nextVal = seq.currentSequence + 1
        val updated = seq.copy(
            currentSequence = nextVal,
            lastUsedTimestamp = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        update(updated)
        return updated.formatDocumentNumber(nextVal)
    }
}
