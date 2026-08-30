package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.ExpenseEntryEntity
import com.example.data.model.GodownEntity
import com.example.data.model.InventoryReconciliationEntity
import com.example.data.model.IoTTelemetryEntity
import com.example.data.model.OutboundDispatchEntity
import com.example.data.model.ProcurementEntity
import com.example.data.model.StorageFacilityIntakeEntity
import com.example.data.model.TradeBookingEntity
import com.example.data.model.TruckRejectionEntity
import com.example.data.model.VendorLedgerEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ProcurementEntity::class,
        GodownEntity::class,
        StorageFacilityIntakeEntity::class,
        OutboundDispatchEntity::class,
        IoTTelemetryEntity::class,
        TradeBookingEntity::class,
        ExpenseEntryEntity::class,
        TruckRejectionEntity::class,
        VendorLedgerEntity::class,
        InventoryReconciliationEntity::class,
    ],
    version = 8,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun procurementDao(): ProcurementDao
    abstract fun godownDao(): GodownDao
    abstract fun storageIntakeDao(): StorageIntakeDao
    abstract fun dispatchDao(): DispatchDao
    abstract fun iotTelemetryDao(): IoTTelemetryDao
    abstract fun tradeDao(): TradeDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun truckRejectionDao(): TruckRejectionDao
    abstract fun vendorLedgerDao(): VendorLedgerDao
    abstract fun inventoryReconciliationDao(): InventoryReconciliationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "grainos_vault_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(AppDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class AppDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialGodowns(database.godownDao())
                        populateInitialProcurements(database.procurementDao(), database.vendorLedgerDao())
                        populateInitialStorageIntakes(database.storageIntakeDao())
                        populateInitialTelemetry(database.iotTelemetryDao())
                        populateInitialTrades(database.tradeDao(), database.dispatchDao())
                        populateInitialExpenses(database.expenseDao(), database.vendorLedgerDao())
                        populateInitialRejections(database.truckRejectionDao())
                    }
                }
            }

            suspend fun populateInitialGodowns(godownDao: GodownDao) {
                val defaultGodowns = listOf(
                    GodownEntity(
                        godownId = "GODOWN_A",
                        displayName = "Godown A (Main Silo)",
                        capacityMt = 200.0,
                        currentStockMt = 154.0,
                        activeCrop = "MAIZE",
                        averageMoisture = 12.2,
                        temperatureCelsius = 23.8,
                        baseCostPerQuintal = 2400.0,
                        adjustedAvgCostPerQuintal = 2400.0
                    ),
                    GodownEntity(
                        godownId = "GODOWN_B",
                        displayName = "Godown B (Secondary)",
                        capacityMt = 150.0,
                        currentStockMt = 112.0,
                        activeCrop = "WHEAT",
                        averageMoisture = 11.8,
                        temperatureCelsius = 24.1,
                        baseCostPerQuintal = 2380.0,
                        adjustedAvgCostPerQuintal = 2380.0
                    ),
                    GodownEntity(
                        godownId = "GODOWN_C",
                        displayName = "Godown C (Buffer Bay)",
                        capacityMt = 100.0,
                        currentStockMt = 64.0,
                        activeCrop = "SOYBEAN",
                        averageMoisture = 10.4,
                        temperatureCelsius = 23.0,
                        baseCostPerQuintal = 4850.0,
                        adjustedAvgCostPerQuintal = 4850.0
                    ),
                    GodownEntity(
                        godownId = "SILO_BAY_1",
                        displayName = "Silo Bay 1 (Aerated Tower)",
                        capacityMt = 300.0,
                        currentStockMt = 185.0,
                        activeCrop = "MAIZE",
                        averageMoisture = 12.0,
                        temperatureCelsius = 22.5,
                        baseCostPerQuintal = 2420.0,
                        adjustedAvgCostPerQuintal = 2420.0
                    ),
                    GodownEntity(
                        godownId = "DRYING_YARD",
                        displayName = "Drying Yard & Solar Beds",
                        capacityMt = 50.0,
                        currentStockMt = 22.0,
                        activeCrop = "MAIZE",
                        averageMoisture = 15.6,
                        temperatureCelsius = 31.2,
                        baseCostPerQuintal = 2250.0,
                        adjustedAvgCostPerQuintal = 2250.0
                    )
                )
                godownDao.insertGodowns(defaultGodowns)
            }

            suspend fun populateInitialProcurements(
                procurementDao: ProcurementDao,
                ledgerDao: VendorLedgerDao
            ) {
                val p1Gross = 8420.0
                val p1Tare = 3260.0
                val p1Net = p1Gross - p1Tare // 5160 kg = 51.60 Qtl
                val p1Rate = 2450.0
                val p1GrossVal = (p1Net / 100.0) * p1Rate // 126,420
                val p1MandiCess = p1GrossVal * 0.015 // 1896.30 (1.0% + 0.5%)
                val p1NetPayable = p1GrossVal - p1MandiCess // 124,523.70

                val p1 = ProcurementEntity(
                    tokenNo = "TK-1081",
                    farmerName = "Ramesh Patil",
                    mobileNumber = "+91 98224 51230",
                    village = "Pimpalner, Dhule",
                    vehicleNumber = "MH 15 AB 1234",
                    panNumber = "ABCDE1234F",
                    isPanVerified = true,
                    cropType = "MAIZE",
                    grossWeightKg = p1Gross,
                    tareWeightKg = p1Tare,
                    netWeightKg = p1Net,
                    bagCount = 103,
                    bagWeightKg = 50.0,
                    moisturePercentage = 12.8,
                    qualityGrade = "GRADE_A",
                    ratePerQuintal = p1Rate,
                    grossBillAmount = p1GrossVal,
                    applyMandiCess = true,
                    mandiMarketFee = p1GrossVal * 0.01,
                    mandiSupervisoryCharge = p1GrossVal * 0.005,
                    totalMandiCess = p1MandiCess,
                    enableTds194q = false,
                    totalAmount = p1NetPayable,
                    godownAssigned = "Godown A",
                    status = "COMPLETED",
                    paymentStatus = "PAID",
                    paymentMode = "RTGS",
                    utrOrChequeNo = "BARB202608151234",
                    whatsappEntrySent = true,
                    whatsappReceiptSent = true,
                    completedTimestamp = System.currentTimeMillis() - 1000 * 60 * 45
                )
                procurementDao.insertProcurement(p1)

                ledgerDao.insertLedgerEntry(
                    VendorLedgerEntity(
                        vendorType = "FARMER",
                        vendorName = "Ramesh Patil",
                        contactNumber = "+91 98224 51230",
                        panNumber = "ABCDE1234F",
                        transactionType = "BILL_CREDIT",
                        amount = p1NetPayable,
                        paymentMode = "RTGS",
                        utrOrChequeNo = "BARB202608151234",
                        referenceDocNo = "TK-1081",
                        runningBalance = 0.0,
                        notes = "Sauda Patti #TK-1081 Settled via RTGS"
                    )
                )

                val p2Gross = 9150.0
                val p2Tare = 3420.0
                val p2Net = p2Gross - p2Tare
                val p2Rate = 2380.0
                val p2GrossVal = (p2Net / 100.0) * p2Rate
                val p2MandiCess = p2GrossVal * 0.015
                val p2NetPayable = p2GrossVal - p2MandiCess

                val p2 = ProcurementEntity(
                    tokenNo = "TK-1082",
                    farmerName = "Suresh Shinde",
                    mobileNumber = "+91 94231 87654",
                    village = "Sakri, Dhule",
                    vehicleNumber = "MH 18 Q 4589",
                    panNumber = "WXYZP5678K",
                    isPanVerified = true,
                    cropType = "MAIZE",
                    grossWeightKg = p2Gross,
                    tareWeightKg = p2Tare,
                    netWeightKg = p2Net,
                    bagCount = 114,
                    bagWeightKg = 50.0,
                    moisturePercentage = 13.4,
                    qualityGrade = "GRADE_B",
                    ratePerQuintal = p2Rate,
                    grossBillAmount = p2GrossVal,
                    applyMandiCess = true,
                    mandiMarketFee = p2GrossVal * 0.01,
                    mandiSupervisoryCharge = p2GrossVal * 0.005,
                    totalMandiCess = p2MandiCess,
                    enableTds194q = false,
                    totalAmount = p2NetPayable,
                    godownAssigned = "Godown A",
                    status = "COMPLETED",
                    paymentStatus = "PENDING",
                    paymentMode = "CHEQUE",
                    utrOrChequeNo = "CHQ-882104",
                    chequeDate = System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 3,
                    isPdc = true,
                    pdcCleared = false,
                    whatsappEntrySent = true,
                    whatsappReceiptSent = true,
                    completedTimestamp = System.currentTimeMillis() - 1000 * 60 * 90
                )
                procurementDao.insertProcurement(p2)

                ledgerDao.insertLedgerEntry(
                    VendorLedgerEntity(
                        vendorType = "FARMER",
                        vendorName = "Suresh Shinde",
                        contactNumber = "+91 94231 87654",
                        panNumber = "WXYZP5678K",
                        transactionType = "PDC_ISSUED",
                        amount = p2NetPayable,
                        paymentMode = "CHEQUE",
                        utrOrChequeNo = "CHQ-882104",
                        chequeMaturityDate = System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 3,
                        pdcStatus = "PENDING_MATURITY",
                        referenceDocNo = "TK-1082",
                        runningBalance = p2NetPayable,
                        notes = "Post-Dated Cheque issued for Sauda Patti #TK-1082"
                    )
                )
            }

            suspend fun populateInitialStorageIntakes(storageIntakeDao: StorageIntakeDao) {
                val initialIntakes = listOf(
                    StorageFacilityIntakeEntity(
                        storageFacilityId = "GODOWN_A",
                        storageFacilityName = "Godown A (Main Silo)",
                        tokenNo = "TK-1081",
                        farmerName = "Ramesh Patil",
                        mobileNumber = "+91 98224 51230",
                        village = "Pimpalner, Dhule",
                        vehicleNumber = "MH 15 AB 1234",
                        cropType = "MAIZE",
                        qualityGrade = "GRADE_A",
                        grossWeightKg = 8420.0,
                        tareWeightKg = 3260.0,
                        netWeightKg = 5160.0,
                        netWeightMt = 5.16,
                        bagCount = 103,
                        bagWeightKg = 50.0,
                        moisturePercentage = 12.8,
                        temperatureCelsius = 23.8,
                        ratePerQuintal = 2450.0,
                        grossBillAmount = 126420.0,
                        totalAmount = 124523.70,
                        paymentStatus = "PAID",
                        paymentMode = "RTGS",
                        intakeTimestamp = System.currentTimeMillis() - 1000 * 60 * 45,
                        notes = "Unloaded into Bay 1-A • Moisture tested 12.8%"
                    ),
                    StorageFacilityIntakeEntity(
                        storageFacilityId = "GODOWN_A",
                        storageFacilityName = "Godown A (Main Silo)",
                        tokenNo = "TK-1082",
                        farmerName = "Suresh Shinde",
                        mobileNumber = "+91 94231 87654",
                        village = "Sakri, Dhule",
                        vehicleNumber = "MH 18 Q 4589",
                        cropType = "MAIZE",
                        qualityGrade = "GRADE_B",
                        grossWeightKg = 9150.0,
                        tareWeightKg = 3420.0,
                        netWeightKg = 5730.0,
                        netWeightMt = 5.73,
                        bagCount = 114,
                        bagWeightKg = 50.0,
                        moisturePercentage = 13.4,
                        temperatureCelsius = 24.0,
                        ratePerQuintal = 2380.0,
                        grossBillAmount = 136374.0,
                        totalAmount = 134328.39,
                        paymentStatus = "PENDING",
                        paymentMode = "CHEQUE",
                        intakeTimestamp = System.currentTimeMillis() - 1000 * 60 * 90,
                        notes = "Unloaded into Bay 1-B • Standard 50kg bag packing"
                    )
                )
                storageIntakeDao.insertIntakes(initialIntakes)
            }

            suspend fun populateInitialTelemetry(telemetryDao: IoTTelemetryDao) {
                val initialTelemetry = listOf(
                    IoTTelemetryEntity(
                        deviceType = "WEIGHBRIDGE",
                        deviceId = "WB-DIGI-01",
                        readingValue = 8420.0,
                        unit = "kg",
                        status = "STABLE",
                        rawPayloadJson = "{\"sensor\":\"LoadCell_4Ch\",\"calibration_ok\":true,\"gross_kg\":8420}",
                        latencyMs = 1
                    ),
                    IoTTelemetryEntity(
                        deviceType = "MOISTURE_METER",
                        deviceId = "MM-GRAIN-PRO",
                        readingValue = 12.8,
                        unit = "%",
                        status = "ACCEPTED",
                        rawPayloadJson = "{\"crop\":\"MAIZE\",\"dielectric_const\":4.32,\"temp_c\":24.2}",
                        latencyMs = 2
                    )
                )
                for (t in initialTelemetry) {
                    telemetryDao.insertTelemetry(t)
                }
            }

            suspend fun populateInitialTrades(
                tradeDao: TradeDao,
                dispatchDao: DispatchDao
            ) {
                val demoTrades = listOf(
                    TradeBookingEntity(
                        tradeNo = "TRD-8821",
                        cropType = "MAIZE",
                        brokerOrBuyerName = "Cargill Agro India / Broker R. Singhania",
                        quantityTons = 50.0,
                        bookedPricePerQuintal = 2580.0,
                        farmerPurchasePricePerQuintal = 2390.0,
                        laborPerQuintal = 18.0,
                        bagCostPerQuintal = 25.0,
                        transportPerQuintal = 35.0,
                        brokeragePerQuintal = 12.0,
                        tradeStatus = "ACTIVE",
                        tradeTimestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 2,
                        notes = "Forward delivery contract • Moisture spec < 12.5%"
                    ),
                    TradeBookingEntity(
                        tradeNo = "TRD-8822",
                        cropType = "WHEAT",
                        brokerOrBuyerName = "ITC Choupal Sagar / Broker M. Kulkarni",
                        quantityTons = 40.0,
                        bookedPricePerQuintal = 2510.0,
                        farmerPurchasePricePerQuintal = 2350.0,
                        laborPerQuintal = 18.0,
                        bagCostPerQuintal = 25.0,
                        transportPerQuintal = 35.0,
                        brokeragePerQuintal = 12.0,
                        tradeStatus = "SETTLED",
                        tradeTimestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 18,
                        notes = "Dispatched 400 Qtl to Indore Mill"
                    )
                )
                tradeDao.insertTrades(demoTrades)

                // Populate a completed dispatch to feed the P&L line graph
                val d1 = OutboundDispatchEntity(
                    dispatchNo = "DSP-2021",
                    buyerName = "ITC Choupal Sagar",
                    destination = "Indore Processing Plant",
                    vehicleNumber = "MH 18 AA 4410",
                    cropType = "WHEAT",
                    godownSource = "Godown B",
                    tareWeightKg = 9200.0,
                    grossWeightKg = 34200.0,
                    netLoadedWeightKg = 25000.0, // 250 Qtl
                    ratePerQuintal = 2510.0,
                    totalInvoiceAmount = 627500.0,
                    companyUnloadedWeightKg = 24920.0, // 249.20 Qtl
                    weightShortageKg = 80.0,
                    companyRateDeductionPenalty = 1200.0,
                    brokerName = "Broker M. Kulkarni",
                    brokerageRatePerQtl = 12.0,
                    finalBrokerageFee = 2990.40,
                    loadingLaborCost = 4500.0,
                    freightCost = 14500.0,
                    bagCost = 6250.0,
                    miscCost = 800.0,
                    fifoProcurementCost = 587500.0, // 250 * 2350
                    actualNetRevenue = (24920.0 / 100.0 * 2510.0) - 1200.0, // 624,292
                    actualNetProfit = 624292.0 - (587500.0 + 4500.0 + 14500.0 + 6250.0 + 800.0 + 2990.40), // 7,751.60
                    status = "UNLOADED",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 24,
                    unloadedTimestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 12,
                    notes = "Final unloaded weight acknowledged by ITC lab"
                )
                dispatchDao.insertDispatch(d1)
            }

            suspend fun populateInitialExpenses(
                expenseDao: ExpenseDao,
                ledgerDao: VendorLedgerDao
            ) {
                val demoExpenses = listOf(
                    ExpenseEntryEntity(
                        expenseNo = "EXP-301",
                        truckOrBatchRef = "MH 15 AB 1234 (Lot #1081)",
                        cropType = "MAIZE",
                        laborCost = 1450.0,
                        bagsCost = 2800.0,
                        transportCost = 3500.0,
                        miscCost = 450.0,
                        miscDescription = "Toll Taxes & APMC Gate Slip",
                        paidToOrParty = "Kisan Hamal Mandali",
                        paymentMode = "CASH",
                        notes = "Unloading & 50kg bag packing"
                    ),
                    ExpenseEntryEntity(
                        expenseNo = "EXP-302",
                        truckOrBatchRef = "MH 18 Q 4589 (Lot #1082)",
                        cropType = "MAIZE",
                        laborCost = 1600.0,
                        bagsCost = 3100.0,
                        transportCost = 3800.0,
                        miscCost = 350.0,
                        miscDescription = "Weighbridge computer slip fee",
                        paidToOrParty = "Dhule Freight Logistics",
                        paymentMode = "RTGS",
                        utrOrChequeNo = "SBIN00261984210",
                        notes = "Direct transfer to Godown A"
                    )
                )
                expenseDao.insertExpenses(demoExpenses)

                ledgerDao.insertLedgerEntry(
                    VendorLedgerEntity(
                        vendorType = "LABOR",
                        vendorName = "Kisan Hamal Mandali",
                        transactionType = "PAYMENT_DEBIT",
                        amount = 1450.0,
                        paymentMode = "CASH",
                        referenceDocNo = "EXP-301",
                        notes = "Hamali paid for Lot #1081"
                    )
                )
                ledgerDao.insertLedgerEntry(
                    VendorLedgerEntity(
                        vendorType = "TRANSPORTER",
                        vendorName = "Dhule Freight Logistics",
                        transactionType = "PAYMENT_DEBIT",
                        amount = 3800.0,
                        paymentMode = "RTGS",
                        utrOrChequeNo = "SBIN00261984210",
                        referenceDocNo = "EXP-302",
                        notes = "Freight paid for Lot #1082"
                    )
                )
            }

            suspend fun populateInitialRejections(rejectionDao: TruckRejectionDao) {
                val origLabor = 4800.0
                val returnLabor = origLabor * 0.50 // Exact 50%
                val demoRejections = listOf(
                    TruckRejectionEntity(
                        rejectionNo = "REJ-401",
                        truckNumber = "MH 18 B 9912",
                        buyerOrCompany = "Patanjali Feed Mill, Nashik",
                        cropType = "MAIZE",
                        dispatchedWeightKg = 22400.0,
                        rejectionReason = "Moisture > 14.8% & Black Grains > 3%",
                        transportLoss = 14500.0,
                        penaltiesDemurrage = 4000.0,
                        originalLoadingLaborCost = origLabor,
                        returnBagShiftingLaborCost = returnLabor, // Exactly ₹2400
                        qualitySalvageDeduction = 8500.0,
                        salvageAction = "Diverted to local Poultry Mill at ₹2150/qtl",
                        salvageRealizedRatePerQtl = 2150.0,
                        notes = "Return freight charged by transporter + 50% bag shifting labor"
                    )
                )
                rejectionDao.insertRejections(demoRejections)
            }
        }
    }
}
