package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.IoTTelemetryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IoTTelemetryDao {
    @Query("SELECT * FROM iot_telemetry ORDER BY id DESC LIMIT 50")
    fun getRecentTelemetry(): Flow<List<IoTTelemetryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTelemetry(telemetry: IoTTelemetryEntity): Long

    @Query("DELETE FROM iot_telemetry WHERE id NOT IN (SELECT id FROM iot_telemetry ORDER BY id DESC LIMIT 100)")
    suspend fun trimOldTelemetry()
}
