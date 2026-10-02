package com.example.data

import com.example.data.dao.FavoriteDao
import com.example.data.dao.RecentFileDao
import com.example.data.dao.SettingDao
import com.example.data.model.FavoriteEntity
import com.example.data.model.RecentFileEntity
import com.example.data.model.SettingEntity
import com.example.domain.model.FileCategory
import com.example.domain.model.RafiqiFile
import com.example.domain.model.SortOption
import com.example.domain.model.ViewMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File

class RafiqiRepository(
    private val favoriteDao: FavoriteDao,
    private val recentFileDao: RecentFileDao,
    private val settingDao: SettingDao
) {
    val favorites: Flow<List<RafiqiFile>> = favoriteDao.getAllFavorites().map { list ->
        list.map { entity ->
            val file = File(entity.path)
            RafiqiFile(
                path = entity.path,
                name = entity.name,
                isDirectory = entity.isDirectory,
                size = if (file.exists()) file.length() else entity.size,
                lastModified = if (file.exists()) file.lastModified() else entity.lastModified,
                extension = entity.extension,
                mimeType = entity.mimeType,
                category = FileCategory.fromExtension(entity.extension),
                isFavorite = true,
                exists = file.exists(),
                itemCount = if (file.isDirectory) (file.listFiles()?.size ?: 0) else 0
            )
        }
    }

    val recentFiles: Flow<List<RafiqiFile>> = recentFileDao.getRecentFiles().map { list ->
        list.map { entity ->
            val file = File(entity.path)
            RafiqiFile(
                path = entity.path,
                name = entity.name,
                isDirectory = entity.isDirectory,
                size = if (file.exists()) file.length() else entity.size,
                lastModified = if (file.exists()) file.lastModified() else entity.lastModified,
                extension = entity.extension,
                mimeType = entity.mimeType,
                category = FileCategory.fromExtension(entity.extension),
                isFavorite = false,
                exists = file.exists(),
                itemCount = if (file.isDirectory) (file.listFiles()?.size ?: 0) else 0
            )
        }
    }

    suspend fun toggleFavorite(file: RafiqiFile) = withContext(Dispatchers.IO) {
        val isFav = favoriteDao.isFavoriteDirect(file.path)
        if (isFav) {
            favoriteDao.deleteFavoriteByPath(file.path)
        } else {
            favoriteDao.insertFavorite(
                FavoriteEntity(
                    path = file.path,
                    name = file.name,
                    isDirectory = file.isDirectory,
                    size = file.size,
                    lastModified = file.lastModified,
                    extension = file.extension,
                    mimeType = file.mimeType
                )
            )
        }
    }

    suspend fun removeFavorite(path: String) = withContext(Dispatchers.IO) {
        favoriteDao.deleteFavoriteByPath(path)
    }

    suspend fun recordRecent(file: RafiqiFile) = withContext(Dispatchers.IO) {
        recentFileDao.recordAccess(
            RecentFileEntity(
                path = file.path,
                name = file.name,
                isDirectory = file.isDirectory,
                size = file.size,
                lastModified = file.lastModified,
                extension = file.extension,
                mimeType = file.mimeType,
                accessedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun removeRecent(path: String) = withContext(Dispatchers.IO) {
        recentFileDao.deleteRecentByPath(path)
    }

    suspend fun clearRecents() = withContext(Dispatchers.IO) {
        recentFileDao.clearAll()
    }

    fun isFavorite(path: String): Flow<Boolean> = favoriteDao.isFavorite(path)

    // Settings
    fun getThemeMode(): Flow<String> = settingDao.getSetting("theme_mode").map { it ?: "SYSTEM" }
    suspend fun setThemeMode(mode: String) = withContext(Dispatchers.IO) {
        settingDao.saveSetting(SettingEntity("theme_mode", mode))
    }

    fun getViewMode(): Flow<ViewMode> = settingDao.getSetting("view_mode").map {
        if (it == "GRID") ViewMode.GRID else ViewMode.LIST
    }
    suspend fun setViewMode(mode: ViewMode) = withContext(Dispatchers.IO) {
        settingDao.saveSetting(SettingEntity("view_mode", mode.name))
    }

    fun getSortOption(): Flow<SortOption> = settingDao.getSetting("sort_option").map {
        try {
            if (it != null) SortOption.valueOf(it) else SortOption.DATE_DESC
        } catch (e: Exception) {
            SortOption.DATE_DESC
        }
    }
    suspend fun setSortOption(sort: SortOption) = withContext(Dispatchers.IO) {
        settingDao.saveSetting(SettingEntity("sort_option", sort.name))
    }

    fun getConfirmDelete(): Flow<Boolean> = settingDao.getSetting("confirm_delete").map {
        it?.toBooleanStrictOrNull() ?: true
    }
    suspend fun setConfirmDelete(confirm: Boolean) = withContext(Dispatchers.IO) {
        settingDao.saveSetting(SettingEntity("confirm_delete", confirm.toString()))
    }
}
