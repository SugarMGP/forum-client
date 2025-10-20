package org.jh.forum.client.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.jh.forum.client.data.model.GetPostListElement
import org.jh.forum.client.data.model.PostCategory
import org.jh.forum.client.data.model.SortType
import org.jh.forum.client.di.AppModule
import org.jh.forum.client.ui.theme.AppIcons
import org.jh.forum.client.util.TimeUtils
import kotlin.enums.EnumEntries

@Composable
fun ImageGrid(
    images: List<String?>,
    totalPictures: Int = images.size,
    onClick: () -> Unit
) {
    // 最多显示9张图片
    val displayImages = images.take(9)

    if (displayImages.isEmpty()) return

    when (displayImages.size) {
        1 -> {
            // 单图显示 - 改为正方形，并设置更合适的高度限制
            Box(
                modifier = Modifier
                    .sizeIn(maxWidth = 300.dp)
                    .aspectRatio(1f)
                    .clickable(onClick = onClick)
            ) {
                AsyncImage(
                    model = displayImages[0],
                    contentDescription = "帖子图片",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // 如果有更多图片，在图片上添加蒙版显示数量
                if (totalPictures > 1) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "+${totalPictures - 1}",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White
                        )
                    }
                }
            }
        }

        in 2..3 -> {
            // 2-3张图片，水平排列，减小高度限制
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                displayImages.forEachIndexed { index, imageUrl ->
                    val isLastImage = index == displayImages.size - 1
                    val hasMoreImages = totalPictures > displayImages.size

                    Box(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .sizeIn(maxWidth = 300.dp)
                            .aspectRatio(1f)
                            .clickable(onClick = onClick)
                    ) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = "帖子图片 $index",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // 如果是最后一张图片并且有更多图片未显示，添加蒙版显示数量
                        if (isLastImage && hasMoreImages) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.5f))
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

        else -> {
            // 4-9张图片，网格排列，减小高度限制
            val gridColumns = if (displayImages.size == 4) 2 else 3
            val gridRows = (displayImages.size + gridColumns - 1) / gridColumns

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                (0 until gridRows).forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        (0 until gridColumns).forEach { col ->
                            val index = row * gridColumns + col
                            if (index < displayImages.size) {
                                val isLastImage = index == displayImages.size - 1
                                val hasMoreImages = totalPictures > displayImages.size

                                Box(
                                    modifier = Modifier
                                        .weight(1f, fill = false)
                                        .sizeIn(maxWidth = 300.dp)
                                        .aspectRatio(1f)
                                        .clickable(onClick = onClick)
                                ) {
                                    AsyncImage(
                                        model = displayImages[index],
                                        contentDescription = "帖子图片 $index",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )

                                    // 如果是最后一张图片并且有更多图片未显示，添加蒙版显示数量
                                    if (isLastImage && hasMoreImages) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color.Black.copy(alpha = 0.5f))
                                        ) {
                                            Text(
                                                text = "+${totalPictures - displayImages.size}",
                                                style = MaterialTheme.typography.titleLarge,
                                                color = Color.White
                                            )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostListScreen(
    onPostClick: (Long) -> Unit,
    onNavigateToCreatePost: () -> Unit,
    refresh: Boolean = false
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


    val categories = PostCategory.entries

    // 监听滚动到底部，加载更多
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                if (lastVisibleIndex != null && lastVisibleIndex >= posts.size - 3 && hasMore && !isLoading) {
                    viewModel.loadPosts(category = selectedCategory, sortType = sortType.name.lowercase())
                }
            }
    }

    // 在组件初始化时自动加载帖子
    LaunchedEffect(Unit) {
        viewModel.loadPosts()
    }

    // 处理刷新参数
    LaunchedEffect(refresh) {
        if (refresh) {
            viewModel.refresh()
        }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // 顶栏始终显示在最上方
                TopAppBar(
                    title = { Text("精弘论坛") },
                    actions = {
                        IconButton(onClick = { viewModel.refresh() }) {
                            Icon(AppIcons.Refresh, contentDescription = "刷新")
                        }
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
                                contentColor = MaterialTheme.colorScheme.primary,
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
                                selectedTabIndex = selectedCategoryIndex(selectedCategory, categories),
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.primary,
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
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (posts.isEmpty() && !isLoading) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        AppIcons.Inbox,
                        contentDescription = "空列表",
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
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(posts) {
                        PostItem(
                            post = it,
                            onClick = { onPostClick(it.id) }
                        )
                    }

                    if (isLoading) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }

            // 错误提示
            errorMessage?.let { message ->
                LaunchedEffect(message) {
                    // 可以显示一个Snackbar或Toast
                }
            }
        }
    }


}

@Composable
fun PostItem(
    post: GetPostListElement,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 用户头像和名称
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = post.publisherInfo.avatar ?: "",
                        contentDescription = "用户头像",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = post.publisherInfo.nickname ?: "",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = getCategoryDisplayName(post.category),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // 时间
                Text(
                    text = TimeUtils.formatTime(post.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 标题
            Text(
                text = post.title ?: "",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 内容
            Text(
                text = post.content ?: "",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // 图片预览（如果有）
            if (post.pictures.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                ImageGrid(
                    images = post.pictures.map { it.url },
                    totalPictures = post.totalPictures,
                    onClick = onClick
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 底部信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row {
                    Icon(
                        AppIcons.ThumbUp,
                        contentDescription = "点赞",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${post.likeCount}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row {
                    Icon(
                        AppIcons.Comment,
                        contentDescription = "评论",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${post.commentCount}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row {
                    Icon(
                        AppIcons.Visibility,
                        contentDescription = "浏览",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "0", // GetPostListElement没有viewCount字段
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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