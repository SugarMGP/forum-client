package org.jh.forum.client.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
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
 * Clickable image thumbnail using scale library's TransformImageView
 * Automatically uses transform animations when PreviewerState is provided via CompositionLocal
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
    // Get PreviewerState from composition local if available for transform animations
    val transformState = LocalPreviewerState.current
    val scope = rememberCoroutineScope()

    Box(modifier = modifier) {
        if (transformState != null && imageUrl != null) {
            // Use scale library's TransformImageView for smooth transform animations
            TransformImageView(
                modifier = Modifier.fillMaxSize().clickable {
                    onClick()  // Still call onClick for compatibility
                },
                imageLoader = {
                    val painter = rememberAsyncImagePainter(model = imageUrl)
                    Triple(imageUrl, painter, painter.intrinsicSize)
                },
                transformState = transformState,
            )
        } else {
            // Fallback: regular image without transform
            Image(
                painter = rememberAsyncImagePainter(model = imageUrl),
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize().clickable { onClick() }
            )
        }
        content()
    }
}

/**
 * Image gallery dialog using scale library's ImagePreviewer with transform animations enabled
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

    // Handle dismiss callback when user closes the viewer
    LaunchedEffect(previewerState.canClose) {
        if (previewerState.canClose && visible) {
            onDismiss()
        }
    }

    // Provide PreviewerState to child components for transform animations
    CompositionLocalProvider(LocalPreviewerState provides previewerState) {
        // Render ImagePreviewer using scale library's built-in transform support
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
    }
}

/**
 * Composition local for sharing PreviewerState with child components
 * Enables automatic transform animations when ClickableImage is wrapped by ImageGalleryDialog
 */
val LocalPreviewerState = compositionLocalOf<PreviewerState?> { null }
