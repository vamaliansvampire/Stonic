package com.example.service

import android.graphics.Bitmap
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

class GeminiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun testConnection(apiKey: String, model: String): Result<Long> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("API Key cannot be blank"))
        }

        val startTime = System.currentTimeMillis()
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

        val bodyJson = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", "Ping. Respond with 'Pong'"))
                    })
                })
            })
            put("generationConfig", JSONObject().put("maxOutputTokens", 10))
        }

        val request = Request.Builder()
            .url(url)
            .post(bodyJson.toString().toRequestBody(jsonMediaType))
            .build()

        try {
            val response: Response = client.newCall(request).execute()
            val latency = System.currentTimeMillis() - startTime
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                Result.success(latency)
            } else {
                val errorMsg = parseErrorMessage(response.code, responseBody)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun streamGenerateContent(
        apiKey: String,
        model: String,
        history: List<Pair<String, String>>, // role to text
        userPrompt: String,
        imageBitmap: Bitmap? = null,
        personality: String = "JARVIS Stonic",
        responseLength: String = "Balanced"
    ): Flow<String> = callbackFlow {
        if (apiKey.isBlank()) {
            trySend("⚠️ No Gemini API key found. Please configure your API key in Settings to converse with Stonic.")
            close()
            return@callbackFlow
        }

        val systemPrompt = buildSystemInstruction(personality, responseLength)
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:streamGenerateContent?alt=sse&key=$apiKey"

        val rootJson = JSONObject()

        // System Instruction
        rootJson.put("systemInstruction", JSONObject().apply {
            put("parts", JSONArray().apply {
                put(JSONObject().put("text", systemPrompt))
            })
        })

        // Contents
        val contentsArray = JSONArray()

        // Conversation history (limit to last 10 messages for context)
        val recentHistory = history.takeLast(10)
        for ((role, text) in recentHistory) {
            val roleName = if (role.equals("USER", ignoreCase = true)) "user" else "model"
            contentsArray.put(JSONObject().apply {
                put("role", roleName)
                put("parts", JSONArray().apply {
                    put(JSONObject().put("text", text))
                })
            })
        }

        // Current message
        val currentParts = JSONArray()
        if (imageBitmap != null) {
            val base64Image = bitmapToBase64(imageBitmap)
            currentParts.put(JSONObject().apply {
                put("inlineData", JSONObject().apply {
                    put("mimeType", "image/jpeg")
                    put("data", base64Image)
                })
            })
        }
        currentParts.put(JSONObject().put("text", userPrompt.ifBlank { "Describe this image in detail, Sir." }))

        contentsArray.put(JSONObject().apply {
            put("role", "user")
            put("parts", currentParts)
        })

        rootJson.put("contents", contentsArray)

        // Generation config
        val maxTokens = when (responseLength) {
            "Concise" -> 512
            "Detailed" -> 4096
            else -> 2048
        }
        rootJson.put("generationConfig", JSONObject().apply {
            put("temperature", 0.7)
            put("maxOutputTokens", maxTokens)
        })

        val request = Request.Builder()
            .url(url)
            .post(rootJson.toString().toRequestBody(jsonMediaType))
            .build()

        val call = client.newCall(request)

        try {
            val response = call.execute()
            if (!response.isSuccessful) {
                val errBody = response.body?.string() ?: ""
                trySend("⚠️ " + parseErrorMessage(response.code, errBody))
                close()
                return@callbackFlow
            }

            val inputStream = response.body?.byteStream()
            if (inputStream != null) {
                val reader = BufferedReader(InputStreamReader(inputStream))
                var line: String? = reader.readLine()
                while (line != null) {
                    if (line.startsWith("data:")) {
                        val jsonStr = line.substring(5).trim()
                        if (jsonStr.isNotBlank() && jsonStr != "[DONE]") {
                            try {
                                val chunkObj = JSONObject(jsonStr)
                                val candidates = chunkObj.optJSONArray("candidates")
                                if (candidates != null && candidates.length() > 0) {
                                    val candidate = candidates.getJSONObject(0)
                                    val content = candidate.optJSONObject("content")
                                    val parts = content?.optJSONArray("parts")
                                    if (parts != null) {
                                        for (i in 0 until parts.length()) {
                                            val part = parts.getJSONObject(i)
                                            val text = part.optString("text")
                                            if (text.isNotEmpty()) {
                                                trySend(text)
                                            }
                                        }
                                    }
                                }
                            } catch (_: Exception) {
                                // continue streaming
                            }
                        }
                    }
                    line = reader.readLine()
                }
                reader.close()
            }
            close()
        } catch (e: Exception) {
            trySend("\n[Stonic Error: ${e.localizedMessage ?: "Connection interrupted"}]")
            close()
        }

        awaitClose {
            call.cancel()
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        // Resize if too large
        val scaled = if (bitmap.width > 1024 || bitmap.height > 1024) {
            val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
            if (ratio > 1) {
                Bitmap.createScaledBitmap(bitmap, 1024, (1024 / ratio).toInt(), true)
            } else {
                Bitmap.createScaledBitmap(bitmap, (1024 * ratio).toInt(), 1024, true)
            }
        } else {
            bitmap
        }
        scaled.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    private fun buildSystemInstruction(personality: String, length: String): String {
        val base = """
You are Stonic, an advanced futuristic AI assistant inspired by JARVIS from Iron Man.
You speak with a confident, smooth, highly articulate, polite, and respectful tone. You address the user respectfully as 'Sir' (or user's preferred title).
You are equipped with deep scientific, engineering, coding, analytical, and tactical capabilities.
When answering:
- Provide structured, elegant markdown responses.
- Format all code with clear markdown fences, specifying language tags (e.g. ```kotlin, ```python).
- Provide crisp, intelligent, actionable advice.
- When performing calculations, show your logic cleanly.
- Never mention internal platform details, API secrets, or behind-the-scenes tooling.
- Embody the calm, unflappable, hyper-intelligent persona of Stonic.
""".trimIndent()

        val lengthRule = when (length) {
            "Concise" -> "Keep your responses brief, punchy, and direct to the point."
            "Detailed" -> "Provide comprehensive, deeply analyzed, in-depth breakdowns."
            else -> "Balance depth with clarity, remaining succinct yet thorough."
        }

        return "$base\nResponse Style: $personality. $lengthRule"
    }

    private fun parseErrorMessage(code: Int, body: String): String {
        return when (code) {
            400 -> "API Key error: Key invalid or malformed. Please verify in Settings."
            403 -> "Permission error: This API key does not have access to the selected Gemini model."
            404 -> "Model not found: The selected model is unavailable in this API version."
            429 -> "Rate limit exceeded: Please wait a moment or choose another Gemini model."
            500, 503 -> "Gemini service temporarily experiencing high traffic. Please retry."
            else -> {
                try {
                    val json = JSONObject(body)
                    val error = json.optJSONObject("error")
                    val message = error?.optString("message") ?: "HTTP error $code"
                    "Gemini error ($code): $message"
                } catch (_: Exception) {
                    "HTTP error $code"
                }
            }
        }
    }
}
