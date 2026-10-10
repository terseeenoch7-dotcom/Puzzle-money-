package com.example.data.ai

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

object GeminiService {
    private const val TAG = "GeminiService"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private fun getApiKey(): String {
        return try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Transcribes audio using model gemini-3.5-transcribe as required by prompt.
     */
    suspend fun transcribeAudio(wavBase64: String): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(Exception("Gemini API key is not configured."))
        }

        try {
            val url = "$BASE_URL/gemini-3.5-transcribe:generateContent?key=$apiKey"

            val jsonBody = JSONObject().apply {
                val contents = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val parts = JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", "Please accurately transcribe this audio recording into English text. Return only the transcription.")
                            })
                            put(JSONObject().apply {
                                val inlineData = JSONObject().apply {
                                    put("mimeType", "audio/wav")
                                    put("data", wavBase64)
                                }
                                put("inlineData", inlineData)
                            })
                        }
                        put("parts", parts)
                    }
                    put(contentObj)
                }
                put("contents", contents)
            }

            val request = Request.Builder()
                .url(url)
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e(TAG, "Audio transcription failed with code ${response.code}: $responseBody")
                return@withContext Result.failure(Exception("Transcription error (${response.code})"))
            }

            val rootJson = JSONObject(responseBody)
            val candidates = rootJson.optJSONArray("candidates")
            val candidate = candidates?.optJSONObject(0)
            val parts = candidate?.optJSONObject("content")?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text")?.trim() ?: ""

            Result.success(text)
        } catch (e: Exception) {
            Log.e(TAG, "Error in transcribeAudio", e)
            Result.failure(e)
        }
    }

    /**
     * Generates puzzle solving hints using gemini-3.5-flash.
     */
    suspend fun getPuzzleHint(
        puzzleTitle: String,
        gridSize: Int,
        moves: Int,
        isEasyMode: Boolean
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            // Friendly fallback tip if no API key provided
            val defaultTips = listOf(
                "Solve row-by-row! Place the top row tiles in order first, leaving the rest undisturbed.",
                "To place the last two tiles of a row, position the corner tile directly below its target slot, then slide them together!",
                "Keep the blank space maneuvering on the bottom-right corner to maintain solved sections."
            )
            return@withContext Result.success(defaultTips.random())
        }

        try {
            val url = "$BASE_URL/gemini-3.5-flash:generateContent?key=$apiKey"
            val promptText = "You are a friendly casual mobile game puzzle master. Provide a snappy 2-sentence tactical tip for a ${gridSize}x${gridSize} sliding picture puzzle named '$puzzleTitle'. The player has made $moves moves. Make it exciting, encouraging and actionable!"

            val jsonBody = JSONObject().apply {
                val contents = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val parts = JSONArray().apply {
                            put(JSONObject().apply { put("text", promptText) })
                        }
                        put("parts", parts)
                    }
                    put(contentObj)
                }
                put("contents", contents)
            }

            val request = Request.Builder()
                .url(url)
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext Result.success("Solve the top row first, then work downwards!")
            }

            val rootJson = JSONObject(responseBody)
            val candidates = rootJson.optJSONArray("candidates")
            val candidate = candidates?.optJSONObject(0)
            val parts = candidate?.optJSONObject("content")?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text")?.trim() ?: ""

            Result.success(text)
        } catch (e: Exception) {
            Result.success("Keep the blank tile near the edges to navigate pieces smoothly!")
        }
    }

    /**
     * Generates a creative puzzle title and category description using gemini-3.5-flash.
     */
    suspend fun generatePuzzleIdea(theme: String): Result<Pair<String, String>> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.success(Pair("Mystic $theme", "A vibrant puzzle adventure featuring glowing art."))
        }

        try {
            val url = "$BASE_URL/gemini-3.5-flash:generateContent?key=$apiKey"
            val promptText = "Generate a snappy 3-word cartoon game puzzle title and 1-sentence description based on keyword '$theme'. Return as: Title | Description"

            val jsonBody = JSONObject().apply {
                val contents = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val parts = JSONArray().apply {
                            put(JSONObject().apply { put("text", promptText) })
                        }
                        put("parts", parts)
                    }
                    put(contentObj)
                }
                put("contents", contents)
            }

            val request = Request.Builder()
                .url(url)
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            val rootJson = JSONObject(responseBody)
            val text = rootJson.optJSONArray("candidates")
                ?.optJSONObject(0)
                ?.optJSONObject("content")
                ?.optJSONArray("parts")
                ?.optJSONObject(0)
                ?.optString("text") ?: ""

            val split = text.split("|")
            if (split.size >= 2) {
                Result.success(Pair(split[0].trim(), split[1].trim()))
            } else {
                Result.success(Pair("$theme Adventure", text.trim()))
            }
        } catch (e: Exception) {
            Result.success(Pair("$theme Explorer", "A colorful puzzle challenge."))
        }
    }
}
