package com.example.domain.usecase

import com.example.data.model.InventoryMovementEntity
import com.example.data.model.InventoryMovementType
import com.example.data.model.QuantityBasis
import com.example.data.repository.AuditTrailRepository
import com.example.data.repository.InventoryMovementRepository

/**
 * Domain Use Case to append an immutable inventory movement record and audit entry.
 */
class PostInventoryMovementUseCase(
    private val movementRepository: InventoryMovementRepository,
    private val auditTrailRepository: AuditTrailRepository
) {
    suspend operator fun invoke(
        movementType: InventoryMovementType,
        sourceEntityType: String,
        sourceEntityUuid: String,
        facilityId: String,
        batchId: String = "LOT_GEN",
        cropType: String = "MAIZE",
        quantityKg: Double,
        quantityBasis: QuantityBasis = QuantityBasis.INVENTORY,
        costPerQuintalPaise: Long = 0L,
        totalValuePaise: Long = 0L,
        userId: String = "operator",
        deviceId: String = "local_device",
        reason: String = ""
    ): Result<InventoryMovementEntity> {
        return try {
            require(quantityKg != 0.0) { "Movement quantity cannot be zero." }
            require(facilityId.isNotBlank()) { "Facility ID cannot be blank." }

            val movement = InventoryMovementEntity(
                movementType = movementType.name,
                sourceEntityType = sourceEntityType,
                sourceEntityUuid = sourceEntityUuid,
                facilityId = facilityId,
                batchId = batchId,
                cropType = cropType,
                quantityKg = quantityKg,
                quantityGrams = (quantityKg * 1000.0).toLong(),
                quantityBasis = quantityBasis.name,
                costPerQuintalPaise = costPerQuintalPaise,
                totalValuePaise = totalValuePaise,
                userId = userId,
                deviceId = deviceId,
                reason = reason
            )

            movementRepository.post(movement)

            auditTrailRepository.logCreate(
                entityType = "INVENTORY_MOVEMENT",
                entityId = movement.uuid,
                newStateJson = "{\"type\":\"${movement.movementType}\",\"facility\":\"${movement.facilityId}\",\"qtyKg\":${movement.quantityKg}}",
                userId = userId,
                deviceId = deviceId,
                reason = "Posted inventory movement: ${movement.movementType} for $cropType ($quantityKg kg)"
            )

            Result.success(movement)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
