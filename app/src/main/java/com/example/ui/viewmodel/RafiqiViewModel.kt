package com.example.ui.viewmodel

import android.app.Application
import android.os.Environment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.RafiqiDatabase
import com.example.data.RafiqiRepository
import com.example.domain.model.*
import com.example.service.ChatMessage
import com.example.service.FileManagerService
import com.example.service.MessageSender
import com.example.service.RafiqiAssistant
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

enum class Screen {
    HOME,
    FILES,
    RECENTS,
    FAVORITES,
    TOOLS,
    SEARCH,
    CATEGORY_VIEW,
    TOOL_CLEAN,
    TOOL_LARGE,
    TOOL_DUPLICATES,
    TOOL_ASSISTANT,
    SETTINGS
}

enum class ClipboardAction {
    COPY,
    MOVE
}

data class ClipboardState(
    val action: ClipboardAction,
    val files: List<RafiqiFile>
)

data class RafiqiUiState(
    val currentScreen: Screen = Screen.HOME,
    val screenStack: List<Screen> = listOf(Screen.HOME),
    val storageInfo: StorageInfo = StorageInfo(),
    val currentDirectory: File,
    val directoryHistory: List<File> = emptyList(),
    val currentFiles: List<RafiqiFile> = emptyList(),
    val isLoadingFiles: Boolean = false,
    val viewMode: ViewMode = ViewMode.LIST,
    val sortOption: SortOption = SortOption.NAME_ASC,
    val clipboard: ClipboardState? = null,
    val selectedFiles: Set<String> = emptySet(), // For batch actions
    val recentFiles: List<RafiqiFile> = emptyList(),
    val favoriteFiles: List<RafiqiFile> = emptyList(),
    // Search
    val searchQuery: String = "",
    val searchCategory: FileCategory? = null,
    val searchResults: List<RafiqiFile> = emptyList(),
    val isSearching: Boolean = false,
    // Category View
    val activeCategory: FileCategory? = null,
    val categoryFiles: List<RafiqiFile> = emptyList(),
    val isCategoryLoading: Boolean = false,
    // Clean Tool
    val cleanItems: List<CleanItem> = emptyList(),
    val isCleanScanning: Boolean = false,
    // Large Files Tool
    val largeFiles: List<RafiqiFile> = emptyList(),
    val isLargeScanning: Boolean = false,
    // Duplicates Tool
    val duplicateGroups: List<DuplicateGroup> = emptyList(),
    val isDuplicatesScanning: Boolean = false,
    val selectedDuplicatePaths: Set<String> = emptySet(),
    // Assistant
    val chatMessages: List<ChatMessage> = listOf(
        ChatMessage(
            sender = MessageSender.RAFIQI,
            text = "مرحباً بك! أنا «رفيقي»، مساعدك الذكي لتنظيم هاتفك وإدارة ملفاتك. كيف يمكنني مساعدتك اليوم؟"
        )
    ),
    val isAssistantThinking: Boolean = false,
    // Settings
    val themeMode: String = "SYSTEM",
    val confirmDelete: Boolean = true,
    // Snackbars / Feedback
    val feedbackMessage: String? = null
)

class RafiqiViewModel(application: Application) : AndroidViewModel(application) {

    private val db = RafiqiDatabase.getInstance(application)
    private val repository = RafiqiRepository(db.favoriteDao(), db.recentFileDao(), db.settingDao())
    val fileService = FileManagerService(application)
    val assistant = RafiqiAssistant(fileService)

    private val initialDir = fileService.getDefaultStorageDir()

