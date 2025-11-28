package org.jh.forum.client.data.preferences

import androidx.compose.ui.graphics.Color

/**
 * Theme preferences for storing user's theme customization choices
 */
object ThemePreferences {
    val availableColors = listOf(
        ThemeColor("red", "红色", Color(0xFFBA1A1A)),
        ThemeColor("pink", "粉红", Color(0xFFB94073)),
        ThemeColor("orange", "橙色", Color(0xFF944A00)),
        ThemeColor("yellow", "黄色", Color(0xFF795900)),

        ThemeColor("lime", "青柠", Color(0xFF5E6400)),
        ThemeColor("green", "绿色", Color(0xFF006D39)),

        ThemeColor("teal", "蓝绿", Color(0xFF006874)),
        ThemeColor("light_blue", "浅蓝", Color(0xFF00639B)),

        ThemeColor("blue", "蓝色", Color(0xFF335BBC)),
        ThemeColor("indigo", "靛蓝", Color(0xFF5355A9)),
        ThemeColor("purple", "紫色", Color(0xFF6750A4)),
    )

    val defaultColor = availableColors.first().color

    data class ThemeColor(
        val key: String,
        val name: String,
        val color: Color
    )
}
