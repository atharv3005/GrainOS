package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.GodownEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GodownDao {
    @Query("SELECT * FROM godowns ORDER BY godownId ASC")
    fun getAllGodowns(): Flow<List<GodownEntity>>

    @Query("SELECT * FROM godowns WHERE godownId = :id")
    suspend fun getGodownById(id: String): GodownEntity?

    @Query("SELECT * FROM godowns WHERE uuid = :uuid LIMIT 1")
    suspend fun getGodownByUuid(uuid: String): GodownEntity?

    @Query("SELECT * FROM godowns WHERE godownId = :query OR displayName = :query OR displayName LIKE '%' || :query || '%' LIMIT 1")
    suspend fun findGodown(query: String): GodownEntity?

    @Query("SELECT * FROM godowns LIMIT 1")
    suspend fun getFirstGodown(): GodownEntity?

    @Query("SELECT * FROM godowns")
    suspend fun getAllGodownsDirect(): List<GodownEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGodown(godown: GodownEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGodowns(godowns: List<GodownEntity>)

    @Update
    suspend fun updateGodown(godown: GodownEntity)

    @Query("UPDATE godowns SET currentStockMt = currentStockMt + :weightMt, lastUpdated = :timestamp, updated_at = :timestamp WHERE godownId = :godownId")
    suspend fun addStock(godownId: String, weightMt: Double, timestamp: Long = System.currentTimeMillis())

    /**
     * Atomically reduces godown stock only if sufficient stock exists.
     * Returns the number of rows updated (1 if successful, 0 if insufficient stock).
     * NEVER silently clamps to 0.0.
     */
    @Query("""
        UPDATE godowns 
        SET currentStockMt = currentStockMt - :weightMt, 
            lastUpdated = :timestamp,
            updated_at = :timestamp 
        WHERE godownId = :godownId 
        AND currentStockMt >= :weightMt
    """)
    suspend fun reduceStock(godownId: String, weightMt: Double, timestamp: Long = System.currentTimeMillis()): Int
}
