package com.foodpal.data.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Talks to the FoodPal-hosted Open Food Facts service at thecodemill.dev.
 *
 *   POST /api/off/register/            — register per-install UID
 *   GET /api/off/search/?q=&limit=     — FTS5-ranked search
 *   GET /api/off/product/<barcode>/    — single product lookup
 *
 * Every request includes:
 *   X-Api-Key: public app identifier (identifies the FoodPal app)
 *   X-Uid:     per-install UUID (used for per-install rate limiting)
 *
 * The UID is registered on first use via [registerUid]. Runs on
 * [Dispatchers.IO] via [withContext] so callers can launch from the
 * UI thread. Throws [IOException] on network/HTTP errors.
 */
class OffApiClient(
    private val baseUrl: String = com.foodpal.BuildConfig.OFF_BASE_URL,
    private val apiKey: String = com.foodpal.BuildConfig.OFF_API_KEY,
    private val uidManager: UidManager? = null,
    private val client: OkHttpClient = defaultClient(),
) {

    private var registered: Boolean = false

    private fun buildRequest(url: String): Request {
        val builder = Request.Builder()
            .url(url)
            .header("X-Api-Key", apiKey)
        uidManager?.let { builder.header("X-Uid", it.uid) }
        return builder.build()
    }

    /**
     * Registers the per-install UID with the server. Called automatically
     * on first API request. Safe to call multiple times (idempotent).
     */
    suspend fun registerUid(): Boolean = withContext(Dispatchers.IO) {
        val uid = uidManager?.uid ?: return@withContext false
        if (registered) return@withContext true
        val body = org.json.JSONObject().put("uid", uid).toString()
        val req = Request.Builder()
            .url("$baseUrl/api/off/register/")
            .header("X-Api-Key", apiKey)
            .header("Content-Type", "application/json")
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()
        try {
            client.newCall(req).execute().use { resp ->
                registered = resp.isSuccessful
                registered
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun search(query: String, limit: Int = 20): List<OffFood> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        registerUid()
        val url = buildString {
            append(baseUrl).append("/api/off/search/")
            append("?q=").append(java.net.URLEncoder.encode(query.trim(), "UTF-8"))
            append("&limit=").append(limit.coerceIn(1, 50))
        }
        val json = execute(url)
        val arr = json.optJSONArray("results") ?: return@withContext emptyList()
        buildList {
            for (i in 0 until arr.length()) {
                add(parseFood(arr.getJSONObject(i)))
            }
        }
    }

    /**
     * Look up a single product by its barcode. Returns null on 404.
     */
    suspend fun lookupBarcode(barcode: String): OffFood? = withContext(Dispatchers.IO) {
        registerUid()
        val url = "$baseUrl/api/off/product/${java.net.URLEncoder.encode(barcode, "UTF-8")}/"
        val json = try { execute(url) } catch (e: HttpStatusException) {
            if (e.code == 404) return@withContext null
            throw e
        }
        parseFood(json)
    }

    private fun execute(url: String): JSONObject {
        val req = buildRequest(url)
        client.newCall(req).execute().use { resp ->
            val body = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) {
                throw HttpStatusException(resp.code, body, url)
            }
            return JSONObject(body)
        }
    }

    private fun parseFood(obj: JSONObject): OffFood {
        val micros = LinkedHashMap<String, Double>()
        val microsObj = obj.optJSONObject("micros")
        if (microsObj != null) {
            for (key in microsObj.keys()) {
                val v = microsObj.optDouble(key, 0.0)
                if (v > 0.0) micros[key] = v
            }
        }
        return OffFood(
            type = obj.optString("type", "off"),
            barcode = obj.optString("barcode", ""),
            name = obj.optString("name", "Unknown"),
            brand = obj.optString("brand", ""),
            calories = obj.optDouble("calories", 0.0),
            proteinG = obj.optDouble("protein_g", 0.0),
            carbsG = obj.optDouble("carbs_g", 0.0),
            fatG = obj.optDouble("fat_g", 0.0),
            micros = micros,
            url = obj.optString("url", ""),
        )
    }

    companion object {
        private fun defaultClient(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()
    }
}

class HttpStatusException(val code: Int, val body: String, val url: String) :
    IOException("HTTP $code for $url")
