package org.jh.forum.client.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.materialkolor.DynamicMaterialTheme
import com.materialkolor.PaletteStyle

@Composable
actual fun ForumTheme(
    darkTheme: Boolean,
    dynamicColor: Boolean,
    seedColor: Color,
    paletteStyle: PaletteStyle,
    content: @Composable () -> Unit
) {
    DynamicMaterialTheme(
        seedColor = seedColor,
        isDark = darkTheme,
        style = paletteStyle,
        content = content,
        typography = AppTypography()
    )
}

actual fun supportsDynamicColor(): Boolean {
    return false
}
