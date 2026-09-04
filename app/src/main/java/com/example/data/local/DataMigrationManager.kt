package com.example.data.local

import com.example.data.model.DocumentSequenceEntity
import com.example.data.model.DocumentType
import com.example.data.model.PartyEntity
import com.example.data.model.PartyType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Enterprise Data Migration & Backfill Manager.
 * Responsibilities:
 * 1. Automatically auto-creates unified PartyEntity records from legacy free-text names (Farmers, Buyers, Brokers, Transporters).
 * 2. Backfills missing partyId foreign keys and UUIDs across legacy transactions.
 * 3. Scans legacy document token numbers (e.g. GIN/26-27/00142 or TK-8421) and initializes DocumentSequenceEntity with the true maximum sequence.
 */
class DataMigrationManager(private val db: AppDatabase) {

    private val partyDao = db.partyDao()
    private val procurementDao = db.procurementDao()
    private val dispatchDao = db.dispatchDao()
    private val vendorLedgerDao = db.vendorLedgerDao()
    private val tradeDao = db.tradeDao()
    private val sequenceDao = db.documentSequenceDao()

    data class MigrationResult(
        val partiesCreated: Int,
        val procurementsBackfilled: Int,
        val dispatchesBackfilled: Int,
        val ledgersBackfilled: Int,
        val sequencesInitialized: Int,
        val isSuccess: Boolean,
        val message: String
    )

