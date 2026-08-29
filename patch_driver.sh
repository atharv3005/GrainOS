sed -i 's/var vehicleNumber by remember { mutableStateOf("MH 04 FK 8819") }/var vehicleNumber by remember { mutableStateOf("MH 04 FK 8819") }\n    var driverMobile by remember { mutableStateOf("") }/g' app/src/main/java/com/example/ui/screens/OutboundDispatchScreen.kt

cat << 'INNER_EOF' > /tmp/driver_ui.txt
                OutlinedTextField(
                    value = driverMobile,
                    onValueChange = { driverMobile = it },
                    label = { Text("Driver Mobile (WhatsApp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    colors = dispatchFieldColors(),
                    singleLine = true
                )
INNER_EOF
sed -i '/OutlinedTextField(/i \                OutlinedTextField(\n                    value = driverMobile,\n                    onValueChange = { driverMobile = it },\n                    label = { Text("Driver Mobile (WhatsApp)") },\n                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),\n                    modifier = Modifier.fillMaxWidth(),\n                    colors = dispatchFieldColors(),\n                    singleLine = true\n                )' app/src/main/java/com/example/ui/screens/OutboundDispatchScreen.kt
