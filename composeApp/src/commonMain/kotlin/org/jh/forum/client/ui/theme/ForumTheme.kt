package org.jh.forum.client.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import org.jh.forum.client.ui.screen.ThemeMode

@Composable
expect fun ForumTheme(
    darkTheme: Boolean,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
)

data class ThemeState(
    val themeMode: ThemeMode,
    val setThemeMode: (ThemeMode) -> Unit,
)

@Composable
fun rememberThemeState(): ThemeState {
    val themeMode = remember { mutableStateOf(ThemeMode.SYSTEM) }

    return ThemeState(
        themeMode = themeMode.value,
        setThemeMode = { mode -> themeMode.value = mode },
    )
}