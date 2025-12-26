package com.veyra.tv.data

import com.veyra.tv.model.Channel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

class M3uParser {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    // Stream the response body line-by-line instead of buffering everything into a String
    suspend fun parseStream(url: String, playlistId: Int, onChunkLoaded: suspend (List<Channel>) -> Unit) = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .build()
            
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext

            val body = response.body ?: return@withContext
            
            // Process the stream
            parseInputStream(body, playlistId, onChunkLoaded)
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Parse directly from a string (for debug/internal playlists)
    suspend fun parseString(content: String, playlistId: Int, onChunkLoaded: suspend (List<Channel>) -> Unit) = withContext(Dispatchers.Default) {
        val reader = content.reader()
        val buffered = BufferedReader(reader)
        
        val chunk = mutableListOf<Channel>()
        val chunkSize = 500
        
        var line: String? = buffered.readLine()
        var currentName: String? = null
        var currentLogo: String? = null
        var currentGroup: String? = null
        var currentCountry: String? = null

        while (line != null) {
            line = line.trim()
            if (line.startsWith("#EXTINF:")) {
                // Parse metadata
                val info = line.substringAfter("#EXTINF:")
                val commaIndex = info.lastIndexOf(',')
                if (commaIndex != -1) {
                    currentName = info.substring(commaIndex + 1).trim()
                    
                    val attributes = info.substring(0, commaIndex)
                    currentLogo = extractAttribute(attributes, "tvg-logo")
                    currentGroup = extractAttribute(attributes, "group-title")
                    currentCountry = extractAttribute(attributes, "tvg-country")
                }
            } else if (!line.startsWith("#") && line.isNotEmpty()) {
                // This is the URL
                if (currentName != null) {
                    val category = normalizeCategory(currentGroup ?: "Uncategorized")
                    chunk.add(
                        Channel(
                            line,
                            currentName!!,
                            currentLogo,
                            category,
                            currentCountry?.uppercase(),
                            null, // lastWatched
                            false,
                            playlistId
                        )
                    )
                    
                    if (chunk.size >= chunkSize) {
                        onChunkLoaded(chunk.toList())
                        chunk.clear()
                    }

                    // Reset for next channel
                    currentName = null
                    currentLogo = null
                    currentGroup = null
                    currentCountry = null
                }
            }
            line = buffered.readLine()
        }
        
        if (chunk.isNotEmpty()) {
            onChunkLoaded(chunk.toList())
        }
        buffered.close()
    }

    private suspend fun parseInputStream(body: ResponseBody, playlistId: Int, onChunkLoaded: suspend (List<Channel>) -> Unit) {
        val inputStream = body.byteStream()
        val reader = BufferedReader(InputStreamReader(inputStream))
        
        val chunk = mutableListOf<Channel>()
        val chunkSize = 500 // Insert in batches to reduce DB transaction overhead
        
        var line: String? = reader.readLine()
        
        var currentName: String? = null
        var currentLogo: String? = null
        var currentGroup: String? = null
        var currentCountry: String? = null

        while (line != null) {
            line = line.trim()
            if (line.startsWith("#EXTINF:")) {
                // Parse metadata
                val info = line.substringAfter("#EXTINF:")
                val commaIndex = info.lastIndexOf(',')
                if (commaIndex != -1) {
                    currentName = info.substring(commaIndex + 1).trim()
                    
                    val attributes = info.substring(0, commaIndex)
                    currentLogo = extractAttribute(attributes, "tvg-logo")
                    currentGroup = extractAttribute(attributes, "group-title")
                    currentCountry = extractAttribute(attributes, "tvg-country")
                }
            } else if (!line.startsWith("#") && line.isNotEmpty()) {
                // This is the URL
                if (currentName != null) {
                    val category = normalizeCategory(currentGroup ?: "Uncategorized")
                    chunk.add(
                        Channel(
                            line,
                            currentName!!,
                            currentLogo,
                            category,
                            currentCountry?.uppercase(),
                            null, // lastWatched
                            false,
                            playlistId
                        )
                    )
                    
                    if (chunk.size >= chunkSize) {
                        onChunkLoaded(chunk.toList())
                        chunk.clear()
                    }

                    // Reset for next channel
                    currentName = null
                    currentLogo = null
                    currentGroup = null
                    currentCountry = null
                }
            }
            line = reader.readLine()
        }
        
        // Emit remaining items
        if (chunk.isNotEmpty()) {
            onChunkLoaded(chunk.toList())
        }
        
        reader.close()
        inputStream.close()
    }

    private fun extractAttribute(line: String, attribute: String): String? {
        val regex = "$attribute=\"([^\"]*)\"".toRegex()
        val match = regex.find(line)
        return match?.groupValues?.get(1)
    }

    private fun normalizeCategory(category: String): String {
        val lower = category.lowercase()
        return when {
            lower.contains("news") -> "News"
            lower.contains("sport") -> "Sports"
            lower.contains("movie") || lower.contains("cinema") || lower.contains("film") -> "Movies"
            lower.contains("kid") || lower.contains("child") || lower.contains("cartoon") -> "Kids"
            lower.contains("music") -> "Music"
            lower.contains("doc") -> "Documentary"
            lower.contains("entertain") -> "Entertainment"
            lower == "uncategorized" || lower == "" -> "Other"
            else -> category
        }
    }
}
