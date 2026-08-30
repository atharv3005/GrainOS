package com.example

import android.os.Bundle
import com.example.R
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CropType
import com.example.data.model.FirmProfile
import com.example.security.DeviceSecurityReport
import com.example.ui.components.ArchitectureSpecDialog
import com.example.ui.components.Dynamic3DGrainBackground
import com.example.ui.components.ExpenseConfigDialog
import com.example.ui.components.FirmLoginDialog
import com.example.ui.components.GrainOSBrandLogo
import com.example.ui.components.ManualExpenseEntryDialog
import com.example.ui.components.OnboardingSetupDialog
import com.example.ui.components.PdfReceiptDialog
import com.example.ui.components.SecurityStatusDialog
import com.example.ui.components.SettleDispatchDialog
import com.example.ui.components.TradeBookingDialog
import com.example.ui.components.TruckRejectionDialog
import com.example.ui.components.WhatsAppReceiptDialog
import com.example.ui.screens.AiAdvisorScreen
import com.example.ui.screens.BigDashboardScreen
import com.example.ui.screens.ExpenseManagementScreen
import com.example.ui.screens.FarmerReceiptsScreen
import com.example.ui.screens.FinancialPnLScreen
import com.example.ui.screens.FinanceDashboardScreen
import com.example.ui.screens.GateEntryScreen
import com.example.ui.screens.GodownStockScreen
import com.example.ui.screens.InboundProcurementScreen
import com.example.ui.screens.LedgerScreen
import com.example.ui.screens.OutboundDispatchScreen
import com.example.ui.theme.GrainWmsTheme
import com.example.ui.viewmodel.GrainWmsViewModel

enum class NavigationTab(@androidx.annotation.StringRes val titleResId: Int, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    DASHBOARD(R.string.nav_dashboard, Icons.Default.Dashboard),
    PNL(R.string.nav_pnl, Icons.Default.TrendingUp),
    FINANCE(R.string.nav_finance, Icons.Default.AccountBalanceWallet),
    EXPENSES(R.string.nav_expenses, Icons.Default.Receipt),
    INBOUND(R.string.nav_inbound, Icons.Default.Scale),
    DISPATCH(R.string.nav_dispatch, Icons.Default.LocalShipping),
    GODOWNS(R.string.nav_godowns, Icons.Default.Warehouse),
    RECEIPTS(R.string.nav_receipts, Icons.Default.ReceiptLong),
    LEDGER(R.string.nav_ledger, Icons.Default.ListAlt),
    AI_ADVISOR(R.string.nav_ai_advisor, Icons.Default.AutoAwesome)
}

