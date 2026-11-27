package com.example.ui.editor

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Colorize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.OrangePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorPickerSheet(
    initialColor: Long,
    initialGradientEnd: Long?,
    recentColors: List<Long>,
    hasSelectedLayer: Boolean,
    showStrokeOption: Boolean = false,
    onDismiss: () -> Unit,
    onApplyToSelectionOrTool: (color: Long, gradientEnd: Long?) -> Unit,
    onApplyToStroke: ((color: Long) -> Unit)? = null,
    onApplyToArtboardBg: (color: Long, gradientEnd: Long?) -> Unit,
    onActivateEyedropper: () -> Unit
) {
    var useGradient by remember { mutableStateOf(initialGradientEnd != null) }
    var editingEndColor by remember { mutableStateOf(false) }

    var startColor by remember { mutableLongStateOf(initialColor) }
    var endColor by remember { mutableLongStateOf(initialGradientEnd ?: 0xFFCC4400L) }

    val activeTarget = if (editingEndColor) endColor else startColor
    val hsv = remember(activeTarget) {
        val arr = FloatArray(3)
        AndroidColor.colorToHSV(activeTarget.toInt(), arr)
        arr
    }
    var hue by remember(activeTarget) { mutableFloatStateOf(hsv[0]) }
    var sat by remember(activeTarget) { mutableFloatStateOf(hsv[1]) }
    var value by remember(activeTarget) { mutableFloatStateOf(hsv[2]) }

    var hexInput by remember(activeTarget) {
        mutableStateOf(String.format("%06X", activeTarget.toInt() and 0xFFFFFF))
    }

    fun updateActiveFromHsv(h: Float, s: Float, v: Float) {
        hue = h
        sat = s
        value = v
        val c = AndroidColor.HSVToColor(floatArrayOf(h, s, v)).toLong() and 0xFFFFFFFFL
        if (editingEndColor) endColor = c else startColor = c
        hexInput = String.format("%06X", c.toInt() and 0xFFFFFF)
    }

    val quickPalette = listOf(
        0xFFFF7A00L, 0xFFCC4400L, 0xFF1E1E1EL, 0xFFFFFFFFL,
        0xFFD4AF37L, 0xFF0F5132L, 0xFF121C38L, 0xFFDC2626L,
        0xFF2563EBL, 0xFF10B981L, 0xFF7C3AEDL, 0xFFEC4899L,
        0xFFFACC15L, 0xFF06B6D4L, 0xFF64748BL, 0xFF000000L
    )

    val gradientPresets = listOf(
        0xFFFF7A00L to 0xFFCC4400L,
        0xFF0F5132L to 0xFF041F17L,
        0xFF121C38L to 0xFF312E81L,
        0xFFEC4899L to 0xFF7C3AEDL,
        0xFFD4AF37L to 0xFF92400EL,
        0xFF06B6D4L to 0xFF2563EBL
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
                Text("Color & Gradient • কালার প্যানেল", style = MaterialTheme.typography.titleLarge)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalIconButton(onClick = onActivateEyedropper) {
                        Icon(Icons.Default.Colorize, contentDescription = "Eyedropper", tint = OrangePrimary)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            }

            // Solid vs Gradient Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilterChip(
                    selected = !useGradient,
                    onClick = {
                        useGradient = false
                        editingEndColor = false
                    },
                    label = { Text("Solid Color") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = useGradient,
                    onClick = { useGradient = true },
                    label = { Text("Gradient Color") },
                    modifier = Modifier.weight(1f)
                )
            }

            // Preview Swatch / Gradient Endpoints
            if (useGradient) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Brush.horizontalGradient(listOf(Color(startColor.toInt()), Color(endColor.toInt()))))
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                    )
                    Button(
                        onClick = { editingEndColor = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!editingEndColor) OrangePrimary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (!editingEndColor) Color.White else MaterialTheme.colorScheme.onSurface
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Text("Start", fontSize = 12.sp)
                    }
                    Button(
                        onClick = { editingEndColor = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (editingEndColor) OrangePrimary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (editingEndColor) Color.White else MaterialTheme.colorScheme.onSurface
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Text("End", fontSize = 12.sp)
                    }
                }
            }

            // Interactive Hue Strip
            Text("Hue Spectrum", style = MaterialTheme.typography.labelMedium)
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .pointerInput(editingEndColor) {
                        detectTapGestures { offset ->
                            val newHue = (offset.x / size.width).coerceIn(0f, 1f) * 360f
                            updateActiveFromHsv(newHue, sat.coerceAtLeast(0.75f), value.coerceAtLeast(0.85f))
                        }
                    }
                    .pointerInput(editingEndColor) {
                        detectDragGestures { change, _ ->
                            val newHue = (change.position.x / size.width).coerceIn(0f, 1f) * 360f
                            updateActiveFromHsv(newHue, sat.coerceAtLeast(0.75f), value.coerceAtLeast(0.85f))
                        }
                    }
            ) {
                val rainbow = listOf(
                    Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red
                )
                drawRect(brush = Brush.horizontalGradient(rainbow))
                val markerX = (hue / 360f) * size.width
                drawCircle(
                    color = Color.White,
                    radius = 12f,
                    center = Offset(markerX, size.height / 2f)
                )
            }

            // Saturation & Brightness Sliders
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Saturation", fontSize = 12.sp, modifier = Modifier.width(82.dp))
                Slider(
                    value = sat,
                    onValueChange = { updateActiveFromHsv(hue, it, value) },
                    colors = SliderDefaults.colors(thumbColor = OrangePrimary, activeTrackColor = OrangePrimary),
                    modifier = Modifier.weight(1f)
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Brightness", fontSize = 12.sp, modifier = Modifier.width(82.dp))
                Slider(
                    value = value,
                    onValueChange = { updateActiveFromHsv(hue, sat, it) },
                    colors = SliderDefaults.colors(thumbColor = OrangePrimary, activeTrackColor = OrangePrimary),
                    modifier = Modifier.weight(1f)
                )
            }

            // Hex Field + Current Swatch
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(activeTarget.toInt()))
                        .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                )
                OutlinedTextField(
                    value = hexInput,
                    onValueChange = { raw ->
                        val cleaned = raw.uppercase().filter { it in '0'..'9' || it in 'A'..'F' }.take(6)
                        hexInput = cleaned
                        if (cleaned.length == 6) {
                            val parsed = (0xFF000000L or cleaned.toLong(16))
                            if (editingEndColor) endColor = parsed else startColor = parsed
                        }
                    },
                    prefix = { Text("#") },
                    label = { Text("Hex Color") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            // Recent Colors
            if (recentColors.isNotEmpty()) {
                Text("Recent Colors • সাম্প্রতিক রঙ", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    recentColors.take(8).forEach { c ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(c.toInt()))
                                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                .clickable {
                                    if (editingEndColor) endColor = c else startColor = c
                                    hexInput = String.format("%06X", c.toInt() and 0xFFFFFF)
                                }
                        )
                    }
                }
            }

            // Studio Swatches or Gradient Presets
            Text(
                if (useGradient) "Gradient Presets" else "Studio Palette",
                style = MaterialTheme.typography.labelMedium
            )
            if (useGradient) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    gradientPresets.forEach { (s, e) ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Brush.linearGradient(listOf(Color(s.toInt()), Color(e.toInt()))))
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                                .clickable {
                                    startColor = s
                                    endColor = e
                                }
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    quickPalette.chunked(8).forEach { rowColors ->
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            rowColors.forEach { c ->
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(Color(c.toInt()))
                                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                        .clickable {
                                            startColor = c
                                            hexInput = String.format("%06X", c.toInt() and 0xFFFFFF)
                                        }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        onApplyToArtboardBg(startColor, if (useGradient) endColor else null)
                        onDismiss()
                    },
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Canvas BG", fontSize = 11.sp)
                }
                if (showStrokeOption && onApplyToStroke != null) {
                    FilledTonalButton(
                        onClick = {
                            onApplyToStroke(startColor)
                            onDismiss()
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Shape Stroke", fontSize = 11.sp)
                    }
                }
                Button(
                    onClick = {
                        onApplyToSelectionOrTool(startColor, if (useGradient) endColor else null)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        if (hasSelectedLayer) "Apply Fill" else "Use Color",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
