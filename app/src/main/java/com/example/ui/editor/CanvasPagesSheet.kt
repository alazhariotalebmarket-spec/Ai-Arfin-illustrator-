package com.example.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.model.CanvasDocument
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.OrangePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CanvasPagesSheet(
    document: CanvasDocument,
    onDismiss: () -> Unit,
    onOpenColorPicker: () -> Unit,
    onPickBackgroundImage: () -> Unit,
    onClearBackgroundImage: () -> Unit,
    onOpenCanvasSize: () -> Unit,
    onSwitchPage: (Int) -> Unit,
    onAddPage: () -> Unit,
    onDuplicatePage: () -> Unit,
    onDeletePage: () -> Unit
) {
    val pages = document.normalizedPages()
    val activeIdx = document.activePageIndex.coerceIn(0, pages.lastIndex)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Canvas & Pages • ক্যানভাস ও পেজ", style = MaterialTheme.typography.titleLarge)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            // Canvas Background & Size Controls
            Text("Background & Size", style = MaterialTheme.typography.labelMedium, color = OrangePrimary)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = {
                        onDismiss()
                        onOpenColorPicker()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("BG Color / Gradient", fontSize = 11.sp)
                }
                FilledTonalButton(
                    onClick = {
                        onDismiss()
                        onOpenCanvasSize()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.AspectRatio, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Resize (${document.artboardWidth}×${document.artboardHeight})", fontSize = 11.sp, maxLines = 1)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onPickBackgroundImage,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Wallpaper, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("BG Image", fontSize = 11.sp)
                }
                if (document.backgroundImagePath != null) {
                    OutlinedButton(
                        onClick = onClearBackgroundImage,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.HideImage, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Remove BG Image", fontSize = 11.sp, color = ErrorRed)
                    }
                }
            }

            HorizontalDivider()

            // Multi-Page Manager
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Pages (${pages.size}) • Active: Page ${activeIdx + 1}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                itemsIndexed(pages) { idx, page ->
                    val selected = idx == activeIdx
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (selected) OrangePrimary.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .width(116.dp)
                            .border(
                                width = if (selected) 2.dp else 1.dp,
                                color = if (selected) OrangePrimary else MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSwitchPage(idx) }
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(page.backgroundColor.toInt()))
                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            )
                            Text(page.name, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                            Text("${page.layers.size} layers", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onAddPage,
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.NoteAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Page", fontSize = 11.sp)
                }
                FilledTonalButton(
                    onClick = onDuplicatePage,
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.ControlPointDuplicate, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Duplicate", fontSize = 11.sp)
                }
                OutlinedButton(
                    onClick = onDeletePage,
                    enabled = pages.size > 1,
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = if (pages.size > 1) ErrorRed else Color.Gray, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete", fontSize = 11.sp)
                }
            }
        }
    }
}
