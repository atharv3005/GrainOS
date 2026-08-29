package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.CropType
import com.example.security.FirmEncryptedPreferencesHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("GrainOS", appName)
    }

    @Test
    fun `test firm encrypted preferences helper store and retrieval`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val helper = FirmEncryptedPreferencesHelper(context)

        helper.saveFirmOnboardingData(
            firmName = "Kisan Samriddhi Agri Ltd.",
            crop = CropType.SOYBEAN,
            capacityMt = 8500.0,
            isOnboarded = true
        )

        assertEquals("Kisan Samriddhi Agri Ltd.", helper.getFirmName())
        assertEquals(CropType.SOYBEAN, helper.getMainCrop())
        assertEquals(8500.0, helper.getStorageCapacityMt(), 0.01)
        assertTrue(helper.isOnboarded())
    }

    @Test
    fun `test negotiated farmer rate calculation overrides MSP`() {
        val grossKg = 8500.0
        val tareKg = 3500.0
        val netKg = grossKg - tareKg
        val netQuintals = netKg / 100.0 // 50 Quintals

        val mspRate = CropType.MAIZE.standardMsp // 2090.0
        val negotiatedRate = 2250.0 // Custom negotiated rate

        val totalAmountAtMsp = netQuintals * mspRate
        val totalAmountAtNegotiated = netQuintals * negotiatedRate

        assertEquals(50.0, netQuintals, 0.001)
        assertEquals(122500.0, totalAmountAtMsp, 0.01)
        assertEquals(112500.0, totalAmountAtNegotiated, 0.01)
    }
}
