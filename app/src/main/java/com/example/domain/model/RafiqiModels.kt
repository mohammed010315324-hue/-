package com.example.domain.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

enum class FileCategory(val arabicTitle: String, val englishTitle: String, val color: Color) {
    IMAGES("الصور", "Images", CatImages),
    VIDEOS("الفيديوهات", "Videos", CatVideos),
    AUDIO("الصوت", "Audio", CatAudio),
    DOCUMENTS("المستندات", "Documents", CatDocs),
    DOWNLOADS("التنزيلات", "Downloads", CatDownloads),
    ARCHIVES("الملفات المضغوطة", "Archives", CatArchives),
    APPS("التطبيقات", "Apps", CatApps),
    OTHER("ملفات أخرى", "Other", CatOther);

    companion object {
        fun fromExtension(ext: String): FileCategory {
            val lower = ext.lowercase()
            return when (lower) {
                "jpg", "jpeg", "png", "gif", "webp", "bmp", "heic", "svg" -> IMAGES
                "mp4", "mkv", "webm", "avi", "mov", "3gp", "flv", "wmv" -> VIDEOS
                "mp3", "m4a", "wav", "aac", "flac", "ogg", "opus", "wma" -> AUDIO
                "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "rtf", "csv", "epub" -> DOCUMENTS
                "zip", "rar", "7z", "tar", "gz", "bz2", "xz" -> ARCHIVES
                "apk", "xapk", "apks" -> APPS
                else -> OTHER
            }
        }
    }
}

data class RafiqiFile(
    val path: String,
    val name: String,
    val isDirectory: Boolean,
    val size: Long,
    val lastModified: Long,
    val extension: String,
    val mimeType: String,
    val category: FileCategory,
    val isFavorite: Boolean = false,
    val exists: Boolean = true,
    val itemCount: Int = 0 // For directories: number of children
)

data class StorageInfo(
    val totalBytes: Long = 0L,
    val usedBytes: Long = 0L,
    val freeBytes: Long = 0L
) {
    val usedPercent: Float
        get() = if (totalBytes > 0) (usedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f) else 0f
}

enum class SortOption(val arabicTitle: String) {
    NAME_ASC("الاسم (أ - ي)"),
    NAME_DESC("الاسم (ي - أ)"),
    DATE_DESC("الأحدث أولاً"),
    DATE_ASC("الأقدم أولاً"),
    SIZE_DESC("الأكبر حجماً"),
    SIZE_ASC("الأصغر حجماً")
}

enum class ViewMode {
    LIST,
    GRID
}

enum class CleanReason(val arabicTitle: String) {
    LARGE_FILE("ملف كبير الحجم"),
    DUPLICATE("ملف مكرر"),
    TEMP_CACHE("ملف مؤقت أو ذاكرة تخزين مؤقتة"),
    EMPTY_FOLDER("مجلد فارغ")
}

data class CleanItem(
    val file: RafiqiFile,
    val reason: CleanReason,
    val isSelected: Boolean = false
)

data class DuplicateGroup(
    val size: Long,
    val files: List<RafiqiFile>
)
