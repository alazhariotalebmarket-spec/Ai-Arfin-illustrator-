package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.ExportResolution
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "arfin_studio_prefs")

data class UserPreferences(
    val isDarkTheme: Boolean = true,
    val userName: String = "Arfin",
    val autoSaveEnabled: Boolean = true,
    val defaultResolution: ExportResolution = ExportResolution.RES_4K,
    val showGridDefault: Boolean = false,
    val showRulerDefault: Boolean = true,
    val snapToGridDefault: Boolean = false,
    val hasCompletedTutorial: Boolean = false,
    val recentColors: List<Long> = listOf(
        0xFFFF7A00L, 0xFFCC4400L, 0xFF1E1E1EL, 0xFFFFFFFFL,
        0xFFD4AF37L, 0xFF0F5132L, 0xFF2563EBL, 0xFFDC2626L
    )
)

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
        val USER_NAME = stringPreferencesKey("user_name")
        val AUTO_SAVE = booleanPreferencesKey("auto_save_enabled")
        val DEFAULT_RESOLUTION = stringPreferencesKey("default_resolution")
        val SHOW_GRID = booleanPreferencesKey("show_grid_default")
        val SHOW_RULER = booleanPreferencesKey("show_ruler_default")
        val SNAP_TO_GRID = booleanPreferencesKey("snap_to_grid_default")
        val TUTORIAL_DONE = booleanPreferencesKey("has_completed_tutorial")
        val RECENT_COLORS = stringPreferencesKey("recent_colors")
    }

    val preferences: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        val resName = prefs[Keys.DEFAULT_RESOLUTION] ?: ExportResolution.RES_4K.name
        val res = ExportResolution.entries.find { it.name == resName } ?: ExportResolution.RES_4K
        val colorsRaw = prefs[Keys.RECENT_COLORS]
        val colors = if (!colorsRaw.isNullOrBlank()) {
            colorsRaw.split(",").mapNotNull { it.toLongOrNull() }
        } else {
            UserPreferences().recentColors
        }
        UserPreferences(
            isDarkTheme = prefs[Keys.IS_DARK_THEME] ?: true,
            userName = prefs[Keys.USER_NAME] ?: "Arfin",
            autoSaveEnabled = prefs[Keys.AUTO_SAVE] ?: true,
            defaultResolution = res,
            showGridDefault = prefs[Keys.SHOW_GRID] ?: false,
            showRulerDefault = prefs[Keys.SHOW_RULER] ?: true,
            snapToGridDefault = prefs[Keys.SNAP_TO_GRID] ?: false,
            hasCompletedTutorial = prefs[Keys.TUTORIAL_DONE] ?: false,
            recentColors = colors
        )
    }

    suspend fun setDarkTheme(dark: Boolean) {
        context.dataStore.edit { it[Keys.IS_DARK_THEME] = dark }
    }

    suspend fun setUserName(name: String) {
        context.dataStore.edit { it[Keys.USER_NAME] = name.trim().ifEmpty { "Arfin" } }
    }

    suspend fun setAutoSave(enabled: Boolean) {
        context.dataStore.edit { it[Keys.AUTO_SAVE] = enabled }
    }

    suspend fun setDefaultResolution(res: ExportResolution) {
        context.dataStore.edit { it[Keys.DEFAULT_RESOLUTION] = res.name }
    }

    suspend fun setGridDefault(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SHOW_GRID] = enabled }
    }

    suspend fun setRulerDefault(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SHOW_RULER] = enabled }
    }

    suspend fun setSnapDefault(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SNAP_TO_GRID] = enabled }
    }

    suspend fun setTutorialCompleted(completed: Boolean) {
        context.dataStore.edit { it[Keys.TUTORIAL_DONE] = completed }
    }

    suspend fun addRecentColor(color: Long) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.RECENT_COLORS]?.split(",")?.mapNotNull { it.toLongOrNull() }
                ?: UserPreferences().recentColors
            val updated = (listOf(color) + current.filter { it != color }).take(12)
            prefs[Keys.RECENT_COLORS] = updated.joinToString(",")
        }
    }
}
