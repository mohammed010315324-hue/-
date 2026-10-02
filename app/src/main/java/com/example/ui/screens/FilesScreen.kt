package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.RafiqiFile
import com.example.domain.model.SortOption
import com.example.domain.model.ViewMode
import com.example.ui.components.*
import com.example.ui.viewmodel.ClipboardAction
import com.example.ui.viewmodel.ClipboardState
import java.io.File

@Composable
fun FilesScreen(
    currentDir: File,
    directoryHistory: List<File>,
    files: List<RafiqiFile>,
    isLoading: Boolean,
    viewMode: ViewMode,
    sortOption: SortOption,
    clipboard: ClipboardState?,
    onNavigateBack: () -> Unit,
    onNavigateToDir: (File) -> Unit,
    onFileClick: (RafiqiFile) -> Unit,
    onFavoriteToggle: (RafiqiFile) -> Unit,
    onShare: (RafiqiFile) -> Unit,
    onRenameRequest: (RafiqiFile) -> Unit,
    onDeleteRequest: (RafiqiFile) -> Unit,
    onCopyRequest: (RafiqiFile) -> Unit,
    onMoveRequest: (RafiqiFile) -> Unit,
    onZipRequest: (RafiqiFile) -> Unit,
    onUnzipRequest: (RafiqiFile) -> Unit,
    onDetailsRequest: (RafiqiFile) -> Unit,
    onCreateFolderClick: () -> Unit,
    onViewModeToggle: () -> Unit,
    onSortOptionSelect: (SortOption) -> Unit,
    onPasteClipboard: () -> Unit,
    onClearClipboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSortMenu by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateFolderClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .padding(bottom = if (clipboard != null) 70.dp else 16.dp)
                    .testTag("create_folder_fab")
            ) {
                Icon(Icons.Default.CreateNewFolder, contentDescription = "إنشاء مجلد جديد")
            }
        },
        bottomBar = {
            if (clipboard != null) {
                Surface(
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            val actionName = if (clipboard.action == ClipboardAction.COPY) "جاهز للنسخ" else "جاهز للنقل"
                            Text(
                                text = "$actionName (${clipboard.files.size} عنصر)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "انتقل للمجلد المطلوب ثم اضغط لصق",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = onClearClipboard) {
                                Text("إلغاء")
                            }
                            Button(
                                onClick = onPasteClipboard,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.testTag("paste_here_button")
                            ) {
                                Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("لصق هنا")
                            }
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
        ) {
            // Top App Bar / Path Header
            Surface(
                tonalElevation = 2.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (directoryHistory.isNotEmpty()) {
                                IconButton(onClick = onNavigateBack) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "رجوع"
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Default.FolderSpecial,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }

                            Text(
                                text = currentDir.name.ifEmpty { "التخزين الداخلي" },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row {
                            // View Mode Toggle
                            IconButton(onClick = onViewModeToggle) {
                                Icon(
                                    imageVector = if (viewMode == ViewMode.LIST) Icons.Default.GridView else Icons.AutoMirrored.Filled.ViewList,
                                    contentDescription = "تغيير طريقة العرض"
                                )
                            }

                            // Sort Order Menu
                            Box {
                                IconButton(onClick = { showSortMenu = true }) {
                                    Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "ترتيب")
                                }

                                DropdownMenu(
                                    expanded = showSortMenu,
                                    onDismissRequest = { showSortMenu = false }
                                ) {
                                    SortOption.values().forEach { option ->
                                        DropdownMenuItem(
                                            text = {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    if (option == sortOption) {
                                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                    }
                                                    Text(option.arabicTitle)
                                                }
                                            },
                                            onClick = {
                                                onSortOptionSelect(option)
                                                showSortMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Breadcrumb Scrollable Bar
                    val pathSegments = remember(currentDir) {
                        currentDir.absolutePath.split("/").filter { it.isNotEmpty() }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "التخزين",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    onNavigateToDir(File(currentDir.path.substringBefore("0") + "0"))
                                }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                        pathSegments.takeLast(3).forEach { segment ->
                            Text(
                                text = " / ",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = segment,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Files Container
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (files.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "المجلد فارغ",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                if (viewMode == ViewMode.LIST) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 120.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(files, key = { it.path }) { file ->
                            FileListItem(
                                file = file,
                                onClick = { onFileClick(file) },
                                onFavoriteToggle = { onFavoriteToggle(file) },
                                onShare = { onShare(file) },
                                onRename = { onRenameRequest(file) },
                                onDelete = { onDeleteRequest(file) },
                                onCopy = { onCopyRequest(file) },
                                onMove = { onMoveRequest(file) },
                                onZip = { onZipRequest(file) },
                                onUnzip = { onUnzipRequest(file) },
                                onDetails = { onDetailsRequest(file) }
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 100.dp),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 120.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(files, key = { it.path }) { file ->
                            FileGridItem(
                                file = file,
                                onClick = { onFileClick(file) },
                                onLongClick = { onDetailsRequest(file) }
                            )
                        }
                    }
                }
            }
        }
    }
}
