package com.veyra.tv.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.Locale

object CountryRepository {
    private val client = OkHttpClient()

    suspend fun getUserCountry(): String? = withContext(Dispatchers.IO) {
        // 1. Try IP Geolocation (HTTPS)
        try {
            val request = Request.Builder().url("https://ipwho.is/").build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val json = response.body?.string()
                if (json != null) {
                    val jsonObj = JSONObject(json)
                    // ipwho.is returns "country_code" (e.g., "IN")
                    val code = jsonObj.optString("country_code")
                    if (code.isNotEmpty()) return@withContext code
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Fallback to System Locale
        try {
            val country = Locale.getDefault().country
            if (country.isNotEmpty()) {
                // Ensure 2-letter uppercase code
                return@withContext country.uppercase()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return@withContext null
    }
}