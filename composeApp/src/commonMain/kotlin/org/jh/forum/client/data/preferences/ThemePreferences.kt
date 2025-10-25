package org.jh.forum.client.data.preferences

import androidx.compose.ui.graphics.Color

/**
 * Theme preferences for storing user's theme customization choices
 */
object ThemePreferences {
    // Available theme seed colors
    val Red = Color(0xFFFF0000)
    val Green = Color(0xFF00FF00)
    val Blue = Color(0xFF0000FF)
    val Yellow = Color(0xFFFFFF00)
    val Cyan = Color(0xFF00FFFF)
    val Magenta = Color(0xFFFF00FF)
    val Orange = Color(0xFFFF6600)
    val Purple = Color(0xFF9C27B0)

    val availableColors = listOf(
        ThemeColor("红色", Red),
        ThemeColor("绿色", Green),
        ThemeColor("蓝色", Blue),
        ThemeColor("黄色", Yellow),
        ThemeColor("青色", Cyan),
        ThemeColor("品红", Magenta),
        ThemeColor("橙色", Orange),
        ThemeColor("紫色", Purple)
    )

    data class ThemeColor(
        val name: String,
        val color: Color
    )
}
