package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.domain.managers.OrganizationContext
import java.util.UUID

/**
 * Supported audit action types.
 */
enum class AuditAction(val label: String) {
    CREATE("Create / निर्मिती"),
    UPDATE("Update / बदल"),
    DELETE("Delete / हटवणे"),
    REVERSE("Reverse / उलट"),
    APPROVE("Approve / मंजुरी"),
    LOCK("Lock / कुलूप")
}

/**
 * Immutable audit trail entity recording all transactional modifications, deletions, and supervisor actions.
 */
@Entity(
    tableName = "audit_trails",
    indices = [
        Index(value = ["uuid"], unique = true),
        Index(value = ["entity_type", "entity_id"]),
        Index(value = ["action"]),
        Index(value = ["timestamp"]),
        Index(value = ["org_code", "timestamp"])
    ]
)
data class AuditTrailEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "uuid")
    val uuid: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "org_code")
    val orgCode: String = OrganizationContext.getCurrentOrgCode(),

    @ColumnInfo(name = "entity_type")
    val entityType: String, // PROCUREMENT, DISPATCH, EXPENSE, PDC, etc.

    @ColumnInfo(name = "entity_id")
    val entityId: String, // Reference token, ID, or UUID

    @ColumnInfo(name = "action")
    val action: String = AuditAction.CREATE.name,

    @ColumnInfo(name = "previous_state_json")
    val previousStateJson: String? = null,

    @ColumnInfo(name = "new_state_json")
    val newStateJson: String? = null,

    @ColumnInfo(name = "user_id")
    val userId: String = "operator",

    @ColumnInfo(name = "device_id")
    val deviceId: String = "local_device",

    @ColumnInfo(name = "reason")
    val reason: String = "",

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "sync_status")
    val syncStatus: String = SyncStatus.PENDING.name,

    @ColumnInfo(name = "synced_at")
    val syncedAt: Long? = null,

    @ColumnInfo(name = "idempotency_key")
    val idempotencyKey: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "organization_id")
    val organizationId: String = "default",

    @ColumnInfo(name = "schema_version")
    val schemaVersion: Int = 1
)
