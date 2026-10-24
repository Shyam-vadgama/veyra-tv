package com.veyra.tv.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.veyra.tv.model.Channel
import com.veyra.tv.model.Playlist

@Database(entities = [Channel::class, Playlist::class], version = 6, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun channelDao(): ChannelDao
    abstract fun playlistDao(): PlaylistDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE playlists ADD COLUMN kind TEXT NOT NULL DEFAULT 'M3U'")
                db.execSQL("ALTER TABLE playlists ADD COLUMN serverUrl TEXT")
                db.execSQL("ALTER TABLE playlists ADD COLUMN username TEXT")
                db.execSQL("ALTER TABLE playlists ADD COLUMN password TEXT")
                db.execSQL("ALTER TABLE playlists ADD COLUMN epgUrl TEXT")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "iptv_player_database"
                )
                .enableMultiInstanceInvalidation()
                .addMigrations(MIGRATION_5_6)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
