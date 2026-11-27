package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.SwapHoriz
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
import com.example.model.CustomSizeChartCatalog
import com.example.model.SizeChartEntry
import com.example.model.SizeUnit
import com.example.ui.theme.OrangePrimary
import com.example.ui.theme.SuccessGreen

@Composable
fun CustomSizeChartSection(
    onSelectChartSize: (entry: SizeChartEntry) -> Unit,
    onCreateCustomSize: (widthPx: Int, heightPx: Int, dpi: Int, label: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedChartTab by remember { mutableIntStateOf(0) } // 0 = Inch, 1 = Feet
    val activeChart = if (selectedChartTab == 0) CustomSizeChartCatalog.inchChart else CustomSizeChartCatalog.feetChart

    // Custom Size Box State
    var customWidthText by remember { mutableStateOf("1") }
    var customHeightText by remember { mutableStateOf("2") }
    var selectedUnit by remember { mutableStateOf(SizeUnit.FEET) }
    var selectedDpi by remember { mutableIntStateOf(300) }

    val computedW = remember(customWidthText, selectedUnit, selectedDpi) {
        val v = customWidthText.toFloatOrNull() ?: 1f
        CustomSizeChartCatalog.convertToPixels(v, selectedUnit, selectedDpi)
    }
    val computedH = remember(customHeightText, selectedUnit, selectedDpi) {
        val v = customHeightText.toFloatOrNull() ?: 1f
        CustomSizeChartCatalog.convertToPixels(v, selectedUnit, selectedDpi)
    }
    val isSmartBanner = maxOf(computedW, computedH) > 1800

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Custom Size Chart • সাইজ চার্ট", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Tap any Inch / Feet size (300 DPI) • Smart Mode enabled",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(
                color = SuccessGreen.copy(alpha = 0.16f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "300 DPI",
                    color = SuccessGreen,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // Inch vs Feet Chart Switcher
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ChartTabButton(
                label = "INCH CHART (1\"–20\")",
                selected = selectedChartTab == 0,
                onClick = { selectedChartTab = 0 },
                modifier = Modifier
                    .weight(1f)
                    .testTag("inch_chart_tab")
            )
            ChartTabButton(
                label = "FEET CHART (1'–20')",
                selected = selectedChartTab == 1,
                onClick = { selectedChartTab = 1 },
                modifier = Modifier
                    .weight(1f)
                    .testTag("feet_chart_tab")
            )
        }

        // Side-by-side Inch/Feet + Pixel Size (300 DPI) list
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        if (selectedChartTab == 0) "Inch Size" else "Feet Size",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = OrangePrimary
                    )
                    Text(
                        "Pixel Size (300 DPI)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = OrangePrimary
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

                activeChart.forEach { entry ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                            .clickable { onSelectChartSize(entry) }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                            .testTag("chart_row_${entry.physicalLabel}"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.AspectRatio,
                                contentDescription = entry.physicalLabel,
                                tint = OrangePrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = entry.physicalLabel,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                        Text(
                            text = entry.pixelLabel,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Custom Size Box (Width + Height + Unit + DPI + Create)
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, OrangePrimary.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Custom Size Box • কাস্টম সাইজ", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    if (isSmartBanner) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(14.dp))
                            Text("Smart Mode", fontSize = 11.sp, color = OrangePrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Popular Bangladesh Print & Business Sizes (1-Tap)
                val bdPrintPresets = remember {
                    listOf(
                        Triple("ভিজিটিং কার্ড 3.5×2\"", 1050 to 600, 300),
                        Triple("ক্যাশ মেমো A5", 1748 to 2480, 300),
                        Triple("দাওয়াত কার্ড 5×7\"", 1500 to 2100, 300),
                        Triple("ফেস্টুন 2×4 ft", 7200 to 14400, 300),
                        Triple("সাইনবোর্ড 6×2 ft", 21600 to 7200, 300),
                        Triple("ব্যানার 8×4 ft", 28800 to 14400, 300),
                        Triple("ফেসবুক কভার", 1640 to 624, 300)
                    )
                }
                Text("জনপ্রিয় বাংলাদেশ প্রিন্ট সাইজ (১-ট্যাপ)", fontSize = 11.sp, color = OrangePrimary, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    bdPrintPresets.forEach { (label, dims, dpiVal) ->
                        AssistChip(
                            onClick = { onCreateCustomSize(dims.first, dims.second, dpiVal, label) },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = customWidthText,
                        onValueChange = { customWidthText = it.filter { ch -> ch.isDigit() || ch == '.' }.take(6) },
                        label = { Text("Width") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("custom_box_width")
                    )
                    FilledTonalIconButton(
                        onClick = {
                            val prevW = customWidthText
                            customWidthText = customHeightText
                            customHeightText = prevW
                        }
                    ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = "Swap Width and Height", tint = OrangePrimary)
                    }
                    OutlinedTextField(
                        value = customHeightText,
                        onValueChange = { customHeightText = it.filter { ch -> ch.isDigit() || ch == '.' }.take(6) },
                        label = { Text("Height") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("custom_box_height")
                    )
                }

                // Unit Selector: Inch / Feet / Cm / Pixel
                Text("Unit • একক", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SizeUnit.entries.forEach { unit ->
                        FilterChip(
                            selected = selectedUnit == unit,
                            onClick = { selectedUnit = unit },
                            label = { Text(unit.label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = OrangePrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                // DPI Selector: 72 / 150 / 300
                Text("DPI Quality", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(72, 150, 300).forEach { dpiVal ->
                        val active = selectedDpi == dpiVal
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (active) OrangePrimary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { selectedDpi = dpiVal },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "$dpiVal DPI",
                                color = if (active) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Text(
                    text = "Output: $computedW × $computedH px ($selectedDpi DPI)" +
                        if (isSmartBanner) " • Smooth Smart Mode preview" else "",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    onClick = {
                        val label = "${customWidthText.ifBlank { "1" }} × ${customHeightText.ifBlank { "1" }} ${selectedUnit.label} ($selectedDpi DPI)"
                        onCreateCustomSize(computedW, computedH, selectedDpi, label)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("custom_box_create_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Create Canvas • ক্যানভাস তৈরি করুন", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ChartTabButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) OrangePrimary else MaterialTheme.colorScheme.surfaceVariant)
            .border(
                1.dp,
                if (selected) OrangePrimary else MaterialTheme.colorScheme.outline,
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}
