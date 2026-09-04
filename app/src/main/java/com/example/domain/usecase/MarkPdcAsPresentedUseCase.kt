package com.example.domain.usecase

import com.example.data.local.VendorLedgerDao
import com.example.data.model.PdcStatus
import com.example.data.repository.AuditTrailRepository

/**
 * Domain Use Case to mark a deposited Post-Dated Cheque as presented for clearing.
 */
class MarkPdcAsPresentedUseCase(
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

            require(entry.pdcStatus == PdcStatus.DEPOSITED.name) {
                "Cannot present PDC in status ${entry.pdcStatus}. Must be DEPOSITED first."
            }

            val updated = entry.copy(
                pdcStatus = PdcStatus.PRESENTED.name,
                presentedAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            vendorLedgerDao.updateLedgerEntry(updated)

            auditTrailRepository.logUpdate(
                entityType = "PDC",
                entityId = entry.referenceDocNo.ifEmpty { ledgerUuid },
                previousStateJson = "{\"status\":\"${entry.pdcStatus}\"}",
                newStateJson = "{\"status\":\"${PdcStatus.PRESENTED.name}\",\"presentedAt\":${updated.presentedAt}}",
                userId = userId,
                deviceId = deviceId,
                reason = "Marked PDC ${entry.utrOrChequeNo} as PRESENTED for clearing"
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
