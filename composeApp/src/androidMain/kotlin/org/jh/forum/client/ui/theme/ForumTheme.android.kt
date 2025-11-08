package org.jh.forum.client.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicColorScheme

@Composable
actual fun ForumTheme(
    darkTheme: Boolean,
    dynamicColor: Boolean,
    seedColor: Color,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    // Dynamic color is available on Android 12+ (API 31+)
    var colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        else -> rememberDynamicColorScheme(
            seedColor = seedColor,
            isDark = darkTheme,
            specVersion = ColorSpec.SpecVersion.SPEC_2025
        )
    }
    
    // In dark mode, lighten the surface colors for better card visibility
    if (darkTheme) {
        colorScheme = colorScheme.copy(
            surface = colorScheme.surface,
            surfaceVariant = colorScheme.surfaceVariant.copy(
                red = minOf(1f, colorScheme.surfaceVariant.red + 0.05f),
                green = minOf(1f, colorScheme.surfaceVariant.green + 0.05f),
                blue = minOf(1f, colorScheme.surfaceVariant.blue + 0.05f)
            ),
            surfaceTint = colorScheme.primary.copy(alpha = 0.2f),
            outline = colorScheme.outline.copy(alpha = 0.5f)
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
        typography = AppTypography()
    )
}

actual fun supportsDynamicColor(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
}