    private val _uiState = MutableStateFlow(
        RafiqiUiState(currentDirectory = initialDir)
    )
    val uiState: StateFlow<RafiqiUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            fileService.initializeSampleDirectories()
            refreshStorageInfo()
            loadCurrentDirectory()
        }

        // Observe favorites from database
        viewModelScope.launch {
            repository.favorites.collect { favs ->
                _uiState.update { it.copy(favoriteFiles = favs) }
            }
        }

        // Observe recents from database
        viewModelScope.launch {
            repository.recentFiles.collect { recents ->
                _uiState.update { it.copy(recentFiles = recents) }
            }
        }

        // Observe settings
        viewModelScope.launch {
            repository.getThemeMode().collect { mode ->
                _uiState.update { it.copy(themeMode = mode) }
            }
        }

        viewModelScope.launch {
            repository.getViewMode().collect { mode ->
                _uiState.update { it.copy(viewMode = mode) }
            }
        }

        viewModelScope.launch {
            repository.getSortOption().collect { sort ->
                _uiState.update { it.copy(sortOption = sort) }
            }
        }

        viewModelScope.launch {
            repository.getConfirmDelete().collect { confirm ->
                _uiState.update { it.copy(confirmDelete = confirm) }
            }
        }
    }

    fun navigateTo(screen: Screen) {
        _uiState.update { state ->
            if (state.currentScreen == screen) return@update state
            val newStack = state.screenStack + screen
            state.copy(currentScreen = screen, screenStack = newStack)
        }

        when (screen) {
            Screen.HOME -> refreshStorageInfo()
            Screen.TOOL_CLEAN -> startCleanScan()
            Screen.TOOL_LARGE -> startLargeFilesScan()
            Screen.TOOL_DUPLICATES -> startDuplicatesScan()
            Screen.FILES -> loadCurrentDirectory()
            else -> {}
        }
    }

    fun navigateBack(): Boolean {
        val state = _uiState.value
        // If in directory hierarchy in FILES screen, go up first
        if (state.currentScreen == Screen.FILES && state.directoryHistory.isNotEmpty()) {
            val parent = state.directoryHistory.last()
            val newHistory = state.directoryHistory.dropLast(1)
            _uiState.update { it.copy(currentDirectory = parent, directoryHistory = newHistory) }
            loadCurrentDirectory()
            return true
        }

        // Pop screen stack
        if (state.screenStack.size > 1) {
            val newStack = state.screenStack.dropLast(1)
            val prevScreen = newStack.last()
            _uiState.update { it.copy(currentScreen = prevScreen, screenStack = newStack) }
            return true
        }
        return false
    }

    fun refreshStorageInfo() {
        viewModelScope.launch {
            val info = fileService.getStorageInfo()
            _uiState.update { it.copy(storageInfo = info) }
        }
    }

    fun loadCurrentDirectory() {
        val dir = _uiState.value.currentDirectory
        val sort = _uiState.value.sortOption
        _uiState.update { it.copy(isLoadingFiles = true) }
        viewModelScope.launch {
            val files = fileService.listFiles(dir, sort)
            _uiState.update { it.copy(currentFiles = files, isLoadingFiles = false) }
        }
    }

    fun navigateIntoDirectory(dir: File) {
        val current = _uiState.value.currentDirectory
        val history = _uiState.value.directoryHistory + current
        _uiState.update {
            it.copy(currentDirectory = dir, directoryHistory = history, selectedFiles = emptySet())
        }
        loadCurrentDirectory()
    }

    fun navigateToDirectoryPath(dir: File) {
        _uiState.update {
            it.copy(
                currentScreen = Screen.FILES,
                currentDirectory = dir,
                directoryHistory = emptyList(),
                selectedFiles = emptySet()
            )
        }
        loadCurrentDirectory()
    }

    fun openFileItem(file: RafiqiFile) {
        val f = File(file.path)
        if (f.isDirectory) {
            navigateIntoDirectory(f)
        } else {
            viewModelScope.launch {
                repository.recordRecent(file)
                val res = fileService.openFile(f)
                if (res.isFailure) {
                    showFeedback(res.exceptionOrNull()?.message ?: "تعذر فتح الملف")
                }
            }
        }
    }

    fun shareFileItem(file: RafiqiFile) {
        val f = File(file.path)
        val res = fileService.shareFiles(listOf(f))
        if (res.isFailure) {
            showFeedback("تعذر مشاركة الملف")
        }
    }

    fun toggleFavorite(file: RafiqiFile) {
        viewModelScope.launch {
            repository.toggleFavorite(file)
            showFeedback(if (file.isFavorite) "تمت الإزالة من المفضلة" else "تمت الإضافة إلى المفضلة")
        }
    }

    fun createFolder(name: String) {
        viewModelScope.launch {
            val res = fileService.createFolder(_uiState.value.currentDirectory, name)
            if (res.isSuccess) {
                showFeedback("تم إنشاء المجلد بنجاح")
                loadCurrentDirectory()
            } else {
                showFeedback(res.exceptionOrNull()?.message ?: "تعذر إنشاء المجلد")
            }
        }
    }

    fun renameFile(file: RafiqiFile, newName: String) {
        viewModelScope.launch {
            val res = fileService.rename(File(file.path), newName)
            if (res.isSuccess) {
                showFeedback("تمت إعادة التسمية بنجاح")
                loadCurrentDirectory()
            } else {
                showFeedback(res.exceptionOrNull()?.message ?: "تعذر إعادة التسمية")
            }
        }
    }

    fun deleteFile(file: RafiqiFile) {
        viewModelScope.launch {
            val res = fileService.delete(File(file.path))
            if (res.isSuccess) {
                repository.removeFavorite(file.path)
                repository.removeRecent(file.path)
                showFeedback("تم الحذف بنجاح")
                loadCurrentDirectory()
                refreshStorageInfo()
            } else {
                showFeedback("تعذر حذف الملف")
            }
        }
    }

    fun setClipboard(action: ClipboardAction, files: List<RafiqiFile>) {
        _uiState.update { it.copy(clipboard = ClipboardState(action, files), selectedFiles = emptySet()) }
        val msg = if (action == ClipboardAction.COPY) "تم نسخ ${files.size} عنصر، انتقل للوجهة واضغط لصق"
                  else "تم تحديد ${files.size} عنصر للنقل، انتقل للوجهة واضغط لصق"
        showFeedback(msg)
    }

    fun clearClipboard() {
        _uiState.update { it.copy(clipboard = null) }
    }

    fun pasteClipboard() {
        val clip = _uiState.value.clipboard ?: return
        val dest = _uiState.value.currentDirectory
        viewModelScope.launch {
            var successCount = 0
            for (item in clip.files) {
                val f = File(item.path)
                val res = if (clip.action == ClipboardAction.COPY) {
                    fileService.copy(f, dest)
                } else {
                    fileService.move(f, dest)
                }
                if (res.isSuccess) successCount++
            }
            _uiState.update { it.copy(clipboard = null) }
            showFeedback("تمت العملية بنجاح ($successCount عنصر)")
            loadCurrentDirectory()
            refreshStorageInfo()
        }
    }

    fun zipFiles(files: List<RafiqiFile>, zipName: String) {
        viewModelScope.launch {
            val cleanName = if (zipName.endsWith(".zip")) zipName else "$zipName.zip"
            val destZip = File(_uiState.value.currentDirectory, cleanName)
            val res = fileService.zip(files.map { File(it.path) }, destZip)
            if (res.isSuccess) {
                showFeedback("تم إنشاء الملف المضغوط بنجاح")
                loadCurrentDirectory()
            } else {
                showFeedback("تعذر ضغط الملفات")
            }
        }
    }

    fun unzipFile(file: RafiqiFile) {
        viewModelScope.launch {
            val zip = File(file.path)
            val destDir = File(zip.parentFile, zip.nameWithoutExtension)
            val res = fileService.unzip(zip, destDir)
            if (res.isSuccess) {
                showFeedback("تم فك الضغط بنجاح إلى: ${destDir.name}")
                loadCurrentDirectory()
            } else {
                showFeedback(res.exceptionOrNull()?.message ?: "تعذر فك الضغط")
            }
        }
    }

    // Search
    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        executeSearch()
    }

    fun onSearchCategorySelect(cat: FileCategory?) {
        _uiState.update { it.copy(searchCategory = cat) }
        executeSearch()
    }

    private fun executeSearch() {
        val q = _uiState.value.searchQuery
        val cat = _uiState.value.searchCategory
        if (q.isBlank() && cat == null) {
            _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
            return
        }
        _uiState.update { it.copy(isSearching = true) }
        viewModelScope.launch {
            val results = fileService.searchFiles(q, cat)
            _uiState.update { it.copy(searchResults = results, isSearching = false) }
        }
    }

    // Category Screen
    fun openCategory(cat: FileCategory) {
        _uiState.update {
            it.copy(
                activeCategory = cat,
                isCategoryLoading = true,
                currentScreen = Screen.CATEGORY_VIEW,
                screenStack = it.screenStack + Screen.CATEGORY_VIEW
            )
        }
        viewModelScope.launch {
            val list = fileService.scanCategory(cat)
            _uiState.update { it.copy(categoryFiles = list, isCategoryLoading = false) }
        }
    }

    // Cleaning Tool
    fun startCleanScan() {
        _uiState.update { it.copy(isCleanScanning = true) }
        viewModelScope.launch {
            val items = fileService.scanCleanableFiles()
            _uiState.update { it.copy(cleanItems = items, isCleanScanning = false) }
        }
    }

    fun toggleCleanItem(item: CleanItem) {
        _uiState.update { state ->
            val updated = state.cleanItems.map {
                if (it.file.path == item.file.path) it.copy(isSelected = !it.isSelected) else it
            }
            state.copy(cleanItems = updated)
        }
    }

    fun selectAllCleanItems(select: Boolean) {
        _uiState.update { state ->
            val updated = state.cleanItems.map { it.copy(isSelected = select) }
            state.copy(cleanItems = updated)
        }
    }

    fun deleteSelectedCleanItems() {
        val selected = _uiState.value.cleanItems.filter { it.isSelected }
        if (selected.isEmpty()) return

        viewModelScope.launch {
            var deletedBytes = 0L
            var count = 0
            for (item in selected) {
                val f = File(item.file.path)
                val len = f.length()
                if (fileService.delete(f).isSuccess) {
                    deletedBytes += len
                    count++
                }
            }
            showFeedback("تم تنظيف $count ملف وتحرير ${formatBytes(deletedBytes)}")
            startCleanScan()
            refreshStorageInfo()
        }
    }

    // Large Files Tool
    fun startLargeFilesScan() {
        _uiState.update { it.copy(isLargeScanning = true) }
        viewModelScope.launch {
            val files = fileService.scanLargeFiles(10L * 1024 * 1024)
            _uiState.update { it.copy(largeFiles = files, isLargeScanning = false) }
        }
    }

    fun toggleLargeFilesSort() {
        _uiState.update { state ->
            val reversed = state.largeFiles.reversed()
            state.copy(largeFiles = reversed)
        }
    }

    // Duplicates Tool
    fun startDuplicatesScan() {
        _uiState.update { it.copy(isDuplicatesScanning = true, selectedDuplicatePaths = emptySet()) }
        viewModelScope.launch {
            val groups = fileService.scanDuplicates()
            // Auto-select duplicate candidates (all files except the first one in each group)
            val toSelect = mutableSetOf<String>()
            for (g in groups) {
                toSelect.addAll(g.files.drop(1).map { it.path })
            }
            _uiState.update {
                it.copy(
                    duplicateGroups = groups,
                    isDuplicatesScanning = false,
                    selectedDuplicatePaths = toSelect
                )
            }
        }
    }

    fun toggleDuplicateSelection(path: String) {
        _uiState.update { state ->
            val set = state.selectedDuplicatePaths.toMutableSet()
            if (set.contains(path)) set.remove(path) else set.add(path)
            state.copy(selectedDuplicatePaths = set)
        }
    }

    fun deleteSelectedDuplicates() {
        val paths = _uiState.value.selectedDuplicatePaths
        if (paths.isEmpty()) return

        viewModelScope.launch {
            var freed = 0L
            var count = 0
            for (p in paths) {
                val f = File(p)
                val len = f.length()
                if (fileService.delete(f).isSuccess) {
                    freed += len
                    count++
                }
            }
            showFeedback("تم حذف $count نسخة مكررة وتوفير ${formatBytes(freed)}")
            startDuplicatesScan()
            refreshStorageInfo()
        }
    }

    // Assistant Chat
    fun sendAssistantMessage(text: String) {
        if (text.isBlank()) return
        val userMsg = ChatMessage(sender = MessageSender.USER, text = text)
        _uiState.update {
            it.copy(chatMessages = it.chatMessages + userMsg, isAssistantThinking = true)
        }

        viewModelScope.launch {
            val reply = assistant.getAssistantResponse(text)
            val rafiqiMsg = ChatMessage(sender = MessageSender.RAFIQI, text = reply)
            _uiState.update {
                it.copy(chatMessages = it.chatMessages + rafiqiMsg, isAssistantThinking = false)
            }
        }
    }

    // View Options
    fun setViewMode(mode: ViewMode) {
        viewModelScope.launch {
            repository.setViewMode(mode)
        }
    }

    fun setSortOption(sort: SortOption) {
        viewModelScope.launch {
            repository.setSortOption(sort)
            loadCurrentDirectory()
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            repository.setThemeMode(mode)
        }
    }

    fun setConfirmDelete(confirm: Boolean) {
        viewModelScope.launch {
            repository.setConfirmDelete(confirm)
        }
    }

    fun showFeedback(msg: String) {
        _uiState.update { it.copy(feedbackMessage = msg) }
    }

    fun clearFeedback() {
        _uiState.update { it.copy(feedbackMessage = null) }
    }

    fun clearRecents() {
        viewModelScope.launch {
            repository.clearRecents()
            showFeedback("تم مسح سجل الملفات الحديثة")
        }
    }

    private fun formatBytes(bytes: Long): String {
        val mb = bytes / (1024.0 * 1024.0)
        val gb = mb / 1024.0
        return when {
            gb >= 1.0 -> String.format(java.util.Locale.US, "%.1f GB", gb)
            mb >= 1.0 -> String.format(java.util.Locale.US, "%.1f MB", mb)
            else -> String.format(java.util.Locale.US, "%.1f KB", bytes / 1024.0)
        }
    }
}
