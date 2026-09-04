package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.domain.managers.OrganizationContext
import java.util.UUID

enum class ApprovalType(val title: String, val description: String) {
    MOISTURE_TOLERANCE_OVERRIDE(
        title = "Moisture Tolerance Cut Override",
        description = "Waiving or adjusting dockage deduction for high moisture grain (>15.0%)"
    ),
    RATE_DISCOUNT_OVERRIDE(
        title = "Quality Rate Discount Override",
        description = "Bypassing standard quality grade price deductions (> ₹50/Qtl)"
    ),
    STOCK_WRITE_OFF(
        title = "Manual Stock Write-Off / Shrinkage",
        description = "Writing off more than 0.5 MT of silo stock due to cleaning loss or moisture evaporation"
    ),
    EOD_UNLOCK(
        title = "Day-End Statutory Re-Opening",
        description = "Unlocking a frozen day-end closing sequence to record a missed back-dated entry"
    ),
    TRANSACTION_DELETION(
        title = "Record Deletion / Reversal Override",
        description = "Deleting or reversing an accepted procurement voucher or outward gate pass"
    )
}

enum class ApprovalStatus {
    PENDING,
    APPROVED,
    REJECTED
}

@Entity(
    tableName = "approval_requests",
    indices = [
        Index(value = ["uuid"], unique = true),
        Index(value = ["status"]),
        Index(value = ["approval_type"]),
        Index(value = ["requested_at"]),
        Index(value = ["org_code", "requested_at"])
    ]
)
data class ApprovalRequestEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "uuid")
    val uuid: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "org_code")
    val orgCode: String = OrganizationContext.getCurrentOrgCode(),

    @ColumnInfo(name = "approval_type")
    val approvalType: String,

    @ColumnInfo(name = "target_reference_doc_no")
    val targetReferenceDocNo: String,

    @ColumnInfo(name = "requested_by")
    val requestedBy: String, // username / user ID

    @ColumnInfo(name = "requested_at")
    val requestedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "deviation_details")
    val deviationDetails: String,

    @ColumnInfo(name = "status")
    val status: String = ApprovalStatus.PENDING.name,

    @ColumnInfo(name = "approved_by")
    val approvedBy: String? = null,

    @ColumnInfo(name = "approved_at")
    val approvedAt: Long? = null,

    @ColumnInfo(name = "action_reason")
    val actionReason: String? = null
)
