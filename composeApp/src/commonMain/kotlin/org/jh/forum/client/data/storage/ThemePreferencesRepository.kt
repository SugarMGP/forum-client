package org.jh.forum.client.data.storage

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.jh.forum.client.ui.screen.ThemeMode

/**
 * Repository for theme preferences using DataStore.
 * Persists user's theme customization choices.
 */
class ThemePreferencesRepository(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        private val USE_DYNAMIC_COLOR_KEY = booleanPreferencesKey("use_dynamic_color")
        private val SEED_COLOR_KEY = intPreferencesKey("seed_color")
    }

    /**
     * Flow of theme mode (SYSTEM, LIGHT, or DARK)
     */
    val themeModeFlow: Flow<ThemeMode> = dataStore.data.map { preferences ->
        val modeName = preferences[THEME_MODE_KEY] ?: ThemeMode.SYSTEM.name
        try {
            ThemeMode.valueOf(modeName)
        } catch (_: Exception) {
            ThemeMode.SYSTEM
        }
    }

    /**
     * Flow of dynamic color preference
     */
    val useDynamicColorFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[USE_DYNAMIC_COLOR_KEY] ?: true
    }

    /**
     * Flow of seed color
     */
    val seedColorFlow: Flow<Color> = dataStore.data.map { preferences ->
        val colorInt = preferences[SEED_COLOR_KEY] ?: Color.Red.toArgb()
        Color(colorInt)
    }

    /**
     * Save theme mode
     */
    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = mode.name
        }
    }

    /**
     * Save dynamic color preference
     */
    suspend fun setUseDynamicColor(useDynamic: Boolean) {
        dataStore.edit { preferences ->
            preferences[USE_DYNAMIC_COLOR_KEY] = useDynamic
        }
    }

    /**
     * Save seed color
     */
    suspend fun setSeedColor(color: Color) {
        dataStore.edit { preferences ->
            preferences[SEED_COLOR_KEY] = color.toArgb()
        }
    }
}
