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
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        else -> rememberDynamicColorScheme(
            seedColor = seedColor,
            isDark = darkTheme,
            specVersion = ColorSpec.SpecVersion.SPEC_2025
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
