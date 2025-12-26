package com.veyra.tv.data

import androidx.paging.PagingSource
import androidx.room.*
import com.veyra.tv.model.Channel
import kotlinx.coroutines.flow.Flow

@Dao
interface ChannelDao {
    @Query("SELECT * FROM channels WHERE playlistId = :playlistId")
    fun getAllChannelsPagingSource(playlistId: Int): PagingSource<Int, Channel>

    @Query("SELECT * FROM channels WHERE playlistId = :playlistId AND (country = :countryCode OR :countryCode IS NULL)")
    fun getAllChannelsByCountryPagingSource(playlistId: Int, countryCode: String?): PagingSource<Int, Channel>

    @Query("SELECT * FROM channels WHERE category = :category AND playlistId = :playlistId AND (country = :countryCode OR :countryCode IS NULL)")
    fun getChannelsByCategoryAndCountryPagingSource(category: String, playlistId: Int, countryCode: String?): PagingSource<Int, Channel>

    @Query("SELECT * FROM channels WHERE category = :category AND playlistId = :playlistId")
    fun getChannelsByCategoryPagingSource(category: String, playlistId: Int): PagingSource<Int, Channel>

    @Query("SELECT * FROM channels WHERE isFavorite = 1 AND playlistId = :playlistId")
    fun getFavoriteChannelsPagingSource(playlistId: Int): PagingSource<Int, Channel>

    @Query("SELECT * FROM channels WHERE name LIKE '%' || :query || '%' AND playlistId = :playlistId")
    fun searchChannelsPagingSource(query: String, playlistId: Int): PagingSource<Int, Channel>

    @Query("SELECT * FROM channels WHERE name LIKE '%' || :query || '%'")
    fun searchAllChannelsPagingSource(query: String): PagingSource<Int, Channel>

    // New function for fallback search
    @Query("SELECT * FROM channels WHERE name LIKE '%' || :channelName || '%' AND streamUrl != :excludeStreamUrl")
    suspend fun findAlternativeChannels(channelName: String, excludeStreamUrl: String): List<Channel>

    @Query("SELECT DISTINCT category FROM channels WHERE playlistId = :playlistId AND (country = :countryCode OR :countryCode IS NULL) ORDER BY category ASC")
    fun getCategoriesByCountry(playlistId: Int, countryCode: String?): Flow<List<String>>

    @Query("SELECT DISTINCT country FROM channels WHERE playlistId = :playlistId AND country IS NOT NULL ORDER BY country ASC")
    fun getAvailableCountries(playlistId: Int): Flow<List<String>>

    @Query("SELECT * FROM channels WHERE playlistId = :playlistId AND lastWatched IS NOT NULL ORDER BY lastWatched DESC LIMIT :limit")
    fun getRecentChannels(playlistId: Int, limit: Int): Flow<List<Channel>>

    @Query("UPDATE channels SET lastWatched = :timestamp WHERE streamUrl = :streamUrl")
    suspend fun updateLastWatched(streamUrl: String, timestamp: Long)

    @Query("SELECT * FROM channels WHERE isFavorite = 1 AND playlistId = :playlistId")
    fun getFavoriteChannels(playlistId: Int): Flow<List<Channel>>

    @Query("SELECT * FROM channels WHERE isFavorite = 1 AND playlistId = :playlistId")
    suspend fun getFavoriteChannelsSync(playlistId: Int): List<Channel>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannels(channels: List<Channel>)

    @Query("UPDATE channels SET isFavorite = :isFavorite WHERE streamUrl = :streamUrl")
    suspend fun updateFavoriteStatus(streamUrl: String, isFavorite: Boolean)

    @Query("DELETE FROM channels WHERE playlistId = :playlistId")
    suspend fun clearChannelsForPlaylist(playlistId: Int)
    
    @Query("SELECT COUNT(*) FROM channels WHERE playlistId = :playlistId")
    suspend fun getChannelCountForPlaylist(playlistId: Int): Int
}
