package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.OutboundDispatchEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DispatchDao {
    @Query("SELECT * FROM outbound_dispatches ORDER BY id DESC")
    fun getAllDispatches(): Flow<List<OutboundDispatchEntity>>

    @Query("SELECT * FROM outbound_dispatches WHERE id = :id")
    suspend fun getDispatchById(id: Long): OutboundDispatchEntity?

    @Query("SELECT * FROM outbound_dispatches WHERE vehicleNumber = :truckNo LIMIT 1")
    suspend fun getDispatchByTruck(truckNo: String): OutboundDispatchEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDispatch(dispatch: OutboundDispatchEntity): Long

    @Update
    suspend fun updateDispatch(dispatch: OutboundDispatchEntity)

    @Query("DELETE FROM outbound_dispatches WHERE id = :id")
    suspend fun deleteDispatch(id: Long)

    @Query("SELECT SUM(netLoadedWeightKg) FROM outbound_dispatches")
    fun getTotalDispatchedKg(): Flow<Double?>
}
