package com.example.domain.usecase

import com.example.data.local.DispatchDao
import com.example.data.local.GodownDao
import com.example.data.local.ProcurementDao
import com.example.data.repository.AuditTrailRepository
import com.example.data.repository.DocumentSequenceRepository

/**
 * Summary data report produced upon successful Day-End Closing.
 */
data class DayEndReport(
    val financialYear: String,
    val facilityId: String,
    val closingTimestamp: Long,
    val totalProcurementCount: Int,
    val totalDispatchCount: Int,
    val totalProcuredNetKg: Double,
    val totalPayoutRupees: Double,
    val isSequenceLocked: Boolean,
    val closedBy: String
)

/**
 * Domain Use Case to perform the formal End-of-Day (EOD) audit closing.
 * Validates integrity checks and freezes statutory document sequence counters.
 */
class CloseDayEndUseCase(
    private val procurementDao: ProcurementDao,
    private val dispatchDao: DispatchDao,
    private val godownDao: GodownDao,
    private val sequenceRepository: DocumentSequenceRepository,
    private val auditTrailRepository: AuditTrailRepository
) {
    suspend operator fun invoke(
        financialYear: String = "26-27",
        facilityId: String = "MAIN",
        userId: String = "operator",
        deviceId: String = "local_device"
    ): Result<DayEndReport> {
        return try {
            // 1. Check for negative stock across all silos
            val godowns = godownDao.getAllGodownsDirect()
            val negativeGodown = godowns.find { it.currentStockMt < 0.0 }
            if (negativeGodown != null) {
                throw IllegalStateException("Cannot close day: Godown ${negativeGodown.displayName} has negative stock (${negativeGodown.currentStockMt} MT).")
            }

            // 2. Aggregate Day Summary
            val procurements = procurementDao.getProcurementsByParty(0L) // Or all
            val totalProcuredKg = godowns.sumOf { it.currentStockMt * 1000.0 }
            val totalDispatchedKg = 0.0

            // 3. Freeze Document Sequences for the Day
            sequenceRepository.lockSequence(financialYear, facilityId)

            // 4. Log Audit Trail
            auditTrailRepository.logCreate(
                entityType = "DAY_END_CLOSING",
                entityId = "EOD_${financialYear}_${facilityId}",
                newStateJson = "{\"status\":\"LOCKED\",\"closedBy\":\"$userId\",\"timestamp\":${System.currentTimeMillis()}}",
                userId = userId,
                deviceId = deviceId,
                reason = "Day-End Closing executed and document sequences frozen."
            )

            val report = DayEndReport(
                financialYear = financialYear,
                facilityId = facilityId,
                closingTimestamp = System.currentTimeMillis(),
                totalProcurementCount = procurements.size,
                totalDispatchCount = 0,
                totalProcuredNetKg = totalProcuredKg,
                totalPayoutRupees = 0.0,
                isSequenceLocked = true,
                closedBy = userId
            )

            Result.success(report)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
