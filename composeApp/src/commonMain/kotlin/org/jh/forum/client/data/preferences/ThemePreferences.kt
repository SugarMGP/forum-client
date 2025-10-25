package org.jh.forum.client.data.preferences

import androidx.compose.ui.graphics.Color

/**
 * Theme preferences for storing user's theme customization choices
 */
object ThemePreferences {
    val availableColors = listOf(
        ThemeColor("红色", Color.Red),
        ThemeColor("绿色", Color.Green),
        ThemeColor("蓝色", Color.Blue),
        ThemeColor("黄色", Color.Yellow),
        ThemeColor("青色", Color.Cyan),
        ThemeColor("品红", Color.Magenta)
    )

    data class ThemeColor(
        val name: String,
        val color: Color
    )
}
