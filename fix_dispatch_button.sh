cat << 'INNER_EOF' > /tmp/btn.patch
--- app/src/main/java/com/example/ui/screens/OutboundDispatchScreen.kt
+++ app/src/main/java/com/example/ui/screens/OutboundDispatchScreen.kt
@@ -288,27 +288,32 @@
                 }
 
                 Button(
                     onClick = {
+                        if (tare > gross) {
+                            errorMessage = "Error: Tare weight cannot be greater than Gross weight."
+                            return@Button
+                        }
                         val currentStockMt = liveGodownStockLedger[selectedGodown] ?: 0.0
                         if (netMt > currentStockMt) {
                             errorMessage = "Error: Cannot dispatch ${netMt.toInt()} MT. Only ${currentStockMt.toInt()} MT available in this storage facility."
                             return@Button
                         }
                         errorMessage = null
+                        
+                        val capturedDest = destination
+                        val capturedTruck = vehicleNumber
+                        val capturedGross = gross
+                        val capturedTare = tare
+                        val capturedNet = netKg
+                        
                         onCreateDispatch(buyerName, destination, vehicleNumber, activeCrop, selectedGodown, tare, gross, rate) {
                             buyerName = ""
                             destination = ""
                             vehicleNumber = ""
                             tareWeightKg = ""
                             grossWeightKg = ""
                             ratePerQuintal = ""
                         val firmName = "GrainOS Enterprise"
-                        val netKg = (gross - tare).coerceAtLeast(0.0)
                         val msg = """*GATE PASS*
 $firmName
 -----------------------
-Destination: $destination
-Truck No: $vehicleNumber
-Gross: ${gross.toInt()} kg
-Tare: ${tare.toInt()} kg
-Net: ${netKg.toInt()} kg
+Destination: $capturedDest
+Truck No: $capturedTruck
+Gross: ${capturedGross.toInt()} kg
+Tare: ${capturedTare.toInt()} kg
+Net: ${capturedNet.toInt()} kg
 """.trimIndent()
INNER_EOF
patch -p0 < /tmp/btn.patch
