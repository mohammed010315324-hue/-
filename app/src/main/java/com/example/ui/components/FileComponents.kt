package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.domain.model.FileCategory
import com.example.domain.model.RafiqiFile
import com.example.domain.model.StorageInfo
import com.example.ui.theme.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StorageCard(
    storageInfo: StorageInfo,
    onCleanClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("storage_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = "التخزين",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "مساحة التخزين",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = formatStorageSubtitle(storageInfo),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                FilledTonalButton(
                    onClick = onCleanClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.testTag("storage_clean_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CleaningServices,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "تنظيف", fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress Bar
            val percent = storageInfo.usedPercent
            LinearProgressIndicator(
                progress = { percent },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = if (percent > 0.85f) StorageWarning else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${(percent * 100).toInt()}% مستخدم",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "المتبقي: ${formatBytes(storageInfo.freeBytes)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = StorageGood,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

fun formatStorageSubtitle(info: StorageInfo): String {
    return "${formatBytes(info.usedBytes)} مستخدمة من ${formatBytes(info.totalBytes)}"
}

fun formatBytes(bytes: Long): String {
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    val tb = gb / 1024.0
    return when {
        tb >= 1.0 -> String.format(Locale.US, "%.1f TB", tb)
        gb >= 1.0 -> String.format(Locale.US, "%.1f GB", gb)
        mb >= 1.0 -> String.format(Locale.US, "%.1f MB", mb)
        kb >= 1.0 -> String.format(Locale.US, "%.1f KB", kb)
        else -> "$bytes B"
    }
}

@Composable
fun CategoryCard(
    category: FileCategory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("category_${category.name.lowercase()}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(category.color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCategoryIcon(category),
                    contentDescription = category.arabicTitle,
                    tint = category.color,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = category.arabicTitle,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

fun getCategoryIcon(cat: FileCategory): ImageVector {
    return when (cat) {
        FileCategory.IMAGES -> Icons.Default.Image
        FileCategory.VIDEOS -> Icons.Default.Movie
        FileCategory.AUDIO -> Icons.Default.Audiotrack
        FileCategory.DOCUMENTS -> Icons.Default.Description
        FileCategory.DOWNLOADS -> Icons.Default.Download
        FileCategory.ARCHIVES -> Icons.Default.FolderZip
        FileCategory.APPS -> Icons.Default.Android
        FileCategory.OTHER -> Icons.Default.InsertDriveFile
    }
}

@Composable
fun FileListItem(
    file: RafiqiFile,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onShare: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onCopy: () -> Unit,
    onMove: () -> Unit,
    onZip: () -> Unit,
    onUnzip: () -> Unit,
    onDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("file_item_${file.name.hashCode()}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (!file.exists) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon / Thumbnail
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (file.isDirectory) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        else file.category.color.copy(alpha = 0.12f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (file.category == FileCategory.IMAGES && file.exists) {
                    AsyncImage(
                        model = File(file.path),
                        contentDescription = file.name,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = if (file.isDirectory) Icons.Default.Folder else getCategoryIcon(file.category),
                        contentDescription = null,
                        tint = if (file.isDirectory) MaterialTheme.colorScheme.primary else file.category.color,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (file.exists) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!file.exists) {
                        Text(
                            text = "العنصر لم يعد متاحاً على الجهاز",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else if (file.isDirectory) {
                        Text(
                            text = "${file.itemCount} عنصر • ${formatDate(file.lastModified)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "${formatBytes(file.size)} • ${formatDate(file.lastModified)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Favorite Icon Button
            IconButton(
                onClick = onFavoriteToggle,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = if (file.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "المفضلة",
                    tint = if (file.isFavorite) RafiqiAmber else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Menu 3 dots
            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "خيارات الملف",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("فتح") },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null) },
                        onClick = { menuExpanded = false; onClick() }
                    )
                    if (!file.isDirectory) {
                        DropdownMenuItem(
                            text = { Text("مشاركة") },
                            leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                            onClick = { menuExpanded = false; onShare() }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("إعادة تسمية") },
                        leadingIcon = { Icon(Icons.Default.DriveFileRenameOutline, contentDescription = null) },
                        onClick = { menuExpanded = false; onRename() }
                    )
                    DropdownMenuItem(
                        text = { Text("نسخ") },
                        leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                        onClick = { menuExpanded = false; onCopy() }
                    )
                    DropdownMenuItem(
                        text = { Text("نقل") },
                        leadingIcon = { Icon(Icons.Default.DriveFileMove, contentDescription = null) },
                        onClick = { menuExpanded = false; onMove() }
                    )
                    if (!file.isDirectory && (file.extension == "zip" || file.category == FileCategory.ARCHIVES)) {
                        DropdownMenuItem(
                            text = { Text("فك الضغط") },
                            leadingIcon = { Icon(Icons.Default.Unarchive, contentDescription = null) },
                            onClick = { menuExpanded = false; onUnzip() }
                        )
                    } else {
                        DropdownMenuItem(
                            text = { Text("ضغط إلى ZIP") },
                            leadingIcon = { Icon(Icons.Default.FolderZip, contentDescription = null) },
                            onClick = { menuExpanded = false; onZip() }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("التفاصيل") },
                        leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                        onClick = { menuExpanded = false; onDetails() }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("حذف", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                        onClick = { menuExpanded = false; onDelete() }
                    )
                }
            }
        }
    }
}

@Composable
fun FileGridItem(
    file: RafiqiFile,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("file_grid_${file.name.hashCode()}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (file.isDirectory) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        else file.category.color.copy(alpha = 0.12f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (file.category == FileCategory.IMAGES && file.exists) {
                    AsyncImage(
                        model = File(file.path),
                        contentDescription = file.name,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = if (file.isDirectory) Icons.Default.Folder else getCategoryIcon(file.category),
                        contentDescription = null,
                        tint = if (file.isDirectory) MaterialTheme.colorScheme.primary else file.category.color,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = file.name,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (file.isDirectory) "${file.itemCount} عنصر" else formatBytes(file.size),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

fun formatDate(millis: Long): String {
    val date = Date(millis)
    val format = SimpleDateFormat("dd/MM/yyyy", Locale.US)
    return format.format(date)
}
