package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.CleanItem
import com.example.domain.model.DuplicateGroup
import com.example.domain.model.RafiqiFile
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.Screen

@Composable
fun ToolsOverviewScreen(
    onNavigate: (Screen) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                }
                Text(
                    text = "أدوات رفيقي",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        item {
            ToolCardDetailed(
                title = "تنظيف التخزين",
                description = "فحص الملفات غير الضرورية، الملفات المؤقتة والذاكرة المخبأة لتحرير مساحة بأمان.",
                icon = Icons.Default.CleaningServices,
                iconColor = CleanGreen,
                onClick = { onNavigate(Screen.TOOL_CLEAN) }
            )
        }

        item {
            ToolCardDetailed(
                title = "الملفات الكبيرة",
                description = "اكتشاف الملفات الأكبر حجماً في هاتفك وترتيبها لسهولة مراجعتها وحذف ما انتهيت منه.",
                icon = Icons.Default.DataUsage,
                iconColor = RafiqiSecondary,
                onClick = { onNavigate(Screen.TOOL_LARGE) }
            )
        }

        item {
            ToolCardDetailed(
                title = "الملفات المكررة",
                description = "اكتشاف ومقارنة النسخ المتطابقة بالبصمة الرقمية لتوفير مساحة التخزين بدون إزالة الأصل.",
                icon = Icons.Default.ContentCopy,
                iconColor = RafiqiAmber,
                onClick = { onNavigate(Screen.TOOL_DUPLICATES) }
            )
        }

        item {
            ToolCardDetailed(
                title = "مساعد رفيقي الذكي",
                description = "تحليل حالة هاتفك، تقديم نصائح تنظيمية فورية، وإجابات ذكية للأسئلة الشائعة.",
                icon = Icons.Default.Psychology,
                iconColor = MaterialTheme.colorScheme.primary,
                onClick = { onNavigate(Screen.TOOL_ASSISTANT) }
            )
        }
    }
}

@Composable
fun ToolCardDetailed(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("tool_detailed_${title.hashCode()}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(18.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CleaningScreen(
    items: List<CleanItem>,
    isScanning: Boolean,
    onToggleItem: (CleanItem) -> Unit,
    onSelectAll: (Boolean) -> Unit,
    onDeleteSelected: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val selectedItems = items.filter { it.isSelected }
    val totalSelectedSize = selectedItems.sumOf { it.file.size }

    Scaffold(
        bottomBar = {
            if (items.isNotEmpty()) {
                Surface(
                    tonalElevation = 6.dp,
                    shadowElevation = 6.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "المحدد: ${selectedItems.size} ملف",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "توفير: ${formatBytes(totalSelectedSize)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = CleanGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Button(
                            onClick = { showDeleteConfirm = true },
                            enabled = selectedItems.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.testTag("clean_delete_selected_button")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("حذف المحدد")
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                    Column {
                        Text(
                            text = "تنظيف التخزين",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "حدد الملفات التي ترغب بحذفها بأمان",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (items.isNotEmpty()) {
                    val allSelected = selectedItems.size == items.size
                    TextButton(onClick = { onSelectAll(!allSelected) }) {
                        Text(if (allSelected) "إلغاء الكل" else "تحديد الكل")
                    }
                }
            }

            if (isScanning) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("جارٍ فحص الملفات المؤقتة والزائدة...")
                    }
                }
            } else if (items.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = CleanGreen,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "جهازك نظيف ومثالي!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "لم يتم العثور على ملفات مؤقتة غير ضرورية",
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
                    items(items, key = { it.file.path }) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onToggleItem(item) },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (item.isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = item.isSelected,
                                    onCheckedChange = { onToggleItem(item) }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.file.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = item.reason.arabicTitle,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = " • ${formatBytes(item.file.size)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        DeleteConfirmDialog(
            itemName = "",
            isMultiple = true,
            count = selectedItems.size,
            onDismiss = { showDeleteConfirm = false },
            onConfirm = {
                showDeleteConfirm = false
                onDeleteSelected()
            }
        )
    }
}

@Composable
fun LargeFilesScreen(
    files: List<RafiqiFile>,
    isScanning: Boolean,
    onToggleSort: () -> Unit,
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
    onBack: () -> Unit,
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                }
                Column {
                    Text(
                        text = "الملفات الكبيرة",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "مرتبة حسب استهلاك المساحة",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (files.isNotEmpty()) {
                IconButton(onClick = onToggleSort) {
                    Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "عكس الترتيب")
                }
            }
        }

        if (isScanning) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (files.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.FolderSpecial,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "لا توجد ملفات كبيرة تتجاوز 10 MB",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(files, key = { it.path }) { file ->
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
fun DuplicatesScreen(
    groups: List<DuplicateGroup>,
    isScanning: Boolean,
    selectedPaths: Set<String>,
    onToggleSelect: (String) -> Unit,
    onDeleteSelected: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val totalPotentialFreed = groups.sumOf { group ->
        val selectedInGroup = group.files.count { selectedPaths.contains(it.path) }
        group.size * selectedInGroup
    }

    Scaffold(
        bottomBar = {
            if (groups.isNotEmpty()) {
                Surface(
                    tonalElevation = 6.dp,
                    shadowElevation = 6.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "محدد: ${selectedPaths.size} نسخة زائدة",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "توفير: ${formatBytes(totalPotentialFreed)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = CleanGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Button(
                            onClick = { showDeleteConfirm = true },
                            enabled = selectedPaths.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.testTag("delete_duplicates_button")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("حذف النسخ المحددة")
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                }
                Column {
                    Text(
                        text = "الملفات المكررة",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "اكتشاف النسخ المتطابقة بالبصمة الرقمية للحجم والمحتوى",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (isScanning) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("جارٍ فحص الملفات المكررة وحساب البصمات...")
                    }
                }
            } else if (groups.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = CleanGreen,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "لا توجد ملفات مكررة!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "ذاكرة جهازك لا تحتوي على نسخ متطابقة مهدرة",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(groups) { group ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${group.files.size} نسخ متطابقة",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = formatBytes(group.size),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                group.files.forEachIndexed { index, file ->
                                    val isOriginal = index == 0
                                    val isSelected = selectedPaths.contains(file.path)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onToggleSelect(file.path) }
                                            .padding(vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = isSelected,
                                            onCheckedChange = { onToggleSelect(file.path) }
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = file.name,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Medium,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                if (isOriginal) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Surface(
                                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                                        shape = RoundedCornerShape(4.dp)
                                                    ) {
                                                        Text(
                                                            text = "الأصل",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.primary,
                                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                        )
                                                    }
                                                }
                                            }
                                            Text(
                                                text = file.path,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        DeleteConfirmDialog(
            itemName = "",
            isMultiple = true,
            count = selectedPaths.size,
            onDismiss = { showDeleteConfirm = false },
            onConfirm = {
                showDeleteConfirm = false
                onDeleteSelected()
            }
        )
    }
}
