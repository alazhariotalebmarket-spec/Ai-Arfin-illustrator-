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
import com.example.model.GradientType
import com.example.model.StrokeStyle
import com.example.ui.theme.OrangePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorPickerSheet(
    initialColor: Long,
    initialGradientMid: Long?,
    initialGradientEnd: Long?,
    initialGradientType: GradientType,
    initialGradientAngle: Float,
    initialStrokeWidth: Float,
    initialStrokeStyle: StrokeStyle,
    recentColors: List<Long>,
    hasSelectedLayer: Boolean,
    showStrokeOption: Boolean = false,
    onDismiss: () -> Unit,
    onApplyToSelectionOrTool: (
        color: Long,
        gradientEnd: Long?,
        gradientMid: Long?,
        gradientType: GradientType,
        gradientAngle: Float
    ) -> Unit,
    onApplyToStroke: ((color: Long, width: Float, style: StrokeStyle) -> Unit)? = null,
    onApplyToArtboardBg: (
        color: Long,
        gradientEnd: Long?,
        gradientMid: Long?,
        gradientType: GradientType
    ) -> Unit,
    onActivateEyedropper: () -> Unit
) {
    var useGradient by remember { mutableStateOf(initialGradientEnd != null) }
    var useThreeColor by remember { mutableStateOf(initialGradientMid != null) }
    var gradientType by remember { mutableStateOf(initialGradientType) }
    var gradientAngle by remember { mutableFloatStateOf(initialGradientAngle) }
    var editingSlot by remember { mutableIntStateOf(0) } // 0 = Start, 1 = Mid, 2 = End

    var startColor by remember { mutableLongStateOf(initialColor) }
    var midColor by remember { mutableLongStateOf(initialGradientMid ?: 0xFFFACC15L) }
    var endColor by remember { mutableLongStateOf(initialGradientEnd ?: 0xFFCC4400L) }

    var strokeWidth by remember { mutableFloatStateOf(initialStrokeWidth.coerceAtLeast(4f)) }
    var strokeStyle by remember { mutableStateOf(initialStrokeStyle) }

    val activeTarget = when (editingSlot) {
        1 -> midColor
        2 -> endColor
        else -> startColor
    }
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

    fun setSlotColor(c: Long) {
        when (editingSlot) {
            1 -> midColor = c
            2 -> endColor = c
            else -> startColor = c
        }
    }

    fun updateActiveFromHsv(h: Float, s: Float, v: Float) {
        hue = h
        sat = s
        value = v
        val c = AndroidColor.HSVToColor(floatArrayOf(h, s, v)).toLong() and 0xFFFFFFFFL
        setSlotColor(c)
        hexInput = String.format("%06X", c.toInt() and 0xFFFFFF)
    }

    val quickPalette = listOf(
        0xFFFF7A00L, 0xFFCC4400L, 0xFF1E1E1EL, 0xFFFFFFFFL,
        0xFFD4AF37L, 0xFF0F5132L, 0xFF121C38L, 0xFFDC2626L,
        0xFF2563EBL, 0xFF10B981L, 0xFF7C3AEDL, 0xFFEC4899L,
        0xFFFACC15L, 0xFF06B6D4L, 0xFF64748BL, 0xFF000000L
    )

    val gradientPresets = listOf(
        listOf(0xFFFF7A00L, 0xFFFACC15L, 0xFFCC4400L),
        listOf(0xFF0F5132L, 0xFF10B981L, 0xFF041F17L),
        listOf(0xFF121C38L, 0xFF4F46E5L, 0xFF312E81L),
        listOf(0xFFEC4899L, 0xFF9333EAL, 0xFF7C3AEDL),
        listOf(0xFFD4AF37L, 0xFFFDE047L, 0xFF92400EL),
        listOf(0xFF06B6D4L, 0xFF3B82F6L, 0xFF1D4ED8L)
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
                Text("Color, Gradient & Stroke", style = MaterialTheme.typography.titleLarge)
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
                        editingSlot = 0
                    },
                    label = { Text("Solid Color") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = useGradient,
                    onClick = { useGradient = true },
                    label = { Text("Gradient (Linear/Radial)") },
                    modifier = Modifier.weight(1f)
                )
            }

            // Gradient controls
            if (useGradient) {
                val previewStops = if (useThreeColor) {
                    listOf(Color(startColor.toInt()), Color(midColor.toInt()), Color(endColor.toInt()))
                } else {
                    listOf(Color(startColor.toInt()), Color(endColor.toInt()))
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (gradientType == GradientType.RADIAL) Brush.radialGradient(previewStops)
                            else Brush.horizontalGradient(previewStops)
                        )
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = gradientType == GradientType.LINEAR,
                        onClick = { gradientType = GradientType.LINEAR },
                        label = { Text("Linear", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = gradientType == GradientType.RADIAL,
                        onClick = { gradientType = GradientType.RADIAL },
                        label = { Text("Radial", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = useThreeColor,
                        onClick = {
                            useThreeColor = !useThreeColor
                            if (!useThreeColor && editingSlot == 1) editingSlot = 0
                        },
                        label = { Text(if (useThreeColor) "3 Colors" else "2 Colors", fontSize = 11.sp) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { editingSlot = 0 },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (editingSlot == 0) OrangePrimary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (editingSlot == 0) Color.White else MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.weight(1f)
                    ) { Text("Start", fontSize = 11.sp) }

                    if (useThreeColor) {
                        Button(
                            onClick = { editingSlot = 1 },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (editingSlot == 1) OrangePrimary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (editingSlot == 1) Color.White else MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.weight(1f)
                        ) { Text("Mid", fontSize = 11.sp) }
                    }

                    Button(
                        onClick = { editingSlot = 2 },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (editingSlot == 2) OrangePrimary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (editingSlot == 2) Color.White else MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.weight(1f)
                    ) { Text("End", fontSize = 11.sp) }
                }

                if (gradientType == GradientType.LINEAR) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Angle: ${gradientAngle.toInt()}°", fontSize = 11.sp, modifier = Modifier.width(86.dp))
                        Slider(
                            value = gradientAngle,
                            onValueChange = { gradientAngle = it },
                            valueRange = 0f..360f,
                            colors = SliderDefaults.colors(thumbColor = OrangePrimary, activeTrackColor = OrangePrimary),
                            modifier = Modifier.weight(1f)
                        )
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
                    .pointerInput(editingSlot) {
                        detectTapGestures { offset ->
                            val newHue = (offset.x / size.width).coerceIn(0f, 1f) * 360f
                            updateActiveFromHsv(newHue, sat.coerceAtLeast(0.75f), value.coerceAtLeast(0.85f))
                        }
                    }
                    .pointerInput(editingSlot) {
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
                drawCircle(color = Color.White, radius = 12f, center = Offset(markerX, size.height / 2f))
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

            // Hex Field
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
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
                            setSlotColor(parsed)
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
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(Color(c.toInt()))
                                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                .clickable {
                                    setSlotColor(c)
                                    hexInput = String.format("%06X", c.toInt() and 0xFFFFFF)
                                }
                        )
                    }
                }
            }

            // Palette / Gradient Presets
            Text(if (useGradient) "Gradient Presets" else "Studio Palette", style = MaterialTheme.typography.labelMedium)
            if (useGradient) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    gradientPresets.forEach { cols ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Brush.linearGradient(cols.map { Color(it.toInt()) }))
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                                .clickable {
                                    startColor = cols[0]
                                    midColor = cols[1]
                                    endColor = cols[2]
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
                                            setSlotColor(c)
                                            hexInput = String.format("%06X", c.toInt() and 0xFFFFFF)
                                        }
                                )
                            }
                        }
                    }
                }
            }

            // Stroke Width & Dashed Line Options when Shape / Path is active
            if (showStrokeOption && onApplyToStroke != null) {
                HorizontalDivider()
                Text("Stroke Style & Width", style = MaterialTheme.typography.labelMedium, color = OrangePrimary)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Width: ${strokeWidth.toInt()}px", fontSize = 11.sp, modifier = Modifier.width(86.dp))
                    Slider(
                        value = strokeWidth,
                        onValueChange = { strokeWidth = it },
                        valueRange = 0f..32f,
                        colors = SliderDefaults.colors(thumbColor = OrangePrimary, activeTrackColor = OrangePrimary),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StrokeStyle.entries.forEach { st ->
                        FilterChip(
                            selected = strokeStyle == st,
                            onClick = { strokeStyle = st },
                            label = { Text(st.label, fontSize = 11.sp) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        onApplyToArtboardBg(
                            startColor,
                            if (useGradient) endColor else null,
                            if (useGradient && useThreeColor) midColor else null,
                            gradientType
                        )
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
                            onApplyToStroke(startColor, strokeWidth, strokeStyle)
                            onDismiss()
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Apply Stroke", fontSize = 11.sp)
                    }
                }
                Button(
                    onClick = {
                        onApplyToSelectionOrTool(
                            startColor,
                            if (useGradient) endColor else null,
                            if (useGradient && useThreeColor) midColor else null,
                            gradientType,
                            gradientAngle
                        )
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
