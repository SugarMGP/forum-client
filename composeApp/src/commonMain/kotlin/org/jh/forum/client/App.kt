package org.jh.forum.client

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jh.forum.client.ui.navigation.MainNavigation
import org.jh.forum.client.ui.screen.ThemeMode
import org.jh.forum.client.ui.theme.ForumTheme
import org.jh.forum.client.ui.theme.rememberThemeState

@OptIn(ExperimentalSharedTransitionApi::class)
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
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            SharedTransitionLayout {
                MainNavigation(
                    sharedTransitionScope = this,
                    onThemeChanged = { mode ->
                        themeState.setThemeMode(mode)
                    }
                )
            }
        }
    }
}