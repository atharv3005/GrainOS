package com.example.data.model

data class FirmProfile(
    val firmName: String = "Bijasani Mata Agro FPC Ltd.",
    val registrationNumber: String = "U01110MH2021PTC",
    val location: String = "Dhule, Maharashtra",
    val operatorName: String = "Operator A. Patil",
    val contactNumber: String = "+91 98220 12345",
    val gstNumber: String = "27AABCB1234F1Z5",
    val tagLine: String = "GrainOS Enterprise Agri-Commodity Division",
    val totalCapacityMt: Double = 5000.0,
    val mainTargetCrop: CropType = CropType.MAIZE,
    val isOnboarded: Boolean = true,
    val initialCapital: Double = 5000000.0, // 50 Lakhs Default Capital
    // Universal Per-Quintal Default Expenses (₹/Quintal)
    val laborPerQuintal: Double = 18.0,
    val bagCostPerQuintal: Double = 25.0,
    val transportPerQuintal: Double = 35.0,
    val brokeragePerQuintal: Double = 12.0
) {
    val totalOverheadPerQuintal: Double
        get() = laborPerQuintal + bagCostPerQuintal + transportPerQuintal + brokeragePerQuintal
}
