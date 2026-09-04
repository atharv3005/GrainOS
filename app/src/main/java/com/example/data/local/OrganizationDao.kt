package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.OrganizationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OrganizationDao {
    @Query("SELECT * FROM organizations ORDER BY id ASC")
    fun getAllOrganizationsFlow(): Flow<List<OrganizationEntity>>

    @Query("SELECT * FROM organizations WHERE is_active = 1 ORDER BY id ASC")
    suspend fun getActiveOrganizations(): List<OrganizationEntity>

    @Query("SELECT * FROM organizations WHERE org_code = :orgCode LIMIT 1")
    suspend fun getByCode(orgCode: String): OrganizationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(org: OrganizationEntity): Long

    @Update
    suspend fun update(org: OrganizationEntity)
}
