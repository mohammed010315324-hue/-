package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.RafiqiFile
import com.example.ui.components.FileListItem

@Composable
fun RecentsScreen(
    recentFiles: List<RafiqiFile>,
    onFileClick: (RafiqiFile) -> Unit,
    onFavoriteToggle: (RafiqiFile) -> Unit,
    onShare: (RafiqiFile) -> Unit,
    onRename: (RafiqiFile) -> Unit,
    onDelete: (RafiqiFile) -> Unit,
    onCopy: (RafiqiFile) -> Unit,
    onMove: (RafiqiFile) -> Unit,
    onZip: (RafiqiFile) -> Unit,
    onUnzip: (RafiqiFile) -> Unit,
    onDetails: (RafiqiFile) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "الملفات الحديثة",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "آخر الملفات التي تم الوصول إليها على هاتفك",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (recentFiles.isNotEmpty()) {
                TextButton(
                    onClick = onClearAll,
                    modifier = Modifier.testTag("clear_recents_button")
                ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("مسح السجل")
                }
            }
        }

        if (recentFiles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "لا توجد ملفات حديثة",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "الملفات التي تفتحها أو تعدلها ستظهر هنا للوصول السريع",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(recentFiles, key = { it.path }) { file ->
                    FileListItem(
                        file = file,
                        onClick = { onFileClick(file) },
                        onFavoriteToggle = { onFavoriteToggle(file) },
                        onShare = { onShare(file) },
                        onRename = { onRename(file) },
                        onDelete = { onDelete(file) },
                        onCopy = { onCopy(file) },
                        onMove = { onMove(file) },
                        onZip = { onZip(file) },
                        onUnzip = { onUnzip(file) },
                        onDetails = { onDetails(file) }
                    )
                }
            }
        }
    }
}

@Composable
fun FavoritesScreen(
    favoriteFiles: List<RafiqiFile>,
    onFileClick: (RafiqiFile) -> Unit,
    onFavoriteToggle: (RafiqiFile) -> Unit,
    onShare: (RafiqiFile) -> Unit,
    onRename: (RafiqiFile) -> Unit,
    onDelete: (RafiqiFile) -> Unit,
    onCopy: (RafiqiFile) -> Unit,
    onMove: (RafiqiFile) -> Unit,
    onZip: (RafiqiFile) -> Unit,
    onUnzip: (RafiqiFile) -> Unit,
    onDetails: (RafiqiFile) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 12.dp)) {
            Text(
                text = "المفضلة",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "ملفاتك ومجلداتك المفضلة للوصول الفوري",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (favoriteFiles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.StarBorder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "قائمة المفضلة فارغة",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "اضغط على رمز النجمة بجانب أي ملف لإضافته إلى المفضلة",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(favoriteFiles, key = { it.path }) { file ->
                    FileListItem(
                        file = file,
                        onClick = { onFileClick(file) },
                        onFavoriteToggle = { onFavoriteToggle(file) },
                        onShare = { onShare(file) },
                        onRename = { onRename(file) },
                        onDelete = { onDelete(file) },
                        onCopy = { onCopy(file) },
                        onMove = { onMove(file) },
                        onZip = { onZip(file) },
                        onUnzip = { onUnzip(file) },
                        onDetails = { onDetails(file) }
                    )
                }
            }
        }
    }
}
