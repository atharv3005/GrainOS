package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.PartyEntity
import com.example.data.model.PartyType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartyFormDialog(
    partyToEdit: PartyEntity? = null,
    onDismiss: () -> Unit,
    onSave: (PartyEntity) -> Unit
) {
    var selectedPartyType by remember {
        mutableStateOf(
            partyToEdit?.let { PartyType.entries.find { pt -> pt.name == it.partyType } } ?: PartyType.FARMER
        )
    }
    var partyTypeExpanded by remember { mutableStateOf(false) }

    var legalName by remember { mutableStateOf(partyToEdit?.legalName ?: "") }
    var tradeName by remember { mutableStateOf(partyToEdit?.tradeName ?: "") }
    var mobile by remember { mutableStateOf(partyToEdit?.mobile ?: "") }
    var altPhone by remember { mutableStateOf(partyToEdit?.alternateMobile ?: "") }
    var village by remember { mutableStateOf(partyToEdit?.village ?: "") }
    var taluka by remember { mutableStateOf(partyToEdit?.taluka ?: "") }
    var district by remember { mutableStateOf(partyToEdit?.district ?: "Dhule") }
    var state by remember { mutableStateOf(partyToEdit?.state ?: "Maharashtra") }
    var pan by remember { mutableStateOf(partyToEdit?.pan ?: "") }
    var gstin by remember { mutableStateOf(partyToEdit?.gstin ?: "") }
    var bankAccountName by remember { mutableStateOf(partyToEdit?.bankAccountName ?: "") }
    var bankAccountNumber by remember { mutableStateOf(partyToEdit?.bankAccountNumber ?: "") }
    var bankIfsc by remember { mutableStateOf(partyToEdit?.bankIfsc ?: "") }
    var bankName by remember { mutableStateOf(partyToEdit?.bankName ?: "") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF131B26),
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (partyToEdit == null) "New Party Master Entry" else "Edit Party Master",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF9FAFB)
                        )
                        Text(
                            text = "Unified KYC, Bank Details & Tax Profile",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF9CA3AF)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF9CA3AF))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Party Type Dropdown
                ExposedDropdownMenuBox(
                    expanded = partyTypeExpanded,
                    onExpandedChange = { partyTypeExpanded = !partyTypeExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedPartyType.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Party Type (प्रकार)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = partyTypeExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF10B981),
                            unfocusedBorderColor = Color(0xFF374151),
                            focusedTextColor = Color(0xFFF9FAFB),
                            unfocusedTextColor = Color(0xFFF9FAFB)
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = partyTypeExpanded,
                        onDismissRequest = { partyTypeExpanded = false }
                    ) {
                        PartyType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.label) },
                                onClick = {
                                    selectedPartyType = type
                                    partyTypeExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = Color(0xFFEF4444),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Legal Name
                OutlinedTextField(
                    value = legalName,
                    onValueChange = { legalName = it },
                    label = { Text("Legal Name / Full Name *") },
                    placeholder = { Text("e.g., Ramesh Patil or Cargill India Pvt Ltd") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF10B981),
                        unfocusedBorderColor = Color(0xFF374151),
                        focusedTextColor = Color(0xFFF9FAFB),
                        unfocusedTextColor = Color(0xFFF9FAFB)
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Trade Name
                OutlinedTextField(
                    value = tradeName,
                    onValueChange = { tradeName = it },
                    label = { Text("Trade Name / Firm Name (Optional)") },
                    placeholder = { Text("e.g., Patil Krushi Kendra") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF10B981),
                        unfocusedBorderColor = Color(0xFF374151),
                        focusedTextColor = Color(0xFFF9FAFB),
                        unfocusedTextColor = Color(0xFFF9FAFB)
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Primary Mobile & Alternate Mobile
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = mobile,
                        onValueChange = { mobile = it },
                        label = { Text("Primary Mobile *") },
                        placeholder = { Text("10-digit number") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF10B981),
                            unfocusedBorderColor = Color(0xFF374151),
                            focusedTextColor = Color(0xFFF9FAFB),
                            unfocusedTextColor = Color(0xFFF9FAFB)
                        )
                    )
                    OutlinedTextField(
                        value = altPhone,
                        onValueChange = { altPhone = it },
                        label = { Text("Alternate Phone") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF10B981),
                            unfocusedBorderColor = Color(0xFF374151),
                            focusedTextColor = Color(0xFFF9FAFB),
                            unfocusedTextColor = Color(0xFFF9FAFB)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Village & Taluka
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = village,
                        onValueChange = { village = it },
                        label = { Text("Village / City") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF10B981),
                            unfocusedBorderColor = Color(0xFF374151),
                            focusedTextColor = Color(0xFFF9FAFB),
                            unfocusedTextColor = Color(0xFFF9FAFB)
                        )
                    )
                    OutlinedTextField(
                        value = taluka,
                        onValueChange = { taluka = it },
                        label = { Text("Taluka") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF10B981),
                            unfocusedBorderColor = Color(0xFF374151),
                            focusedTextColor = Color(0xFFF9FAFB),
                            unfocusedTextColor = Color(0xFFF9FAFB)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // District & State
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = district,
                        onValueChange = { district = it },
                        label = { Text("District") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF10B981),
                            unfocusedBorderColor = Color(0xFF374151),
                            focusedTextColor = Color(0xFFF9FAFB),
                            unfocusedTextColor = Color(0xFFF9FAFB)
                        )
                    )
                    OutlinedTextField(
                        value = state,
                        onValueChange = { state = it },
                        label = { Text("State") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF10B981),
                            unfocusedBorderColor = Color(0xFF374151),
                            focusedTextColor = Color(0xFFF9FAFB),
                            unfocusedTextColor = Color(0xFFF9FAFB)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tax Info
                Text(
                    text = "Tax & Statutory Identifiers (कर आणि वैधानिक माहिती)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF10B981)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = pan,
                        onValueChange = { pan = it },
                        label = { Text("PAN Number") },
                        placeholder = { Text("ABCDE1234F") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF10B981),
                            unfocusedBorderColor = Color(0xFF374151),
                            focusedTextColor = Color(0xFFF9FAFB),
                            unfocusedTextColor = Color(0xFFF9FAFB)
                        )
                    )
                    OutlinedTextField(
                        value = gstin,
                        onValueChange = { gstin = it },
                        label = { Text("GSTIN") },
                        placeholder = { Text("27ABCDE1234F1Z5") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF10B981),
                            unfocusedBorderColor = Color(0xFF374151),
                            focusedTextColor = Color(0xFFF9FAFB),
                            unfocusedTextColor = Color(0xFFF9FAFB)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bank Details
                Text(
                    text = "Bank Settlement Details (बँक तपशील)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF10B981)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = bankAccountName,
                    onValueChange = { bankAccountName = it },
                    label = { Text("Account Holder Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF10B981),
                        unfocusedBorderColor = Color(0xFF374151),
                        focusedTextColor = Color(0xFFF9FAFB),
                        unfocusedTextColor = Color(0xFFF9FAFB)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = bankAccountNumber,
                    onValueChange = { bankAccountNumber = it },
                    label = { Text("Bank Account Number") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF10B981),
                        unfocusedBorderColor = Color(0xFF374151),
                        focusedTextColor = Color(0xFFF9FAFB),
                        unfocusedTextColor = Color(0xFFF9FAFB)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = bankIfsc,
                        onValueChange = { bankIfsc = it },
                        label = { Text("IFSC Code") },
                        placeholder = { Text("SBIN0001234") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF10B981),
                            unfocusedBorderColor = Color(0xFF374151),
                            focusedTextColor = Color(0xFFF9FAFB),
                            unfocusedTextColor = Color(0xFFF9FAFB)
                        )
                    )
                    OutlinedTextField(
                        value = bankName,
                        onValueChange = { bankName = it },
                        label = { Text("Bank Name") },
                        placeholder = { Text("State Bank of India") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF10B981),
                            unfocusedBorderColor = Color(0xFF374151),
                            focusedTextColor = Color(0xFFF9FAFB),
                            unfocusedTextColor = Color(0xFFF9FAFB)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF374151))
                    ) {
                        Text("Cancel", color = Color(0xFFF9FAFB))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            if (legalName.isBlank()) {
                                errorMessage = "Legal Name is required."
                                return@Button
                            }
                            val party = PartyEntity(
                                id = partyToEdit?.id ?: 0L,
                                uuid = partyToEdit?.uuid ?: java.util.UUID.randomUUID().toString(),
                                partyType = selectedPartyType.name,
                                legalName = legalName.trim(),
                                tradeName = tradeName.trim().ifBlank { null },
                                mobile = mobile.trim(),
                                alternateMobile = altPhone.trim().ifBlank { null },
                                village = village.trim(),
                                taluka = taluka.trim(),
                                district = district.trim(),
                                state = state.trim(),
                                pan = pan.trim().uppercase().ifBlank { null },
                                gstin = gstin.trim().uppercase().ifBlank { null },
                                bankAccountName = bankAccountName.trim().ifBlank { null },
                                bankAccountNumber = bankAccountNumber.trim().ifBlank { null },
                                bankIfsc = bankIfsc.trim().uppercase().ifBlank { null },
                                bankName = bankName.trim().ifBlank { null },
                                cumulativePurchasesInFy = partyToEdit?.cumulativePurchasesInFy ?: 0.0,
                                runningBalance = partyToEdit?.runningBalance ?: 0.0
                            )
                            onSave(party)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        Text(if (partyToEdit == null) "Save Party" else "Update Party", color = Color.White)
                    }
                }
            }
        }
    }
}
