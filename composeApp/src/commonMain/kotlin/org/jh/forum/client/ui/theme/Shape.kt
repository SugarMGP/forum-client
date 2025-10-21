package org.jh.forum.client.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Define shapes following Material 3 guidelines
val ForumShapes = Shapes(
    // Extra small components like chips
    extraSmall = RoundedCornerShape(4.dp),
    // Small components like buttons
    small = RoundedCornerShape(8.dp),
    // Medium components like cards
    medium = RoundedCornerShape(12.dp),
    // Large components like sheets
    large = RoundedCornerShape(16.dp),
    // Extra large components like dialogs
    extraLarge = RoundedCornerShape(28.dp)
)
