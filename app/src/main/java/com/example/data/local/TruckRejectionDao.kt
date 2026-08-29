package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.TruckRejectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TruckRejectionDao {
    @Query("SELECT * FROM truck_rejections ORDER BY timestamp DESC")
    fun getAllRejections(): Flow<List<TruckRejectionEntity>>

    @Query("SELECT * FROM truck_rejections WHERE cropType = :cropType ORDER BY timestamp DESC")
    fun getRejectionsForCrop(cropType: String): Flow<List<TruckRejectionEntity>>

    @Query("SELECT SUM(totalRejectionLoss) FROM truck_rejections")
    fun getTotalRejectionLoss(): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRejection(rejection: TruckRejectionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRejections(rejections: List<TruckRejectionEntity>)

    @Query("DELETE FROM truck_rejections WHERE id = :id")
    suspend fun deleteRejection(id: Long)
}
