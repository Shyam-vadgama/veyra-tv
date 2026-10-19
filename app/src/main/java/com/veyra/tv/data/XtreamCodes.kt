package com.veyra.tv.data

/**
 * Helpers for the Xtream Codes login format.
 *
 * Veyra TV is an M3U/JSON player, so Xtream credentials are reduced to the
 * standard endpoint URLs here and then flow through the existing parsing and
 * playback pipeline unchanged.
 */
object XtreamCodes {

    /** Trims, defaults a missing scheme to http, and removes a trailing slash. */
    fun normalizeServer(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return ""
        val withScheme = if (trimmed.contains("://")) trimmed else "http://$trimmed"
        return withScheme.trimEnd('/')
    }

    fun buildPlaylistUrl(server: String, username: String, password: String): String {
        val base = normalizeServer(server)
        return "$base/get.php?username=$username&password=$password&type=m3u_plus&output=ts"
    }

    fun buildEpgUrl(server: String, username: String, password: String): String {
        val base = normalizeServer(server)
        return "$base/xmltv.php?username=$username&password=$password"
    }

    fun isXtreamUrl(url: String): Boolean =
        url.contains("/get.php") && url.contains("username=")
}
