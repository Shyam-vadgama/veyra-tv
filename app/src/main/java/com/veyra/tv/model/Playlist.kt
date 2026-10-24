package com.veyra.tv.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class PlaylistKind { M3U, XTREAM }

@Entity(tableName = "playlists")
data class Playlist(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val url: String,
    val isSelected: Boolean = false,
    val kind: PlaylistKind = PlaylistKind.M3U,
    val serverUrl: String? = null,
    val username: String? = null,
    val password: String? = null,
    val epgUrl: String? = null
)
