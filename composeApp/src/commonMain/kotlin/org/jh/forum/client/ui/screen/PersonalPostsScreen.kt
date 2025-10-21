package org.jh.forum.client.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jh.forum.client.data.model.GetPersonalPostListElement
import org.jh.forum.client.data.repository.ForumRepository
import org.jh.forum.client.ui.theme.AppIcons
import org.jh.forum.client.ui.theme.Dimensions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalPostsScreen(
    repository: ForumRepository,
    userId: Long? = null,
    onPostClick: (Long) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    var posts by remember { mutableStateOf<List<GetPersonalPostListElement>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var hasMore by remember { mutableStateOf(true) }
    var currentPage by remember { mutableStateOf(1) }

    val listState = rememberLazyListState()

    // 加载个人帖子列表
    LaunchedEffect(currentPage, userId) {
        try {
            isLoading = true
            val result = repository.getPersonalPostList(page = currentPage, pageSize = 20, userId = userId)
            if (result.code == 200 && result.data != null) {
                val postList = result.data
                posts = if (currentPage == 1) {
                    postList.list
                } else {
                    posts + postList.list
                }
                hasMore = postList.page * postList.pageSize < postList.total
                errorMessage = null
            } else {
                errorMessage = "加载失败: ${result.msg}"
            }
            isLoading = false
        } catch (e: Exception) {
            errorMessage = "加载失败: ${e.message}"
            isLoading = false
        }
    }

    // 监听滚动到底部，加载更多
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                if (lastVisibleIndex != null && lastVisibleIndex >= posts.size - 3 && hasMore && !isLoading) {
                    currentPage++
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (userId == null) "我的帖子" else "用户帖子")
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(AppIcons.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        if (isLoading && posts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        AppIcons.Error,
                        contentDescription = "错误",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = errorMessage ?: "加载失败",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        errorMessage = null
                        currentPage = 1
                    }) {
                        Text("重试")
                    }
                }
            }
        } else if (posts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        AppIcons.Article,
                        contentDescription = "无帖子",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "暂无帖子",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(Dimensions.spaceMedium),
                verticalArrangement = Arrangement.spacedBy(Dimensions.spaceMedium)
            ) {
                items(posts) { post ->
                    PersonalPostItem(
                        post = post,
                        onClick = { onPostClick(post.id) }
                    )
                }

                // 加载更多指示器
                if (isLoading && posts.isNotEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PersonalPostItem(
    post: GetPersonalPostListElement,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.elevationSmall),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(Dimensions.spaceMedium)
        ) {
            // 置顶标识（如果有）
            if (post.isTopped) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Text(
                        text = "置顶",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(
                            horizontal = Dimensions.spaceSmall,
                            vertical = Dimensions.spaceExtraSmall
                        )
                    )
                }
                Spacer(modifier = Modifier.height(Dimensions.spaceSmall))
            }

            // 标题
            post.title?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(Dimensions.spaceSmall))

            // 内容
            post.content?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 图片预览（如果有）
            if (post.pictures.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Dimensions.spaceSmall))
                ImageGrid(
                    images = post.pictures.map { it.url },
                    totalPictures = post.totalPictures,
                    onClick = onClick
                )
            }

            // 话题标签（在内容下方）
            if (post.topics.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Dimensions.spaceSmall))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.spaceExtraSmall),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    post.topics.take(2).forEach { topicName ->
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "# $topicName",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(
                                    horizontal = Dimensions.spaceSmall - 2.dp,
                                    vertical = 2.dp
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimensions.spaceMedium))

            // 底部：统计信息和时间
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.spaceMedium)
                ) {
                    // 点赞数
                    PostStatChip(
                        icon = if (post.isLiked) AppIcons.Favorite else AppIcons.FavoriteBorder,
                        count = post.likeCount,
                        tint = if (post.isLiked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // 评论数
                    PostStatChip(
                        icon = AppIcons.Comment,
                        count = post.commentCount,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // 浏览数
                    PostStatChip(
                        icon = AppIcons.Visibility,
                        count = post.viewCount,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // 时间
                Text(
                    text = post.createdAt.substring(0, 10), // 只显示日期部分
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PostStatChip(
    icon: ImageVector,
    count: Int,
    tint: androidx.compose.ui.graphics.Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimensions.spaceExtraSmall)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(Dimensions.iconSmall),
            tint = tint
        )
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelMedium,
            color = tint
        )
    }
}