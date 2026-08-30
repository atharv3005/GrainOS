package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.export.CsvExporter
import com.example.data.model.CropType
import com.example.data.model.PaymentStatus
import com.example.data.model.ProcurementEntity
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ProcurementFilterTab(val label: String) {
    ACTIVE("Active"),
    ARCHIVED("Archived"),
    PENDING_PAY("Pending Pay"),
    PAID("Paid"),
    ALL("All Records")
}

@Composable
fun FarmerReceiptsScreen(
    procurements: List<ProcurementEntity>,
    activeCrop: CropType,
    onOpenWhatsApp: (ProcurementEntity) -> Unit,
    onOpenPdf: (ProcurementEntity) -> Unit,
    onDownloadPdf: (ProcurementEntity) -> Unit = {},
    onPrintReceipt: (ProcurementEntity) -> Unit = {},
    onTogglePaymentStatus: (Long, PaymentStatus) -> Unit,
    onEdit: (ProcurementEntity) -> Unit = {},
    onDelete: (ProcurementEntity) -> Unit = {},
    onToggleArchive: (ProcurementEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Real-time farmer name search state
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(ProcurementFilterTab.ACTIVE) }
    var editingProcurement by remember { mutableStateOf<ProcurementEntity?>(null) }
    var itemToDelete by remember { mutableStateOf<ProcurementEntity?>(null) }
    var itemToArchive by remember { mutableStateOf<ProcurementEntity?>(null) }

    val inrFormat = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
    val context = LocalContext.current

    // Summary counts across all data
    val totalCount = procurements.size
    val activeCount = procurements.count { !it.isArchived }
    val archivedCount = procurements.count { it.isArchived }
    val pendingPayCount = procurements.count { it.paymentStatus != "PAID" && !it.isArchived }
    val paidCount = procurements.count { it.paymentStatus == "PAID" && !it.isArchived }

    // Real-time filtering by farmer name as user types
    val filteredList = remember(procurements, searchQuery, selectedFilter) {
        procurements.filter { p ->
            val trimmedQuery = searchQuery.trim()
            val matchesFarmerSearch = if (trimmedQuery.isEmpty()) {
                true
            } else {
                // Match farmer name in real-time (and also support token/village/vehicle fallback)
                p.farmerName.contains(trimmedQuery, ignoreCase = true) ||
                        p.vehicleNumber.contains(trimmedQuery, ignoreCase = true) ||
                        p.tokenNo.contains(trimmedQuery, ignoreCase = true) ||
                        p.village.contains(trimmedQuery, ignoreCase = true)
            }

            val matchesFilter = when (selectedFilter) {
                ProcurementFilterTab.ACTIVE -> !p.isArchived
                ProcurementFilterTab.ARCHIVED -> p.isArchived
                ProcurementFilterTab.PENDING_PAY -> p.paymentStatus != "PAID" && !p.isArchived
                ProcurementFilterTab.PAID -> p.paymentStatus == "PAID" && !p.isArchived
                ProcurementFilterTab.ALL -> true
            }
            matchesFarmerSearch && matchesFilter
        }
    }

    // Dynamic stats for filtered view
    val totalNetWeightKg = filteredList.sumOf { it.netWeightKg }
    val totalPayout = filteredList.sumOf { it.totalAmount }
    val avgMoisture = if (filteredList.isNotEmpty()) {
        filteredList.map { it.moisturePercentage }.average()
    } else 0.0

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Prominent Real-time Farmer Search Bar at the Top
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            border = BorderStroke(1.2.dp, activeCrop.primaryColor.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                text = "Search farmer name (e.g. Ramesh, Patil, Suresh)...",
                                color = Color(0xFF64748B),
                                fontSize = 13.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.PersonSearch,
                                contentDescription = "Search Farmer Name",
                                tint = activeCrop.primaryColor
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { searchQuery = "" },
                                    modifier = Modifier.testTag("procurement_search_clear_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear Search",
                                        tint = Color(0xFF94A3B8)
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("procurement_search_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = activeCrop.primaryColor,
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF1E293B),
                            unfocusedContainerColor = Color(0xFF1E293B)
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Quick CSV Export Button
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1E293B))
                            .border(1.dp, activeCrop.primaryColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .clickable { CsvExporter.exportProcurements(context, filteredList) }
                            .testTag("export_procurements_csv_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Export CSV",
                            tint = activeCrop.primaryColor
                        )
                    }
                }

                // Real-time search status indicator pill
                AnimatedVisibility(
                    visible = searchQuery.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(activeCrop.primaryColor)
                            )
                            Text(
                                text = "Filtering by farmer: \"$searchQuery\"",
                                color = Color(0xFFE2E8F0),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Text(
                            text = "${filteredList.size} match${if (filteredList.size != 1) "es" else ""}",
                            color = activeCrop.primaryColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 2. Filter Pills with Dynamic Counts
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                ProcurementFilterTab.ACTIVE to activeCount,
                ProcurementFilterTab.ARCHIVED to archivedCount,
                ProcurementFilterTab.PENDING_PAY to pendingPayCount,
                ProcurementFilterTab.PAID to paidCount,
                ProcurementFilterTab.ALL to totalCount
            ).forEach { (tab, count) ->
                val isSelected = selectedFilter == tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) activeCrop.primaryColor else Color(0xFF1E293B))
                        .border(
                            1.dp,
                            if (isSelected) activeCrop.primaryColor else Color(0xFF334155),
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { selectedFilter = tab }
                        .padding(vertical = 8.dp, horizontal = 2.dp)
                        .testTag("filter_tab_${tab.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = tab.label,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                            color = if (isSelected) Color.Black else Color(0xFFCBD5E1),
                            maxLines = 1
                        )
                        Text(
                            text = "($count)",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.Black.copy(alpha = 0.8f) else Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }

        // 3. Quick Metrics Overview Strip
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            border = BorderStroke(1.dp, Color(0xFF1E293B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("RECORDS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                    Text("${filteredList.size}", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                }
                Column {
                    Text("TOTAL NET WT", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                    Text(
                        text = "${String.format(Locale.US, "%.2f", totalNetWeightKg / 1000.0)} MT",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = activeCrop.primaryColor
                    )
                }
                Column {
                    Text("AVG MOISTURE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                    Text(
                        text = "${String.format(Locale.US, "%.1f", avgMoisture)}%",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (avgMoisture <= 14.0) Color(0xFF34D399) else Color(0xFFFBBF24)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("TOTAL DISBURSED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                    Text(
                        text = inrFormat.format(totalPayout),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF10B981)
                    )
                }
            }
        }

        // 4. LazyColumn List of Procurement Records
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = if (searchQuery.isNotEmpty()) Icons.Default.PersonSearch else if (selectedFilter == ProcurementFilterTab.ARCHIVED) Icons.Default.Archive else Icons.Default.Scale,
                        contentDescription = null,
                        tint = activeCrop.primaryColor.copy(alpha = 0.6f),
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = if (searchQuery.isNotEmpty()) "No records found for farmer \"$searchQuery\""
                        else if (selectedFilter == ProcurementFilterTab.ARCHIVED) "No archived procurement records"
                        else "No procurement records found",
                        color = Color(0xFF94A3B8),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    if (searchQuery.isNotEmpty()) {
                        TextButton(
                            onClick = { searchQuery = "" },
                            colors = ButtonDefaults.textButtonColors(contentColor = activeCrop.primaryColor)
                        ) {
                            Text("Clear search query", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Text(
                            text = "New arrivals and weighbridge entries will appear here.",
                            color = Color(0xFF64748B),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("procurement_lazy_column"),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredList, key = { it.id }) { item ->
                    ProcurementItemCard(
                        item = item,
                        searchQuery = searchQuery,
                        accentColor = activeCrop.primaryColor,
                        onOpenWhatsApp = { onOpenWhatsApp(item) },
                        onOpenPdf = { onOpenPdf(item) },
                        onDownloadPdf = { onDownloadPdf(item) },
                        onPrintReceipt = { onPrintReceipt(item) },
                        onTogglePayment = {
                            val newStatus = if (item.paymentStatus == "PAID") PaymentStatus.PENDING else PaymentStatus.PAID
                            onTogglePaymentStatus(item.id, newStatus)
                        },
                        onEdit = { editingProcurement = item },
                        onArchiveToggle = { itemToArchive = item },
                        onDeleteClick = { itemToDelete = item },
                        inrFormat = inrFormat,
                        dateFormat = dateFormat
                    )
                }
            }
        }
    }

    // Archive Confirmation Dialog
    itemToArchive?.let { target ->
        val isCurrentlyArchived = target.isArchived
        AlertDialog(
            onDismissRequest = { itemToArchive = null },
            containerColor = Color(0xFF1E293B),
            icon = {
                Icon(
                    imageVector = if (isCurrentlyArchived) Icons.Default.Unarchive else Icons.Default.Archive,
                    contentDescription = null,
                    tint = if (isCurrentlyArchived) Color(0xFF34D399) else Color(0xFFF59E0B),
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = if (isCurrentlyArchived) "Restore Procurement Record?" else "Archive Procurement Record?",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isCurrentlyArchived) {
                            "Restore token ${target.tokenNo} for farmer ${target.farmerName} back to active records?"
                        } else {
                            "Are you sure you want to archive token ${target.tokenNo} for farmer ${target.farmerName}?"
                        },
                        color = Color(0xFFCBD5E1),
                        fontSize = 13.sp
                    )
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        border = BorderStroke(1.dp, Color(0xFF334155)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "• Farmer: ${target.farmerName} (${target.village})",
                                fontSize = 12.sp,
                                color = Color(0xFFE2E8F0)
                            )
                            Text(
                                text = "• Vehicle: ${target.vehicleNumber} • Crop: ${target.cropType}",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8)
                            )
                            Text(
                                text = "• Net Weight: ${target.netWeightKg.toInt()} kg (${String.format(Locale.US, "%.2f", target.netWeightKg / 100.0)} Qtl)",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8)
                            )
                            Text(
                                text = "• Total Payout: ${inrFormat.format(target.totalAmount)} (${target.paymentStatus})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                        }
                    }
                    Text(
                        text = if (isCurrentlyArchived) {
                            "Restored records will immediately reappear in the Active procurement queue and daily financial tallies."
                        } else {
                            "Archived records are preserved safely for audit compliance and can be accessed or restored anytime under the 'Archived' tab."
                        },
                        color = if (isCurrentlyArchived) Color(0xFF34D399) else Color(0xFFFBBF24),
                        fontSize = 11.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onToggleArchive(target)
                        itemToArchive = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCurrentlyArchived) Color(0xFF10B981) else Color(0xFFF59E0B)
                    ),
                    modifier = Modifier.testTag(if (isCurrentlyArchived) "confirm_unarchive_procurement_btn" else "confirm_archive_procurement_btn")
                ) {
                    Text(
                        text = if (isCurrentlyArchived) "Restore Record" else "Archive Record",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { itemToArchive = null },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF94A3B8)),
                    border = BorderStroke(1.dp, Color(0xFF475569)),
                    modifier = Modifier.testTag("cancel_archive_procurement_btn")
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Confirmation Dialog
    itemToDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            containerColor = Color(0xFF1E293B),
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text("Permanently Delete Entry?", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Are you sure you want to permanently delete token ${target.tokenNo} for farmer ${target.farmerName}? This action cannot be undone.",
                        color = Color(0xFFCBD5E1),
                        fontSize = 13.sp
                    )
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "• Farmer: ${target.farmerName} (${target.village})",
                                fontSize = 12.sp,
                                color = Color(0xFFE2E8F0)
                            )
                            Text(
                                text = "• Vehicle: ${target.vehicleNumber} • Crop: ${target.cropType}",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8)
                            )
                            Text(
                                text = "• Net Weight: ${target.netWeightKg.toInt()} kg (${String.format(Locale.US, "%.2f", target.netWeightKg / 100.0)} Qtl)",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8)
                            )
                            Text(
                                text = "• Total Payout: ${inrFormat.format(target.totalAmount)} (${target.paymentStatus})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEF4444)
                            )
                        }
                    }
                    Text(
                        text = "Warning: Permanent deletion removes this entry from all reports and ledger reconciliations. If you only want to hide it, choose 'Archive' instead.",
                        color = Color(0xFFF59E0B),
                        fontSize = 11.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete(target)
                        itemToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    modifier = Modifier.testTag("confirm_delete_procurement_btn")
                ) {
                    Text("Delete Permanently", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { itemToDelete = null },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF94A3B8)),
                    border = BorderStroke(1.dp, Color(0xFF475569)),
                    modifier = Modifier.testTag("cancel_delete_procurement_btn")
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Edit Procurement Modal Dialog
    editingProcurement?.let { proc ->
        com.example.ui.components.EditProcurementDialog(
            procurement = proc,
            onDismiss = { editingProcurement = null },
            onConfirm = { updatedProc ->
                onEdit(updatedProc)
                editingProcurement = null
            }
        )
    }
}

