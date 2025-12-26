package com.veyra.tv.data

import com.veyra.tv.model.Channel
import com.veyra.tv.model.Playlist
import kotlinx.coroutines.flow.Flow

class ChannelRepository(
    private val channelDao: ChannelDao,
    private val playlistDao: PlaylistDao,
    private val parser: M3uParser
) {
    private val jsonParser = JsonPlaylistParser()

    val allPlaylists: Flow<List<Playlist>> = playlistDao.getAllPlaylists()
    val selectedPlaylist: Flow<Playlist?> = playlistDao.getSelectedPlaylist()

    fun categories(playlistId: Int, countryCode: String?): Flow<List<String>> = channelDao.getCategoriesByCountry(playlistId, countryCode)
    fun countries(playlistId: Int): Flow<List<String>> = channelDao.getAvailableCountries(playlistId)
    fun recentChannels(playlistId: Int, limit: Int): Flow<List<Channel>> = channelDao.getRecentChannels(playlistId, limit)

    suspend fun updateLastWatched(streamUrl: String) {
        channelDao.updateLastWatched(streamUrl, System.currentTimeMillis())
    }

    suspend fun setupDefaultPlaylists(defaultPlaylists: List<Playlist>) {
        playlistDao.insertPlaylists(defaultPlaylists)
    }

    suspend fun streamAndSaveChannels(playlist: Playlist) {
        // Keep favorites in memory before clearing
        val currentFavorites = try {
            channelDao.getFavoriteChannelsSync(playlist.id).map { it.streamUrl }.toSet()
        } catch (e: Exception) {
            emptySet()
        }

        channelDao.clearChannelsForPlaylist(playlist.id)

        val onChunkLoaded: suspend (List<Channel>) -> Unit = { chunk ->
            val processedChunk = chunk.map { channel ->
                channel.copy(
                    isFavorite = currentFavorites.contains(channel.streamUrl)
                )
            }
            channelDao.insertChannels(processedChunk)
        }

        if (playlist.url == "internal://debug") {
            parser.parseString(DebugChannels.m3uContent, playlist.id, onChunkLoaded)
        } else if (playlist.url.endsWith(".json", ignoreCase = true)) {
            jsonParser.parseStream(playlist.url, playlist.id, onChunkLoaded)
        } else {
            parser.parseStream(playlist.url, playlist.id, onChunkLoaded)
        }
    }

    suspend fun toggleFavorite(streamUrl: String, isFavorite: Boolean) {
        channelDao.updateFavoriteStatus(streamUrl, isFavorite)
    }

    suspend fun selectPlaylist(playlistId: Int) {
        playlistDao.clearAllSelections()
        playlistDao.selectPlaylist(playlistId)
    }

    suspend fun findAlternativeChannels(channelName: String, excludeStreamUrl: String): List<Channel> {
        return channelDao.findAlternativeChannels(channelName, excludeStreamUrl)
    }
}
