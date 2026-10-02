package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val path: String,
    val name: String,
    val isDirectory: Boolean,
    val size: Long,
    val lastModified: Long,
    val extension: String,
    val mimeType: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "recent_files")
data class RecentFileEntity(
    @PrimaryKey val path: String,
    val name: String,
    val isDirectory: Boolean,
    val size: Long,
    val lastModified: Long,
    val extension: String,
    val mimeType: String,
    val accessedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_settings")
data class SettingEntity(
    @PrimaryKey val key: String,
    val value: String
)