class MainActivity : ComponentActivity() {
    private val viewModel: GrainWmsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = getSharedPreferences("crash_prefs", android.content.Context.MODE_PRIVATE)
        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            val stackTrace = android.util.Log.getStackTraceString(throwable)
            prefs.edit().putString("last_crash", stackTrace).commit()
            kotlin.system.exitProcess(1)
        }
        
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val lastCrash = prefs.getString("last_crash", null)
        if (lastCrash != null) {
            setContent {
                androidx.compose.material3.MaterialTheme {
                    androidx.compose.foundation.layout.Box(modifier = androidx.compose.ui.Modifier.fillMaxSize().padding(16.dp)) {
                        androidx.compose.foundation.lazy.LazyColumn {
                            item {
                                androidx.compose.material3.Text("CRASH LOG:", color = androidx.compose.ui.graphics.Color.Red)
                                androidx.compose.material3.Text(lastCrash, color = androidx.compose.ui.graphics.Color.Red, fontSize = 10.sp)
                                androidx.compose.material3.Button(onClick = { prefs.edit().clear().apply() }) {
                                    androidx.compose.material3.Text("Clear Crash Log")
                                }
                            }
                        }
                    }
                }
            }
            return
        }
        
        setContent {
            val activeCrop by viewModel.activeCrop.collectAsState()

            GrainWmsTheme(darkTheme = true, activeCrop = activeCrop) {
                GrainOSApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun GrainOSApp(viewModel: GrainWmsViewModel) {
    val activeCrop by viewModel.activeCrop.collectAsState()
    val firmProfile by viewModel.firmProfile.collectAsState()
    val securityReport by viewModel.securityReport.collectAsState()

    // Dialog state collectors
    val showOnboarding by viewModel.showOnboardingDialog.collectAsState()
    val showFirmLogin by viewModel.showFirmLoginDialog.collectAsState()
    val showExpenseConfig by viewModel.showExpenseConfigDialog.collectAsState()
    val showTradeBooking by viewModel.showTradeBookingDialog.collectAsState()
    val showSecurity by viewModel.showSecurityDialog.collectAsState()
    val showArchitecture by viewModel.showArchitectureDialog.collectAsState()
    val showExpenseEntry by viewModel.showExpenseEntryDialog.collectAsState()
    val showTruckRejection by viewModel.showTruckRejectionDialog.collectAsState()
    val showSettleDispatch by viewModel.showSettleDispatchDialog.collectAsState()
    val selectedDispatchForSettlement by viewModel.selectedDispatchForSettlement.collectAsState()

    val rejTruckNo by viewModel.rejectionTruckNumber.collectAsState()
    val rejBuyer by viewModel.rejectionBuyerName.collectAsState()
    val rejWeight by viewModel.rejectionWeightKg.collectAsState()

    val isStreamingActive by viewModel.isStreamingActive.collectAsState()
    val allProcurements by viewModel.allProcurements.collectAsState()
    val allGodowns by viewModel.allGodowns.collectAsState()
    val allStorageIntakes by viewModel.allStorageIntakes.collectAsState()
    val allDispatches by viewModel.allDispatches.collectAsState()
    val recentTelemetry by viewModel.recentTelemetry.collectAsState()
    val allTrades by viewModel.allTrades.collectAsState()
    val allExpenses by viewModel.allExpenses.collectAsState()
    val allRejections by viewModel.allRejections.collectAsState()
    val allLedgers by viewModel.allLedgers.collectAsState()
    val liveGodownStockLedger by viewModel.liveGodownStockLedger.collectAsState()

    val selectedWhatsApp by viewModel.selectedWhatsAppReceipt.collectAsState()
    val isWhatsAppEntryOnly by viewModel.isWhatsAppEntryOnly.collectAsState()
    val selectedPdf by viewModel.selectedPdfReceipt.collectAsState()

    val aiResult by viewModel.aiAnalysisResult.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()
    val isSubmittingDispatch by viewModel.isSubmittingDispatch.collectAsState()

    var currentTab by remember { mutableStateOf(NavigationTab.DASHBOARD) }

    // Dynamic Language Switcher State
    var isFinanceUnlocked by remember { mutableStateOf(false) }

    var appLanguage by remember { mutableStateOf("en") }
    val context = androidx.compose.ui.platform.LocalContext.current
    val localizedContext = remember(appLanguage) { com.example.util.LocaleHelper.getLocalizedContextWrapper(context, appLanguage) }
    val configuration = remember(appLanguage) { android.content.res.Configuration(localizedContext.resources.configuration) }

    androidx.compose.runtime.CompositionLocalProvider(
        androidx.compose.ui.platform.LocalContext provides localizedContext,
        androidx.compose.ui.platform.LocalConfiguration provides configuration
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 1. Dynamic 3D Grain Animated Canvas (shifts geometry & palette on crop switch!)
            Dynamic3DGrainBackground(activeCrop = activeCrop)

            // 2. Main Scaffold Layer
            Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                GrainOSTopBar(
                    activeCrop = activeCrop,
                    firmProfile = firmProfile,
                    securityReport = securityReport,
                    isStreamingActive = isStreamingActive,
                    appLanguage = appLanguage,
                    onLanguageChanged = { appLanguage = it },
                    onOpenSecurity = { viewModel.setShowSecurityDialog(true) },
                    onOpenFirmLogin = { viewModel.setShowFirmLoginDialog(true) },
                    onOpenArchitecture = { viewModel.setShowArchitectureDialog(true) },
                    onOpenAiAdvisor = { currentTab = NavigationTab.AI_ADVISOR }
                )
            },
            bottomBar = {
                GrainOSBottomNavBar(
                    currentTab = currentTab,
                    onTabSelected = { currentTab = it },
                    accentColor = activeCrop.primaryColor
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    NavigationTab.DASHBOARD -> {
                        BigDashboardScreen(
                            activeCrop = activeCrop,
                            firmProfile = firmProfile,
                            onCropSelected = { viewModel.setCrop(it) },
                            procurements = allProcurements,
                            godowns = allGodowns,
                            liveGodownStockLedger = liveGodownStockLedger,
                            trades = allTrades,
                            telemetryLogs = recentTelemetry,
                            isStreamingActive = isStreamingActive,
                            onToggleStreaming = { viewModel.toggleStreaming() },
                            onInjectTestPacket = { viewModel.injectTestTelemetryPulse() },
                            onStartNewInbound = { currentTab = NavigationTab.INBOUND },
                            onOpenWhatsAppReceipt = { viewModel.openWhatsAppReceipt(it, false) },
                            onOpenPdfReceipt = { viewModel.openPdfReceipt(it) },
                            onOpenAiAdvisor = { currentTab = NavigationTab.AI_ADVISOR },
                            onOpenArchitecture = { viewModel.setShowArchitectureDialog(true) },
                            onOpenFirmLogin = { viewModel.setShowFirmLoginDialog(true) },
                            onOpenSecurity = { viewModel.setShowSecurityDialog(true) },
                            onNavigateToPnL = { currentTab = NavigationTab.PNL },
                            onOpenBookTrade = { viewModel.setShowTradeBookingDialog(true) },
                            onOpenExpenses = { currentTab = NavigationTab.EXPENSES }
                            , smartInsight = viewModel.smartInsight.collectAsState().value,
                            onRefreshInsight = { viewModel.refreshSmartInsight(allProcurements, allGodowns) }
                        )
                    }
                    NavigationTab.PNL -> {
                        FinancialPnLScreen(
                            activeCrop = activeCrop,
                            firmProfile = firmProfile,
                            trades = allTrades,
                            expenses = allExpenses,
                            rejections = allRejections,
                            dispatches = allDispatches,
                            onOpenBookTrade = { viewModel.setShowTradeBookingDialog(true) },
                            onOpenAddExpense = { viewModel.setShowExpenseEntryDialog(true) },
                            onOpenRecordRejection = { viewModel.openTruckRejectionDialog() },
                            onUpdateTradeStatus = { id, status -> viewModel.updateTradeStatus(id, status) },
                            onDeleteTrade = { id -> viewModel.deleteTrade(id) },
                            onDeleteRejection = { id -> viewModel.deleteTruckRejection(id) }
                        )
                    }
                    NavigationTab.FINANCE -> {
                        FinanceDashboardScreen(
                            isUnlocked = isFinanceUnlocked,
                            onUnlockSuccess = { isFinanceUnlocked = true },
                            activeCrop = activeCrop,
                            firmProfile = firmProfile,
                            allTrades = allTrades,
                            allProcurements = allProcurements,
                            allExpenses = allExpenses,
                            allLedgers = allLedgers,
                            onReceivePayment = { amt, src, notes -> viewModel.receiveCorporatePayment(amt, src, notes) },
                            onLogInterestExpense = { amt, notes -> viewModel.logInterestExpense(amt, notes) },
                            onExportCaReport = { viewModel.exportCaReportToExcel(context) }
                        )
                    }
                    NavigationTab.EXPENSES -> {
                        ExpenseManagementScreen(
                            isUnlocked = isFinanceUnlocked,
                            onUnlockSuccess = { isFinanceUnlocked = true },
                            activeCrop = activeCrop,
                            expenses = allExpenses,
                            onOpenAddExpense = { viewModel.setShowExpenseEntryDialog(true) },
                            onDeleteExpense = { viewModel.deleteExpense(it) }
                        )
                    }
                    NavigationTab.INBOUND -> {
                        GateEntryScreen(
                            viewModel = viewModel,
                            
                            activeCrop = activeCrop
                        )
                    }
                    NavigationTab.DISPATCH -> {
                        OutboundDispatchScreen(
                            dispatches = allDispatches,
                            liveGodownStockLedger = liveGodownStockLedger,
                            onCreateDispatch = { buyer, dest, veh, crop, godown, tare, gross, rate, onComp -> viewModel.createOutboundDispatch(buyerName = buyer, destination = dest, vehicleNumber = veh, cropType = crop.name, godownSource = godown, tareWeightKg = tare, grossWeightKg = gross, ratePerQuintal = rate, onComplete = { onComp() }) },
                            
                            activeCrop = activeCrop,
                            godowns = allGodowns
                        )
                    }
                    NavigationTab.GODOWNS -> {
                        GodownStockScreen(
                            godowns = allGodowns,
                            activeCrop = activeCrop,
                            liveGodownStockLedger = liveGodownStockLedger,
                            getEstimatedPhysicalStock = { viewModel.getEstimatedPhysicalStock(it) },
                            onEndOfSeasonAudit = { viewModel.endOfSeasonZeroOut(it) },
                            storageIntakes = allStorageIntakes,
                            onDeleteIntake = { viewModel.deleteStorageIntake(it) }
                        )
                    }
                    NavigationTab.RECEIPTS -> {
                        FarmerReceiptsScreen(
                            procurements = allProcurements,
                            activeCrop = activeCrop,
                            onOpenWhatsApp = { viewModel.openWhatsAppReceipt(it, false) },
                            onOpenPdf = { viewModel.openPdfReceipt(it) },
                            onDownloadPdf = { com.example.data.export.PdfExporter.downloadProfessionalPdf(context, it, firmProfile) },
                            onPrintReceipt = { com.example.util.ThermalPrinterHelper.printReceipt(context, it, firmProfile.firmName) },
                            onTogglePaymentStatus = { id, status ->
                                viewModel.updatePaymentStatus(id, status)
                            },
                            onEdit = { viewModel.updateProcurement(it) },
                            onDelete = { viewModel.deleteProcurement(it.id) },
                            onToggleArchive = { viewModel.toggleArchive(it) }
                        )
                    }
                    NavigationTab.AI_ADVISOR -> {
                        AiAdvisorScreen(
                            activeCrop = activeCrop,
                            aiResult = aiResult,
                            isLoading = isAiLoading,
                            onRunAnalysis = { crop, moist, temp, godown, name ->
                                viewModel.runGeminiAnalysis(crop, moist, temp, godown, name)
                            }
                        )
                    }
                    NavigationTab.LEDGER -> {
                        LedgerScreen(
                            viewModel = viewModel
                        )
                    }
                }
            }
        }

        // Active Dialog Overlays
        if (showOnboarding) {
            OnboardingSetupDialog(
                currentProfile = firmProfile,
                onCompleteSetup = { name, cap, crop, facs ->
                    viewModel.completeOnboarding(
                        firmName = name,
                        capacityMt = cap,
                        mainCrop = crop,
                        facilities = facs
                    )
                },
                onDismiss = { viewModel.setShowOnboardingDialog(false) }
            )
        }

        if (showExpenseConfig) {
            ExpenseConfigDialog(
                firmProfile = firmProfile,
                activeCrop = activeCrop,
                onSaveExpenses = { labor, bag, transport, brokerage ->
                    viewModel.updateExpenseDefaults(labor, bag, transport, brokerage)
                },
                onDismiss = { viewModel.setShowExpenseConfigDialog(false) }
            )
        }

        if (showTradeBooking) {
            TradeBookingDialog(
                firmProfile = firmProfile,
                activeCrop = activeCrop,
                onBookTrade = { crop, broker, qty, bookedPrice, farmerPrice, notes ->
                    viewModel.bookTrade(
                        cropType = crop,
                        brokerOrBuyerName = broker,
                        quantityTons = qty,
                        bookedPricePerQuintal = bookedPrice,
                        farmerPurchasePricePerQuintal = farmerPrice,
                        notes = notes
                    )
                },
                onDismiss = { viewModel.setShowTradeBookingDialog(false) }
            )
        }

        if (showExpenseEntry) {
            ManualExpenseEntryDialog(
                activeCrop = activeCrop,
                onSaveExpense = { truck, crop, labor, bags, trans, misc, party, notes ->
                    viewModel.recordManualExpense(
                        truckOrBatchRef = truck,
                        cropType = crop,
                        laborCost = labor,
                        bagsCost = bags,
                        transportCost = trans,
                        miscCost = misc,
                        paidToOrParty = party,
                        notes = notes
                    )
                },
                onDismiss = { viewModel.setShowExpenseEntryDialog(false) }
            )
        }

        if (showTruckRejection) {
            TruckRejectionDialog(
                activeCrop = activeCrop,
                initialTruckNo = rejTruckNo,
                initialBuyer = rejBuyer,
                initialWeightKg = rejWeight,
                onRecordRejection = { truck, buyer, crop, wt, reason, loss, pen, ded, action, notes ->
                    viewModel.recordTruckRejection(
                        truckNumber = truck,
                        buyerOrCompany = buyer,
                        cropType = crop,
                        dispatchedWeightKg = wt,
                        rejectionReason = reason,
                        transportLoss = loss,
                        penaltiesDemurrage = pen,
                        originalLoadingLaborCost = 1500.0,
                        qualitySalvageDeduction = ded,
                        salvageAction = action,
                        notes = notes
                    )
                },
                onDismiss = { viewModel.closeTruckRejectionDialog() }
            )
        }

        if (showSettleDispatch && selectedDispatchForSettlement != null) {
            SettleDispatchDialog(
                dispatch = selectedDispatchForSettlement!!,
                onSettle = { unlWt, pen, frt, lbr, bag, misc, brk, brkRt ->
                    viewModel.settleUnloadedDispatch(
                        dispatchId = selectedDispatchForSettlement!!.id,
                        companyUnloadedWeightKg = unlWt,
                        qualityPenalty = pen,
                        freightCost = frt,
                        laborCost = lbr,
                        bagCost = bag,
                        miscCost = misc,
                        brokerName = brk,
                        brokerageRatePerQtl = brkRt
                    )
                },
                onDismiss = { viewModel.closeSettleDispatchDialog() }
            )
        }

        if (showSecurity) {
            SecurityStatusDialog(
                report = securityReport,
                onDismiss = { viewModel.setShowSecurityDialog(false) }
            )
        }

        if (showFirmLogin) {
            FirmLoginDialog(
                currentProfile = firmProfile,
                activeCrop = activeCrop,
                onSaveFacilities = { facs -> viewModel.addStorageFacilities(facs) },
                onSaveProfile = { newProfile ->
                    viewModel.updateFirmProfile(newProfile)
                },
                onDismiss = { viewModel.setShowFirmLoginDialog(false) }
            )
        }

        selectedWhatsApp?.let { item ->
            WhatsAppReceiptDialog(
                procurement = item,
                firmProfile = firmProfile,
                isEntryOnly = isWhatsAppEntryOnly,
                onDismiss = { viewModel.closeWhatsAppReceipt() }
            )
        }

        selectedPdf?.let { item ->
            PdfReceiptDialog(
                procurement = item,
                firmProfile = firmProfile,
                onDismiss = { viewModel.closePdfReceipt() }
            )
        }

        if (showArchitecture) {
            ArchitectureSpecDialog(
                onDismiss = { viewModel.setShowArchitectureDialog(false) }
            )
        }
    }
    }
}

