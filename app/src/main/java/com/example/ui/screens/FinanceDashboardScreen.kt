package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceDashboardScreen(
    isUnlocked: Boolean = false,
    onUnlockSuccess: () -> Unit = {},
    activeCrop: CropType,
    firmProfile: FirmProfile,
    allTrades: List<TradeBookingEntity>,
    allProcurements: List<ProcurementEntity>,
    allExpenses: List<ExpenseEntryEntity>,
    allLedgers: List<VendorLedgerEntity>,
    onReceivePayment: (amount: Double, source: String, notes: String) -> Unit,
    onLogInterestExpense: (amount: Double, notes: String) -> Unit,
    onExportCaReport: () -> Unit
) {
    if (!isUnlocked) {
        com.example.ui.components.PinLockScreen(
            activeCrop = activeCrop,
            onUnlockSuccess = onUnlockSuccess
        )
        return
    }

    val inrFormat = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }
    var showReceivePaymentDialog by remember { mutableStateOf(false) }
    var showInterestDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Math Calculations
    val incomingPayments = allLedgers
        .filter { it.transactionType == com.example.data.model.TransactionType.PAYMENT_RECEIVED.name }
        .sumOf { it.amount }
        
    val outgoingFarmerPayouts = allProcurements
        .filter { it.paymentStatus == PaymentStatus.PAID.name || it.paymentStatus == PaymentStatus.PROCESSING.name }
        .sumOf { it.totalAmount }
        
    val operationalExpenses = allExpenses.sumOf { it.totalExpense }
    
    val liquidAssets = firmProfile.initialCapital + incomingPayments - outgoingFarmerPayouts - operationalExpenses

    val now = System.currentTimeMillis()
    val threeDaysLater = now + (3 * 24 * 60 * 60 * 1000L)
    
    val upcomingPdcLiability = allLedgers
        .filter { it.pdcStatus == PdcStatus.PENDING_MATURITY.name && it.chequeMaturityDate <= threeDaysLater }
        .sumOf { it.amount }

    // True Net Margin
    val grossRevenue = allTrades.sumOf { it.totalRevenue }
    val farmerProcurement = allTrades.sumOf { it.totalProcurementCost }
    val operationsCost = allTrades.sumOf { it.totalOverhead }
    val financingCost = allExpenses.filter { it.miscDescription == "Capital Loan Interest" }.sumOf { it.totalExpense }
    val trueNetMargin = grossRevenue - farmerProcurement - operationsCost - financingCost

    // Validate PDC Warning
    val isPdcDanger = upcomingPdcLiability > liquidAssets

    LaunchedEffect(isPdcDanger) {
        if (isPdcDanger) {
            snackbarHostState.showSnackbar(
                message = "URGENT: Liquid assets insufficient for upcoming cheque clearances.",
                duration = SnackbarDuration.Indefinite
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFF0F172A)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Financial Command Center",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            )

            // 1. Liquid Assets Dashboard
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.95f)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF10B981).copy(alpha = 0.4f), RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Liquid Assets / Bank Balance", color = Color.LightGray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = inrFormat.format(liquidAssets),
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF10B981) // Neon Green
                        )
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { showReceivePaymentDialog = true },
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981), contentColor = Color.Black),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Receive Payment", fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { showInterestDialog = true },
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155), contentColor = Color.White),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Log Loan Interest", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 2. PDC Bounce Protection Widget
            Card(
                colors = CardDefaults.cardColors(containerColor = if (isPdcDanger) Color(0xFF450a0a) else Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        2.dp,
                        if (isPdcDanger) Color.Red.copy(alpha = 0.8f) else Color(0xFF334155),
                        RoundedCornerShape(16.dp)
                    )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isPdcDanger) Icons.Default.Warning else Icons.Default.AccountBalance,
                        contentDescription = null,
                        tint = if (isPdcDanger) Color.Red else activeCrop.accentColor,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Upcoming PDC Clearances (3 Days)", color = Color.LightGray, fontSize = 13.sp)
                        Text(
                            text = inrFormat.format(upcomingPdcLiability),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isPdcDanger) Color.Red else Color.White
                            )
                        )
                    }
                }
            }

            // 4. True Net Margin Breakdown
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("P&L Breakdown", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Button(
                        onClick = onExportCaReport,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6), contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generate Annual CA Report (Excel)", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Gross Corporate Revenue", color = Color.LightGray)
                        Text(inrFormat.format(grossRevenue), color = Color.White)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("(-) Farmer Procurement", color = Color.LightGray)
                        Text(inrFormat.format(farmerProcurement), color = Color.White)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("(-) Operations (Labor/Bags)", color = Color.LightGray)
                        Text(inrFormat.format(operationsCost), color = Color.White)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("(-) Financing Cost (Interest)", color = Color.LightGray)
                        Text(inrFormat.format(financingCost), color = Color.White)
                    }
                    Divider(color = Color(0xFF334155))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("True Net Margin", color = Color.White, fontWeight = FontWeight.Bold)
                        Text(
                            text = inrFormat.format(trueNetMargin),
                            fontWeight = FontWeight.Black,
                            color = if (trueNetMargin >= 0) Color(0xFF10B981) else Color.Red
                        )
                    }
                }
            }
        }
    }

    if (showReceivePaymentDialog) {
        var amt by remember { mutableStateOf("") }
        var src by remember { mutableStateOf("") }
        var note by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { showReceivePaymentDialog = false },
            title = { Text("Receive Payment") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = amt,
                        onValueChange = { amt = it },
                        label = { Text("Amount (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = src,
                        onValueChange = { src = it },
                        label = { Text("Source (Corporate Buyer)") }
                    )
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Notes / UTR") }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val a = amt.toDoubleOrNull() ?: 0.0
                    if (a > 0) {
                        onReceivePayment(a, src, note)
                        showReceivePaymentDialog = false
                    }
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showReceivePaymentDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showInterestDialog) {
        var amt by remember { mutableStateOf("") }
        var note by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { showInterestDialog = false },
            title = { Text("Log Loan Interest") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = amt,
                        onValueChange = { amt = it },
                        label = { Text("Interest Amount (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Month / Details") }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val a = amt.toDoubleOrNull() ?: 0.0
                    if (a > 0) {
                        onLogInterestExpense(a, note)
                        showInterestDialog = false
                    }
                }) { Text("Deduct Asset") }
            },
            dismissButton = {
                TextButton(onClick = { showInterestDialog = false }) { Text("Cancel") }
            }
        )
    }
}
