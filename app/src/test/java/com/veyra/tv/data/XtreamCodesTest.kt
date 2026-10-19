package com.veyra.tv.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class XtreamCodesTest {
    @Test fun normalize_adds_scheme_and_strips_trailing_slash() {
        assertEquals("http://host.example:8080", XtreamCodes.normalizeServer("  host.example:8080/  "))
    }

    @Test fun normalize_keeps_https() {
        assertEquals("https://host.example", XtreamCodes.normalizeServer("https://host.example/"))
    }

    @Test fun build_playlist_url_uses_get_php() {
        assertEquals(
            "http://host.example:8080/get.php?username=user1&password=pass1&type=m3u_plus&output=ts",
            XtreamCodes.buildPlaylistUrl("http://host.example:8080", "user1", "pass1")
        )
    }

    @Test fun build_epg_url_uses_xmltv_php() {
        assertEquals(
            "http://host.example:8080/xmltv.php?username=user1&password=pass1",
            XtreamCodes.buildEpgUrl("http://host.example:8080", "user1", "pass1")
        )
    }

    @Test fun detects_xtream_urls() {
        assertTrue(XtreamCodes.isXtreamUrl("http://h/get.php?username=u&password=p&type=m3u_plus"))
        assertFalse(XtreamCodes.isXtreamUrl("http://h/iptv/index.m3u"))
    }
}
