package org.jh.forum.client.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.coroutines.flow.distinctUntilChanged
import org.jh.forum.client.data.model.GetPostInfoResponse
import org.jh.forum.client.data.model.PostCategory
import org.jh.forum.client.di.AppModule
import org.jh.forum.client.ui.component.ClickableImage
import org.jh.forum.client.ui.component.CommentEditor
import org.jh.forum.client.ui.component.CommentItem
import org.jh.forum.client.ui.component.ImageGalleryDialog
import org.jh.forum.client.ui.theme.AppIcons
import org.jh.forum.client.ui.theme.Dimensions
import org.jh.forum.client.ui.viewmodel.CommentViewModel
import org.jh.forum.client.ui.viewmodel.PostViewModel
import org.jh.forum.client.util.TimeUtils

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun PostDetailScreen(
    postId: Long,
    viewModel: PostViewModel,
    commentViewModel: CommentViewModel,
    onBack: () -> Unit,
    onUserClick: (Long) -> Unit = {}
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

    // Get current user ID to check if they're the post author
    val authViewModel = AppModule.authViewModel
    val currentUserId = authViewModel.userProfile.collectAsState().value?.userId

    LaunchedEffect(postId) {
        viewModel.getPost(postId) { result ->
            post = result
        }
        commentViewModel.loadComments(postId, true)
        listState.scrollToItem(0)
    }

    // 控制评论弹窗显示的状态
    var showCommentDialog by remember { mutableStateOf(false) }

    // 自动翻页逻辑：当最后可见项接近总数时触发加载下一页
    LaunchedEffect(listState, isCommentLoading, commentHasMore) {
        snapshotFlow {
            val layout = listState.layoutInfo
            val visible = layout.visibleItemsInfo
            val lastVisibleIndex = visible.lastOrNull()?.index ?: -1
            val totalCount = layout.totalItemsCount
            Triple(lastVisibleIndex, totalCount, visible.size)
        }
            // 减少频繁触发：只在值发生变化时继续
            .distinctUntilChanged()
            .collect { (lastVisible, totalCount, visibleSize) ->
                // Safety checks to prevent crashes
                if (!commentHasMore || isCommentLoading) return@collect
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
        if (errorMessage != null) {
            // 一段时间后清除错误消息
            kotlinx.coroutines.delay(3000)
            viewModel.clearError()
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
                        CommentItem(
                            comment = comment,
                            onUpvote = { commentViewModel.upvoteComment(comment.commentId) },
                            onPin = if (currentUserId != null && currentUserId == post?.publisherInfo?.id) {
                                { commentViewModel.pinComment(comment.commentId) }
                            } else null,
                            onDelete = if (comment.isAuthor) {
                                { commentViewModel.deleteComment(comment.commentId) }
                            } else null,
                            onUserProfileClick = { userId ->
                                onUserClick(userId)
                            },
                            onImageClick = { imageUrl ->
                                selectedImageUrl = imageUrl
                                showImageViewer = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        HorizontalDivider()
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
            Box(modifier = Modifier.fillMaxSize()) {
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
                            .padding(
                                horizontal = Dimensions.buttonHeightLarge,
                                vertical = Dimensions.spaceMedium
                            )
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
                        initialOffsetY = { fullHeight -> fullHeight }
                    ),
                    exit = slideOutVertically(
                        targetOffsetY = { fullHeight -> fullHeight }
                    ),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(
                            horizontal = Dimensions.buttonHeightLarge,
                            vertical = Dimensions.spaceMedium
                        )
                ) {
                    // 评论编辑器状态 - 小而美设计，与界面边框保持间隔
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        shadowElevation = 8.dp,
                        shape = RoundedCornerShape(16.dp)
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
                                        style = MaterialTheme.typography.titleLarge
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
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(Modifier.height(12.dp))

                                // 评论编辑器
                                CommentEditor(
                                    onSubmit = { content ->
                                        commentViewModel.publishComment(postId, content)
                                        showCommentDialog = false
                                    },
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

@OptIn(ExperimentalAnimationApi::class)
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
                            model = post.publisherInfo.avatar ?: "",
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
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = Dimensions.spaceMedium)
                    )

                    // 帖子内容
                    Text(
                        text = post.content ?: "",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = Dimensions.spaceMedium)
                    )

                    // 图片显示 - using SharedElement for smooth transitions
                    if (post.pictures.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(Dimensions.spaceMedium))
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
                                                                .background(
                                                                    MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f)
                                                                )
                                                        ) {
                                                            Text(
                                                                text = "+${post.pictures.size - displayImages.size}",
                                                                style = MaterialTheme.typography.titleLarge,
                                                                color = MaterialTheme.colorScheme.onPrimary
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

                // 话题标签
                if (post.topics.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(Dimensions.spaceMedium))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Dimensions.spaceSmall),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Dimensions.spaceMedium)
                    ) {
                        post.topics.forEach { tag ->
                            AssistChip(
                                onClick = { },
                                label = {
                                    Text(
                                        text = "#$tag",
                                        style = MaterialTheme.typography.labelMedium
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
                                shape = RoundedCornerShape(Dimensions.cornerRadiusMedium),
                                modifier = Modifier.height(30.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Dimensions.spaceMedium))

                // 底部操作按钮 - Compact design
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.spaceSmall),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimensions.spaceMedium)
                        .padding(bottom = Dimensions.spaceMedium)
                ) {
                    // 点赞按钮
                    FilledTonalButton(
                        onClick = {
                            isLikeAnimating = true
                            onUpvote()
                        },
                        modifier = Modifier.height(Dimensions.buttonHeightSmall),
                        shape = MaterialTheme.shapes.small,
                        colors = ButtonDefaults.filledTonalButtonColors(
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