package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.WindowCompat
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.ArtboardPreset
import com.example.ui.components.CanvasSizeSheet
import com.example.ui.components.SplashScreen
import com.example.ui.components.TutorialDialog
import com.example.ui.editor.EditorScreen
import com.example.ui.screens.*
import com.example.ui.theme.ArfinIllustratorTheme
import com.example.ui.theme.OrangePrimary
import com.example.viewmodel.MainTab
import com.example.viewmodel.StudioViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false
        setContent {
            val viewModel: StudioViewModel = viewModel()
            val prefs by viewModel.preferences.collectAsStateWithLifecycle()
            val showSplash by viewModel.showSplash.collectAsStateWithLifecycle()
            val showTutorialOverride by viewModel.showTutorialOverride.collectAsStateWithLifecycle()
            val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
            val projects by viewModel.projects.collectAsStateWithLifecycle()
            val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
            val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()
            val editorState by viewModel.editorState.collectAsStateWithLifecycle()

            var newDesignPreset by remember { mutableStateOf<ArtboardPreset?>(null) }
            var showNewDesignSheet by remember { mutableStateOf(false) }

            val homeFilePhotoLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.OpenDocument()
            ) { uri ->
                if (uri != null) {
                    viewModel.openPhotoInTransparentHdStudio(uri)
                }
            }

            val homeGalleryPhotoLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.PickVisualMedia()
            ) { uri ->
                if (uri != null) {
                    viewModel.openPhotoInTransparentHdStudio(uri)
                }
            }

            ArfinIllustratorTheme(darkTheme = prefs.isDarkTheme) {
                if (showSplash) {
                    SplashScreen(onFinished = { viewModel.finishSplash() })
                } else if (editorState.isOpen) {
                    EditorScreen(
                        viewModel = viewModel,
                        state = editorState,
                        defaultResolution = prefs.defaultResolution,
                        recentColors = prefs.recentColors,
                        onBack = { viewModel.selectTab(MainTab.HOME) }
                    )
                } else {
                    if (!prefs.hasCompletedTutorial || showTutorialOverride) {
                        TutorialDialog(onDismiss = { viewModel.completeTutorial() })
                    }

                    if (showNewDesignSheet) {
                        val preset = newDesignPreset ?: ArtboardPreset.FACEBOOK_POST
                        CanvasSizeSheet(
                            title = "Create New Design • নতুন ডিজাইন",
                            initialName = "${preset.label} Design",
                            initialWidth = preset.width,
                            initialHeight = preset.height,
                            initialCategory = preset.category,
                            showNameAndBg = true,
                            onDismiss = { showNewDesignSheet = false },
                            onConfirm = { name, w, h, cat, bg ->
                                showNewDesignSheet = false
                                viewModel.createNewDesign(name, w, h, cat, bg)
                            }
                        )
                    }

                    Scaffold(
                        contentWindowInsets = WindowInsets.safeDrawing,
                        bottomBar = {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                tonalElevation = 6.dp
                            ) {
                                NavigationBarItem(
                                    selected = currentTab == MainTab.HOME,
                                    onClick = { viewModel.selectTab(MainTab.HOME) },
                                    icon = {
                                        Icon(
                                            if (currentTab == MainTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                                            contentDescription = "Home"
                                        )
                                    },
                                    label = { Text("Home", fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = OrangePrimary,
                                        selectedTextColor = OrangePrimary,
                                        indicatorColor = OrangePrimary.copy(alpha = 0.16f)
                                    ),
                                    modifier = Modifier.testTag("nav_home")
                                )

                                NavigationBarItem(
                                    selected = false,
                                    onClick = {
                                        newDesignPreset = null
                                        showNewDesignSheet = true
                                    },
                                    icon = {
                                        Icon(
                                            Icons.Filled.AddCircle,
                                            contentDescription = "New Design",
                                            tint = OrangePrimary,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    },
                                    label = {
                                        Text(
                                            "New Design",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = OrangePrimary
                                        )
                                    },
                                    modifier = Modifier.testTag("nav_new_design")
                                )

                                NavigationBarItem(
                                    selected = currentTab == MainTab.PROJECTS,
                                    onClick = { viewModel.selectTab(MainTab.PROJECTS) },
                                    icon = {
                                        Icon(
                                            if (currentTab == MainTab.PROJECTS) Icons.Filled.Folder else Icons.Outlined.Folder,
                                            contentDescription = "My Projects"
                                        )
                                    },
                                    label = { Text("Projects", fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = OrangePrimary,
                                        selectedTextColor = OrangePrimary,
                                        indicatorColor = OrangePrimary.copy(alpha = 0.16f)
                                    ),
                                    modifier = Modifier.testTag("nav_projects")
                                )

                                NavigationBarItem(
                                    selected = currentTab == MainTab.TEMPLATES,
                                    onClick = { viewModel.selectTab(MainTab.TEMPLATES) },
                                    icon = {
                                        Icon(
                                            if (currentTab == MainTab.TEMPLATES) Icons.Filled.DashboardCustomize else Icons.Outlined.DashboardCustomize,
                                            contentDescription = "Templates"
                                        )
                                    },
                                    label = { Text("Templates", fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = OrangePrimary,
                                        selectedTextColor = OrangePrimary,
                                        indicatorColor = OrangePrimary.copy(alpha = 0.16f)
                                    ),
                                    modifier = Modifier.testTag("nav_templates")
                                )

                                NavigationBarItem(
                                    selected = currentTab == MainTab.SETTINGS,
                                    onClick = { viewModel.selectTab(MainTab.SETTINGS) },
                                    icon = {
                                        Icon(
                                            if (currentTab == MainTab.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                                            contentDescription = "Settings"
                                        )
                                    },
                                    label = { Text("Settings", fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = OrangePrimary,
                                        selectedTextColor = OrangePrimary,
                                        indicatorColor = OrangePrimary.copy(alpha = 0.16f)
                                    ),
                                    modifier = Modifier.testTag("nav_settings")
                                )
                            }
                        }
                    ) { innerPadding ->
                        Crossfade(
                            targetState = currentTab,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                            label = "tabCrossfade"
                        ) { tab ->
                            when (tab) {
                                MainTab.HOME -> HomeScreen(
                                    userName = prefs.userName,
                                    searchQuery = searchQuery,
                                    onSearchChange = viewModel::updateSearchQuery,
                                    projects = projects,
                                    onCreateNewDesign = { preset ->
                                        newDesignPreset = preset
                                        showNewDesignSheet = true
                                    },
                                    onSelectChartSize = { entry ->
                                        viewModel.createNewDesign(
                                            name = "Banner ${entry.physicalLabel}",
                                            width = entry.targetWidthPx,
                                            height = entry.targetHeightPx,
                                            category = if (entry.isFeet) "Feet Banner" else "Inch Print",
                                            dpi = 300,
                                            smartLabel = "Smart Mode • ${entry.physicalLabel} (${entry.pixelLabel}, 300 DPI)"
                                        )
                                    },
                                    onCreateCustomSize = { w, h, dpi, label ->
                                        viewModel.createNewDesign(
                                            name = "Custom $label",
                                            width = w,
                                            height = h,
                                            category = "Custom Size",
                                            dpi = dpi
                                        )
                                    },
                                    onOpenProject = viewModel::openProject,
                                    onDuplicateProject = viewModel::duplicateProject,
                                    onDeleteProject = viewModel::deleteProject,
                                    onOpenTemplate = viewModel::openTemplate,
                                    onOpenAllProjects = { viewModel.selectTab(MainTab.PROJECTS) },
                                    onOpenAllTemplates = { viewModel.selectTab(MainTab.TEMPLATES) },
                                    onProfileClick = { viewModel.selectTab(MainTab.SETTINGS) },
                                    onImportPhotoFromFileManager = {
                                        homeFilePhotoLauncher.launch(arrayOf("image/*"))
                                    },
                                    onImportPhotoFromGallery = {
                                        homeGalleryPhotoLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    }
                                )
                                MainTab.PROJECTS -> ProjectsScreen(
                                    projects = projects,
                                    searchQuery = searchQuery,
                                    sortOrder = sortOrder,
                                    onSearchChange = viewModel::updateSearchQuery,
                                    onSortChange = viewModel::updateSortOrder,
                                    onOpenProject = viewModel::openProject,
                                    onDuplicateProject = viewModel::duplicateProject,
                                    onDeleteProject = viewModel::deleteProject,
                                    onImportVector = viewModel::importVectorProject,
                                    onCreateNew = {
                                        newDesignPreset = null
                                        showNewDesignSheet = true
                                    }
                                )
                                MainTab.TEMPLATES -> TemplatesScreen(
                                    onOpenTemplate = viewModel::openTemplate
                                )
                                MainTab.SETTINGS -> SettingsScreen(
                                    preferences = prefs,
                                    projectCount = projects.size,
                                    onToggleDarkTheme = viewModel::setDarkTheme,
                                    onUpdateUserName = viewModel::setUserName,
                                    onToggleAutoSave = viewModel::setAutoSave,
                                    onChangeDefaultResolution = viewModel::setDefaultResolution,
                                    onToggleGridDefault = viewModel::setGridDefault,
                                    onToggleRulerDefault = viewModel::setRulerDefault,
                                    onToggleSnapDefault = viewModel::setSnapDefault,
                                    onReplayTutorial = viewModel::openTutorial
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
