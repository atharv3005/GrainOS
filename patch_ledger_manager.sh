sed -i 's/^}$//g' app/src/main/java/com/example/domain/managers/TransactionLedgerManager.kt
cat << 'INNER_EOF' >> app/src/main/java/com/example/domain/managers/TransactionLedgerManager.kt
    suspend fun recordDispatch(dispatch: OutboundDispatchEntity) {
        if (dispatch.status != DispatchStatus.REJECTED.name) {
            inventoryManager.deductWeightFromSilo(dispatch.godownSource, dispatch.netLoadedWeightKg)
        }
    }
}
INNER_EOF
