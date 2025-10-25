package org.jh.forum.client.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import org.jh.forum.client.data.preferences.ThemePreferences
import org.jh.forum.client.ui.screen.ThemeMode

@Composable
expect fun ForumTheme(
    darkTheme: Boolean,
    dynamicColor: Boolean = false,
    seedColor: Color = ThemePreferences.Red,
    content: @Composable () -> Unit
)

/**
 * Check if the current platform supports dynamic color (Android 12+)
 */
expect fun supportsDynamicColor(): Boolean

data class ThemeState(
    val themeMode: ThemeMode,
    val setThemeMode: (ThemeMode) -> Unit,
    val useDynamicColor: Boolean,
    val setUseDynamicColor: (Boolean) -> Unit,
    val seedColor: Color,
    val setSeedColor: (Color) -> Unit,
)

@Composable
fun rememberThemeState(): ThemeState {
    val themeMode = remember { mutableStateOf(ThemeMode.SYSTEM) }
    val useDynamicColor = remember { mutableStateOf(supportsDynamicColor()) }
    val seedColor = remember { mutableStateOf(ThemePreferences.Red) }

    return ThemeState(
        themeMode = themeMode.value,
        setThemeMode = { mode -> themeMode.value = mode },
        useDynamicColor = useDynamicColor.value,
        setUseDynamicColor = { use -> useDynamicColor.value = use },
        seedColor = seedColor.value,
        setSeedColor = { color -> seedColor.value = color },
    )
}