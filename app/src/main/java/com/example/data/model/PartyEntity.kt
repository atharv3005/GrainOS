package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.domain.managers.OrganizationContext
import java.util.UUID

/**
 * Party types supported in GrainOS.
 */
enum class PartyType(val label: String) {
    FARMER("Farmer / शेतकरी"),
    BUYER("Buyer / व्यापारी"),
    BROKER("Broker / दलाल"),
    TRANSPORTER("Transporter / वाहतूकदार"),
    LABOUR("Labour / हमाल"),
    VENDOR("Vendor / पुरवठादार")
}

/**
 * Unified Party Master entity supporting Farmers, Corporate Buyers, Brokers, Transporters, Labour Gangs, and General Vendors.
 */
@Entity(
    tableName = "parties",
    indices = [
        Index(value = ["uuid"], unique = true),
        Index(value = ["mobile"]),
        Index(value = ["pan"]),
        Index(value = ["party_type"]),
        Index(value = ["legal_name"]),
        Index(value = ["org_code", "party_type"])
    ]
)
data class PartyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "uuid")
    val uuid: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "org_code")
    val orgCode: String = OrganizationContext.getCurrentOrgCode(),

    @ColumnInfo(name = "party_type")
    val partyType: String = PartyType.FARMER.name,

    @ColumnInfo(name = "legal_name")
    val legalName: String,

    @ColumnInfo(name = "trade_name")
    val tradeName: String? = null,

    @ColumnInfo(name = "mobile")
    val mobile: String,

    @ColumnInfo(name = "alternate_mobile")
    val alternateMobile: String? = null,

    @ColumnInfo(name = "email")
    val email: String? = null,

    @ColumnInfo(name = "village")
    val village: String = "",

    @ColumnInfo(name = "taluka")
    val taluka: String = "",

    @ColumnInfo(name = "district")
    val district: String = "",

    @ColumnInfo(name = "state")
    val state: String = "Maharashtra",

    @ColumnInfo(name = "pin_code")
    val pinCode: String = "",

    @ColumnInfo(name = "pan")
    val pan: String? = null,

    @ColumnInfo(name = "gstin")
    val gstin: String? = null,

    @ColumnInfo(name = "masked_aadhaar")
    val maskedAadhaar: String? = null,

    @ColumnInfo(name = "bank_account_name")
    val bankAccountName: String? = null,

    @ColumnInfo(name = "bank_account_number")
    val bankAccountNumber: String? = null,

    @ColumnInfo(name = "bank_ifsc")
    val bankIfsc: String? = null,

    @ColumnInfo(name = "bank_name")
    val bankName: String? = null,

    @ColumnInfo(name = "bank_branch")
    val bankBranch: String? = null,

    @ColumnInfo(name = "upi_id")
    val upiId: String? = null,

    @ColumnInfo(name = "opening_balance")
    val openingBalance: Double = 0.0,

    @ColumnInfo(name = "running_balance")
    val runningBalance: Double = 0.0,

    @ColumnInfo(name = "cumulative_purchases_in_fy")
    val cumulativePurchasesInFy: Double = 0.0,

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,

    @ColumnInfo(name = "is_pan_verified")
    val isPanVerified: Boolean = false,

    @ColumnInfo(name = "notes")
    val notes: String = "",

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

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
    val altPhone: String get() = alternateMobile ?: ""
}
