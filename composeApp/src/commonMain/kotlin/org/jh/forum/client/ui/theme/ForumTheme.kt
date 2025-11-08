package org.jh.forum.client.ui.theme

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch
import org.jh.forum.client.data.storage.ThemePreferencesRepository
import org.jh.forum.client.data.storage.createDataStore
import org.jh.forum.client.ui.screen.ThemeMode

@Composable
expect fun ForumTheme(
    darkTheme: Boolean,
    dynamicColor: Boolean = false,
    seedColor: Color = Color.Red,
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
    val repository = remember { ThemePreferencesRepository(createDataStore("theme.preferences_pb")) }
    val coroutineScope = rememberCoroutineScope()

    val themeMode by repository.themeModeFlow.collectAsState(initial = ThemeMode.SYSTEM)
    val useDynamicColor by repository.useDynamicColorFlow.collectAsState(
        initial = supportsDynamicColor()
    )
    val seedColor by repository.seedColorFlow.collectAsState(initial = Color.Red)

    return ThemeState(
        themeMode = themeMode,
        setThemeMode = { mode ->
            coroutineScope.launch {
                repository.setThemeMode(mode)
            }
        },
        useDynamicColor = useDynamicColor,
        setUseDynamicColor = { use ->
            coroutineScope.launch {
                repository.setUseDynamicColor(use)
            }
        },
        seedColor = seedColor,
        setSeedColor = { color ->
            coroutineScope.launch {
                repository.setSeedColor(color)
            }
        },
    )
}