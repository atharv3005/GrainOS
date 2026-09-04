package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.TruckRejectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TruckRejectionDao {
    @Query("SELECT * FROM truck_rejections ORDER BY timestamp DESC")
    fun getAllRejections(): Flow<List<TruckRejectionEntity>>

    @Query("SELECT * FROM truck_rejections WHERE id = :id LIMIT 1")
    suspend fun getRejectionById(id: Long): TruckRejectionEntity?

    @Query("SELECT * FROM truck_rejections WHERE uuid = :uuid LIMIT 1")
    suspend fun getRejectionByUuid(uuid: String): TruckRejectionEntity?

    @Query("SELECT * FROM truck_rejections WHERE cropType = :cropType ORDER BY timestamp DESC")
    fun getRejectionsForCrop(cropType: String): Flow<List<TruckRejectionEntity>>

    @Query("SELECT SUM(totalRejectionLoss) FROM truck_rejections")
    fun getTotalRejectionLoss(): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRejection(rejection: TruckRejectionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRejections(rejections: List<TruckRejectionEntity>)

    @Update
    suspend fun updateRejection(rejection: TruckRejectionEntity)

    @Deprecated("Prefer logging audit trail and using reversal transactions rather than hard deletion.")
    @Query("DELETE FROM truck_rejections WHERE id = :id")
    suspend fun deleteRejection(id: Long)
}
