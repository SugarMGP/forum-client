package org.jh.forum.client.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Creates a responsive Typography based on screen width
 * 
 * This function scales all Material 3 typography styles based on the device's screen width,
 * ensuring better readability across different device sizes. The scaling is applied uniformly
 * to font sizes, line heights, and letter spacing.
 * 
 * Scale factors by device category:
 * - Extra small screens (<360dp, e.g., small phones): 0.85x scale
 * - Small screens (360-600dp, e.g., most phones): 0.92x scale
 * - Medium screens (600-840dp, e.g., large phones, small tablets): 1.0x scale (baseline)
 * - Large screens (>840dp, e.g., tablets, desktop): 1.05x scale
 * 
 * @param screenWidthDp The screen width in density-independent pixels (dp)
 * @return Typography with scaled font sizes appropriate for the screen width
 */
@Composable
fun createResponsiveTypography(screenWidthDp: Int): Typography {
    // Calculate scale factor based on screen width
    val scaleFactor = when {
        screenWidthDp < 360 -> 0.85f  // Extra small screens
        screenWidthDp < 600 -> 0.92f  // Small screens (phones)
        screenWidthDp < 840 -> 1.0f   // Medium screens (large phones, small tablets)
        else -> 1.05f                 // Large screens (tablets, desktop)
    }
    
    return Typography(
        displayLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = (57 * scaleFactor).sp,
            lineHeight = (64 * scaleFactor).sp,
            letterSpacing = (-0.25 * scaleFactor).sp
        ),
        displayMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = (45 * scaleFactor).sp,
            lineHeight = (52 * scaleFactor).sp,
            letterSpacing = 0.sp
        ),
        displaySmall = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = (36 * scaleFactor).sp,
            lineHeight = (44 * scaleFactor).sp,
            letterSpacing = 0.sp
        ),
        headlineLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = (32 * scaleFactor).sp,
            lineHeight = (40 * scaleFactor).sp,
            letterSpacing = 0.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = (28 * scaleFactor).sp,
            lineHeight = (36 * scaleFactor).sp,
            letterSpacing = 0.sp
        ),
        headlineSmall = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = (24 * scaleFactor).sp,
            lineHeight = (32 * scaleFactor).sp,
            letterSpacing = 0.sp
        ),
        titleLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = (22 * scaleFactor).sp,
            lineHeight = (28 * scaleFactor).sp,
            letterSpacing = 0.sp
        ),
        titleMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = (16 * scaleFactor).sp,
            lineHeight = (24 * scaleFactor).sp,
            letterSpacing = (0.15 * scaleFactor).sp
        ),
        titleSmall = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = (14 * scaleFactor).sp,
            lineHeight = (20 * scaleFactor).sp,
            letterSpacing = (0.1 * scaleFactor).sp
        ),
        bodyLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = (16 * scaleFactor).sp,
            lineHeight = (24 * scaleFactor).sp,
            letterSpacing = (0.5 * scaleFactor).sp
        ),
        bodyMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = (14 * scaleFactor).sp,
            lineHeight = (20 * scaleFactor).sp,
            letterSpacing = (0.25 * scaleFactor).sp
        ),
        bodySmall = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = (12 * scaleFactor).sp,
            lineHeight = (16 * scaleFactor).sp,
            letterSpacing = (0.4 * scaleFactor).sp
        ),
        labelLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = (14 * scaleFactor).sp,
            lineHeight = (20 * scaleFactor).sp,
            letterSpacing = (0.1 * scaleFactor).sp
        ),
        labelMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = (12 * scaleFactor).sp,
            lineHeight = (16 * scaleFactor).sp,
            letterSpacing = (0.5 * scaleFactor).sp
        ),
        labelSmall = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = (11 * scaleFactor).sp,
            lineHeight = (16 * scaleFactor).sp,
            letterSpacing = (0.5 * scaleFactor).sp
        )
    )
}
