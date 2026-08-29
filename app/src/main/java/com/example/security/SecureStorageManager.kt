package com.example.security

import android.content.Context
import com.example.data.model.CropType
import com.example.data.model.FirmProfile

/**
 * Enterprise Secure Storage Manager orchestrating EncryptedSharedPreferences and hardware-backed
 * cryptographic operations for GrainOS.
 */
class SecureStorageManager(context: Context) {

    private val encryptedPrefsHelper = FirmEncryptedPreferencesHelper(context)

    fun saveFirmOnboardingData(firmName: String, crop: CropType, capacityMt: Double, isOnboarded: Boolean = true) {
        encryptedPrefsHelper.saveFirmOnboardingData(firmName, crop, capacityMt, isOnboarded)
    }

    fun saveFirmProfile(profile: FirmProfile) {
        encryptedPrefsHelper.saveFirmProfile(profile)
    }

    fun loadFirmProfile(): FirmProfile {
        return encryptedPrefsHelper.loadFirmProfile()
    }

    fun isUserOnboarded(): Boolean {
        return encryptedPrefsHelper.isOnboarded()
    }

    fun setOnboarded(onboarded: Boolean) {
        encryptedPrefsHelper.setOnboarded(onboarded)
    }

    fun saveExpenseDefaults(labor: Double, bag: Double, transport: Double, brokerage: Double) {
        encryptedPrefsHelper.saveExpenseDefaults(labor, bag, transport, brokerage)
    }

    fun getEncryptedPreferencesHelper(): FirmEncryptedPreferencesHelper {
        return encryptedPrefsHelper
    }

    fun saveLong(key: String, value: Long) {
        encryptedPrefsHelper.saveLong(key, value)
    }

    fun getLong(key: String, defValue: Long): Long {
        return encryptedPrefsHelper.getLong(key, defValue)
    }
}
