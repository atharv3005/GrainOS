package com.example.ui.screens
import androidx.compose.material.icons.filled.Print

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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.data.export.CsvExporter
import com.example.data.model.CropType
import com.example.data.model.PaymentStatus
import com.example.data.model.ProcurementEntity
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var filterStatus by remember { mutableStateOf<String?>("ALL") }
    var editingProcurement by remember { mutableStateOf<ProcurementEntity?>(null) }

    val filteredList = procurements.filter { p ->
        val matchesSearch = p.farmerName.contains(searchQuery, ignoreCase = true) ||
                p.vehicleNumber.contains(searchQuery, ignoreCase = true) ||
                p.tokenNo.contains(searchQuery, ignoreCase = true) ||
                p.village.contains(searchQuery, ignoreCase = true)
        val matchesFilter = when (filterStatus) {
            "PAID" -> p.paymentStatus == "PAID"
            "PENDING" -> p.paymentStatus == "PENDING"
            else -> true
        }
        matchesSearch && matchesFilter
    }

    val inrFormat = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Search & Filter Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by farmer, vehicle, token...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = activeCrop.primaryColor) },
                modifier = Modifier.weight(1f).testTag("receipt_search_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = activeCrop.primaryColor,
                    unfocusedBorderColor = Color(0xFF334155),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E293B))
                    .border(1.dp, activeCrop.primaryColor, RoundedCornerShape(12.dp))
                    .clickable { CsvExporter.exportProcurements(context, filteredList) },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Download, contentDescription = "Export CSV", tint = activeCrop.primaryColor)
            }
        }

        // Filter Pills Row
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("ALL", "PAID", "PENDING").forEach { filter ->
                val isSel = filterStatus == filter
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSel) activeCrop.primaryColor else Color(0xFF1E293B))
                        .clickable { filterStatus = filter }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = filter,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSel) Color.Black else Color(0xFF94A3B8)
                    )
                }
            }
        }

        // List
        if (filteredList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No procurement records found.", color = Color(0xFF64748B))
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredList, key = { it.id }) { item ->
                    ReceiptCard(
                        item = item,
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
                        onDelete = { onDelete(item) },
                        inrFormat = inrFormat,
                        dateFormat = dateFormat
                    )
                }
            }
        }
    }

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
private fun ReceiptCard(
    item: ProcurementEntity,
    accentColor: Color,
    onOpenWhatsApp: () -> Unit,
    onOpenPdf: () -> Unit,
    onDownloadPdf: () -> Unit,
    onPrintReceipt: () -> Unit,
    onTogglePayment: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    inrFormat: NumberFormat,
    dateFormat: SimpleDateFormat
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Top Row: Token + Status Badge + WhatsApp/PDF Icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.tokenNo,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 11.sp),
                        color = accentColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (item.paymentStatus == "PAID") Color(0xFF10B981).copy(alpha = 0.2f)
                                else Color(0xFFF59E0B).copy(alpha = 0.2f)
                            )
                            .clickable { onTogglePayment() }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item.paymentStatus,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (item.paymentStatus == "PAID") Color(0xFF34D399) else Color(0xFFFBBF24)
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // WhatsApp Trigger
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF25D366).copy(alpha = 0.2f))
                            .clickable { onOpenWhatsApp() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = "WhatsApp", tint = Color(0xFF25D366), modifier = Modifier.size(14.dp))
                    }

                    // Print Trigger
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF3B82F6).copy(alpha = 0.2f))
                            .clickable { onPrintReceipt() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = androidx.compose.material.icons.Icons.Default.Print, contentDescription = "Print Thermal Receipt", tint = Color(0xFF3B82F6), modifier = Modifier.size(14.dp))
                    }

                    // PDF Trigger
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444).copy(alpha = 0.2f))
                            .clickable { onOpenPdf() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = "PDF", tint = Color(0xFFF87171), modifier = Modifier.size(14.dp))
                    }

                    // Download PDF Trigger
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF8B5CF6).copy(alpha = 0.2f))
                            .clickable { onDownloadPdf() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = "Download", tint = Color(0xFFA78BFA), modifier = Modifier.size(14.dp))
                    }

                    // Edit Trigger
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF3B82F6).copy(alpha = 0.2f))
                            .clickable { onEdit() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF60A5FA), modifier = Modifier.size(14.dp))
                    }

                    // Delete Trigger
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF64748B).copy(alpha = 0.2f))
                            .clickable { onDelete() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFF94A3B8), modifier = Modifier.size(14.dp))
                    }
                }
            }

            // Farmer & Vehicle
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(item.farmerName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                    Text("${item.village} • ${item.vehicleNumber}", fontSize = 11.sp, color = Color(0xFF94A3B8))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(inrFormat.format(item.totalAmount), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Color(0xFF10B981))
                    Text("₹${item.ratePerQuintal.toInt()}/qtl", fontSize = 11.sp, color = Color(0xFF94A3B8))
                }
            }

            Divider(color = Color(0xFF334155), thickness = 0.5.dp)

            // Weight metrics
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Gross: ${item.grossWeightKg.toInt()} kg", fontSize = 11.sp, color = Color(0xFF94A3B8))
                Text("Tare: ${item.tareWeightKg.toInt()} kg", fontSize = 11.sp, color = Color(0xFF94A3B8))
                Text("Net: ${item.netWeightKg.toInt()} kg", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = accentColor)
                Text("Moist: ${item.moisturePercentage}%", fontSize = 11.sp, color = Color(0xFF38BDF8))
            }
        }
    }
}
