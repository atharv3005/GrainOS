cat << 'INNER_EOF' > /tmp/ui_insert.txt

                    Divider(color = Color(0xFF334155), thickness = 0.5.dp)
                    Text(
                        text = "STORAGE FACILITIES",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp
                        ),
                        color = Color(0xFF94A3B8)
                    )
                    facilities.forEachIndexed { index, facility ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = facility.name,
                                onValueChange = { newName ->
                                    facilities[index] = facility.copy(name = newName)
                                },
                                label = { Text("Facility Name") },
                                modifier = Modifier.weight(1.5f),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = activeCrop.primaryColor,
                                    unfocusedBorderColor = Color(0xFF334155),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                )
                            )
                            OutlinedTextField(
                                value = facility.capacityStr,
                                onValueChange = { newCap ->
                                    facilities[index] = facility.copy(capacityStr = newCap.filter { it.isDigit() || it == '.' })
                                },
                                label = { Text("Capacity (MT)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = activeCrop.primaryColor,
                                    unfocusedBorderColor = Color(0xFF334155),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                )
                            )
                            IconButton(
                                onClick = { facilities.removeAt(index) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                            }
                        }
                    }
                    OutlinedButton(
                        onClick = {
                            facilities.add(DynamicFacilityItem(System.currentTimeMillis(), "", "500"))
                        },
                        modifier = Modifier.fillMaxWidth().height(40.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("+ Add Another Facility")
                    }
INNER_EOF
sed -i '/Spacer(modifier = Modifier.height(4.dp))/r /tmp/ui_insert.txt' app/src/main/java/com/example/ui/components/FirmLoginDialog.kt
