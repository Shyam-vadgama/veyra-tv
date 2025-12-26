package com.veyra.tv.data

import androidx.room.*
import com.veyra.tv.model.Playlist
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists")
    fun getAllPlaylists(): Flow<List<Playlist>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: Playlist)
    
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

    @Query("SELECT * FROM playlists WHERE isSelected = 1 LIMIT 1")
    fun getSelectedPlaylist(): Flow<Playlist?>
}
