package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.VendorLedgerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VendorLedgerDao {
    @Query("SELECT * FROM vendor_ledgers ORDER BY timestamp DESC")
    fun getAllLedgerEntries(): Flow<List<VendorLedgerEntity>>

    @Query("SELECT * FROM vendor_ledgers WHERE vendorType = :vendorType ORDER BY timestamp DESC")
    fun getLedgerEntriesByType(vendorType: String): Flow<List<VendorLedgerEntity>>

    @Query("SELECT * FROM vendor_ledgers WHERE vendorName = :vendorName ORDER BY timestamp DESC")
    fun getLedgerEntriesForVendor(vendorName: String): Flow<List<VendorLedgerEntity>>

    @Query("SELECT * FROM vendor_ledgers WHERE pdcStatus = 'PENDING_MATURITY' ORDER BY chequeMaturityDate ASC")
    fun getPendingPdcs(): Flow<List<VendorLedgerEntity>>

    @Query("SELECT * FROM vendor_ledgers WHERE pdcStatus = 'PENDING_MATURITY' AND chequeMaturityDate <= :currentTimestamp")
    suspend fun getMaturedPdcs(currentTimestamp: Long): List<VendorLedgerEntity>

    @Query("SELECT SUM(amount) FROM vendor_ledgers WHERE vendorName = :vendorName AND transactionType = 'BILL_CREDIT'")
    suspend fun getCumulativeFarmerPurchases(vendorName: String): Double?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLedgerEntry(entry: VendorLedgerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLedgerEntries(entries: List<VendorLedgerEntity>)

    @Update
    suspend fun updateLedgerEntry(entry: VendorLedgerEntity)

    @Query("DELETE FROM vendor_ledgers WHERE id = :id")
    suspend fun deleteLedgerEntry(id: Long)
}
