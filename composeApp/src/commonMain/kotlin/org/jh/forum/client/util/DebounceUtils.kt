package org.jh.forum.client.util

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.semantics.Role

@Composable
fun rememberDebouncedClick(
    delayMillis: Long = 500L,
    onClick: () -> Unit
): () -> Unit {
    var lastClickTime by remember { mutableStateOf(0L) }

    return {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime >= delayMillis) {
            lastClickTime = currentTime
            onClick()
        }
    }
}

fun Modifier.debouncedClickable(
    delayMillis: Long = 500L,
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    interactionSource: MutableInteractionSource? = null,
    onClick: () -> Unit,
): Modifier = composed {
    var lastClickTime by remember { mutableStateOf(0L) }

    clickable(enabled = enabled, onClickLabel = onClickLabel, role = role, interactionSource = interactionSource) {
        val now = System.currentTimeMillis()
        if (now - lastClickTime >= delayMillis) {
            lastClickTime = now
            onClick()
        }
    }
}
