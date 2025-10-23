package org.jh.forum.client.ui.component

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
import org.jh.forum.client.ui.theme.AppIcons

/**
 * Data class to hold thumbnail position and size information
 */
data class ThumbnailBounds(
    val position: Offset,
    val size: Size
)

/**
 * Full-screen image viewer with zoom and pan support
 */
@Composable
fun ImageViewer(
    imageUrl: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.7f))
            .clickable(onClick = onDismiss)
    ) {
        // Image with zoom and pan
        AsyncImage(
            model = imageUrl,
            contentDescription = "全屏图片",
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offsetX,
                    translationY = offsetY
                )
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 5f)
                        
                        // Reset offset when scale is 1
                        if (scale == 1f) {
                            offsetX = 0f
                            offsetY = 0f
                        } else {
                            offsetX += pan.x
                            offsetY += pan.y
                        }
                    }
                },
            contentScale = ContentScale.Fit
        )

        // Close button
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 48.dp, end = 16.dp)
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

        // Zoom hint
        if (scale > 1f) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = "双指缩放: ${String.format("%.1f", scale)}x",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}

/**
 * Full-screen image viewer dialog
 */
@Composable
fun ImageViewerDialog(
    visible: Boolean,
    imageUrl: String?,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = visible && imageUrl != null,
        enter = fadeIn(animationSpec = tween(300)) + scaleIn(
            initialScale = 0.8f,
            animationSpec = tween(300)
        ),
        exit = fadeOut(animationSpec = tween(300)) + scaleOut(
            targetScale = 0.8f,
            animationSpec = tween(300)
        )
    ) {
        if (imageUrl != null) {
            ImageViewer(
                imageUrl = imageUrl,
                onDismiss = onDismiss
            )
        }
    }
}

/**
 * Image viewer with shared element-like transition
 * Animates from thumbnail position to fullscreen
 */
@Composable
fun SharedElementImageViewer(
    imageUrl: String,
    thumbnailBounds: ThumbnailBounds?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    
    // Animation state
    var isAnimatingIn by remember { mutableStateOf(true) }
    val animationProgress = remember { Animatable(0f) }
    
    // Zoom and pan state (for pinch-to-zoom)
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    
    // Get screen size from the container
    var screenWidth by remember { mutableStateOf(1080f) }
    var screenHeight by remember { mutableStateOf(1920f) }
    
    // Start animation when composable enters
    LaunchedEffect(Unit) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        isAnimatingIn = false
    }
    
    // Handle dismiss with reverse animation
    val handleDismiss: () -> Unit = {
        if (!isAnimatingIn && scale == 1f) {
            coroutineScope.launch {
                animationProgress.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
                onDismiss()
            }
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                screenWidth = coordinates.size.width.toFloat()
                screenHeight = coordinates.size.height.toFloat()
            }
            .background(
                MaterialTheme.colorScheme.scrim.copy(
                    alpha = 0.7f * animationProgress.value
                )
            )
    ) {
        // Calculate interpolated position and scale
        val currentProgress = animationProgress.value
        
        val startX = thumbnailBounds?.position?.x ?: (screenWidth / 2)
        val startY = thumbnailBounds?.position?.y ?: (screenHeight / 2)
        val startWidth = thumbnailBounds?.size?.width ?: 100f
        val startHeight = thumbnailBounds?.size?.height ?: 100f
        
        // Target is centered fullscreen
        val targetX = screenWidth / 2
        val targetY = screenHeight / 2
        
        // Calculate scale based on aspect ratio
        val startScale = minOf(startWidth / screenWidth, startHeight / screenHeight)
        val targetScale = 1f
        
        val currentScale = lerp(startScale, targetScale, currentProgress)
        val currentX = lerp(startX, targetX, currentProgress)
        val currentY = lerp(startY, targetY, currentProgress)
        
        // Image with animation
        AsyncImage(
            model = imageUrl,
            contentDescription = "全屏图片",
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = currentScale * scale,
                    scaleY = currentScale * scale,
                    translationX = (currentX - targetX) + offsetX,
                    translationY = (currentY - targetY) + offsetY
                )
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        if (!isAnimatingIn && currentProgress >= 0.99f) {
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
                }
                .clickable(enabled = scale == 1f && !isAnimatingIn) {
                    handleDismiss()
                },
            contentScale = ContentScale.Fit
        )

        // Close button (only show when animation is complete)
        if (currentProgress >= 0.99f) {
            IconButton(
                onClick = handleDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(top = 48.dp, end = 16.dp)
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
        }

        // Zoom hint
        if (scale > 1f && currentProgress >= 0.99f) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = "双指缩放: ${String.format("%.1f", scale)}x",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}

