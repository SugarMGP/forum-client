package org.jh.forum.client.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import com.materialkolor.PaletteStyle
import com.materialkolor.ktx.animateColorScheme
import com.materialkolor.rememberDynamicColorScheme

@Composable
actual fun ForumTheme(
    darkTheme: Boolean,
    dynamicColor: Boolean,
    seedColor: Color,
    paletteStyle: PaletteStyle,
    content: @Composable () -> Unit
) {
    // On Android 12+, get the system Monet color when dynamic color is enabled
    val effectiveSeedColor = if (dynamicColor && supportsDynamicColor()) {
        colorResource(id = android.R.color.system_accent1_500)
    } else {
        seedColor
    }

    // Always use MaterialKolor to generate color scheme with palette style
    val colorScheme = animateColorScheme(
        rememberDynamicColorScheme(
            seedColor = effectiveSeedColor,
            isDark = darkTheme,
            style = paletteStyle
        )
    )

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
        typography = AppTypography()
    )
}

actual fun supportsDynamicColor(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
}
