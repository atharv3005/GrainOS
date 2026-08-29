package com.example.domain.managers

import com.example.data.repository.GrainRepository
import com.example.data.model.ProcurementEntity
import com.example.data.model.OutboundDispatchEntity
import com.example.data.model.DispatchStatus
import com.example.data.model.ProcurementStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class TransactionLedgerManager(
    private val repository: GrainRepository,
    private val inventoryManager: InventoryManager
) {
    suspend fun recordProcurement(procurement: ProcurementEntity) {
        repository.insertProcurement(procurement)
        if (procurement.status == ProcurementStatus.COMPLETED.name || procurement.status == ProcurementStatus.UNLOADED.name) {
            inventoryManager.addWeightToSilo(procurement.godownAssigned, procurement.netWeightKg)
        }
    }

    suspend fun recordDispatch(dispatch: OutboundDispatchEntity) {
        if (dispatch.status != DispatchStatus.REJECTED.name) {
            inventoryManager.subtractWeightFromSilo(dispatch.godownSource, dispatch.netLoadedWeightKg)
        }
    }
}
