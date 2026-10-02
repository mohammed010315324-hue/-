package com.example.service

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.os.StatFs
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import com.example.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class FileManagerService(private val context: Context) {

    fun getDefaultStorageDir(): File {
        return try {
            val ext = Environment.getExternalStorageDirectory()
            if (ext != null && ext.exists() && ext.canRead()) {
                ext
            } else {
                context.getExternalFilesDir(null) ?: context.filesDir
            }
        } catch (e: Exception) {
            context.filesDir
        }
    }

    fun getStorageInfo(): StorageInfo {
        return try {
            val path = Environment.getDataDirectory()
            val stat = StatFs(path.path)
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            val availableBlocks = stat.availableBlocksLong

            val totalBytes = totalBlocks * blockSize
            val freeBytes = availableBlocks * blockSize
            val usedBytes = (totalBytes - freeBytes).coerceAtLeast(0L)

            StorageInfo(totalBytes = totalBytes, usedBytes = usedBytes, freeBytes = freeBytes)
        } catch (e: Exception) {
            StorageInfo(totalBytes = 64L * 1024 * 1024 * 1024, usedBytes = 32L * 1024 * 1024 * 1024, freeBytes = 32L * 1024 * 1024 * 1024)
        }
    }

    suspend fun listFiles(
        directory: File,
        sortOption: SortOption = SortOption.NAME_ASC
    ): List<RafiqiFile> = withContext(Dispatchers.IO) {
        if (!directory.exists() || !directory.canRead()) {
            return@withContext emptyList()
        }

        val rawFiles = directory.listFiles() ?: return@withContext emptyList()
        val mapped = rawFiles.map { file ->
            toRafiqiFile(file)
        }

        sortFiles(mapped, sortOption)
    }

    fun toRafiqiFile(file: File): RafiqiFile {
        val ext = file.extension.lowercase()
        val mime = getMimeType(file)
        val isDir = file.isDirectory
        val children = if (isDir) file.listFiles()?.size ?: 0 else 0
        return RafiqiFile(
            path = file.absolutePath,
            name = file.name,
            isDirectory = isDir,
            size = if (isDir) 0L else file.length(),
            lastModified = file.lastModified(),
            extension = ext,
            mimeType = mime,
            category = if (isDir) FileCategory.OTHER else FileCategory.fromExtension(ext),
            exists = true,
            itemCount = children
        )
    }

    private fun sortFiles(files: List<RafiqiFile>, sortOption: SortOption): List<RafiqiFile> {
        val (dirs, nonDirs) = files.partition { it.isDirectory }
        val sortedDirs = when (sortOption) {
            SortOption.NAME_ASC -> dirs.sortedBy { it.name.lowercase() }
            SortOption.NAME_DESC -> dirs.sortedByDescending { it.name.lowercase() }
            SortOption.DATE_DESC -> dirs.sortedByDescending { it.lastModified }
            SortOption.DATE_ASC -> dirs.sortedBy { it.lastModified }
            SortOption.SIZE_DESC -> dirs.sortedByDescending { it.itemCount }
            SortOption.SIZE_ASC -> dirs.sortedBy { it.itemCount }
        }

        val sortedNonDirs = when (sortOption) {
            SortOption.NAME_ASC -> nonDirs.sortedBy { it.name.lowercase() }
            SortOption.NAME_DESC -> nonDirs.sortedByDescending { it.name.lowercase() }
            SortOption.DATE_DESC -> nonDirs.sortedByDescending { it.lastModified }
            SortOption.DATE_ASC -> nonDirs.sortedBy { it.lastModified }
            SortOption.SIZE_DESC -> nonDirs.sortedByDescending { it.size }
            SortOption.SIZE_ASC -> nonDirs.sortedBy { it.size }
        }

        return sortedDirs + sortedNonDirs
    }

    suspend fun createFolder(parentDir: File, folderName: String): Result<File> = withContext(Dispatchers.IO) {
        try {
            val cleanName = folderName.trim()
            if (cleanName.isEmpty()) {
                return@withContext Result.failure(IllegalArgumentException("اسم المجلد لا يمكن أن يكون فارغاً"))
            }
            val target = File(parentDir, cleanName)
            if (target.exists()) {
                return@withContext Result.failure(IllegalStateException("يوجد مجلد أو ملف بهذا الاسم بالفعل"))
            }
            if (target.mkdirs()) {
                Result.success(target)
            } else {
                Result.failure(IOException("تعذر إنشاء المجلد، تحقق من الأذونات"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun rename(file: File, newName: String): Result<File> = withContext(Dispatchers.IO) {
        try {
            val cleanName = newName.trim()
            if (cleanName.isEmpty()) {
                return@withContext Result.failure(IllegalArgumentException("الاسم الجديد غير صالح"))
            }
            val parent = file.parentFile ?: return@withContext Result.failure(IOException("المجلد الأصلي غير متاح"))
            val target = File(parent, cleanName)
            if (target.exists()) {
                return@withContext Result.failure(IllegalStateException("يوجد عنصر بهذا الاسم بالفعل"))
            }
            if (file.renameTo(target)) {
                Result.success(target)
            } else {
                Result.failure(IOException("تعذر إعادة التسمية"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun delete(file: File): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            if (!file.exists()) {
                return@withContext Result.failure(FileNotFoundException("الملف غير موجود"))
            }
            val success = file.deleteRecursively()
            if (success) {
                Result.success(true)
            } else {
                Result.failure(IOException("تعذر حذف الملف"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun copy(source: File, destFolder: File): Result<File> = withContext(Dispatchers.IO) {
        try {
            if (!source.exists()) {
                return@withContext Result.failure(FileNotFoundException("الملف المصدر غير موجود"))
            }
            if (!destFolder.exists() && !destFolder.mkdirs()) {
                return@withContext Result.failure(IOException("مجلد الوجهة غير موجود"))
            }

            var destFile = File(destFolder, source.name)
            if (destFile.exists()) {
                val nameWithoutExt = source.nameWithoutExtension
                val ext = if (source.extension.isNotEmpty()) ".${source.extension}" else ""
                var counter = 1
                while (destFile.exists()) {
                    destFile = File(destFolder, "$nameWithoutExt (نسخة $counter)$ext")
                    counter++
                }
            }

            if (source.isDirectory) {
                source.copyRecursively(destFile, overwrite = true)
            } else {
                source.copyTo(destFile, overwrite = true)
            }
            Result.success(destFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun move(source: File, destFolder: File): Result<File> = withContext(Dispatchers.IO) {
        try {
            if (!source.exists()) {
                return@withContext Result.failure(FileNotFoundException("الملف المصدر غير موجود"))
            }
            val target = File(destFolder, source.name)
            if (source.renameTo(target)) {
                return@withContext Result.success(target)
            }
            // Fallback for cross-device moves: copy then delete
            val copyResult = copy(source, destFolder)
            if (copyResult.isSuccess) {
                source.deleteRecursively()
                copyResult
            } else {
                Result.failure(IOException("حدث خطأ أثناء نقل الملف"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun zip(files: List<File>, destZip: File): Result<File> = withContext(Dispatchers.IO) {
        try {
            ZipOutputStream(BufferedOutputStream(FileOutputStream(destZip))).use { out ->
                for (file in files) {
                    addFileToZip(out, file, file.name)
                }
            }
            Result.success(destZip)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun addFileToZip(out: ZipOutputStream, file: File, entryName: String) {
        if (file.isDirectory) {
            val children = file.listFiles() ?: return
            for (child in children) {
                addFileToZip(out, child, "$entryName/${child.name}")
            }
        } else {
            FileInputStream(file).use { input ->
                out.putNextEntry(ZipEntry(entryName))
                input.copyTo(out)
                out.closeEntry()
            }
        }
    }

    suspend fun unzip(zipFile: File, destFolder: File): Result<File> = withContext(Dispatchers.IO) {
        try {
            if (!destFolder.exists()) destFolder.mkdirs()
            ZipInputStream(BufferedInputStream(FileInputStream(zipFile))).use { zin ->
                var entry = zin.nextEntry
                while (entry != null) {
                    val newFile = File(destFolder, entry.name)
                    // Guard against Zip Slip vulnerability
                    val canonicalDest = destFolder.canonicalPath
                    val canonicalNewFile = newFile.canonicalPath
                    if (!canonicalNewFile.startsWith(canonicalDest + File.separator)) {
                        throw SecurityException("محاولة استخراج غير آمنة: ${entry.name}")
                    }

                    if (entry.isDirectory) {
                        newFile.mkdirs()
                    } else {
                        newFile.parentFile?.mkdirs()
                        FileOutputStream(newFile).use { out ->
                            zin.copyTo(out)
                        }
                    }
                    zin.closeEntry()
                    entry = zin.nextEntry
                }
            }
            Result.success(destFolder)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun openFile(file: File): Result<Unit> {
        return try {
            if (!file.exists()) {
                return Result.failure(FileNotFoundException("الملف غير موجود"))
            }

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )

            val mimeType = getMimeType(file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(intent)
            Result.success(Unit)
        } catch (e: ActivityNotFoundException) {
            Result.failure(Exception("لا يوجد تطبيق مثبت على هاتفك يدعم فتح هذا النوع من الملفات"))
        } catch (e: Exception) {
            Result.failure(Exception("تعذر فتح الملف: ${e.localizedMessage}"))
        }
    }

    fun shareFiles(files: List<File>): Result<Unit> {
        return try {
            val validFiles = files.filter { it.exists() }
            if (validFiles.isEmpty()) {
                return Result.failure(FileNotFoundException("لا توجد ملفات صالحة للمشاركة"))
            }

            val intent = if (validFiles.size == 1) {
                val file = validFiles.first()
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                Intent(Intent.ACTION_SEND).apply {
                    type = getMimeType(file)
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            } else {
                val uris = ArrayList<Uri>()
                for (file in validFiles) {
                    uris.add(FileProvider.getUriForFile(context, "${context.packageName}.provider", file))
                }
                Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                    type = "*/*"
                    putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            }

            val chooser = Intent.createChooser(intent, "مشاركة بواسطة رفيقي").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun scanCategory(category: FileCategory): List<RafiqiFile> = withContext(Dispatchers.IO) {
        val rootDirs = getScanRoots()
        val results = mutableListOf<RafiqiFile>()

        fun walk(dir: File, depth: Int = 0) {
            if (depth > 6 || results.size > 500) return
            val list = dir.listFiles() ?: return
            for (f in list) {
                if (f.isDirectory) {
                    if (!f.name.startsWith(".")) {
                        walk(f, depth + 1)
                    }
                } else {
                    val rFile = toRafiqiFile(f)
                    if (category == FileCategory.DOWNLOADS) {
                        if (f.parentFile?.name?.equals("Download", ignoreCase = true) == true ||
                            f.parentFile?.name?.equals("Downloads", ignoreCase = true) == true
                        ) {
                            results.add(rFile)
                        }
                    } else if (rFile.category == category) {
                        results.add(rFile)
                    }
                }
            }
        }

        for (root in rootDirs) {
            walk(root)
        }
        results.sortedByDescending { it.lastModified }
    }

    suspend fun scanLargeFiles(thresholdBytes: Long = 10L * 1024 * 1024): List<RafiqiFile> = withContext(Dispatchers.IO) {
        val rootDirs = getScanRoots()
        val results = mutableListOf<RafiqiFile>()

        fun walk(dir: File, depth: Int = 0) {
            if (depth > 6 || results.size > 200) return
            val list = dir.listFiles() ?: return
            for (f in list) {
                if (f.isDirectory) {
                    if (!f.name.startsWith(".")) {
                        walk(f, depth + 1)
                    }
                } else if (f.length() >= thresholdBytes) {
                    results.add(toRafiqiFile(f))
                }
            }
        }

        for (root in rootDirs) {
            walk(root)
        }
        results.sortedByDescending { it.size }
    }

    suspend fun scanDuplicates(): List<DuplicateGroup> = withContext(Dispatchers.IO) {
        val rootDirs = getScanRoots()
        val sizeMap = mutableMapOf<Long, MutableList<File>>()

        fun walk(dir: File, depth: Int = 0) {
            if (depth > 6) return
            val list = dir.listFiles() ?: return
            for (f in list) {
                if (f.isDirectory) {
                    if (!f.name.startsWith(".")) {
                        walk(f, depth + 1)
                    }
                } else if (f.length() > 1024) { // Only files > 1KB
                    sizeMap.getOrPut(f.length()) { mutableListOf() }.add(f)
                }
            }
        }

        for (root in rootDirs) {
            walk(root)
        }

        // Only check hashes for size collisions
        val candidateGroups = sizeMap.filter { it.value.size >= 2 }
        val duplicateGroups = mutableListOf<DuplicateGroup>()

        for ((size, files) in candidateGroups) {
            val hashMap = mutableMapOf<String, MutableList<RafiqiFile>>()
            for (f in files) {
                val hash = calculateQuickHash(f)
                hashMap.getOrPut(hash) { mutableListOf() }.add(toRafiqiFile(f))
            }

            for ((_, matchingFiles) in hashMap) {
                if (matchingFiles.size >= 2) {
                    duplicateGroups.add(DuplicateGroup(size = size, files = matchingFiles))
                }
            }
        }

        duplicateGroups.sortedByDescending { it.size * it.files.size }
    }

    suspend fun scanCleanableFiles(): List<CleanItem> = withContext(Dispatchers.IO) {
        val items = mutableListOf<CleanItem>()
        val rootDirs = getScanRoots()

        // 1. Temporary and cache files
        val tempExtensions = setOf("tmp", "temp", "log", "bak", "cache", "crdownload", "part")
        fun walkTemp(dir: File, depth: Int = 0) {
            if (depth > 5) return
            val list = dir.listFiles() ?: return
            for (f in list) {
                if (f.isDirectory) {
                    val name = f.name.lowercase()
                    if (name == ".thumbnails" || name == "cache" || name == ".cache") {
                        f.listFiles()?.forEach { cf ->
                            if (!cf.isDirectory) {
                                items.add(CleanItem(toRafiqiFile(cf), CleanReason.TEMP_CACHE, isSelected = true))
                            }
                        }
                    } else if (!f.name.startsWith(".")) {
                        walkTemp(f, depth + 1)
                    }
                } else {
                    if (tempExtensions.contains(f.extension.lowercase())) {
                        items.add(CleanItem(toRafiqiFile(f), CleanReason.TEMP_CACHE, isSelected = true))
                    }
                }
            }
        }

        for (root in rootDirs) {
            walkTemp(root)
        }

        // 2. Large files (> 50MB) - default unselected so user chooses
        val largeFiles = scanLargeFiles(50L * 1024 * 1024)
        for (lf in largeFiles.take(15)) {
            items.add(CleanItem(lf, CleanReason.LARGE_FILE, isSelected = false))
        }

        items
    }

    suspend fun searchFiles(
        query: String,
        category: FileCategory? = null,
        minSizeBytes: Long? = null,
        maxSizeBytes: Long? = null
    ): List<RafiqiFile> = withContext(Dispatchers.IO) {
        if (query.isBlank() && category == null) return@withContext emptyList()
        val cleanQuery = query.trim().lowercase()
        val rootDirs = getScanRoots()
        val matches = mutableListOf<RafiqiFile>()

        fun walk(dir: File, depth: Int = 0) {
            if (depth > 6 || matches.size > 200) return
            val list = dir.listFiles() ?: return
            for (f in list) {
                if (f.isDirectory) {
                    if (cleanQuery.isNotEmpty() && f.name.lowercase().contains(cleanQuery)) {
                        matches.add(toRafiqiFile(f))
                    }
                    if (!f.name.startsWith(".")) {
                        walk(f, depth + 1)
                    }
                } else {
                    val nameMatch = cleanQuery.isEmpty() || f.name.lowercase().contains(cleanQuery)
                    val rFile = toRafiqiFile(f)
                    val catMatch = category == null || rFile.category == category
                    val minMatch = minSizeBytes == null || f.length() >= minSizeBytes
                    val maxMatch = maxSizeBytes == null || f.length() <= maxSizeBytes

                    if (nameMatch && catMatch && minMatch && maxMatch) {
                        matches.add(rFile)
                    }
                }
            }
        }

        for (root in rootDirs) {
            walk(root)
        }
        matches.sortedByDescending { it.lastModified }
    }

    private fun getScanRoots(): List<File> {
        val roots = mutableListOf<File>()
        try {
            val ext = Environment.getExternalStorageDirectory()
            if (ext != null && ext.exists()) roots.add(ext)
        } catch (_: Exception) {}

        try {
            context.getExternalFilesDir(null)?.let { roots.add(it) }
        } catch (_: Exception) {}

        roots.add(context.filesDir)
        return roots.distinctBy { it.absolutePath }
    }

    private fun calculateQuickHash(file: File): String {
        return try {
            val md = MessageDigest.getInstance("MD5")
            FileInputStream(file).use { fis ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                var total = 0
                // Read first 64KB for speed, or full file if small
                while (fis.read(buffer).also { bytesRead = it } != -1 && total < 65536) {
                    md.update(buffer, 0, bytesRead)
                    total += bytesRead
                }
            }
            md.digest().joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            "${file.length()}_${file.name}"
        }
    }

    private fun getMimeType(file: File): String {
        val ext = file.extension.lowercase()
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: when (ext) {
            "pdf" -> "application/pdf"
            "apk" -> "application/vnd.android.package-archive"
            "zip" -> "application/zip"
            "rar" -> "application/x-rar-compressed"
            "7z" -> "application/x-7z-compressed"
            "mp3", "m4a", "wav", "aac", "flac", "ogg" -> "audio/*"
            "mp4", "mkv", "webm", "avi", "mov" -> "video/*"
            "jpg", "jpeg", "png", "gif", "webp", "bmp" -> "image/*"
            "txt", "csv", "log" -> "text/plain"
            "doc", "docx" -> "application/msword"
            "xls", "xlsx" -> "application/vnd.ms-excel"
            "ppt", "pptx" -> "application/vnd.ms-powerpoint"
            else -> "*/*"
        }
    }

    /**
     * Seeds initial friendly companion directories and sample notes if fresh
     */
    suspend fun initializeSampleDirectories() = withContext(Dispatchers.IO) {
        try {
            val base = context.getExternalFilesDir(null) ?: context.filesDir
            val rafiqiFolder = File(base, "مجلد رفيقي")
            if (!rafiqiFolder.exists()) {
                rafiqiFolder.mkdirs()

                // Create a welcome note
                val welcome = File(rafiqiFolder, "مرحباً بك في رفيقي.txt")
                if (!welcome.exists()) {
                    welcome.writeText(
                        "أهلاً بك في رفيقي!\n\n" +
                        "رفيقك لإدارة ملفات هاتفك وتنظيمها بسهولة وبدون إنترنت.\n\n" +
                        "المميزات:\n" +
                        "1. تصفح الملفات والبحث السريع.\n" +
                        "2. أدوات التنظيف الذكية للملفات الكبيرة والمكررة.\n" +
                        "3. إضافة الملفات للمفضلة والوصول السريع للملفات الحديثة.\n" +
                        "4. مشاركة الملفات وفتحها ونسخها ونقلها وضغطها بسهولة.\n" +
                        "5. حماية خصوصيتك 100% دون رفع أي ملف إلى الإنترنت.\n"
                    )
                }

                // Create subfolders
                File(rafiqiFolder, "مستندات مهمة").mkdirs()
                File(rafiqiFolder, "صور وتصاميم").mkdirs()
            }
        } catch (_: Exception) {}
    }
}
