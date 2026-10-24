package com.veyra.tv.data

import androidx.room.TypeConverter
import com.veyra.tv.model.PlaylistKind

class Converters {
    @TypeConverter fun toKind(value: String?): PlaylistKind =
        if (value == null) PlaylistKind.M3U else PlaylistKind.valueOf(value)

    @TypeConverter fun fromKind(kind: PlaylistKind): String = kind.name
}
