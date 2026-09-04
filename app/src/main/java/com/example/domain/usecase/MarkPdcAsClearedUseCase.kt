package com.example.domain.usecase

import com.example.data.local.ProcurementDao
import com.example.data.local.VendorLedgerDao
import com.example.data.model.PaymentStatus
import com.example.data.model.PdcStatus
import com.example.data.repository.AuditTrailRepository

/**
 * Domain Use Case to mark a presented PDC as officially cleared by the bank,
 * updating the procurement payment status to PAID and zeroing out running liability.
 */
class MarkPdcAsClearedUseCase(
    private val vendorLedgerDao: VendorLedgerDao,
    private val procurementDao: ProcurementDao,
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

            require(entry.pdcStatus == PdcStatus.PRESENTED.name || entry.pdcStatus == PdcStatus.DEPOSITED.name) {
                "Cannot clear PDC in status ${entry.pdcStatus}. Must be PRESENTED or DEPOSITED."
            }

            // 1. Update Vendor Ledger PDC status
            val updated = entry.copy(
                pdcStatus = PdcStatus.CLEARED.name,
                clearedAt = System.currentTimeMillis(),
                runningBalance = 0.0,
                updatedAt = System.currentTimeMillis()
            )
            vendorLedgerDao.updateLedgerEntry(updated)

            // 2. Mark corresponding procurement as PAID
            if (entry.referenceDocNo.isNotBlank()) {
                val procurement = procurementDao.getProcurementByToken(entry.referenceDocNo)
                procurement?.let {
                    procurementDao.updateProcurement(
                        it.copy(
                            paymentStatus = PaymentStatus.PAID.name,
                            pdcCleared = true,
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                }
            }

            // 3. Log Audit Trail
            auditTrailRepository.logUpdate(
                entityType = "PDC",
                entityId = entry.referenceDocNo.ifEmpty { ledgerUuid },
                previousStateJson = "{\"status\":\"${entry.pdcStatus}\",\"runningBalance\":${entry.runningBalance}}",
                newStateJson = "{\"status\":\"${PdcStatus.CLEARED.name}\",\"clearedAt\":${updated.clearedAt}}",
                userId = userId,
                deviceId = deviceId,
                reason = "Confirmed bank clearance of PDC ${entry.utrOrChequeNo} for ${entry.vendorName}"
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
