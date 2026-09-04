package com.example.domain.usecase

import com.example.data.local.VendorLedgerDao
import com.example.data.model.PdcStatus
import com.example.data.repository.AuditTrailRepository

/**
 * Domain Use Case to mark an issued Post-Dated Cheque as deposited into the bank.
 */
class MarkPdcAsDepositedUseCase(
    private val vendorLedgerDao: VendorLedgerDao,
    private val auditTrailRepository: AuditTrailRepository
) {
    suspend operator fun invoke(
        ledgerUuid: String,
        userId: String = "operator",
        deviceId: String = "local_device"
    ): Result<Unit> {
        return try {
            val entry = vendorLedgerDao.getByUuid(ledgerUuid)
                ?: throw IllegalArgumentException("Ledger entry $ledgerUuid not found.")

            require(entry.pdcStatus == PdcStatus.ISSUED.name || entry.pdcStatus == PdcStatus.PENDING_MATURITY.name) {
                "Cannot deposit PDC in status ${entry.pdcStatus}. Must be ISSUED."
            }

            val updated = entry.copy(
                pdcStatus = PdcStatus.DEPOSITED.name,
                depositedAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            vendorLedgerDao.updateLedgerEntry(updated)

            auditTrailRepository.logUpdate(
                entityType = "PDC",
                entityId = entry.referenceDocNo.ifEmpty { ledgerUuid },
                previousStateJson = "{\"status\":\"${entry.pdcStatus}\"}",
                newStateJson = "{\"status\":\"${PdcStatus.DEPOSITED.name}\",\"depositedAt\":${updated.depositedAt}}",
                userId = userId,
                deviceId = deviceId,
                reason = "Marked PDC ${entry.utrOrChequeNo} as DEPOSITED in bank"
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
