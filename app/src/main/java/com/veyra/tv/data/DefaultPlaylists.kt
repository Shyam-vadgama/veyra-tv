package com.veyra.tv.data

import com.veyra.tv.model.Playlist

object DefaultPlaylists {

    private val topPriorityUrls = listOf(
        "https://shyam-vadgama.github.io/iptv-json-parser/channels.json", // Fast JSON
        "https://iptv-org.github.io/iptv/index.country.m3u",
        "https://iptv-org.github.io/iptv/index.language.m3u"
    )

    fun getPlaylists(): List<Playlist> {
        return topPriorityUrls.mapIndexed { index, url ->
            val name = when {
                url.contains("channels.json") -> "IPTV-ORG (JSON - Fast)"
                url.contains("index.country") -> "IPTV-ORG (Country)"
                url.contains("index.language") -> "IPTV-ORG (Language)"
                else -> "Playlist ${index + 1}"
            }
            Playlist(name = name, url = url.trim(), isSelected = url.contains("channels.json"))
        }
    }
}