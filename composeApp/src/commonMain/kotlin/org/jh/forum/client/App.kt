package org.jh.forum.client

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import org.jh.forum.client.ui.navigation.MainNavigation
import org.jh.forum.client.ui.screen.ThemeMode
import org.jh.forum.client.ui.theme.ForumTheme
import org.jh.forum.client.ui.theme.rememberThemeState

@Composable
fun App() {
    val themeState = rememberThemeState()
    val darkTheme = when (themeState.themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    ForumTheme(
        darkTheme = darkTheme,
        dynamicColor = true // Enable dynamic color on Android 12+
    ) {
        MainNavigation(
            onThemeChanged = { mode ->
                themeState.setThemeMode(mode)
            }
        )
    }
}