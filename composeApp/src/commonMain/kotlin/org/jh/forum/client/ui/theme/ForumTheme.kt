package org.jh.forum.client.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import org.jh.forum.client.ui.screen.ThemeMode

@Composable
fun ForumTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme(),
        content = content
    )
}

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