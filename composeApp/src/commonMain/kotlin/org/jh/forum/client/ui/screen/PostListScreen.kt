package org.jh.forum.client.ui.screen

import androidx.compose.animation.*
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
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.jh.forum.client.data.model.GetPostListElement
import org.jh.forum.client.data.model.PostCategory
import org.jh.forum.client.data.model.SortType
import org.jh.forum.client.di.AppModule
import org.jh.forum.client.ui.component.ClickableImage
import org.jh.forum.client.ui.component.ImageGalleryDialog
import org.jh.forum.client.ui.theme.AppIcons
import org.jh.forum.client.ui.theme.Dimensions
import org.jh.forum.client.util.TimeUtils
import kotlin.enums.EnumEntries


@Composable
fun ImageGrid(
    images: List<String?>,
    totalPictures: Int = images.size,
    onClick: (String) -> Unit
) {
    // 最多显示9张图片
    val displayImages = images.take(9)

    if (displayImages.isEmpty()) return

    when (displayImages.size) {
        1 -> {
            // 单图显示 - 改为正方形，并设置更合适的高度限制
            Box(
                modifier = Modifier
                    .sizeIn(maxWidth = 200.dp)
                    .aspectRatio(1f)
            ) {
                ClickableImage(
                    imageUrl = displayImages[0],
                    contentDescription = "帖子图片",
                    onClick = { displayImages[0]?.let { onClick(it) } }
                )
            }
        }

        in 2..3 -> {
            // 2-3张图片，水平排列，减小高度限制
            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimensions.spaceExtraSmall)
            ) {
                displayImages.forEachIndexed { index, imageUrl ->
                    val isLastImage = index == displayImages.size - 1
                    val hasMoreImages = totalPictures > displayImages.size

                    Box(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .sizeIn(maxWidth = 200.dp)
                            .aspectRatio(1f)
                    ) {
                        ClickableImage(
                            imageUrl = imageUrl,
                            contentDescription = "帖子图片 $index",
                            onClick = { imageUrl?.let { onClick(it) } }
                        ) {
                            // 如果是最后一张图片并且有更多图片未显示，添加蒙版显示数量
                            if (isLastImage && hasMoreImages) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(MaterialTheme.shapes.medium)
                                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f))
                                ) {
                                    Text(
                                        text = "+${totalPictures - displayImages.size}",
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

        else -> {
            // 4-9张图片，网格排列，减小高度限制
            val gridColumns = if (displayImages.size == 4) 2 else 3
            val gridRows = (displayImages.size + gridColumns - 1) / gridColumns

            Column(
                verticalArrangement = Arrangement.spacedBy(Dimensions.spaceExtraSmall)
            ) {
                (0 until gridRows).forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Dimensions.spaceExtraSmall)
                    ) {
                        (0 until gridColumns).forEach { col ->
                            val index = row * gridColumns + col
                            if (index < displayImages.size) {
                                val isLastImage = index == displayImages.size - 1
                                val hasMoreImages = totalPictures > displayImages.size

                                Box(
                                    modifier = Modifier
                                        .weight(1f, fill = false)
                                        .sizeIn(maxWidth = 200.dp)
                                        .aspectRatio(1f)
                                ) {
                                    ClickableImage(
                                        imageUrl = displayImages[index],
                                        contentDescription = "帖子图片 $index",
                                        onClick = { displayImages[index]?.let { onClick(it) } }
                                    ) {
                                        // 如果是最后一张图片并且有更多图片未显示，添加蒙版显示数量
                                        if (isLastImage && hasMoreImages) {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(MaterialTheme.shapes.medium)
                                                    .background(
                                                        MaterialTheme.colorScheme.scrim.copy(
                                                            alpha = 0.6f
                                                        )
                                                    )
                                            ) {
                                                Text(
                                                    text = "+${totalPictures - displayImages.size}",
                                                    style = MaterialTheme.typography.titleLarge,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                // 空白占位
                                Spacer(Modifier.weight(1f).aspectRatio(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Non-shared element version of ImageGrid for backwards compatibility
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostListScreen(
    onPostClick: (Long) -> Unit,
    onNavigateToCreatePost: () -> Unit,
    onUserClick: (Long) -> Unit = {}, // New parameter for user profile navigation
    refresh: Boolean = false,
    onRefreshComplete: () -> Unit = {}
) {
    val viewModel = AppModule.postListViewModel
    val posts by viewModel.posts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val sortType by viewModel.sortType.collectAsState()
    val hasMore by viewModel.hasMore.collectAsState()

    val listState = rememberLazyListState()
    var showTabs by remember { mutableStateOf(false) }

    // Image gallery state
    var showImageGallery by remember { mutableStateOf(false) }
    var galleryImages by remember { mutableStateOf<List<String>>(emptyList()) }
    var galleryInitialIndex by remember { mutableStateOf(0) }


    val categories = PostCategory.entries

    // 监听滚动到底部，加载更多
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                if (lastVisibleIndex != null && lastVisibleIndex >= posts.size - 3 && hasMore && !isLoading) {
                    viewModel.loadPosts(
                        category = selectedCategory,
                        sortType = sortType.name.lowercase()
                    )
                }
            }
    }

    // 在组件初始化时自动加载帖子
    LaunchedEffect(Unit) {
        viewModel.loadPosts()
    }

    // Pull-to-refresh state
    var isRefreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullToRefreshState()

    // 处理刷新参数 - 触发下拉刷新UI
    LaunchedEffect(refresh) {
        if (refresh) {
            isRefreshing = true
            onRefreshComplete()
        }
    }

    // Monitor loading state to update refresh indicator
    LaunchedEffect(isLoading) {
        if (!isLoading && isRefreshing) {
            isRefreshing = false
        }
    }

    // Snackbar state for error messages
    val snackbarHostState = remember { SnackbarHostState() }

    // Show error message in Snackbar
    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            },
            topBar = {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 顶栏始终显示在最上方
                    TopAppBar(
                        title = { Text("精弘论坛") },
                        actions = {
                            IconButton(onClick = { showTabs = !showTabs }) {
                                Icon(AppIcons.FilterList, contentDescription = "筛选")
                            }
                        }
                    )

                    // 分类和排序选项卡 - 只有点击按钮后才显示，添加淡入淡出动画
                    // 选项卡组件放在顶栏下方的单独区域
                    AnimatedVisibility(
                        visible = showTabs,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { -20 }),
                        exit = fadeOut() + slideOutVertically(targetOffsetY = { -20 })
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                // 排序选项卡
                                SecondaryScrollableTabRow(
                                    selectedTabIndex = if (sortType == SortType.HOT) 1 else 0,
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    indicator = {
                                        TabRowDefaults.SecondaryIndicator(
                                            Modifier.tabIndicatorOffset(if (sortType == SortType.HOT) 1 else 0)
                                        )
                                    }
                                ) {
                                    Tab(
                                        selected = sortType == SortType.NEWEST,
                                        onClick = {
                                            viewModel.setSortType(SortType.NEWEST)
                                        },
                                        text = { Text("最新") }
                                    )
                                    Tab(
                                        selected = sortType == SortType.HOT,
                                        onClick = {
                                            viewModel.setSortType(SortType.HOT)
                                        },
                                        text = { Text("最热") }
                                    )
                                }

                                // 分类选项卡
                                SecondaryScrollableTabRow(
                                    selectedTabIndex = selectedCategoryIndex(
                                        selectedCategory,
                                        categories
                                    ),
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    indicator = {
                                        TabRowDefaults.SecondaryIndicator(
                                            Modifier.tabIndicatorOffset(
                                                selectedCategoryIndex(
                                                    selectedCategory,
                                                    categories
                                                )
                                            ),
                                        )
                                    }
                                ) {
                                    // 全部 Tab
                                    Tab(
                                        selected = selectedCategory == null,
                                        onClick = {
                                            viewModel.selectCategory(null)
                                        },
                                        text = { Text("全部") }
                                    )

                                    // 分类 Tab
                                    categories.forEach { category ->
                                        Tab(
                                            selected = selectedCategory == category.value,
                                            onClick = {
                                                viewModel.selectCategory(category.value)
                                            },
                                            text = { Text(category.displayName) },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            floatingActionButton = {
                FloatingActionButton(onClick = onNavigateToCreatePost) {
                    Icon(AppIcons.Add, contentDescription = "发帖")
                }
            }
        ) { paddingValues ->
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    isRefreshing = true
                    viewModel.refresh()
                },
                state = pullRefreshState,
                modifier = Modifier.fillMaxSize().padding(paddingValues)
            ) {
                if (posts.isEmpty() && !isLoading) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            AppIcons.Inbox,
                            contentDescription = "空列表",
                            modifier = Modifier.size(Dimensions.iconExtraLarge + Dimensions.spaceMedium),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(Dimensions.spaceMedium))
                        Text(
                            text = "暂无帖子",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(Dimensions.spaceMedium),
                        verticalArrangement = Arrangement.spacedBy(Dimensions.spaceMedium)
                    ) {
                        items(posts) {
                            PostItem(
                                post = it,
                                onClick = { onPostClick(it.id) },
                                onUserClick = { userId -> onUserClick(userId) },
                                onUpvoteClick = { it -> viewModel.upvotePost(it) },
                                onImageClick = { images, index ->
                                    galleryImages = images
                                    galleryInitialIndex = index
                                    showImageGallery = true
                                }
                            )
                        }
                    }
                }
            }
        } // Close Scaffold content lambda

        // Image gallery dialog with shared element transitions
        // Placed outside Scaffold to ensure proper z-index above top bar
        ImageGalleryDialog(
            visible = showImageGallery,
            images = galleryImages,
            initialIndex = galleryInitialIndex,
            onDismiss = {
                showImageGallery = false
                galleryImages = emptyList()
                galleryInitialIndex = 0
            }
        )
    }
}


@Composable
fun PostItem(
    post: GetPostListElement,
    onClick: () -> Unit,
    onUpvoteClick: (Long) -> Unit,
    onUserClick: (Long) -> Unit = {},
    onImageClick: (List<String>, Int) -> Unit = { _, _ -> }, // New parameter for image gallery
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(
            defaultElevation = Dimensions.elevationSmall,
            pressedElevation = Dimensions.elevationMedium
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(Dimensions.spaceMedium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 用户头像和名称 - 改为可点击，但不占满整行
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .clickable {
                            post.publisherInfo.id?.let { onUserClick(it) }
                        }
                ) {
                    AsyncImage(
                        model = post.publisherInfo.avatar ?: "",
                        contentDescription = "用户头像",
                        modifier = Modifier
                            .size(Dimensions.avatarMedium)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(Dimensions.spaceSmall))
                    Column(
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Text(
                            text = post.publisherInfo.nickname ?: "",
                            style = MaterialTheme.typography.titleSmall,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1
                        )
                        Text(
                            text = getCategoryDisplayName(post.category),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(Dimensions.spaceExtraSmall))

                // 右上角信息：时间、评论、浏览量
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = TimeUtils.formatTime(post.createdAt),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = Dimensions.spaceExtraSmall)
                    ) {
                        Icon(
                            AppIcons.Comment,
                            contentDescription = "评论",
                            modifier = Modifier.size(Dimensions.iconSmall),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(Dimensions.spaceExtraSmall))
                        Text(
                            text = "${post.commentCount}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimensions.spaceMedium))

            // 标题（带置顶标识）
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimensions.spaceExtraSmall)
            ) {
                // Show pin indicator for pinned posts before title
                if (post.isPinned) {
                    Icon(
                        imageVector = AppIcons.PushPin,
                        contentDescription = "置顶",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(Dimensions.iconSmall)
                    )
                }
                Text(
                    text = post.title ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.animateContentSize()
                )
            }

            Spacer(modifier = Modifier.height(Dimensions.spaceSmall))

            // 内容
            Text(
                text = post.content ?: "",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.animateContentSize()
            )

            // 图片预览（如果有）
            if (post.pictures.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Dimensions.spaceSmall))
                val imageUrls = post.pictures.mapNotNull { it.url }
                ImageGrid(
                    images = imageUrls,
                    totalPictures = post.totalPictures,
                    onClick = { clickedUrl: String ->
                        // Find index of clicked image and open gallery
                        val clickedIndex = imageUrls.indexOf(clickedUrl)
                        onImageClick(imageUrls, if (clickedIndex >= 0) clickedIndex else 0)
                    }
                )
            }

            // Display post tags if available
            if (post.topics.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.spaceExtraSmall),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Dimensions.spaceSmall)
                ) {
                    post.topics.take(3).forEach { topicName ->
                        AssistChip(
                            onClick = { },
                            label = {
                                Text(
                                    text = "#$topicName",
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
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
                            modifier = Modifier.height(24.dp).weight(1f, fill = false)
                        )
                    }
                    if (post.topics.size > 3) {
                        AssistChip(
                            onClick = { },
                            label = {
                                Text(
                                    text = "+${post.topics.size - 3}",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            border = BorderStroke(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(Dimensions.cornerRadiusSmall),
                            modifier = Modifier.height(24.dp)
                        )
                    }
                }
            }

            // 底部信息 - 优化的点赞按钮设计
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Dimensions.spaceSmall),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 左侧：简洁的点赞按钮
                OutlinedButton(
                    onClick = { onUpvoteClick(post.id) },
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
                            text = "${post.likeCount}",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}

private fun getCategoryDisplayName(categoryValue: String): String {
    return PostCategory.getDisplayName(categoryValue)
}

private fun selectedCategoryIndex(
    selectedCategory: String?,
    categories: EnumEntries<PostCategory>
): Int {
    return selectedCategory?.let { value ->
        val index = categories.indexOfFirst { it.value == value }
        if (index >= 0) index + 1 else 0
    } ?: 0
}