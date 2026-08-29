cat << 'INNER_EOF' > /tmp/db.patch
--- app/src/main/java/com/example/data/repository/GrainRepository.kt
+++ app/src/main/java/com/example/data/repository/GrainRepository.kt
@@ -426,6 +426,8 @@
         id
     }
 
+    suspend fun getDispatchById(id: Long): OutboundDispatchEntity? = dispatchDao.getDispatchById(id)
+
     // 8. Settle Unloaded Dispatch & Calculate Actual Net Profit
     suspend fun settleUnloadedDispatch(
         dispatchId: Long,
--- app/src/main/java/com/example/ui/viewmodel/GrainWmsViewModel.kt
+++ app/src/main/java/com/example/ui/viewmodel/GrainWmsViewModel.kt
@@ -541,8 +541,7 @@
                     onComplete = onComplete
                 )
                 
-                val dispatchFlow = allDispatches.value
-                val dispatch = dispatchFlow.find { it.id == id }
+                val dispatch = repository.getDispatchById(id)
                 if (dispatch != null) {
                     transactionManager.recordDispatch(dispatch)
                 }
INNER_EOF
patch -p0 < /tmp/db.patch
