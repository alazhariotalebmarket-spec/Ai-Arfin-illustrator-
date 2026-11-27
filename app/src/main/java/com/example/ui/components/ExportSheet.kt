package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.CanvasRenderer
import com.example.engine.ExportManager
import com.example.model.CanvasDocument
import com.example.model.CustomSizeChartCatalog
import com.example.model.ExportFormat
import com.example.model.ExportResolution
import com.example.model.SizeUnit
import com.example.ui.theme.OrangePrimary
import com.example.ui.theme.SuccessGreen
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportSheet(
    document: CanvasDocument,
    defaultResolution: ExportResolution,
    defaultTransparentBg: Boolean = false,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedFormat by remember { mutableStateOf(ExportFormat.PNG) }
    var selectedResolution by remember(defaultTransparentBg) {
        mutableStateOf(
            if (defaultTransparentBg && defaultResolution.multiplierOrTargetMax < 3840) ExportResolution.RES_4K
            else defaultResolution
        )
    }
    var useCustomResolution by remember { mutableStateOf(false) }

    val baseExportW = max(document.artboardWidth, document.targetExportWidth)
    val baseExportH = max(document.artboardHeight, document.targetExportHeight)
    val aspectRatio = remember(document.id, document.artboardWidth, document.artboardHeight) {
        document.artboardWidth.toFloat() / max(1, document.artboardHeight).toFloat()
    }

    var customUnit by remember { mutableStateOf(SizeUnit.PIXEL) }
    var customDpi by remember { mutableIntStateOf(document.dpi.coerceIn(72, 300)) }
    var customWidthInput by remember(document.id) { mutableStateOf(baseExportW.toString()) }
    var customHeightInput by remember(document.id) { mutableStateOf(baseExportH.toString()) }
    var lockAspectRatio by remember { mutableStateOf(true) }
    var stretchToFit by remember { mutableStateOf(false) }

    var transparentBg by remember(document.id, defaultTransparentBg) { mutableStateOf(defaultTransparentBg) }
    var isExporting by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf<String?>(null) }

    fun formatValueForUnit(valFloat: Float, unit: SizeUnit): String {
        return if (unit == SizeUnit.PIXEL) {
            valFloat.roundToInt().coerceAtLeast(1).toString()
        } else {
            String.format(java.util.Locale.US, "%.2f", valFloat).trimEnd('0').trimEnd('.')
        }
    }

    val rawCustomW = CustomSizeChartCatalog.convertToPixels(
        customWidthInput.toFloatOrNull()?.coerceAtLeast(0.1f) ?: baseExportW.toFloat(),
        customUnit,
        customDpi
    )
    val rawCustomH = CustomSizeChartCatalog.convertToPixels(
        customHeightInput.toFloatOrNull()?.coerceAtLeast(0.1f) ?: baseExportH.toFloat(),
        customUnit,
        customDpi
    )
    val (safeCustomW, safeCustomH) = remember(rawCustomW, rawCustomH) {
        CanvasRenderer.resolveSafeExportDimensions(rawCustomW, rawCustomH)
    }
    val customWasCapped = rawCustomW != safeCustomW || rawCustomH != safeCustomH
    val customSupportedFormat = selectedFormat == ExportFormat.PNG ||
        selectedFormat == ExportFormat.JPG ||
        selectedFormat == ExportFormat.WEBP ||
        selectedFormat == ExportFormat.PDF

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Export & Share • এক্সপোর্ট", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "${document.name} (${document.artboardWidth}×${document.artboardHeight})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text("File Format • ফরম্যাট", style = MaterialTheme.typography.labelMedium, color = OrangePrimary)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExportFormat.entries.take(3).forEach { fmt ->
                    FormatPill(
                        label = fmt.label,
                        selected = selectedFormat == fmt,
                        onClick = { selectedFormat = fmt },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExportFormat.entries.drop(3).forEach { fmt ->
                    FormatPill(
                        label = fmt.label,
                        selected = selectedFormat == fmt,
                        onClick = { selectedFormat = fmt },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Resolution • রেজুলেশন", style = MaterialTheme.typography.labelMedium, color = OrangePrimary)
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExportResolution.entries.forEach { res ->
                    val selected = !useCustomResolution && selectedResolution == res
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selected) OrangePrimary.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant)
                            .border(
                                width = if (selected) 1.5.dp else 1.dp,
                                color = if (selected) OrangePrimary else MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                useCustomResolution = false
                                selectedResolution = res
                            }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(res.label, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(res.subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        RadioButton(
                            selected = selected,
                            onClick = {
                                useCustomResolution = false
                                selectedResolution = res
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = OrangePrimary)
                        )
                    }
                }

                // Custom Resolution Option Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (useCustomResolution) OrangePrimary.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant)
                        .border(
                            width = if (useCustomResolution) 1.5.dp else 1.dp,
                            color = if (useCustomResolution) OrangePrimary else MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { useCustomResolution = true }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Custom • কাস্টম রেজুলেশন", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                "নিজের মতো Width × Height (Pixel / Inch / Cm) দিয়ে সেভ করুন",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        RadioButton(
                            selected = useCustomResolution,
                            onClick = { useCustomResolution = true },
                            colors = RadioButtonDefaults.colors(selectedColor = OrangePrimary)
                        )
                    }

                    if (useCustomResolution) {
                        // Unit selector (Pixel / Inch / Cm)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(SizeUnit.PIXEL, SizeUnit.INCH, SizeUnit.CM).forEach { unit ->
                                FilterChip(
                                    selected = customUnit == unit,
                                    onClick = {
                                        if (customUnit != unit) {
                                            val curPxW = rawCustomW.toFloat()
                                            val curPxH = rawCustomH.toFloat()
                                            customUnit = unit
                                            val (newW, newH) = when (unit) {
                                                SizeUnit.PIXEL -> curPxW to curPxH
                                                SizeUnit.INCH -> (curPxW / customDpi) to (curPxH / customDpi)
                                                SizeUnit.CM -> (curPxW / customDpi * 2.54f) to (curPxH / customDpi * 2.54f)
                                                SizeUnit.FEET -> (curPxW / (12f * customDpi)) to (curPxH / (12f * customDpi))
                                            }
                                            customWidthInput = formatValueForUnit(newW, unit)
                                            customHeightInput = formatValueForUnit(newH, unit)
                                        }
                                    },
                                    label = { Text(unit.label, fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        if (customUnit != SizeUnit.PIXEL) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("DPI:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                listOf(72, 150, 300).forEach { dpiOpt ->
                                    FilterChip(
                                        selected = customDpi == dpiOpt,
                                        onClick = { customDpi = dpiOpt },
                                        label = { Text("$dpiOpt", fontSize = 11.sp) }
                                    )
                                }
                            }
                        }

                        // Width × Height + Aspect Lock
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = customWidthInput,
                                onValueChange = { next ->
                                    customWidthInput = next
                                    if (lockAspectRatio) {
                                        val wVal = next.toFloatOrNull()
                                        if (wVal != null && wVal > 0f) {
                                            customHeightInput = formatValueForUnit(wVal / aspectRatio, customUnit)
                                        }
                                    }
                                },
                                label = { Text("Width (${customUnit.label})", fontSize = 11.sp) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = if (customUnit == SizeUnit.PIXEL) KeyboardType.Number else KeyboardType.Decimal
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("export_custom_width")
                            )

                            IconButton(
                                onClick = { lockAspectRatio = !lockAspectRatio },
                                modifier = Modifier.size(44.dp)
                            ) {
                                Icon(
                                    imageVector = if (lockAspectRatio) Icons.Default.Lock else Icons.Default.LockOpen,
                                    contentDescription = if (lockAspectRatio) "Aspect Locked" else "Aspect Unlocked",
                                    tint = if (lockAspectRatio) OrangePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            OutlinedTextField(
                                value = customHeightInput,
                                onValueChange = { next ->
                                    customHeightInput = next
                                    if (lockAspectRatio) {
                                        val hVal = next.toFloatOrNull()
                                        if (hVal != null && hVal > 0f) {
                                            customWidthInput = formatValueForUnit(hVal * aspectRatio, customUnit)
                                        }
                                    }
                                },
                                label = { Text("Height (${customUnit.label})", fontSize = 11.sp) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = if (customUnit == SizeUnit.PIXEL) KeyboardType.Number else KeyboardType.Decimal
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("export_custom_height")
                            )
                        }

                        // Quick Custom Pixel Presets
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val quickSizes = listOf(
                                "1080×1080" to (1080 to 1080),
                                "1920×1080" to (1920 to 1080),
                                "2560×1440" to (2560 to 1440),
                                "3000×3000" to (3000 to 3000),
                                "4000×4000" to (4000 to 4000),
                                "6000×4000" to (6000 to 4000)
                            )
                            quickSizes.forEach { (lbl, dims) ->
                                AssistChip(
                                    onClick = {
                                        customUnit = SizeUnit.PIXEL
                                        lockAspectRatio = false
                                        customWidthInput = dims.first.toString()
                                        customHeightInput = dims.second.toString()
                                    },
                                    label = { Text(lbl, fontSize = 10.sp) }
                                )
                            }
                        }

                        if (!lockAspectRatio) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (stretchToFit) "Mode: Stretch to Fill" else "Mode: Fit Cleanly (No Distortion)",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                TextButton(
                                    onClick = { stretchToFit = !stretchToFit },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                ) {
                                    Text(if (stretchToFit) "Switch to Fit" else "Switch to Stretch", fontSize = 11.sp, color = OrangePrimary)
                                }
                            }
                        }

                        val mp = (safeCustomW.toLong() * safeCustomH.toLong()) / 1_000_000.0
                        Text(
                            text = if (customWasCapped) {
                                "Output: ${safeCustomW} × ${safeCustomH} px (RAM-safe max ${String.format(java.util.Locale.US, "%.1f", mp)} MP)"
                            } else {
                                "Final Output: ${safeCustomW} × ${safeCustomH} px (${String.format(java.util.Locale.US, "%.1f", mp)} MP)"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OrangePrimary
                        )
                    }
                }
            }

            if (selectedFormat.supportsTransparency) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Transparent Background", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("ব্যাকগ্রাউন্ড ছাড়া সেভ (PNG / SVG / WEBP)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = transparentBg,
                        onCheckedChange = { transparentBg = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = OrangePrimary)
                    )
                }
            }

            if (resultMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = SuccessGreen.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = resultMessage!!,
                        color = SuccessGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            val activeCustomW = if (useCustomResolution && customSupportedFormat) safeCustomW else null
            val activeCustomH = if (useCustomResolution && customSupportedFormat) safeCustomH else null
            val activeStretch = useCustomResolution && !lockAspectRatio && stretchToFit

            Button(
                onClick = {
                    scope.launch {
                        isExporting = true
                        val res = ExportManager.exportToGalleryOrDownloads(
                            context = context,
                            document = document,
                            format = selectedFormat,
                            resolution = selectedResolution,
                            transparentBackground = transparentBg && selectedFormat.supportsTransparency,
                            customWidthPx = activeCustomW,
                            customHeightPx = activeCustomH,
                            stretchToFit = activeStretch
                        )
                        isExporting = false
                        resultMessage = res.message
                    }
                },
                enabled = !isExporting,
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("export_save_gallery_btn")
            ) {
                if (isExporting) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    val resTag = if (activeCustomW != null && activeCustomH != null) "${activeCustomW}×${activeCustomH}" else selectedResolution.label
                    Text("Rendering $resTag...")
                } else {
                    Icon(Icons.Default.Download, contentDescription = "Save")
                    Spacer(modifier = Modifier.width(8.dp))
                    val saveLabel = if (activeCustomW != null && activeCustomH != null) {
                        "Save (${activeCustomW}×${activeCustomH}px)"
                    } else {
                        "Save to Gallery & Device"
                    }
                    Text(saveLabel, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text("Direct Share • শেয়ার করুন", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val shareTargets = listOf(
                    "WhatsApp" to "com.whatsapp",
                    "Facebook" to "com.facebook.katana",
                    "Messenger" to "com.facebook.orca",
                    "Share All" to null
                )
                shareTargets.forEach { (label, pkg) ->
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                isExporting = true
                                val res = ExportManager.prepareShareUri(
                                    context = context,
                                    document = document,
                                    format = selectedFormat,
                                    resolution = selectedResolution,
                                    transparentBackground = transparentBg && selectedFormat.supportsTransparency,
                                    customWidthPx = activeCustomW,
                                    customHeightPx = activeCustomH,
                                    stretchToFit = activeStretch
                                )
                                isExporting = false
                                res.shareUri?.let { uri ->
                                    ExportManager.launchShareIntent(context, uri, selectedFormat.mimeType, pkg)
                                }
                            }
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (pkg == null) {
                            Icon(Icons.Default.Share, contentDescription = label, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(label, fontSize = 11.sp, maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
private fun FormatPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) OrangePrimary else MaterialTheme.colorScheme.surfaceVariant)
            .border(
                1.dp,
                if (selected) OrangePrimary else MaterialTheme.colorScheme.outline,
                RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}
