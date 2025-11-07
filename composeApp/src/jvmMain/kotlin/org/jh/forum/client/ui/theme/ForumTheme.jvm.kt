package org.jh.forum.client.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.materialkolor.DynamicMaterialTheme
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicColorScheme
import java.awt.Toolkit

@Composable
actual fun ForumTheme(
    darkTheme: Boolean,
    dynamicColor: Boolean,
    seedColor: Color,
    content: @Composable () -> Unit
) {
    // Get screen width for responsive typography
    val density = LocalDensity.current
    val screenWidthDp = try {
        val screenSize = Toolkit.getDefaultToolkit().screenSize
        (screenSize.width / density.density).toInt()
    } catch (e: Exception) {
        // Fallback to a reasonable default if we can't get screen size
        600
    }
    
    val colorScheme = rememberDynamicColorScheme(
        seedColor = seedColor,
        isDark = darkTheme,
        specVersion = ColorSpec.SpecVersion.SPEC_2025
    )
    
    val typography = createResponsiveTypography(screenWidthDp)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}

actual fun supportsDynamicColor(): Boolean {
    // Desktop/JVM doesn't support dynamic color from wallpaper
    return false
}
