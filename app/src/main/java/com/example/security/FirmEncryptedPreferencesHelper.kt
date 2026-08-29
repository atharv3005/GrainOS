package com.example.security

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.data.model.CropType
import com.example.data.model.FirmProfile

/**
 * Helper class utilizing [EncryptedSharedPreferences] and [MasterKey] with AES-256 GCM encryption
 * to securely handle retrieval and storage of firm onboarding data (Firm Name, Crop, Capacity)
 * and firm operational configuration.
 */
class FirmEncryptedPreferencesHelper(context: Context) {

    private val sharedPreferences: SharedPreferences = createEncryptedSharedPreferences(context)

    companion object {
        private const val TAG = "FirmEncryptedPrefs"
        private const val SECURE_PREFS_FILE = "grainos_firm_encrypted_prefs"

        // Primary Onboarding Data Keys
        private const val KEY_IS_ONBOARDED = "key_is_onboarded"
        private const val KEY_FIRM_NAME = "key_firm_name"
        private const val KEY_MAIN_CROP = "key_main_crop"
        private const val KEY_CAPACITY_MT = "key_capacity_mt"

        // Detailed Firm Metadata Keys
        private const val KEY_REGISTRATION_NO = "key_reg_no"
        private const val KEY_LOCATION = "key_location"
        private const val KEY_OPERATOR_NAME = "key_operator_name"
        private const val KEY_CONTACT_NUMBER = "key_contact_number"
        private const val KEY_GST_NUMBER = "key_gst_number"
        private const val KEY_TAG_LINE = "key_tag_line"

        // Per-Quintal Operational Expense Keys
        private const val KEY_LABOR_PER_QTL = "key_labor_per_qtl"
        private const val KEY_BAG_PER_QTL = "key_bag_per_qtl"
        private const val KEY_TRANSPORT_PER_QTL = "key_transport_per_qtl"
        private const val KEY_BROKERAGE_PER_QTL = "key_brokerage_per_qtl"

        // Enterprise Defaults
        const val DEFAULT_FIRM_NAME = "Bijasani Mata Agro FPC Ltd."
        const val DEFAULT_LOCATION = "Dhule, Maharashtra"
        const val DEFAULT_OPERATOR = "Operator A. Patil"
        const val DEFAULT_CONTACT = "+91 98220 12345"
        const val DEFAULT_GST = "27AABCB1234F1Z5"
        const val DEFAULT_REG_NO = "U01110MH2021PTC"
        const val DEFAULT_TAGLINE = "GrainOS Enterprise Agri-Commodity Division"
        const val DEFAULT_CAPACITY_MT = 5000.0
        val DEFAULT_CROP = CropType.MAIZE

        private fun createEncryptedSharedPreferences(context: Context): SharedPreferences {
            return try {
                val masterKey = MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()

                EncryptedSharedPreferences.create(
                    context,
                    SECURE_PREFS_FILE,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
            } catch (e: Exception) {
                Log.w(TAG, "EncryptedSharedPreferences initialization error, falling back to private prefs", e)
                context.getSharedPreferences("${SECURE_PREFS_FILE}_fallback", Context.MODE_PRIVATE)
            }
        }
    }

    /**
     * Securely store primary firm onboarding data (Firm Name, Crop, Capacity).
     */
    fun saveFirmOnboardingData(
        firmName: String,
        crop: CropType,
        capacityMt: Double,
        isOnboarded: Boolean = true
    ) {
        sharedPreferences.edit()
            .putString(KEY_FIRM_NAME, firmName.trim())
            .putString(KEY_MAIN_CROP, crop.name)
            .putFloat(KEY_CAPACITY_MT, capacityMt.toFloat())
            .putBoolean(KEY_IS_ONBOARDED, isOnboarded)
            .apply()
    }

    /**
     * Securely store complete firm profile including operational expenses.
     */
    fun saveFirmProfile(profile: FirmProfile) {
        sharedPreferences.edit()
            .putBoolean(KEY_IS_ONBOARDED, profile.isOnboarded)
            .putString(KEY_FIRM_NAME, profile.firmName)
            .putString(KEY_MAIN_CROP, profile.mainTargetCrop.name)
            .putFloat(KEY_CAPACITY_MT, profile.totalCapacityMt.toFloat())
            .putString(KEY_REGISTRATION_NO, profile.registrationNumber)
            .putString(KEY_LOCATION, profile.location)
            .putString(KEY_OPERATOR_NAME, profile.operatorName)
            .putString(KEY_CONTACT_NUMBER, profile.contactNumber)
            .putString(KEY_GST_NUMBER, profile.gstNumber)
            .putString(KEY_TAG_LINE, profile.tagLine)
            .putFloat(KEY_LABOR_PER_QTL, profile.laborPerQuintal.toFloat())
            .putFloat(KEY_BAG_PER_QTL, profile.bagCostPerQuintal.toFloat())
            .putFloat(KEY_TRANSPORT_PER_QTL, profile.transportPerQuintal.toFloat())
            .putFloat(KEY_BROKERAGE_PER_QTL, profile.brokeragePerQuintal.toFloat())
            .apply()
    }

    /**
     * Retrieve Firm Name securely.
     */
    fun getFirmName(): String {
        return sharedPreferences.getString(KEY_FIRM_NAME, DEFAULT_FIRM_NAME) ?: DEFAULT_FIRM_NAME
    }

    /**
     * Store Firm Name securely.
     */
    fun setFirmName(name: String) {
        sharedPreferences.edit().putString(KEY_FIRM_NAME, name.trim()).apply()
    }

    /**
     * Retrieve Main Crop securely.
     */
    fun getMainCrop(): CropType {
        val cropName = sharedPreferences.getString(KEY_MAIN_CROP, DEFAULT_CROP.name) ?: DEFAULT_CROP.name
        return try {
            CropType.valueOf(cropName)
        } catch (_: Exception) {
            DEFAULT_CROP
        }
    }

    /**
     * Store Main Crop securely.
     */
    fun setMainCrop(crop: CropType) {
        sharedPreferences.edit().putString(KEY_MAIN_CROP, crop.name).apply()
    }

    /**
     * Retrieve Storage Capacity in Metric Tons (MT) securely.
     */
    fun getStorageCapacityMt(): Double {
        return sharedPreferences.getFloat(KEY_CAPACITY_MT, DEFAULT_CAPACITY_MT.toFloat()).toDouble()
    }

    /**
     * Store Storage Capacity in Metric Tons (MT) securely.
     */
    fun setStorageCapacityMt(capacityMt: Double) {
        sharedPreferences.edit().putFloat(KEY_CAPACITY_MT, capacityMt.toFloat()).apply()
    }

    /**
     * Check if firm has completed initial onboarding.
     */
    fun isOnboarded(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_ONBOARDED, false)
    }

