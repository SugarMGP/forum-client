package org.jh.forum.client.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged
import org.jh.forum.client.data.model.GetPostInfoResponse
import org.jh.forum.client.ui.component.CommentEditor
import org.jh.forum.client.ui.component.CommentItem
import org.jh.forum.client.ui.theme.AppIcons
import org.jh.forum.client.ui.viewmodel.CommentViewModel
import org.jh.forum.client.ui.viewmodel.PostViewModel
import org.jh.forum.client.util.TimeUtils

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun PostDetailScreen(
    postId: Long,
    viewModel: PostViewModel,
    commentViewModel: CommentViewModel,
    onBack: () -> Unit
) {
    var post by remember { mutableStateOf<GetPostInfoResponse?>(null) }
    val errorMessage by viewModel.errorMessage.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    val comments by commentViewModel.comments.collectAsState()
    val isCommentLoading by commentViewModel.isLoading.collectAsState()
    val commentHasMore by commentViewModel.hasMore.collectAsState()
    val commentError by commentViewModel.errorMessage.collectAsState()

    val listState = rememberLazyListState()

    LaunchedEffect(postId) {
        viewModel.getPost(postId) { result ->
            post = result
        }
        commentViewModel.loadComments(postId, true)
        listState.scrollToItem(0)
    }

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
                if (!commentHasMore || isCommentLoading) return@collect
                if (totalCount <= 0) return@collect

                // 当最后可见项索引接近总项数（比如还剩 3 个 item）时触发加载
                val threshold = 3
                if (lastVisible >= totalCount - 1 - threshold) {
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
                }
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                        onShare = { /* 复制/分享逻辑 */ },
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
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }

            // 如果没有评论且不在加载中，显示占位
            if (comments.isEmpty() && !isCommentLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("暂无评论")
                    }
                }
            }

            // 正式渲染每条评论
            if (comments.isNotEmpty()) {
                items(comments) { comment ->
                    CommentItem(
                        comment = comment,
                        onUpvote = { commentViewModel.upvoteComment(comment.commentId) },
                        onPin = if (true) {
                            { commentViewModel.pinComment(comment.commentId) }
                        } else null,
                        onDelete = if (comment.isAuthor) {
                            { commentViewModel.deleteComment(comment.commentId) }
                        } else null,
                        onUserProfileClick = {
                            comment.publisherInfo.id?.let { userId ->
                                // 这里可以添加跳转到用户个人资料页面的逻辑
                                // 例如：navController.navigate("userProfile/$userId")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
                }
            }

            // 评论编辑器（放在列表底部）
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
                        CommentEditor(
                            onSubmit = { content -> commentViewModel.publishComment(postId, content) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .animateContentSize()
                        )
                    }
                }
            }

            // 评论相关错误消息（如果有）
            if (commentError != null) {
                item {
                    Snackbar(modifier = Modifier.padding(16.dp)) { Text(commentError ?: "") }
                }
            }

            // 全局 error（来自 viewModel）
            if (errorMessage != null) {
                item {
                    Snackbar(modifier = Modifier.padding(16.dp)) { Text(errorMessage ?: "") }
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

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PostContent(
    post: GetPostInfoResponse,
    onUpvote: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showShareMessage by remember { mutableStateOf(false) }

    // 动画状态
    var isLikeAnimating by remember { mutableStateOf(false) }
    var isFavoriteAnimating by remember { mutableStateOf(false) }

    // 点赞动画
    val likeScale by animateFloatAsState(
        targetValue = if (isLikeAnimating) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        finishedListener = { isLikeAnimating = false }
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + expandVertically(),
            modifier = Modifier.animateContentSize()
        ) {
            Column {
                Text(
                    text = post.title ?: "",
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = post.publisherInfo.nickname ?: "",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = TimeUtils.formatTime(post.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            tonalElevation = 1.dp,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
        ) {
            Text(
                text = post.content ?: "",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(16.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        // 图片显示
        if (post.pictures.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            ImageGrid(
                images = post.pictures.map { it.url },
                totalPictures = post.pictures.size,
                onClick = { /* 可以添加点击放大功能 */ }
            )
        }

        AnimatedVisibility(
            visible = post.topics.isNotEmpty(),
            enter = fadeIn() + expandHorizontally(),
            exit = fadeOut() + shrinkHorizontally()
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                tonalElevation = 1.dp,
                shape = MaterialTheme.shapes.medium
            ) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(post.topics) { tag ->
                        FilterChip(
                            selected = false,
                            onClick = { },
                            label = { Text(tag) }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = {
                    isLikeAnimating = true
                    onUpvote()
                }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = AppIcons.ThumbUp,
                        contentDescription = "点赞",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.scale(likeScale)
                    )
                    Text(text = "${post.likeCount}")
                }
            }

            IconButton(
                onClick = {
                    showShareMessage = true
                    onShare()
                }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = AppIcons.Share,
                        contentDescription = "分享"
                    )
                    Text(
                        text = "分享",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            AnimatedVisibility(
                visible = showShareMessage,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Snackbar(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("链接已复制到剪贴板")
                }
            }

            if (showShareMessage) {
                LaunchedEffect(true) {
                    kotlinx.coroutines.delay(2000)
                    showShareMessage = false
                }
            }
        }
    }
}