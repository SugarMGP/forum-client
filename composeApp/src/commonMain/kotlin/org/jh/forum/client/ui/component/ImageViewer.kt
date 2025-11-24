package org.jh.forum.client.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
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
 * Clickable image thumbnail with press animation
 * When transformState is provided, uses TransformImageView for smooth zoom animations
 */
@Composable
fun ClickableImage(
    imageUrl: String?,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    shape: androidx.compose.ui.graphics.Shape = MaterialTheme.shapes.medium,
    onClick: () -> Unit,
    transformState: PreviewerState? = null,
    transformIndex: Int = 0,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(100)
    )
    val scope = rememberCoroutineScope()

    Box(modifier = modifier) {
        if (transformState != null && imageUrl != null) {
            // Use TransformImageView for smooth transform animations
            TransformImageView(
                modifier = Modifier
                    .fillMaxSize()
                    .scale(scale)
                    .clip(shape)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = {
                            scope.launch {
                                transformState.enterTransform(transformIndex)
                            }
                        }
                    ),
                imageLoader = {
                    val painter = rememberAsyncImagePainter(model = imageUrl)
                    // Return key, painter, and intrinsic size
                    Triple(imageUrl, painter, painter.intrinsicSize)
                },
                transformState = transformState,
            )
        } else {
            // Regular AsyncImage without transform
            AsyncImage(
                model = imageUrl,
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = Modifier
                    .fillMaxSize()
                    .scale(scale)
                    .clip(shape)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
            )
        }
        content()
    }
}

/**
 * Image gallery dialog using scale library's ImagePreviewer
 * Supports both regular open/close and transform animations
 */
@Composable
fun ImageGalleryDialog(
    visible: Boolean,
    images: List<String>,
    initialIndex: Int = 0,
    onDismiss: () -> Unit,
    useTransform: Boolean = false
) {
    if (images.isEmpty()) return

    val scope = rememberCoroutineScope()
    val previewerState = rememberPreviewerState(
        verticalDragType = VerticalDragType.Down,
        pageCount = { images.size },
        getKey = { images[it] }
    )

    // Handle visibility changes
    LaunchedEffect(visible, initialIndex) {
        if (visible && !previewerState.visible) {
            if (useTransform) {
                // When using transform, the TransformImageView will trigger enterTransform
                // We don't auto-open here
            } else {
                previewerState.open(initialIndex)
            }
        }
    }

    // Handle dismiss callback when user closes the viewer
    LaunchedEffect(previewerState.canClose) {
        if (previewerState.canClose && visible) {
            onDismiss()
        }
    }

    // Render ImagePreviewer
    ImagePreviewer(
        state = previewerState,
        detectGesture = PagerGestureScope(
            onTap = {
                scope.launch {
                    if (useTransform) {
                        previewerState.exitTransform()
                    } else {
                        previewerState.close()
                    }
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
        ),
        pageDecoration = { page, innerPage ->
            var mounted = false
            Box(modifier = Modifier.fillMaxSize()) {
                mounted = innerPage()
                
                // Show page indicator for multiple images
                if (images.size > 1) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(24.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "${page + 1}/${images.size}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }
            mounted
        }
    )

    // Provide the previewer state through composition local
    // This allows ClickableImage to access it for transform animations
    CompositionLocalProvider(LocalPreviewerState provides previewerState) {
        // State is available to children
    }
}

/**
 * Composition local for sharing PreviewerState with child components
 */
val LocalPreviewerState = compositionLocalOf<PreviewerState?> { null }
