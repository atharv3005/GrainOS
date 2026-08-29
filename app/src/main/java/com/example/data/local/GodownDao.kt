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

    @Query("SELECT * FROM godowns WHERE godownId = :query OR displayName = :query OR displayName LIKE '%' || :query || '%' LIMIT 1")
    suspend fun findGodown(query: String): GodownEntity?

    @Query("SELECT * FROM godowns LIMIT 1")
    suspend fun getFirstGodown(): GodownEntity?

    @Query("SELECT * FROM godowns")
    suspend fun getAllGodownsDirect(): List<GodownEntity>

    @Query("DELETE FROM godowns")
    suspend fun deleteAllGodowns()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGodowns(godowns: List<GodownEntity>)

    @Update
    suspend fun updateGodown(godown: GodownEntity)

    @Query("UPDATE godowns SET currentStockMt = currentStockMt + :weightMt, lastUpdated = :timestamp WHERE godownId = :godownId")
    suspend fun addStock(godownId: String, weightMt: Double, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE godowns SET currentStockMt = MAX(0.0, currentStockMt - :weightMt), lastUpdated = :timestamp WHERE godownId = :godownId")
    suspend fun reduceStock(godownId: String, weightMt: Double, timestamp: Long = System.currentTimeMillis())
}
