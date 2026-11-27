package com.example.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TextAlignMode
import com.example.model.TextLayer
import com.example.ui.theme.FontCatalog
import com.example.ui.theme.OrangePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextEditorSheet(
    existingLayer: TextLayer?,
    defaultColor: Long,
    onDismiss: () -> Unit,
    onConfirmNewText: (TextLayer) -> Unit,
    onUpdateExistingText: (TextLayer) -> Unit
) {
    val layerKey = existingLayer?.id ?: "new_text_layer"
    var text by remember(layerKey) { mutableStateOf(existingLayer?.text ?: "আমার বাংলা ডিজাইন") }
    var fontId by remember(layerKey) { mutableStateOf(existingLayer?.fontId ?: "hind_siliguri") }
    var fontSize by remember(layerKey) { mutableFloatStateOf(existingLayer?.fontSize ?: 72f) }
    var textColor by remember(layerKey) { mutableLongStateOf(existingLayer?.color ?: defaultColor) }
    var bold by remember(layerKey) { mutableStateOf(existingLayer?.bold ?: false) }
    var italic by remember(layerKey) { mutableStateOf(existingLayer?.italic ?: false) }
    var align by remember(layerKey) { mutableStateOf(existingLayer?.align ?: TextAlignMode.CENTER) }
    var shadowEnabled by remember(layerKey) { mutableStateOf(existingLayer?.shadowEnabled ?: false) }
    var strokeWidth by remember(layerKey) { mutableFloatStateOf(existingLayer?.strokeWidth ?: 0f) }
    var strokeColor by remember(layerKey) { mutableLongStateOf(existingLayer?.strokeColor ?: 0xFFFFFFFFL) }
    var fontFilter by remember { mutableStateOf("All") }

    val selectedFontOption = remember(fontId) { FontCatalog.findById(fontId) }
    val fontCategories = listOf("All", "Bangla", "English", "Calligraphy", "Display", "Mono")
    val filteredFonts = remember(fontFilter) {
        if (fontFilter == "All") FontCatalog.fonts else FontCatalog.fonts.filter { it.category == fontFilter }
    }

    val colorSwatches = listOf(
        0xFF1E1E1EL, 0xFFFFFFFFL, 0xFFFF7A00L, 0xFFD4AF37L,
        0xFF0F5132L, 0xFFDC2626L, 0xFF2563EBL, 0xFFFACC15L
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
                Text(
                    if (existingLayer == null) "Add Bangla / English Text" else "Edit Text Style",
                    style = MaterialTheme.typography.titleLarge
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Enter Bangla or English Text • টেক্সট লিখুন") },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("text_editor_input")
            )

            // Live Font Preview Box
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, OrangePrimary.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Preview • ${selectedFontOption.displayName}",
                        fontSize = 10.sp,
                        color = OrangePrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = text.ifBlank { "Arfin Illustrator" },
                        fontFamily = FontCatalog.composeFontFamily(selectedFontOption),
                        fontWeight = if (bold || selectedFontOption.defaultBold) FontWeight.Bold else FontWeight.Normal,
                        fontStyle = if (italic || selectedFontOption.defaultItalic) FontStyle.Italic else FontStyle.Normal,
                        fontSize = 20.sp,
                        color = if (textColor == 0xFF1E1E1EL) MaterialTheme.colorScheme.onSurface else Color(textColor.toInt()),
                        maxLines = 2
                    )
                }
            }

            // Quick Bangla Phrases + 1-Tap Bangla Digit Converter (০-৯)
            val bdPhrases = remember {
                listOf(
                    "বিসমিল্লাহির রাহমানির রাহিম",
                    "আসসালামু আলাইকুম",
                    "শুভ উদ্বোধন",
                    "ভর্তি চলছে",
                    "বিশেষ ছাড়! ৫০% অফ",
                    "ঈদ মোবারক",
                    "জুম্মা মোবারক",
                    "শুভ নববর্ষ",
                    "সকলকে আমন্ত্রণ",
                    "প্রোঃ মোঃ ...",
                    "যোগাযোগ: ০১...",
                    "৳ ৫০০/-",
                    "২১শে ফেব্রুয়ারি",
                    "মহান বিজয় দিবস"
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("দ্রুত বাংলা বাক্য (Quick Phrases)", fontSize = 11.sp, color = OrangePrimary, fontWeight = FontWeight.SemiBold)
                TextButton(
                    onClick = {
                        val bnDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
                        text = buildString {
                            text.forEach { ch ->
                                if (ch in '0'..'9') append(bnDigits[ch - '0']) else append(ch)
                            }
                        }
                    },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("0-9 → ০-৯ বাংলা সংখ্যা", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OrangePrimary)
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                bdPhrases.forEach { phrase ->
                    AssistChip(
                        onClick = { text = phrase },
                        label = { Text(phrase, fontSize = 11.sp) }
                    )
                }
            }

            // Style & Alignment Toggles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = bold,
                    onClick = { bold = !bold },
                    label = { Text("B", fontWeight = FontWeight.Bold) }
                )
                FilterChip(
                    selected = italic,
                    onClick = { italic = !italic },
                    label = { Text("I", fontStyle = FontStyle.Italic) }
                )
                FilterChip(
                    selected = shadowEnabled,
                    onClick = { shadowEnabled = !shadowEnabled },
                    label = { Text("Shadow") }
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { align = TextAlignMode.LEFT }) {
                    Icon(
                        Icons.Default.FormatAlignLeft,
                        contentDescription = "Align Left",
                        tint = if (align == TextAlignMode.LEFT) OrangePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { align = TextAlignMode.CENTER }) {
                    Icon(
                        Icons.Default.FormatAlignCenter,
                        contentDescription = "Align Center",
                        tint = if (align == TextAlignMode.CENTER) OrangePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { align = TextAlignMode.RIGHT }) {
                    Icon(
                        Icons.Default.FormatAlignRight,
                        contentDescription = "Align Right",
                        tint = if (align == TextAlignMode.RIGHT) OrangePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Font Size Slider
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Size: ${fontSize.toInt()}px", fontSize = 12.sp, modifier = Modifier.width(86.dp))
                Slider(
                    value = fontSize,
                    onValueChange = { fontSize = it },
                    valueRange = 18f..220f,
                    colors = SliderDefaults.colors(thumbColor = OrangePrimary, activeTrackColor = OrangePrimary),
                    modifier = Modifier.weight(1f)
                )
            }

            // Outline Stroke Slider
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Outline: ${strokeWidth.toInt()}", fontSize = 12.sp, modifier = Modifier.width(86.dp))
                Slider(
                    value = strokeWidth,
                    onValueChange = { strokeWidth = it },
                    valueRange = 0f..24f,
                    colors = SliderDefaults.colors(thumbColor = OrangePrimary, activeTrackColor = OrangePrimary),
                    modifier = Modifier.weight(1f)
                )
            }

            // Quick Text Colors
            Text("Text Color", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                colorSwatches.forEach { c ->
                    val active = textColor == c
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(c.toInt()))
                            .border(
                                width = if (active) 3.dp else 1.dp,
                                color = if (active) OrangePrimary else MaterialTheme.colorScheme.outline,
                                shape = CircleShape
                            )
                            .clickable { textColor = c }
                    )
                }
            }

            // 50+ Font Picker
            Text("Font Family (${FontCatalog.fonts.size} Bangla & English Fonts)", style = MaterialTheme.typography.labelMedium, color = OrangePrimary)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                fontCategories.forEach { cat ->
                    FilterChip(
                        selected = fontFilter == cat,
                        onClick = { fontFilter = cat },
                        label = { Text(cat, fontSize = 12.sp) }
                    )
                }
            }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(filteredFonts, key = { it.id }) { opt ->
                    val selected = fontId == opt.id
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (selected) OrangePrimary.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .width(156.dp)
                            .border(
                                width = if (selected) 1.5.dp else 1.dp,
                                color = if (selected) OrangePrimary else MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { fontId = opt.id }
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = opt.sampleText,
                                fontFamily = FontCatalog.composeFontFamily(opt),
                                fontWeight = if (opt.defaultBold) FontWeight.Bold else FontWeight.Normal,
                                fontStyle = if (opt.defaultItalic) FontStyle.Italic else FontStyle.Normal,
                                fontSize = 15.sp,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = opt.displayName,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Button(
                onClick = {
                    val safeText = text.ifBlank { "Arfin Illustrator" }
                    if (existingLayer != null) {
                        onUpdateExistingText(
                            existingLayer.copy(
                                name = safeText.take(18),
                                text = safeText,
                                fontId = fontId,
                                fontSize = fontSize,
                                color = textColor,
                                bold = bold,
                                italic = italic,
                                align = align,
                                shadowEnabled = shadowEnabled,
                                strokeWidth = strokeWidth,
                                strokeColor = strokeColor
                            )
                        )
                    } else {
                        onConfirmNewText(
                            TextLayer(
                                name = safeText.take(18),
                                text = safeText,
                                fontId = fontId,
                                fontSize = fontSize,
                                color = textColor,
                                bold = bold,
                                italic = italic,
                                align = align,
                                shadowEnabled = shadowEnabled,
                                strokeWidth = strokeWidth,
                                strokeColor = strokeColor
                            )
                        )
                    }
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("apply_text_btn")
            ) {
                Text(
                    if (existingLayer == null) "Add Text to Artboard" else "Apply Text Changes",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
