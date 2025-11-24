package org.jh.forum.client.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil3.compose.rememberAsyncImagePainter
import com.jvziyaoyao.scale.image.previewer.ImagePreviewer
import com.jvziyaoyao.scale.image.previewer.TransformImageView
import com.jvziyaoyao.scale.zoomable.pager.PagerGestureScope
import com.jvziyaoyao.scale.zoomable.previewer.PreviewerState
import com.jvziyaoyao.scale.zoomable.previewer.TransformLayerScope
import com.jvziyaoyao.scale.zoomable.previewer.VerticalDragType
import com.jvziyaoyao.scale.zoomable.previewer.rememberPreviewerState
import kotlinx.coroutines.launch

/**
 * Composition local for sharing PreviewerState across the screen
 */
val LocalPreviewerState = compositionLocalOf<PreviewerState?> { null }

/**
 * Clickable image thumbnail using TransformImageView for transform animations
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
    val previewerState = LocalPreviewerState.current
    val scope = rememberCoroutineScope()
    
    Box(modifier = modifier) {
        if (imageUrl != null && previewerState != null) {
            TransformImageView(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shape)
                    .clickable {
                        onClick()
                    },
                imageLoader = {
                    val painter = rememberAsyncImagePainter(model = imageUrl)
                    Triple(imageUrl, painter, painter.intrinsicSize)
                },
                transformState = previewerState,
            )
        }
        content()
    }
}

/**
 * Image gallery with transform animations
 * Must be used with ImageGalleryProvider to share PreviewerState
 */
@Composable
fun ImageGalleryDialog(
    visible: Boolean,
    images: List<String>,
    initialIndex: Int = 0,
    onDismiss: () -> Unit,
    onIndexChange: (Int) -> Unit = {}
) {
    if (images.isEmpty()) return

    val scope = rememberCoroutineScope()
    val previewerState = LocalPreviewerState.current ?: return

    // Trigger enterTransform when visible changes and we have an index
    LaunchedEffect(visible, initialIndex) {
        if (visible) {
            previewerState.enterTransform(initialIndex)
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
                    previewerState.exitTransform()
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

/**
 * Provider that wraps screen content to enable transform animations
 * Usage: Wrap your screen content with this to enable image transform animations
 */
@Composable
fun ImageGalleryProvider(
    images: List<String>,
    content: @Composable () -> Unit
) {
    val previewerState = rememberPreviewerState(
        verticalDragType = VerticalDragType.Down,
        pageCount = { images.size },
        getKey = { images.getOrNull(it) ?: "" }
    )

    CompositionLocalProvider(LocalPreviewerState provides previewerState) {
        content()
    }
}
