package org.jh.forum.client.ui.component

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import org.jh.forum.client.ui.theme.AppIcons

/**
 * Gallery viewer with support for multiple images, swipe navigation, and optional shared element transitions
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun ImageGalleryViewer(
    images: List<String>,
    initialIndex: Int = 0,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Guard against empty image list
    if (images.isEmpty()) {
        onDismiss()
        return
    }
    
    val pagerState = rememberPagerState(
        initialPage = initialIndex.coerceIn(0, images.size - 1),
        pageCount = { images.size }
    )
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.7f))
    ) {
        // Horizontal pager for swiping between images
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = true,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            var scale by remember { mutableStateOf(1f) }
            var offsetX by remember { mutableStateOf(0f) }
            var offsetY by remember { mutableStateOf(0f) }
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { if (scale == 1f) onDismiss() }
            ) {
                val imageModifier = Modifier
                    .fillMaxSize()
                    .let { baseModifier ->
                        // Add shared element bounds if both scopes are available
                        if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                            with(sharedTransitionScope) {
                                baseModifier.sharedBounds(
                                    sharedContentState = rememberSharedContentState(key = "image_${images[page]}"),
                                    animatedVisibilityScope = animatedVisibilityScope,
                                    boundsTransform = { _, _ ->
                                        tween(durationMillis = 300)
                                    }
                                )
                            }
                        } else {
                            baseModifier
                        }
                    }
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offsetX,
                        translationY = offsetY
                    )
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 5f)
                            
                            if (scale == 1f) {
                                offsetX = 0f
                                offsetY = 0f
                            } else {
                                offsetX += pan.x
                                offsetY += pan.y
                            }
                        }
                    }
                
                AsyncImage(
                    model = images[page],
                    contentDescription = "图片 ${page + 1}",
                    modifier = imageModifier,
                    contentScale = ContentScale.Fit
                )
            }
        }

        // Close button
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 16.dp, end = 16.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        AppIcons.Close,
                        contentDescription = "关闭",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Image counter at bottom
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
            shape = MaterialTheme.shapes.small
        ) {
            Text(
                text = "${pagerState.currentPage + 1}/${images.size}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

/**
 * Gallery viewer dialog with full screen coverage and optional shared element transitions
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ImageGalleryDialog(
    visible: Boolean,
    images: List<String>,
    initialIndex: Int = 0,
    sharedTransitionScope: SharedTransitionScope? = null,
    onDismiss: () -> Unit
) {
    if (sharedTransitionScope != null) {
        // Use shared element transitions when scope is provided
        // NOTE: Cannot use Dialog with SharedElement as they create separate hierarchies
        // Use a full-screen Box overlay with high zIndex to appear above all content
        with(sharedTransitionScope) {
            AnimatedVisibility(
                visible = visible && images.isNotEmpty(),
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(Float.MAX_VALUE) // Ensure it appears above everything
                ) {
                    ImageGalleryViewer(
                        images = images,
                        initialIndex = initialIndex,
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = this@AnimatedVisibility,
                        onDismiss = onDismiss
                    )
                }
            }
        }
    } else {
        // Fallback without shared elements for screens that don't provide the scope
        if (visible && images.isNotEmpty()) {
            Dialog(
                onDismissRequest = onDismiss,
                properties = DialogProperties(
                    usePlatformDefaultWidth = false,
                    dismissOnBackPress = true,
                    dismissOnClickOutside = false
                )
            ) {
                ImageGalleryViewer(
                    images = images,
                    initialIndex = initialIndex,
                    onDismiss = onDismiss
                )
            }
        }
    }
}
