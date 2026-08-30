package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.StorageFacilityIntakeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StorageIntakeDao {
    @Query("SELECT * FROM storage_facility_intakes ORDER BY intakeTimestamp DESC")
    fun getAllIntakes(): Flow<List<StorageFacilityIntakeEntity>>

    @Query("SELECT * FROM storage_facility_intakes WHERE storageFacilityId = :facilityId OR storageFacilityName = :facilityId OR storageFacilityName LIKE '%' || :facilityId || '%' ORDER BY intakeTimestamp DESC")
    fun getIntakesForFacility(facilityId: String): Flow<List<StorageFacilityIntakeEntity>>

    @Query("SELECT * FROM storage_facility_intakes WHERE storageFacilityId = :facilityId OR storageFacilityName = :facilityId ORDER BY intakeTimestamp DESC")
    suspend fun getIntakesForFacilityDirect(facilityId: String): List<StorageFacilityIntakeEntity>

    @Query("SELECT * FROM storage_facility_intakes WHERE tokenNo = :tokenNo LIMIT 1")
    suspend fun getIntakeByToken(tokenNo: String): StorageFacilityIntakeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIntake(intake: StorageFacilityIntakeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIntakes(intakes: List<StorageFacilityIntakeEntity>)

    @Query("DELETE FROM storage_facility_intakes WHERE id = :id")
    suspend fun deleteIntake(id: Long)

    @Query("DELETE FROM storage_facility_intakes")
    suspend fun deleteAllIntakes()

    @Query("SELECT SUM(netWeightKg) FROM storage_facility_intakes WHERE storageFacilityId = :facilityId")
    suspend fun getTotalNetWeightKgForFacility(facilityId: String): Double?

    @Query("SELECT AVG(moisturePercentage) FROM storage_facility_intakes WHERE storageFacilityId = :facilityId")
    suspend fun getAvgMoistureForFacility(facilityId: String): Double?
}
