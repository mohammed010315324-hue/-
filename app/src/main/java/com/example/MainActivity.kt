package com.example

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.RafiqiFile
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.RafiqiTheme
import com.example.ui.viewmodel.RafiqiViewModel
import com.example.ui.viewmodel.Screen
import java.io.File

class MainActivity : ComponentActivity() {

    private val viewModel: RafiqiViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            val isDarkTheme = when (uiState.themeMode) {
                "LIGHT" -> false
                "DARK" -> true
                else -> isSystemInDarkTheme()
            }

            RafiqiTheme(darkTheme = isDarkTheme) {
                // Mandatory Arabic RTL Layout Direction
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    RafiqiApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun RafiqiApp(viewModel: RafiqiViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Dialog States
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var fileToRename by remember { mutableStateOf<RafiqiFile?>(null) }
    var fileToDelete by remember { mutableStateOf<RafiqiFile?>(null) }
    var fileForDetails by remember { mutableStateOf<RafiqiFile?>(null) }
    var filesToZip by remember { mutableStateOf<List<RafiqiFile>?>(null) }
    var showPermissionRationale by remember { mutableStateOf(false) }

    // Feedback Snackbar Trigger
    LaunchedEffect(uiState.feedbackMessage) {
        uiState.feedbackMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearFeedback()
        }
    }

    // Permission Launchers
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val anyGranted = permissions.values.any { it }
        if (anyGranted) {
            viewModel.loadCurrentDirectory()
            viewModel.refreshStorageInfo()
        }
    }