    /**
     * Set onboarding completion flag securely.
     */
    fun setOnboarded(onboarded: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_ONBOARDED, onboarded).apply()
    }

    /**
     * Retrieve complete FirmProfile securely.
     */
    fun loadFirmProfile(): FirmProfile {
        val isOnboarded = sharedPreferences.getBoolean(KEY_IS_ONBOARDED, false)
        val firmName = getFirmName()
        val mainCrop = getMainCrop()
        val capacityMt = getStorageCapacityMt()

        val regNo = sharedPreferences.getString(KEY_REGISTRATION_NO, DEFAULT_REG_NO) ?: DEFAULT_REG_NO
        val location = sharedPreferences.getString(KEY_LOCATION, DEFAULT_LOCATION) ?: DEFAULT_LOCATION
        val operator = sharedPreferences.getString(KEY_OPERATOR_NAME, DEFAULT_OPERATOR) ?: DEFAULT_OPERATOR
        val contact = sharedPreferences.getString(KEY_CONTACT_NUMBER, DEFAULT_CONTACT) ?: DEFAULT_CONTACT
        val gst = sharedPreferences.getString(KEY_GST_NUMBER, DEFAULT_GST) ?: DEFAULT_GST
        val tagLine = sharedPreferences.getString(KEY_TAG_LINE, DEFAULT_TAGLINE) ?: DEFAULT_TAGLINE

        val labor = sharedPreferences.getFloat(KEY_LABOR_PER_QTL, 18.0f).toDouble()
        val bag = sharedPreferences.getFloat(KEY_BAG_PER_QTL, 25.0f).toDouble()
        val transport = sharedPreferences.getFloat(KEY_TRANSPORT_PER_QTL, 35.0f).toDouble()
        val brokerage = sharedPreferences.getFloat(KEY_BROKERAGE_PER_QTL, 12.0f).toDouble()

        return FirmProfile(
            firmName = firmName,
            registrationNumber = regNo,
            location = location,
            operatorName = operator,
            contactNumber = contact,
            gstNumber = gst,
            tagLine = tagLine,
            totalCapacityMt = capacityMt,
            mainTargetCrop = mainCrop,
            isOnboarded = isOnboarded,
            laborPerQuintal = labor,
            bagCostPerQuintal = bag,
            transportPerQuintal = transport,
            brokeragePerQuintal = brokerage
        )
    }

    /**
     * Save updated per-quintal operational expense defaults.
     */
    fun saveExpenseDefaults(labor: Double, bag: Double, transport: Double, brokerage: Double) {
        sharedPreferences.edit()
            .putFloat(KEY_LABOR_PER_QTL, labor.toFloat())
            .putFloat(KEY_BAG_PER_QTL, bag.toFloat())
            .putFloat(KEY_TRANSPORT_PER_QTL, transport.toFloat())
            .putFloat(KEY_BROKERAGE_PER_QTL, brokerage.toFloat())
            .apply()
    }

    /**
     * Clear all stored firm onboarding data.
     */
    fun clear() {
        sharedPreferences.edit().clear().apply()
    }

    fun saveLong(key: String, value: Long) {
        sharedPreferences.edit().putLong(key, value).apply()
    }

    fun getLong(key: String, defValue: Long): Long {
        return sharedPreferences.getLong(key, defValue)
    }
}
