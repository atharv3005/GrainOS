package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AuditTrailEntity
import com.example.data.model.DocumentSequenceEntity
import com.example.data.model.DocumentType
import com.example.data.model.ExpenseEntryEntity
import com.example.data.model.GodownEntity
import com.example.data.model.InventoryMovementEntity
import com.example.data.model.InventoryMovementType
import com.example.data.model.InventoryReconciliationEntity
import com.example.data.model.IoTTelemetryEntity
import com.example.data.model.OutboundDispatchEntity
import com.example.data.model.PartyEntity
import com.example.data.model.PartyType
import com.example.data.model.PaymentAllocationEntity
import com.example.data.model.ProcurementEntity
import com.example.data.model.QuantityBasis
import com.example.data.model.StorageFacilityIntakeEntity
import com.example.data.model.TradeBookingEntity
import com.example.data.model.TruckRejectionEntity
import com.example.data.model.VendorLedgerEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import com.example.data.model.ApprovalRequestEntity
import com.example.data.model.CashDrawerCountEntity
import com.example.data.model.GeneralLedgerEntity
import com.example.data.model.OrganizationEntity
import com.example.security.RbacManager
import com.example.security.UserEntity
import com.example.security.UserRole

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
        PartyEntity::class,
        DocumentSequenceEntity::class,
        AuditTrailEntity::class,
        InventoryMovementEntity::class,
        PaymentAllocationEntity::class,
        UserEntity::class,
        ApprovalRequestEntity::class,
        CashDrawerCountEntity::class,
        GeneralLedgerEntity::class,
        OrganizationEntity::class
    ],
    version = 10,
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
    abstract fun partyDao(): PartyDao
    abstract fun documentSequenceDao(): DocumentSequenceDao
    abstract fun auditTrailDao(): AuditTrailDao
    abstract fun inventoryMovementDao(): InventoryMovementDao
    abstract fun paymentAllocationDao(): PaymentAllocationDao
    abstract fun userDao(): UserDao
    abstract fun approvalDao(): ApprovalDao
    abstract fun cashDrawerDao(): CashDrawerDao
    abstract fun generalLedgerDao(): GeneralLedgerDao
    abstract fun organizationDao(): OrganizationDao

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
                        populateInitialDocumentSequences(database.documentSequenceDao())
                        populateInitialParties(database.partyDao())
                        populateInitialGodowns(database.godownDao(), database.inventoryMovementDao())
                        populateInitialProcurements(database.procurementDao(), database.vendorLedgerDao(), database.inventoryMovementDao())
                        populateInitialStorageIntakes(database.storageIntakeDao())
                        populateInitialTelemetry(database.iotTelemetryDao())
                        populateInitialTrades(database.tradeDao(), database.dispatchDao(), database.inventoryMovementDao())
                        populateInitialExpenses(database.expenseDao(), database.vendorLedgerDao())
                        populateInitialRejections(database.truckRejectionDao())
                        populateInitialUsers(database.userDao())
                        populateInitialOrganizations(database.organizationDao())
                    }
                }
            }

            suspend fun populateInitialDocumentSequences(dao: DocumentSequenceDao) {
                val fy = "26-27"
                DocumentType.entries.forEach { docType ->
                    dao.insert(
                        DocumentSequenceEntity(
                            financialYear = fy,
                            facilityId = "MAIN",
                            documentType = docType.name,
                            seriesCode = "GEN",
                            currentSequence = when (docType) {
                                DocumentType.GIN -> 1082L
                                DocumentType.DSP -> 2021L
                                DocumentType.EXP -> 302L
                                DocumentType.TRD -> 8822L
                                DocumentType.REJ -> 401L
                                else -> 100L
                            }
                        )
                    )
                }
            }

            suspend fun populateInitialParties(dao: PartyDao) {
                val initialParties = listOf(
                    PartyEntity(
                        partyType = PartyType.FARMER.name,
                        legalName = "Ramesh Patil",
                        mobile = "+91 98224 51230",
                        village = "Pimpalner",
                        taluka = "Sakri",
                        district = "Dhule",
                        pan = "ABCDE1234F",
                        cumulativePurchasesInFy = 126420.0
                    ),
                    PartyEntity(
                        partyType = PartyType.FARMER.name,
                        legalName = "Suresh Shinde",
                        mobile = "+91 94231 87654",
                        village = "Sakri",
                        taluka = "Sakri",
                        district = "Dhule",
                        pan = "WXYZP5678K",
                        cumulativePurchasesInFy = 136374.0
                    ),
                    PartyEntity(
                        partyType = PartyType.BUYER.name,
                        legalName = "ITC Choupal Sagar Ltd",
                        tradeName = "ITC Agri Business",
                        mobile = "+91 98200 11223",
                        district = "Indore",
                        state = "Madhya Pradesh",
                        gstin = "23AAACI1234A1Z5",
                        pan = "AAACI1234A"
                    ),
                    PartyEntity(
                        partyType = PartyType.BUYER.name,
                        legalName = "Cargill Agro India Ltd",
                        mobile = "+91 99300 44556",
                        district = "Mumbai",
                        state = "Maharashtra",
                        gstin = "27AAACC4321B1Z2",
                        pan = "AAACC4321B"
                    ),
                    PartyEntity(
                        partyType = PartyType.TRANSPORTER.name,
                        legalName = "Dhule Freight Logistics",
                        mobile = "+91 94222 77889",
                        district = "Dhule"
                    ),
                    PartyEntity(
                        partyType = PartyType.LABOUR.name,
                        legalName = "Kisan Hamal Mandali",
                        mobile = "+91 98233 44112",
                        district = "Dhule"
                    )
                )
                dao.insertAll(initialParties)
            }

            suspend fun populateInitialGodowns(godownDao: GodownDao, movementDao: InventoryMovementDao) {
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

                // Populate initial opening stock inventory movements
                defaultGodowns.forEach { g ->
                    movementDao.insert(
                        InventoryMovementEntity(
                            movementType = InventoryMovementType.RECEIPT.name,
                            sourceEntityType = "OPENING_STOCK",
                            sourceEntityUuid = "INIT_${g.godownId}",
                            facilityId = g.godownId,
                            cropType = g.activeCrop,
                            quantityKg = g.currentStockMt * 1000.0,
                            quantityBasis = QuantityBasis.INVENTORY.name,
                            costPerQuintalPaise = (g.baseCostPerQuintal * 100).toLong(),
                            totalValuePaise = ((g.currentStockMt * 10.0) * g.baseCostPerQuintal * 100).toLong(),
                            reason = "Initial Godown Opening Stock"
                        )
                    )
                }
            }

            suspend fun populateInitialProcurements(
                procurementDao: ProcurementDao,
                ledgerDao: VendorLedgerDao,
                movementDao: InventoryMovementDao
            ) {
                val p1Gross = 8420.0
                val p1Tare = 3260.0
                val p1Net = p1Gross - p1Tare // 5160 kg = 51.60 Qtl
                val p1Rate = 2450.0
                val p1GrossVal = (p1Net / 100.0) * p1Rate // 126,420
                val p1MandiCess = p1GrossVal * 0.015 // 1896.30 (1.0% + 0.5%)
                val p1NetPayable = p1GrossVal - p1MandiCess // 124,523.70

                val p1 = ProcurementEntity(
                    tokenNo = "GIN/26-27/01081",
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

                movementDao.insert(
                    InventoryMovementEntity(
                        movementType = InventoryMovementType.RECEIPT.name,
                        sourceEntityType = "PROCUREMENT",
                        sourceEntityUuid = p1.uuid,
                        facilityId = "GODOWN_A",
                        cropType = p1.cropType,
                        quantityKg = p1.netWeightKg,
                        costPerQuintalPaise = (p1.ratePerQuintal * 100).toLong(),
                        totalValuePaise = (p1.totalAmount * 100).toLong(),
                        reason = "Farmer Procurement GIN #01081"
                    )
                )

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
                        referenceDocNo = "GIN/26-27/01081",
                        runningBalance = 0.0,
                        notes = "Sauda Patti #GIN/26-27/01081 Settled via RTGS"
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
                    tokenNo = "GIN/26-27/01082",
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

                movementDao.insert(
                    InventoryMovementEntity(
                        movementType = InventoryMovementType.RECEIPT.name,
                        sourceEntityType = "PROCUREMENT",
                        sourceEntityUuid = p2.uuid,
                        facilityId = "GODOWN_A",
                        cropType = p2.cropType,
                        quantityKg = p2.netWeightKg,
                        costPerQuintalPaise = (p2.ratePerQuintal * 100).toLong(),
                        totalValuePaise = (p2.totalAmount * 100).toLong(),
                        reason = "Farmer Procurement GIN #01082"
                    )
                )

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
                        pdcStatus = "ISSUED",
                        referenceDocNo = "GIN/26-27/01082",
                        runningBalance = p2NetPayable,
                        notes = "Post-Dated Cheque issued for Sauda Patti #GIN/26-27/01082"
                    )
                )
            }

            suspend fun populateInitialStorageIntakes(storageIntakeDao: StorageIntakeDao) {
                val initialIntakes = listOf(
                    StorageFacilityIntakeEntity(
                        storageFacilityId = "GODOWN_A",
                        storageFacilityName = "Godown A (Main Silo)",
                        tokenNo = "GIN/26-27/01081",
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
                        tokenNo = "GIN/26-27/01082",
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
                dispatchDao: DispatchDao,
                movementDao: InventoryMovementDao
            ) {
                val demoTrades = listOf(
                    TradeBookingEntity(
                        tradeNo = "TRD/26-27/08821",
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
                        tradeNo = "TRD/26-27/08822",
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

                val d1 = OutboundDispatchEntity(
                    dispatchNo = "DSP/26-27/02021",
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

                movementDao.insert(
                    InventoryMovementEntity(
                        movementType = InventoryMovementType.DISPATCH.name,
                        sourceEntityType = "DISPATCH",
                        sourceEntityUuid = d1.uuid,
                        facilityId = "GODOWN_B",
                        cropType = d1.cropType,
                        quantityKg = -d1.netLoadedWeightKg,
                        costPerQuintalPaise = (d1.ratePerQuintal * 100).toLong(),
                        totalValuePaise = (d1.totalInvoiceAmount * 100).toLong(),
                        reason = "Outbound Dispatch DSP #02021"
                    )
                )
            }

            suspend fun populateInitialExpenses(
                expenseDao: ExpenseDao,
                ledgerDao: VendorLedgerDao
            ) {
                val demoExpenses = listOf(
                    ExpenseEntryEntity(
                        expenseNo = "EXP/26-27/00301",
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
                        expenseNo = "EXP/26-27/00302",
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
                        referenceDocNo = "EXP/26-27/00301",
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
                        referenceDocNo = "EXP/26-27/00302",
                        notes = "Freight paid for Lot #1082"
                    )
                )
            }

            suspend fun populateInitialRejections(rejectionDao: TruckRejectionDao) {
                val origLabor = 4800.0
                val returnLabor = origLabor * 0.50 // Exact 50%
                val demoRejections = listOf(
                    TruckRejectionEntity(
                        rejectionNo = "REJ/26-27/00401",
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

            suspend fun populateInitialUsers(userDao: UserDao) {
                val ownerSalt = RbacManager.generateSalt()
                val opSalt = RbacManager.generateSalt()
                val accSalt = RbacManager.generateSalt()

                val initialUsers = listOf(
                    UserEntity(
                        username = "owner",
                        fullName = "Ramesh Patil (Proprietor)",
                        pinSalt = ownerSalt,
                        pinHash = RbacManager.hashPin("1234", ownerSalt),
                        role = UserRole.OWNER.name,
                        mobile = "+91 98220 12345"
                    ),
                    UserEntity(
                        username = "operator",
                        fullName = "Ganesh Shinde (Weighbridge In-charge)",
                        pinSalt = opSalt,
                        pinHash = RbacManager.hashPin("0000", opSalt),
                        role = UserRole.OPERATOR.name,
                        mobile = "+91 98220 54321"
                    ),
                    UserEntity(
                        username = "accountant",
                        fullName = "Vijay Kulkarni (Munim / CA)",
                        pinSalt = accSalt,
                        pinHash = RbacManager.hashPin("1111", accSalt),
                        role = UserRole.ACCOUNTANT.name,
                        mobile = "+91 98220 99887"
                    )
                )
                userDao.insertUsers(initialUsers)
            }

            suspend fun populateInitialOrganizations(orgDao: OrganizationDao) {
                val initialOrgs = listOf(
                    OrganizationEntity(
                        orgCode = "DHULE_MAIN",
                        legalName = "GrainOS Enterprise Agri Hub",
                        tradeName = "GrainOS Trading Co",
                        apmcLicenseNo = "APMC/MH/2026/088",
                        gstin = "27AABCB1234F1Z5",
                        pan = "AABCB1234F",
                        address = "Dhule APMC Market Yard, Maharashtra"
                    ),
                    OrganizationEntity(
                        orgCode = "NANDURBAR_BRANCH",
                        legalName = "GrainOS Tribal Agri Hub",
                        tradeName = "GrainOS Nandurbar Branch",
                        apmcLicenseNo = "APMC/MH/2026/102",
                        gstin = "27AABCB1234F1Z5",
                        pan = "AABCB1234F",
                        address = "Nandurbar Mandi Yard, Maharashtra"
                    )
                )
                initialOrgs.forEach { orgDao.insert(it) }
            }
        }
    }
}
