package org.jh.forum.client.ui.gesture

import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput

@OptIn(ExperimentalComposeUiApi::class)
actual fun Modifier.imageViewerGestures(
    scale: Float,
    onScaleChange: (Float) -> Unit,
    onOffsetChange: (x: Float, y: Float) -> Unit
): Modifier =
    this
        // 鼠标滚轮缩放
        .onPointerEvent(PointerEventType.Scroll) { event ->
            val scroll = event.changes.first().scrollDelta.y
            val zoom = if (scroll < 0) 1.1f else 0.9f
            val newScale = (scale * zoom).coerceIn(1f, 5f)
            onScaleChange(newScale)

            if (newScale == 1f) {
                onOffsetChange(0f, 0f)
            }
        }
        // 拖动 + 触摸缩放（支持触控显示器）
        .pointerInput(scale) {
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