package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CropType
import com.example.data.model.ExpenseEntryEntity
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Dedicated Expense Management Screen.
 * Displays all live manual operational expense vouchers:
 * - Labor
 * - Bags (Entered PER TRUCK LOADING)
 * - Transport / Freight
 * - Miscellaneous
 */
@Composable
fun ExpenseManagementScreen(
    isUnlocked: Boolean = false,
    onUnlockSuccess: () -> Unit = {},
    activeCrop: CropType,
    expenses: List<ExpenseEntryEntity>,
    onOpenAddExpense: () -> Unit,
    onDeleteExpense: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedAccent by animateColorAsState(
        targetValue = activeCrop.primaryColor,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "expense_screen_accent"
    )

    if (!isUnlocked) {
        com.example.ui.components.PinLockScreen(
            activeCrop = activeCrop,
            onUnlockSuccess = onUnlockSuccess
        )
        return
    }

    val inrFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.ENGLISH)

    var selectedFilter by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("All") }
    val filters = listOf("All", "Logistics", "Labor", "Interest", "Bags", "Other")
    
    val filteredExpenses = androidx.compose.runtime.remember(expenses, selectedFilter) {
        when (selectedFilter) {
            "Logistics" -> expenses.filter { it.transportCost > 0 }
            "Labor" -> expenses.filter { it.laborCost > 0 }
            "Interest" -> expenses.filter { it.miscDescription.contains("Interest", ignoreCase = true) }
            "Bags" -> expenses.filter { it.bagsCost > 0 }
            "Other" -> expenses.filter { it.miscCost > 0 && !it.miscDescription.contains("Interest", ignoreCase = true) }
            else -> expenses
        }
    }

    val totalLabor = filteredExpenses.sumOf { it.laborCost }
    val totalBags = filteredExpenses.sumOf { it.bagsCost }
    val totalTransport = filteredExpenses.sumOf { it.transportCost }
    val totalMisc = filteredExpenses.sumOf { it.miscCost }
    val grandTotal = totalLabor + totalBags + totalTransport + totalMisc

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Header Banner & Action Button
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, animatedAccent.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Manual Expense Management",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp
                                ),
                                color = Color.White
                            )
                            Text(
                                text = "Fluctuating daily operational costs per transaction",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = Color(0xFF94A3B8)
                            )
                        }

                        Button(
                            onClick = onOpenAddExpense,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = animatedAccent,
                                contentColor = Color.Black
                            )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Expense", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Filter Interface
                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(filters) { filter ->
                            androidx.compose.material3.FilterChip(
                                selected = selectedFilter == filter,
                                onClick = { selectedFilter = filter },
                                label = { Text(filter) },
                                colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(selectedContainerColor = animatedAccent)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Aggregate Metrics 4-Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ExpenseMetricCard(
                            label = "Labor (Hamali)",
                            amount = totalLabor,
                            color = animatedAccent,
                            modifier = Modifier.weight(1f)
                        )
                        ExpenseMetricCard(
                            label = "Bags (Per Truck)",
                            amount = totalBags,
                            color = Color(0xFF38BDF8),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ExpenseMetricCard(
                            label = "Transport / Freight",
                            amount = totalTransport,
                            color = Color(0xFFF59E0B),
                            modifier = Modifier.weight(1f)
                        )
                        ExpenseMetricCard(
                            label = "Misc / Handling",
                            amount = totalMisc,
                            color = Color(0xFFA855F7),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Grand Total Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F172A))
                            .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Logged Expenses:",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFE2E8F0)
                            )
                            Text(
                                text = inrFormat.format(grandTotal),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp
                                ),
                                color = animatedAccent
                            )
                        }
                    }
                }
            }
        }

        // 2. Section Header
        item {
            Text(
                text = "EXPENSE VOUCHER RECORDS (${expenses.size})",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = Color(0xFF94A3B8)
            )
        }

        // 3. Expense List
        if (expenses.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1E293B))
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No manual expenses logged yet",
                            color = Color(0xFF94A3B8),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Tap 'New Expense' to record daily labor, truck bags, or transport costs.",
                            color = Color(0xFF64748B),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        } else {
            items(expenses, key = { it.id }) { item ->
                ExpenseVoucherCard(
                    item = item,
                    accentColor = animatedAccent,
                    onDelete = { onDeleteExpense(item.id) },
                    dateFormat = dateFormat,
                    inrFormat = inrFormat
                )
            }
        }
    }
}

@Composable
private fun ExpenseMetricCard(
    label: String,
    amount: Double,
    color: Color,
    modifier: Modifier = Modifier
) {

    val inrFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0F172A))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = Color(0xFF94A3B8)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = inrFormat.format(amount),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                ),
                color = color
            )
        }
    }
}

@Composable
private fun ExpenseVoucherCard(
    item: ExpenseEntryEntity,
    accentColor: Color,
    onDelete: () -> Unit,
    dateFormat: SimpleDateFormat,
    inrFormat: NumberFormat
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
        modifier = Modifier.fillMaxWidth()
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
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(accentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalShipping,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = item.truckOrBatchRef,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            ),
                            color = Color.White
                        )
                        Text(
                            text = "${item.expenseNo} • ${dateFormat.format(Date(item.timestamp))}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = inrFormat.format(item.totalExpense),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        ),
                        color = accentColor
                    )
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFEF4444).copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Breakdown Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MiniPill(label = "Labor", amount = item.laborCost, color = accentColor, modifier = Modifier.weight(1f))
                MiniPill(label = "Bags (Truck)", amount = item.bagsCost, color = Color(0xFF38BDF8), modifier = Modifier.weight(1f))
                MiniPill(label = "Freight", amount = item.transportCost, color = Color(0xFFF59E0B), modifier = Modifier.weight(1f))
                MiniPill(label = "Misc", amount = item.miscCost, color = Color(0xFFA855F7), modifier = Modifier.weight(1f))
            }

            if (item.paidToOrParty.isNotBlank() || item.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Paid to: ${item.paidToOrParty} ${if (item.notes.isNotBlank()) "• " + item.notes else ""}",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = Color(0xFF64748B)
                )
            }
        }
    }
}

@Composable
private fun MiniPill(
    label: String,
    amount: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    val inrFormat = NumberFormat.getNumberInstance(Locale("en", "IN"))
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0F172A))
            .padding(vertical = 4.dp, horizontal = 6.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(label, fontSize = 9.sp, color = Color(0xFF94A3B8), maxLines = 1)
            Text("₹${inrFormat.format(amount.toInt())}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}
