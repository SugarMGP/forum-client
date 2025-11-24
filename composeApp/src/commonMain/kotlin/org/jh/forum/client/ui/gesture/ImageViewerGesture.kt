package org.jh.forum.client.ui.gesture

import androidx.compose.ui.Modifier

expect fun Modifier.imageViewerGestures(
    scale: Float,
    onScaleChange: (Float) -> Unit,
    onOffsetChange: (x: Float, y: Float) -> Unit
): Modifier