package org.jh.forum.client.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import org.jh.forum.client.ui.screen.ThemeMode

// Blue color palette for the forum theme - all colors are in blue tones for consistency
private val md_theme_light_primary = Color(0xFF2196F3) // Material Blue
private val md_theme_light_onPrimary = Color(0xFFFFFFFF)
private val md_theme_light_primaryContainer = Color(0xFFBBDEFB)
private val md_theme_light_onPrimaryContainer = Color(0xFF01579B)
private val md_theme_light_secondary = Color(0xFF03A9F4) // Light Blue
private val md_theme_light_onSecondary = Color(0xFFFFFFFF)
private val md_theme_light_secondaryContainer = Color(0xFFB3E5FC)
private val md_theme_light_onSecondaryContainer = Color(0xFF01579B)
private val md_theme_light_tertiary = Color(0xFF00BCD4) // Cyan
private val md_theme_light_onTertiary = Color(0xFFFFFFFF)
private val md_theme_light_tertiaryContainer = Color(0xFFB2EBF2)
private val md_theme_light_onTertiaryContainer = Color(0xFF006064)
private val md_theme_light_error = Color(0xFFD32F2F)
private val md_theme_light_errorContainer = Color(0xFFF9DEDC)
private val md_theme_light_onError = Color(0xFFFFFFFF)
private val md_theme_light_onErrorContainer = Color(0xFF8C1D18)
private val md_theme_light_background = Color(0xFFFCFCFF) // Slight blue tint
private val md_theme_light_onBackground = Color(0xFF1A1C1E)
private val md_theme_light_surface = Color(0xFFFCFCFF)
private val md_theme_light_onSurface = Color(0xFF1A1C1E)
private val md_theme_light_surfaceVariant = Color(0xFFDEE3EB) // Blue-grey variant
private val md_theme_light_onSurfaceVariant = Color(0xFF42474E)
private val md_theme_light_outline = Color(0xFF72777F) // Blue-grey outline
private val md_theme_light_inverseOnSurface = Color(0xFFF0F0F4)
private val md_theme_light_inverseSurface = Color(0xFF2E3036)
private val md_theme_light_inversePrimary = Color(0xFF90CAF9)

private val md_theme_dark_primary = Color(0xFF90CAF9) // Light blue for dark mode
private val md_theme_dark_onPrimary = Color(0xFF003A5E)
private val md_theme_dark_primaryContainer = Color(0xFF0D47A1)
private val md_theme_dark_onPrimaryContainer = Color(0xFFE3F2FD)
private val md_theme_dark_secondary = Color(0xFF81D4FA)
private val md_theme_dark_onSecondary = Color(0xFF003A5E)
private val md_theme_dark_secondaryContainer = Color(0xFF0277BD)
private val md_theme_dark_onSecondaryContainer = Color(0xFFE1F5FE)
private val md_theme_dark_tertiary = Color(0xFF80DEEA)
private val md_theme_dark_onTertiary = Color(0xFF003A40)
private val md_theme_dark_tertiaryContainer = Color(0xFF00838F)
private val md_theme_dark_onTertiaryContainer = Color(0xFFE0F7FA)
private val md_theme_dark_error = Color(0xFFEF5350)
private val md_theme_dark_errorContainer = Color(0xFF8C1D18)
private val md_theme_dark_onError = Color(0xFF601410)
private val md_theme_dark_onErrorContainer = Color(0xFFF9DEDC)
private val md_theme_dark_background = Color(0xFF1A1C1E) // Dark blue-grey
private val md_theme_dark_onBackground = Color(0xFFE2E2E6)
private val md_theme_dark_surface = Color(0xFF1A1C1E)
private val md_theme_dark_onSurface = Color(0xFFE2E2E6)
private val md_theme_dark_surfaceVariant = Color(0xFF42474E) // Dark blue-grey variant
private val md_theme_dark_onSurfaceVariant = Color(0xFFC2C7CF)
private val md_theme_dark_outline = Color(0xFF8C9199) // Blue-grey outline
private val md_theme_dark_inverseOnSurface = Color(0xFF1A1C1E)
private val md_theme_dark_inverseSurface = Color(0xFFE2E2E6)
private val md_theme_dark_inversePrimary = Color(0xFF1976D2)

private val LightColorScheme = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    tertiary = md_theme_light_tertiary,
    onTertiary = md_theme_light_onTertiary,
    tertiaryContainer = md_theme_light_tertiaryContainer,
    onTertiaryContainer = md_theme_light_onTertiaryContainer,
    error = md_theme_light_error,
    errorContainer = md_theme_light_errorContainer,
    onError = md_theme_light_onError,
    onErrorContainer = md_theme_light_onErrorContainer,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    outline = md_theme_light_outline,
    inverseOnSurface = md_theme_light_inverseOnSurface,
    inverseSurface = md_theme_light_inverseSurface,
    inversePrimary = md_theme_light_inversePrimary,
)

private val DarkColorScheme = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    secondary = md_theme_dark_secondary,
    onSecondary = md_theme_dark_onSecondary,
    secondaryContainer = md_theme_dark_secondaryContainer,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
    tertiary = md_theme_dark_tertiary,
    onTertiary = md_theme_dark_onTertiary,
    tertiaryContainer = md_theme_dark_tertiaryContainer,
    onTertiaryContainer = md_theme_dark_onTertiaryContainer,
    error = md_theme_dark_error,
    errorContainer = md_theme_dark_errorContainer,
    onError = md_theme_dark_onError,
    onErrorContainer = md_theme_dark_onErrorContainer,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
    surfaceVariant = md_theme_dark_surfaceVariant,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
    outline = md_theme_dark_outline,
    inverseOnSurface = md_theme_dark_inverseOnSurface,
    inverseSurface = md_theme_dark_inverseSurface,
    inversePrimary = md_theme_dark_inversePrimary,
)

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