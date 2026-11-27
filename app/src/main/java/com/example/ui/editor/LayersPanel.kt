package com.example.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.OrangePrimary

@Composable
fun LayersPanel(
    layers: List<LayerElement>,
    selectedLayerId: String?,
    onSelectLayer: (String) -> Unit,
    onToggleVisibility: (String) -> Unit,
    onToggleLock: (String) -> Unit,
    onBringForward: (String) -> Unit,
    onSendBackward: (String) -> Unit,
    onReorderLayer: (fromActualIndex: Int, toActualIndex: Int) -> Unit,
    onRenameLayer: (String, String) -> Unit,
    onDeleteLayer: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var renamingLayer by remember { mutableStateOf<LayerElement?>(null) }
    var draftName by remember { mutableStateOf("") }

    if (renamingLayer != null) {
        AlertDialog(
            onDismissRequest = { renamingLayer = null },
            title = { Text("Rename Layer") },
            text = {
                OutlinedTextField(
                    value = draftName,
                    onValueChange = { draftName = it },
                    singleLine = true,
                    label = { Text("Layer Name") }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRenameLayer(renamingLayer!!.id, draftName)
                        renamingLayer = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { renamingLayer = null }) { Text("Cancel") }
            }
        )
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 10.dp,
        shadowElevation = 16.dp,
        modifier = modifier
            .fillMaxHeight()
            .width(292.dp)
            .border(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Layers • লেয়ার (${layers.size})", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("Drag handle to reorder • Tap to select", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close Layers")
                }
            }

            HorizontalDivider()

            if (layers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No layers yet. Add shapes, text, stickers, or draw with Pen/Brush!",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Reverse order so topmost layer appears at top of list
                val reversed = remember(layers) { layers.reversed() }
                LazyColumn(
                    contentPadding = PaddingValues(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(reversed, key = { _, l -> l.id }) { revIndex, layer ->
                        val isSelected = layer.id == selectedLayerId
                        val actualIdx = layers.lastIndex - revIndex
                        var dragAccum by remember(layer.id) { mutableFloatStateOf(0f) }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) OrangePrimary.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) OrangePrimary else MaterialTheme.colorScheme.outline,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onSelectLayer(layer.id) }
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // Drag handle for vertical reorder
                                    Icon(
                                        imageVector = Icons.Default.DragHandle,
                                        contentDescription = "Drag to reorder",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier
                                            .size(20.dp)
                                            .pointerInput(layer.id, layers.size) {
                                                detectVerticalDragGestures(
                                                    onDragStart = { dragAccum = 0f },
                                                    onVerticalDrag = { _, dragAmount ->
                                                        dragAccum += dragAmount
                                                        val threshold = 72f
                                                        if (dragAccum <= -threshold && actualIdx < layers.lastIndex) {
                                                            onReorderLayer(actualIdx, actualIdx + 1)
                                                            dragAccum = 0f
                                                        } else if (dragAccum >= threshold && actualIdx > 0) {
                                                            onReorderLayer(actualIdx, actualIdx - 1)
                                                            dragAccum = 0f
                                                        }
                                                    }
                                                )
                                            }
                                    )
                                    Icon(
                                        imageVector = when (layer) {
                                            is ShapeLayer -> Icons.Default.Category
                                            is PathLayer -> Icons.Default.Gesture
                                            is TextLayer -> Icons.Default.Title
                                            is ImageLayer -> Icons.Default.Image
                                            is StickerLayer -> Icons.Default.AutoAwesome
                                        },
                                        contentDescription = layer.name,
                                        tint = if (isSelected) OrangePrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = layer.name,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (layer.groupId != null) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = OrangePrimary.copy(alpha = 0.2f)
                                        ) {
                                            Text("GRP", fontSize = 8.sp, color = OrangePrimary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                        }
                                    }
                                    IconButton(
                                        onClick = {
                                            draftName = layer.name
                                            renamingLayer = layer
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Rename", modifier = Modifier.size(15.dp))
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { onToggleVisibility(layer.id) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            if (layer.visible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = "Toggle Visibility",
                                            tint = if (layer.visible) MaterialTheme.colorScheme.onSurface else Color.Gray,
                                            modifier = Modifier.size(17.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { onToggleLock(layer.id) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            if (layer.locked) Icons.Default.Lock else Icons.Default.LockOpen,
                                            contentDescription = "Toggle Lock",
                                            tint = if (layer.locked) OrangePrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(17.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { onBringForward(layer.id) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.ArrowUpward, contentDescription = "Bring Forward", modifier = Modifier.size(17.dp))
                                    }
                                    IconButton(
                                        onClick = { onSendBackward(layer.id) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.ArrowDownward, contentDescription = "Send Backward", modifier = Modifier.size(17.dp))
                                    }
                                    IconButton(
                                        onClick = { onDeleteLayer(layer.id) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete Layer", tint = ErrorRed, modifier = Modifier.size(17.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
