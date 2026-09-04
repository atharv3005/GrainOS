package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.GeneralLedgerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GeneralLedgerDao {
    @Query("SELECT * FROM general_ledger ORDER BY timestamp DESC")
    fun getAllEntriesFlow(): Flow<List<GeneralLedgerEntity>>

    @Query("SELECT * FROM general_ledger WHERE voucher_no = :voucherNo ORDER BY id ASC")
    suspend fun getVoucherEntries(voucherNo: String): List<GeneralLedgerEntity>

    @Query("SELECT * FROM general_ledger WHERE account_code = :accountCode ORDER BY timestamp DESC")
    fun getEntriesByAccountFlow(accountCode: String): Flow<List<GeneralLedgerEntity>>

    @Query("SELECT * FROM general_ledger WHERE party_id = :partyId ORDER BY timestamp DESC")
    fun getEntriesByPartyFlow(partyId: Long): Flow<List<GeneralLedgerEntity>>

    @Query("SELECT SUM(debit_amount) - SUM(credit_amount) FROM general_ledger WHERE account_code = :accountCode")
    suspend fun getAccountNetDebitBalance(accountCode: String): Double?

    @Query("SELECT SUM(debit_amount) FROM general_ledger")
    suspend fun getTotalDebits(): Double?

    @Query("SELECT SUM(credit_amount) FROM general_ledger")
    suspend fun getTotalCredits(): Double?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: GeneralLedgerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<GeneralLedgerEntity>)
}