// Linear interpolation helper
private fun lerp(start: Float, end: Float, fraction: Float): Float {
    return start + (end - start) * fraction
}

/**
 * Gallery viewer with support for multiple images and swipe navigation
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImageGalleryViewer(
    images: List<String>,
    initialIndex: Int = 0,
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
                AsyncImage(
                    model = images[page],
                    contentDescription = "图片 ${page + 1}",
                    modifier = Modifier
                        .fillMaxSize()
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
                        },
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
                .padding(top = 48.dp, end = 16.dp)
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
 * Gallery viewer dialog with animations
 */
@Composable
fun ImageGalleryDialog(
    visible: Boolean,
    images: List<String>,
    initialIndex: Int = 0,
    thumbnailBounds: ThumbnailBounds? = null,
    onDismiss: () -> Unit
) {
    // Use shared element transition if thumbnail bounds provided
    if (visible && images.isNotEmpty() && thumbnailBounds != null) {
        SharedElementImageGalleryViewer(
            images = images,
            initialIndex = initialIndex,
            thumbnailBounds = thumbnailBounds,
            onDismiss = onDismiss
        )
    } else {
        // Fallback to simple animation
        AnimatedVisibility(
            visible = visible && images.isNotEmpty(),
            enter = fadeIn(animationSpec = tween(300)) + scaleIn(
                initialScale = 0.8f,
                animationSpec = tween(300)
            ),
            exit = fadeOut(animationSpec = tween(300)) + scaleOut(
                targetScale = 0.8f,
                animationSpec = tween(300)
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

/**
 * Gallery viewer with shared element-like transition
 * Animates from thumbnail position to fullscreen with swipe support
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SharedElementImageGalleryViewer(
    images: List<String>,
    initialIndex: Int = 0,
    thumbnailBounds: ThumbnailBounds?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (images.isEmpty()) {
        onDismiss()
        return
    }
    
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    
    val pagerState = rememberPagerState(
        initialPage = initialIndex.coerceIn(0, images.size - 1),
        pageCount = { images.size }
    )
    
    // Animation state
    var isAnimatingIn by remember { mutableStateOf(true) }
    val animationProgress = remember { Animatable(0f) }
    
    // Get screen size from container
    var screenWidth by remember { mutableStateOf(1080f) }
    var screenHeight by remember { mutableStateOf(1920f) }
    
    // Start animation when composable enters
    LaunchedEffect(Unit) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        isAnimatingIn = false
    }
    
    // Handle dismiss with reverse animation
    val handleDismiss: () -> Unit = {
        if (!isAnimatingIn) {
            coroutineScope.launch {
                animationProgress.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
                onDismiss()
            }
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                screenWidth = coordinates.size.width.toFloat()
                screenHeight = coordinates.size.height.toFloat()
            }
            .background(
                MaterialTheme.colorScheme.scrim.copy(
                    alpha = 0.7f * animationProgress.value
                )
            )
    ) {
        // Calculate interpolated position and scale
        val currentProgress = animationProgress.value
        
        val startX = thumbnailBounds?.position?.x ?: (screenWidth / 2)
        val startY = thumbnailBounds?.position?.y ?: (screenHeight / 2)
        val startWidth = thumbnailBounds?.size?.width ?: 100f
        val startHeight = thumbnailBounds?.size?.height ?: 100f
        
        val targetX = screenWidth / 2
        val targetY = screenHeight / 2
        
        val startScale = minOf(startWidth / screenWidth, startHeight / screenHeight)
        val targetScale = 1f
        
        val currentScale = lerp(startScale, targetScale, currentProgress)
        val currentX = lerp(startX, targetX, currentProgress)
        val currentY = lerp(startY, targetY, currentProgress)
        
        // Horizontal pager for swiping between images
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = currentProgress >= 0.99f,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = currentScale,
                    scaleY = currentScale,
                    translationX = currentX - targetX,
                    translationY = currentY - targetY
                )
        ) { page ->
            var scale by remember { mutableStateOf(1f) }
            var offsetX by remember { mutableStateOf(0f) }
            var offsetY by remember { mutableStateOf(0f) }
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(enabled = scale == 1f && currentProgress >= 0.99f) {
                        handleDismiss()
                    }
            ) {
                AsyncImage(
                    model = images[page],
                    contentDescription = "图片 ${page + 1}",
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offsetX,
                            translationY = offsetY
                        )
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                if (currentProgress >= 0.99f) {
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
                        },
                    contentScale = ContentScale.Fit
                )
            }
        }

        // Close button (only show when animation is complete)
        if (currentProgress >= 0.99f) {
            IconButton(
                onClick = handleDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(top = 48.dp, end = 16.dp)
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
        }

        // Image counter at bottom (only show when animation is complete)
        if (currentProgress >= 0.99f) {
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
}
