package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "iot_telemetry")
data class IoTTelemetryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val deviceType: String, // "WEIGHBRIDGE", "MOISTURE_METER", "BARRIER_GATE", "SILO_SENSOR"
    val deviceId: String,
    val readingValue: Double,
    val unit: String,
    val status: String,
    val rawPayloadJson: String,
    val latencyMs: Long = 2,
    val timestamp: Long = System.currentTimeMillis()
)
