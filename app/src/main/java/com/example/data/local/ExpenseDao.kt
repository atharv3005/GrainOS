package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ExpenseEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM manual_expenses ORDER BY timestamp DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntryEntity>>

    @Query("SELECT * FROM manual_expenses ORDER BY timestamp DESC")
    suspend fun getAllExpensesDirect(): List<ExpenseEntryEntity>

    @Query("SELECT * FROM manual_expenses WHERE id = :id LIMIT 1")
    suspend fun getExpenseById(id: Long): ExpenseEntryEntity?

    @Query("SELECT * FROM manual_expenses WHERE uuid = :uuid LIMIT 1")
    suspend fun getExpenseByUuid(uuid: String): ExpenseEntryEntity?

    @Query("SELECT * FROM manual_expenses WHERE cropType = :cropType ORDER BY timestamp DESC")
    fun getExpensesForCrop(cropType: String): Flow<List<ExpenseEntryEntity>>

    @Query("SELECT * FROM manual_expenses WHERE party_id = :partyId ORDER BY timestamp DESC")
    suspend fun getExpensesByParty(partyId: Long): List<ExpenseEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenses(expenses: List<ExpenseEntryEntity>)

    @Update
    suspend fun updateExpense(expense: ExpenseEntryEntity)

    @Deprecated("Prefer logging audit trail and using reversal transactions rather than hard deletion.")
    @Query("DELETE FROM manual_expenses WHERE id = :id")
    suspend fun deleteExpense(id: Long)
}
