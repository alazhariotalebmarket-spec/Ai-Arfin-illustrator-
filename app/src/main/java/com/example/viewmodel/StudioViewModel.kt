package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.engine.*
import com.example.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

enum class MainTab { HOME, PROJECTS, TEMPLATES, SETTINGS }
enum class SortOrder(val label: String) { NEWEST("Newest First"), OLDEST("Oldest First"), NAME("Name A–Z") }

data class EditorUiState(
    val isOpen: Boolean = false,
    val document: CanvasDocument = CanvasDocument(),
    val selectedLayerId: String? = null,
    val clipboardLayer: LayerElement? = null,
    val activeTool: ToolMode = ToolMode.SELECT,
    val activeShapeType: ShapeType = ShapeType.RECTANGLE,
    val activeColor: Long = 0xFFFF7A00L,
    val activeGradientMid: Long? = null,
    val activeGradientEnd: Long? = null,
    val activeGradientType: GradientType = GradientType.LINEAR,
    val activeGradientAngle: Float = 45f,
    val activeStrokeColor: Long = 0xFF1E1E1EL,
    val activeStrokeWidth: Float = 0f,
    val activeStrokeStyle: StrokeStyle = StrokeStyle.SOLID,
    val activeCornerRadius: Float = 16f,
    val brushSize: Float = 18f,
    val brushOpacity: Float = 1f,
    val brushStyle: BrushStyle = BrushStyle.ROUND,
    val penStrokeWidth: Float = 10f,
    val eraserSize: Float = 44f,
    val showGrid: Boolean = false,
    val showRuler: Boolean = true,
    val snapToGrid: Boolean = false,
    val transparentPreview: Boolean = false,
    val layersPanelOpen: Boolean = false,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val undoCount: Int = 0,
    val lastSavedAt: Long? = null,
    val isSaving: Boolean = false,
    val isProcessingImage: Boolean = false,
    val statusMessage: String? = null
)

class StudioViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val projectRepo = ProjectRepository(application, db.projectDao())
    private val prefsRepo = UserPreferencesRepository(application)

    val preferences: StateFlow<UserPreferences> = prefsRepo.preferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferences())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortOrder = MutableStateFlow(SortOrder.NEWEST)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _currentTab = MutableStateFlow(MainTab.HOME)
    val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

    private val _showSplash = MutableStateFlow(true)
    val showSplash: StateFlow<Boolean> = _showSplash.asStateFlow()

    private val _showTutorialOverride = MutableStateFlow(false)
    val showTutorialOverride: StateFlow<Boolean> = _showTutorialOverride.asStateFlow()

    val projects: StateFlow<List<ProjectEntity>> = combine(
        projectRepo.allProjects,
        _searchQuery,
        _sortOrder
    ) { list, query, sort ->
        val filtered = if (query.isBlank()) list else {
            val q = query.trim().lowercase()
            list.filter { it.name.lowercase().contains(q) || it.category.lowercase().contains(q) }
        }
        when (sort) {
            SortOrder.NEWEST -> filtered.sortedByDescending { it.updatedAt }
            SortOrder.OLDEST -> filtered.sortedBy { it.updatedAt }
            SortOrder.NAME -> filtered.sortedBy { it.name.lowercase() }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _editorState = MutableStateFlow(EditorUiState())
    val editorState: StateFlow<EditorUiState> = _editorState.asStateFlow()

    // Up to 100 history steps
    private val undoStack = ArrayDeque<CanvasDocument>()
    private val redoStack = ArrayDeque<CanvasDocument>()
    private var autoSaveJob: Job? = null

    fun finishSplash() {
        _showSplash.value = false
    }

    fun selectTab(tab: MainTab) {
        _currentTab.value = tab
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    // Preferences Actions
    fun setDarkTheme(dark: Boolean) = viewModelScope.launch { prefsRepo.setDarkTheme(dark) }
    fun setUserName(name: String) = viewModelScope.launch { prefsRepo.setUserName(name) }
    fun setAutoSave(enabled: Boolean) = viewModelScope.launch { prefsRepo.setAutoSave(enabled) }
    fun setDefaultResolution(res: ExportResolution) = viewModelScope.launch { prefsRepo.setDefaultResolution(res) }
    fun setGridDefault(enabled: Boolean) = viewModelScope.launch { prefsRepo.setGridDefault(enabled) }
    fun setRulerDefault(enabled: Boolean) = viewModelScope.launch { prefsRepo.setRulerDefault(enabled) }
    fun setSnapDefault(enabled: Boolean) = viewModelScope.launch { prefsRepo.setSnapDefault(enabled) }
    fun completeTutorial() = viewModelScope.launch {
        prefsRepo.setTutorialCompleted(true)
        _showTutorialOverride.value = false
    }
    fun openTutorial() {
        _showTutorialOverride.value = true
    }

    // Project Browser Actions
    fun createNewDesign(
        name: String,
        width: Int,
        height: Int,
        category: String = "Blank Canvas",
        backgroundColor: Long = 0xFFFFFFFFL,
        dpi: Int = 300,
        smartLabel: String? = null
    ) {
        val doc = CanvasDocument.createSmart(
            name = name.ifBlank { "Untitled Design" },
            targetWidthPx = width,
            targetHeightPx = height,
            dpi = dpi,
            category = category,
            backgroundColor = backgroundColor,
            smartLabel = smartLabel
        )
        openDocumentInEditor(doc)
    }

    fun openTemplate(template: DesignTemplate) {
        val doc = template.documentBuilder()
        openDocumentInEditor(doc)
    }

    fun openProject(projectId: String) {
        viewModelScope.launch {
            val doc = projectRepo.loadDocument(projectId) ?: return@launch
            openDocumentInEditor(doc)
        }
    }

    fun duplicateProject(project: ProjectEntity) {
        viewModelScope.launch {
            projectRepo.duplicateProject(project)
        }
    }

    fun renameProject(projectId: String, newName: String) {
        viewModelScope.launch {
            projectRepo.renameProject(projectId, newName)
        }
    }

    fun deleteProject(projectId: String) {
        viewModelScope.launch {
            projectRepo.deleteProject(projectId)
        }
    }

    fun importVectorProject(uri: Uri) {
        viewModelScope.launch {
            val imported = projectRepo.importVectorBackup(uri)
            if (imported != null) {
                openDocumentInEditor(imported)
                _editorState.update { it.copy(statusMessage = "Vector project imported offline") }
            }
        }
    }

    private fun openDocumentInEditor(doc: CanvasDocument) {
        undoStack.clear()
        redoStack.clear()
        val prefs = preferences.value
        val normalized = doc.copy(pages = doc.normalizedPages())
        _editorState.value = EditorUiState(
            isOpen = true,
            document = normalized,
            showGrid = prefs.showGridDefault,
            showRuler = prefs.showRulerDefault,
            snapToGrid = prefs.snapToGridDefault,
            lastSavedAt = normalized.updatedAt
        )
        startAutoSaveLoop()
    }

    fun closeEditor() {
        autoSaveJob?.cancel()
        val currentDoc = _editorState.value.document
        viewModelScope.launch {
            projectRepo.saveProject(currentDoc)
        }
        _editorState.update { it.copy(isOpen = false) }
    }

    private fun startAutoSaveLoop() {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            while (true) {
                delay(30_000L)
                if (_editorState.value.isOpen && preferences.value.autoSaveEnabled) {
                    saveCurrentProject(silent = true)
                }
            }
        }
    }

    fun saveCurrentProject(silent: Boolean = false) {
        val doc = _editorState.value.document
        viewModelScope.launch {
            _editorState.update { it.copy(isSaving = true) }
            val saved = projectRepo.saveProject(doc)
            _editorState.update {
                it.copy(
                    isSaving = false,
                    lastSavedAt = saved.updatedAt,
                    statusMessage = if (silent) null else "Project saved offline"
                )
            }
        }
    }

    fun clearStatusMessage() {
        _editorState.update { it.copy(statusMessage = null) }
    }

    // Undo / Redo (up to 100 steps)
    private fun pushHistoryAndUpdate(newDoc: CanvasDocument, newSelection: String? = _editorState.value.selectedLayerId) {
        val synced = newDoc.copy(pages = newDoc.normalizedPages())
        val current = _editorState.value.document
        if (current != synced) {
            undoStack.addLast(current)
            while (undoStack.size > 100) {
                undoStack.removeFirst()
            }
            redoStack.clear()
        }
        _editorState.update {
            it.copy(
                document = synced,
                selectedLayerId = newSelection,
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty(),
                undoCount = undoStack.size
            )
        }
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        val current = _editorState.value.document
        redoStack.addLast(current)
        val prev = undoStack.removeLast()
        _editorState.update {
            it.copy(
                document = prev,
                selectedLayerId = prev.layers.find { l -> l.id == it.selectedLayerId }?.id,
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty(),
                undoCount = undoStack.size
            )
        }
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        val current = _editorState.value.document
        undoStack.addLast(current)
        val next = redoStack.removeLast()
        _editorState.update {
            it.copy(
                document = next,
                selectedLayerId = next.layers.find { l -> l.id == it.selectedLayerId }?.id,
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty(),
                undoCount = undoStack.size
            )
        }
    }

    // Tool & Studio UI Toggles
    fun setTool(tool: ToolMode) {
        _editorState.update { it.copy(activeTool = tool) }
    }

    fun setShapeType(shapeType: ShapeType) {
        _editorState.update { it.copy(activeShapeType = shapeType, activeTool = ToolMode.SHAPES) }
    }

    fun toggleGrid() = _editorState.update { it.copy(showGrid = !it.showGrid) }
    fun toggleRuler() = _editorState.update { it.copy(showRuler = !it.showRuler) }
    fun toggleSnap() = _editorState.update { it.copy(snapToGrid = !it.snapToGrid) }
    fun toggleTransparentPreview() = _editorState.update { it.copy(transparentPreview = !it.transparentPreview) }
    fun setLayersPanelOpen(open: Boolean) = _editorState.update { it.copy(layersPanelOpen = open) }

    fun renameDocument(newName: String) {
        val clean = newName.trim().ifEmpty { "Untitled Design" }
        pushHistoryAndUpdate(_editorState.value.document.copy(name = clean))
    }

    fun updateArtboardSize(width: Int, height: Int) {
        val doc = _editorState.value.document
        pushHistoryAndUpdate(
            doc.copy(
                artboardWidth = width.coerceIn(100, 4096),
                artboardHeight = height.coerceIn(100, 4096)
            )
        )
    }

    fun setArtboardBackground(
        color: Long,
        gradientEnd: Long? = null,
        gradientMid: Long? = null,
        gradientType: GradientType = GradientType.LINEAR
    ) {
        val doc = _editorState.value.document
        pushHistoryAndUpdate(
            doc.copy(
                backgroundColor = color,
                backgroundGradientMid = gradientMid,
                backgroundGradientEnd = gradientEnd,
                backgroundGradientType = gradientType
            )
        )
        viewModelScope.launch { prefsRepo.addRecentColor(color) }
    }

    fun setArtboardBackgroundImage(uri: Uri?) {
        if (uri == null) {
            val doc = _editorState.value.document
            pushHistoryAndUpdate(doc.copy(backgroundImagePath = null))
            return
        }
        viewModelScope.launch {
            _editorState.update { it.copy(isProcessingImage = true) }
            val imported = projectRepo.importImageHighRes(uri)
            if (imported != null) {
                val doc = _editorState.value.document
                pushHistoryAndUpdate(doc.copy(backgroundImagePath = imported.path))
                _editorState.update { it.copy(statusMessage = "Canvas background image updated") }
            }
            _editorState.update { it.copy(isProcessingImage = false) }
        }
    }

    // Multi-Page Controls (Add Page, Duplicate Page, Delete Page, Switch Page)
    fun addPage() {
        val doc = _editorState.value.document
        val currentPages = doc.normalizedPages()
        val newPage = CanvasPage(
            name = "Page ${currentPages.size + 1}",
            artboardWidth = doc.artboardWidth,
            artboardHeight = doc.artboardHeight,
            targetExportWidth = doc.targetExportWidth,
            targetExportHeight = doc.targetExportHeight,
            backgroundColor = 0xFFFFFFFFL,
            layers = emptyList()
        )
        val updatedPages = currentPages + newPage
        val switched = doc.copy(pages = updatedPages).switchToPage(updatedPages.lastIndex)
        pushHistoryAndUpdate(switched, newSelection = null)
        _editorState.update { it.copy(statusMessage = "Added ${newPage.name}") }
    }

    fun duplicateCurrentPage() {
        val doc = _editorState.value.document
        val currentPages = doc.normalizedPages()
        val activeIdx = doc.activePageIndex.coerceIn(0, currentPages.lastIndex)
        val cur = currentPages[activeIdx]
        val dupPage = cur.copy(
            id = UUID.randomUUID().toString(),
            name = "${cur.name} Copy",
            layers = cur.layers.map { it.duplicateWithNewId() }
        )
        val updatedPages = currentPages.toMutableList().apply { add(activeIdx + 1, dupPage) }
        val switched = doc.copy(pages = updatedPages).switchToPage(activeIdx + 1)
        pushHistoryAndUpdate(switched, newSelection = null)
        _editorState.update { it.copy(statusMessage = "Duplicated page") }
    }

    fun deleteCurrentPage() {
        val doc = _editorState.value.document
        val currentPages = doc.normalizedPages()
        if (currentPages.size <= 1) {
            _editorState.update { it.copy(statusMessage = "At least 1 page is required") }
            return
        }
        val activeIdx = doc.activePageIndex.coerceIn(0, currentPages.lastIndex)
        val updatedPages = currentPages.toMutableList().apply { removeAt(activeIdx) }
        val nextIdx = activeIdx.coerceAtMost(updatedPages.lastIndex)
        val switched = doc.copy(pages = updatedPages).switchToPage(nextIdx)
        pushHistoryAndUpdate(switched, newSelection = null)
        _editorState.update { it.copy(statusMessage = "Page deleted") }
    }

    fun switchPage(index: Int) {
        val doc = _editorState.value.document
        val switched = doc.switchToPage(index)
        _editorState.update {
            it.copy(
                document = switched,
                selectedLayerId = null
            )
        }
    }

    fun setActiveColor(
        color: Long,
        gradientEnd: Long? = null,
        gradientMid: Long? = null,
        gradientType: GradientType = _editorState.value.activeGradientType,
        gradientAngle: Float = _editorState.value.activeGradientAngle
    ) {
        _editorState.update {
            it.copy(
                activeColor = color,
                activeGradientMid = gradientMid,
                activeGradientEnd = gradientEnd,
                activeGradientType = gradientType,
                activeGradientAngle = gradientAngle
            )
        }
        viewModelScope.launch { prefsRepo.addRecentColor(color) }
        val sel = selectedLayer() ?: return
        if (sel.locked) return
        val updated: LayerElement = when (sel) {
            is ShapeLayer -> sel.copy(
                fillColor = color,
                gradientMidColor = gradientMid,
                gradientEndColor = gradientEnd,
                gradientType = gradientType,
                gradientAngle = gradientAngle
            )
            is PathLayer -> sel.copy(strokeColor = color)
            is TextLayer -> sel.copy(
                color = color,
                gradientMidColor = gradientMid,
                gradientEndColor = gradientEnd,
                gradientType = gradientType,
                gradientAngle = gradientAngle
            )
            is StickerLayer -> sel.copy(primaryColor = color)
            is ImageLayer -> sel
        }
        replaceLayerWithHistory(updated)
    }

    fun setShapeStrokeOptions(color: Long, width: Float, style: StrokeStyle) {
        _editorState.update {
            it.copy(
                activeStrokeColor = color,
                activeStrokeWidth = width,
                activeStrokeStyle = style
            )
        }
        viewModelScope.launch { prefsRepo.addRecentColor(color) }
        val sel = selectedLayer() ?: return
        if (sel.locked) return
        when (sel) {
            is ShapeLayer -> replaceLayerWithHistory(sel.copy(strokeColor = color, strokeWidth = width, strokeStyle = style))
            is PathLayer -> replaceLayerWithHistory(sel.copy(strokeColor = color, strokeWidth = width.coerceAtLeast(2f), strokeStyle = style))
            is TextLayer -> replaceLayerWithHistory(sel.copy(strokeColor = color, strokeWidth = width))
            else -> {}
        }
    }

    fun setShapeStrokeColor(color: Long) {
        val curW = _editorState.value.activeStrokeWidth.let { if (it <= 0f) 6f else it }
        setShapeStrokeOptions(color, curW, _editorState.value.activeStrokeStyle)
    }

    fun setBrushSettings(size: Float, opacity: Float, style: BrushStyle) {
        _editorState.update { it.copy(brushSize = size, brushOpacity = opacity, brushStyle = style) }
    }

    fun setPenStrokeWidth(width: Float) {
        _editorState.update { it.copy(penStrokeWidth = width) }
    }

    fun setEraserSize(size: Float) {
        _editorState.update { it.copy(eraserSize = size) }
    }

    fun selectedLayer(): LayerElement? {
        val id = _editorState.value.selectedLayerId ?: return null
        return _editorState.value.document.layers.find { it.id == id }
    }

    fun selectLayer(layerId: String?) {
        _editorState.update { it.copy(selectedLayerId = layerId) }
    }

    fun hitTestAndSelect(artboardX: Float, artboardY: Float): LayerElement? {
        val layers = _editorState.value.document.layers
        for (i in layers.indices.reversed()) {
            val layer = layers[i]
            if (!layer.visible) continue
            val pad = 20f
            if (artboardX >= layer.x - pad && artboardX <= layer.x + layer.width + pad &&
                artboardY >= layer.y - pad && artboardY <= layer.y + layer.height + pad
            ) {
                _editorState.update { it.copy(selectedLayerId = layer.id) }
                return layer
            }
        }
        _editorState.update { it.copy(selectedLayerId = null) }
        return null
    }

    // Add Layers
    fun addShapeLayer(
        shapeType: ShapeType = _editorState.value.activeShapeType,
        x: Float? = null,
        y: Float? = null,
        width: Float? = null,
        height: Float? = null
    ) {
        val state = _editorState.value
        val doc = state.document
        val w = width ?: min(doc.artboardWidth, doc.artboardHeight) * 0.34f
        val h = height ?: if (shapeType == ShapeType.LINE) 40f else w
        val lx = x ?: (doc.artboardWidth - w) / 2f
        val ly = y ?: (doc.artboardHeight - h) / 2f
        val layer = ShapeLayer(
            name = shapeType.label,
            x = snap(lx),
            y = snap(ly),
            width = max(24f, w),
            height = max(24f, h),
            shapeType = shapeType,
            fillColor = state.activeColor,
            gradientMidColor = state.activeGradientMid,
            gradientEndColor = state.activeGradientEnd,
            gradientType = state.activeGradientType,
            gradientAngle = state.activeGradientAngle,
            strokeColor = state.activeStrokeColor,
            strokeWidth = if (shapeType == ShapeType.ARC) max(10f, state.activeStrokeWidth) else state.activeStrokeWidth,
            strokeStyle = state.activeStrokeStyle,
            cornerRadius = when (shapeType) {
                ShapeType.ROUNDED_RECT -> max(36f, state.activeCornerRadius)
                ShapeType.RECTANGLE -> state.activeCornerRadius
                else -> 0f
            }
        )
        pushHistoryAndUpdate(doc.copy(layers = doc.layers + layer), newSelection = layer.id)
    }

    fun addPathLayer(rawPoints: List<PathPoint>, isPenTool: Boolean, closed: Boolean = false) {
        if (rawPoints.size < 2) return
        val state = _editorState.value
        val minX = rawPoints.minOf { it.x }
        val minY = rawPoints.minOf { it.y }
        val maxX = rawPoints.maxOf { it.x }
        val maxY = rawPoints.maxOf { it.y }
        val w = max(24f, maxX - minX)
        val h = max(24f, maxY - minY)
        val normalized = rawPoints.map { PathPoint(it.x - minX, it.y - minY) }
        val layer = PathLayer(
            name = if (isPenTool) "Vector Anchor Path" else "Brush Stroke",
            x = minX,
            y = minY,
            width = w,
            height = h,
            opacity = if (isPenTool) 1f else state.brushOpacity,
            points = normalized,
            strokeColor = state.activeColor,
            strokeWidth = if (isPenTool) state.penStrokeWidth else state.brushSize,
            strokeStyle = state.activeStrokeStyle,
            isPenSmooth = true,
            isClosed = closed,
            fillColor = if (closed) state.activeColor else null,
            brushStyle = if (isPenTool) BrushStyle.ROUND else state.brushStyle
        )
        val doc = state.document
        pushHistoryAndUpdate(doc.copy(layers = doc.layers + layer), newSelection = layer.id)
    }

    fun convertSelectedShapeToEditablePath(): PathLayer? {
        val sel = selectedLayer() as? ShapeLayer ?: return null
        val pts = when (sel.shapeType) {
            ShapeType.TRIANGLE -> listOf(
                PathPoint(sel.width / 2f, 0f),
                PathPoint(sel.width, sel.height),
                PathPoint(0f, sel.height)
            )
            ShapeType.LINE -> listOf(
                PathPoint(0f, sel.height / 2f),
                PathPoint(sel.width / 2f, sel.height / 2f),
                PathPoint(sel.width, sel.height / 2f)
            )
            else -> listOf(
                PathPoint(0f, 0f),
                PathPoint(sel.width, 0f),
                PathPoint(sel.width, sel.height),
                PathPoint(0f, sel.height)
            )
        }
        val pathLayer = PathLayer(
            id = sel.id,
            name = "${sel.name} (Points)",
            x = sel.x,
            y = sel.y,
            width = sel.width,
            height = sel.height,
            rotation = sel.rotation,
            opacity = sel.opacity,
            points = pts,
            strokeColor = if ((sel.strokeColor ushr 24) > 0L) sel.strokeColor else sel.fillColor,
            strokeWidth = max(6f, sel.strokeWidth),
            isPenSmooth = sel.shapeType == ShapeType.CIRCLE,
            fillColor = if (sel.shapeType != ShapeType.LINE) sel.fillColor else null,
            isClosed = sel.shapeType != ShapeType.LINE,
            flipH = sel.flipH,
            flipV = sel.flipV,
            groupId = sel.groupId,
            effects = sel.effects
        )
        replaceLayerWithHistory(pathLayer)
        return pathLayer
    }

    fun updatePathAnchorPointLive(layerId: String, pointIndex: Int, artboardX: Float, artboardY: Float) {
        val doc = _editorState.value.document
        val layer = doc.layers.find { it.id == layerId } as? PathLayer ?: return
        if (layer.locked || pointIndex !in layer.points.indices) return
        val newLocalX = artboardX - layer.x
        val newLocalY = artboardY - layer.y
        val updatedPts = layer.points.toMutableList().apply {
            this[pointIndex] = PathPoint(newLocalX, newLocalY)
        }
        val updatedLayer = layer.copy(points = updatedPts)
        _editorState.update {
            it.copy(document = doc.copy(layers = doc.layers.map { l -> if (l.id == layerId) updatedLayer else l }))
        }
    }

    fun addTextLayer(
        text: String,
        fontId: String = "hind_siliguri",
        fontSize: Float = 72f,
        color: Long = _editorState.value.activeColor,
        bold: Boolean = false,
        italic: Boolean = false,
        underline: Boolean = false,
        lineSpacing: Float = 1.15f,
        letterSpacing: Float = 0f,
        textBackgroundColor: Long? = null,
        curveAmount: Float = 0f,
        align: TextAlignMode = TextAlignMode.CENTER,
        shadowEnabled: Boolean = false,
        strokeWidth: Float = 0f,
        strokeColor: Long = 0xFFFFFFFFL
    ) {
        val doc = _editorState.value.document
        val w = doc.artboardWidth * 0.72f
        val h = max(140f, fontSize * 2.2f)
        val layer = TextLayer(
            name = text.take(18).ifBlank { "Text" },
            x = (doc.artboardWidth - w) / 2f,
            y = (doc.artboardHeight - h) / 2f,
            width = w,
            height = h,
            text = text,
            fontId = fontId,
            fontSize = fontSize,
            color = color,
            bold = bold,
            italic = italic,
            underline = underline,
            lineSpacing = lineSpacing,
            letterSpacing = letterSpacing,
            textBackgroundColor = textBackgroundColor,
            curveAmount = curveAmount,
            align = align,
            shadowEnabled = shadowEnabled,
            strokeWidth = strokeWidth,
            strokeColor = strokeColor
        )
        pushHistoryAndUpdate(doc.copy(layers = doc.layers + layer), newSelection = layer.id)
    }

    fun addStickerLayer(sticker: StickerAsset) {
        val doc = _editorState.value.document
        val size = min(doc.artboardWidth, doc.artboardHeight) * 0.35f
        val layer = StickerLayer(
            name = sticker.title,
            x = (doc.artboardWidth - size) / 2f,
            y = (doc.artboardHeight - size) / 2f,
            width = size,
            height = size,
            stickerId = sticker.id,
            primaryColor = sticker.defaultPrimaryColor,
            secondaryColor = sticker.defaultSecondaryColor
        )
        pushHistoryAndUpdate(doc.copy(layers = doc.layers + layer), newSelection = layer.id)
    }

    fun importImageFromUri(uri: Uri) {
        viewModelScope.launch {
            _editorState.update { it.copy(isProcessingImage = true) }
            val imported = projectRepo.importImageHighRes(uri)
            if (imported != null) {
                val doc = _editorState.value.document
                val aspect = imported.width.toFloat() / max(1, imported.height).toFloat()
                val maxW = doc.artboardWidth * 0.78f
                val maxH = doc.artboardHeight * 0.78f
                val (layerW, layerH) = if (maxW / aspect <= maxH) {
                    maxW to (maxW / aspect)
                } else {
                    (maxH * aspect) to maxH
                }
                val layer = ImageLayer(
                    name = "Photo (${imported.width}×${imported.height})",
                    x = (doc.artboardWidth - layerW) / 2f,
                    y = (doc.artboardHeight - layerH) / 2f,
                    width = layerW,
                    height = layerH,
                    imagePath = imported.path,
                    originalImagePath = imported.path
                )
                pushHistoryAndUpdate(doc.copy(layers = doc.layers + layer), newSelection = layer.id)
                _editorState.update { it.copy(statusMessage = "Photo loaded (${imported.width}×${imported.height} HD)") }
            } else {
                _editorState.update { it.copy(statusMessage = "Could not load selected image") }
            }
            _editorState.update { it.copy(isProcessingImage = false, activeTool = ToolMode.SELECT) }
        }
    }

    fun openPhotoInTransparentHdStudio(uri: Uri) {
        viewModelScope.launch {
            val imported = projectRepo.importImageHighRes(uri)
            if (imported == null) {
                _editorState.update { it.copy(statusMessage = "Could not open photo from storage") }
                return@launch
            }
            val aspect = imported.width.toFloat() / max(1, imported.height).toFloat()
            val targetMax = max(3840, max(imported.width, imported.height))
            val (targetW, targetH) = if (aspect >= 1f) {
                targetMax to max(320, (targetMax / aspect).roundToInt())
            } else {
                max(320, (targetMax * aspect).roundToInt()) to targetMax
            }
            val baseDoc = CanvasDocument.createSmart(
                name = "HD Cutout ${imported.width}×${imported.height}",
                targetWidthPx = targetW,
                targetHeightPx = targetH,
                dpi = 300,
                category = "HD Cutout",
                backgroundColor = 0x00FFFFFFL,
                smartLabel = "HD Studio • ${targetW}×${targetH}px Transparent"
            )
            val layer = ImageLayer(
                name = "Photo (${imported.width}×${imported.height})",
                x = 0f,
                y = 0f,
                width = baseDoc.artboardWidth.toFloat(),
                height = baseDoc.artboardHeight.toFloat(),
                imagePath = imported.path,
                originalImagePath = imported.path
            )
            val doc = baseDoc.copy(layers = listOf(layer))
            openDocumentInEditor(doc)
            _editorState.update {
                it.copy(
                    selectedLayerId = layer.id,
                    transparentPreview = true,
                    activeTool = ToolMode.SELECT,
                    statusMessage = "Tap Image below to Crop part or Remove Background in 4K HD"
                )
            }
        }
    }

    fun removeBackgroundFromSelectedImage(
        tolerance: Float = 0.24f,
        globalKey: Boolean = false,
        targetColor: Int? = null,
        edgeSmoothness: Float = 0.28f
    ) {
        val sel = selectedLayer() as? ImageLayer ?: return
        val sourcePath = sel.originalImagePath.ifBlank { sel.imagePath }
        viewModelScope.launch {
            _editorState.update { it.copy(isProcessingImage = true) }
            val processed = BackgroundRemover.removeBackground(
                imagePath = sourcePath,
                tolerance = tolerance,
                globalKeyMode = globalKey,
                targetColor = targetColor,
                edgeSmoothness = edgeSmoothness
            )
            if (processed != null) {
                val newPath = projectRepo.saveBitmapToLocalStorage(processed)
                processed.recycle()
                CanvasRenderer.invalidateBitmapCache(sel.imagePath)
                replaceLayerWithHistory(
                    sel.copy(
                        imagePath = newPath,
                        originalImagePath = sourcePath,
                        name = if (sel.name.contains("Transparent")) sel.name else "${sel.name} (Transparent)"
                    )
                )
                _editorState.update {
                    it.copy(
                        transparentPreview = true,
                        statusMessage = "Transparent background applied! Export as PNG for HD"
                    )
                }
            } else {
                _editorState.update { it.copy(statusMessage = "Background removal failed") }
            }
            _editorState.update { it.copy(isProcessingImage = false) }
        }
    }

    fun applyCropAndEnhanceSelectedImage(
        cropLeft: Float,
        cropTop: Float,
        cropRight: Float,
        cropBottom: Float,
        cornerRadiusRatio: Float,
        upscaleHd: Boolean,
        sharpenAmount: Float,
        fitArtboardToCutout: Boolean
    ) {
        val sel = selectedLayer() as? ImageLayer ?: return
        val sourcePath = sel.imagePath
        viewModelScope.launch {
            _editorState.update { it.copy(isProcessingImage = true) }
            val croppedBmp = BackgroundRemover.cropAndEnhanceToHighRes(
                imagePath = sourcePath,
                cropLeft = cropLeft,
                cropTop = cropTop,
                cropRight = cropRight,
                cropBottom = cropBottom,
                upscaleToHighRes = upscaleHd,
                targetMaxEdge = if (upscaleHd) 3840 else 2048,
                sharpenAmount = sharpenAmount
            )
            if (croppedBmp != null) {
                val cw = croppedBmp.width
                val ch = croppedBmp.height
                val newPath = projectRepo.saveBitmapToLocalStorage(croppedBmp)
                croppedBmp.recycle()
                CanvasRenderer.invalidateBitmapCache(sel.imagePath)

                val aspect = cw.toFloat() / max(1, ch).toFloat()
                if (fitArtboardToCutout) {
                    val targetMax = max(3840, max(cw, ch))
                    val (targetW, targetH) = if (aspect >= 1f) {
                        targetMax to max(320, (targetMax / aspect).roundToInt())
                    } else {
                        max(320, (targetMax * aspect).roundToInt()) to targetMax
                    }
                    val doc = _editorState.value.document
                    val smartDoc = CanvasDocument.createSmart(
                        name = doc.name,
                        targetWidthPx = targetW,
                        targetHeightPx = targetH,
                        dpi = 300,
                        category = doc.category,
                        backgroundColor = doc.backgroundColor
                    )
                    val updatedLayer = sel.copy(
                        name = "HD Cutout (${cw}×${ch})",
                        x = 0f,
                        y = 0f,
                        width = smartDoc.artboardWidth.toFloat(),
                        height = smartDoc.artboardHeight.toFloat(),
                        imagePath = newPath,
                        originalImagePath = newPath,
                        cropLeft = 0f,
                        cropTop = 0f,
                        cropRight = 1f,
                        cropBottom = 1f,
                        cornerRadiusRatio = cornerRadiusRatio
                    )
                    val nextDoc = doc.copy(
                        artboardWidth = smartDoc.artboardWidth,
                        artboardHeight = smartDoc.artboardHeight,
                        targetExportWidth = smartDoc.targetExportWidth,
                        targetExportHeight = smartDoc.targetExportHeight,
                        smartModeLabel = "HD Cutout • ${targetW}×${targetH}px",
                        layers = doc.layers.map { if (it.id == sel.id) updatedLayer else it }
                    )
                    pushHistoryAndUpdate(nextDoc, newSelection = updatedLayer.id)
                    _editorState.update {
                        it.copy(
                            transparentPreview = true,
                            statusMessage = "Cropped & upscaled to ${cw}×${ch} HD! Ready to Export"
                        )
                    }
                } else {
                    val baseArea = max(sel.width, sel.height)
                    val (newW, newH) = if (aspect >= 1f) {
                        baseArea to (baseArea / aspect).coerceAtLeast(48f)
                    } else {
                        (baseArea * aspect).coerceAtLeast(48f) to baseArea
                    }
                    val updatedLayer = sel.copy(
                        name = "Cutout (${cw}×${ch})",
                        width = newW,
                        height = newH,
                        imagePath = newPath,
                        originalImagePath = newPath,
                        cropLeft = 0f,
                        cropTop = 0f,
                        cropRight = 1f,
                        cropBottom = 1f,
                        cornerRadiusRatio = cornerRadiusRatio
                    )
                    replaceLayerWithHistory(updatedLayer)
                    _editorState.update {
                        it.copy(statusMessage = "Cropped part created at ${cw}×${ch} HD")
                    }
                }
            } else {
                _editorState.update { it.copy(statusMessage = "Could not process selected crop") }
            }
            _editorState.update { it.copy(isProcessingImage = false) }
        }
    }

    fun resetSelectedImageToOriginal() {
        val sel = selectedLayer() as? ImageLayer ?: return
        val orig = sel.originalImagePath.ifBlank { return }
        replaceLayerWithHistory(
            sel.copy(
                imagePath = orig,
                cropLeft = 0f,
                cropTop = 0f,
                cropRight = 1f,
                cropBottom = 1f,
                cornerRadiusRatio = 0f
            )
        )
        _editorState.update { it.copy(statusMessage = "Restored original photo") }
    }

    fun superResolveSelectedImage(
        targetMaxEdge: Int = 3840,
        profile: PixelRepairProfile = PixelRepairProfile.BALANCED_HD
    ) {
        val sel = selectedLayer() as? ImageLayer ?: return
        val origSource = sel.originalImagePath.ifBlank { sel.imagePath }
        viewModelScope.launch {
            _editorState.update { it.copy(isProcessingImage = true) }
            val enhanced = SuperResolutionEngine.enhanceImageFile(
                imagePath = sel.imagePath,
                targetMaxEdge = targetMaxEdge,
                profile = profile
            )
            if (enhanced != null) {
                val ew = enhanced.width
                val eh = enhanced.height
                val newPath = projectRepo.saveBitmapToLocalStorage(enhanced)
                enhanced.recycle()
                CanvasRenderer.invalidateBitmapCache(sel.imagePath)

                val doc = _editorState.value.document
                val updatedLayer = sel.copy(
                    name = "HD Restored (${ew}×${eh})",
                    imagePath = newPath,
                    originalImagePath = origSource
                )
                val nextDoc = if (doc.category == "HD Cutout" || doc.category == "HD Restore") {
                    doc.copy(
                        targetExportWidth = max(doc.targetExportWidth, ew),
                        targetExportHeight = max(doc.targetExportHeight, eh),
                        smartModeLabel = "Super-Res HD • ${ew}×${eh}px",
                        layers = doc.layers.map { if (it.id == sel.id) updatedLayer else it }
                    )
                } else {
                    doc.copy(layers = doc.layers.map { if (it.id == sel.id) updatedLayer else it })
                }
                pushHistoryAndUpdate(nextDoc, newSelection = updatedLayer.id)
                _editorState.update {
                    it.copy(statusMessage = "Every pixel restored to ${ew}×${eh} HD! Zoom in to inspect")
                }
            } else {
                _editorState.update { it.copy(statusMessage = "Pixel HD restore failed") }
            }
            _editorState.update { it.copy(isProcessingImage = false) }
        }
    }

    fun openAndSuperResolvePhotoFromUri(
        uri: Uri,
        targetMaxEdge: Int = 3840,
        profile: PixelRepairProfile = PixelRepairProfile.BALANCED_HD
    ) {
        viewModelScope.launch {
            val imported = projectRepo.importImageHighRes(uri)
            if (imported == null) {
                _editorState.update { it.copy(statusMessage = "Could not open photo") }
                return@launch
            }
            val enhanced = SuperResolutionEngine.enhanceImageFile(
                imagePath = imported.path,
                targetMaxEdge = targetMaxEdge,
                profile = profile
            )
            val finalPath: String
            val finalW: Int
            val finalH: Int
            if (enhanced != null) {
                finalW = enhanced.width
                finalH = enhanced.height
                finalPath = projectRepo.saveBitmapToLocalStorage(enhanced)
                enhanced.recycle()
            } else {
                finalW = imported.width
                finalH = imported.height
                finalPath = imported.path
            }

            val baseDoc = CanvasDocument.createSmart(
                name = "HD Restored ${finalW}×${finalH}",
                targetWidthPx = finalW,
                targetHeightPx = finalH,
                dpi = 300,
                category = "HD Restore",
                backgroundColor = 0xFFFFFFFFL,
                smartLabel = "Super-Res HD • ${finalW}×${finalH}px"
            )
            val layer = ImageLayer(
                name = "HD Photo (${finalW}×${finalH})",
                x = 0f,
                y = 0f,
                width = baseDoc.artboardWidth.toFloat(),
                height = baseDoc.artboardHeight.toFloat(),
                imagePath = finalPath,
                originalImagePath = imported.path
            )
            val doc = baseDoc.copy(layers = listOf(layer))
            openDocumentInEditor(doc)
            _editorState.update {
                it.copy(
                    selectedLayerId = layer.id,
                    activeTool = ToolMode.ZOOM,
                    statusMessage = "Restored ${imported.width}×${imported.height} → ${finalW}×${finalH} HD! Pinch to zoom 10x"
                )
            }
        }
    }

    // Live interactive transform (drag/resize/rotate) with deferred history snapshot
    private var preGestureDoc: CanvasDocument? = null

    fun beginTransformGesture() {
        preGestureDoc = _editorState.value.document
    }

    fun updateSelectedTransformLive(
        dx: Float = 0f,
        dy: Float = 0f,
        newWidth: Float? = null,
        newHeight: Float? = null,
        newRotation: Float? = null
    ) {
        val sel = selectedLayer() ?: return
        if (sel.locked) return
        val gid = sel.groupId
        val isMovingOnly = newWidth == null && newHeight == null && newRotation == null && gid != null
        val doc = _editorState.value.document
        val nextLayers = doc.layers.map { l ->
            when {
                l.id == sel.id -> l.withBase(
                    x = l.x + dx,
                    y = l.y + dy,
                    width = (newWidth ?: l.width).coerceAtLeast(24f),
                    height = (newHeight ?: l.height).coerceAtLeast(24f),
                    rotation = newRotation ?: l.rotation
                )
                isMovingOnly && l.groupId == gid && !l.locked -> l.withBase(x = l.x + dx, y = l.y + dy)
                else -> l
            }
        }
        _editorState.update {
            it.copy(document = doc.copy(layers = nextLayers))
        }
    }

    fun commitTransformGesture() {
        val before = preGestureDoc ?: return
        preGestureDoc = null
        val sel = selectedLayer()
        var current = _editorState.value.document
        if (sel != null && _editorState.value.snapToGrid) {
            val snapped = sel.withBase(x = snap(sel.x), y = snap(sel.y))
            current = current.copy(layers = current.layers.map { if (it.id == sel.id) snapped else it })
        }
        current = current.copy(pages = current.normalizedPages())
        if (before != current) {
            undoStack.addLast(before)
            while (undoStack.size > 100) undoStack.removeFirst()
            redoStack.clear()
            _editorState.update {
                it.copy(
                    document = current,
                    canUndo = undoStack.isNotEmpty(),
                    canRedo = redoStack.isNotEmpty(),
                    undoCount = undoStack.size
                )
            }
        }
    }

    fun eraseAtArtboardPoint(artboardX: Float, artboardY: Float) {
        val radius = _editorState.value.eraserSize
        val doc = _editorState.value.document
        // Remove any unlocked PathLayer or top hit layer overlapping eraser point
        val target = doc.layers.lastOrNull { layer ->
            if (!layer.visible || layer.locked) return@lastOrNull false
            if (layer is PathLayer) {
                layer.points.any { pt ->
                    val px = layer.x + pt.x
                    val py = layer.y + pt.y
                    abs(px - artboardX) <= radius && abs(py - artboardY) <= radius
                }
            } else {
                artboardX >= layer.x && artboardX <= layer.x + layer.width &&
                    artboardY >= layer.y && artboardY <= layer.y + layer.height
            }
        }
        if (target != null) {
            pushHistoryAndUpdate(
                doc.copy(layers = doc.layers.filterNot { it.id == target.id }),
                newSelection = if (_editorState.value.selectedLayerId == target.id) null else _editorState.value.selectedLayerId
            )
        }
    }

    fun replaceLayerWithHistory(updated: LayerElement) {
        val doc = _editorState.value.document
        val newLayers = doc.layers.map { if (it.id == updated.id) updated else it }
        pushHistoryAndUpdate(doc.copy(layers = newLayers), newSelection = updated.id)
    }

    // Layer Panel & Quick Actions
    fun duplicateSelected() {
        val sel = selectedLayer() ?: return
        val copy = sel.duplicateWithNewId()
        val doc = _editorState.value.document
        pushHistoryAndUpdate(doc.copy(layers = doc.layers + copy), newSelection = copy.id)
    }

    fun copySelected() {
        val sel = selectedLayer() ?: return
        _editorState.update { it.copy(clipboardLayer = sel, statusMessage = "Layer copied") }
    }

    fun pasteClipboard() {
        val clip = _editorState.value.clipboardLayer ?: return
        val pasted = clip.duplicateWithNewId()
        val doc = _editorState.value.document
        pushHistoryAndUpdate(doc.copy(layers = doc.layers + pasted), newSelection = pasted.id)
    }

    fun deleteLayer(layerId: String) {
        val doc = _editorState.value.document
        val nextSel = if (_editorState.value.selectedLayerId == layerId) null else _editorState.value.selectedLayerId
        pushHistoryAndUpdate(doc.copy(layers = doc.layers.filterNot { it.id == layerId }), newSelection = nextSel)
    }

    fun toggleLayerVisibility(layerId: String) {
        val doc = _editorState.value.document
        val next = doc.layers.map { if (it.id == layerId) it.withBase(visible = !it.visible) else it }
        pushHistoryAndUpdate(doc.copy(layers = next))
    }

    fun toggleLayerLock(layerId: String) {
        val doc = _editorState.value.document
        val next = doc.layers.map { if (it.id == layerId) it.withBase(locked = !it.locked) else it }
        pushHistoryAndUpdate(doc.copy(layers = next))
    }

    fun renameLayer(layerId: String, newName: String) {
        val doc = _editorState.value.document
        val clean = newName.trim().ifEmpty { "Layer" }
        val next = doc.layers.map { if (it.id == layerId) it.withBase(name = clean) else it }
        pushHistoryAndUpdate(doc.copy(layers = next))
    }

    fun bringForward(layerId: String) {
        val doc = _editorState.value.document
        val idx = doc.layers.indexOfFirst { it.id == layerId }
        if (idx in 0 until doc.layers.lastIndex) {
            val mutable = doc.layers.toMutableList()
            val item = mutable.removeAt(idx)
            mutable.add(idx + 1, item)
            pushHistoryAndUpdate(doc.copy(layers = mutable))
        }
    }

    fun sendBackward(layerId: String) {
        val doc = _editorState.value.document
        val idx = doc.layers.indexOfFirst { it.id == layerId }
        if (idx > 0) {
            val mutable = doc.layers.toMutableList()
            val item = mutable.removeAt(idx)
            mutable.add(idx - 1, item)
            pushHistoryAndUpdate(doc.copy(layers = mutable))
        }
    }

    fun alignSelected(action: AlignAction) {
        val sel = selectedLayer() ?: return
        if (sel.locked) return
        val doc = _editorState.value.document
        val aw = doc.artboardWidth.toFloat()
        val ah = doc.artboardHeight.toFloat()
        val aligned = when (action) {
            AlignAction.LEFT -> sel.withBase(x = 0f)
            AlignAction.CENTER_H -> sel.withBase(x = (aw - sel.width) / 2f)
            AlignAction.RIGHT -> sel.withBase(x = aw - sel.width)
            AlignAction.TOP -> sel.withBase(y = 0f)
            AlignAction.CENTER_V -> sel.withBase(y = (ah - sel.height) / 2f)
            AlignAction.BOTTOM -> sel.withBase(y = ah - sel.height)
        }
        replaceLayerWithHistory(aligned)
    }

    fun updateSelectedOpacity(opacity: Float) {
        val sel = selectedLayer() ?: return
        if (sel.locked) return
        val clamped = opacity.coerceIn(0.05f, 1f)
        val doc = _editorState.value.document
        _editorState.update {
            it.copy(document = doc.copy(layers = doc.layers.map { l -> if (l.id == sel.id) l.withBase(opacity = clamped) else l }))
        }
    }

    fun rotateSelectedBy(deltaDeg: Float) {
        val sel = selectedLayer() ?: return
        if (sel.locked) return
        val nextRot = (sel.rotation + deltaDeg) % 360f
        replaceLayerWithHistory(sel.withBase(rotation = nextRot))
    }

    fun nudgeSelected(dx: Float, dy: Float) {
        val sel = selectedLayer() ?: return
        if (sel.locked) return
        replaceLayerWithHistory(sel.withBase(x = sel.x + dx, y = sel.y + dy))
    }

    fun flipSelectedHorizontal() {
        val sel = selectedLayer() ?: return
        if (sel.locked) return
        replaceLayerWithHistory(sel.withExtras(flipH = !sel.flipH))
    }

    fun flipSelectedVertical() {
        val sel = selectedLayer() ?: return
        if (sel.locked) return
        replaceLayerWithHistory(sel.withExtras(flipV = !sel.flipV))
    }

    fun updateSelectedEffects(effects: LayerEffects) {
        val sel = selectedLayer() ?: return
        if (sel.locked) return
        replaceLayerWithHistory(sel.withExtras(effects = effects))
    }

    fun toggleGroupSelected() {
        val sel = selectedLayer() ?: return
        val doc = _editorState.value.document
        if (sel.groupId != null) {
            val targetGroup = sel.groupId
            val next = doc.layers.map { l ->
                if (l.groupId == targetGroup) l.withExtras(groupId = null) else l
            }
            pushHistoryAndUpdate(doc.copy(layers = next))
            _editorState.update { it.copy(statusMessage = "Ungrouped layers") }
        } else {
            val idx = doc.layers.indexOfFirst { it.id == sel.id }
            val partner = doc.layers.getOrNull(idx - 1) ?: doc.layers.getOrNull(idx + 1)
            if (partner == null) {
                _editorState.update { it.copy(statusMessage = "Add at least 2 layers to Group") }
                return
            }
            val newGid = "grp_${UUID.randomUUID().toString().take(6)}"
            val next = doc.layers.map { l ->
                if (l.id == sel.id || l.id == partner.id) l.withExtras(groupId = newGid) else l
            }
            pushHistoryAndUpdate(doc.copy(layers = next))
            _editorState.update { it.copy(statusMessage = "Grouped ${sel.name} + ${partner.name}") }
        }
    }

    fun reorderLayer(fromActualIndex: Int, toActualIndex: Int) {
        val doc = _editorState.value.document
        if (fromActualIndex !in doc.layers.indices || toActualIndex !in doc.layers.indices || fromActualIndex == toActualIndex) return
        val mutable = doc.layers.toMutableList()
        val item = mutable.removeAt(fromActualIndex)
        mutable.add(toActualIndex, item)
        pushHistoryAndUpdate(doc.copy(layers = mutable))
    }

    fun sampleColorAtArtboard(artboardX: Float, artboardY: Float) {
        val context = getApplication<Application>()
        val doc = _editorState.value.document
        viewModelScope.launch {
            val bmp = CanvasRenderer.renderThumbnail(context, doc, maxDimension = 540)
            val sx = ((artboardX / doc.artboardWidth.toFloat()) * bmp.width).toInt().coerceIn(0, bmp.width - 1)
            val sy = ((artboardY / doc.artboardHeight.toFloat()) * bmp.height).toInt().coerceIn(0, bmp.height - 1)
            val pixel = bmp.getPixel(sx, sy)
            bmp.recycle()
            val colorLong = pixel.toLong() and 0xFFFFFFFFL
            _editorState.update {
                it.copy(
                    activeColor = colorLong,
                    activeGradientEnd = null,
                    activeTool = ToolMode.SELECT,
                    statusMessage = "Color picked: #${Integer.toHexString(pixel).uppercase().takeLast(6)}"
                )
            }
            prefsRepo.addRecentColor(colorLong)
        }
    }

    private fun snap(value: Float, gridSize: Float = 40f): Float {
        if (!_editorState.value.snapToGrid) return value
        return (value / gridSize).roundToInt() * gridSize
    }
}
