package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.domain.managers.OrganizationContext
import java.util.UUID

@Entity(
    tableName = "trade_bookings",
    indices = [
        Index(value = ["uuid"], unique = true),
        Index(value = ["tradeNo"], unique = true),
        Index(value = ["buyer_party_id"]),
        Index(value = ["broker_party_id"]),
        Index(value = ["tradeStatus"]),
        Index(value = ["tradeTimestamp"]),
        Index(value = ["org_code", "tradeTimestamp"])
    ]
)
data class TradeBookingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "uuid")
    val uuid: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "org_code")
    val orgCode: String = OrganizationContext.getCurrentOrgCode(),

    @ColumnInfo(name = "buyer_party_id")
    val buyerPartyId: Long? = null,

    @ColumnInfo(name = "broker_party_id")
    val brokerPartyId: Long? = null,

    val tradeNo: String,
    val cropType: String,
    val brokerOrBuyerName: String,
    val quantityTons: Double,
    val bookedPricePerQuintal: Double,      // Locked-in broker selling price in ₹/qtl
    val farmerPurchasePricePerQuintal: Double, // Procurement cost given to farmer in ₹/qtl
    val laborPerQuintal: Double = 18.0,
    val bagCostPerQuintal: Double = 25.0,
    val transportPerQuintal: Double = 35.0,
    val brokeragePerQuintal: Double = 12.0,
    val tradeStatus: String = "ACTIVE",     // ACTIVE, EXECUTED, SETTLED, UNLOADED_AT_COMPANY
    val tradeTimestamp: Long = System.currentTimeMillis(),
    val notes: String = "",
    val finalCompanyUnloadedWeightKg: Double = 0.0,
    val gateWeightKg: Double = 0.0,
    val actualLoggedExpenses: Double = 0.0,

    @ColumnInfo(name = "sync_status")
    val syncStatus: String = SyncStatus.PENDING.name,

    @ColumnInfo(name = "synced_at")
    val syncedAt: Long? = null,

    @ColumnInfo(name = "idempotency_key")
    val idempotencyKey: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "device_id")
    val deviceId: String = "local_device",

    @ColumnInfo(name = "organization_id")
    val organizationId: String = "default",

    @ColumnInfo(name = "schema_version")
    val schemaVersion: Int = 1,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
) {
    // 1 Metric Ton = 10 Quintals
    val totalQuintals: Double get() = quantityTons * 10.0
    val finalCompanyUnloadedQuintals: Double get() = if (finalCompanyUnloadedWeightKg > 0) finalCompanyUnloadedWeightKg / 100.0 else totalQuintals
    val gateWeightQuintals: Double get() = if (gateWeightKg > 0) gateWeightKg / 100.0 else totalQuintals

    // Financial Calculation Engine
    val totalRevenue: Double get() = bookedPricePerQuintal * finalCompanyUnloadedQuintals
    val totalProcurementCost: Double get() = farmerPurchasePricePerQuintal * gateWeightQuintals
    val overheadPerQuintal: Double get() = laborPerQuintal + bagCostPerQuintal + transportPerQuintal + brokeragePerQuintal
    val totalOverhead: Double get() = (overheadPerQuintal * gateWeightQuintals) + actualLoggedExpenses
    val netProfit: Double get() = totalRevenue - (totalProcurementCost + totalOverhead)
    val netMarginPerQuintal: Double get() = if (finalCompanyUnloadedQuintals > 0) netProfit / finalCompanyUnloadedQuintals else 0.0

    val roiPercentage: Double get() {
        val totalCost = totalProcurementCost + totalOverhead
        return if (totalCost > 0) (netProfit / totalCost) * 100.0 else 0.0
    }
}
