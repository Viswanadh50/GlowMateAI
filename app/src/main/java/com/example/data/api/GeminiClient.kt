package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Analyzes skin profile and (optional) image, returning a structured analysis JSON.
     */
    suspend fun analyzeSkin(
        age: Int,
        selectedSkinType: String,
        concerns: String,
        base64Image: String? = null
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "API Key is empty or placeholder!")
            return@withContext getMockResponseJson(selectedSkinType, concerns, age)
        }

        val prompt = """
            You are an expert AI Skincare Dermatologist. Analyze the following skin profile and recommend a morning and evening regimen.
            User Profile:
            - Age: $age
            - User Selected Skin Type: $selectedSkinType
            - Specified Concerns: $concerns
            
            Provide a detailed skin analysis and customized product recommendations.
            Respond ONLY with a valid raw JSON object matching this schema (do NOT wrap in markdown backticks or any other formatting):
            {
              "overallHealthScore": Int (from 0 to 100),
              "acneScore": Int (0 to 100, where 0 is no acne, 100 is severe acne),
              "wrinkleScore": Int (0 to 100, where 0 is no wrinkles, 100 is heavy wrinkles),
              "textureScore": Int (0 to 100, where 0 is perfectly smooth, 100 is very rough),
              "dryScore": Int (0 to 100, where 0 is balanced hydration, 100 is extremely dry/dehydrated),
              "detailedReasoning": "String describing the skin analysis in detail, explaining the reasons for each recommendation, highlighting the user's specific skin profile and history.",
              "recommendedMorningRoutine": "String summarizing morning steps (e.g., Step 1: Cleanser, Step 2: Vitamin C, Step 3: Moisturizer, Step 4: Sunscreen)",
              "recommendedEveningRoutine": "String summarizing evening steps (e.g., Step 1: Double Cleanser, Step 2: Retinol Serum, Step 3: Night Cream)"
            }
        """.trimIndent()

        try {
            val jsonRequest = JSONObject()
            val contentsArray = JSONArray()
            val contentObject = JSONObject()
            val partsArray = JSONArray()

            // Add text prompt
            val textPart = JSONObject().put("text", prompt)
            partsArray.put(textPart)

            // Add base64 image if present
            if (base64Image != null) {
                val imagePart = JSONObject().put(
                    "inlineData",
                    JSONObject()
                        .put("mimeType", "image/jpeg")
                        .put("data", base64Image)
                )
                partsArray.put(imagePart)
            }

            contentObject.put("parts", partsArray)
            contentsArray.put(contentObject)
            jsonRequest.put("contents", contentsArray)

            // Configure generation for JSON format if possible
            val generationConfig = JSONObject()
            generationConfig.put("responseMimeType", "application/json")
            jsonRequest.put("generationConfig", generationConfig)

            val requestBody = jsonRequest.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errBody = response.body?.string() ?: ""
                    Log.e(TAG, "Request failed: ${response.code} $errBody")
                    return@withContext getMockResponseJson(selectedSkinType, concerns, age)
                }

                val responseBodyStr = response.body?.string() ?: ""
                val responseJson = JSONObject(responseBodyStr)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.optJSONObject("content")
                    if (content != null) {
                        val parts = content.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            var rawText = parts.getJSONObject(0).optString("text", "")
                            // Cleanup any markdown codeblock backticks if present
                            if (rawText.trim().startsWith("```")) {
                                rawText = rawText.trim()
                                    .replace(Regex("^```json\\s*"), "")
                                    .replace(Regex("^```\\s*"), "")
                                    .replace(Regex("\\s*```$"), "")
                            }
                            return@withContext rawText
                        }
                    }
                }
                return@withContext getMockResponseJson(selectedSkinType, concerns, age)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during Gemini call: ${e.message}", e)
            return@withContext getMockResponseJson(selectedSkinType, concerns, age)
        }
    }

    private fun getMockResponseJson(skinType: String, concerns: String, age: Int): String {
        // Fallback or mockup response if key is missing or network fails
        val overallScore = if (concerns.contains("Acne", true)) 65 else if (concerns.contains("Wrinkle", true)) 72 else 85
        val acne = if (concerns.contains("Acne", true)) 45 else 10
        val wrinkle = if (age > 40) 35 else if (concerns.contains("Aging", true) || concerns.contains("Wrinkle", true)) 28 else 12
        val texture = if (skinType.equals("Oily", true)) 40 else 20
        val dry = if (skinType.equals("Dry", true)) 55 else 15

        val reasoning = "Based on your $skinType skin type and concerns ($concerns) for a $age year old, " +
                "your skin profile shows localized congestion and mild barrier depletion. " +
                "To resolve these concerns, we recommend incorporating targeted active ingredients. " +
                "Hyaluronic Acid will address $skinType-related moisture loss, and Niacinamide will balance oil production. " +
                "In the morning, a powerful sunscreen like Zinc Oxide protects against UV-induced free radicals, while an evening Retinol promotes healthy cell turnover to prevent breakouts."

        val morning = "Step 1: CeraVe Hydrating Cleanser to cleanse without stripping. " +
                "Step 2: The Ordinary Niacinamide 10% to regulate sebum. " +
                "Step 3: Neutrogena Hydro Boost Water Gel to lock in lightweight moisture. " +
                "Step 4: EltaMD UV Clear SPF 46 for sun protection."

        val evening = "Step 1: CeraVe Hydrating Cleanser. " +
                "Step 2: The Ordinary Retinol 0.5% (apply twice weekly to stimulate cell regeneration). " +
                "Step 3: La Roche-Posay Double Repair Face Moisturizer to soothe and restore skin barrier overnight."

        return JSONObject()
            .put("overallHealthScore", overallScore)
            .put("acneScore", acne)
            .put("wrinkleScore", wrinkle)
            .put("textureScore", texture)
            .put("dryScore", dry)
            .put("detailedReasoning", reasoning)
            .put("recommendedMorningRoutine", morning)
            .put("recommendedEveningRoutine", evening)
            .toString()
    }
}
