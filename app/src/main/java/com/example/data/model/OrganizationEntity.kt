package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "organizations",
    indices = [
        Index(value = ["org_code"], unique = true),
        Index(value = ["uuid"], unique = true)
    ]
)
data class OrganizationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "uuid")
    val uuid: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "org_code")
    val orgCode: String, // e.g. "DHULE_MAIN", "NANDURBAR_BRANCH"

    @ColumnInfo(name = "legal_name")
    val legalName: String,

    @ColumnInfo(name = "trade_name")
    val tradeName: String,

    @ColumnInfo(name = "apmc_license_no")
    val apmcLicenseNo: String,

    @ColumnInfo(name = "gstin")
    val gstin: String,

    @ColumnInfo(name = "pan")
    val pan: String,

    @ColumnInfo(name = "address")
    val address: String,

    @ColumnInfo(name = "state_code")
    val stateCode: String = "27",

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
