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

    @Query("SELECT * FROM outbound_dispatches ORDER BY id ASC")
    suspend fun getAllDispatchesDirect(): List<OutboundDispatchEntity>

    @Query("SELECT * FROM outbound_dispatches WHERE id = :id LIMIT 1")
    suspend fun getDispatchById(id: Long): OutboundDispatchEntity?

    @Query("SELECT * FROM outbound_dispatches WHERE uuid = :uuid LIMIT 1")
    suspend fun getDispatchByUuid(uuid: String): OutboundDispatchEntity?

    @Query("SELECT * FROM outbound_dispatches WHERE dispatchNo = :dispatchNo LIMIT 1")
    suspend fun getDispatchByNo(dispatchNo: String): OutboundDispatchEntity?

    @Query("SELECT * FROM outbound_dispatches WHERE buyer_party_id = :partyId ORDER BY id DESC")
    suspend fun getDispatchesByBuyerParty(partyId: Long): List<OutboundDispatchEntity>

    @Query("SELECT * FROM outbound_dispatches WHERE status = :status ORDER BY id DESC")
    fun getDispatchesByStatusFlow(status: String): Flow<List<OutboundDispatchEntity>>

    @Query("SELECT * FROM outbound_dispatches WHERE vehicleNumber = :truckNo LIMIT 1")
    suspend fun getDispatchByTruck(truckNo: String): OutboundDispatchEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDispatch(dispatch: OutboundDispatchEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDispatches(dispatches: List<OutboundDispatchEntity>)

    @Update
    suspend fun updateDispatch(dispatch: OutboundDispatchEntity)

    @Deprecated("Prefer logging audit trail and using reversal transactions rather than hard deletion.")
    @Query("DELETE FROM outbound_dispatches WHERE id = :id")
    suspend fun deleteDispatch(id: Long)

    @Query("SELECT SUM(netLoadedWeightKg) FROM outbound_dispatches")
    fun getTotalDispatchedKg(): Flow<Double?>
}
