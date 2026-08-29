cat << 'INNER_EOF' > /tmp/firm_sheet.patch
--- app/src/main/java/com/example/ui/components/FirmInitializationSheet.kt
+++ app/src/main/java/com/example/ui/components/FirmInitializationSheet.kt
@@ -10,6 +10,8 @@
 import androidx.compose.ui.text.font.FontWeight
 import androidx.compose.ui.text.input.KeyboardType
 import androidx.compose.ui.unit.dp
 import androidx.compose.ui.unit.sp
+import androidx.compose.foundation.rememberScrollState
+import androidx.compose.foundation.verticalScroll
 
 @OptIn(ExperimentalMaterial3Api::class)
 @Composable
 fun FirmInitializationSheet(
     onDismissRequest: () -> Unit,
-    onSave: (firmName: String, apmcCode: String, location: String, initialCapital: Double) -> Unit
+    onSave: (firmName: String, apmcCode: String, location: String, initialCapital: Double, facilities: List<Pair<String, Double>>) -> Unit
 ) {
     val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
     
     var firmName by remember { mutableStateOf("") }
     var apmcCode by remember { mutableStateOf("") }
     var location by remember { mutableStateOf("") }
     var capitalString by remember { mutableStateOf("") }
+    
+    // Storage Facilities State
+    val facilities = remember { mutableStateListOf(Pair("Godown 1", "")) }
 
     ModalBottomSheet(
         onDismissRequest = onDismissRequest,
         sheetState = sheetState,
         containerColor = Color(0xFF121826), // Deep Dark Navy
         shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
     ) {
         Column(
             modifier = Modifier
                 .fillMaxWidth()
-                .padding(start = 24.dp, end = 24.dp, bottom = 32.dp, top = 8.dp),
+                .padding(start = 24.dp, end = 24.dp, bottom = 32.dp, top = 8.dp)
+                .verticalScroll(rememberScrollState()),
             verticalArrangement = Arrangement.spacedBy(16.dp)
         ) {
             Text(
@@ -62,6 +66,42 @@
             )
 
             Spacer(modifier = Modifier.height(16.dp))
+            
+            Divider(color = Color(0xFF334155))
+            
+            Text(
+                text = "Storage Facilities",
+                color = Color.White,
+                fontSize = 18.sp,
+                fontWeight = FontWeight.Bold
+            )
+            
+            facilities.forEachIndexed { index, facility ->
+                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
+                    Box(modifier = Modifier.weight(1f)) {
+                        DarkGlassTextField(
+                            value = facility.first,
+                            onValueChange = { facilities[index] = facility.copy(first = it) },
+                            label = "Facility Name",
+                            icon = Icons.Default.Business
+                        )
+                    }
+                    Box(modifier = Modifier.weight(1f)) {
+                        DarkGlassTextField(
+                            value = facility.second,
+                            onValueChange = { facilities[index] = facility.copy(second = it) },
+                            label = "Capacity (MT)",
+                            icon = Icons.Default.Assessment,
+                            keyboardType = KeyboardType.Decimal
+                        )
+                    }
+                }
+            }
+            
+            TextButton(onClick = { facilities.add(Pair("", "")) }) {
+                Text("+ Add Another Facility", color = Color(0xFFFF9800), fontWeight = FontWeight.Bold)
+            }
+            
+            Spacer(modifier = Modifier.height(8.dp))
 
             // Neon Orange Save Button
             Button(
                 onClick = {
                     val capital = capitalString.toDoubleOrNull() ?: 0.0
-                    onSave(firmName, apmcCode, location, capital)
+                    val parsedFacilities = facilities.filter { it.first.isNotBlank() && it.second.toDoubleOrNull() != null }
+                        .map { Pair(it.first, it.second.toDouble()) }
+                    onSave(firmName, apmcCode, location, capital, parsedFacilities)
                 },
INNER_EOF
patch -p0 < /tmp/firm_sheet.patch
