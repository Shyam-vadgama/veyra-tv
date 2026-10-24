package com.veyra.tv.data

import androidx.room.TypeConverter
import com.veyra.tv.model.PlaylistKind

class Converters {
    // Stored explicitly instead of enum.name()/valueOf() so R8 minification
    // cannot change the persisted values.
    @TypeConverter
    fun toKind(value: String?): PlaylistKind = when (value) {
        "XTREAM" -> PlaylistKind.XTREAM
        else -> PlaylistKind.M3U
    }

    @TypeConverter
    fun fromKind(kind: PlaylistKind): String = when (kind) {
        PlaylistKind.XTREAM -> "XTREAM"
        PlaylistKind.M3U -> "M3U"
    }
}
