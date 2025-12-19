package com.example.iptv_player.data

import com.example.iptv_player.model.Channel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedReader
import java.io.StringReader
import java.util.concurrent.TimeUnit

class M3uParser {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun parse(url: String): List<Channel> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .build()
            
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext emptyList()
            
            val content = response.body?.string() ?: return@withContext emptyList()

            val channels = mutableListOf<Channel>()
            val reader = BufferedReader(StringReader(content))
            var line: String? = reader.readLine()
            
            var currentName: String? = null
            var currentLogo: String? = null
            var currentGroup: String? = null

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
                    }
                } else if (!line.startsWith("#") && line.isNotEmpty()) {
                    // This is the URL
                    if (currentName != null) {
                        val category = normalizeCategory(currentGroup ?: "Uncategorized")
                        channels.add(
                            Channel(
                                name = currentName,
                                logoUrl = currentLogo,
                                streamUrl = line,
                                category = category
                            )
                        )
                        // Reset for next channel
                        currentName = null
                        currentLogo = null
                        currentGroup = null
                    }
                }
                line = reader.readLine()
            }
            channels
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
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
            else -> category // Keep original if it doesn't match common ones, or could map to "Other"
        }
    }
}
