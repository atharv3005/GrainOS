package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "godowns")
data class GodownEntity(
    @PrimaryKey
    val godownId: String, // "GODOWN_A", "GODOWN_B", "GODOWN_C", "SILO_BAY_1", "SILO_BAY_2", "DRYING_YARD"
    val displayName: String,
    val capacityMt: Double,
    val currentStockMt: Double,
    val activeCrop: String = CropType.MAIZE.name,
    val averageMoisture: Double = 12.4,
    val temperatureCelsius: Double = 24.5,
    val baseCostPerQuintal: Double = 2400.0,
    val cumulativeShrinkageKg: Double = 0.0,
    val shrinkageCapitalizedCost: Double = 0.0,
    val adjustedAvgCostPerQuintal: Double = 2400.0,
    val ventilationStatus: String = "OPTIMAL",
    val lastUpdated: Long = System.currentTimeMillis()
)
