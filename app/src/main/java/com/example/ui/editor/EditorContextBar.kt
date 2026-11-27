package com.example.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.OrangePrimary
import com.example.viewmodel.EditorUiState

@Composable
fun EditorContextBar(
    state: EditorUiState,
    selectedLayer: LayerElement?,
    zoomScale: Float,
    penPointCount: Int,
    onSelectShape: (ShapeType) -> Unit,
    onAddQuickShape: (ShapeType) -> Unit,
    onUpdateShape: (ShapeLayer) -> Unit,
    onUpdatePath: (PathLayer) -> Unit,
    onFinishPenPath: (closed: Boolean) -> Unit,
    onClearPenPath: () -> Unit,
    onConvertShapeToPoints: () -> Unit,
    onBrushChange: (size: Float, opacity: Float, style: BrushStyle) -> Unit,
    onPenStrokeChange: (Float) -> Unit,
    onEraserSizeChange: (Float) -> Unit,
    onAlign: (AlignAction) -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onZoomReset: () -> Unit,
    onEditSelectedText: () -> Unit,
    onEditSelectedImage: () -> Unit,
    onDuplicateSelected: () -> Unit,
    onCopySelected: () -> Unit,
    onPasteClipboard: () -> Unit,
    onDeleteSelected: () -> Unit,
    onFlipH: () -> Unit,
    onFlipV: () -> Unit,
    onToggleGroup: () -> Unit,
    onBringForward: () -> Unit,
    onSendBackward: () -> Unit,
    onOpenEffectsSheet: () -> Unit,
    onUpdateOpacity: (Float) -> Unit,
    onRotateSelected: (Float) -> Unit,
    onNudgeSelected: (dx: Float, dy: Float) -> Unit,
    onOpenColorPicker: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        tonalElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            when (state.activeTool) {
                ToolMode.DIRECT_SELECT -> {
                    if (selectedLayer is PathLayer) {
                        Text(
                            "Anchor Points (${selectedLayer.points.size}) • Drag any point on canvas",
                            fontSize = 11.sp,
                            color = OrangePrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    } else if (selectedLayer is ShapeLayer) {
                        FilledTonalButton(
                            onClick = onConvertShapeToPoints,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.AdsClick, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit Anchor Points", fontSize = 11.sp)
                        }
                    } else {
                        Text(
                            "Select a vector path or shape to edit anchor points",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                ToolMode.SHAPES -> {
                    ShapeType.entries.forEach { st ->
                        val active = state.activeShapeType == st
                        FilterChip(
                            selected = active,
                            onClick = { onSelectShape(st) },
                            label = { Text(st.label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = OrangePrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                    FilledTonalButton(
                        onClick = { onAddQuickShape(state.activeShapeType) },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Shape", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Drop ${state.activeShapeType.label}", fontSize = 11.sp)
                    }
                }

                ToolMode.BRUSH -> {
                    Text("Size ${state.brushSize.toInt()}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Slider(
                        value = state.brushSize,
                        onValueChange = { onBrushChange(it, state.brushOpacity, state.brushStyle) },
                        valueRange = 4f..72f,
                        modifier = Modifier.width(105.dp)
                    )
                    Text("Alpha ${(state.brushOpacity * 100).toInt()}%", fontSize = 11.sp)
                    Slider(
                        value = state.brushOpacity,
                        onValueChange = { onBrushChange(state.brushSize, it, state.brushStyle) },
                        valueRange = 0.1f..1f,
                        modifier = Modifier.width(95.dp)
                    )
                    BrushStyle.entries.forEach { bs ->
                        FilterChip(
                            selected = state.brushStyle == bs,
                            onClick = { onBrushChange(state.brushSize, state.brushOpacity, bs) },
                            label = { Text(bs.label, fontSize = 11.sp) }
                        )
                    }
                }

                ToolMode.PEN, ToolMode.CURVATURE -> {
                    Text("Vector ${state.penStrokeWidth.toInt()}px", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Slider(
                        value = state.penStrokeWidth,
                        onValueChange = onPenStrokeChange,
                        valueRange = 2f..40f,
                        modifier = Modifier.width(110.dp)
                    )
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(Color(state.activeColor.toInt()))
                            .border(1.dp, Color.White, CircleShape)
                            .clickable(onClick = onOpenColorPicker)
                    )
                    if (penPointCount >= 2) {
                        FilledTonalButton(
                            onClick = { onFinishPenPath(false) },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                        ) { Text("Done ($penPointCount pts)", fontSize = 11.sp) }
                        if (penPointCount >= 3) {
                            Button(
                                onClick = { onFinishPenPath(true) },
                                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                            ) { Text("Close & Fill", fontSize = 11.sp) }
                        }
                        IconButton(onClick = onClearPenPath) {
                            Icon(Icons.Default.Clear, contentDescription = "Cancel Points")
                        }
                    } else {
                        Text("Tap to place anchor points OR drag to draw curve", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                ToolMode.ERASER -> {
                    Text("Eraser ${state.eraserSize.toInt()}px", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Slider(
                        value = state.eraserSize,
                        onValueChange = onEraserSizeChange,
                        valueRange = 16f..120f,
                        modifier = Modifier.width(140.dp)
                    )
                    Text("Tap or drag over strokes/layers to erase", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                ToolMode.HAND -> {
                    Icon(Icons.Default.PanTool, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(16.dp))
                    Text("Hand Tool • Drag anywhere with 1 finger to move canvas", fontSize = 11.sp)
                    OutlinedButton(onClick = onZoomReset, contentPadding = PaddingValues(horizontal = 10.dp)) {
                        Text("Center Canvas", fontSize = 11.sp)
                    }
                }

                ToolMode.ALIGN -> {
                    AlignAction.entries.forEach { act ->
                        OutlinedButton(
                            onClick = { onAlign(act) },
                            enabled = selectedLayer != null && !selectedLayer.locked,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(act.label, fontSize = 11.sp)
                        }
                    }
                }

                ToolMode.ZOOM -> {
                    IconButton(onClick = onZoomOut) { Icon(Icons.Default.ZoomOut, contentDescription = "Zoom Out") }
                    Text("${(zoomScale * 100).toInt()}%", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    IconButton(onClick = onZoomIn) { Icon(Icons.Default.ZoomIn, contentDescription = "Zoom In") }
                    OutlinedButton(onClick = onZoomReset, contentPadding = PaddingValues(horizontal = 10.dp)) {
                        Text("Fit / Reset", fontSize = 11.sp)
                    }
                }

                else -> {
                    if (selectedLayer != null) {
                        Text(
                            selectedLayer.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = OrangePrimary,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        if (selectedLayer is TextLayer) {
                            FilledTonalButton(
                                onClick = onEditSelectedText,
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Edit Text", fontSize = 11.sp)
                            }
                        }
                        if (selectedLayer is ImageLayer) {
                            FilledTonalButton(
                                onClick = onEditSelectedImage,
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Crop / BG / HD", fontSize = 11.sp)
                            }
                        }
                        if (selectedLayer is ShapeLayer) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(Color(selectedLayer.fillColor.toInt()))
                                    .border(1.5.dp, Color.White, CircleShape)
                                    .clickable(onClick = onOpenColorPicker)
                            )
                            Text("Corner", fontSize = 11.sp)
                            Slider(
                                value = selectedLayer.cornerRadius,
                                onValueChange = { onUpdateShape(selectedLayer.copy(cornerRadius = it)) },
                                valueRange = 0f..140f,
                                modifier = Modifier.width(75.dp)
                            )
                            Text("Stroke", fontSize = 11.sp)
                            Slider(
                                value = selectedLayer.strokeWidth,
                                onValueChange = { onUpdateShape(selectedLayer.copy(strokeWidth = it)) },
                                valueRange = 0f..28f,
                                modifier = Modifier.width(75.dp)
                            )
                            AssistChip(
                                onClick = {
                                    val nextStyle = when (selectedLayer.strokeStyle) {
                                        StrokeStyle.SOLID -> StrokeStyle.DASHED
                                        StrokeStyle.DASHED -> StrokeStyle.DOTTED
                                        StrokeStyle.DOTTED -> StrokeStyle.SOLID
                                    }
                                    onUpdateShape(
                                        selectedLayer.copy(
                                            strokeStyle = nextStyle,
                                            strokeWidth = selectedLayer.strokeWidth.coerceAtLeast(4f),
                                            strokeColor = if ((selectedLayer.strokeColor ushr 24) == 0L) 0xFF1E1E1EL else selectedLayer.strokeColor
                                        )
                                    )
                                },
                                label = { Text(selectedLayer.strokeStyle.label, fontSize = 10.sp) }
                            )
                        }
                        if (selectedLayer is PathLayer) {
                            AssistChip(
                                onClick = {
                                    val nextStyle = when (selectedLayer.strokeStyle) {
                                        StrokeStyle.SOLID -> StrokeStyle.DASHED
                                        StrokeStyle.DASHED -> StrokeStyle.DOTTED
                                        StrokeStyle.DOTTED -> StrokeStyle.SOLID
                                    }
                                    onUpdatePath(selectedLayer.copy(strokeStyle = nextStyle))
                                },
                                label = { Text(selectedLayer.strokeStyle.label, fontSize = 10.sp) }
                            )
                        }
                        FilledTonalButton(
                            onClick = onOpenEffectsSheet,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Effects", fontSize = 11.sp)
                        }
                        IconButton(onClick = onOpenColorPicker) {
                            Icon(Icons.Default.Palette, contentDescription = "Color", tint = OrangePrimary)
                        }
                        Text("Alpha ${(selectedLayer.opacity * 100).toInt()}%", fontSize = 11.sp)
                        Slider(
                            value = selectedLayer.opacity,
                            onValueChange = onUpdateOpacity,
                            valueRange = 0.05f..1f,
                            modifier = Modifier.width(75.dp)
                        )
                        FilledTonalButton(
                            onClick = { onRotateSelected(15f) },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.RotateRight, contentDescription = "Rotate 15°", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("15°", fontSize = 11.sp)
                        }
                        IconButton(onClick = onFlipH) {
                            Icon(Icons.Default.Flip, contentDescription = "Flip Horizontal")
                        }
                        IconButton(onClick = onFlipV) {
                            Icon(Icons.Default.SwapVert, contentDescription = "Flip Vertical")
                        }
                        IconButton(onClick = onToggleGroup) {
                            Icon(
                                Icons.Default.Workspaces,
                                contentDescription = if (selectedLayer.groupId != null) "Ungroup" else "Group",
                                tint = if (selectedLayer.groupId != null) OrangePrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(onClick = onBringForward) {
                            Icon(Icons.Default.FlipToFront, contentDescription = "Bring Forward")
                        }
                        IconButton(onClick = onSendBackward) {
                            Icon(Icons.Default.FlipToBack, contentDescription = "Send Backward")
                        }
                        IconButton(onClick = { onNudgeSelected(-4f, 0f) }) {
                            Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Nudge Left")
                        }
                        IconButton(onClick = { onNudgeSelected(0f, -4f) }) {
                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Nudge Up")
                        }
                        IconButton(onClick = { onNudgeSelected(0f, 4f) }) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Nudge Down")
                        }
                        IconButton(onClick = { onNudgeSelected(4f, 0f) }) {
                            Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Nudge Right")
                        }
                        IconButton(onClick = onDuplicateSelected) {
                            Icon(Icons.Default.ControlPointDuplicate, contentDescription = "Duplicate")
                        }
                        IconButton(onClick = onCopySelected) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                        }
                        if (state.clipboardLayer != null) {
                            IconButton(onClick = onPasteClipboard) {
                                Icon(Icons.Default.ContentPaste, contentDescription = "Paste")
                            }
                        }
                        IconButton(onClick = onDeleteSelected) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed)
                        }
                    } else {
                        Text(
                            "Tap object to select • Long-press for Copy/Duplicate/Delete/Lock",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (state.clipboardLayer != null) {
                            OutlinedButton(
                                onClick = onPasteClipboard,
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.ContentPaste, contentDescription = "Paste", modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Paste Layer", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
