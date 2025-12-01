package org.jh.forum.client.util

import androidx.compose.runtime.*
import kotlinx.coroutines.delay

/**
 * Creates a debounced click handler that prevents multiple rapid clicks
 * @param delayMillis The delay in milliseconds before allowing another click
 * @param onClick The action to perform on click
 * @return A debounced click handler
 */
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
