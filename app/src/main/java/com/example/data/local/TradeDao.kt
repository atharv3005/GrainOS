package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.TradeBookingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TradeDao {
    @Query("SELECT * FROM trade_bookings ORDER BY tradeTimestamp DESC")
    fun getAllTrades(): Flow<List<TradeBookingEntity>>

    @Query("SELECT * FROM trade_bookings WHERE tradeStatus = 'ACTIVE' ORDER BY tradeTimestamp DESC")
    fun getActiveTrades(): Flow<List<TradeBookingEntity>>

    @Query("SELECT * FROM trade_bookings WHERE tradeStatus IN ('SETTLED', 'UNLOADED_AT_COMPANY') ORDER BY tradeTimestamp DESC")
    fun getSettledTrades(): Flow<List<TradeBookingEntity>>

    @Query("SELECT * FROM trade_bookings WHERE id = :id LIMIT 1")
    suspend fun getTradeById(id: Long): TradeBookingEntity?

    @Query("SELECT * FROM trade_bookings WHERE uuid = :uuid LIMIT 1")
    suspend fun getTradeByUuid(uuid: String): TradeBookingEntity?

    @Query("SELECT * FROM trade_bookings WHERE tradeNo = :tradeNo LIMIT 1")
    suspend fun getTradeByNo(tradeNo: String): TradeBookingEntity?

    @Query("SELECT * FROM trade_bookings WHERE buyer_party_id = :partyId ORDER BY tradeTimestamp DESC")
    suspend fun getTradesByBuyerParty(partyId: Long): List<TradeBookingEntity>

    @Query("SELECT * FROM trade_bookings WHERE cropType = :cropType ORDER BY tradeTimestamp DESC")
    fun getTradesByCrop(cropType: String): Flow<List<TradeBookingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrade(trade: TradeBookingEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrades(trades: List<TradeBookingEntity>)

    @Update
    suspend fun updateTrade(trade: TradeBookingEntity)

    @Deprecated("Prefer logging audit trail and using reversal transactions rather than hard deletion.")
    @Query("DELETE FROM trade_bookings WHERE id = :id")
    suspend fun deleteTradeById(id: Long)

    @Query("SELECT COUNT(*) FROM trade_bookings")
    suspend fun getTradeCount(): Int
}
