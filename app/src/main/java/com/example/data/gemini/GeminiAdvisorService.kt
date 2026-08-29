package com.example.data.gemini

import com.example.BuildConfig
import com.example.data.model.CropType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiAdvisorService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun analyzeGrainLot(
        crop: CropType,
        moisturePct: Double,
        ambientTempC: Double,
        targetGodown: String,
        farmerName: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext generateLocalExpertAdvisory(crop, moisturePct, ambientTempC, targetGodown, farmerName)
        }

        val prompt = """
            You are the Senior Agronomist and Grain Storage Specialist at Bijasani Mata Agro FPC.
            Analyze this inbound grain procurement lot:
            - Crop: ${crop.displayName} (${crop.hindiName})
            - Standard Ideal Moisture: ${crop.idealMoisture}% (Max safe: ${crop.maxSafeMoisture}%)
            - Measured Lot Moisture: $moisturePct%
            - Ambient Temp: $ambientTempC°C
            - Target Location: $targetGodown
            - Farmer: $farmerName

            Provide a concise, highly structured expert recommendation containing:
            1. Quality & Storage Verdict (Safe for Long-term Storage / Moderate Risk / High Moisture Alert)
            2. Drying Yard Action (Hours of solar sun-drying or mechanical aeration needed if moisture > ${crop.idealMoisture}%)
            3. Recommended Price & Payout Guidance (Fair MSP rate: ₹${crop.standardMsp}/qtl)
            4. Warehouse Aeration & Pest Prevention tip for $targetGodown
            5. Farmer Guidance Note (Friendly 2-line advice for $farmerName).
            Keep it structured with bullet points.
        """.trimIndent()

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-pro-preview:generateContent?key=$apiKey"
            val payload = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                })
            }

            val request = Request.Builder()
                .url(url)
                .post(payload.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                return@withContext generateLocalExpertAdvisory(crop, moisturePct, ambientTempC, targetGodown, farmerName)
            }

            val json = JSONObject(responseBody)
            val candidates = json.optJSONArray("candidates")
            val content = candidates?.optJSONObject(0)?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text")

            if (!text.isNullOrBlank()) {
                text.trim()
            } else {
                generateLocalExpertAdvisory(crop, moisturePct, ambientTempC, targetGodown, farmerName)
            }
        } catch (e: Exception) {
            generateLocalExpertAdvisory(crop, moisturePct, ambientTempC, targetGodown, farmerName)
        }
    }

    private fun generateLocalExpertAdvisory(
        crop: CropType,
        moisturePct: Double,
        ambientTempC: Double,
        targetGodown: String,
        farmerName: String
    ): String {
        val safe = moisturePct <= crop.idealMoisture
        val moderate = moisturePct > crop.idealMoisture && moisturePct <= crop.maxSafeMoisture
        return buildString {
            append("🌾 **Grain Advisory Report: ${crop.displayName}**\n\n")
            if (safe) {
                append("✅ **Quality & Storage Verdict**: Prime Grade A Lot. Moisture at $moisturePct% is below safe threshold (${crop.idealMoisture}%). Eligible for immediate long-term bulk storage.\n")
                append("• **Drying Bed**: Not required. Direct conveyor transfer to $targetGodown permitted.\n")
                append("• **MSP Rate**: Full Base MSP rate of ₹${crop.standardMsp.toInt()}/quintal approved.\n")
                append("• **Aeration Plan**: Maintain relative humidity <60% in $targetGodown with standard 4-hour cycle fan runs.\n")
            } else if (moderate) {
                append("⚠️ **Quality & Storage Verdict**: Grade B Standard. Moisture at $moisturePct% exceeds optimum (${crop.idealMoisture}%).\n")
                append("• **Drying Bed**: 4 to 6 hours of solar bed drying or 8 hours mechanical forced air ventilation recommended before airtight binning.\n")
                append("• **MSP Rate**: Standard Grade B rate ₹${(crop.standardMsp * 0.96).toInt()}/quintal with 4% moisture deduction.\n")
                append("• **Aeration Plan**: Continuous bottom-up cross ventilation in $targetGodown to prevent hotspot formation.\n")
            } else {
                append("🚨 **Quality & Storage Verdict**: High Moisture Risk ($moisturePct%). Immediate routing to Drying Yard required to prevent fungal spoilage and heating.\n")
                append("• **Drying Bed**: Mandatory 12-18 hours thin-layer yard drying. Retest moisture before shifting to Godown.\n")
                append("• **MSP Rate**: Moisture discount applied; rate revised to ₹${(crop.standardMsp * 0.88).toInt()}/quintal.\n")
                append("• **Storage Warning**: Do NOT store in closed silo until moisture drops below ${crop.idealMoisture}%.\n")
            }
            append("\n💡 **Farmer Advice for $farmerName**:\n")
            append("Harvest during sunny morning hours and ensure tarpaulin coverage during transit to protect grain quality and maximize MSP returns.")
        }
    }
}
