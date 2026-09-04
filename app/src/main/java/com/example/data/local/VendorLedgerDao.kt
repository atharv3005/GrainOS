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

    @Query("SELECT * FROM vendor_ledgers ORDER BY id ASC")
    suspend fun getAllLedgersDirect(): List<VendorLedgerEntity>

    @Query("SELECT * FROM vendor_ledgers WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): VendorLedgerEntity?

    @Query("SELECT * FROM vendor_ledgers WHERE uuid = :uuid LIMIT 1")
    suspend fun getByUuid(uuid: String): VendorLedgerEntity?

    @Query("SELECT * FROM vendor_ledgers WHERE party_id = :partyId ORDER BY timestamp DESC")
    suspend fun getLedgerEntriesByParty(partyId: Long): List<VendorLedgerEntity>

    @Query("SELECT * FROM vendor_ledgers WHERE party_id = :partyId ORDER BY timestamp DESC")
    fun getLedgerEntriesByPartyFlow(partyId: Long): Flow<List<VendorLedgerEntity>>

    @Query("SELECT * FROM vendor_ledgers WHERE vendorType = :vendorType ORDER BY timestamp DESC")
    fun getLedgerEntriesByType(vendorType: String): Flow<List<VendorLedgerEntity>>

    @Query("SELECT * FROM vendor_ledgers WHERE vendorName = :vendorName ORDER BY timestamp DESC")
    fun getLedgerEntriesForVendor(vendorName: String): Flow<List<VendorLedgerEntity>>

    @Query("SELECT * FROM vendor_ledgers WHERE pdcStatus != 'NONE' ORDER BY chequeMaturityDate ASC")
    fun getAllPdcsFlow(): Flow<List<VendorLedgerEntity>>

    @Query("SELECT * FROM vendor_ledgers WHERE pdcStatus = :status ORDER BY chequeMaturityDate ASC")
    fun getPdcsByStatusFlow(status: String): Flow<List<VendorLedgerEntity>>

    @Query("SELECT * FROM vendor_ledgers WHERE pdcStatus IN ('ISSUED', 'PENDING_MATURITY', 'DEPOSITED', 'PRESENTED') ORDER BY chequeMaturityDate ASC")
    fun getActivePdcsFlow(): Flow<List<VendorLedgerEntity>>

    @Query("SELECT * FROM vendor_ledgers WHERE pdcStatus IN ('ISSUED', 'PENDING_MATURITY', 'DEPOSITED') AND chequeMaturityDate BETWEEN :startTs AND :endTs ORDER BY chequeMaturityDate ASC")
    suspend fun getPdcsDueBetween(startTs: Long, endTs: Long): List<VendorLedgerEntity>

    @Query("SELECT SUM(amount) FROM vendor_ledgers WHERE vendorName = :vendorName AND transactionType = 'BILL_CREDIT'")
    suspend fun getCumulativeFarmerPurchases(vendorName: String): Double?

    @Query("SELECT SUM(amount) FROM vendor_ledgers WHERE party_id = :partyId AND transactionType = 'BILL_CREDIT'")
    suspend fun getCumulativePartyPurchases(partyId: Long): Double?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLedgerEntry(entry: VendorLedgerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLedgerEntries(entries: List<VendorLedgerEntity>)

    @Update
    suspend fun updateLedgerEntry(entry: VendorLedgerEntity)

    @Deprecated("Prefer logging audit trail and using reversal transactions rather than hard deletion.")
    @Query("DELETE FROM vendor_ledgers WHERE id = :id")
    suspend fun deleteLedgerEntry(id: Long)
}
