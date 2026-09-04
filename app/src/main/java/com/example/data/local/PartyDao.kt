package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.PartyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PartyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(party: PartyEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(parties: List<PartyEntity>)

    @Update
    suspend fun update(party: PartyEntity)

    @Query("SELECT * FROM parties WHERE is_active = 1 ORDER BY legal_name ASC")
    fun getAllPartiesFlow(): Flow<List<PartyEntity>>

    @Query("SELECT * FROM parties WHERE is_active = 1 ORDER BY legal_name ASC")
    suspend fun getAllParties(): List<PartyEntity>

    @Query("SELECT * FROM parties WHERE party_type = :type AND is_active = 1 ORDER BY legal_name ASC")
    suspend fun getByType(type: String): List<PartyEntity>

    @Query("SELECT * FROM parties WHERE party_type = :type AND is_active = 1 ORDER BY legal_name ASC")
    fun getByTypeFlow(type: String): Flow<List<PartyEntity>>

    @Query("SELECT * FROM parties WHERE mobile = :mobile LIMIT 1")
    suspend fun getByMobile(mobile: String): PartyEntity?

    @Query("SELECT * FROM parties WHERE pan = :pan LIMIT 1")
    suspend fun getByPan(pan: String): PartyEntity?

    @Query("SELECT * FROM parties WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): PartyEntity?

    @Query("SELECT * FROM parties WHERE uuid = :uuid LIMIT 1")
    suspend fun getByUuid(uuid: String): PartyEntity?

    @Query("""
        SELECT * FROM parties 
        WHERE is_active = 1 
        AND (:searchQuery IS NULL OR legal_name LIKE '%' || :searchQuery || '%' OR mobile LIKE '%' || :searchQuery || '%' OR village LIKE '%' || :searchQuery || '%')
        ORDER BY legal_name ASC
    """)
    suspend fun search(searchQuery: String?): List<PartyEntity>

    @Query("UPDATE parties SET cumulative_purchases_in_fy = cumulative_purchases_in_fy + :amount, updated_at = :timestamp WHERE id = :partyId")
    suspend fun incrementCumulativePurchases(partyId: Long, amount: Double, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE parties SET running_balance = running_balance + :deltaAmount, updated_at = :timestamp WHERE id = :partyId")
    suspend fun updateRunningBalance(partyId: Long, deltaAmount: Double, timestamp: Long = System.currentTimeMillis())
}
