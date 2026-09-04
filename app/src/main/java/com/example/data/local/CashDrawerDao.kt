package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CashDrawerCountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CashDrawerDao {
    @Query("SELECT * FROM cash_drawer_counts ORDER BY timestamp DESC")
    fun getAllCountsFlow(): Flow<List<CashDrawerCountEntity>>

    @Query("SELECT * FROM cash_drawer_counts WHERE count_date = :date LIMIT 1")
    suspend fun getCountByDate(date: String): CashDrawerCountEntity?

    @Query("SELECT * FROM cash_drawer_counts ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestCount(): CashDrawerCountEntity?

    @Query("SELECT * FROM cash_drawer_counts WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): CashDrawerCountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(count: CashDrawerCountEntity): Long

    @Update
    suspend fun update(count: CashDrawerCountEntity)
}
