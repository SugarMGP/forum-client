package org.jh.forum.client.ui.screen

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.jh.forum.client.data.model.GetPersonalPostListElement
import org.jh.forum.client.data.repository.ForumRepository
import org.jh.forum.client.ui.theme.AppIcons
import org.jh.forum.client.ui.theme.Dimensions
import org.jh.forum.client.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    userId: Long,
    authViewModel: AuthViewModel,
    repository: ForumRepository,
    onPostClick: (Long) -> Unit = {},
    onNavigateBack: (() -> Unit)? = null,
    onNavigateToSettings: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }
    val isCurrentUser = authViewModel.userProfile.collectAsState().value?.userId == userId
    var userProfile by remember { mutableStateOf<org.jh.forum.client.data.model.GetUserProfileResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    
    // Load user profile
    LaunchedEffect(userId) {
        try {
            isLoading = true
            val result = repository.getProfile(userId)
            if (result.code == 200) {
                userProfile = result.data
            }
            isLoading = false
        } catch (e: Exception) {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(userProfile?.nickname ?: "用户主页") },
                navigationIcon = {
                    if (onNavigateBack != null) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(AppIcons.ArrowBack, contentDescription = "返回")
                        }
                    }
                },
                actions = {
                    if (isCurrentUser) {
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(AppIcons.Settings, contentDescription = "设置")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Tabs
                PrimaryTabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("帖子") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("评论") }
                    )
                }

                // Tab Content with user info header inside
                when (selectedTab) {
                    0 -> UserPostsTab(
                        userId = userId,
                        repository = repository,
                        userProfile = userProfile,
                        onPostClick = onPostClick
                    )
                    1 -> UserCommentsTab(
                        userId = userId,
                        repository = repository,
                        userProfile = userProfile
                    )
                }
            }
        }
    }
}

@Composable
fun UserPostsTab(
    userId: Long,
    repository: ForumRepository,
    userProfile: org.jh.forum.client.data.model.GetUserProfileResponse?,
    onPostClick: (Long) -> Unit
) {
    var posts by remember { mutableStateOf<List<GetPersonalPostListElement>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var hasMore by remember { mutableStateOf(true) }
    var currentPage by remember { mutableStateOf(1) }
    val listState = rememberLazyListState()

    // Load posts
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
            }
            isLoading = false
        } catch (e: Exception) {
            isLoading = false
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(Dimensions.spaceMedium),
        verticalArrangement = Arrangement.spacedBy(Dimensions.spaceMedium)
    ) {
        // User Info Card Header
        item {
            UserInfoCard(userProfile = userProfile)
        }

        items(posts, key = { it.id }) { post ->
            PersonalPostCard(post = post, onClick = { onPostClick(post.id) })
        }

        if (isLoading && posts.isNotEmpty()) {
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

        if (!isLoading && posts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimensions.spaceExtraLarge),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "暂无帖子",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // Load more when scrolled to bottom
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                if (lastVisibleIndex != null &&
                    lastVisibleIndex >= posts.size - 2 &&
                    !isLoading &&
                    hasMore
                ) {
                    currentPage++
                }
            }
    }
}

@Composable
fun PersonalPostCard(
    post: GetPersonalPostListElement,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.elevationSmall),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.spaceMedium)
        ) {
            // Pinned badge (if pinned)
            if (post.isTopped) {
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = MaterialTheme.shapes.extraSmall,
                    modifier = Modifier.padding(bottom = Dimensions.spaceSmall)
                ) {
                    Text(
                        text = "置顶",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Title
            Text(
                text = post.title ?: "",
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Content
            post.content?.let { content ->
                if (content.isNotBlank()) {
                    Spacer(modifier = Modifier.height(Dimensions.spaceSmall))
                    Text(
                        text = content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Images
            if (post.pictures.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Dimensions.spaceMedium))
                ImageGrid(
                    images = post.pictures.map { it.url },
                    totalPictures = post.totalPictures,
                    onClick = { }
                )
            }

            // Topic tags (below content)
            if (post.topics.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Dimensions.spaceSmall))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.spaceSmall)
                ) {
                    post.topics.take(2).forEach { topic ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.extraSmall
                        ) {
                            Text(
                                text = "#$topic",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Stats row
            Spacer(modifier = Modifier.height(Dimensions.spaceMedium))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.spaceMedium)
                ) {
                    PostStatChip(icon = AppIcons.ThumbUp, count = post.likeCount)
                    PostStatChip(icon = AppIcons.Comment, count = post.commentCount)
                    PostStatChip(icon = AppIcons.Visibility, count = post.viewCount)
                }

                Text(
                    text = post.createdAt,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun PostStatChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    count: Int
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimensions.spaceExtraSmall)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(Dimensions.iconSmall),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun UserCommentsTab(
    userId: Long,
    repository: ForumRepository,
    userProfile: org.jh.forum.client.data.model.GetUserProfileResponse?
) {
    val listState = rememberLazyListState()
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(Dimensions.spaceMedium),
        verticalArrangement = Arrangement.spacedBy(Dimensions.spaceMedium)
    ) {
        // User Info Card Header
        item {
            UserInfoCard(userProfile = userProfile)
        }
        
        // Placeholder for comments
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimensions.spaceExtraLarge),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "评论功能开发中...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun UserInfoCard(
    userProfile: org.jh.forum.client.data.model.GetUserProfileResponse?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.elevationSmall),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(Dimensions.spaceLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            Surface(
                shape = CircleShape,
                shadowElevation = Dimensions.elevationSmall,
                color = MaterialTheme.colorScheme.surface
            ) {
                AsyncImage(
                    model = userProfile?.avatar,
                    contentDescription = "用户头像",
                    modifier = Modifier
                        .size(Dimensions.avatarExtraLarge)
                        .padding(Dimensions.spaceExtraSmall)
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(Dimensions.spaceMedium))

            // Username
            Text(
                text = userProfile?.nickname ?: "",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(Dimensions.spaceSmall))

            // Signature
            userProfile?.signature?.let { signature ->
                Text(
                    text = signature,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

