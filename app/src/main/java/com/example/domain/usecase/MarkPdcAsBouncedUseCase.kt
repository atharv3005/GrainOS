package com.example.domain.usecase

import com.example.data.local.ProcurementDao
import com.example.data.local.VendorLedgerDao
import com.example.data.model.PaymentStatus
import com.example.data.model.PdcStatus
import com.example.data.repository.AuditTrailRepository

/**
 * Domain Use Case to handle a bounced / dishonored Post-Dated Cheque.
 * Critical Invariant: Bouncing a PDC must automatically reopen the original payable debt to UNPAID.
 */
class MarkPdcAsBouncedUseCase(
    private val vendorLedgerDao: VendorLedgerDao,
    private val procurementDao: ProcurementDao,
    private val auditTrailRepository: AuditTrailRepository
) {
    suspend operator fun invoke(
        ledgerUuid: String,
        bounceReason: String,
        userId: String = "operator",
        deviceId: String = "local_device"
    ): Result<Unit> {
        return try {
            require(bounceReason.isNotBlank()) { "Bounce reason must be provided for audit compliance." }

            val entry = vendorLedgerDao.getByUuid(ledgerUuid)
                ?: throw IllegalArgumentException("Ledger entry $ledgerUuid not found.")

            require(entry.pdcStatus == PdcStatus.PRESENTED.name || entry.pdcStatus == PdcStatus.DEPOSITED.name || entry.pdcStatus == PdcStatus.ISSUED.name) {
                "Cannot mark PDC in status ${entry.pdcStatus} as bounced."
            }

            var reopenedUuid: String? = null

            // 1. Reopen original Procurement Payable if linked
            if (entry.referenceDocNo.isNotBlank()) {
                val procurement = procurementDao.getProcurementByToken(entry.referenceDocNo)
                procurement?.let {
                    reopenedUuid = it.uuid
                    procurementDao.updateProcurement(
                        it.copy(
                            paymentStatus = PaymentStatus.UNPAID.name,
                            pdcCleared = false,
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                }
            }

            // 2. Update Vendor Ledger to BOUNCED and restore running liability balance
            val updated = entry.copy(
                pdcStatus = PdcStatus.BOUNCED.name,
                bouncedAt = System.currentTimeMillis(),
                bounceReason = bounceReason,
                reopenedPayableId = reopenedUuid,
                runningBalance = entry.amount, // Restore debt liability
                notes = "${entry.notes} • BOUNCED: $bounceReason",
                updatedAt = System.currentTimeMillis()
            )
            vendorLedgerDao.updateLedgerEntry(updated)

            // 3. Log Audit Trail
            auditTrailRepository.logUpdate(
                entityType = "PDC",
                entityId = entry.referenceDocNo.ifEmpty { ledgerUuid },
                previousStateJson = "{\"status\":\"${entry.pdcStatus}\"}",
                newStateJson = "{\"status\":\"${PdcStatus.BOUNCED.name}\",\"reason\":\"$bounceReason\",\"reopenedPayable\":\"$reopenedUuid\"}",
                userId = userId,
                deviceId = deviceId,
                reason = "PDC ${entry.utrOrChequeNo} BOUNCED: $bounceReason. Reopened farmer payable balance."
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
