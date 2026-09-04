package com.example.security

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.domain.managers.OrganizationContext
import java.util.UUID

/**
 * Role-Based Access Control (RBAC) user roles in GrainOS.
 */
enum class UserRole(val displayName: String, val description: String) {
    OWNER(
        displayName = "Proprietor / Owner (मालक)",
        description = "Full administrative access: rate approvals, EOD locks, financial payouts, user management"
    ),
    OPERATOR(
        displayName = "Weighbridge / Gate Operator (वजन ऑपरेटर)",
        description = "Gate registration, gross/tare weighment, moisture testing, storage intake"
    ),
    ACCOUNTANT(
        displayName = "Accountant / Munim (मुनीम / सीए)",
        description = "Payment vouchers, PDC clearing, CA tax reports, P&L statements, journal vouchers"
    ),
    VIEWER(
        displayName = "Auditor / Viewer (तपासणीस)",
        description = "Read-only access to dashboards, audit logs, and stock telemetry"
    )
}

/**
 * System user entity for authentication and permission enforcement.
 */
@Entity(
    tableName = "users",
    indices = [
        Index(value = ["uuid"], unique = true),
        Index(value = ["username"], unique = true),
        Index(value = ["org_code"])
    ]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "uuid")
    val uuid: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "org_code")
    val orgCode: String = OrganizationContext.getCurrentOrgCode(),

    @ColumnInfo(name = "username")
    val username: String,

    @ColumnInfo(name = "full_name")
    val fullName: String,

    @ColumnInfo(name = "pin_hash")
    val pinHash: String, // PBKDF2 / Salted Hash

    @ColumnInfo(name = "pin_salt")
    val pinSalt: String = "",

    @ColumnInfo(name = "role")
    val role: String = UserRole.OPERATOR.name,

    @ColumnInfo(name = "mobile")
    val mobile: String = "",

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "last_login_at")
    val lastLoginAt: Long? = null
)
