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
import androidx.compose.ui.unit.DpOffset
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
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

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

    // Modal sheets & menus
    var showRenameDialog by remember { mutableStateOf(false) }
    var showCanvasSizeSheet by remember { mutableStateOf(false) }
    var showCanvasPagesSheet by remember { mutableStateOf(false) }
    var showEffectsSheet by remember { mutableStateOf(false) }
    var showExportSheet by remember { mutableStateOf(false) }
    var showStickerSheet by remember { mutableStateOf(false) }
    var showColorSheet by remember { mutableStateOf(false) }
    var showTextSheet by remember { mutableStateOf(false) }
    var editingTextLayer by remember { mutableStateOf<TextLayer?>(null) }
    var showImageSheet by remember { mutableStateOf(false) }
    var showPhotoCutoutDialog by remember { mutableStateOf(false) }
    var pickedBgColor by remember(state.selectedLayerId) { mutableStateOf<Int?>(null) }
    var showLongPressMenu by remember { mutableStateOf(false) }
    var quickExportMenuOpen by remember { mutableStateOf(false) }

    // Camera, File Manager & Photo Picker launchers
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val fileManagerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri -> if (uri != null) viewModel.importImageFromUri(uri) }

    val bgImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri -> if (uri != null) viewModel.setArtboardBackgroundImage(uri) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) viewModel.importImageFromUri(uri) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && pendingCameraUri != null) viewModel.importImageFromUri(pendingCameraUri!!)
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

    // Active live drawing / point editing states
    var activeStrokePoints by remember { mutableStateOf<List<PathPoint>>(emptyList()) }
    var penAnchorPoints by remember { mutableStateOf<List<PathPoint>>(emptyList()) }
    var activeShapeStart by remember { mutableStateOf<PathPoint?>(null) }
    var activeShapeEnd by remember { mutableStateOf<PathPoint?>(null) }
    var draggingMode by remember { mutableStateOf("NONE") } // NONE, MOVE, RESIZE, ROTATE, ANCHOR
    var draggingAnchorIndex by remember { mutableIntStateOf(-1) }

    LaunchedEffect(state.activeTool) {
        if (state.activeTool != ToolMode.PEN && state.activeTool != ToolMode.CURVATURE && penAnchorPoints.size >= 2) {
            viewModel.addPathLayer(penAnchorPoints, isPenTool = true, closed = false)
        }
        if (state.activeTool != ToolMode.PEN && state.activeTool != ToolMode.CURVATURE) {
            penAnchorPoints = emptyList()
        }
    }

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

    if (showCanvasPagesSheet) {
        CanvasPagesSheet(
            document = state.document,
            onDismiss = { showCanvasPagesSheet = false },
            onOpenColorPicker = { showColorSheet = true },
            onPickBackgroundImage = {
                showCanvasPagesSheet = false
                bgImagePickerLauncher.launch(arrayOf("image/*"))
            },
            onClearBackgroundImage = { viewModel.setArtboardBackgroundImage(null) },
            onOpenCanvasSize = { showCanvasSizeSheet = true },
            onSwitchPage = { idx ->
                viewModel.switchPage(idx)
                zoomScale = 1f
                panOffset = Offset.Zero
            },
            onAddPage = { viewModel.addPage() },
            onDuplicatePage = { viewModel.duplicateCurrentPage() },
            onDeletePage = { viewModel.deleteCurrentPage() }
        )
    }

    if (showEffectsSheet) {
        EffectsSheet(
            selectedLayer = selectedLayer,
            onDismiss = { showEffectsSheet = false },
            onApplyEffects = { fx -> viewModel.updateSelectedEffects(fx) },
            onFlipHorizontal = { viewModel.flipSelectedHorizontal() },
            onFlipVertical = { viewModel.flipSelectedVertical() },
            onToggleGroup = { viewModel.toggleGroupSelected() },
            onUpdateStrokeOptions = { c, w, s -> viewModel.setShapeStrokeOptions(c, w, s) }
        )
    }

    if (showExportSheet) {
        ExportSheet(
            document = state.document,
            defaultResolution = defaultResolution,
            defaultTransparentBg = state.transparentPreview,
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
            initialGradientMid = state.activeGradientMid,
            initialGradientEnd = state.activeGradientEnd,
            initialGradientType = state.activeGradientType,
            initialGradientAngle = state.activeGradientAngle,
            initialStrokeWidth = state.activeStrokeWidth,
            initialStrokeStyle = state.activeStrokeStyle,
            recentColors = recentColors,
            hasSelectedLayer = selectedLayer != null,
            showStrokeOption = selectedLayer is ShapeLayer || selectedLayer is PathLayer || state.activeTool == ToolMode.SHAPES,
            onDismiss = { showColorSheet = false },
            onApplyToSelectionOrTool = { c, end, mid, gType, angle ->
                viewModel.setActiveColor(c, end, mid, gType, angle)
            },
            onApplyToStroke = { c, w, st -> viewModel.setShapeStrokeOptions(c, w, st) },
            onApplyToArtboardBg = { c, end, mid, gType ->
                viewModel.setArtboardBackground(c, end, mid, gType)
            },
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
                    underline = layer.underline,
                    lineSpacing = layer.lineSpacing,
                    letterSpacing = layer.letterSpacing,
                    textBackgroundColor = layer.textBackgroundColor,
                    curveAmount = layer.curveAmount,
                    align = layer.align,
                    shadowEnabled = layer.shadowEnabled,
                    strokeWidth = layer.strokeWidth,
                    strokeColor = layer.strokeColor
                )
                viewModel.setTool(ToolMode.SELECT)
            },
            onUpdateExistingText = { updated ->
                CanvasRenderer.clearTextCache()
                viewModel.replaceLayerWithHistory(updated)
            }
        )
    }

    if (showImageSheet) {
        ImageToolsSheet(
            selectedImageLayer = selectedLayer as? ImageLayer,
            isProcessing = state.isProcessingImage,
            pickedBgColor = pickedBgColor,
            onClearPickedBgColor = { pickedBgColor = null },
            onDismiss = { showImageSheet = false },
            onPickFromFileManager = { fileManagerLauncher.launch(arrayOf("image/*")) },
            onPickFromGallery = { galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
            onCaptureFromCamera = {
                val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
                if (granted) launchCameraCapture()
                else cameraPermLauncher.launch(Manifest.permission.CAMERA)
            },
            onOpenInteractiveCrop = {
                showImageSheet = false
                showPhotoCutoutDialog = true
            },
            onUpdateImageLayer = { updated -> viewModel.replaceLayerWithHistory(updated) },
            onRemoveBackground = { tol, global, color, smooth ->
                viewModel.removeBackgroundFromSelectedImage(tol, global, color, smooth)
            },
            onSuperResolveImage = { targetEdge, profile ->
                viewModel.superResolveSelectedImage(targetEdge, profile)
            },
            onResetToOriginal = {
                pickedBgColor = null
                viewModel.resetSelectedImageToOriginal()
            },
            onQuickTransparentExport = {
                if (!state.transparentPreview) viewModel.toggleTransparentPreview()
                showImageSheet = false
                showExportSheet = true
            }
        )
    }

    if (showPhotoCutoutDialog && selectedLayer is ImageLayer) {
        PhotoCutoutDialog(
            imageLayer = selectedLayer,
            initialPickedColor = pickedBgColor,
            onPickedColorChange = { pickedBgColor = it },
            onDismiss = { showPhotoCutoutDialog = false },
            onApplyCropAndEnhance = { l, t, r, b, cornerRatio, upscaleHd, sharpenAmt, fitArtboard ->
                viewModel.applyCropAndEnhanceSelectedImage(
                    cropLeft = l,
                    cropTop = t,
                    cropRight = r,
                    cropBottom = b,
                    cornerRadiusRatio = cornerRatio,
                    upscaleHd = upscaleHd,
                    sharpenAmount = sharpenAmt,
                    fitArtboardToCutout = fitArtboard
                )
                if (fitArtboard) {
                    zoomScale = 1f
                    panOffset = Offset.Zero
                }
            }
        )
    }

    val pages = state.document.normalizedPages()
    val activePageNum = state.document.activePageIndex.coerceIn(0, pages.lastIndex) + 1

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .windowInsetsPadding(WindowInsets.statusBars.union(WindowInsets.displayCutout))
                    .padding(top = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
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
                            .padding(horizontal = 2.dp)
                    ) {
                        Text(
                            text = state.document.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = (state.document.smartModeLabel ?: "${state.document.artboardWidth}×${state.document.artboardHeight}") +
                                if (state.isSaving) " • Saving" else "",
                            fontSize = 10.sp,
                            color = OrangePrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.clickable { showCanvasSizeSheet = true }
                        )
                    }

                    // Undo / Redo right in Top Toolbar
                    IconButton(
                        onClick = { viewModel.undo() },
                        enabled = state.canUndo,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo", modifier = Modifier.size(19.dp))
                    }
                    IconButton(
                        onClick = { viewModel.redo() },
                        enabled = state.canRedo,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Redo", modifier = Modifier.size(19.dp))
                    }

                    IconButton(
                        onClick = { viewModel.saveCurrentProject(silent = false) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Save Offline", modifier = Modifier.size(19.dp))
                    }

                    Box {
                        Button(
                            onClick = { showExportSheet = true },
                            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("editor_export_btn")
                        ) {
                            Text("Export", fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1)
                        }
                    }
                    IconButton(
                        onClick = { quickExportMenuOpen = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Quick Export PNG/JPG/PDF", modifier = Modifier.size(18.dp))
                        DropdownMenu(
                            expanded = quickExportMenuOpen,
                            onDismissRequest = { quickExportMenuOpen = false }
                        ) {
                            listOf(
                                ExportFormat.PNG to "Quick Export PNG",
                                ExportFormat.JPG to "Quick Export JPG",
                                ExportFormat.PDF to "Quick Export PDF"
                            ).forEach { (fmt, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    leadingIcon = { Icon(Icons.Default.Download, contentDescription = null, tint = OrangePrimary) },
                                    onClick = {
                                        quickExportMenuOpen = false
                                        scope.launch {
                                            val res = ExportManager.exportToGalleryOrDownloads(
                                                context = context,
                                                document = state.document,
                                                format = fmt,
                                                resolution = defaultResolution,
                                                transparentBackground = fmt.supportsTransparency && state.transparentPreview
                                            )
                                            snackbarHostState.showSnackbar(res.message)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // Top Sub-Bar: Zoom In/Out, Grid, Ruler, Snap, Page Manager, Alpha BG
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SmallToggleChip("-", Icons.Default.ZoomOut, false) {
                        zoomScale = (zoomScale / 1.25f).coerceAtLeast(0.35f)
                    }
                    SmallToggleChip("${(zoomScale * 100).toInt()}%", Icons.Default.ZoomIn, zoomScale != 1f) {
                        if (zoomScale > 2.5f) {
                            zoomScale = 1f; panOffset = Offset.Zero
                        } else {
                            zoomScale = (zoomScale * 1.25f).coerceAtMost(10f)
                        }
                    }
                    SmallToggleChip("Grid", Icons.Default.GridOn, state.showGrid) { viewModel.toggleGrid() }
                    SmallToggleChip("Ruler", Icons.Default.Straighten, state.showRuler) { viewModel.toggleRuler() }
                    SmallToggleChip("Snap", Icons.Default.GridGoldenratio, state.snapToGrid) { viewModel.toggleSnap() }
                    SmallToggleChip("Page $activePageNum/${pages.size}", Icons.Default.Layers, false) {
                        showCanvasPagesSheet = true
                    }
                    SmallToggleChip("Alpha BG", Icons.Default.Texture, state.transparentPreview) { viewModel.toggleTransparentPreview() }
                }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                EditorContextBar(
                    state = state,
                    selectedLayer = selectedLayer,
                    zoomScale = zoomScale,
                    penPointCount = penAnchorPoints.size,
                    onSelectShape = { viewModel.setShapeType(it) },
                    onAddQuickShape = { viewModel.addShapeLayer(it) },
                    onUpdateShape = { viewModel.replaceLayerWithHistory(it) },
                    onUpdatePath = { viewModel.replaceLayerWithHistory(it) },
                    onFinishPenPath = { closed ->
                        if (penAnchorPoints.size >= 2) {
                            viewModel.addPathLayer(penAnchorPoints, isPenTool = true, closed = closed)
                        }
                        penAnchorPoints = emptyList()
                        viewModel.setTool(ToolMode.SELECT)
                    },
                    onClearPenPath = { penAnchorPoints = emptyList() },
                    onConvertShapeToPoints = { viewModel.convertSelectedShapeToEditablePath() },
                    onBrushChange = { s, o, st -> viewModel.setBrushSettings(s, o, st) },
                    onPenStrokeChange = { viewModel.setPenStrokeWidth(it) },
                    onEraserSizeChange = { viewModel.setEraserSize(it) },
                    onAlign = { viewModel.alignSelected(it) },
                    onZoomIn = { zoomScale = (zoomScale * 1.25f).coerceAtMost(10f) },
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
                    onFlipH = { viewModel.flipSelectedHorizontal() },
                    onFlipV = { viewModel.flipSelectedVertical() },
                    onToggleGroup = { viewModel.toggleGroupSelected() },
                    onBringForward = { selectedLayer?.let { viewModel.bringForward(it.id) } },
                    onSendBackward = { selectedLayer?.let { viewModel.sendBackward(it.id) } },
                    onOpenEffectsSheet = { showEffectsSheet = true },
                    onUpdateOpacity = { viewModel.updateSelectedOpacity(it) },
                    onRotateSelected = { viewModel.rotateSelectedBy(it) },
                    onNudgeSelected = { dx, dy -> viewModel.nudgeSelected(dx, dy) },
                    onOpenColorPicker = { showColorSheet = true }
                )

                // Full Illustrator Bottom Bar
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
                        ToolButton("Text", Icons.Default.Title, selectedLayer is TextLayer) {
                            editingTextLayer = selectedLayer as? TextLayer
                            showTextSheet = true
                        }
                        ToolButton("Fonts", Icons.Default.FontDownload, false) {
                            editingTextLayer = selectedLayer as? TextLayer
                            showTextSheet = true
                        }
                        ToolButton("Color", Icons.Default.Palette, false) {
                            showColorSheet = true
                        }
                        ToolButton("Gradient", Icons.Default.Gradient, state.activeGradientEnd != null) {
                            showColorSheet = true
                        }
                        ToolButton("Stroke", Icons.Default.BorderColor, false) {
                            if (selectedLayer != null) showEffectsSheet = true else showColorSheet = true
                        }
                        ToolButton("Shadow", Icons.Default.AutoAwesome, false) {
                            showEffectsSheet = true
                        }
                        ToolButton("Opacity", Icons.Default.Opacity, false) {
                            if (selectedLayer != null) viewModel.setTool(ToolMode.SELECT)
                            else showEffectsSheet = true
                        }
                        ToolButton("Align", Icons.Default.FormatAlignCenter, state.activeTool == ToolMode.ALIGN) {
                            viewModel.setTool(ToolMode.ALIGN)
                        }
                        ToolButton("Layers", Icons.Default.Layers, state.layersPanelOpen) {
                            viewModel.setLayersPanelOpen(!state.layersPanelOpen)
                        }
                        ToolButton("Background", Icons.Default.Wallpaper, false) {
                            showCanvasPagesSheet = true
                        }
                        ToolButton("Elements", Icons.Default.Interests, false) {
                            showStickerSheet = true
                        }
                        ToolButton("Stickers", Icons.Default.EmojiEmotions, false) {
                            showStickerSheet = true
                        }
                        ToolButton("Shapes", Icons.Default.Category, state.activeTool == ToolMode.SHAPES) {
                            viewModel.setTool(ToolMode.SHAPES)
                        }
                        ToolButton("Image", Icons.Default.Image, false) {
                            showImageSheet = true
                        }
                        ToolButton("Brush", Icons.Default.Brush, state.activeTool == ToolMode.BRUSH) {
                            viewModel.setTool(ToolMode.BRUSH)
                        }
                        ToolButton("Eraser", Icons.Default.AutoFixNormal, state.activeTool == ToolMode.ERASER) {
                            viewModel.setTool(ToolMode.ERASER)
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
                        ToolButton("Grid", Icons.Default.GridOn, state.showGrid) {
                            viewModel.toggleGrid()
                        }
                        ToolButton("Ruler", Icons.Default.Straighten, state.showRuler) {
                            viewModel.toggleRuler()
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
                                        zoomScale = (zoomScale * zoom).coerceIn(0.35f, 10f)
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
                                zoomScale = (zoomScale * zoom).coerceIn(0.35f, 10f)
                                panOffset += pan
                            }
                        } else {
                            detectTapGestures(
                                onTap = { offset ->
                                    val ax = (offset.x - artboardLeft) / effectiveScale
                                    val ay = (offset.y - artboardTop) / effectiveScale
                                    when (state.activeTool) {
                                        ToolMode.SELECT, ToolMode.DIRECT_SELECT -> {
                                            viewModel.hitTestAndSelect(ax, ay)
                                        }
                                        ToolMode.PEN, ToolMode.CURVATURE -> {
                                            penAnchorPoints = penAnchorPoints + PathPoint(ax, ay)
                                        }
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
                                },
                                onLongPress = { offset ->
                                    val ax = (offset.x - artboardLeft) / effectiveScale
                                    val ay = (offset.y - artboardTop) / effectiveScale
                                    val hit = viewModel.hitTestAndSelect(ax, ay)
                                    if (hit != null) {
                                        showLongPressMenu = true
                                    }
                                }
                            )
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
                                        ToolMode.PEN, ToolMode.CURVATURE, ToolMode.BRUSH -> {
                                            activeStrokePoints = listOf(PathPoint(ax, ay))
                                        }
                                        ToolMode.SHAPES -> {
                                            activeShapeStart = PathPoint(ax, ay)
                                            activeShapeEnd = PathPoint(ax, ay)
                                        }
                                        ToolMode.DIRECT_SELECT -> {
                                            val sel = viewModel.selectedLayer()
                                            if (sel is PathLayer && !sel.locked) {
                                                val hitIdx = sel.points.indexOfFirst { pt ->
                                                    val px = sel.x + pt.x
                                                    val py = sel.y + pt.y
                                                    abs(ax - px) < 48f / effectiveScale && abs(ay - py) < 48f / effectiveScale
                                                }
                                                if (hitIdx >= 0) {
                                                    draggingMode = "ANCHOR"
                                                    draggingAnchorIndex = hitIdx
                                                    viewModel.beginTransformGesture()
                                                } else {
                                                    draggingMode = "NONE"
                                                }
                                            } else if (sel is ShapeLayer && !sel.locked) {
                                                val converted = viewModel.convertSelectedShapeToEditablePath()
                                                if (converted != null) {
                                                    draggingMode = "ANCHOR"
                                                    draggingAnchorIndex = 0
                                                    viewModel.beginTransformGesture()
                                                }
                                            } else {
                                                viewModel.hitTestAndSelect(ax, ay)
                                                draggingMode = "NONE"
                                            }
                                        }
                                        ToolMode.SELECT -> {
                                            val sel = viewModel.selectedLayer()
                                            if (sel != null && !sel.locked) {
                                                val rotHandleX = sel.x + sel.width / 2f
                                                val rotHandleY = sel.y - (52f / effectiveScale)
                                                val hitRotate = abs(ax - rotHandleX) < 50f / effectiveScale &&
                                                    abs(ay - rotHandleY) < 50f / effectiveScale
                                                val handleX = sel.x + sel.width
                                                val handleY = sel.y + sel.height
                                                val hitResize = abs(ax - handleX) < 54f / effectiveScale &&
                                                    abs(ay - handleY) < 54f / effectiveScale
                                                when {
                                                    hitRotate -> {
                                                        draggingMode = "ROTATE"
                                                        viewModel.beginTransformGesture()
                                                    }
                                                    hitResize -> {
                                                        draggingMode = "RESIZE"
                                                        viewModel.beginTransformGesture()
                                                    }
                                                    else -> {
                                                        val hit = viewModel.hitTestAndSelect(ax, ay)
                                                        if (hit != null && !hit.locked) {
                                                            draggingMode = "MOVE"
                                                            viewModel.beginTransformGesture()
                                                        } else {
                                                            draggingMode = "NONE"
                                                        }
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
                                        ToolMode.HAND -> {
                                            panOffset += dragAmount
                                        }
                                        ToolMode.PEN, ToolMode.CURVATURE, ToolMode.BRUSH -> {
                                            val last = activeStrokePoints.lastOrNull()
                                            val minStep = 3.5f / effectiveScale
                                            if (last == null || abs(ax - last.x) >= minStep || abs(ay - last.y) >= minStep) {
                                                activeStrokePoints = activeStrokePoints + PathPoint(ax, ay)
                                            }
                                        }
                                        ToolMode.SHAPES -> {
                                            activeShapeEnd = PathPoint(ax, ay)
                                        }
                                        ToolMode.DIRECT_SELECT -> {
                                            if (draggingMode == "ANCHOR" && draggingAnchorIndex >= 0) {
                                                val selId = state.selectedLayerId
                                                if (selId != null) {
                                                    viewModel.updatePathAnchorPointLive(selId, draggingAnchorIndex, ax, ay)
                                                }
                                            }
                                        }
                                        ToolMode.SELECT -> {
                                            val dx = dragAmount.x / effectiveScale
                                            val dy = dragAmount.y / effectiveScale
                                            val sel = viewModel.selectedLayer()
                                            when (draggingMode) {
                                                "MOVE" -> viewModel.updateSelectedTransformLive(dx = dx, dy = dy)
                                                "RESIZE" -> if (sel != null) {
                                                    viewModel.updateSelectedTransformLive(
                                                        newWidth = sel.width + dx,
                                                        newHeight = sel.height + dy
                                                    )
                                                }
                                                "ROTATE" -> if (sel != null) {
                                                    val cx = sel.x + sel.width / 2f
                                                    val cy = sel.y + sel.height / 2f
                                                    val deg = (atan2((ay - cy).toDouble(), (ax - cx).toDouble()) * 180.0 / PI).toFloat() + 90f
                                                    viewModel.updateSelectedTransformLive(newRotation = deg)
                                                }
                                            }
                                        }
                                        ToolMode.ERASER -> viewModel.eraseAtArtboardPoint(ax, ay)
                                        else -> {}
                                    }
                                },
                                onDragEnd = {
                                    when (state.activeTool) {
                                        ToolMode.PEN, ToolMode.CURVATURE -> {
                                            if (activeStrokePoints.size >= 2) {
                                                viewModel.addPathLayer(activeStrokePoints, isPenTool = true)
                                            }
                                            activeStrokePoints = emptyList()
                                        }
                                        ToolMode.BRUSH -> {
                                            if (activeStrokePoints.size >= 2) {
                                                viewModel.addPathLayer(activeStrokePoints, isPenTool = false)
                                            }
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
                                        ToolMode.SELECT, ToolMode.DIRECT_SELECT -> {
                                            viewModel.commitTransformGesture()
                                            draggingMode = "NONE"
                                            draggingAnchorIndex = -1
                                        }
                                        else -> {}
                                    }
                                },
                                onDragCancel = {
                                    activeStrokePoints = emptyList()
                                    activeShapeStart = null
                                    activeShapeEnd = null
                                    draggingMode = "NONE"
                                    draggingAnchorIndex = -1
                                }
                            )
                        }
                    }
            ) {
                val doc = state.document
                val artW = doc.artboardWidth * effectiveScale
                val artH = doc.artboardHeight * effectiveScale

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

                drawRect(
                    color = Color.White.copy(alpha = 0.25f),
                    topLeft = Offset(artboardLeft, artboardTop),
                    size = Size(artW, artH),
                    style = Stroke(width = 2f)
                )

                if (state.showGrid) {
                    val step = 80f * effectiveScale
                    if (step >= 8f) {
                        var gx = artboardLeft + step
                        while (gx < artboardLeft + artW) {
                            drawLine(Color.Cyan.copy(alpha = 0.28f), Offset(gx, artboardTop), Offset(gx, artboardTop + artH), 1f)
                            gx += step
                        }
                        var gy = artboardTop + step
                        while (gy < artboardTop + artH) {
                            drawLine(Color.Cyan.copy(alpha = 0.28f), Offset(artboardLeft, gy), Offset(artboardLeft + artW, gy), 1f)
                            gy += step
                        }
                    }
                }

                // Multi-point Pen / Curvature Anchor Preview
                if (penAnchorPoints.isNotEmpty()) {
                    if (penAnchorPoints.size >= 2) {
                        val previewPath = Path().apply {
                            moveTo(artboardLeft + penAnchorPoints[0].x * effectiveScale, artboardTop + penAnchorPoints[0].y * effectiveScale)
                            for (i in 1 until penAnchorPoints.size) {
                                lineTo(artboardLeft + penAnchorPoints[i].x * effectiveScale, artboardTop + penAnchorPoints[i].y * effectiveScale)
                            }
                        }
                        drawPath(
                            path = previewPath,
                            color = Color(state.activeColor.toInt()),
                            style = Stroke(width = state.penStrokeWidth * effectiveScale, cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                    }
                    penAnchorPoints.forEach { pt ->
                        val p = Offset(artboardLeft + pt.x * effectiveScale, artboardTop + pt.y * effectiveScale)
                        drawCircle(color = Color.White, radius = 9f, center = p)
                        drawCircle(color = OrangePrimary, radius = 9f, center = p, style = Stroke(3f))
                    }
                }

                // Live drag Pen / Brush stroke preview
                if (activeStrokePoints.size >= 2) {
                    val previewPath = Path().apply {
                        moveTo(artboardLeft + activeStrokePoints[0].x * effectiveScale, artboardTop + activeStrokePoints[0].y * effectiveScale)
                        for (i in 1 until activeStrokePoints.size) {
                            lineTo(artboardLeft + activeStrokePoints[i].x * effectiveScale, artboardTop + activeStrokePoints[i].y * effectiveScale)
                        }
                    }
                    val strokeW = (if (state.activeTool == ToolMode.BRUSH) state.brushSize else state.penStrokeWidth) * effectiveScale
                    drawPath(
                        path = previewPath,
                        color = Color(state.activeColor.toInt()),
                        style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }

                if (activeShapeStart != null && activeShapeEnd != null) {
                    val sx = artboardLeft + min(activeShapeStart!!.x, activeShapeEnd!!.x) * effectiveScale
                    val sy = artboardTop + min(activeShapeStart!!.y, activeShapeEnd!!.y) * effectiveScale
                    val sw = abs(activeShapeEnd!!.x - activeShapeStart!!.x) * effectiveScale
                    val sh = abs(activeShapeEnd!!.y - activeShapeStart!!.y) * effectiveScale
                    drawRect(color = OrangePrimary, topLeft = Offset(sx, sy), size = Size(sw, sh), style = Stroke(3f))
                }

                // Selection Box + Corner Resize + Rotation Handle + Direct-Select Anchor Points
                if (selectedLayer != null && selectedLayer.visible) {
                    val bx = artboardLeft + selectedLayer.x * effectiveScale
                    val by = artboardTop + selectedLayer.y * effectiveScale
                    val bw = selectedLayer.width * effectiveScale
                    val bh = selectedLayer.height * effectiveScale
                    drawRect(color = OrangePrimary, topLeft = Offset(bx, by), size = Size(bw, bh), style = Stroke(3f))

                    if (state.activeTool == ToolMode.DIRECT_SELECT && selectedLayer is PathLayer) {
                        selectedLayer.points.forEach { pt ->
                            val ap = Offset(
                                artboardLeft + (selectedLayer.x + pt.x) * effectiveScale,
                                artboardTop + (selectedLayer.y + pt.y) * effectiveScale
                            )
                            drawCircle(color = OrangePrimary, radius = 12f, center = ap)
                            drawCircle(color = Color.White, radius = 12f, center = ap, style = Stroke(3f))
                        }
                    } else {
                        // Top Rotation Handle stem & knob
                        val rotTop = Offset(bx + bw / 2f, by - 52f)
                        drawLine(OrangePrimary, Offset(bx + bw / 2f, by), rotTop, strokeWidth = 2.5f)
                        drawCircle(color = OrangePrimary, radius = 11f, center = rotTop)
                        drawCircle(color = Color.White, radius = 11f, center = rotTop, style = Stroke(2.5f))

                        val handleRadius = 11f
                        listOf(
                            Offset(bx, by),
                            Offset(bx + bw, by),
                            Offset(bx, by + bh),
                            Offset(bx + bw, by + bh)
                        ).forEachIndexed { idx, pt ->
                            drawCircle(color = if (idx == 3) OrangePrimary else Color.White, radius = handleRadius, center = pt)
                            drawCircle(color = if (idx == 3) Color.White else OrangePrimary, radius = handleRadius, center = pt, style = Stroke(3f))
                        }
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

            // Collapsible Left Illustrator Drawing Toolbar
            EditorLeftToolbar(
                activeTool = state.activeTool,
                activeShapeType = state.activeShapeType,
                onSelectTool = { viewModel.setTool(it) },
                onSelectShapeTool = { st -> viewModel.setShapeType(st) },
                modifier = Modifier.align(Alignment.TopStart)
            )

            // Long-Press Context Menu for Selected Element (Copy, Duplicate, Delete, Bring Forward, Send Backward, Lock)
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 32.dp)
            ) {
                DropdownMenu(
                    expanded = showLongPressMenu && selectedLayer != null,
                    onDismissRequest = { showLongPressMenu = false },
                    offset = DpOffset(0.dp, 8.dp)
                ) {
                    val sel = selectedLayer
                    if (sel != null) {
                        DropdownMenuItem(
                            text = { Text("Copy • কপি") },
                            leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                            onClick = {
                                showLongPressMenu = false
                                viewModel.copySelected()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Duplicate • ডুপ্লিকেট") },
                            leadingIcon = { Icon(Icons.Default.ControlPointDuplicate, contentDescription = null, tint = OrangePrimary) },
                            onClick = {
                                showLongPressMenu = false
                                viewModel.duplicateSelected()
                            }
                        )
                        if (state.clipboardLayer != null) {
                            DropdownMenuItem(
                                text = { Text("Paste • পেস্ট") },
                                leadingIcon = { Icon(Icons.Default.ContentPaste, contentDescription = null) },
                                onClick = {
                                    showLongPressMenu = false
                                    viewModel.pasteClipboard()
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Bring Forward • সামনে আনুন") },
                            leadingIcon = { Icon(Icons.Default.FlipToFront, contentDescription = null) },
                            onClick = {
                                showLongPressMenu = false
                                viewModel.bringForward(sel.id)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Send Backward • পিছনে পাঠান") },
                            leadingIcon = { Icon(Icons.Default.FlipToBack, contentDescription = null) },
                            onClick = {
                                showLongPressMenu = false
                                viewModel.sendBackward(sel.id)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (sel.locked) "Unlock • আনলক" else "Lock • লক") },
                            leadingIcon = {
                                Icon(if (sel.locked) Icons.Default.LockOpen else Icons.Default.Lock, contentDescription = null)
                            },
                            onClick = {
                                showLongPressMenu = false
                                viewModel.toggleLayerLock(sel.id)
                            }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Delete • ডিলিট", color = ErrorRed) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = ErrorRed) },
                            onClick = {
                                showLongPressMenu = false
                                viewModel.deleteLayer(sel.id)
                            }
                        )
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
                    onReorderLayer = { from, to -> viewModel.reorderLayer(from, to) },
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
            .widthIn(min = 52.dp)
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) OrangePrimary.copy(alpha = 0.16f) else Color.Transparent)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 7.dp, vertical = 4.dp)
            .testTag("tool_btn_${label.lowercase()}")
    ) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(20.dp))
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
