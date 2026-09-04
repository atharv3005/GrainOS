package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.domain.managers.OrganizationContext
import java.util.UUID

enum class DispatchStatus(val label: String) {
    IN_TRANSIT("In Transit (वाहतुकीत)"),
    UNLOADED("Unloaded & Weight Verified (अनलोड झाले)"),
    REJECTED("Rejected at Mill (रद्द / परत)"),
    SETTLED("Settled (हिशोब पूर्ण)")
}

@Entity(
    tableName = "outbound_dispatches",
    indices = [
        Index(value = ["uuid"], unique = true),
        Index(value = ["dispatchNo"], unique = true),
        Index(value = ["buyer_party_id"]),
        Index(value = ["status"]),
        Index(value = ["timestamp"]),
        Index(value = ["org_code", "timestamp"])
    ]
)
data class OutboundDispatchEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "uuid")
    val uuid: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "org_code")
    val orgCode: String = OrganizationContext.getCurrentOrgCode(),

    @ColumnInfo(name = "buyer_party_id")
    val buyerPartyId: Long? = null,

    @ColumnInfo(name = "trade_booking_id")
    val tradeBookingId: Long? = null,

    @ColumnInfo(name = "transporter_party_id")
    val transporterPartyId: Long? = null,

    val dispatchNo: String,
    val buyerName: String,
    val destination: String,
    val vehicleNumber: String,
    val cropType: String,
    val godownSource: String,
    val tareWeightKg: Double,
    val grossWeightKg: Double,
    val netLoadedWeightKg: Double, // Gate Net Weight
    val ratePerQuintal: Double, // Booked contract rate
    val totalInvoiceAmount: Double, // Initial invoice based on gate weight
    
    // Post-Unloading & Actual P&L settlement fields
    val companyUnloadedWeightKg: Double = 0.0, // Final Mill Unloaded Weight
    val weightShortageKg: Double = 0.0, // Gate Net - Company Unloaded
    val companyRateDeductionPenalty: Double = 0.0, // Mill quality/moisture price cut
    val brokerName: String = "",
    val brokerageRatePerQtl: Double = 0.0,
    val finalBrokerageFee: Double = 0.0,
    val loadingLaborCost: Double = 0.0,
    val freightCost: Double = 0.0,
    val bagCost: Double = 0.0,
    val miscCost: Double = 0.0,
    val fifoProcurementCost: Double = 0.0, // Realized FIFO purchase cost from godown batches
    val actualNetRevenue: Double = 0.0, // (Company Unloaded Weight / 100 * Rate) - Penalty
    val actualNetProfit: Double = 0.0, // Actual Net Revenue - (FIFO Cost + Labor + Freight + Bags + Misc + Brokerage)
    
    val status: String = DispatchStatus.IN_TRANSIT.name,
    val timestamp: Long = System.currentTimeMillis(),
    val unloadedTimestamp: Long = 0L,
    val notes: String = "",

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
)
