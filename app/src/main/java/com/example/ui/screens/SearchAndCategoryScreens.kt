package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.FileCategory
import com.example.domain.model.RafiqiFile
import com.example.ui.components.FileListItem
import com.example.ui.components.formatBytes
import com.example.ui.components.getCategoryIcon

@Composable
fun SearchScreen(
    query: String,
    onQueryChange: (String) -> Unit,
    selectedCategory: FileCategory?,
    onCategorySelect: (FileCategory?) -> Unit,
    results: List<RafiqiFile>,
    isSearching: Boolean,
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
        // Search Input Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
            }

            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("ابحث في أسماء الملفات والمجلدات...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "مسح")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("search_text_input")
            )
        }

        // Category Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelect(null) },
                label = { Text("الكل") }
            )
            FileCategory.values().forEach { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { onCategorySelect(if (selectedCategory == cat) null else cat) },
                    label = { Text(cat.arabicTitle) }
                )
            }
        }

        // Search Results
        if (isSearching) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (query.isBlank() && selectedCategory == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "اكتب كلمة للبحث عن الملفات",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else if (results.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.SearchOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "لم يتم العثور على أي نتائج مطابقة",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            Text(
                text = "النتائج (${results.size})",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(
                contentPadding = PaddingValues(bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(results, key = { it.path }) { file ->
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
fun CategoryScreen(
    category: FileCategory?,
    files: List<RafiqiFile>,
    isLoading: Boolean,
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
    if (category == null) return

    val totalSize = files.sumOf { it.size }

    Column(
        modifier = modifier
            .fillMaxSize()
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
            Icon(
                imageVector = getCategoryIcon(category),
                contentDescription = null,
                tint = category.color,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = category.arabicTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${files.size} ملف • ${formatBytes(totalSize)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (files.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = getCategoryIcon(category),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "لا توجد ملفات في قسم «${category.arabicTitle}»",
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
