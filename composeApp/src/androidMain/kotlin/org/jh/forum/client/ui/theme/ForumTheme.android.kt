package org.jh.forum.client.ui.theme

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
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
    val effectiveSeedColor = if (dynamicColor && supportsDynamicColor()) {
        colorResource(id = android.R.color.system_accent1_500)
    } else {
        seedColor
    }

    DynamicMaterialTheme(
        seedColor = effectiveSeedColor,
        isDark = darkTheme,
        animate = true,
        style = paletteStyle,
        content = content,
        typography = AppTypography(),
    )
}

actual fun supportsDynamicColor(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
}
