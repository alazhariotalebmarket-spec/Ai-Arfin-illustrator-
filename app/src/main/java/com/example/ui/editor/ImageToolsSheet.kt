package com.example.ui.editor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ImageLayer
import com.example.ui.theme.OrangePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageToolsSheet(
    selectedImageLayer: ImageLayer?,
    isProcessing: Boolean,
    onDismiss: () -> Unit,
    onPickFromGallery: () -> Unit,
    onCaptureFromCamera: () -> Unit,
    onUpdateImageLayer: (ImageLayer) -> Unit,
    onRemoveBackground: (tolerance: Float, globalKey: Boolean) -> Unit
) {
    var tolerance by remember { mutableFloatStateOf(0.24f) }
    var globalKey by remember { mutableStateOf(false) }

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
                Text("Image Studio • ছবি ও ব্যাকগ্রাউন্ড রিমুভার", style = MaterialTheme.typography.titleLarge)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            // Upload from Gallery (Photo Picker) or Camera
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onPickFromGallery,
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = "Gallery", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Gallery", fontWeight = FontWeight.Bold)
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
                Text("Selected Photo Controls", style = MaterialTheme.typography.labelMedium, color = OrangePrimary)

                // Opacity Slider
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

                // Rotate Slider + 90 deg button
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

                // Crop Presets
                Text("Quick Crop • ক্রপ", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val crops = listOf(
                        "Full" to listOf(0f, 0f, 1f, 1f),
                        "Center" to listOf(0.12f, 0.12f, 0.88f, 0.88f),
                        "Wide" to listOf(0f, 0.2f, 1f, 0.8f),
                        "Tall" to listOf(0.2f, 0f, 0.8f, 1f)
                    )
                    crops.forEach { (label, box) ->
                        OutlinedButton(
                            onClick = {
                                onUpdateImageLayer(
                                    selectedImageLayer.copy(
                                        cropLeft = box[0],
                                        cropTop = box[1],
                                        cropRight = box[2],
                                        cropBottom = box[3]
                                    )
                                )
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(label, fontSize = 11.sp)
                        }
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
                        Text("Offline Background Remover", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            "Removes solid/studio backdrop cleanly on-device without internet.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Tolerance ${(tolerance * 100).toInt()}%", fontSize = 12.sp, modifier = Modifier.width(104.dp))
                            Slider(
                                value = tolerance,
                                onValueChange = { tolerance = it },
                                valueRange = 0.08f..0.55f,
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
                            onClick = { onRemoveBackground(tolerance, globalKey) },
                            enabled = !isProcessing,
                            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isProcessing) {
                                CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Removing Background...")
                            } else {
                                Icon(Icons.Default.AutoFixHigh, contentDescription = "Remove BG", modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Remove Background Now", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