    // Storage access request on launch
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(
                arrayOf(
                    android.Manifest.permission.READ_MEDIA_IMAGES,
                    android.Manifest.permission.READ_MEDIA_VIDEO,
                    android.Manifest.permission.READ_MEDIA_AUDIO
                )
            )
        } else {
            permissionLauncher.launch(
                arrayOf(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
        }
    }

    // Android Hardware / Gesture Back Handling
    BackHandler(enabled = uiState.currentScreen != Screen.HOME || uiState.directoryHistory.isNotEmpty()) {
        val handled = viewModel.navigateBack()
        if (!handled && uiState.currentScreen != Screen.HOME) {
            viewModel.navigateTo(Screen.HOME)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            // Show bottom navigation bar only on root tabs or appropriate screens
            val isRootTab = uiState.currentScreen in listOf(
                Screen.HOME,
                Screen.FILES,
                Screen.RECENTS,
                Screen.FAVORITES,
                Screen.TOOLS
            )

            if (isRootTab) {
                NavigationBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("main_navigation_bar"),
                    tonalElevation = 6.dp
                ) {
                    NavigationBarItem(
                        selected = uiState.currentScreen == Screen.HOME,
                        onClick = { viewModel.navigateTo(Screen.HOME) },
                        icon = { Icon(Icons.Default.Home, contentDescription = "الرئيسية") },
                        label = { Text("الرئيسية") },
                        modifier = Modifier.testTag("nav_item_home")
                    )
                    NavigationBarItem(
                        selected = uiState.currentScreen == Screen.FILES,
                        onClick = { viewModel.navigateTo(Screen.FILES) },
                        icon = { Icon(Icons.Default.Folder, contentDescription = "الملفات") },
                        label = { Text("الملفات") },
                        modifier = Modifier.testTag("nav_item_files")
                    )
                    NavigationBarItem(
                        selected = uiState.currentScreen == Screen.RECENTS,
                        onClick = { viewModel.navigateTo(Screen.RECENTS) },
                        icon = { Icon(Icons.Default.History, contentDescription = "حديثة") },
                        label = { Text("حديثة") },
                        modifier = Modifier.testTag("nav_item_recents")
                    )
                    NavigationBarItem(
                        selected = uiState.currentScreen == Screen.FAVORITES,
                        onClick = { viewModel.navigateTo(Screen.FAVORITES) },
                        icon = { Icon(Icons.Default.Star, contentDescription = "المفضلة") },
                        label = { Text("المفضلة") },
                        modifier = Modifier.testTag("nav_item_favorites")
                    )
                    NavigationBarItem(
                        selected = uiState.currentScreen == Screen.TOOLS,
                        onClick = { viewModel.navigateTo(Screen.TOOLS) },
                        icon = { Icon(Icons.Default.Build, contentDescription = "أدوات رفيقي") },
                        label = { Text("الأدوات") },
                        modifier = Modifier.testTag("nav_item_tools")
                    )
                }
            }
        }
    ) { innerPadding ->
        val screenModifier = Modifier.padding(innerPadding)

        when (uiState.currentScreen) {
            Screen.HOME -> {
                HomeScreen(
                    storageInfo = uiState.storageInfo,
                    recentFiles = uiState.recentFiles,
                    onNavigate = { viewModel.navigateTo(it) },
                    onCategoryClick = { viewModel.openCategory(it) },
                    onFileClick = { viewModel.openFileItem(it) },
                    onCleanClick = { viewModel.navigateTo(Screen.TOOL_CLEAN) },
                    modifier = screenModifier
                )
            }
            Screen.FILES -> {
                FilesScreen(
                    currentDir = uiState.currentDirectory,
                    directoryHistory = uiState.directoryHistory,
                    files = uiState.currentFiles,
                    isLoading = uiState.isLoadingFiles,
                    viewMode = uiState.viewMode,
                    sortOption = uiState.sortOption,
                    clipboard = uiState.clipboard,
                    onNavigateBack = { viewModel.navigateBack() },
                    onNavigateToDir = { viewModel.navigateToDirectoryPath(it) },
                    onFileClick = { viewModel.openFileItem(it) },
                    onFavoriteToggle = { viewModel.toggleFavorite(it) },
                    onShare = { viewModel.shareFileItem(it) },
                    onRenameRequest = { fileToRename = it },
                    onDeleteRequest = {
                        if (uiState.confirmDelete) {
                            fileToDelete = it
                        } else {
                            viewModel.deleteFile(it)
                        }
                    },
                    onCopyRequest = { viewModel.setClipboard(com.example.ui.viewmodel.ClipboardAction.COPY, listOf(it)) },
                    onMoveRequest = { viewModel.setClipboard(com.example.ui.viewmodel.ClipboardAction.MOVE, listOf(it)) },
                    onZipRequest = { filesToZip = listOf(it) },
                    onUnzipRequest = { viewModel.unzipFile(it) },
                    onDetailsRequest = { fileForDetails = it },
                    onCreateFolderClick = { showCreateFolderDialog = true },
                    onViewModeToggle = {
                        viewModel.setViewMode(if (uiState.viewMode == com.example.domain.model.ViewMode.LIST) com.example.domain.model.ViewMode.GRID else com.example.domain.model.ViewMode.LIST)
                    },
                    onSortOptionSelect = { viewModel.setSortOption(it) },
                    onPasteClipboard = { viewModel.pasteClipboard() },
                    onClearClipboard = { viewModel.clearClipboard() },
                    modifier = screenModifier
                )
            }
            Screen.RECENTS -> {
                RecentsScreen(
                    recentFiles = uiState.recentFiles,
                    onFileClick = { viewModel.openFileItem(it) },
                    onFavoriteToggle = { viewModel.toggleFavorite(it) },
                    onShare = { viewModel.shareFileItem(it) },
                    onRename = { fileToRename = it },
                    onDelete = {
                        if (uiState.confirmDelete) fileToDelete = it else viewModel.deleteFile(it)
                    },
                    onCopy = { viewModel.setClipboard(com.example.ui.viewmodel.ClipboardAction.COPY, listOf(it)) },
                    onMove = { viewModel.setClipboard(com.example.ui.viewmodel.ClipboardAction.MOVE, listOf(it)) },
                    onZip = { filesToZip = listOf(it) },
                    onUnzip = { viewModel.unzipFile(it) },
                    onDetails = { fileForDetails = it },
                    onClearAll = { viewModel.clearRecents() },
                    modifier = screenModifier
                )
            }
            Screen.FAVORITES -> {
                FavoritesScreen(
                    favoriteFiles = uiState.favoriteFiles,
                    onFileClick = { viewModel.openFileItem(it) },
                    onFavoriteToggle = { viewModel.toggleFavorite(it) },
                    onShare = { viewModel.shareFileItem(it) },
                    onRename = { fileToRename = it },
                    onDelete = {
                        if (uiState.confirmDelete) fileToDelete = it else viewModel.deleteFile(it)
                    },
                    onCopy = { viewModel.setClipboard(com.example.ui.viewmodel.ClipboardAction.COPY, listOf(it)) },
                    onMove = { viewModel.setClipboard(com.example.ui.viewmodel.ClipboardAction.MOVE, listOf(it)) },
                    onZip = { filesToZip = listOf(it) },
                    onUnzip = { viewModel.unzipFile(it) },
                    onDetails = { fileForDetails = it },
                    modifier = screenModifier
                )
            }
            Screen.TOOLS -> {
                ToolsOverviewScreen(
                    onNavigate = { viewModel.navigateTo(it) },
                    onBack = { viewModel.navigateBack() },
                    modifier = screenModifier
                )
            }
            Screen.SEARCH -> {
                SearchScreen(
                    query = uiState.searchQuery,
                    onQueryChange = { viewModel.onSearchQueryChange(it) },
                    selectedCategory = uiState.searchCategory,
                    onCategorySelect = { viewModel.onSearchCategorySelect(it) },
                    results = uiState.searchResults,
                    isSearching = uiState.isSearching,
                    onFileClick = { viewModel.openFileItem(it) },
                    onFavoriteToggle = { viewModel.toggleFavorite(it) },
                    onShare = { viewModel.shareFileItem(it) },
                    onRename = { fileToRename = it },
                    onDelete = {
                        if (uiState.confirmDelete) fileToDelete = it else viewModel.deleteFile(it)
                    },
                    onCopy = { viewModel.setClipboard(com.example.ui.viewmodel.ClipboardAction.COPY, listOf(it)) },
                    onMove = { viewModel.setClipboard(com.example.ui.viewmodel.ClipboardAction.MOVE, listOf(it)) },
                    onZip = { filesToZip = listOf(it) },
                    onUnzip = { viewModel.unzipFile(it) },
                    onDetails = { fileForDetails = it },
                    onBack = { viewModel.navigateBack() },
                    modifier = screenModifier
                )
            }
            Screen.CATEGORY_VIEW -> {
                CategoryScreen(
                    category = uiState.activeCategory,
                    files = uiState.categoryFiles,
                    isLoading = uiState.isCategoryLoading,
                    onFileClick = { viewModel.openFileItem(it) },
                    onFavoriteToggle = { viewModel.toggleFavorite(it) },
                    onShare = { viewModel.shareFileItem(it) },
                    onRename = { fileToRename = it },
                    onDelete = {
                        if (uiState.confirmDelete) fileToDelete = it else viewModel.deleteFile(it)
                    },
                    onCopy = { viewModel.setClipboard(com.example.ui.viewmodel.ClipboardAction.COPY, listOf(it)) },
                    onMove = { viewModel.setClipboard(com.example.ui.viewmodel.ClipboardAction.MOVE, listOf(it)) },
                    onZip = { filesToZip = listOf(it) },
                    onUnzip = { viewModel.unzipFile(it) },
                    onDetails = { fileForDetails = it },
                    onBack = { viewModel.navigateBack() },
                    modifier = screenModifier
                )
            }
            Screen.TOOL_CLEAN -> {
                CleaningScreen(
                    items = uiState.cleanItems,
                    isScanning = uiState.isCleanScanning,
                    onToggleItem = { viewModel.toggleCleanItem(it) },
                    onSelectAll = { viewModel.selectAllCleanItems(it) },
                    onDeleteSelected = { viewModel.deleteSelectedCleanItems() },
                    onBack = { viewModel.navigateBack() },
                    modifier = screenModifier
                )
            }
            Screen.TOOL_LARGE -> {
                LargeFilesScreen(
                    files = uiState.largeFiles,
                    isScanning = uiState.isLargeScanning,
                    onToggleSort = { viewModel.toggleLargeFilesSort() },
                    onFileClick = { viewModel.openFileItem(it) },
                    onFavoriteToggle = { viewModel.toggleFavorite(it) },
                    onShare = { viewModel.shareFileItem(it) },
                    onRename = { fileToRename = it },
                    onDelete = {
                        if (uiState.confirmDelete) fileToDelete = it else viewModel.deleteFile(it)
                    },
                    onCopy = { viewModel.setClipboard(com.example.ui.viewmodel.ClipboardAction.COPY, listOf(it)) },
                    onMove = { viewModel.setClipboard(com.example.ui.viewmodel.ClipboardAction.MOVE, listOf(it)) },
                    onZip = { filesToZip = listOf(it) },
                    onUnzip = { viewModel.unzipFile(it) },
                    onDetails = { fileForDetails = it },
                    onBack = { viewModel.navigateBack() },
                    modifier = screenModifier
                )
            }
            Screen.TOOL_DUPLICATES -> {
                DuplicatesScreen(
                    groups = uiState.duplicateGroups,
                    isScanning = uiState.isDuplicatesScanning,
                    selectedPaths = uiState.selectedDuplicatePaths,
                    onToggleSelect = { viewModel.toggleDuplicateSelection(it) },
                    onDeleteSelected = { viewModel.deleteSelectedDuplicates() },
                    onBack = { viewModel.navigateBack() },
                    modifier = screenModifier
                )
            }
            Screen.TOOL_ASSISTANT -> {
                AssistantScreen(
                    messages = uiState.chatMessages,
                    isThinking = uiState.isAssistantThinking,
                    onSendMessage = { viewModel.sendAssistantMessage(it) },
                    onBack = { viewModel.navigateBack() },
                    modifier = screenModifier
                )
            }
            Screen.SETTINGS -> {
                SettingsScreen(
                    themeMode = uiState.themeMode,
                    onThemeModeChange = { viewModel.setThemeMode(it) },
                    viewMode = uiState.viewMode,
                    onViewModeChange = { viewModel.setViewMode(it) },
                    sortOption = uiState.sortOption,
                    onSortOptionChange = { viewModel.setSortOption(it) },
                    confirmDelete = uiState.confirmDelete,
                    onConfirmDeleteChange = { viewModel.setConfirmDelete(it) },
                    onBack = { viewModel.navigateBack() },
                    modifier = screenModifier
                )
            }
        }
    }

    // Active Dialogs
    if (showCreateFolderDialog) {
        CreateFolderDialog(
            onDismiss = { showCreateFolderDialog = false },
            onConfirm = { name ->
                showCreateFolderDialog = false
                viewModel.createFolder(name)
            }
        )
    }

    fileToRename?.let { file ->
        RenameDialog(
            file = file,
            onDismiss = { fileToRename = null },
            onConfirm = { newName ->
                fileToRename = null
                viewModel.renameFile(file, newName)
            }
        )
    }

    fileToDelete?.let { file ->
        DeleteConfirmDialog(
            itemName = file.name,
            isMultiple = false,
            count = 1,
            onDismiss = { fileToDelete = null },
            onConfirm = {
                fileToDelete = null
                viewModel.deleteFile(file)
            }
        )
    }

    fileForDetails?.let { file ->
        FileDetailsDialog(
            file = file,
            onDismiss = { fileForDetails = null }
        )
    }

    filesToZip?.let { files ->
        val defaultName = if (files.size == 1) "${files.first().name.substringBeforeLast(".")}.zip" else "archive.zip"
        ZipDialog(
            suggestedName = defaultName,
            onDismiss = { filesToZip = null },
            onConfirm = { zipName ->
                filesToZip = null
                viewModel.zipFiles(files, zipName)
            }
        )
    }
}
