package com.veyra.tv.data

import androidx.room.*
import com.veyra.tv.model.Playlist
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists")
    fun getAllPlaylists(): Flow<List<Playlist>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPlaylist(playlist: Playlist): Long

    @Query("SELECT COUNT(*) FROM playlists")
    suspend fun getPlaylistCount(): Int

    @Query("SELECT COUNT(*) FROM playlists WHERE url = :url")
    suspend fun countByUrl(url: String): Int
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPlaylists(playlists: List<Playlist>)

    @Update
    suspend fun updatePlaylist(playlist: Playlist)

    @Delete
    suspend fun deletePlaylist(playlist: Playlist)
    
    @Query("UPDATE playlists SET isSelected = 0")
    suspend fun clearAllSelections()

    @Query("UPDATE playlists SET isSelected = 1 WHERE id = :playlistId")
    suspend fun selectPlaylist(playlistId: Int)

    /** Clears the old selection and sets the new one in a single atomic statement. */
    @Query("UPDATE playlists SET isSelected = CASE WHEN id = :playlistId THEN 1 ELSE 0 END")
    suspend fun selectOnly(playlistId: Int)

    @Query("SELECT * FROM playlists WHERE isSelected = 1 LIMIT 1")
    fun getSelectedPlaylist(): Flow<Playlist?>
}
