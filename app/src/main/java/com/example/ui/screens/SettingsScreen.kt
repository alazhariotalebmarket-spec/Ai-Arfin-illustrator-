package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.example.data.UserPreferences
import com.example.model.ExportResolution
import com.example.ui.components.AiBrandLockup
import com.example.ui.theme.OrangePrimary
import com.example.ui.theme.SuccessGreen

@Composable
fun SettingsScreen(
    preferences: UserPreferences,
    projectCount: Int,
    onToggleDarkTheme: (Boolean) -> Unit,
    onUpdateUserName: (String) -> Unit,
    onToggleAutoSave: (Boolean) -> Unit,
    onChangeDefaultResolution: (ExportResolution) -> Unit,
    onToggleGridDefault: (Boolean) -> Unit,
    onToggleRulerDefault: (Boolean) -> Unit,
    onToggleSnapDefault: (Boolean) -> Unit,
    onReplayTutorial: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showNameDialog by remember { mutableStateOf(false) }
    var draftName by remember(preferences.userName) { mutableStateOf(preferences.userName) }

    if (showNameDialog) {
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            title = { Text("Designer Profile Name") },
            text = {
                OutlinedTextField(
                    value = draftName,
                    onValueChange = { draftName = it },
                    label = { Text("Name on Home Greeting") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateUserName(draftName)
                        showNameDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNameDialog = false }) { Text("Cancel") }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Settings • সেটিংস", style = MaterialTheme.typography.headlineMedium)

        // Brand & Offline Status Card
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AiBrandLockup(badgeSize = 48.dp)
                Column(horizontalAlignment = Alignment.End) {
                    Surface(
                        color = SuccessGreen.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "১০০% অফলাইন • OFFLINE",
                            color = SuccessGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$projectCount Saved Projects",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Appearance & Profile
        SettingsSection(title = "Appearance & Profile • থিম ও প্রোফাইল") {
            SettingsActionRow(
                icon = Icons.Default.Person,
                title = "Designer Name",
                subtitle = "Shown as \"Hello, ${preferences.userName}\"",
                onClick = { showNameDialog = true }
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            SettingsSwitchRow(
                icon = Icons.Default.DarkMode,
                title = "Dark Studio Mode • ডার্ক মোড",
                subtitle = "Charcoal #1E1E1E workspace (recommended)",
                checked = preferences.isDarkTheme,
                onCheckedChange = onToggleDarkTheme,
                testTag = "settings_dark_mode_switch"
            )
        }

        // Editor & Auto-Save
        SettingsSection(title = "Editor & Storage • এডিটর ও অটো-সেভ") {
            SettingsSwitchRow(
                icon = Icons.Default.Save,
                title = "Auto-Save Every 30 Seconds",
                subtitle = "Automatically saves work offline to local database",
                checked = preferences.autoSaveEnabled,
                onCheckedChange = onToggleAutoSave
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            SettingsSwitchRow(
                icon = Icons.Default.GridOn,
                title = "Show Grid by Default",
                subtitle = "Display alignment grid when opening canvas",
                checked = preferences.showGridDefault,
                onCheckedChange = onToggleGridDefault
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            SettingsSwitchRow(
                icon = Icons.Default.Straighten,
                title = "Show Rulers by Default",
                subtitle = "Top & left pixel rulers in Editor",
                checked = preferences.showRulerDefault,
                onCheckedChange = onToggleRulerDefault
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            SettingsSwitchRow(
                icon = Icons.Default.GridGoldenratio,
                title = "Snap to Grid by Default",
                subtitle = "Align moved layers cleanly to grid intervals",
                checked = preferences.snapToGridDefault,
                onCheckedChange = onToggleSnapDefault
            )
        }

        // Default Export Quality
        SettingsSection(title = "Default Export Quality • এক্সপোর্ট") {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Default Output Resolution", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExportResolution.entries.forEach { res ->
                        val selected = preferences.defaultResolution == res
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selected) OrangePrimary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { onChangeDefaultResolution(res) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = res.label,
                                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // Help & Tutorial
        SettingsSection(title = "Help & Guide • সহায়তা") {
            SettingsActionRow(
                icon = Icons.Default.HelpOutline,
                title = "Replay 3-Step Tutorial • ব্যবহারবিধি",
                subtitle = "Learn Pen, Bangla Fonts, Layers & 8K Export",
                onClick = onReplayTutorial
            )
        }

        // Developer & About App
        Text(
            text = "Developer • ডেভেলপার",
            style = MaterialTheme.typography.labelMedium,
            color = OrangePrimary
        )
        com.example.ui.components.DeveloperInfoCard()

        SettingsSection(title = "About App • অ্যাপ তথ্য") {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("App Name: Arfin Illustrator", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Version: 1.0.0", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Developed by মোঃ ফেরদাউস মোল্লা", fontSize = 12.sp, color = OrangePrimary, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = OrangePrimary
        )
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
        ) {
            Column(content = content)
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String = ""
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, contentDescription = title, tint = OrangePrimary)
            Column {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = OrangePrimary),
            modifier = if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier
        )
    }
}

@Composable
private fun SettingsActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, contentDescription = title, tint = OrangePrimary)
            Column {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
