package com.example.data.dao

import androidx.room.*
import com.example.data.model.FavoriteEntity
import com.example.data.model.RecentFileEntity
import com.example.data.model.SettingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE path = :path)")
    fun isFavorite(path: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE path = :path)")
    suspend fun isFavoriteDirect(path: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE path = :path")
    suspend fun deleteFavoriteByPath(path: String)
}

@Dao
interface RecentFileDao {
    @Query("SELECT * FROM recent_files ORDER BY accessedAt DESC LIMIT 100")
    fun getRecentFiles(): Flow<List<RecentFileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun recordAccess(recentFile: RecentFileEntity)

    @Query("DELETE FROM recent_files WHERE path = :path")
    suspend fun deleteRecentByPath(path: String)

    @Query("DELETE FROM recent_files")
    suspend fun clearAll()
}

@Dao
interface SettingDao {
    @Query("SELECT value FROM app_settings WHERE `key` = :key")
    fun getSetting(key: String): Flow<String?>

    @Query("SELECT value FROM app_settings WHERE `key` = :key")
    suspend fun getSettingDirect(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSetting(setting: SettingEntity)
}
