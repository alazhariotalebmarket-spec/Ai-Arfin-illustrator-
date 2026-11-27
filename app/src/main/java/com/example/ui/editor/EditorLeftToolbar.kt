package com.example.ui.editor

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ShapeType
import com.example.model.ToolMode
import com.example.ui.theme.OrangePrimary

private data class LeftToolEntry(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val isSelected: Boolean,
    val onClick: () -> Unit
)

@Composable
fun EditorLeftToolbar(
    activeTool: ToolMode,
    activeShapeType: ShapeType,
    onSelectTool: (ToolMode) -> Unit,
    onSelectShapeTool: (ShapeType) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(true) }

    val entries = listOf(
        LeftToolEntry(
            id = "left_select",
            label = "Select",
            icon = Icons.Default.NearMe,
            isSelected = activeTool == ToolMode.SELECT,
            onClick = { onSelectTool(ToolMode.SELECT) }
        ),
        LeftToolEntry(
            id = "left_direct_select",
            label = "Points",
            icon = Icons.Default.AdsClick,
            isSelected = activeTool == ToolMode.DIRECT_SELECT,
            onClick = { onSelectTool(ToolMode.DIRECT_SELECT) }
        ),
        LeftToolEntry(
            id = "left_pen",
            label = "Pen",
            icon = Icons.Default.Create,
            isSelected = activeTool == ToolMode.PEN,
            onClick = { onSelectTool(ToolMode.PEN) }
        ),
        LeftToolEntry(
            id = "left_curvature",
            label = "Curve",
            icon = Icons.Default.Timeline,
            isSelected = activeTool == ToolMode.CURVATURE,
            onClick = { onSelectTool(ToolMode.CURVATURE) }
        ),
        LeftToolEntry(
            id = "left_line",
            label = "Line",
            icon = Icons.Default.HorizontalRule,
            isSelected = activeTool == ToolMode.SHAPES && activeShapeType == ShapeType.LINE,
            onClick = { onSelectShapeTool(ShapeType.LINE) }
        ),
        LeftToolEntry(
            id = "left_arc",
            label = "Arc",
            icon = Icons.Default.Gesture,
            isSelected = activeTool == ToolMode.SHAPES && activeShapeType == ShapeType.ARC,
            onClick = { onSelectShapeTool(ShapeType.ARC) }
        ),
        LeftToolEntry(
            id = "left_rect",
            label = "Rect",
            icon = Icons.Default.CropSquare,
            isSelected = activeTool == ToolMode.SHAPES && activeShapeType == ShapeType.RECTANGLE,
            onClick = { onSelectShapeTool(ShapeType.RECTANGLE) }
        ),
        LeftToolEntry(
            id = "left_rrect",
            label = "Round",
            icon = Icons.Default.RoundedCorner,
            isSelected = activeTool == ToolMode.SHAPES && activeShapeType == ShapeType.ROUNDED_RECT,
            onClick = { onSelectShapeTool(ShapeType.ROUNDED_RECT) }
        ),
        LeftToolEntry(
            id = "left_ellipse",
            label = "Ellipse",
            icon = Icons.Default.RadioButtonUnchecked,
            isSelected = activeTool == ToolMode.SHAPES && activeShapeType == ShapeType.CIRCLE,
            onClick = { onSelectShapeTool(ShapeType.CIRCLE) }
        ),
        LeftToolEntry(
            id = "left_polygon",
            label = "Poly",
            icon = Icons.Default.Hexagon,
            isSelected = activeTool == ToolMode.SHAPES && activeShapeType == ShapeType.POLYGON,
            onClick = { onSelectShapeTool(ShapeType.POLYGON) }
        ),
        LeftToolEntry(
            id = "left_star",
            label = "Star",
            icon = Icons.Default.StarOutline,
            isSelected = activeTool == ToolMode.SHAPES && activeShapeType == ShapeType.STAR,
            onClick = { onSelectShapeTool(ShapeType.STAR) }
        ),
        LeftToolEntry(
            id = "left_brush",
            label = "Brush",
            icon = Icons.Default.Brush,
            isSelected = activeTool == ToolMode.BRUSH,
            onClick = { onSelectTool(ToolMode.BRUSH) }
        ),
        LeftToolEntry(
            id = "left_eraser",
            label = "Eraser",
            icon = Icons.Default.AutoFixNormal,
            isSelected = activeTool == ToolMode.ERASER,
            onClick = { onSelectTool(ToolMode.ERASER) }
        ),
        LeftToolEntry(
            id = "left_picker",
            label = "Picker",
            icon = Icons.Default.Colorize,
            isSelected = activeTool == ToolMode.EYEDROPPER,
            onClick = { onSelectTool(ToolMode.EYEDROPPER) }
        ),
        LeftToolEntry(
            id = "left_hand",
            label = "Hand",
            icon = Icons.Default.PanTool,
            isSelected = activeTool == ToolMode.HAND,
            onClick = { onSelectTool(ToolMode.HAND) }
        ),
        LeftToolEntry(
            id = "left_zoom",
            label = "Zoom",
            icon = Icons.Default.ZoomIn,
            isSelected = activeTool == ToolMode.ZOOM,
            onClick = { onSelectTool(ToolMode.ZOOM) }
        )
    )

    Column(
        modifier = modifier.padding(start = 6.dp, top = 28.dp, bottom = 12.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
            tonalElevation = 6.dp,
            shadowElevation = 6.dp,
            modifier = Modifier
                .size(34.dp)
                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                .clip(CircleShape)
                .clickable { expanded = !expanded }
                .testTag("left_toolbar_toggle")
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (expanded) Icons.Default.ChevronLeft else Icons.Default.Build,
                    contentDescription = if (expanded) "Collapse Tools" else "Expand Tools",
                    tint = OrangePrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        AnimatedVisibility(visible = expanded) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                tonalElevation = 8.dp,
                shadowElevation = 10.dp,
                modifier = Modifier
                    .width(50.dp)
                    .heightIn(max = 460.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f), RoundedCornerShape(14.dp))
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 6.dp, horizontal = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    entries.forEach { entry ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (entry.isSelected) OrangePrimary.copy(alpha = 0.2f)
                                    else Color.Transparent
                                )
                                .clickable { entry.onClick() }
                                .padding(vertical = 5.dp, horizontal = 2.dp)
                                .testTag(entry.id)
                        ) {
                            Icon(
                                imageVector = entry.icon,
                                contentDescription = entry.label,
                                tint = if (entry.isSelected) OrangePrimary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = entry.label,
                                fontSize = 8.sp,
                                fontWeight = if (entry.isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (entry.isSelected) OrangePrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
