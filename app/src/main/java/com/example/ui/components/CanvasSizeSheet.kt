package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ArtboardPreset
import com.example.ui.theme.OrangePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CanvasSizeSheet(
    title: String = "New Design • নতুন ডিজাইন",
    initialName: String = "My Design",
    initialWidth: Int = 1080,
    initialHeight: Int = 1080,
    initialCategory: String = "Blank Canvas",
    showNameAndBg: Boolean = true,
    onDismiss: () -> Unit,
    onConfirm: (name: String, width: Int, height: Int, category: String, bgColor: Long) -> Unit
) {
    var designName by remember { mutableStateOf(initialName) }
    var selectedPreset by remember {
        mutableStateOf(
            ArtboardPreset.entries.find { it.width == initialWidth && it.height == initialHeight }
                ?: ArtboardPreset.CUSTOM
        )
    }
    var customW by remember { mutableStateOf(initialWidth.toString()) }
    var customH by remember { mutableStateOf(initialHeight.toString()) }
    var selectedBg by remember { mutableLongStateOf(0xFFFFFFFFL) }

    val bgOptions = listOf(
        0xFFFFFFFFL, 0xFF1E1E1EL, 0xFF0B3B2EL, 0xFF121C38L, 0xFFFFF8F2L, 0xFFFF7A00L
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.titleLarge)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            if (showNameAndBg) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = designName,
                    onValueChange = { designName = it },
                    label = { Text("Project Name • প্রজেক্টের নাম") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_design_name_input")
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text("Artboard Size • ক্যানভাস সাইজ", style = MaterialTheme.typography.labelMedium, color = OrangePrimary)
            Spacer(modifier = Modifier.height(8.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.heightIn(max = 260.dp)
            ) {
                items(ArtboardPreset.entries) { preset ->
                    val isSelected = selectedPreset == preset
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) OrangePrimary.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) OrangePrimary else MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable {
                                selectedPreset = preset
                                if (preset != ArtboardPreset.CUSTOM) {
                                    customW = preset.width.toString()
                                    customH = preset.height.toString()
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                Icons.Default.AspectRatio,
                                contentDescription = preset.label,
                                tint = if (isSelected) OrangePrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    preset.label,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    maxLines = 1
                                )
                                Text(
                                    "${preset.width}×${preset.height}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = customW,
                    onValueChange = {
                        customW = it.filter { ch -> ch.isDigit() }.take(4)
                        selectedPreset = ArtboardPreset.CUSTOM
                    },
                    label = { Text("Width (px)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = customH,
                    onValueChange = {
                        customH = it.filter { ch -> ch.isDigit() }.take(4)
                        selectedPreset = ArtboardPreset.CUSTOM
                    },
                    label = { Text("Height (px)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            if (showNameAndBg) {
                Spacer(modifier = Modifier.height(14.dp))
                Text("Artboard Background", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    bgOptions.forEach { bg ->
                        val active = selectedBg == bg
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(bg.toInt()))
                                .border(
                                    width = if (active) 3.dp else 1.dp,
                                    color = if (active) OrangePrimary else MaterialTheme.colorScheme.outline,
                                    shape = CircleShape
                                )
                                .clickable { selectedBg = bg }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = {
                    val w = (customW.toIntOrNull() ?: 1080).coerceIn(100, 4096)
                    val h = (customH.toIntOrNull() ?: 1080).coerceIn(100, 4096)
                    val cat = if (selectedPreset != ArtboardPreset.CUSTOM) selectedPreset.category else initialCategory
                    onConfirm(designName, w, h, cat, selectedBg)
                },
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("confirm_canvas_size_btn")
            ) {
                Text(
                    if (showNameAndBg) "Create Canvas • শুরু করুন" else "Apply Size",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}
