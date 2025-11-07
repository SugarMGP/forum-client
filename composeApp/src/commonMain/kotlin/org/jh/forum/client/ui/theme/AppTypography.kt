package org.jh.forum.client.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import forum_client.composeapp.generated.resources.HarmonyOS_Sans_SC_Bold
import forum_client.composeapp.generated.resources.HarmonyOS_Sans_SC_Medium
import forum_client.composeapp.generated.resources.HarmonyOS_Sans_SC_Regular
import forum_client.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.Font

@Composable
fun AppTypography(): Typography {
    val interFont = FontFamily(
        Font(resource = Res.font.HarmonyOS_Sans_SC_Regular, weight = FontWeight.Normal),
        Font(resource = Res.font.HarmonyOS_Sans_SC_Medium, weight = FontWeight.Medium),
        Font(resource = Res.font.HarmonyOS_Sans_SC_Bold, weight = FontWeight.Bold),
    )

    return with(MaterialTheme.typography) {
        copy(
            displayLarge = displayLarge.copy(fontFamily = interFont),
            displayMedium = displayMedium.copy(fontFamily = interFont),
            displaySmall = displaySmall.copy(fontFamily = interFont),
            headlineLarge = headlineLarge.copy(fontFamily = interFont),
            headlineMedium = headlineMedium.copy(fontFamily = interFont),
            headlineSmall = headlineSmall.copy(fontFamily = interFont),
            titleLarge = titleLarge.copy(fontFamily = interFont),
            titleMedium = titleMedium.copy(fontFamily = interFont),
            titleSmall = titleSmall.copy(fontFamily = interFont),
            labelLarge = labelLarge.copy(fontFamily = interFont),
            labelMedium = labelMedium.copy(fontFamily = interFont),
            labelSmall = labelSmall.copy(fontFamily = interFont),
            bodyLarge = bodyLarge.copy(fontFamily = interFont),
            bodyMedium = bodyMedium.copy(fontFamily = interFont),
            bodySmall = bodySmall.copy(fontFamily = interFont),
        )
    }
}
