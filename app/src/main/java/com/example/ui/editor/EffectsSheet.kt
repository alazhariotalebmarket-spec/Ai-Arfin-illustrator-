package com.example.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LayerEffects
import com.example.model.LayerElement
import com.example.model.PathLayer
import com.example.model.ShapeLayer
import com.example.model.StrokeStyle
import com.example.ui.theme.OrangePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EffectsSheet(
    selectedLayer: LayerElement?,
    onDismiss: () -> Unit,
    onApplyEffects: (LayerEffects) -> Unit,
    onFlipHorizontal: () -> Unit,
    onFlipVertical: () -> Unit,
    onToggleGroup: () -> Unit,
    onUpdateStrokeOptions: (color: Long, width: Float, style: StrokeStyle) -> Unit
) {
    val initial = selectedLayer?.effects ?: LayerEffects()
    val key = selectedLayer?.id ?: "none"

    var shadowEnabled by remember(key) { mutableStateOf(initial.shadowEnabled) }
    var shadowBlur by remember(key) { mutableFloatStateOf(initial.shadowBlur) }
    var shadowOffsetX by remember(key) { mutableFloatStateOf(initial.shadowOffsetX) }
    var shadowOffsetY by remember(key) { mutableFloatStateOf(initial.shadowOffsetY) }
    var shadowColor by remember(key) { mutableLongStateOf(initial.shadowColor) }

    var glowEnabled by remember(key) { mutableStateOf(initial.glowEnabled) }
    var glowRadius by remember(key) { mutableFloatStateOf(initial.glowRadius) }
    var glowColor by remember(key) { mutableLongStateOf(initial.glowColor) }

    var blurRadius by remember(key) { mutableFloatStateOf(initial.blurRadius) }
    var is3dEffect by remember(key) { mutableStateOf(initial.is3dEffect) }
    var outlineWidth by remember(key) { mutableFloatStateOf(initial.outlineWidth) }
    var outlineColor by remember(key) { mutableLongStateOf(initial.outlineColor) }

    val initialStrokeColor = when (selectedLayer) {
        is ShapeLayer -> selectedLayer.strokeColor
        is PathLayer -> selectedLayer.strokeColor
        else -> 0xFF1E1E1EL
    }
    val initialStrokeWidth = when (selectedLayer) {
        is ShapeLayer -> selectedLayer.strokeWidth
        is PathLayer -> selectedLayer.strokeWidth
        else -> 4f
    }
    val initialStrokeStyle = when (selectedLayer) {
        is ShapeLayer -> selectedLayer.strokeStyle
        is PathLayer -> selectedLayer.strokeStyle
        else -> StrokeStyle.SOLID
    }
    var strokeColor by remember(key) { mutableLongStateOf(initialStrokeColor) }
    var strokeWidth by remember(key) { mutableFloatStateOf(initialStrokeWidth) }
    var strokeStyle by remember(key) { mutableStateOf(initialStrokeStyle) }

    val swatchColors = listOf(
        0x88000000L, 0xFFFF7A00L, 0xFFFFFFFFL, 0xFF00E5FFL,
        0xFFD4AF37L, 0xFFEF4444L, 0xFF10B981L, 0xFF7C3AEDL
    )

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
                Text("Effects & Transform • ইফেক্টস", style = MaterialTheme.typography.titleLarge)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            if (selectedLayer == null) {
                Text(
                    "Select any layer on the canvas to apply Shadow, Glow, Blur, 3D Effect, Outline, Flip, or Group.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            } else {
                // Quick Flip & Group row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onFlipHorizontal,
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Flip, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Flip H", fontSize = 11.sp)
                    }
                    OutlinedButton(
                        onClick = onFlipVertical,
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.SwapVert, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Flip V", fontSize = 11.sp)
                    }
                    OutlinedButton(
                        onClick = onToggleGroup,
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Workspaces, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (selectedLayer.groupId != null) "Ungroup" else "Group", fontSize = 11.sp)
                    }
                }

                HorizontalDivider()

                // Shadow Effect
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Drop Shadow • শ্যাডো", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Switch(checked = shadowEnabled, onCheckedChange = { shadowEnabled = it })
                }
                if (shadowEnabled) {
                    EffectSlider("Blur ${shadowBlur.toInt()}px", shadowBlur, 2f..48f) { shadowBlur = it }
                    EffectSlider("Offset X ${shadowOffsetX.toInt()}", shadowOffsetX, -32f..32f) { shadowOffsetX = it }
                    EffectSlider("Offset Y ${shadowOffsetY.toInt()}", shadowOffsetY, -32f..32f) { shadowOffsetY = it }
                    ColorSwatchRow("Shadow Color", swatchColors, shadowColor) { shadowColor = it }
                }

                // Glow Effect
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Outer Glow • গ্লো", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Switch(checked = glowEnabled, onCheckedChange = { glowEnabled = it })
                }
                if (glowEnabled) {
                    EffectSlider("Radius ${glowRadius.toInt()}px", glowRadius, 4f..54f) { glowRadius = it }
                    ColorSwatchRow("Glow Color", swatchColors, glowColor) { glowColor = it }
                }

                // 3D Extrude Effect
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("3D Depth Effect • থ্রিডি ইফেক্ট", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Adds multi-layer isometric 3D extrusion", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = is3dEffect, onCheckedChange = { is3dEffect = it })
                }

                // Blur & Outline
                EffectSlider("Gaussian Blur ${blurRadius.toInt()}px", blurRadius, 0f..32f) { blurRadius = it }
                EffectSlider("Outline ${outlineWidth.toInt()}px", outlineWidth, 0f..24f) { outlineWidth = it }
                if (outlineWidth > 0f) {
                    ColorSwatchRow("Outline Color", swatchColors, outlineColor) { outlineColor = it }
                }

                if (selectedLayer is ShapeLayer || selectedLayer is PathLayer) {
                    HorizontalDivider()
                    Text("Stroke & Dashed Line • স্ট্রোক", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = OrangePrimary)
                    EffectSlider("Stroke ${strokeWidth.toInt()}px", strokeWidth, 0f..32f) { strokeWidth = it }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StrokeStyle.entries.forEach { st ->
                            FilterChip(
                                selected = strokeStyle == st,
                                onClick = { strokeStyle = st },
                                label = { Text(st.label, fontSize = 11.sp) }
                            )
                        }
                    }
                    ColorSwatchRow("Stroke Color", swatchColors, strokeColor) { strokeColor = it }
                }

                Button(
                    onClick = {
                        onApplyEffects(
                            LayerEffects(
                                shadowEnabled = shadowEnabled,
                                shadowBlur = shadowBlur,
                                shadowOffsetX = shadowOffsetX,
                                shadowOffsetY = shadowOffsetY,
                                shadowColor = shadowColor,
                                glowEnabled = glowEnabled,
                                glowRadius = glowRadius,
                                glowColor = glowColor,
                                blurRadius = blurRadius,
                                is3dEffect = is3dEffect,
                                outlineWidth = outlineWidth,
                                outlineColor = outlineColor
                            )
                        )
                        if (selectedLayer is ShapeLayer || selectedLayer is PathLayer) {
                            onUpdateStrokeOptions(strokeColor, strokeWidth, strokeStyle)
                        }
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Apply Effects", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun EffectSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 11.sp, modifier = Modifier.width(108.dp))
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(thumbColor = OrangePrimary, activeTrackColor = OrangePrimary),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ColorSwatchRow(
    label: String,
    colors: List<Long>,
    selectedColor: Long,
    onPick: (Long) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            colors.forEach { c ->
                val active = selectedColor == c
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(c.toInt()))
                        .border(if (active) 2.5.dp else 1.dp, if (active) OrangePrimary else MaterialTheme.colorScheme.outline, CircleShape)
                        .clickable { onPick(c) }
                )
            }
        }
    }
}
