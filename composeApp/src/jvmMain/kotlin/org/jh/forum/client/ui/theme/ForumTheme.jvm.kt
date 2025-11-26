package org.jh.forum.client.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.materialkolor.DynamicMaterialTheme

@Composable
actual fun ForumTheme(
    darkTheme: Boolean,
    dynamicColor: Boolean,
    seedColor: Color,
    content: @Composable () -> Unit
) {
    DynamicMaterialTheme(
        seedColor = seedColor,
        isDark = darkTheme,
        animate = true,
        content = content,
        typography = AppTypography()
    )
}

actual fun supportsDynamicColor(): Boolean {
    // Desktop/JVM doesn't support dynamic color from wallpaper
    return false
}
