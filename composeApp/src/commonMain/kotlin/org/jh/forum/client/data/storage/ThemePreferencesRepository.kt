package org.jh.forum.client.data.storage

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.jh.forum.client.ui.screen.ThemeMode

/**
 * Repository for theme preferences using multiplatform-settings.
 * Persists user's theme customization choices.
 */
class ThemePreferencesRepository(
    private val settings: Settings
) {
    companion object {
        private const val THEME_MODE_KEY = "theme_mode"
        private const val USE_DYNAMIC_COLOR_KEY = "use_dynamic_color"
        private const val SEED_COLOR_KEY = "seed_color"
    }

    private val _themeModeFlow = MutableStateFlow(getThemeMode())
    private val _useDynamicColorFlow = MutableStateFlow(getUseDynamicColor())
    private val _seedColorFlow = MutableStateFlow(getSeedColor())

    /**
     * Flow of theme mode (SYSTEM, LIGHT, or DARK)
     */
    val themeModeFlow: Flow<ThemeMode> = _themeModeFlow.asStateFlow()

    /**
     * Flow of dynamic color preference
     */
    val useDynamicColorFlow: Flow<Boolean> = _useDynamicColorFlow.asStateFlow()

    /**
     * Flow of seed color
     */
    val seedColorFlow: Flow<Color> = _seedColorFlow.asStateFlow()

    private fun getThemeMode(): ThemeMode {
        val modeName = settings.getString(THEME_MODE_KEY, ThemeMode.SYSTEM.name)
        return try {
            ThemeMode.valueOf(modeName)
        } catch (_: Exception) {
            ThemeMode.SYSTEM
        }
    }

    private fun getUseDynamicColor(): Boolean {
        return settings.getBoolean(USE_DYNAMIC_COLOR_KEY, true)
    }

    private fun getSeedColor(): Color {
        val colorInt = settings.getInt(SEED_COLOR_KEY, Color.Red.toArgb())
        return Color(colorInt)
    }

    /**
     * Save theme mode
     */
    suspend fun setThemeMode(mode: ThemeMode) {
        settings.putString(THEME_MODE_KEY, mode.name)
        _themeModeFlow.value = mode
    }

    /**
     * Save dynamic color preference
     */
    suspend fun setUseDynamicColor(useDynamic: Boolean) {
        settings.putBoolean(USE_DYNAMIC_COLOR_KEY, useDynamic)
        _useDynamicColorFlow.value = useDynamic
    }

    /**
     * Save seed color
     */
    suspend fun setSeedColor(color: Color) {
        settings.putInt(SEED_COLOR_KEY, color.toArgb())
        _seedColorFlow.value = color
    }
}
