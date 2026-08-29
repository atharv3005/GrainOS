package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.ExpenseEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM manual_expenses ORDER BY timestamp DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntryEntity>>

    @Query("SELECT * FROM manual_expenses WHERE cropType = :cropType ORDER BY timestamp DESC")
    fun getExpensesForCrop(cropType: String): Flow<List<ExpenseEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenses(expenses: List<ExpenseEntryEntity>)

    @Query("DELETE FROM manual_expenses WHERE id = :id")
    suspend fun deleteExpense(id: Long)
}
