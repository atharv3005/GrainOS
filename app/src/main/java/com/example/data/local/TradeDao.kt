package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.TradeBookingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TradeDao {
    @Query("SELECT * FROM trade_bookings WHERE tradeStatus = 'UNLOADED_AT_COMPANY' ORDER BY tradeTimestamp DESC")
    fun getAllTrades(): Flow<List<TradeBookingEntity>>

    @Query("SELECT * FROM trade_bookings WHERE id = :id")
    suspend fun getTradeById(id: Long): TradeBookingEntity?

    @Query("SELECT * FROM trade_bookings WHERE cropType = :cropType AND tradeStatus = 'UNLOADED_AT_COMPANY' ORDER BY tradeTimestamp DESC")
    fun getTradesByCrop(cropType: String): Flow<List<TradeBookingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrade(trade: TradeBookingEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrades(trades: List<TradeBookingEntity>)

    @Update
    suspend fun updateTrade(trade: TradeBookingEntity)

    @Delete
    suspend fun deleteTrade(trade: TradeBookingEntity)

    @Query("DELETE FROM trade_bookings WHERE id = :id")
    suspend fun deleteTradeById(id: Long)

    @Query("SELECT COUNT(*) FROM trade_bookings")
    suspend fun getTradeCount(): Int
}
