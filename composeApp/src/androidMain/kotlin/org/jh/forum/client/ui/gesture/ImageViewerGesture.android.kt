package org.jh.forum.client.ui.gesture

import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput

actual fun Modifier.imageViewerGestures(
    scale: Float,
    onScaleChange: (Float) -> Unit,
    onOffsetChange: (x: Float, y: Float) -> Unit
): Modifier = pointerInput(scale) {
    detectTransformGestures { _, pan, zoom, _ ->
        val newScale = (scale * zoom).coerceIn(1f, 5f)
        onScaleChange(newScale)

        if (newScale == 1f) {
            onOffsetChange(0f, 0f)
        } else {
            onOffsetChange(pan.x, pan.y)
        }
    }
}