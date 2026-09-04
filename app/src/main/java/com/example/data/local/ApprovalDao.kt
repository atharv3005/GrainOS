package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ApprovalRequestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ApprovalDao {
    @Query("SELECT * FROM approval_requests ORDER BY requested_at DESC")
    fun getAllApprovalsFlow(): Flow<List<ApprovalRequestEntity>>

    @Query("SELECT * FROM approval_requests WHERE status = 'PENDING' ORDER BY requested_at DESC")
    fun getPendingApprovalsFlow(): Flow<List<ApprovalRequestEntity>>

    @Query("SELECT * FROM approval_requests WHERE id = :id LIMIT 1")
    suspend fun getApprovalById(id: Long): ApprovalRequestEntity?

    @Query("SELECT * FROM approval_requests WHERE uuid = :uuid LIMIT 1")
    suspend fun getApprovalByUuid(uuid: String): ApprovalRequestEntity?

    @Query("SELECT * FROM approval_requests WHERE target_reference_doc_no = :docNo ORDER BY requested_at DESC LIMIT 1")
    suspend fun getLatestApprovalForDoc(docNo: String): ApprovalRequestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(request: ApprovalRequestEntity): Long

    @Update
    suspend fun update(request: ApprovalRequestEntity)
}
