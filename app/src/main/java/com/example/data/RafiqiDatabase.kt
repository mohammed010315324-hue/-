package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.FavoriteDao
import com.example.data.dao.RecentFileDao
import com.example.data.dao.SettingDao
import com.example.data.model.FavoriteEntity
import com.example.data.model.RecentFileEntity
import com.example.data.model.SettingEntity

@Database(
    entities = [FavoriteEntity::class, RecentFileEntity::class, SettingEntity::class],
    version = 1,
    exportSchema = false
)
abstract class RafiqiDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun recentFileDao(): RecentFileDao
    abstract fun settingDao(): SettingDao

    companion object {
        @Volatile
        private var INSTANCE: RafiqiDatabase? = null

        fun getInstance(context: Context): RafiqiDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RafiqiDatabase::class.java,
                    "rafiqi_database.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
