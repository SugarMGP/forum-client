package org.jh.forum.client.data.preferences

import androidx.compose.ui.graphics.Color

/**
 * Theme preferences for storing user's theme customization choices
 */
object ThemePreferences {
    /**
     * Material 3 optimized seed colors
     * Based on InstallerX-Revived color palette
     */
    val availableColors = listOf(
        ThemeColor("default", "默认", Color(0xFF4A672D)),
        ThemeColor("pink", "粉红", Color(0xFFB94073)),
        ThemeColor("red", "红色", Color(0xFFBA1A1A)),
        ThemeColor("orange", "橙色", Color(0xFF944A00)),
        ThemeColor("amber", "琥珀", Color(0xFF8C5300)),
        ThemeColor("yellow", "黄色", Color(0xFF795900)),
        ThemeColor("lime", "青柠", Color(0xFF5E6400)),
        ThemeColor("green", "绿色", Color(0xFF006D39)),
        ThemeColor("cyan", "青色", Color(0xFF006A64)),
        ThemeColor("teal", "蓝绿", Color(0xFF006874)),
        ThemeColor("light_blue", "浅蓝", Color(0xFF00639B)),
        ThemeColor("blue", "蓝色", Color(0xFF335BBC)),
        ThemeColor("indigo", "靛蓝", Color(0xFF5355A9)),
        ThemeColor("purple", "紫色", Color(0xFF6750A4)),
        ThemeColor("deep_purple", "深紫", Color(0xFF7E42A4)),
        ThemeColor("blue_grey", "蓝灰", Color(0xFF575D7E)),
        ThemeColor("brown", "棕色", Color(0xFF7D524A)),
        ThemeColor("grey", "灰色", Color(0xFF5F6162))
    )

    /**
     * Default seed color
     */
    val defaultColor = availableColors.first().color

    data class ThemeColor(
        val key: String,
        val name: String,
        val color: Color
    )
}
