package org.jh.forum.client.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import org.jh.forum.client.data.model.GetPostInfoResponse
import org.jh.forum.client.data.model.PostCategory
import org.jh.forum.client.di.AppModule
import org.jh.forum.client.ui.component.*
import org.jh.forum.client.ui.theme.AppIcons
import org.jh.forum.client.ui.theme.Dimensions
import org.jh.forum.client.ui.viewmodel.CommentViewModel
import org.jh.forum.client.ui.viewmodel.PostViewModel
import org.jh.forum.client.util.TimeUtils
import org.jh.forum.client.util.getAvatarOrDefault

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun PostDetailScreen(
    postId: Long,
    highlightCommentId: Long,
    viewModel: PostViewModel,
    commentViewModel: CommentViewModel,
    onBack: () -> Unit,
    onUserClick: (Long) -> Unit = {},
    onCommentClick: (Long) -> Unit = {},
    onPostUpdated: (postId: Long, isLiked: Boolean, likeCount: Int) -> Unit = { _, _, _ -> },
    onPostDeleted: (postId: Long) -> Unit = {}
) {
    var post by remember { mutableStateOf<GetPostInfoResponse?>(null) }
    val errorMessage by viewModel.errorMessage.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showImageViewer by remember { mutableStateOf(false) }
    var selectedImageIndex by remember { mutableStateOf(0) }
    var selectedImageUrl by remember { mutableStateOf<String?>(null) }

    val comments by commentViewModel.comments.collectAsState()
    val isCommentLoading by commentViewModel.isLoading.collectAsState()
    val commentHasMore by commentViewModel.hasMore.collectAsState()
    val commentError by commentViewModel.errorMessage.collectAsState()

    val listState = rememberLazyListState()

    // Track the last loaded postId and highlightId to detect new navigation
    var lastPostId by remember { mutableStateOf(0L) }
    var lastHighlightCommentId by remember { mutableStateOf(0L) }

    // Get current user ID to check if they're the post author
    val authViewModel = AppModule.authViewModel
    val currentUserId = authViewModel.userProfile.collectAsState().value?.userId

    LaunchedEffect(postId, highlightCommentId) {
        // Set highlight ID BEFORE clearing to ensure pagination uses correct value
        val param = if (highlightCommentId > 0) highlightCommentId else null
        commentViewModel.setHighlightCommentId(param)

        // Only clear and reset scroll if we're navigating from a different source
        // (either different post OR different highlight, which means new navigation from messages)
        val isNewNavigation =
            postId != lastPostId || (highlightCommentId > 0 && highlightCommentId != lastHighlightCommentId)
        if (isNewNavigation) {
            lastPostId = postId
            lastHighlightCommentId = highlightCommentId
            // Clear comments and errors immediately when navigating from a different source
            commentViewModel.clearComments()
            viewModel.clearError()
            listState.scrollToItem(0)
        }

        viewModel.getPost(postId) { result ->
            post = result
            // Only load comments if post loaded successfully
            if (result != null) {
                commentViewModel.loadComments(postId, true, param)
            }
        }
    }

    // 控制评论弹窗显示的状态
    var showCommentDialog by remember { mutableStateOf(false) }

    // 自动翻页逻辑：当最后可见项接近总数时触发加载下一页
    LaunchedEffect(listState, isCommentLoading, commentHasMore, commentError) {
        snapshotFlow {
            val layout = listState.layoutInfo
            val visible = layout.visibleItemsInfo
            val lastVisibleIndex = visible.lastOrNull()?.index ?: -1
            val totalCount = layout.totalItemsCount
            Triple(lastVisibleIndex, totalCount, visible.size)
        }
            // 减少频繁触发：只在值发生变化时继续
            .distinctUntilChanged()
            .collect { (lastVisible, totalCount) ->
                // Safety checks to prevent crashes and retries on error
                if (!commentHasMore || isCommentLoading || commentError != null) return@collect
                if (totalCount <= 0 || lastVisible < 0) return@collect

                // Only trigger load if we're not already at the end and have more items to load
                val threshold = 3
                if (lastVisible >= totalCount - 1 - threshold && lastVisible < totalCount) {
                    // 调用加载下一页（你的 viewModel 应该负责分页状态）
                    commentViewModel.loadComments(postId)
                }
            }
    }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null && post != null) {
            delay(3000)
            viewModel.clearError()
        }
    }

    // Scroll to highlighted comment when comments load
    LaunchedEffect(highlightCommentId, comments.size) {
        if (highlightCommentId > 0 && comments.isNotEmpty()) {
            val highlightIndex = comments.indexOfFirst { it.commentId == highlightCommentId }
            if (highlightIndex >= 0) {
                delay(150)
                listState.animateScrollToItem(
                    index = highlightIndex + 2,
                    scrollOffset = -56
                )
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("帖子详情") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = AppIcons.ArrowBack,
                                contentDescription = "返回"
                            )
                        }
                    },
                    actions = {
                        // Only show delete option if current user is the post author
                        if (currentUserId != null && currentUserId == post?.publisherInfo?.id) {
                            IconButton(onClick = { showDeleteDialog = true }) {
                                Icon(
                                    imageVector = AppIcons.Delete,
                                    contentDescription = "删除帖子"
                                )
                            }
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
                item {
                    post?.let { currentPost ->
                        var localPost by remember { mutableStateOf(currentPost) }
                        PostContent(
                            post = localPost,
                            onUpvote = {
                                viewModel.upvotePost(postId) { isLiked ->
                                    localPost = localPost.copy(
                                        isLiked = isLiked,
                                        likeCount = if (isLiked) localPost.likeCount + 1 else localPost.likeCount - 1
                                    )
                                    // Notify parent to update post list
                                    onPostUpdated(postId, isLiked, localPost.likeCount)
                                }
                            },
                            onUserProfileClick = {
                                localPost.publisherInfo.id?.let { userId ->
                                    onUserClick(userId)
                                }
                            },
                            onImageClick = { imageIndex ->
                                selectedImageIndex = imageIndex
                                showImageViewer = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } ?: run {
                        // Show error message if post failed to load instead of loading spinner
                        if (errorMessage != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Dimensions.spaceLarge),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(Dimensions.spaceSmall)
                                ) {
                                    Icon(
                                        imageVector = AppIcons.Error,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                    Text(
                                        text = errorMessage ?: "加载失败",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }

                // 评论标题（作为单独 item，显示评论总数）
                item {
                    post?.let {
                        Text(
                            text = "评论（${it.commentCount}）",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(
                                horizontal = Dimensions.spaceMedium,
                                vertical = Dimensions.spaceSmall
                            )
                        )
                    }
                }

                // 如果没有评论且不在加载中，显示占位
                if (comments.isEmpty() && !isCommentLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Dimensions.spaceMedium),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "暂无评论",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // 正式渲染每条评论
                if (comments.isNotEmpty()) {
                    items(
                        items = comments,
                        key = { comment -> comment.commentId }
                    ) { comment ->
                        val isHighlighted = comment.commentId == highlightCommentId
                        CommentItem(
                            comment = comment,
                            isHighlighted = isHighlighted,
                            onUpvote = { commentViewModel.upvoteComment(comment.commentId) },
                            onPin = if (currentUserId != null && currentUserId == post?.publisherInfo?.id) {
                                { commentViewModel.pinComment(comment.commentId) }
                            } else null,
                            onDelete = if (currentUserId != null && currentUserId == comment.publisherInfo.id) {
                                { commentViewModel.deleteComment(comment.commentId) }
                            } else null,
                            onUserProfileClick = { userId ->
                                onUserClick(userId)
                            },
                            onImageClick = { imageUrl ->
                                selectedImageUrl = imageUrl
                                showImageViewer = true
                            },
                            onViewReplies = {
                                onCommentClick(comment.commentId)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // 底部空间，确保内容不被悬浮按钮遮挡
                item {
                    Spacer(modifier = Modifier.height(Dimensions.avatarExtraLarge))
                }

                // 评论相关错误消息（如果有）
                if (commentError != null) {
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Dimensions.spaceMedium),
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                commentError ?: "",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(Dimensions.spaceMedium)
                            )
                        }
                    }
                }

                // 全局 error（来自 viewModel）
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

            // 底部悬浮的评论组件
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        horizontal = Dimensions.buttonHeightLarge,
                        vertical = Dimensions.spaceMedium
                    )
            ) {
                // 悬浮按钮 - 无动画显示
                if (!showCommentDialog) {
                    FloatingActionButton(
                        onClick = { showCommentDialog = true },
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
                                contentDescription = "发表评论"
                            )
                            Text(
                                text = "评论",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }

                // 评论编辑器 - 从底部滑上来
                AnimatedVisibility(
                    visible = showCommentDialog,
                    enter = slideInVertically(
                        initialOffsetY = { fullHeight -> fullHeight + 100 }
                    ),
                    exit = slideOutVertically(
                        targetOffsetY = { fullHeight -> fullHeight + 100 }
                    ),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                ) {
                    // 评论编辑器状态 - 小而美设计，与界面边框保持间隔
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
                                // 顶部控制栏
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "发表评论",
                                        style = MaterialTheme.typography.titleLarge,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    // 美化的关闭按钮
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.clickable { showCommentDialog = false }
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

                                // 评论编辑器
                                CommentEditor(
                                    onSubmit = { content, picture ->
                                        commentViewModel.publishComment(postId, content, picture)
                                        showCommentDialog = false
                                    },
                                    shouldRequestFocus = true,
                                    focusDelayMillis = 350,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = showDeleteDialog,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    icon = {
                        Icon(
                            imageVector = AppIcons.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    },
                    title = {
                        Text(
                            "删除帖子",
                            style = MaterialTheme.typography.headlineSmall
                        )
                    },
                    text = {
                        Text(
                            "确定要删除这篇帖子吗？此操作无法撤销。",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.deletePost(postId)
                                showDeleteDialog = false
                                onPostDeleted(postId)
                                onBack()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("删除")
                        }
                    },
                    dismissButton = {
                        OutlinedButton(onClick = { showDeleteDialog = false }) {
                            Text("取消")
                        }
                    }
                )
            }
        }
    }

    // Image gallery dialog - placed outside Scaffold but inside Box for proper z-order
    ImageGalleryDialog(
        visible = showImageViewer,
        images = if (selectedImageUrl != null) {
            listOf(selectedImageUrl!!)
        } else {
            post?.pictures?.mapNotNull { it.url } ?: emptyList()
        },
        initialIndex = if (selectedImageUrl != null) 0 else selectedImageIndex,
        onDismiss = {
            showImageViewer = false
            selectedImageIndex = 0
            selectedImageUrl = null
        }
    )
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun PostContent(
    post: GetPostInfoResponse,
    onUpvote: () -> Unit,
    onUserProfileClick: () -> Unit,
    onImageClick: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // 动画状态
    var isLikeAnimating by remember { mutableStateOf(false) }

    // 点赞动画
    val likeScale by animateFloatAsState(
        targetValue = if (isLikeAnimating) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        finishedListener = { isLikeAnimating = false }
    )

    // 内容变化动画
    val contentAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium)
    )

    Box(modifier = modifier) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(Dimensions.spaceMedium)
                .animateContentSize(),
            color = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurface,
            tonalElevation = Dimensions.elevationSmall,
            shape = MaterialTheme.shapes.medium
        ) {
            Column {
                // 作者信息区域
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimensions.spaceMedium),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .clickable {
                                onUserProfileClick()
                            }
                    ) {
                        // 用户头像
                        AsyncImage(
                            model = post.publisherInfo.avatar.getAvatarOrDefault(),
                            contentDescription = "用户头像",
                            modifier = Modifier
                                .size(Dimensions.avatarLarge)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(Dimensions.spaceMedium))
                        Column(
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Text(
                                text = post.publisherInfo.nickname ?: "未知用户",
                                style = MaterialTheme.typography.titleSmall,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1
                            )
                            // 显示帖子板块
                            if (post.category.isNotEmpty()) {
                                Text(
                                    text = PostCategory.getDisplayName(post.category),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(Dimensions.spaceExtraSmall))

                    // 右侧显示时间和浏览量
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = TimeUtils.formatTime(post.createdAt),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = AppIcons.Eye,
                                contentDescription = "浏览量",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(Dimensions.iconSmall)
                            )
                            Text(
                                text = "${post.viewCount}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = Dimensions.spaceExtraSmall)
                            )
                        }
                    }
                }

                // 帖子标题和内容
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimensions.spaceMedium)
                        .alpha(contentAlpha)
                ) {
                    Text(
                        text = post.title ?: "",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // 帖子内容
                    ParagraphText(
                        text = (post.content ?: "").trimEnd('\n'),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = Dimensions.spaceSmall)
                    )

                    // 图片显示 - using SharedElement for smooth transitions
                    if (post.pictures.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(Dimensions.spaceSmall))
                        val displayImages = post.pictures.take(9)

                        // Single image layout
                        if (displayImages.size == 1) {
                            Box(
                                modifier = Modifier
                                    .sizeIn(maxWidth = 300.dp)
                                    .aspectRatio(1f)
                            ) {
                                ClickableImage(
                                    imageUrl = displayImages[0].url,
                                    contentDescription = "帖子图片",
                                    onClick = { onImageClick(0) }
                                )
                            }
                        }
                        // 2 images horizontal layout
                        else if (displayImages.size == 2) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(Dimensions.spaceSmall),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                displayImages.forEachIndexed { index, picture ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f, fill = false)
                                            .sizeIn(maxWidth = 200.dp)
                                            .aspectRatio(1f)
                                    ) {
                                        ClickableImage(
                                            imageUrl = picture.url,
                                            contentDescription = "帖子图片",
                                            onClick = { onImageClick(index) }
                                        )
                                    }
                                }
                            }
                        }
                        // Grid layout for 3+ images
                        else {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(Dimensions.spaceSmall),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                displayImages.chunked(3).forEach { rowImages ->
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(Dimensions.spaceSmall),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        rowImages.forEachIndexed { index, picture ->
                                            val globalIndex = displayImages.indexOf(picture)
                                            val isLastImage = globalIndex == displayImages.size - 1
                                            val hasMoreImages = post.pictures.size > displayImages.size

                                            Box(
                                                modifier = Modifier
                                                    .weight(1f, fill = false)
                                                    .sizeIn(maxWidth = 200.dp)
                                                    .aspectRatio(1f)
                                            ) {
                                                ClickableImage(
                                                    imageUrl = picture.url,
                                                    contentDescription = "帖子图片",
                                                    onClick = { onImageClick(globalIndex) }
                                                ) {
                                                    if (isLastImage && hasMoreImages) {
                                                        Box(
                                                            contentAlignment = Alignment.Center,
                                                            modifier = Modifier
                                                                .fillMaxSize()
                                                                .clip(MaterialTheme.shapes.medium)
                                                                .background(
                                                                    MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f)
                                                                )
                                                        ) {
                                                            Text(
                                                                text = "+${post.pictures.size - displayImages.size}",
                                                                style = MaterialTheme.typography.titleLarge,
                                                                color = Color.White
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (post.topics.isNotEmpty()) {
                    val scrollState = rememberScrollState()
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Dimensions.spaceMedium)
                            .padding(top = Dimensions.spaceMedium)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(Dimensions.spaceSmall),
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(scrollState)
                        ) {
                            post.topics.forEach { tag ->
                                AssistChip(
                                    onClick = { },
                                    label = {
                                        Text(
                                            text = "#$tag",
                                            style = MaterialTheme.typography.labelMedium,
                                            maxLines = 1,
                                            softWrap = false,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                        labelColor = MaterialTheme.colorScheme.onTertiaryContainer
                                    ),
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
                                    ),
                                    shape = RoundedCornerShape(Dimensions.cornerRadiusSmall),
                                    modifier = Modifier.height(30.dp)
                                )
                            }
                        }
                    }
                }

                // 底部操作按钮 - Compact design
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.spaceSmall),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimensions.spaceMedium)
                        .padding(top = Dimensions.spaceMedium, bottom = Dimensions.spaceMedium)
                ) {
                    // 点赞按钮
                    OutlinedButton(
                        onClick = {
                            isLikeAnimating = true
                            onUpvote()
                        },
                        modifier = Modifier.height(Dimensions.buttonHeightSmall),
                        shape = MaterialTheme.shapes.small,
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (post.isLiked) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            contentColor = if (post.isLiked) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(horizontal = Dimensions.spaceSmall)
                        ) {
                            Icon(
                                imageVector = AppIcons.ThumbUp,
                                contentDescription = "点赞",
                                modifier = Modifier
                                    .size(Dimensions.iconSmall)
                                    .scale(likeScale)
                            )
                            Text(
                                text = "${post.likeCount}",
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(start = Dimensions.spaceExtraSmall)
                            )
                        }
                    }
                }
            }
        }
    }
}