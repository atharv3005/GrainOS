package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.PartyEntity
import com.example.data.model.PartyType
import com.example.ui.viewmodel.FinanceViewModel

@Composable
fun PartyMasterScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val allParties by viewModel.allParties.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterType by remember { mutableStateOf<String?>("ALL") }
    var showPartyDialog by remember { mutableStateOf(false) }
    var partyToEdit by remember { mutableStateOf<PartyEntity?>(null) }

    val filteredParties = remember(allParties, searchQuery, selectedFilterType) {
        allParties.filter { party ->
            val matchesFilter = selectedFilterType == "ALL" || party.partyType == selectedFilterType
            val matchesSearch = searchQuery.isBlank() ||
                    party.legalName.contains(searchQuery, ignoreCase = true) ||
                    party.mobile.contains(searchQuery, ignoreCase = true) ||
                    party.village.contains(searchQuery, ignoreCase = true) ||
                    (party.pan?.contains(searchQuery, ignoreCase = true) == true)
            matchesFilter && matchesSearch
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFF0F172A),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    partyToEdit = null
                    showPartyDialog = true
                },
                containerColor = Color(0xFF10B981),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Party")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Party Master (पक्ष / ग्राहक / शेतकरी)",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF9FAFB)
                    )
                    Text(
                        text = "${filteredParties.size} registered counterparties",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF9CA3AF)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search by name, mobile, village, or PAN...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF10B981)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF10B981),
                    unfocusedBorderColor = Color(0xFF374151),
                    focusedTextColor = Color(0xFFF9FAFB),
                    unfocusedTextColor = Color(0xFFF9FAFB)
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Type Filter Chips
            val filterOptions = listOf("ALL" to "All Parties") + PartyType.entries.map { it.name to it.label }
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(filterOptions) { (key, label) ->
                    val isSelected = selectedFilterType == key
                    Surface(
                        modifier = Modifier.clickable { selectedFilterType = key },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) Color(0xFF10B981) else Color(0xFF1E293B)
                    ) {
                        Text(
                            text = label,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSelected) Color.White else Color(0xFF9CA3AF),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Party List
            if (filteredParties.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No parties found matching criteria.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF6B7280)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredParties, key = { it.uuid }) { party ->
                        PartyCard(
                            party = party,
                            onEdit = {
                                partyToEdit = party
                                showPartyDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    if (showPartyDialog) {
        PartyFormDialog(
            partyToEdit = partyToEdit,
            onDismiss = { showPartyDialog = false },
            onSave = { savedParty ->
                if (partyToEdit == null) {
                    viewModel.createParty(savedParty)
                } else {
                    viewModel.updateParty(savedParty)
                }
                showPartyDialog = false
            }
        )
    }
}

@Composable
private fun PartyCard(
    party: PartyEntity,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF10B981).copy(alpha = 0.2f), CircleShape)
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = if (party.partyType == PartyType.BUYER.name) Icons.Default.Business else Icons.Default.Person,
                            contentDescription = null,
                            tint = Color(0xFF10B981)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = party.legalName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF9FAFB)
                        )
                        if (!party.tradeName.isNullOrBlank()) {
                            Text(
                                text = party.tradeName ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF9CA3AF)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF374151)
                    ) {
                        Text(
                            text = party.partyType,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF10B981),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF9CA3AF))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Details
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                if (party.mobile.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF6B7280), modifier = Modifier.padding(end = 4.dp))
                        Text(text = party.mobile, style = MaterialTheme.typography.bodySmall, color = Color(0xFFD1D5DB))
                    }
                }
                if (party.village.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Place, contentDescription = null, tint = Color(0xFF6B7280), modifier = Modifier.padding(end = 4.dp))
                        Text(text = "${party.village}, ${party.district}", style = MaterialTheme.typography.bodySmall, color = Color(0xFFD1D5DB))
                    }
                }
            }

            if (!party.pan.isNullOrBlank() || party.cumulativePurchasesInFy > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (!party.pan.isNullOrBlank()) {
                        Text(
                            text = "PAN: ${party.pan}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF9CA3AF)
                        )
                    }
                    if (party.cumulativePurchasesInFy > 0) {
                        Text(
                            text = "FY Turnover: ₹${"%,.0f".format(party.cumulativePurchasesInFy)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8)
                        )
                    }
                }
            }
        }
    }
}
