package com.veyra.tv.data

import com.veyra.tv.model.Channel
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class M3uParserTest {
    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private val m3u = """
        #EXTM3U
        #EXTINF:-1 tvg-logo="http://logo" group-title="News",BBC News
        http://stream/bbc
        #EXTINF:-1 group-title="Sports",Sky Sports
        http://stream/sky
    """.trimIndent()

    @Test
    fun parseStream_returns_true_and_emits_channels_on_success() = runBlocking {
        server.enqueue(MockResponse().setBody(m3u))
        val channels = mutableListOf<Channel>()

        val ok = M3uParser().parseStream(server.url("/list.m3u").toString(), 7) { channels += it }

        assertTrue(ok)
        assertEquals(2, channels.size)
        assertEquals("BBC News", channels[0].name)
        assertEquals(7, channels[0].playlistId)
    }

    @Test
    fun parseStream_returns_false_on_http_error() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(500))
        val channels = mutableListOf<Channel>()

        val ok = M3uParser().parseStream(server.url("/list.m3u").toString(), 1) { channels += it }

        assertFalse(ok)
        assertTrue(channels.isEmpty())
    }

    @Test
    fun parseStream_returns_false_when_no_channels_are_found() = runBlocking {
        // e.g. a captive portal or an error page served with HTTP 200
        server.enqueue(MockResponse().setBody("<html><body>Blocked</body></html>"))
        val channels = mutableListOf<Channel>()

        val ok = M3uParser().parseStream(server.url("/list.m3u").toString(), 1) { channels += it }

        assertFalse(ok)
        assertTrue(channels.isEmpty())
    }

    @Test
    fun parseString_returns_true_and_emits_channels() = runBlocking {
        val channels = mutableListOf<Channel>()

        val ok = M3uParser().parseString(m3u, 3) { channels += it }

        assertTrue(ok)
        assertEquals(2, channels.size)
        assertEquals(3, channels[0].playlistId)
    }
}
