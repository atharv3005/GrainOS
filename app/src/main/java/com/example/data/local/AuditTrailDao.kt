package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.AuditTrailEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AuditTrailDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(audit: AuditTrailEntity): Long

    @Query("SELECT * FROM audit_trails ORDER BY timestamp DESC")
    fun getAllAuditTrailsFlow(): Flow<List<AuditTrailEntity>>

    @Query("SELECT * FROM audit_trails ORDER BY timestamp DESC")
    suspend fun getAllAuditTrails(): List<AuditTrailEntity>

    @Query("SELECT * FROM audit_trails WHERE entity_type = :type AND entity_id = :entityId ORDER BY timestamp DESC")
    suspend fun getAuditHistory(type: String, entityId: String): List<AuditTrailEntity>

    @Query("SELECT * FROM audit_trails WHERE entity_type = :type AND entity_id = :entityId ORDER BY timestamp DESC")
    fun getAuditHistoryFlow(type: String, entityId: String): Flow<List<AuditTrailEntity>>

    @Query("SELECT * FROM audit_trails WHERE action = 'DELETE' ORDER BY timestamp DESC")
    suspend fun getAllDeletions(): List<AuditTrailEntity>

    @Query("SELECT * FROM audit_trails WHERE action = 'DELETE' ORDER BY timestamp DESC")
    fun getAllDeletionsFlow(): Flow<List<AuditTrailEntity>>

    @Query("""
        SELECT * FROM audit_trails 
        WHERE (:action IS NULL OR action = :action) 
        AND (:entityType IS NULL OR entity_type = :entityType)
        ORDER BY timestamp DESC
    """)
    suspend fun filterAudit(action: String?, entityType: String?): List<AuditTrailEntity>
}
