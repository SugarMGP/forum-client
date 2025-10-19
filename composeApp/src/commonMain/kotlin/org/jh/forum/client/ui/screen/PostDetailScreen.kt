package org.jh.forum.client.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.unit.dp
import org.jh.forum.client.data.model.GetPostInfoResponse
import org.jh.forum.client.ui.component.CommentEditor
import org.jh.forum.client.ui.component.CommentList
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

    LaunchedEffect(postId) {
        viewModel.getPost(postId) { result ->
            post = result
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
                        Icon(AppIcons.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    post?.let { currentPost ->
                        // actions intentionally left empty; ownership-based actions handled elsewhere
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            post?.let { currentPost ->
                var localPost by remember { mutableStateOf(currentPost) }

                PostContent(
                    post = localPost,
                    onUpvote = {
                        viewModel.upvotePost(postId) { isLiked ->
                            localPost = localPost.copy(
                                isLiked = isLiked,
                                likeCount = if (isLiked) localPost.likeCount + 1
                                else localPost.likeCount - 1
                            )
                        }
                    },
                    onFavorite = {
                        // favorite functionality is not available; show placeholder behavior
                        viewModel.favoritePost(postId) { /* no-op */ }
                    },
                    onShare = {
                    },
                    modifier = Modifier.weight(1f)
                )

                // 评论部分，降低权重以避免遮挡过多帖子内容
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier
                        .weight(1f)
                        .animateContentSize()
                ) {
                    Column {
                        CommentList(
                            viewModel = commentViewModel,
                            postId = postId,
                            isAuthor = false,
                            modifier = Modifier.weight(1f)
                        )

                        HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

                        CommentEditor(
                            onSubmit = { content ->
                                commentViewModel.publishComment(postId, content)
                            },
                            modifier = Modifier.animateContentSize()
                        )
                    }
                }
            } ?: run {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            errorMessage?.let { message ->
                Snackbar(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(message)
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
    onFavorite: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboard = LocalClipboard.current
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

    // 收藏动画
    val favoriteScale by animateFloatAsState(
        targetValue = if (isFavoriteAnimating) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        finishedListener = { isFavoriteAnimating = false }
    )
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
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
                    isFavoriteAnimating = true
                    onFavorite()
                }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = AppIcons.Favorite,
                        contentDescription = "收藏",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.scale(favoriteScale)
                    )
                    // 收藏数量/动画暂不实现
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