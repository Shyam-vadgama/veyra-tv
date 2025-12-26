package com.veyra.tv.data

import android.util.JsonReader
import android.util.JsonToken
import android.util.Log
import com.veyra.tv.model.Channel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

class JsonPlaylistParser {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun parseStream(url: String, playlistId: Int, onChunkLoaded: suspend (List<Channel>) -> Unit) = withContext(Dispatchers.IO) {
        try {
            Log.d("JsonPlaylistParser", "Starting download: $url")
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .build()
            
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.e("JsonPlaylistParser", "Network error: ${response.code}")
                return@withContext
            }

            val body = response.body
            if (body == null) {
                Log.e("JsonPlaylistParser", "Empty body")
                return@withContext
            }
            
            parseJsonStream(body, playlistId, onChunkLoaded)
            
        } catch (e: Exception) {
            Log.e("JsonPlaylistParser", "Exception during download", e)
            e.printStackTrace()
        }
    }

    private suspend fun parseJsonStream(body: ResponseBody, playlistId: Int, onChunkLoaded: suspend (List<Channel>) -> Unit) {
        val inputStream = body.byteStream()
        val reader = JsonReader(InputStreamReader(inputStream, "UTF-8"))
        reader.isLenient = true
        
        val chunk = mutableListOf<Channel>()
        val chunkSize = 500
        var count = 0
        
        try {
            reader.beginArray()
            while (reader.hasNext()) {
                try {
                    val channel = readChannel(reader, playlistId)
                    if (channel != null) {
                        chunk.add(channel)
                        count++
                    }

                    if (chunk.size >= chunkSize) {
                        onChunkLoaded(chunk.toList())
                        chunk.clear()
                    }
                } catch (e: Exception) {
                    // Skip malformed item
                    Log.e("JsonPlaylistParser", "Error parsing item", e)
                }
            }
            reader.endArray()
            
            if (chunk.isNotEmpty()) {
                onChunkLoaded(chunk.toList())
            }
            Log.d("JsonPlaylistParser", "Parsed $count channels")
        } catch (e: Exception) {
            Log.e("JsonPlaylistParser", "Error parsing stream", e)
            e.printStackTrace()
        } finally {
            try {
                reader.close()
                inputStream.close()
                body.close()
            } catch (e: Exception) {}
        }
    }

    private fun readChannel(reader: JsonReader, playlistId: Int): Channel? {
        var name: String? = null
        var url: String? = null
        var logo: String? = null
        var category: String? = "Other"
        var country: String? = null

        reader.beginObject()
        while (reader.hasNext()) {
            val propertyName = reader.nextName()
            if (reader.peek() == JsonToken.NULL) {
                reader.nextNull()
                continue
            }

            when (propertyName) {
                "name" -> name = reader.nextString()
                "url" -> url = reader.nextString()
                "logo" -> logo = reader.nextString()
                "category" -> category = reader.nextString()
                "country" -> {
                    // Handle country object or string. In iptv-org json it might be an object or id.
                    // But based on previous output "country": null. 
                    // Let's try to read it as string if it's a primitive.
                    // If it is an object, we skip.
                    if (reader.peek() == JsonToken.BEGIN_OBJECT) {
                        reader.skipValue() 
                    } else {
                        country = reader.nextString()
                    }
                }
                else -> reader.skipValue()
            }
        }
        reader.endObject()

        if (name != null && url != null) {
            val finalCountry = country ?: if (category != null && category != "Other") category else null
            
            return Channel(
                streamUrl = url,
                name = name,
                logoUrl = logo,
                category = normalizeCategory(category ?: "Other"),
                country = finalCountry?.uppercase(),
                isFavorite = false,
                playlistId = playlistId
            )
        }
        return null
    }

    private fun normalizeCategory(category: String): String {
        return category
    }
}