@Composable
private fun ProcurementItemCard(
    item: ProcurementEntity,
    searchQuery: String,
    accentColor: Color,
    onOpenWhatsApp: () -> Unit,
    onOpenPdf: () -> Unit,
    onDownloadPdf: () -> Unit,
    onPrintReceipt: () -> Unit,
    onTogglePayment: () -> Unit,
    onEdit: () -> Unit,
    onArchiveToggle: () -> Unit,
    onDeleteClick: () -> Unit,
    inrFormat: NumberFormat,
    dateFormat: SimpleDateFormat
) {
    val isArchived = item.isArchived
    val moisture = item.moisturePercentage
    val moistureColor = when {
        moisture <= 12.0 -> Color(0xFF10B981) // Grade A Prime
        moisture <= 14.0 -> Color(0xFF38BDF8) // Grade B Standard
        moisture <= 17.0 -> Color(0xFFF59E0B) // Grade C Drying Yard
        else -> Color(0xFFEF4444) // High moisture warning
    }

    // Highlight searched farmer name if matching
    val highlightedFarmerName = remember(item.farmerName, searchQuery) {
        val query = searchQuery.trim()
        if (query.isEmpty() || !item.farmerName.contains(query, ignoreCase = true)) {
            buildAnnotatedString {
                append(item.farmerName)
            }
        } else {
            buildAnnotatedString {
                val startIdx = item.farmerName.indexOf(query, ignoreCase = true)
                val endIdx = startIdx + query.length
                append(item.farmerName.substring(0, startIdx))
                withStyle(
                    SpanStyle(
                        color = Color(0xFFFBBF24),
                        background = Color(0xFF78350F).copy(alpha = 0.5f),
                        fontWeight = FontWeight.Black
                    )
                ) {
                    append(item.farmerName.substring(startIdx, endIdx))
                }
                append(item.farmerName.substring(endIdx))
            }
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isArchived) Color(0xFF131B2E) else Color(0xFF1E293B)
        ),
        border = BorderStroke(
            width = if (isArchived) 1.dp else 1.2.dp,
            color = if (isArchived) Color(0xFF334155).copy(alpha = 0.6f) else Color(0xFF334155)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("procurement_card_${item.id}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Header: Token, Crop Badge, Archive Badge, Payment Status & Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Token Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(accentColor.copy(alpha = 0.15f))
                            .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = item.tokenNo,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp
                            ),
                            color = accentColor
                        )
                    }

                    // Crop Type Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF0F172A))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = item.cropType,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    // Archived Status Tag
                    if (isArchived) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF78350F).copy(alpha = 0.4f))
                                .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "ARCHIVED",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFFBBF24)
                            )
                        }
                    }

                    // Payment Status Pill (Clickable)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (item.paymentStatus == "PAID") Color(0xFF10B981).copy(alpha = 0.2f)
                                else Color(0xFFF59E0B).copy(alpha = 0.2f)
                            )
                            .clickable { onTogglePayment() }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                            .testTag("toggle_payment_btn_${item.id}")
                    ) {
                        Text(
                            text = item.paymentStatus,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (item.paymentStatus == "PAID") Color(0xFF34D399) else Color(0xFFFBBF24)
                        )
                    }
                }

                // Date timestamp
                Text(
                    text = dateFormat.format(Date(item.createdAt)),
                    fontSize = 10.sp,
                    color = Color(0xFF64748B)
                )
            }

            // 2. Farmer & Vehicle Details with Highlight
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = if (searchQuery.isNotEmpty() && item.farmerName.contains(searchQuery.trim(), ignoreCase = true)) Color(0xFFFBBF24) else accentColor,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = highlightedFarmerName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        if (item.isPanVerified) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "PAN Verified",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(12.dp))
                        Text(item.village, fontSize = 11.sp, color = Color(0xFF94A3B8))
                        Text("•", fontSize = 11.sp, color = Color(0xFF475569))
                        Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(12.dp))
                        Text(item.mobileNumber, fontSize = 11.sp, color = Color(0xFF94A3B8))
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(12.dp))
                        Text(item.vehicleNumber, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFCBD5E1))
                        Text("•", fontSize = 11.sp, color = Color(0xFF475569))
                        Text("Silo: ${item.godownAssigned}", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    }
                }

                // Financial Total
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = inrFormat.format(item.totalAmount),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = Color(0xFF10B981)
                    )
                    Text(
                        text = "@ ₹${item.ratePerQuintal.toInt()} / Qtl",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                    if (item.applyMandiCess || item.enableTds194q) {
                        Text(
                            text = "Tax/Cess Deducted",
                            fontSize = 9.sp,
                            color = Color(0xFFF59E0B)
                        )
                    }
                }
            }

            // 3. Logistics & Quality Specs Card (Weights & Moisture)
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = BorderStroke(1.dp, Color(0xFF334155).copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Weighbridge Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            Column {
                                Text("GROSS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                                Text("${item.grossWeightKg.toInt()} kg", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                            }
                            Column {
                                Text("TARE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                                Text("${item.tareWeightKg.toInt()} kg", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                            }
                            Column {
                                Text("NET WEIGHT", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = accentColor)
                                Text(
                                    text = "${item.netWeightKg.toInt()} kg (${String.format(Locale.US, "%.2f", item.netWeightKg / 100.0)} Qtl)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        }

                        // Bag count
                        if (item.bagCount > 0) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF1E293B))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${item.bagCount} bags",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = Color(0xFF1E293B), thickness = 0.5.dp)

                    // Moisture & Quality Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Opacity,
                                contentDescription = "Moisture",
                                tint = moistureColor,
                                modifier = Modifier.size(13.dp)
                            )
                            Text("Moisture: ", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Text(
                                text = "${item.moisturePercentage}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = moistureColor
                            )
                            Text(
                                text = "(${item.qualityGrade.replace('_', ' ')})",
                                fontSize = 10.sp,
                                color = Color(0xFF64748B)
                            )
                        }

                        Text(
                            text = if (item.moisturePercentage <= 12.0) "Prime Dry"
                            else if (item.moisturePercentage <= 14.0) "Standard Safe"
                            else "Requires Drying",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = moistureColor
                        )
                    }
                }
            }

            HorizontalDivider(color = Color(0xFF334155).copy(alpha = 0.4f), thickness = 0.5.dp)

            // 4. Action Toolbar: WhatsApp, Thermal Print, PDF, Download, Edit, Archive, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left group: Communication & Printing
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // WhatsApp Share
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF25D366).copy(alpha = 0.15f))
                            .border(1.dp, Color(0xFF25D366).copy(alpha = 0.3f), CircleShape)
                            .clickable { onOpenWhatsApp() }
                            .testTag("whatsapp_btn_${item.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "WhatsApp Receipt",
                            tint = Color(0xFF25D366),
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    // Thermal Print
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF3B82F6).copy(alpha = 0.15f))
                            .border(1.dp, Color(0xFF3B82F6).copy(alpha = 0.3f), CircleShape)
                            .clickable { onPrintReceipt() }
                            .testTag("thermal_print_btn_${item.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Print,
                            contentDescription = "Print Thermal Receipt",
                            tint = Color(0xFF60A5FA),
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    // View PDF
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444).copy(alpha = 0.15f))
                            .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f), CircleShape)
                            .clickable { onOpenPdf() }
                            .testTag("view_pdf_btn_${item.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "View PDF",
                            tint = Color(0xFFF87171),
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    // Download PDF
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF8B5CF6).copy(alpha = 0.15f))
                            .border(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.3f), CircleShape)
                            .clickable { onDownloadPdf() }
                            .testTag("download_pdf_btn_${item.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Download PDF",
                            tint = Color(0xFFA78BFA),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }

                // Right group: Edit, Archive / Unarchive, Delete
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Edit Button
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF38BDF8).copy(alpha = 0.15f))
                            .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.3f), CircleShape)
                            .clickable { onEdit() }
                            .testTag("edit_procurement_btn_${item.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Procurement",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    // Archive / Unarchive Button
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(
                                if (isArchived) Color(0xFFF59E0B).copy(alpha = 0.2f)
                                else Color(0xFF64748B).copy(alpha = 0.15f)
                            )
                            .border(
                                1.dp,
                                if (isArchived) Color(0xFFF59E0B).copy(alpha = 0.4f)
                                else Color(0xFF64748B).copy(alpha = 0.3f),
                                CircleShape
                            )
                            .clickable { onArchiveToggle() }
                            .testTag("archive_procurement_btn_${item.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isArchived) Icons.Default.Unarchive else Icons.Default.Archive,
                            contentDescription = if (isArchived) "Unarchive Record" else "Archive Record",
                            tint = if (isArchived) Color(0xFFFBBF24) else Color(0xFFCBD5E1),
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    // Delete Button
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444).copy(alpha = 0.12f))
                            .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f), CircleShape)
                            .clickable { onDeleteClick() }
                            .testTag("delete_procurement_btn_${item.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Procurement",
                            tint = Color(0xFFF87171),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }
}
