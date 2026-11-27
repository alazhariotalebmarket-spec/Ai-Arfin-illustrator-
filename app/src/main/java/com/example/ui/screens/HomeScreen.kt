package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ProjectEntity
import com.example.engine.CanvasRenderer
import com.example.model.ArtboardPreset
import com.example.model.DesignTemplate
import com.example.model.SizeChartEntry
import com.example.model.TemplateCatalog
import com.example.ui.components.AiLogoBadge
import com.example.ui.components.ContactDeveloperDialog
import com.example.ui.components.CustomSizeChartSection
import com.example.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    userName: String,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    projects: List<ProjectEntity>,
    onCreateNewDesign: (preset: ArtboardPreset?) -> Unit,
    onSelectChartSize: (SizeChartEntry) -> Unit,
    onCreateCustomSize: (widthPx: Int, heightPx: Int, dpi: Int, label: String) -> Unit,
    onOpenProject: (String) -> Unit,
    onDuplicateProject: (ProjectEntity) -> Unit,
    onDeleteProject: (String) -> Unit,
    onOpenTemplate: (DesignTemplate) -> Unit,
    onOpenAllProjects: () -> Unit,
    onOpenAllTemplates: () -> Unit,
    onProfileClick: () -> Unit,
    onImportPhotoFromFileManager: () -> Unit = {},
    onImportPhotoFromGallery: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val categories = remember {
        listOf(
            "Logo" to ArtboardPreset.LOGO,
            "Poster" to ArtboardPreset.A4,
            "Banner" to ArtboardPreset.BANNER,
            "YouTube Thumbnail" to ArtboardPreset.YOUTUBE_THUMB,
            "Business Card" to ArtboardPreset.BUSINESS_CARD,
            "Social Post" to ArtboardPreset.FACEBOOK_POST,
            "Islamic Design" to ArtboardPreset.ISLAMIC,
            "Blank Canvas" to ArtboardPreset.CUSTOM
        )
    }

    val templateCategories = remember {
        listOf("All", "মাদ্রাসা", "ইসলামিক", "ব্যবসা", "উৎসব", "খেলা")
    }
    var selectedTemplateFilter by remember { mutableStateOf("All") }
    var templateSearchQuery by remember { mutableStateOf("") }
    var headerMenuOpen by remember { mutableStateOf(false) }
    var showContactDeveloper by remember { mutableStateOf(false) }

    if (showContactDeveloper) {
        ContactDeveloperDialog(onDismiss = { showContactDeveloper = false })
    }

    val filteredTemplates = remember(searchQuery, templateSearchQuery, selectedTemplateFilter) {
        val combinedQ = templateSearchQuery.ifBlank { searchQuery }.trim().lowercase()
        TemplateCatalog.templates.filter { tpl ->
            val catOk = selectedTemplateFilter == "All" || tpl.category == selectedTemplateFilter
            val qOk = combinedQ.isEmpty() ||
                tpl.title.lowercase().contains(combinedQ) ||
                tpl.titleBn.lowercase().contains(combinedQ) ||
                tpl.category.lowercase().contains(combinedQ)
            catOk && qOk
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Top Header: Hello, Arfin + Contact Developer Menu + Profile
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    AiLogoBadge(size = 46.dp, fontSize = 22.sp, cornerRadius = 12.dp, showGlow = false)
                    Column {
                        Text(
                            text = "Hello, $userName",
                            style = MaterialTheme.typography.headlineMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Arfin Illustrator",
                                style = MaterialTheme.typography.bodyMedium,
                                color = OrangePrimary
                            )
                            Surface(
                                color = SuccessGreen.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "১০০% অফলাইন",
                                    color = SuccessGreen,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box {
                        IconButton(
                            onClick = { headerMenuOpen = true },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .testTag("home_menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SupportAgent,
                                contentDescription = "Home Menu",
                                tint = OrangePrimary
                            )
                        }
                        DropdownMenu(
                            expanded = headerMenuOpen,
                            onDismissRequest = { headerMenuOpen = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Contact Developer • ডেভেলপার") },
                                leadingIcon = { Icon(Icons.Default.Call, contentDescription = null, tint = OrangePrimary) },
                                onClick = {
                                    headerMenuOpen = false
                                    showContactDeveloper = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Settings • সেটিংস") },
                                leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
                                onClick = {
                                    headerMenuOpen = false
                                    onProfileClick()
                                }
                            )
                        }
                    }

                    IconButton(
                        onClick = onProfileClick,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .testTag("profile_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = OrangePrimary
                        )
                    }
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear search")
                        }
                    }
                },
                placeholder = { Text("Search projects, templates... (খুঁজুন)", fontSize = 13.sp) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OrangePrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_search_input")
            )
        }

        // Big Orange Hero Banner: Create New Design
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(OrangePrimary, OrangeDark)
                        )
                    )
                    .clickable { onCreateNewDesign(null) }
                    .padding(20.dp)
                    .testTag("create_new_design_banner")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Surface(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "VECTOR & 8K STUDIO",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Create New Design",
                            color = Color.White,
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 21.sp
                        )
                        Text(
                            text = "নতুন ডিজাইন তৈরি করুন • Logo, Poster, Thumbnail",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Create New Design",
                            tint = OrangeDark,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            }
        }

        // Quick Action Card: File Manager -> Transparent BG & Partial Crop in 4K HD
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, OrangePrimary.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(OrangePrimary.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.AutoFixHigh,
                                contentDescription = "Photo Cutout HD",
                                tint = OrangePrimary
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Photo Cutout & 4K Transparent PNG",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                "ফাইল ম্যানেজার থেকে ফটো এনে ব্যাকগ্রাউন্ড রিমুভ, অংশ ক্রপ ও HD তৈরি করুন",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onImportPhotoFromFileManager,
                            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("home_file_manager_photo_btn")
                        ) {
                            Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("File Manager", fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                        }
                        OutlinedButton(
                            onClick = onImportPhotoFromGallery,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("home_gallery_photo_btn")
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Gallery", fontSize = 12.sp, maxLines = 1)
                        }
                    }
                }
            }
        }

        // Horizontal Categories
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Categories • ক্যাটাগরি",
                    style = MaterialTheme.typography.titleMedium
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    categories.forEach { (label, preset) ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onCreateNewDesign(preset) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = categoryIcon(label),
                                    contentDescription = label,
                                    tint = OrangePrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Column {
                                    Text(label, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    Text(
                                        "${preset.width}×${preset.height}",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Custom Size Chart Section (Inch Chart + Feet Chart + Custom Size Box)
        item {
            CustomSizeChartSection(
                onSelectChartSize = onSelectChartSize,
                onCreateCustomSize = onCreateCustomSize
            )
        }

        // Recent Projects Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Recent Projects • সাম্প্রতিক প্রজেক্ট", style = MaterialTheme.typography.titleMedium)
                if (projects.isNotEmpty()) {
                    TextButton(onClick = onOpenAllProjects) {
                        Text("See All", color = OrangePrimary)
                    }
                }
            }
        }

        if (projects.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Draw,
                            contentDescription = "Empty projects",
                            tint = OrangePrimary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No saved designs yet • এখনো কোনো প্রজেক্ট নেই",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap 'Create New Design', a Size Chart entry, or a Ready Template below!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            val rows = projects.take(6).chunked(2)
            items(rows) { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowItems.forEach { proj ->
                        ProjectGridCard(
                            project = proj,
                            onEdit = { onOpenProject(proj.id) },
                            onDuplicate = { onDuplicateProject(proj) },
                            onDelete = { onDeleteProject(proj.id) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Ready Templates Section in Bangla ("রেডি টেমপ্লেট" / "Ready Templates")
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("রেডি টেমপ্লেট • Ready Templates", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "${TemplateCatalog.templates.size}টি এডিটেবল বাংলা টেমপ্লেট",
                            fontSize = 11.sp,
                            color = OrangePrimary
                        )
                    }
                    TextButton(onClick = onOpenAllTemplates) {
                        Text("সব দেখুন", color = OrangePrimary)
                    }
                }

                // Search in Templates
                OutlinedTextField(
                    value = templateSearchQuery,
                    onValueChange = { templateSearchQuery = it },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Templates") },
                    trailingIcon = {
                        if (templateSearchQuery.isNotEmpty()) {
                            IconButton(onClick = { templateSearchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    placeholder = { Text("বাংলা নামে টেমপ্লেট খুঁজুন (যেমন: মাহফিল, ঈদ, মাদ্রাসা)...", fontSize = 12.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_template_search")
                )

                // Filter Chips: All, মাদ্রাসা, ইসলামিক, ব্যবসা, উৎসব, খেলা
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    templateCategories.forEach { cat ->
                        FilterChip(
                            selected = selectedTemplateFilter == cat,
                            onClick = { selectedTemplateFilter = cat },
                            label = { Text(cat, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = OrangePrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredTemplates, key = { it.id }) { tpl ->
                    TemplatePreviewCard(
                        template = tpl,
                        onClick = { onOpenTemplate(tpl) },
                        modifier = Modifier.width(195.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ProjectGridCard(
    project: ProjectEntity,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    val cacheKey = remember(project.thumbnailPath, project.updatedAt) {
        "proj_${project.id}_${project.updatedAt}"
    }
    val thumbBitmap by produceState<Bitmap?>(
        initialValue = CanvasRenderer.getCachedPreviewThumbnail(cacheKey),
        key1 = cacheKey
    ) {
        if (value == null && project.thumbnailPath != null) {
            value = withContext(Dispatchers.IO) {
                val f = File(project.thumbnailPath)
                if (f.exists()) {
                    BitmapFactory.decodeFile(project.thumbnailPath)?.also {
                        CanvasRenderer.cachePreviewThumbnail(cacheKey, it)
                    }
                } else null
            }
        }
    }
    val dateStr = remember(project.updatedAt) {
        SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(project.updatedAt))
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete Project?") },
            text = { Text("Delete \"${project.name}\" permanently from local storage?") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    onDelete()
                }) {
                    Text("Delete", color = ErrorRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Cancel") }
            }
        )
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onEdit)
            .testTag("project_card_${project.id}")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(126.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                val bmp = thumbBitmap
                if (bmp != null) {
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = project.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    )
                } else {
                    Icon(Icons.Default.Image, contentDescription = project.name, tint = MutedGray)
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                ) {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.55f))
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Project actions",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit • এডিট") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onEdit()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Duplicate • ডুপ্লিকেট") },
                            leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onDuplicate()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete • ডিলিট", color = ErrorRed) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = ErrorRed) },
                            onClick = {
                                menuExpanded = false
                                confirmDelete = true
                            }
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = project.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$dateStr • ${project.artboardWidth}×${project.artboardHeight}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun TemplatePreviewCard(
    template: DesignTemplate,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val tplKey = remember(template.id) { "tpl_${template.id}" }
    val previewBmp by produceState<Bitmap?>(
        initialValue = CanvasRenderer.getCachedPreviewThumbnail(tplKey),
        key1 = tplKey
    ) {
        if (value == null) {
            value = withContext(Dispatchers.Default) {
                val doc = template.documentBuilder()
                CanvasRenderer.renderThumbnail(context, doc, maxDimension = 300).also {
                    CanvasRenderer.cachePreviewThumbnail(tplKey, it)
                }
            }
        }
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("template_card_${template.id}")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(148.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (previewBmp != null) {
                    Image(
                        bitmap = previewBmp!!.asImageBitmap(),
                        contentDescription = template.titleBn,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    )
                } else {
                    CircularProgressIndicator(
                        color = OrangePrimary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Surface(
                    color = Color.Black.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    Text(
                        text = template.sizeLabel,
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = template.titleBn,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${template.category} • ${template.title}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp)
                ) {
                    Text("ব্যবহার করুন", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

private fun categoryIcon(cat: String) = when (cat) {
    "Logo" -> Icons.Default.AutoAwesome
    "Poster" -> Icons.Default.Article
    "Banner" -> Icons.Default.ViewCarousel
    "YouTube Thumbnail" -> Icons.Default.PlayCircleFilled
    "Business Card" -> Icons.Default.Badge
    "Social Post" -> Icons.Default.Share
    "Islamic Design" -> Icons.Default.Star
    else -> Icons.Default.CropSquare
}
