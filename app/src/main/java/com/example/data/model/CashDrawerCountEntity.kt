package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.domain.managers.OrganizationContext
import java.util.UUID

@Entity(
    tableName = "cash_drawer_counts",
    indices = [
        Index(value = ["uuid"], unique = true),
        Index(value = ["count_date"]),
        Index(value = ["timestamp"]),
        Index(value = ["org_code", "timestamp"])
    ]
)
data class CashDrawerCountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "uuid")
    val uuid: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "org_code")
    val orgCode: String = OrganizationContext.getCurrentOrgCode(),

    @ColumnInfo(name = "count_date")
    val countDate: String, // "2026-08-31"

    @ColumnInfo(name = "notes_500")
    val notes500: Int = 0,

    @ColumnInfo(name = "notes_200")
    val notes200: Int = 0,

    @ColumnInfo(name = "notes_100")
    val notes100: Int = 0,

    @ColumnInfo(name = "notes_50")
    val notes50: Int = 0,

    @ColumnInfo(name = "notes_20")
    val notes20: Int = 0,

    @ColumnInfo(name = "notes_10")
    val notes10: Int = 0,

    @ColumnInfo(name = "coins_total")
    val coinsTotal: Double = 0.0,

    @ColumnInfo(name = "physical_cash_total")
    val physicalCashTotal: Double = (notes500 * 500) + (notes200 * 200) + (notes100 * 100) + (notes50 * 50) + (notes20 * 20) + (notes10 * 10) + coinsTotal,

    @ColumnInfo(name = "system_cash_balance")
    val systemCashBalance: Double,

    @ColumnInfo(name = "variance_amount")
    val varianceAmount: Double = physicalCashTotal - systemCashBalance,

    @ColumnInfo(name = "variance_reason")
    val varianceReason: String = "",

    @ColumnInfo(name = "counted_by")
    val countedBy: String = "operator",

    @ColumnInfo(name = "supervisor_approval_pin")
    val supervisorApprovalPin: String? = null,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis()
)
