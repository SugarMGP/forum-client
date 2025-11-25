package org.jh.forum.client.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.More
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import org.jh.forum.client.data.model.CommentInfoResponse
import org.jh.forum.client.data.model.ReplyElement
import org.jh.forum.client.di.AppModule
import org.jh.forum.client.ui.component.CommentEditor
import org.jh.forum.client.ui.component.ImageGalleryDialog
import org.jh.forum.client.ui.theme.AppIcons
import org.jh.forum.client.ui.theme.Dimensions
import org.jh.forum.client.ui.viewmodel.ReplyViewModel
import org.jh.forum.client.util.TimeUtils

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun CommentRepliesScreen(
    commentId: Long,
    highlightReplyId: Long,
    viewModel: ReplyViewModel,
    onBack: () -> Unit,
    onUserClick: (Long) -> Unit = {}
) {
    val commentInfo by viewModel.commentInfo.collectAsState()
    val replies by viewModel.replies.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val hasMore by viewModel.hasMore.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var showReplyDialog by remember { mutableStateOf(false) }
    var replyTarget by remember { mutableStateOf<ReplyElement?>(null) }
    var showImageViewer by remember { mutableStateOf(false) }
    var selectedImageUrl by remember { mutableStateOf<String?>(null) }

    val authViewModel = AppModule.authViewModel
    val currentUserId = authViewModel.userProfile.collectAsState().value?.userId

    val listState = rememberLazyListState()

    LaunchedEffect(commentId, highlightReplyId) {
        val param = if (highlightReplyId > 0) highlightReplyId else null
        viewModel.setHighlightReplyId(param)
        viewModel.loadReplies(commentId, true, param)
    }

    // Auto-pagination logic
    LaunchedEffect(listState, isLoading, hasMore, errorMessage) {
        snapshotFlow {
            val layout = listState.layoutInfo
            val visible = layout.visibleItemsInfo
            val lastVisibleIndex = visible.lastOrNull()?.index ?: -1
            val totalCount = layout.totalItemsCount
            Triple(lastVisibleIndex, totalCount, visible.size)
        }
            .distinctUntilChanged()
            .collect { (lastVisible, totalCount, visibleSize) ->
                // Prevent retry on error
                if (!hasMore || isLoading || errorMessage != null) return@collect
                if (totalCount <= 0 || lastVisible < 0) return@collect

                val threshold = 3
                if (lastVisible >= totalCount - 1 - threshold && lastVisible < totalCount) {
                    viewModel.loadReplies(commentId)
                }
            }
    }

    // Scroll to highlighted reply when replies load
    LaunchedEffect(highlightReplyId, replies.size) {
        if (highlightReplyId > 0 && replies.isNotEmpty()) {
            val highlightIndex = replies.indexOfFirst { it.replyId == highlightReplyId }
            if (highlightIndex >= 0) {
                // Scroll to the highlighted reply (add offset for header items)
                // +2 accounts for comment item and replies title item
                listState.animateScrollToItem(highlightIndex + 2)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("评论详情") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(AppIcons.ArrowBack, contentDescription = "返回")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = Dimensions.spaceMedium),
                verticalArrangement = Arrangement.spacedBy(Dimensions.spaceSmall)
            ) {
                // Original comment
                item {
                    commentInfo?.let { comment ->
                        OriginalCommentItem(
                            comment = comment,
                            onUpvote = { viewModel.upvoteComment(commentId) },
                            onUserProfileClick = {
                                comment.publisherInfo.id?.let { onUserClick(it) }
                            },
                            onImageClick = { imageUrl ->
                                selectedImageUrl = imageUrl
                                showImageViewer = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Replies header
                item {
                    Text(
                        text = "回复（${replies.size}）",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(
                            horizontal = Dimensions.spaceMedium,
                            vertical = Dimensions.spaceSmall
                        )
                    )
                }

                // Empty state
                if (replies.isEmpty() && !isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Dimensions.spaceMedium),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "暂无回复",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Replies list
                if (replies.isNotEmpty()) {
                    items(
                        items = replies,
                        key = { reply -> reply.replyId }
                    ) { reply ->
                        val isHighlighted = reply.replyId == highlightReplyId
                        ReplyItem(
                            reply = reply,
                            onUpvote = { viewModel.upvoteReply(reply.replyId) },
                            onDelete = if (currentUserId != null && currentUserId == reply.publisherInfo.id) {
                                { viewModel.deleteReply(reply.replyId) }
                            } else null,
                            onUserProfileClick = { userId ->
                                onUserClick(userId)
                            },
                            onImageClick = { imageUrl ->
                                selectedImageUrl = imageUrl
                                showImageViewer = true
                            },
                            onReply = {
                                replyTarget = reply
                                showReplyDialog = true
                            },
                            isHighlighted = isHighlighted
                        )
                    }
                }

                // Bottom space for FAB
                item {
                    Spacer(modifier = Modifier.height(Dimensions.avatarExtraLarge))
                }

                // Loading indicator
                if (isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Dimensions.spaceMedium),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }

                // Error message
                if (errorMessage != null) {
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Dimensions.spaceMedium),
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                errorMessage ?: "",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(Dimensions.spaceMedium)
                            )
                        }
                    }
                }
            }

            // Floating reply editor - same pattern as PostDetailScreen
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        horizontal = Dimensions.buttonHeightLarge,
                        vertical = Dimensions.spaceMedium
                    )
            ) {
                // Floating action button
                if (!showReplyDialog) {
                    FloatingActionButton(
                        onClick = {
                            replyTarget = null
                            showReplyDialog = true
                        },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        elevation = FloatingActionButtonDefaults.elevation(
                            defaultElevation = 6.dp,
                            pressedElevation = 8.dp,
                            hoveredElevation = 8.dp
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = AppIcons.Comment,
                                contentDescription = "发表回复"
                            )
                            Text(
                                text = "回复",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }

                // Reply editor - slides up from bottom
                AnimatedVisibility(
                    visible = showReplyDialog,
                    enter = slideInVertically(
                        initialOffsetY = { fullHeight -> fullHeight + 100 }
                    ),
                    exit = slideOutVertically(
                        targetOffsetY = { fullHeight -> fullHeight + 100 }
                    ),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        shadowElevation = 8.dp,
                        shape = RoundedCornerShape(16.dp),
                        tonalElevation = Dimensions.elevationMedium,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    ) {
                        Surface(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            Column {
                                // Header with title and close button
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        if (replyTarget != null) "回复 @${replyTarget?.publisherInfo?.nickname}" else "回复评论",
                                        style = MaterialTheme.typography.titleLarge,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.clickable {
                                            showReplyDialog = false
                                            replyTarget = null
                                        }
                                    ) {
                                        Icon(
                                            imageVector = AppIcons.Close,
                                            contentDescription = "关闭",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                }

                                Spacer(Modifier.height(12.dp))

                                // Reply editor
                                CommentEditor(
                                    onSubmit = { content, picture ->
                                        viewModel.publishReply(
                                            commentId,
                                            replyTarget?.replyId ?: commentId,
                                            content,
                                            picture
                                        )
                                        showReplyDialog = false
                                        replyTarget = null
                                    },
                                    shouldRequestFocus = true,
                                    focusDelayMillis = 350,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Image viewer dialog
    ImageGalleryDialog(
        visible = showImageViewer,
        images = selectedImageUrl?.let { listOf(it) } ?: emptyList(),
        initialIndex = 0,
        onDismiss = {
            showImageViewer = false
            selectedImageUrl = null
        }
    )
}

@Composable
fun OriginalCommentItem(
    comment: CommentInfoResponse,
    onUpvote: () -> Unit,
    onUserProfileClick: () -> Unit,
    onImageClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimensions.spaceMedium, vertical = Dimensions.spaceSmall),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = Dimensions.elevationSmall
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.spaceMedium)
        ) {
            // User info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .clickable { onUserProfileClick() }
                ) {
                    AsyncImage(
                        model = comment.publisherInfo.avatar,
                        contentDescription = "用户头像",
                        modifier = Modifier
                            .size(Dimensions.avatarLarge)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.width(Dimensions.spaceSmall))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = comment.publisherInfo.nickname ?: "未知用户",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (comment.isAuthor) {
                                Spacer(Modifier.width(Dimensions.spaceExtraSmall))
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text(
                                        text = "楼主",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(
                                            horizontal = Dimensions.spaceSmall,
                                            vertical = Dimensions.spaceExtraSmall
                                        )
                                    )
                                }
                            }
                        }
                        Text(
                            text = TimeUtils.formatTime(comment.createdAt),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Content
            Spacer(Modifier.height(Dimensions.spaceMedium))
            Text(
                text = comment.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Images
            if (comment.pictures.isNotEmpty()) {
                Spacer(Modifier.height(Dimensions.spaceSmall))
                comment.pictures.forEach { picture ->
                    AsyncImage(
                        model = picture.url,
                        contentDescription = "评论图片",
                        modifier = Modifier
                            .size(Dimensions.imagePreviewMedium)
                            .clip(MaterialTheme.shapes.medium)
                            .clickable {
                                picture.url?.let { onImageClick(it) }
                            },
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(Modifier.height(Dimensions.spaceMedium))

            // Actions
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Upvote button - 使用OutlinedButton匹配帖子列表风格
                OutlinedButton(
                    onClick = onUpvote,
                    modifier = Modifier.height(Dimensions.buttonHeightSmall),
                    shape = MaterialTheme.shapes.small,
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (comment.isLiked) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        contentColor = if (comment.isLiked) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    ),
                    contentPadding = PaddingValues(
                        horizontal = Dimensions.spaceMedium,
                        vertical = Dimensions.spaceSmall
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            AppIcons.ThumbUp,
                            contentDescription = "点赞",
                            modifier = Modifier.size(Dimensions.iconSmall)
                        )
                        Spacer(modifier = Modifier.width(Dimensions.spaceSmall))
                        Text(
                            text = "${comment.upvoteCount}",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReplyItem(
    reply: ReplyElement,
    onUpvote: () -> Unit,
    onDelete: (() -> Unit)? = null,
    onUserProfileClick: (Long) -> Unit = {},
    onImageClick: (String) -> Unit = {},
    onReply: () -> Unit,
    isHighlighted: Boolean = false,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    // Blink animation for highlighted reply
    var highlightAlpha by remember(isHighlighted) { mutableStateOf(if (isHighlighted) 1f else 0f) }

    LaunchedEffect(isHighlighted) {
        if (isHighlighted) {
            // Blink 3 times
            repeat(3) {
                highlightAlpha = 1f
                delay(300)
                highlightAlpha = 0f
                delay(300)
            }
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimensions.spaceMedium, vertical = Dimensions.spaceSmall),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = Dimensions.elevationSmall,
        onClick = onReply
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = highlightAlpha * 0.3f)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimensions.spaceMedium)
            ) {
                // User info with menu
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .clickable {
                                reply.publisherInfo.id?.let { onUserProfileClick(it) }
                            }
                    ) {
                        AsyncImage(
                            model = reply.publisherInfo.avatar,
                            contentDescription = "用户头像",
                            modifier = Modifier
                                .size(Dimensions.avatarMedium)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.width(Dimensions.spaceSmall))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = reply.publisherInfo.nickname ?: "未知用户",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (reply.isAuthor) {
                                    Spacer(Modifier.width(Dimensions.spaceExtraSmall))
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = MaterialTheme.shapes.small
                                    ) {
                                        Text(
                                            text = "楼主",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(
                                                horizontal = Dimensions.spaceSmall,
                                                vertical = Dimensions.spaceExtraSmall
                                            )
                                        )
                                    }
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (reply.targetUser != null) {
                                    Text(
                                        text = "回复 @${reply.targetUser.nickname}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(Modifier.width(Dimensions.spaceExtraSmall))
                                    Text(
                                        text = "·",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.width(Dimensions.spaceExtraSmall))
                                }
                                Text(
                                    text = TimeUtils.formatTime(reply.createdAt),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // More options menu
                    if (onDelete != null) {
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.More,
                                    "更多选项",
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("删除") },
                                    onClick = {
                                        onDelete()
                                        showMenu = false
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Delete, null)
                                    }
                                )
                            }
                        }
                    }
                }

                // Content
                Spacer(Modifier.height(Dimensions.spaceSmall))
                Text(
                    text = reply.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Images
                if (reply.pictures.isNotEmpty()) {
                    Spacer(Modifier.height(Dimensions.spaceSmall))
                    reply.pictures.forEach { picture ->
                        AsyncImage(
                            model = picture.url,
                            contentDescription = "回复图片",
                            modifier = Modifier
                                .size(Dimensions.imagePreviewMedium)
                                .clip(MaterialTheme.shapes.medium)
                                .clickable {
                                    picture.url?.let { onImageClick(it) }
                                },
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Spacer(Modifier.height(Dimensions.spaceSmall))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.spaceSmall, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Upvote button - 使用OutlinedButton匹配帖子列表风格
                    OutlinedButton(
                        onClick = onUpvote,
                        modifier = Modifier.height(Dimensions.buttonHeightSmall),
                        shape = MaterialTheme.shapes.small,
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (reply.isLiked) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            contentColor = if (reply.isLiked) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        ),
                        contentPadding = PaddingValues(
                            horizontal = Dimensions.spaceMedium,
                            vertical = Dimensions.spaceSmall
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                AppIcons.ThumbUp,
                                contentDescription = "点赞",
                                modifier = Modifier.size(Dimensions.iconSmall)
                            )
                            Spacer(modifier = Modifier.width(Dimensions.spaceSmall))
                            Text(
                                text = "${reply.upvoteCount}",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }  // Close Column

            // Highlight overlay for blink animation
            if (highlightAlpha > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = highlightAlpha * 0.2f),
                            shape = MaterialTheme.shapes.medium
                        )
                )
            }
        }
    }
}
