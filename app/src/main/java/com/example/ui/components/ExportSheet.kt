package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.ExportManager
import com.example.model.CanvasDocument
import com.example.model.ExportFormat
import com.example.model.ExportResolution
import com.example.ui.theme.OrangePrimary
import com.example.ui.theme.SuccessGreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportSheet(
    document: CanvasDocument,
    defaultResolution: ExportResolution,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedFormat by remember { mutableStateOf(ExportFormat.PNG) }
    var selectedResolution by remember { mutableStateOf(defaultResolution) }
    var transparentBg by remember { mutableStateOf(false) }
    var isExporting by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf<String?>(null) }

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
                Column {
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
            Text("Resolution • রেজোলিউশন", style = MaterialTheme.typography.labelMedium, color = OrangePrimary)
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExportResolution.entries.forEach { res ->
                    val selected = selectedResolution == res
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
                            .clickable { selectedResolution = res }
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
                            onClick = { selectedResolution = res },
                            colors = RadioButtonDefaults.colors(selectedColor = OrangePrimary)
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

            Button(
                onClick = {
                    scope.launch {
                        isExporting = true
                        val res = ExportManager.exportToGalleryOrDownloads(
                            context = context,
                            document = document,
                            format = selectedFormat,
                            resolution = selectedResolution,
                            transparentBackground = transparentBg && selectedFormat.supportsTransparency
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
                    Text("Rendering ${selectedResolution.label}...")
                } else {
                    Icon(Icons.Default.Download, contentDescription = "Save")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save to Gallery & Device", fontWeight = FontWeight.Bold)
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
                                    context, document, selectedFormat, selectedResolution,
                                    transparentBg && selectedFormat.supportsTransparency
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
