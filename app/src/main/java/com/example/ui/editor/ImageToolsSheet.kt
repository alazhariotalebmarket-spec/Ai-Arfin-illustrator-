package com.example.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.PixelRepairProfile
import com.example.model.ImageLayer
import com.example.ui.theme.OrangePrimary
import com.example.ui.theme.SuccessGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageToolsSheet(
    selectedImageLayer: ImageLayer?,
    isProcessing: Boolean,
    pickedBgColor: Int?,
    onClearPickedBgColor: () -> Unit,
    onDismiss: () -> Unit,
    onPickFromFileManager: () -> Unit,
    onPickFromGallery: () -> Unit,
    onCaptureFromCamera: () -> Unit,
    onOpenInteractiveCrop: () -> Unit,
    onUpdateImageLayer: (ImageLayer) -> Unit,
    onRemoveBackground: (tolerance: Float, globalKey: Boolean, targetColor: Int?, edgeSmoothness: Float) -> Unit,
    onSuperResolveImage: (targetMaxEdge: Int, profile: PixelRepairProfile) -> Unit,
    onResetToOriginal: () -> Unit,
    onQuickTransparentExport: () -> Unit
) {
    var tolerance by remember { mutableFloatStateOf(0.24f) }
    var edgeSmoothness by remember { mutableFloatStateOf(0.28f) }
    var globalKey by remember { mutableStateOf(false) }
    var selectedRepairProfile by remember { mutableStateOf(PixelRepairProfile.BALANCED_HD) }
    var selectedTargetMaxEdge by remember { mutableIntStateOf(3840) }

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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Photo & Cutout Studio", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "ফাইল ম্যানেজার থেকে ফটো এনে ক্রপ, ব্যাকগ্রাউন্ড রিমুভ ও 4K HD তৈরি করুন",
                        fontSize = 11.sp,
                        color = OrangePrimary
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            // Import from File Manager, Gallery, Camera (Zero storage permission)
            Button(
                onClick = onPickFromFileManager,
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("pick_file_manager_btn")
            ) {
                Icon(Icons.Default.FolderOpen, contentDescription = "File Manager", modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Select Photo from File Manager • ফাইল ম্যানেজার", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onPickFromGallery,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = "Gallery", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Gallery")
                }
                OutlinedButton(
                    onClick = onCaptureFromCamera,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Camera", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Camera")
                }
            }

            if (selectedImageLayer != null) {
                HorizontalDivider()

                // Auto Pixel-by-Pixel Super-Resolution (Fix Pixelated Photos for 10x Zoom)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, OrangePrimary.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Hd, contentDescription = null, tint = OrangePrimary)
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Auto Pixel HD Restorer • ফাটা ছবি HD", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(
                                    "ফাটা বা ঝাপসা ছবির প্রতিটি পিক্সেল ৪কে হাই-রেজুলেশনে রিস্টোর করে (জুম করলেও ফাটবে না)",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            PixelRepairProfile.entries.forEach { prof ->
                                FilterChip(
                                    selected = selectedRepairProfile == prof,
                                    onClick = { selectedRepairProfile = prof },
                                    label = { Text(prof.labelBn, fontSize = 10.sp, maxLines = 1) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("2K HD" to 2560, "4K Ultra" to 3840, "Max 4096" to 4096).forEach { (lbl, edge) ->
                                FilterChip(
                                    selected = selectedTargetMaxEdge == edge,
                                    onClick = { selectedTargetMaxEdge = edge },
                                    label = { Text(lbl, fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Button(
                            onClick = { onSuperResolveImage(selectedTargetMaxEdge, selectedRepairProfile) },
                            enabled = !isProcessing,
                            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auto_pixel_hd_btn")
                        ) {
                            if (isProcessing) {
                                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Restoring Pixels in HD...")
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Auto Enhance Every Pixel • ফাটা ছবি HD করুন", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Interactive Crop Part + HD Upscale Card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = OrangePrimary.copy(alpha = 0.12f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, OrangePrimary.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Crop, contentDescription = null, tint = OrangePrimary)
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Crop Any Part & HD Upscale", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(
                                    "ফটোর নির্দিষ্ট অংশ কেটে 4K হাই-রেজুলেশন করুন এবং ব্যাকগ্রাউন্ডের রঙ বাছুন",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Button(
                            onClick = onOpenInteractiveCrop,
                            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("open_interactive_crop_btn")
                        ) {
                            Icon(Icons.Default.Transform, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Open Visual Crop & Color Picker (HD)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }

                // Cutout Shape Presets (Sharp, Rounded, Circle)
                Text("Cutout Shape • কাটার ধরন", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val shapes = listOf(
                        "Sharp" to 0f,
                        "Rounded" to 0.12f,
                        "Pill" to 0.28f,
                        "Circle" to 0.5f
                    )
                    shapes.forEach { (label, ratio) ->
                        val active = kotlin.math.abs(selectedImageLayer.cornerRadiusRatio - ratio) < 0.02f
                        FilterChip(
                            selected = active,
                            onClick = { onUpdateImageLayer(selectedImageLayer.copy(cornerRadiusRatio = ratio)) },
                            label = { Text(label, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Offline Smart Background Remover
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Transparent Background Remover", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            if (selectedImageLayer.originalImagePath.isNotBlank() &&
                                selectedImageLayer.originalImagePath != selectedImageLayer.imagePath
                            ) {
                                TextButton(
                                    onClick = onResetToOriginal,
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                ) {
                                    Text("Restore Original", fontSize = 11.sp, color = OrangePrimary)
                                }
                            }
                        }

                        if (pickedBgColor != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(Color(pickedBgColor))
                                            .border(1.dp, OrangePrimary, CircleShape)
                                    )
                                    Text("Target Color Picked", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                }
                                TextButton(onClick = onClearPickedBgColor) {
                                    Text("Use Auto", fontSize = 11.sp, color = OrangePrimary)
                                }
                            }
                        } else {
                            Text(
                                "Auto-detects backdrop edges (or open Visual Crop & Color Picker to tap any BG color).",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Tolerance ${(tolerance * 100).toInt()}%", fontSize = 12.sp, modifier = Modifier.width(106.dp))
                            Slider(
                                value = tolerance,
                                onValueChange = { tolerance = it },
                                valueRange = 0.06f..0.60f,
                                colors = SliderDefaults.colors(thumbColor = OrangePrimary, activeTrackColor = OrangePrimary),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Edge Smooth ${(edgeSmoothness * 100).toInt()}%", fontSize = 12.sp, modifier = Modifier.width(106.dp))
                            Slider(
                                value = edgeSmoothness,
                                onValueChange = { edgeSmoothness = it },
                                valueRange = 0.10f..0.60f,
                                colors = SliderDefaults.colors(thumbColor = OrangePrimary, activeTrackColor = OrangePrimary),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Remove inner matching holes too", fontSize = 12.sp)
                            Switch(
                                checked = globalKey,
                                onCheckedChange = { globalKey = it }
                            )
                        }
                        Button(
                            onClick = { onRemoveBackground(tolerance, globalKey, pickedBgColor, edgeSmoothness) },
                            enabled = !isProcessing,
                            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("remove_bg_btn")
                        ) {
                            if (isProcessing) {
                                CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Processing HD Transparent...")
                            } else {
                                Icon(Icons.Default.AutoFixHigh, contentDescription = "Remove BG", modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Make Transparent Background Now", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // 1-Tap High-Res Transparent PNG Export shortcut
                FilledTonalButton(
                    onClick = onQuickTransparentExport,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = SuccessGreen.copy(alpha = 0.18f),
                        contentColor = SuccessGreen
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export Transparent PNG (4K / 8K HD)", fontWeight = FontWeight.Bold)
                }

                // Opacity & Rotate Controls
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Opacity ${(selectedImageLayer.opacity * 100).toInt()}%", fontSize = 12.sp, modifier = Modifier.width(96.dp))
                    Slider(
                        value = selectedImageLayer.opacity,
                        onValueChange = { onUpdateImageLayer(selectedImageLayer.copy(opacity = it)) },
                        valueRange = 0.05f..1f,
                        colors = SliderDefaults.colors(thumbColor = OrangePrimary, activeTrackColor = OrangePrimary),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Rotate ${selectedImageLayer.rotation.toInt()}°", fontSize = 12.sp, modifier = Modifier.width(96.dp))
                    Slider(
                        value = selectedImageLayer.rotation,
                        onValueChange = { onUpdateImageLayer(selectedImageLayer.copy(rotation = it)) },
                        valueRange = -180f..180f,
                        colors = SliderDefaults.colors(thumbColor = OrangePrimary, activeTrackColor = OrangePrimary),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        val nextRot = (selectedImageLayer.rotation + 90f) % 360f
                        onUpdateImageLayer(selectedImageLayer.copy(rotation = nextRot))
                    }) {
                        Icon(Icons.Default.RotateRight, contentDescription = "Rotate 90", tint = OrangePrimary)
                    }
                }
            }
        }
    }
}