    suspend fun runDataMigration(
        financialYear: String = "26-27",
        facilityId: String = "MAIN"
    ): MigrationResult = withContext(Dispatchers.IO) {
        try {
            var partiesCreatedCount = 0
            var procurementsCount = 0
            var dispatchesCount = 0
            var ledgersCount = 0
            var sequencesCount = 0

            // 1. Auto-discover & create Parties from legacy Procurements (Farmers)
            val procurements = procurementDao.getAllProcurementsDirect()
            val existingParties = partyDao.getAllParties().toMutableList()

            for (proc in procurements) {
                var party = existingParties.find {
                    (it.mobile.isNotBlank() && it.mobile == proc.mobileNumber) ||
                    it.legalName.equals(proc.farmerName.trim(), ignoreCase = true)
                }

                if (party == null && proc.farmerName.isNotBlank()) {
                    val newParty = PartyEntity(
                        partyType = PartyType.FARMER.name,
                        legalName = proc.farmerName.trim(),
                        mobile = proc.mobileNumber.trim().ifBlank { "9800000000" },
                        village = proc.village.trim(),
                        pan = proc.panNumber?.trim()?.uppercase()?.ifBlank { null },
                        cumulativePurchasesInFy = proc.grossBillAmount
                    )
                    val newId = partyDao.insert(newParty)
                    party = newParty.copy(id = newId)
                    existingParties.add(party)
                    partiesCreatedCount++
                }

                if (party != null && proc.partyId == null) {
                    val updatedProc = proc.copy(
                        partyId = party.id,
                        uuid = if (proc.uuid.isBlank()) UUID.randomUUID().toString() else proc.uuid
                    )
                    procurementDao.updateProcurement(updatedProc)
                    procurementsCount++
                }
            }

            // 2. Auto-discover & create Parties from legacy Dispatches (Corporate Buyers)
            val dispatches = dispatchDao.getAllDispatchesDirect()
            for (disp in dispatches) {
                var buyerParty = existingParties.find {
                    it.partyType == PartyType.BUYER.name &&
                    it.legalName.equals(disp.buyerName.trim(), ignoreCase = true)
                }

                if (buyerParty == null && disp.buyerName.isNotBlank()) {
                    val newBuyer = PartyEntity(
                        partyType = PartyType.BUYER.name,
                        legalName = disp.buyerName.trim(),
                        mobile = "9900000000",
                        village = disp.destination.trim()
                    )
                    val newId = partyDao.insert(newBuyer)
                    buyerParty = newBuyer.copy(id = newId)
                    existingParties.add(buyerParty)
                    partiesCreatedCount++
                }

                if (buyerParty != null && disp.buyerPartyId == null) {
                    val updatedDisp = disp.copy(
                        buyerPartyId = buyerParty.id,
                        uuid = if (disp.uuid.isBlank()) UUID.randomUUID().toString() else disp.uuid
                    )
                    dispatchDao.updateDispatch(updatedDisp)
                    dispatchesCount++
                }
            }

            // 3. Backfill Vendor Ledgers & Link Parties
            val ledgers = vendorLedgerDao.getAllLedgersDirect()
            for (ledger in ledgers) {
                var party = existingParties.find {
                    it.legalName.equals(ledger.vendorName.trim(), ignoreCase = true)
                }
                if (party == null && ledger.vendorName.isNotBlank()) {
                    val pType = when (ledger.vendorType) {
                        "CORPORATE_BUYER" -> PartyType.BUYER.name
                        "BROKER" -> PartyType.BROKER.name
                        "TRANSPORTER" -> PartyType.TRANSPORTER.name
                        "LABOUR" -> PartyType.LABOUR.name
                        else -> PartyType.FARMER.name
                    }
                    val newP = PartyEntity(
                        partyType = pType,
                        legalName = ledger.vendorName.trim(),
                        mobile = "9800000000"
                    )
                    val newId = partyDao.insert(newP)
                    party = newP.copy(id = newId)
                    existingParties.add(party)
                    partiesCreatedCount++
                }

                if (party != null && ledger.partyId == null) {
                    val updatedLedger = ledger.copy(
                        partyId = party.id,
                        uuid = if (ledger.uuid.isBlank()) UUID.randomUUID().toString() else ledger.uuid
                    )
                    vendorLedgerDao.updateLedgerEntry(updatedLedger)
                    ledgersCount++
                }
            }

            // 4. Initialize Document Sequence Counters based on maximum existing legacy numbers
            var maxGinSeq = 0L
            for (p in procurements) {
                val token = p.tokenNo
                val parsed = extractNumericSequence(token)
                if (parsed > maxGinSeq) maxGinSeq = parsed
            }

            var maxDspSeq = 0L
            for (d in dispatches) {
                val no = d.dispatchNo
                val parsed = extractNumericSequence(no)
                if (parsed > maxDspSeq) maxDspSeq = parsed
            }

            // Initialize GIN Sequence
            val existingGinSeq = sequenceDao.getSequence(financialYear, facilityId, DocumentType.GIN.name)
            if (existingGinSeq == null) {
                sequenceDao.insert(
                    DocumentSequenceEntity(
                        financialYear = financialYear,
                        facilityId = facilityId,
                        documentType = DocumentType.GIN.name,
                        seriesCode = "GIN",
                        currentSequence = maxGinSeq.coerceAtLeast(0L)
                    )
                )
                sequencesCount++
            }

            // Initialize DSP Sequence
            val existingDspSeq = sequenceDao.getSequence(financialYear, facilityId, DocumentType.DSP.name)
            if (existingDspSeq == null) {
                sequenceDao.insert(
                    DocumentSequenceEntity(
                        financialYear = financialYear,
                        facilityId = facilityId,
                        documentType = DocumentType.DSP.name,
                        seriesCode = "DSP",
                        currentSequence = maxDspSeq.coerceAtLeast(0L)
                    )
                )
                sequencesCount++
            }

            // Initialize TRD Sequence
            val existingTrdSeq = sequenceDao.getSequence(financialYear, facilityId, DocumentType.TRD.name)
            if (existingTrdSeq == null) {
                sequenceDao.insert(
                    DocumentSequenceEntity(
                        financialYear = financialYear,
                        facilityId = facilityId,
                        documentType = DocumentType.TRD.name,
                        seriesCode = "TRD",
                        currentSequence = 0L
                    )
                )
                sequencesCount++
            }

            MigrationResult(
                partiesCreated = partiesCreatedCount,
                procurementsBackfilled = procurementsCount,
                dispatchesBackfilled = dispatchesCount,
                ledgersBackfilled = ledgersCount,
                sequencesInitialized = sequencesCount,
                isSuccess = true,
                message = "Migration Complete: $partiesCreatedCount parties created, $procurementsCount procurements backfilled, $sequencesCount sequence series initialized."
            )
        } catch (e: Exception) {
            MigrationResult(
                partiesCreated = 0,
                procurementsBackfilled = 0,
                dispatchesBackfilled = 0,
                ledgersBackfilled = 0,
                sequencesInitialized = 0,
                isSuccess = false,
                message = "Migration Failed: ${e.localizedMessage}"
            )
        }
    }

    private fun extractNumericSequence(docNo: String): Long {
        val clean = docNo.replace("[^0-9]".toRegex(), "")
        return clean.takeLast(5).toLongOrNull() ?: 0L
    }
}
