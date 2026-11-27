package com.example.ui.editor

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Canvas as AndroidCanvas
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.engine.CanvasRenderer
import com.example.engine.ExportManager
import com.example.model.*
import com.example.ui.components.CanvasSizeSheet
import com.example.ui.components.ExportSheet
import com.example.ui.components.StickerSheet
import com.example.ui.theme.*
import com.example.viewmodel.EditorUiState
import com.example.viewmodel.StudioViewModel
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@Composable
fun EditorScreen(
    viewModel: StudioViewModel,
    state: EditorUiState,
    defaultResolution: ExportResolution,
    recentColors: List<Long>,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    BackHandler {
        viewModel.closeEditor()
        onBack()
    }

    // Modal sheets
    var showRenameDialog by remember { mutableStateOf(false) }
    var showCanvasSizeSheet by remember { mutableStateOf(false) }
    var showExportSheet by remember { mutableStateOf(false) }
    var showStickerSheet by remember { mutableStateOf(false) }
    var showColorSheet by remember { mutableStateOf(false) }
    var showTextSheet by remember { mutableStateOf(false) }
    var editingTextLayer by remember { mutableStateOf<TextLayer?>(null) }
    var showImageSheet by remember { mutableStateOf(false) }

    // Camera & Photo Picker launchers
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.importImageFromUri(uri)
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && pendingCameraUri != null) {
            viewModel.importImageFromUri(pendingCameraUri!!)
        }
    }

    fun launchCameraCapture() {
        val camDir = File(context.cacheDir, "camera").apply { mkdirs() }
        val file = File(camDir, "capture_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        pendingCameraUri = uri
        cameraLauncher.launch(uri)
    }

    val cameraPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) launchCameraCapture()
        else scope.launch { snackbarHostState.showSnackbar("Camera permission denied") }
    }

    LaunchedEffect(state.statusMessage) {
        val msg = state.statusMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearStatusMessage()
    }

    val selectedLayer = remember(state.document.layers, state.selectedLayerId) {
        state.document.layers.find { it.id == state.selectedLayerId }
    }

    // Canvas Viewport Transform
    var viewportSize by remember { mutableStateOf(IntSize.Zero) }
    var zoomScale by remember { mutableFloatStateOf(1f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    // Compute base fit scale for artboard inside viewport
    val baseFitScale = remember(viewportSize, state.document.artboardWidth, state.document.artboardHeight) {
        if (viewportSize.width == 0 || viewportSize.height == 0) 0.3f
        else {
            val availW = (viewportSize.width - 72f).coerceAtLeast(100f)
            val availH = (viewportSize.height - 72f).coerceAtLeast(100f)
            min(
                availW / state.document.artboardWidth.toFloat(),
                availH / state.document.artboardHeight.toFloat()
            )
        }
    }
    val effectiveScale = baseFitScale * zoomScale
    val artboardLeft = (viewportSize.width - state.document.artboardWidth * effectiveScale) / 2f + panOffset.x
    val artboardTop = (viewportSize.height - state.document.artboardHeight * effectiveScale) / 2f + panOffset.y

    // Active live drawing states
    var activeStrokePoints by remember { mutableStateOf<List<PathPoint>>(emptyList()) }
    var activeShapeStart by remember { mutableStateOf<PathPoint?>(null) }
    var activeShapeEnd by remember { mutableStateOf<PathPoint?>(null) }
    var draggingMode by remember { mutableStateOf("NONE") } // NONE, MOVE, RESIZE

    // Rename Dialog
    if (showRenameDialog) {
        var draft by remember { mutableStateOf(state.document.name) }
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Design") },
            text = {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    singleLine = true,
                    label = { Text("Project Title") }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.renameDocument(draft)
                        showRenameDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showCanvasSizeSheet) {
        CanvasSizeSheet(
            title = "Resize Artboard • ক্যানভাস সাইজ",
            initialName = state.document.name,
            initialWidth = state.document.artboardWidth,
            initialHeight = state.document.artboardHeight,
            showNameAndBg = false,
            onDismiss = { showCanvasSizeSheet = false },
            onConfirm = { _, w, h, _, _ ->
                viewModel.updateArtboardSize(w, h)
                zoomScale = 1f
                panOffset = Offset.Zero
                showCanvasSizeSheet = false
            }
        )
    }

    if (showExportSheet) {
        ExportSheet(
            document = state.document,
            defaultResolution = defaultResolution,
            onDismiss = { showExportSheet = false }
        )
    }

    if (showStickerSheet) {
        StickerSheet(
            onDismiss = { showStickerSheet = false },
            onSelectSticker = { sticker ->
                viewModel.addStickerLayer(sticker)
                viewModel.setTool(ToolMode.SELECT)
                showStickerSheet = false
            }
        )
    }

    if (showColorSheet) {
        ColorPickerSheet(
            initialColor = state.activeColor,
            initialGradientEnd = state.activeGradientEnd,
            recentColors = recentColors,
            hasSelectedLayer = selectedLayer != null,
            showStrokeOption = selectedLayer is ShapeLayer || state.activeTool == ToolMode.SHAPES,
            onDismiss = { showColorSheet = false },
            onApplyToSelectionOrTool = { c, g -> viewModel.setActiveColor(c, g) },
            onApplyToStroke = { c -> viewModel.setShapeStrokeColor(c) },
            onApplyToArtboardBg = { c, g -> viewModel.setArtboardBackground(c, g) },
            onActivateEyedropper = {
                showColorSheet = false
                viewModel.setTool(ToolMode.EYEDROPPER)
            }
        )
    }

    if (showTextSheet) {
        TextEditorSheet(
            existingLayer = editingTextLayer,
            defaultColor = state.activeColor,
            onDismiss = {
                showTextSheet = false
                editingTextLayer = null
            },
            onConfirmNewText = { layer ->
                viewModel.addTextLayer(
                    text = layer.text,
                    fontId = layer.fontId,
                    fontSize = layer.fontSize,
                    color = layer.color,
                    bold = layer.bold,
                    italic = layer.italic,
                    align = layer.align,
                    shadowEnabled = layer.shadowEnabled,
                    strokeWidth = layer.strokeWidth,
                    strokeColor = layer.strokeColor
                )
                viewModel.setTool(ToolMode.SELECT)
            },
            onUpdateExistingText = { updated ->
                viewModel.replaceLayerWithHistory(updated)
            }
        )
    }

    if (showImageSheet) {
        ImageToolsSheet(
            selectedImageLayer = selectedLayer as? ImageLayer,
            isProcessing = state.isProcessingImage,
            onDismiss = { showImageSheet = false },
            onPickFromGallery = {
                galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            onCaptureFromCamera = {
                val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
                if (granted) launchCameraCapture()
                else cameraPermLauncher.launch(Manifest.permission.CAMERA)
            },
            onUpdateImageLayer = { updated -> viewModel.replaceLayerWithHistory(updated) },
            onRemoveBackground = { tol, global -> viewModel.removeBackgroundFromSelectedImage(tol, global) }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            viewModel.closeEditor()
                            onBack()
                        },
                        modifier = Modifier.testTag("editor_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showRenameDialog = true }
                            .padding(horizontal = 4.dp)
                    ) {
                        Text(
                            text = state.document.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = state.document.smartModeLabel ?: "${state.document.artboardWidth}×${state.document.artboardHeight}",
                                fontSize = 10.sp,
                                color = OrangePrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.clickable { showCanvasSizeSheet = true }
                            )
                            Text(
                                text = if (state.isSaving) "• Saving..." else "• Auto-saved",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = { viewModel.saveCurrentProject(silent = false) }) {
                        Icon(Icons.Default.Save, contentDescription = "Save Offline", modifier = Modifier.size(20.dp))
                    }

                    IconButton(onClick = {
                        scope.launch {
                            val res = ExportManager.prepareShareUri(context, state.document, ExportFormat.PNG, defaultResolution)
                            res.shareUri?.let { uri ->
                                ExportManager.launchShareIntent(context, uri, ExportFormat.PNG.mimeType)
                            }
                        }
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(20.dp))
                    }

                    Button(
                        onClick = { showExportSheet = true },
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("editor_export_btn")
                    ) {
                        Text("Export", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                // Studio Sub-Bar: Canvas Size, Grid, Ruler, Snap, Transparent BG preview
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SmallToggleChip("Size", Icons.Default.AspectRatio, false) { showCanvasSizeSheet = true }
                    SmallToggleChip("Grid", Icons.Default.GridOn, state.showGrid) { viewModel.toggleGrid() }
                    SmallToggleChip("Ruler", Icons.Default.Straighten, state.showRuler) { viewModel.toggleRuler() }
                    SmallToggleChip("Snap", Icons.Default.GridGoldenratio, state.snapToGrid) { viewModel.toggleSnap() }
                    SmallToggleChip("Alpha BG", Icons.Default.Texture, state.transparentPreview) { viewModel.toggleTransparentPreview() }
                }
            }
        },
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                EditorContextBar(
                    state = state,
                    selectedLayer = selectedLayer,
                    zoomScale = zoomScale,
                    onSelectShape = { viewModel.setShapeType(it) },
                    onAddQuickShape = { viewModel.addShapeLayer(it) },
                    onUpdateShape = { viewModel.replaceLayerWithHistory(it) },
                    onBrushChange = { s, o, st -> viewModel.setBrushSettings(s, o, st) },
                    onPenStrokeChange = { viewModel.setPenStrokeWidth(it) },
                    onEraserSizeChange = { viewModel.setEraserSize(it) },
                    onAlign = { viewModel.alignSelected(it) },
                    onZoomIn = { zoomScale = (zoomScale * 1.25f).coerceAtMost(5f) },
                    onZoomOut = { zoomScale = (zoomScale / 1.25f).coerceAtLeast(0.35f) },
                    onZoomReset = { zoomScale = 1f; panOffset = Offset.Zero },
                    onEditSelectedText = {
                        editingTextLayer = selectedLayer as? TextLayer
                        showTextSheet = true
                    },
                    onEditSelectedImage = { showImageSheet = true },
                    onDuplicateSelected = { viewModel.duplicateSelected() },
                    onCopySelected = { viewModel.copySelected() },
                    onPasteClipboard = { viewModel.pasteClipboard() },
                    onDeleteSelected = { selectedLayer?.let { viewModel.deleteLayer(it.id) } },
                    onUpdateOpacity = { viewModel.updateSelectedOpacity(it) },
                    onRotateSelected = { viewModel.rotateSelectedBy(it) },
                    onNudgeSelected = { dx, dy -> viewModel.nudgeSelected(dx, dy) },
                    onOpenColorPicker = { showColorSheet = true }
                )

                // Big Touch-Friendly Bottom Toolbar
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ToolButton("Select", Icons.Default.NearMe, state.activeTool == ToolMode.SELECT) {
                            viewModel.setTool(ToolMode.SELECT)
                        }
                        ToolButton("Pen", Icons.Default.Edit, state.activeTool == ToolMode.PEN) {
                            viewModel.setTool(ToolMode.PEN)
                        }
                        ToolButton("Brush", Icons.Default.Brush, state.activeTool == ToolMode.BRUSH) {
                            viewModel.setTool(ToolMode.BRUSH)
                        }
                        ToolButton("Shapes", Icons.Default.Category, state.activeTool == ToolMode.SHAPES) {
                            viewModel.setTool(ToolMode.SHAPES)
                        }
                        ToolButton("Text", Icons.Default.Title, false) {
                            editingTextLayer = null
                            showTextSheet = true
                        }
                        ToolButton("Image", Icons.Default.Image, false) {
                            showImageSheet = true
                        }
                        ToolButton("Stickers", Icons.Default.AutoAwesome, false) {
                            showStickerSheet = true
                        }
                        ToolButton("Color", Icons.Default.Palette, false) {
                            showColorSheet = true
                        }
                        ToolButton("Eraser", Icons.Default.AutoFixNormal, state.activeTool == ToolMode.ERASER) {
                            viewModel.setTool(ToolMode.ERASER)
                        }
                        ToolButton("Align", Icons.Default.FormatAlignCenter, state.activeTool == ToolMode.ALIGN) {
                            viewModel.setTool(ToolMode.ALIGN)
                        }
                        ToolButton("Layers", Icons.Default.Layers, state.layersPanelOpen) {
                            viewModel.setLayersPanelOpen(!state.layersPanelOpen)
                        }
                        ToolButton("Undo", Icons.AutoMirrored.Filled.Undo, false, enabled = state.canUndo) {
                            viewModel.undo()
                        }
                        ToolButton("Redo", Icons.AutoMirrored.Filled.Redo, false, enabled = state.canRedo) {
                            viewModel.redo()
                        }
                        ToolButton("Zoom", Icons.Default.ZoomIn, state.activeTool == ToolMode.ZOOM) {
                            viewModel.setTool(ToolMode.ZOOM)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(CharcoalCanvasWorkspace)
                .clipToBounds()
                .onSizeChanged { viewportSize = it }
        ) {
            // Interactive Canvas Surface
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            do {
                                val event = awaitPointerEvent()
                                val pressedCount = event.changes.count { it.pressed }
                                if (pressedCount >= 2) {
                                    val zoom = event.calculateZoom()
                                    val pan = event.calculatePan()
                                    if (zoom != 1f || pan != Offset.Zero) {
                                        zoomScale = (zoomScale * zoom).coerceIn(0.35f, 5f)
                                        panOffset += pan
                                        event.changes.forEach { it.consume() }
                                    }
                                }
                            } while (event.changes.any { it.pressed })
                        }
                    }
                    .pointerInput(state.activeTool, effectiveScale, artboardLeft, artboardTop) {
                        if (state.activeTool == ToolMode.ZOOM) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                zoomScale = (zoomScale * zoom).coerceIn(0.35f, 5f)
                                panOffset += pan
                            }
                        } else {
                            detectTapGestures { offset ->
                                val ax = (offset.x - artboardLeft) / effectiveScale
                                val ay = (offset.y - artboardTop) / effectiveScale
                                when (state.activeTool) {
                                    ToolMode.SELECT -> viewModel.hitTestAndSelect(ax, ay)
                                    ToolMode.ERASER -> viewModel.eraseAtArtboardPoint(ax, ay)
                                    ToolMode.EYEDROPPER -> viewModel.sampleColorAtArtboard(ax, ay)
                                    ToolMode.SHAPES -> viewModel.addShapeLayer(
                                        state.activeShapeType,
                                        x = ax - 150f,
                                        y = ay - 150f,
                                        width = 300f,
                                        height = if (state.activeShapeType == ShapeType.LINE) 40f else 300f
                                    )
                                    else -> {}
                                }
                            }
                        }
                    }
                    .pointerInput(
                        state.activeTool,
                        state.selectedLayerId,
                        effectiveScale,
                        artboardLeft,
                        artboardTop
                    ) {
                        if (state.activeTool != ToolMode.ZOOM) {
                            detectDragGestures(
                                onDragStart = { start ->
                                    val ax = (start.x - artboardLeft) / effectiveScale
                                    val ay = (start.y - artboardTop) / effectiveScale
                                    when (state.activeTool) {
                                        ToolMode.PEN, ToolMode.BRUSH -> {
                                            activeStrokePoints = listOf(PathPoint(ax, ay))
                                        }
                                        ToolMode.SHAPES -> {
                                            activeShapeStart = PathPoint(ax, ay)
                                            activeShapeEnd = PathPoint(ax, ay)
                                        }
                                        ToolMode.SELECT -> {
                                            val sel = viewModel.selectedLayer()
                                            if (sel != null && !sel.locked) {
                                                // Check bottom-right resize handle
                                                val handleX = sel.x + sel.width
                                                val handleY = sel.y + sel.height
                                                val hitHandle = abs(ax - handleX) < 54f / effectiveScale &&
                                                    abs(ay - handleY) < 54f / effectiveScale
                                                if (hitHandle) {
                                                    draggingMode = "RESIZE"
                                                    viewModel.beginTransformGesture()
                                                } else {
                                                    val hit = viewModel.hitTestAndSelect(ax, ay)
                                                    if (hit != null && !hit.locked) {
                                                        draggingMode = "MOVE"
                                                        viewModel.beginTransformGesture()
                                                    } else {
                                                        draggingMode = "NONE"
                                                    }
                                                }
                                            } else {
                                                val hit = viewModel.hitTestAndSelect(ax, ay)
                                                if (hit != null && !hit.locked) {
                                                    draggingMode = "MOVE"
                                                    viewModel.beginTransformGesture()
                                                } else {
                                                    draggingMode = "NONE"
                                                }
                                            }
                                        }
                                        ToolMode.ERASER -> viewModel.eraseAtArtboardPoint(ax, ay)
                                        else -> {}
                                    }
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    val ax = (change.position.x - artboardLeft) / effectiveScale
                                    val ay = (change.position.y - artboardTop) / effectiveScale
                                    when (state.activeTool) {
                                        ToolMode.PEN, ToolMode.BRUSH -> {
                                            val last = activeStrokePoints.lastOrNull()
                                            val minStep = 3.5f / effectiveScale
                                            if (last == null || abs(ax - last.x) >= minStep || abs(ay - last.y) >= minStep) {
                                                activeStrokePoints = activeStrokePoints + PathPoint(ax, ay)
                                            }
                                        }
                                        ToolMode.SHAPES -> {
                                            activeShapeEnd = PathPoint(ax, ay)
                                        }
                                        ToolMode.SELECT -> {
                                            val dx = dragAmount.x / effectiveScale
                                            val dy = dragAmount.y / effectiveScale
                                            if (draggingMode == "MOVE") {
                                                viewModel.updateSelectedTransformLive(dx = dx, dy = dy)
                                            } else if (draggingMode == "RESIZE") {
                                                val sel = viewModel.selectedLayer()
                                                if (sel != null) {
                                                    viewModel.updateSelectedTransformLive(
                                                        newWidth = sel.width + dx,
                                                        newHeight = sel.height + dy
                                                    )
                                                }
                                            }
                                        }
                                        ToolMode.ERASER -> viewModel.eraseAtArtboardPoint(ax, ay)
                                        else -> {}
                                    }
                                },
                                onDragEnd = {
                                    when (state.activeTool) {
                                        ToolMode.PEN -> {
                                            viewModel.addPathLayer(activeStrokePoints, isPenTool = true)
                                            activeStrokePoints = emptyList()
                                        }
                                        ToolMode.BRUSH -> {
                                            viewModel.addPathLayer(activeStrokePoints, isPenTool = false)
                                            activeStrokePoints = emptyList()
                                        }
                                        ToolMode.SHAPES -> {
                                            val s = activeShapeStart
                                            val e = activeShapeEnd
                                            if (s != null && e != null) {
                                                val rx = min(s.x, e.x)
                                                val ry = min(s.y, e.y)
                                                val rw = max(48f, abs(e.x - s.x))
                                                val rh = if (state.activeShapeType == ShapeType.LINE) 40f else max(48f, abs(e.y - s.y))
                                                viewModel.addShapeLayer(state.activeShapeType, rx, ry, rw, rh)
                                            }
                                            activeShapeStart = null
                                            activeShapeEnd = null
                                        }
                                        ToolMode.SELECT -> {
                                            viewModel.commitTransformGesture()
                                            draggingMode = "NONE"
                                        }
                                        else -> {}
                                    }
                                },
                                onDragCancel = {
                                    activeStrokePoints = emptyList()
                                    activeShapeStart = null
                                    activeShapeEnd = null
                                    draggingMode = "NONE"
                                }
                            )
                        }
                    }
            ) {
                val doc = state.document
                val artW = doc.artboardWidth * effectiveScale
                val artH = doc.artboardHeight * effectiveScale

                // Checkerboard behind artboard when transparent preview is active
                if (state.transparentPreview) {
                    val tile = 20f
                    var y = artboardTop
                    var row = 0
                    while (y < artboardTop + artH) {
                        var x = artboardLeft
                        var col = 0
                        while (x < artboardLeft + artW) {
                            val cw = min(tile, artboardLeft + artW - x)
                            val ch = min(tile, artboardTop + artH - y)
                            drawRect(
                                color = if ((row + col) % 2 == 0) Color(0xFFCCCCCC) else Color(0xFF999999),
                                topLeft = Offset(x, y),
                                size = Size(cw, ch)
                            )
                            x += tile
                            col++
                        }
                        y += tile
                        row++
                    }
                }

                // Render Artboard via shared CanvasRenderer
                drawIntoCanvas { canvas ->
                    val nc = canvas.nativeCanvas as AndroidCanvas
                    nc.save()
                    nc.translate(artboardLeft, artboardTop)
                    CanvasRenderer.renderDocument(
                        context = context,
                        canvas = nc,
                        document = doc,
                        scaleX = effectiveScale,
                        scaleY = effectiveScale,
                        transparentBackground = state.transparentPreview
                    )
                    nc.restore()
                }

                // Artboard Border
                drawRect(
                    color = Color.White.copy(alpha = 0.25f),
                    topLeft = Offset(artboardLeft, artboardTop),
                    size = Size(artW, artH),
                    style = Stroke(width = 2f)
                )

                // Grid Overlay
                if (state.showGrid) {
                    val step = 80f * effectiveScale
                    if (step >= 8f) {
                        var gx = artboardLeft + step
                        while (gx < artboardLeft + artW) {
                            drawLine(
                                color = Color.Cyan.copy(alpha = 0.28f),
                                start = Offset(gx, artboardTop),
                                end = Offset(gx, artboardTop + artH),
                                strokeWidth = 1f
                            )
                            gx += step
                        }
                        var gy = artboardTop + step
                        while (gy < artboardTop + artH) {
                            drawLine(
                                color = Color.Cyan.copy(alpha = 0.28f),
                                start = Offset(artboardLeft, gy),
                                end = Offset(artboardLeft + artW, gy),
                                strokeWidth = 1f
                            )
                            gy += step
                        }
                    }
                }

                // Live Pen / Brush preview path
                if (activeStrokePoints.size >= 2) {
                    val previewPath = Path().apply {
                        moveTo(
                            artboardLeft + activeStrokePoints[0].x * effectiveScale,
                            artboardTop + activeStrokePoints[0].y * effectiveScale
                        )
                        for (i in 1 until activeStrokePoints.size) {
                            lineTo(
                                artboardLeft + activeStrokePoints[i].x * effectiveScale,
                                artboardTop + activeStrokePoints[i].y * effectiveScale
                            )
                        }
                    }
                    val strokeW = (if (state.activeTool == ToolMode.PEN) state.penStrokeWidth else state.brushSize) * effectiveScale
                    drawPath(
                        path = previewPath,
                        color = Color(state.activeColor.toInt()),
                        style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }

                // Live Shape drag preview
                if (activeShapeStart != null && activeShapeEnd != null) {
                    val sx = artboardLeft + min(activeShapeStart!!.x, activeShapeEnd!!.x) * effectiveScale
                    val sy = artboardTop + min(activeShapeStart!!.y, activeShapeEnd!!.y) * effectiveScale
                    val sw = abs(activeShapeEnd!!.x - activeShapeStart!!.x) * effectiveScale
                    val sh = abs(activeShapeEnd!!.y - activeShapeStart!!.y) * effectiveScale
                    drawRect(
                        color = OrangePrimary,
                        topLeft = Offset(sx, sy),
                        size = Size(sw, sh),
                        style = Stroke(width = 3f)
                    )
                }

                // Selection Bounding Box & Corner Resize Handle
                if (selectedLayer != null && selectedLayer.visible) {
                    val bx = artboardLeft + selectedLayer.x * effectiveScale
                    val by = artboardTop + selectedLayer.y * effectiveScale
                    val bw = selectedLayer.width * effectiveScale
                    val bh = selectedLayer.height * effectiveScale
                    drawRect(
                        color = OrangePrimary,
                        topLeft = Offset(bx, by),
                        size = Size(bw, bh),
                        style = Stroke(width = 3f)
                    )
                    // Corner handles
                    val handleRadius = 11f
                    listOf(
                        Offset(bx, by),
                        Offset(bx + bw, by),
                        Offset(bx, by + bh),
                        Offset(bx + bw, by + bh)
                    ).forEachIndexed { idx, pt ->
                        drawCircle(
                            color = if (idx == 3) OrangePrimary else Color.White,
                            radius = handleRadius,
                            center = pt
                        )
                        drawCircle(
                            color = if (idx == 3) Color.White else OrangePrimary,
                            radius = handleRadius,
                            center = pt,
                            style = Stroke(width = 3f)
                        )
                    }
                }

                // Top & Left Pixel Rulers
                if (state.showRuler) {
                    val rulerBg = Color(0xDD252527)
                    drawRect(color = rulerBg, topLeft = Offset.Zero, size = Size(size.width, 24f))
                    drawRect(color = rulerBg, topLeft = Offset.Zero, size = Size(24f, size.height))
                    val tickStep = 200f
                    var v = 0f
                    while (v <= doc.artboardWidth) {
                        val rx = artboardLeft + v * effectiveScale
                        if (rx in 24f..size.width) {
                            drawLine(Color.LightGray, Offset(rx, 10f), Offset(rx, 24f), strokeWidth = 1.5f)
                        }
                        v += tickStep
                    }
                    var vy = 0f
                    while (vy <= doc.artboardHeight) {
                        val ry = artboardTop + vy * effectiveScale
                        if (ry in 24f..size.height) {
                            drawLine(Color.LightGray, Offset(10f, ry), Offset(24f, ry), strokeWidth = 1.5f)
                        }
                        vy += tickStep
                    }
                }
            }

            // Right-Side Slide-Out Layers Panel
            AnimatedVisibility(
                visible = state.layersPanelOpen,
                enter = slideInHorizontally(initialOffsetX = { it }),
                exit = slideOutHorizontally(targetOffsetX = { it }),
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                LayersPanel(
                    layers = state.document.layers,
                    selectedLayerId = state.selectedLayerId,
                    onSelectLayer = { viewModel.selectLayer(it) },
                    onToggleVisibility = { viewModel.toggleLayerVisibility(it) },
                    onToggleLock = { viewModel.toggleLayerLock(it) },
                    onBringForward = { viewModel.bringForward(it) },
                    onSendBackward = { viewModel.sendBackward(it) },
                    onRenameLayer = { id, name -> viewModel.renameLayer(id, name) },
                    onDeleteLayer = { viewModel.deleteLayer(it) },
                    onClose = { viewModel.setLayersPanelOpen(false) }
                )
            }
        }
    }
}

@Composable
private fun SmallToggleChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    active: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (active) OrangePrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .border(
                1.dp,
                if (active) OrangePrimary else MaterialTheme.colorScheme.outline,
                RoundedCornerShape(8.dp)
            )
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = if (active) OrangePrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(13.dp)
            )
            Text(
                label,
                fontSize = 11.sp,
                color = if (active) OrangePrimary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ToolButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val tint = when {
        !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
        selected -> OrangePrimary
        else -> MaterialTheme.colorScheme.onSurface
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .widthIn(min = 54.dp)
            .heightIn(min = 50.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) OrangePrimary.copy(alpha = 0.16f) else Color.Transparent)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag("tool_btn_${label.lowercase()}")
    ) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = tint,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1
        )
    }
}
