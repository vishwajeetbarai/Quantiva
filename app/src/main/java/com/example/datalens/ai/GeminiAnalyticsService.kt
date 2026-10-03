package com.example.datalens.ai

import android.util.Log
import com.example.BuildConfig
import com.example.datalens.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiAnalyticsService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    private val modelName = "gemini-3.5-flash"
    private val baseUrl = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent"

    val isApiKeyConfigured: Boolean
        get() {
            val key = BuildConfig.GEMINI_API_KEY
            return key.isNotBlank() && key != "MY_GEMINI_API_KEY"
        }

    suspend fun askData(
        dataset: ParsedDataset,
        profile: DatasetProfile,
        userQuery: String,
        fallbackMessage: AskDataMessage
    ): AskDataMessage = withContext(Dispatchers.IO) {
        if (!isApiKeyConfigured) {
            return@withContext fallbackMessage.copy(
                verificationBadge = "Verified by Deterministic Analytics Engine (AI Key not configured)"
            )
        }

        try {
            val contextSummary = buildString {
                append("Dataset: ${dataset.name}\n")
                append("Rows: ${dataset.rowCount}, Columns: ${dataset.columnCount}\n")
                append("Columns: ${dataset.headers.joinToString(", ")}\n")
                append("Quality Score: ${profile.qualityScore}/100\n")
                append("Deterministic Evidence: ${fallbackMessage.calculationEvidence ?: "N/A"}\n")
                append("Calculated Primary Metric: ${fallbackMessage.keyMetric ?: "N/A"}\n")
                append("Raw Sample Rows (first 3):\n")
                dataset.rows.take(3).forEach {
                    append(it.joinToString(" | ")).append("\n")
                }
            }

            val prompt = """
                You are Data Lens AI, an expert executive data analyst.
                The user asked: "$userQuery"
                
                Here is the verified data and calculated statistics:
                $contextSummary
                
                CRITICAL RULES:
                1. DO NOT fabricate or invent any numbers or statistics.
                2. Strictly base your answer on the provided verified calculations.
                3. If the evidence is insufficient, say: "I don't have enough evidence in this dataset to determine that."
                4. Structure your response:
                   - Direct clear conclusion (1-2 sentences)
                   - Business implication / what to investigate next
                Keep it concise and professional.
            """.trimIndent()

            val aiResponse = callGemini(prompt)
            if (aiResponse.isNotBlank()) {
                fallbackMessage.copy(
                    directAnswer = aiResponse,
                    verificationBadge = "Verified by Analytics Engine & Gemini AI"
                )
            } else {
                fallbackMessage
            }
        } catch (e: Exception) {
            Log.e("GeminiAnalytics", "Error querying Gemini API: ${e.message}", e)
            fallbackMessage.copy(
                verificationBadge = "Verified by Analytics Engine (Offline fallback)"
            )
        }
    }

    suspend fun enhanceExecutiveReport(
        dataset: ParsedDataset,
        profile: DatasetProfile,
        baseReport: BusinessReport
    ): BusinessReport = withContext(Dispatchers.IO) {
        if (!isApiKeyConfigured) {
            return@withContext baseReport
        }

        try {
            val prompt = """
                You are Data Lens AI, a senior enterprise data strategist.
                Synthesize an executive summary for this business report:
                Dataset: ${dataset.name} (${dataset.rowCount} rows)
                Quality Score: ${profile.qualityScore}/100
                KPIs: ${baseReport.keyKpis.joinToString { "${it.first}: ${it.second}" }}
                Major Trends: ${baseReport.majorTrends.joinToString("; ")}
                Anomalies: ${baseReport.importantAnomalies.joinToString("; ")}
                
                Provide:
                1. A punchy 3-sentence executive summary emphasizing root causes and forward momentum.
                2. Three high-impact strategic business actions for the leadership team.
                
                Format as JSON:
                {
                  "executiveSummary": "...",
                  "recommendations": ["action 1", "action 2", "action 3"]
                }
            """.trimIndent()

            val response = callGemini(prompt)
            val jsonStart = response.indexOf('{')
            val jsonEnd = response.lastIndexOf('}')
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                val jsonStr = response.substring(jsonStart, jsonEnd + 1)
                val json = JSONObject(jsonStr)
                val newSummary = json.optString("executiveSummary", baseReport.executiveSummary)
                val recsArray = json.optJSONArray("recommendations")
                val newRecs = if (recsArray != null && recsArray.length() > 0) {
                    (0 until recsArray.length()).map { recsArray.getString(it) }
                } else {
                    baseReport.recommendations
                }
                baseReport.copy(
                    executiveSummary = newSummary,
                    recommendations = newRecs
                )
            } else {
                baseReport
            }
        } catch (e: Exception) {
            Log.e("GeminiAnalytics", "Error generating executive summary: ${e.message}")
            baseReport
        }
    }

    private fun callGemini(prompt: String): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val url = "$baseUrl?key=$apiKey"

        val requestJson = JSONObject().apply {
            val contents = JSONArray().apply {
                val contentObj = JSONObject().apply {
                    val parts = JSONArray().apply {
                        put(JSONObject().apply { put("text", prompt) })
                    }
                    put("parts", parts)
                }
                put(contentObj)
            }
            put("contents", contents)
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.2) // Low temperature for high analytical accuracy
                put("topP", 0.9)
            })
        }

        val body = requestJson.toString().toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: ""
                throw IllegalStateException("API error ${response.code}: $errorBody")
            }
            val respString = response.body?.string() ?: ""
            val json = JSONObject(respString)
            val candidates = json.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text") ?: ""
            return text.trim()
        }
    }
}
