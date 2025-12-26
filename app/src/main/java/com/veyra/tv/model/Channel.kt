package com.veyra.tv.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "channels",
    indices = [
        Index(value = ["category"]),
        Index(value = ["name"]),
        Index(value = ["isFavorite"]),
        Index(value = ["playlistId"]) // Index for faster filtering by playlist
    ],
    foreignKeys = [
        ForeignKey(
            entity = Playlist::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE // If a playlist is deleted, its channels are also deleted
        )
    ]
)
data class Channel(
    @PrimaryKey
    val streamUrl: String,
    val name: String,
    val logoUrl: String?,
    val category: String,
    val country: String? = null,
    val lastWatched: Long? = null,
    val isFavorite: Boolean = false,
    val playlistId: Int // Foreign key to link to a Playlist
)
