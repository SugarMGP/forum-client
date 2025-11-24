package org.jh.forum.client.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import com.jvziyaoyao.scale.image.previewer.ImagePreviewer
import com.jvziyaoyao.scale.zoomable.pager.PagerGestureScope
import com.jvziyaoyao.scale.zoomable.previewer.TransformLayerScope
import com.jvziyaoyao.scale.zoomable.previewer.VerticalDragType
import com.jvziyaoyao.scale.zoomable.previewer.rememberPreviewerState
import kotlinx.coroutines.launch

/**
 * Clickable image thumbnail - simple AsyncImage wrapper
 */
@Composable
fun ClickableImage(
    imageUrl: String?,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    shape: androidx.compose.ui.graphics.Shape = MaterialTheme.shapes.medium,
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit = {}
) {
    Box(modifier = modifier) {
        AsyncImage(
            model = imageUrl,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = Modifier.fillMaxSize()
        )
        content()
    }
}

/**
 * Image gallery using scale library's ImagePreviewer
 */
@Composable
fun ImageGalleryDialog(
    visible: Boolean,
    images: List<String>,
    initialIndex: Int = 0,
    onDismiss: () -> Unit
) {
    if (images.isEmpty()) return

    val scope = rememberCoroutineScope()
    val previewerState = rememberPreviewerState(
        verticalDragType = VerticalDragType.Down,
        pageCount = { images.size },
        getKey = { images[it] }
    )

    // Open when visible changes to true
    LaunchedEffect(visible, initialIndex) {
        if (visible) {
            previewerState.open(initialIndex)
        }
    }

    // Handle close callback
    LaunchedEffect(previewerState.canClose) {
        if (previewerState.canClose) {
            onDismiss()
        }
    }

    ImagePreviewer(
        state = previewerState,
        detectGesture = PagerGestureScope(
            onTap = {
                scope.launch {
                    previewerState.close()
                }
            }
        ),
        imageLoader = { index ->
            val painter = rememberAsyncImagePainter(model = images[index])
            Pair(painter, painter.intrinsicSize)
        },
        previewerLayer = TransformLayerScope(
            background = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.9f))
                )
            }
        )
    )
}
