package com.example.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
    onSelectShape: (ShapeType) -> Unit,
    onAddQuickShape: (ShapeType) -> Unit,
    onUpdateShape: (ShapeLayer) -> Unit,
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

                ToolMode.PEN -> {
                    Text("Vector Width ${state.penStrokeWidth.toInt()}px", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Slider(
                        value = state.penStrokeWidth,
                        onValueChange = onPenStrokeChange,
                        valueRange = 2f..40f,
                        modifier = Modifier.width(130.dp)
                    )
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(state.activeColor.toInt()))
                            .border(1.dp, Color.White, CircleShape)
                            .clickable(onClick = onOpenColorPicker)
                    )
                    Text("Drag on canvas for smooth vector curve", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                                Text("Crop / BG Remove", fontSize = 11.sp)
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
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(Color.Transparent)
                                    .border(
                                        3.dp,
                                        if ((selectedLayer.strokeColor ushr 24) == 0L) MaterialTheme.colorScheme.outline
                                        else Color(selectedLayer.strokeColor.toInt()),
                                        CircleShape
                                    )
                                    .clickable(onClick = onOpenColorPicker)
                            )
                            Text("Corner", fontSize = 11.sp)
                            Slider(
                                value = selectedLayer.cornerRadius,
                                onValueChange = { onUpdateShape(selectedLayer.copy(cornerRadius = it)) },
                                valueRange = 0f..140f,
                                modifier = Modifier.width(85.dp)
                            )
                            Text("Stroke", fontSize = 11.sp)
                            Slider(
                                value = selectedLayer.strokeWidth,
                                onValueChange = { onUpdateShape(selectedLayer.copy(strokeWidth = it)) },
                                valueRange = 0f..28f,
                                modifier = Modifier.width(85.dp)
                            )
                        }
                        IconButton(onClick = onOpenColorPicker) {
                            Icon(Icons.Default.Palette, contentDescription = "Color", tint = OrangePrimary)
                        }
                        Text("Alpha ${(selectedLayer.opacity * 100).toInt()}%", fontSize = 11.sp)
                        Slider(
                            value = selectedLayer.opacity,
                            onValueChange = onUpdateOpacity,
                            valueRange = 0.05f..1f,
                            modifier = Modifier.width(80.dp)
                        )
                        FilledTonalButton(
                            onClick = { onRotateSelected(15f) },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.RotateRight, contentDescription = "Rotate 15°", modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("15°", fontSize = 11.sp)
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
                            "Tap any object to select • Drag corner handle to resize",
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
