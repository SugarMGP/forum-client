package org.jh.forum.client.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.materialkolor.DynamicMaterialTheme
import com.materialkolor.dynamiccolor.ColorSpec

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
        specVersion = ColorSpec.SpecVersion.SPEC_2025,
        typography = AppTypography()
    )
}

actual fun supportsDynamicColor(): Boolean {
    // WasmJs doesn't support dynamic color from system
    return false
}
