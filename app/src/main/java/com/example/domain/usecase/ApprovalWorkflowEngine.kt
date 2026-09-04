package com.example.domain.usecase

import com.example.data.local.ApprovalDao
import com.example.data.local.UserDao
import com.example.data.model.ApprovalRequestEntity
import com.example.data.model.ApprovalStatus
import com.example.data.model.ApprovalType
import com.example.security.RbacManager
import com.example.security.UserRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Enterprise Approval Workflow Engine.
 * Evaluates whether procurement parameters, write-offs, or reversals exceed operator thresholds,
 * and validates Owner PIN approval with salted PBKDF2 hashing.
 */
class ApprovalWorkflowEngine(
    private val approvalDao: ApprovalDao,
    private val userDao: UserDao
) {

    data class ThresholdCheckResult(
        val requiresApproval: Boolean,
        val approvalType: ApprovalType?,
        val reason: String
    )

    /**
     * Checks whether incoming procurement moisture or rate cut triggers mandatory supervisor approval.
     */
    fun evaluateProcurementThresholds(
        moisturePercentage: Double,
        rateCutPerQuintal: Double,
        baseRatePerQuintal: Double
    ): ThresholdCheckResult {
        if (moisturePercentage > 15.5) {
            return ThresholdCheckResult(
                requiresApproval = true,
                approvalType = ApprovalType.MOISTURE_TOLERANCE_OVERRIDE,
                reason = "Moisture ${"%.1f".format(moisturePercentage)}% exceeds standard maximum threshold of 15.0%."
            )
        }
        if (rateCutPerQuintal > 50.0) {
            return ThresholdCheckResult(
                requiresApproval = true,
                approvalType = ApprovalType.RATE_DISCOUNT_OVERRIDE,
                reason = "Quality rate cut of ₹${"%.0f".format(rateCutPerQuintal)}/Qtl exceeds maximum operator limit of ₹50/Qtl."
            )
        }
        return ThresholdCheckResult(
            requiresApproval = false,
            approvalType = null,
            reason = "Within standard operational parameters."
        )
    }

    /**
     * Creates a new pending approval request.
     */
    suspend fun createApprovalRequest(
        approvalType: ApprovalType,
        targetDocNo: String,
        requestedBy: String,
        reason: String
    ): Result<ApprovalRequestEntity> = withContext(Dispatchers.IO) {
        try {
            val request = ApprovalRequestEntity(
                approvalType = approvalType.name,
                targetReferenceDocNo = targetDocNo,
                requestedBy = requestedBy,
                deviationDetails = reason
            )
            val id = approvalDao.insert(request)
            Result.success(request.copy(id = id))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Resolves an approval request with Owner PIN authentication.
     */
    suspend fun approveWithPin(
        requestId: Long,
        approverUsername: String,
        enteredPin: String
    ): Result<ApprovalRequestEntity> = withContext(Dispatchers.IO) {
        try {
            val user = userDao.getUserByUsername(approverUsername)
                ?: return@withContext Result.failure(IllegalArgumentException("User '$approverUsername' not found."))

            if (user.role != UserRole.OWNER.name) {
                return@withContext Result.failure(IllegalAccessException("Only the Owner role can authorize threshold overrides."))
            }

            if (!RbacManager.verifyPin(enteredPin, user.pinHash, user.pinSalt)) {
                return@withContext Result.failure(IllegalArgumentException("Invalid Owner PIN entered."))
            }

            val request = approvalDao.getApprovalById(requestId)
                ?: return@withContext Result.failure(IllegalArgumentException("Approval request $requestId not found."))

            val updated = request.copy(
                status = ApprovalStatus.APPROVED.name,
                approvedBy = user.username,
                approvedAt = System.currentTimeMillis()
            )
            approvalDao.update(updated)
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
