package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.model.FileCategory
import com.example.domain.model.RafiqiFile
import com.example.domain.model.StorageInfo
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.Screen

@Composable
fun HomeScreen(
    storageInfo: StorageInfo,
    recentFiles: List<RafiqiFile>,
    onNavigate: (Screen) -> Unit,
    onCategoryClick: (FileCategory) -> Unit,
    onFileClick: (RafiqiFile) -> Unit,
    onCleanClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1. Header with App Name, Slogan & Search Action
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_rafiqi_logo),
                            contentDescription = "شعار رفيقي",
                            modifier = Modifier.size(38.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "رفيقي",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "ملفاتك، أدواتك، ورفيقك في مكان واحد",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row {
                    IconButton(
                        onClick = { onNavigate(Screen.SEARCH) },
                        modifier = Modifier.testTag("home_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "بحث",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = { onNavigate(Screen.SETTINGS) },
                        modifier = Modifier.testTag("home_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "الإعدادات",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 2. Storage Card
        item {
            StorageCard(
                storageInfo = storageInfo,
                onCleanClick = onCleanClick
            )
        }

        // 3. Quick Access Categories (8 Categories)
        item {
            Text(
                text = "الوصول السريع",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            val categories = listOf(
                FileCategory.IMAGES,
                FileCategory.VIDEOS,
                FileCategory.AUDIO,
                FileCategory.DOCUMENTS,
                FileCategory.DOWNLOADS,
                FileCategory.ARCHIVES,
                FileCategory.APPS,
                FileCategory.OTHER
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    for (i in 0..3) {
                        val cat = categories[i]
                        CategoryCard(
                            category = cat,
                            onClick = { onCategoryClick(cat) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    for (i in 4..7) {
                        val cat = categories[i]
                        CategoryCard(
                            category = cat,
                            onClick = { onCategoryClick(cat) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 4. Rafiqi Smart Tools Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "أدوات رفيقي",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(onClick = { onNavigate(Screen.TOOLS) }) {
                    Text("عرض الكل", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ToolBannerCard(
                        title = "تنظيف التخزين",
                        subtitle = "فحص الملفات غير الضرورية بأمان",
                        icon = Icons.Default.CleaningServices,
                        color = CleanGreen,
                        onClick = { onNavigate(Screen.TOOL_CLEAN) },
                        modifier = Modifier.weight(1f)
                    )
                    ToolBannerCard(
                        title = "الملفات الكبيرة",
                        subtitle = "فرز أكبر الملفات في الهاتف",
                        icon = Icons.Default.DataUsage,
                        color = RafiqiSecondary,
                        onClick = { onNavigate(Screen.TOOL_LARGE) },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ToolBannerCard(
                        title = "الملفات المكررة",
                        subtitle = "اكتشاف النسخ وتوفير المساحة",
                        icon = Icons.Default.ContentCopy,
                        color = RafiqiAmber,
                        onClick = { onNavigate(Screen.TOOL_DUPLICATES) },
                        modifier = Modifier.weight(1f)
                    )
                    ToolBannerCard(
                        title = "مساعد رفيقي",
                        subtitle = "نصائح ذكية لتنظيم جهازك",
                        icon = Icons.Default.Psychology,
                        color = MaterialTheme.colorScheme.primary,
                        onClick = { onNavigate(Screen.TOOL_ASSISTANT) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 5. Recent Files Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "الملفات الحديثة",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (recentFiles.isNotEmpty()) {
                    TextButton(onClick = { onNavigate(Screen.RECENTS) }) {
                        Text("عرض الكل (${recentFiles.size})", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        if (recentFiles.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "لا توجد ملفات حديثة حتى الآن",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(recentFiles.take(5)) { file ->
                FileListItem(
                    file = file,
                    onClick = { onFileClick(file) },
                    onFavoriteToggle = { /* handled in list */ },
                    onShare = { /* handled in list */ },
                    onRename = { /* handled in list */ },
                    onDelete = { /* handled in list */ },
                    onCopy = { /* handled in list */ },
                    onMove = { /* handled in list */ },
                    onZip = { /* handled in list */ },
                    onUnzip = { /* handled in list */ },
                    onDetails = { /* handled in list */ }
                )
            }
        }
    }
}

@Composable
fun ToolBannerCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick)
            .testTag("tool_card_${title.hashCode()}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