@Composable
private fun GrainOSTopBar(
    activeCrop: CropType,
    firmProfile: FirmProfile,
    securityReport: DeviceSecurityReport,
    isStreamingActive: Boolean,
    appLanguage: String,
    onLanguageChanged: (String) -> Unit,
    onOpenSecurity: () -> Unit,
    onOpenFirmLogin: () -> Unit,
    onOpenArchitecture: () -> Unit,
    onOpenAiAdvisor: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = Color(0xFF0F172A).copy(alpha = 0.94f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Brand & Location (Clickable to switch firm!)
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onOpenFirmLogin() }
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GrainOSBrandLogo(
                    size = 36.dp,
                    activeCrop = activeCrop,
                    showText = false
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "GrainOS • ${firmProfile.firmName}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp
                            ),
                            color = Color.White,
                            maxLines = 1
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isStreamingActive) Color(0xFF10B981) else Color(0xFFEF4444))
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "${firmProfile.location} • ${firmProfile.totalCapacityMt.toInt()} MT",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = Color(0xFF94A3B8),
                            maxLines = 1
                        )
                    }
                }
            }

            // Action Icons: Language + Security Status + Firm Config + Architecture + AI Advisor
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                
                Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Color(0xFF1E293B)).clickable {
                    val nextLang = when (appLanguage) {
                        "en" -> "hi"
                        "hi" -> "mr"
                        else -> "en"
                    }
                    onLanguageChanged(nextLang)
                }.padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text(
                        text = when (appLanguage) { "en" -> "EN"; "hi" -> "HI"; else -> "MR" },
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = onOpenSecurity,
                    modifier = Modifier.size(34.dp).testTag("topbar_security_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Security Vault",
                        tint = if (securityReport.isRootDetected) Color(0xFFF87171) else Color(0xFF34D399),
                        modifier = Modifier.size(19.dp)
                    )
                }

                IconButton(
                    onClick = onOpenFirmLogin,
                    modifier = Modifier.size(34.dp).testTag("topbar_firm_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Business,
                        contentDescription = "Firm Configuration",
                        tint = activeCrop.primaryColor,
                        modifier = Modifier.size(19.dp)
                    )
                }

                IconButton(
                    onClick = onOpenArchitecture,
                    modifier = Modifier.size(34.dp).testTag("topbar_arch_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountTree,
                        contentDescription = "Architecture Spec",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(19.dp)
                    )
                }

                IconButton(
                    onClick = onOpenAiAdvisor,
                    modifier = Modifier.size(34.dp).testTag("topbar_ai_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Agronomist",
                        tint = activeCrop.primaryColor,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun GrainOSBottomNavBar(
    currentTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit,
    accentColor: Color
) {
    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        containerColor = Color(0xFF0F172A).copy(alpha = 0.95f),
        tonalElevation = 8.dp
    ) {
        val visibleTabs = listOf(
            NavigationTab.DASHBOARD,
            NavigationTab.FINANCE,
            NavigationTab.PNL,
            NavigationTab.INBOUND,
            NavigationTab.DISPATCH,
            NavigationTab.GODOWNS,
            NavigationTab.RECEIPTS
        )

        visibleTabs.forEach { tab ->
            val isSelected = currentTab == tab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = androidx.compose.ui.res.stringResource(tab.titleResId),
                        modifier = Modifier.size(19.dp)
                    )
                },
                label = {
                    Text(
                        text = androidx.compose.ui.res.stringResource(tab.titleResId),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal
                        ),
                        maxLines = 1
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Black,
                    selectedTextColor = accentColor,
                    indicatorColor = accentColor,
                    unselectedIconColor = Color(0xFF94A3B8),
                    unselectedTextColor = Color(0xFF64748B)
                ),
                modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
            )
        }
    }
}
