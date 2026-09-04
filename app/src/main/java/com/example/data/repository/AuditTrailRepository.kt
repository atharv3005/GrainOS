package com.example.data.repository

import com.example.data.local.AuditTrailDao
import com.example.data.model.AuditAction
import com.example.data.model.AuditTrailEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

open class AuditTrailRepository(private val auditTrailDao: AuditTrailDao) {

    val allAuditTrailsFlow: Flow<List<AuditTrailEntity>> = auditTrailDao.getAllAuditTrailsFlow()
    val allDeletionsFlow: Flow<List<AuditTrailEntity>> = auditTrailDao.getAllDeletionsFlow()

    suspend fun logCreate(
        entityType: String,
        entityId: String,
        newStateJson: String,
        userId: String = "operator",
        deviceId: String = "local_device",
        reason: String = "Created record"
    ): Long = withContext(Dispatchers.IO) {
        val entry = AuditTrailEntity(
            entityType = entityType,
            entityId = entityId,
            action = AuditAction.CREATE.name,
            previousStateJson = null,
            newStateJson = newStateJson,
            userId = userId,
            deviceId = deviceId,
            reason = reason
        )
        auditTrailDao.insert(entry)
    }

    suspend fun logUpdate(
        entityType: String,
        entityId: String,
        previousStateJson: String,
        newStateJson: String,
        userId: String = "operator",
        deviceId: String = "local_device",
        reason: String
    ): Long = withContext(Dispatchers.IO) {
        val entry = AuditTrailEntity(
            entityType = entityType,
            entityId = entityId,
            action = AuditAction.UPDATE.name,
            previousStateJson = previousStateJson,
            newStateJson = newStateJson,
            userId = userId,
            deviceId = deviceId,
            reason = reason
        )
        auditTrailDao.insert(entry)
    }

    suspend fun logDelete(
        entityType: String,
        entityId: String,
        previousStateJson: String,
        userId: String = "operator",
        deviceId: String = "local_device",
        reason: String
    ): Long = withContext(Dispatchers.IO) {
        val entry = AuditTrailEntity(
            entityType = entityType,
            entityId = entityId,
            action = AuditAction.DELETE.name,
            previousStateJson = previousStateJson,
            newStateJson = null,
            userId = userId,
            deviceId = deviceId,
            reason = reason
        )
        auditTrailDao.insert(entry)
    }

    suspend fun getAuditHistory(entityType: String, entityId: String): List<AuditTrailEntity> = withContext(Dispatchers.IO) {
        auditTrailDao.getAuditHistory(entityType, entityId)
    }

    suspend fun getAllDeletions(): List<AuditTrailEntity> = withContext(Dispatchers.IO) {
        auditTrailDao.getAllDeletions()
    }

    suspend fun logAction(
        entityType: String,
        entityId: String,
        action: String,
        reason: String,
        previousStateJson: String? = null,
        newStateJson: String? = null,
        userId: String = "operator",
        deviceId: String = "local_device"
    ): Long = withContext(Dispatchers.IO) {
        val entry = AuditTrailEntity(
            entityType = entityType,
            entityId = entityId,
            action = action,
            previousStateJson = previousStateJson,
            newStateJson = newStateJson,
            userId = userId,
            deviceId = deviceId,
            reason = reason
        )
        auditTrailDao.insert(entry)
    }
}
