package com.example.ui.editor

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.engine.BackgroundRemover
import com.example.model.ImageLayer
import com.example.ui.theme.CharcoalCanvasWorkspace
import com.example.ui.theme.OrangePrimary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private enum class CropAspectMode(val label: String, val ratio: Float?, val cornerRatio: Float = 0f) {
    FREE("Free • ফ্রি", null, 0f),
    SQUARE("1:1 Square", 1f, 0f),
    PORTRAIT("4:5", 0.8f, 0f),
    WIDE("16:9", 16f / 9f, 0f),
    ROUNDED("Rounded", null, 0.14f),
    CIRCLE("Circle • গোল", 1f, 0.5f)
}

@Composable
fun PhotoCutoutDialog(
    imageLayer: ImageLayer,
    initialPickedColor: Int?,
    onPickedColorChange: (Int?) -> Unit,
    onDismiss: () -> Unit,
    onApplyCropAndEnhance: (
        cropLeft: Float,
        cropTop: Float,
        cropRight: Float,
        cropBottom: Float,
        cornerRadiusRatio: Float,
        upscaleHd: Boolean,
        sharpenAmount: Float,
        fitArtboardToCutout: Boolean
    ) -> Unit
) {
    val scope = rememberCoroutineScope()

    val previewBitmap by produceState<Bitmap?>(initialValue = null, key1 = imageLayer.imagePath) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeFile(imageLayer.imagePath, opts)
                val maxEdge = max(opts.outWidth, opts.outHeight)
                val sample = max(1, maxEdge / 1080)
                val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sample }
                BitmapFactory.decodeFile(imageLayer.imagePath, decodeOpts)
            }.getOrNull()
        }
    }

    var cropLeft by remember(imageLayer.id) { mutableFloatStateOf(imageLayer.cropLeft.coerceIn(0f, 0.85f)) }
    var cropTop by remember(imageLayer.id) { mutableFloatStateOf(imageLayer.cropTop.coerceIn(0f, 0.85f)) }
    var cropRight by remember(imageLayer.id) { mutableFloatStateOf(imageLayer.cropRight.coerceIn(0.15f, 1f)) }
    var cropBottom by remember(imageLayer.id) { mutableFloatStateOf(imageLayer.cropBottom.coerceIn(0.15f, 1f)) }

    var aspectMode by remember { mutableStateOf(CropAspectMode.FREE) }
    var colorPickMode by remember { mutableStateOf(false) }
    var upscaleHd by remember { mutableStateOf(true) }
    var sharpenAmount by remember { mutableFloatStateOf(0.45f) }
    var fitArtboardToCutout by remember { mutableStateOf(true) }

    // Apply aspect constraint when aspect pill changes
    fun applyAspectRatio(mode: CropAspectMode, imgW: Int, imgH: Int) {
        aspectMode = mode
        val targetRatio = mode.ratio ?: return
        val imgAspect = imgW.toFloat() / max(1, imgH).toFloat()
        val normAspect = targetRatio / imgAspect
        val cx = (cropLeft + cropRight) / 2f
        val cy = (cropTop + cropBottom) / 2f
        var nw = (cropRight - cropLeft).coerceAtLeast(0.25f)
        var nh = nw / normAspect
        if (nh > 0.92f) {
            nh = 0.92f
            nw = nh * normAspect
        }
        if (nw > 0.92f) {
            nw = 0.92f
            nh = nw / normAspect
        }
        cropLeft = (cx - nw / 2f).coerceIn(0f, 1f - nw)
        cropRight = (cropLeft + nw).coerceAtMost(1f)
        cropTop = (cy - nh / 2f).coerceIn(0f, 1f - nh)
        cropBottom = (cropTop + nh).coerceAtMost(1f)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.background,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 18.dp)
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Photo Cutout & HD Crop",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            if (colorPickMode) "ফটোর ব্যাকগ্রাউন্ডের রঙের উপর ট্যাপ করুন"
                            else "ফটোর অংশ সিলেক্ট করে হাই রেজুলেশন করুন",
                            fontSize = 11.sp,
                            color = OrangePrimary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Mode Switch: Crop Part vs Tap BG Color
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = !colorPickMode,
                        onClick = { colorPickMode = false },
                        leadingIcon = { Icon(Icons.Default.Crop, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        label = { Text("Crop Part • অংশ ক্রপ", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = colorPickMode,
                        onClick = { colorPickMode = true },
                        leadingIcon = { Icon(Icons.Default.Colorize, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        label = { Text("Pick BG Color • রঙ বাছুন", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Interactive Preview Viewport
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(CharcoalCanvasWorkspace)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val bmp = previewBitmap
                    if (bmp == null) {
                        CircularProgressIndicator(color = OrangePrimary)
                    } else {
                        val boxW = constraints.maxWidth.toFloat().coerceAtLeast(100f)
                        val boxH = constraints.maxHeight.toFloat().coerceAtLeast(100f)
                        val scale = min(boxW / bmp.width.toFloat(), boxH / bmp.height.toFloat())
                        val drawW = bmp.width * scale
                        val drawH = bmp.height * scale
                        val drawLeft = (boxW - drawW) / 2f
                        val drawTop = (boxH - drawH) / 2f

                        var activeDragHandle by remember { mutableStateOf("NONE") }

                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(colorPickMode, drawLeft, drawTop, drawW, drawH, bmp) {
                                    if (colorPickMode) {
                                        detectTapGestures { tap ->
                                            val nx = ((tap.x - drawLeft) / drawW).coerceIn(0f, 1f)
                                            val ny = ((tap.y - drawTop) / drawH).coerceIn(0f, 1f)
                                            scope.launch {
                                                val sampled = BackgroundRemover.sampleColorAtNormalizedPoint(
                                                    imageLayer.originalImagePath.ifBlank { imageLayer.imagePath },
                                                    nx,
                                                    ny
                                                )
                                                onPickedColorChange(sampled)
                                            }
                                        }
                                    }
                                }
                                .pointerInput(colorPickMode, drawLeft, drawTop, drawW, drawH, aspectMode) {
                                    if (!colorPickMode) {
                                        detectDragGestures(
                                            onDragStart = { start ->
                                                val cl = drawLeft + cropLeft * drawW
                                                val ct = drawTop + cropTop * drawH
                                                val cr = drawLeft + cropRight * drawW
                                                val cb = drawTop + cropBottom * drawH
                                                val hitR = 64f
                                                activeDragHandle = when {
                                                    abs(start.x - cl) < hitR && abs(start.y - ct) < hitR -> "TL"
                                                    abs(start.x - cr) < hitR && abs(start.y - ct) < hitR -> "TR"
                                                    abs(start.x - cl) < hitR && abs(start.y - cb) < hitR -> "BL"
                                                    abs(start.x - cr) < hitR && abs(start.y - cb) < hitR -> "BR"
                                                    start.x in cl..cr && start.y in ct..cb -> "MOVE"
                                                    else -> "NONE"
                                                }
                                            },
                                            onDrag = { change, drag ->
                                                change.consume()
                                                val dx = drag.x / drawW
                                                val dy = drag.y / drawH
                                                val minSpan = 0.12f
                                                when (activeDragHandle) {
                                                    "MOVE" -> {
                                                        val w = cropRight - cropLeft
                                                        val h = cropBottom - cropTop
                                                        val nl = (cropLeft + dx).coerceIn(0f, 1f - w)
                                                        val nt = (cropTop + dy).coerceIn(0f, 1f - h)
                                                        cropLeft = nl
                                                        cropRight = nl + w
                                                        cropTop = nt
                                                        cropBottom = nt + h
                                                    }
                                                    "TL" -> {
                                                        cropLeft = (cropLeft + dx).coerceIn(0f, cropRight - minSpan)
                                                        cropTop = (cropTop + dy).coerceIn(0f, cropBottom - minSpan)
                                                    }
                                                    "TR" -> {
                                                        cropRight = (cropRight + dx).coerceIn(cropLeft + minSpan, 1f)
                                                        cropTop = (cropTop + dy).coerceIn(0f, cropBottom - minSpan)
                                                    }
                                                    "BL" -> {
                                                        cropLeft = (cropLeft + dx).coerceIn(0f, cropRight - minSpan)
                                                        cropBottom = (cropBottom + dy).coerceIn(cropTop + minSpan, 1f)
                                                    }
                                                    "BR" -> {
                                                        cropRight = (cropRight + dx).coerceIn(cropLeft + minSpan, 1f)
                                                        cropBottom = (cropBottom + dy).coerceIn(cropTop + minSpan, 1f)
                                                    }
                                                }
                                                val ratio = aspectMode.ratio
                                                if (ratio != null && activeDragHandle in listOf("TL", "TR", "BL", "BR")) {
                                                    val imgAspect = bmp.width.toFloat() / max(1, bmp.height).toFloat()
                                                    val normAspect = ratio / imgAspect
                                                    val desiredH = ((cropRight - cropLeft) / normAspect).coerceAtLeast(minSpan)
                                                    if (cropTop + desiredH <= 1f) {
                                                        cropBottom = cropTop + desiredH
                                                    } else {
                                                        val maxH = 1f - cropTop
                                                        val adjW = (maxH * normAspect).coerceAtLeast(minSpan)
                                                        cropBottom = 1f
                                                        cropRight = (cropLeft + adjW).coerceAtMost(1f)
                                                    }
                                                }
                                            },
                                            onDragEnd = { activeDragHandle = "NONE" },
                                            onDragCancel = { activeDragHandle = "NONE" }
                                        )
                                    }
                                }
                        ) {
                            // Checkerboard behind photo
                            val tile = 18f
                            var cy = drawTop
                            var rIdx = 0
                            while (cy < drawTop + drawH) {
                                var cx = drawLeft
                                var cIdx = 0
                                while (cx < drawLeft + drawW) {
                                    val tw = min(tile, drawLeft + drawW - cx)
                                    val th = min(tile, drawTop + drawH - cy)
                                    drawRect(
                                        color = if ((rIdx + cIdx) % 2 == 0) Color(0xFFBDBDBD) else Color(0xFF8C8C8C),
                                        topLeft = Offset(cx, cy),
                                        size = Size(tw, th)
                                    )
                                    cx += tile
                                    cIdx++
                                }
                                cy += tile
                                rIdx++
                            }

                            drawImage(
                                image = bmp.asImageBitmap(),
                                dstOffset = IntOffset(drawLeft.roundToInt(), drawTop.roundToInt()),
                                dstSize = IntSize(drawW.roundToInt(), drawH.roundToInt())
                            )

                            val cl = drawLeft + cropLeft * drawW
                            val ct = drawTop + cropTop * drawH
                            val cr = drawLeft + cropRight * drawW
                            val cb = drawTop + cropBottom * drawH
                            val cw = cr - cl
                            val ch = cb - ct

                            // Dim outside area
                            val scrim = Color.Black.copy(alpha = 0.56f)
                            drawRect(scrim, Offset(drawLeft, drawTop), Size(drawW, max(0f, ct - drawTop)))
                            drawRect(scrim, Offset(drawLeft, cb), Size(drawW, max(0f, drawTop + drawH - cb)))
                            drawRect(scrim, Offset(drawLeft, ct), Size(max(0f, cl - drawLeft), ch))
                            drawRect(scrim, Offset(cr, ct), Size(max(0f, drawLeft + drawW - cr), ch))

                            // Rule of thirds lines
                            val thirdW = cw / 3f
                            val thirdH = ch / 3f
                            val dash = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))
                            for (i in 1..2) {
                                drawLine(Color.White.copy(alpha = 0.45f), Offset(cl + thirdW * i, ct), Offset(cl + thirdW * i, cb), 1.5f, pathEffect = dash)
                                drawLine(Color.White.copy(alpha = 0.45f), Offset(cl, ct + thirdH * i), Offset(cr, ct + thirdH * i), 1.5f, pathEffect = dash)
                            }

                            // Crop frame
                            drawRect(
                                color = OrangePrimary,
                                topLeft = Offset(cl, ct),
                                size = Size(cw, ch),
                                style = Stroke(width = 3.5f)
                            )

                            if (aspectMode.cornerRatio >= 0.499f) {
                                drawOval(
                                    color = Color.White.copy(alpha = 0.8f),
                                    topLeft = Offset(cl, ct),
                                    size = Size(cw, ch),
                                    style = Stroke(width = 2f, pathEffect = dash)
                                )
                            }

                            // 4 touch handles
                            listOf(
                                Offset(cl, ct),
                                Offset(cr, ct),
                                Offset(cl, cb),
                                Offset(cr, cb)
                            ).forEach { corner ->
                                drawCircle(color = OrangePrimary, radius = 14f, center = corner)
                                drawCircle(color = Color.White, radius = 7f, center = corner)
                            }
                        }
                    }
                }

                // Bottom Settings & Apply Panel
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (colorPickMode) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (initialPickedColor != null) Color(initialPickedColor)
                                            else Color.Transparent
                                        )
                                        .border(1.5.dp, OrangePrimary, CircleShape)
                                )
                                Text(
                                    text = if (initialPickedColor != null) "Picked BG Color Ready"
                                    else "Auto Corners (or tap photo above)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            if (initialPickedColor != null) {
                                TextButton(onClick = { onPickedColorChange(null) }) {
                                    Text("Reset Auto", fontSize = 12.sp, color = OrangePrimary)
                                }
                            }
                        }
                    }

                    // Aspect Ratio Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CropAspectMode.entries.forEach { mode ->
                            FilterChip(
                                selected = aspectMode == mode,
                                onClick = {
                                    val bmp = previewBitmap
                                    if (bmp != null && mode.ratio != null) {
                                        applyAspectRatio(mode, bmp.width, bmp.height)
                                    } else {
                                        aspectMode = mode
                                    }
                                },
                                label = { Text(mode.label, fontSize = 11.sp) }
                            )
                        }
                    }

                    // High-Res Upscale & Clarity Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("4K HD Upscale & Clarity Boost", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("ছবির ছোট অংশ বড় ও ঝকঝকে হাই-রেজুলেশন করবে", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = upscaleHd,
                            onCheckedChange = { upscaleHd = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = OrangePrimary)
                        )
                    }

                    if (upscaleHd) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Sharpness ${(sharpenAmount * 100).toInt()}%", fontSize = 11.sp, modifier = Modifier.width(106.dp))
                            Slider(
                                value = sharpenAmount,
                                onValueChange = { sharpenAmount = it },
                                valueRange = 0f..1f,
                                colors = SliderDefaults.colors(thumbColor = OrangePrimary, activeTrackColor = OrangePrimary),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Fit Artboard to Cutout Size", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("শুধু কাটা অংশ দিয়ে 4K/8K আর্টবোর্ড তৈরি করবে", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = fitArtboardToCutout,
                            onCheckedChange = { fitArtboardToCutout = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = OrangePrimary)
                        )
                    }

                    Button(
                        onClick = {
                            onApplyCropAndEnhance(
                                cropLeft,
                                cropTop,
                                cropRight,
                                cropBottom,
                                aspectMode.cornerRatio,
                                upscaleHd,
                                if (upscaleHd) sharpenAmount else 0f,
                                fitArtboardToCutout
                            )
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("apply_hd_crop_btn")
                    ) {
                        Icon(Icons.Default.HighQuality, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Apply Cutout in HD • হাই রেজুলেশন করুন", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
