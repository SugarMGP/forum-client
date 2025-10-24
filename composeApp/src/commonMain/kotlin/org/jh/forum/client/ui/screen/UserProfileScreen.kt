package org.jh.forum.client.ui.screen

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.jh.forum.client.data.model.GetPersonalPostListElement
import org.jh.forum.client.data.repository.ForumRepository
import org.jh.forum.client.ui.component.ImageGalleryDialog
import org.jh.forum.client.ui.component.SharedElementImage
import org.jh.forum.client.ui.theme.AppIcons
import org.jh.forum.client.ui.theme.Dimensions
import org.jh.forum.client.ui.viewmodel.AuthViewModel
import org.jh.forum.client.util.TimeUtils

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.UserProfileScreen(
    userId: Long,
    authViewModel: AuthViewModel,
    repository: ForumRepository,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onPostClick: (Long) -> Unit = {},
    onNavigateBack: (() -> Unit)? = null,
    onNavigateToSettings: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }
    val isCurrentUser = authViewModel.userProfile.collectAsState().value?.userId == userId
    var userProfile by remember {
        mutableStateOf<org.jh.forum.client.data.model.GetUserProfileResponse?>(
            null
        )
    }
    var isLoading by remember { mutableStateOf(true) }
    var showImageViewer by remember { mutableStateOf(false) }
    var selectedImageUrl by remember { mutableStateOf<String?>(null) }
    var showImageGallery by remember { mutableStateOf(false) }
    var galleryImages by remember { mutableStateOf<List<String>>(emptyList()) }
    var galleryInitialIndex by remember { mutableStateOf(0) }

    // Load user profile
    LaunchedEffect(userId) {
        try {
            isLoading = true
            val result = repository.getProfile(userId)
            if (result.code == 200) {
                userProfile = result.data
            }
            isLoading = false
        } catch (_: Exception) {
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
                // User Info Card at top
                UserInfoCard(
                    userProfile = userProfile,
                    animatedVisibilityScope = animatedVisibilityScope,
                    onAvatarClick = { avatarUrl ->
                        selectedImageUrl = avatarUrl
                        showImageViewer = true
                    },
                    modifier = Modifier.padding(Dimensions.spaceMedium)
                )

                // Tabs below user info
                PrimaryTabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("帖子") }
                    )
                    // Only show Comments tab for current user
                    if (isCurrentUser) {
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("评论") }
                        )
                    }
                }

                // Tab Content
                when (selectedTab) {
                    0 -> UserPostsTab(
                        userId = userId,
                        repository = repository,
                        onPostClick = onPostClick,
                        onImageClick = { images, index ->
                            galleryImages = images
                            galleryInitialIndex = index
                            showImageGallery = true
                        }
                    )

                    1 -> {
                        if (isCurrentUser) {
                            UserCommentsTab(
                                userId = userId,
                                repository = repository
                            )
                        }
                    }
                }
            }
        }

        // Image gallery dialog - also used for avatar (single image)
        ImageGalleryDialog(
            visible = showImageGallery || showImageViewer,
            images = if (showImageViewer && selectedImageUrl != null) {
                listOf(selectedImageUrl).filterNotNull()
            } else {
                galleryImages
            },
            initialIndex = if (showImageViewer) 0 else galleryInitialIndex,
            sharedTransitionScope = this@UserProfileScreen,
            animatedVisibilityScope = animatedVisibilityScope,
            onDismiss = {
                showImageGallery = false
                showImageViewer = false
                galleryImages = emptyList()
                galleryInitialIndex = 0
                selectedImageUrl = null
            }
        )
    }
}

@Composable
fun UserPostsTab(
    userId: Long,
    repository: ForumRepository,
    onPostClick: (Long) -> Unit,
    onImageClick: (List<String>, Int) -> Unit = { _, _ -> }
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
            val result =
                repository.getPersonalPostList(page = currentPage, pageSize = 20, userId = userId)
            if (result.code == 200 && result.data != null) {
                val postList = result.data
                posts = if (currentPage == 1) {
                    postList.list
                } else {
                    // Merge new posts with existing ones, filtering out duplicates
                    val existingIds = posts.map { it.id }.toSet()
                    val uniqueNewPosts = postList.list.filter { it.id !in existingIds }
                    posts + uniqueNewPosts
                }
                hasMore = postList.page * postList.pageSize < postList.total
            }
            isLoading = false
        } catch (_: Exception) {
            isLoading = false
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(Dimensions.spaceMedium),
        verticalArrangement = Arrangement.spacedBy(Dimensions.spaceMedium)
    ) {
        items(posts, key = { it.id }) { post ->
            PersonalPostCard(
                post = post,
                onClick = { onPostClick(post.id) },
                onImageClick = { images, index -> onImageClick(images, index) }
            )
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
    onClick: () -> Unit,
    onImageClick: (List<String>, Int) -> Unit = { _, _ -> }
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
                val imageUrls = post.pictures.mapNotNull { it.url }
                if (imageUrls.isNotEmpty()) {
                    ImageGrid(
                        images = imageUrls,
                        totalPictures = post.totalPictures,
                        onClick = { clickedUrl ->
                            val clickedIndex = imageUrls.indexOf(clickedUrl)
                            onImageClick(imageUrls, if (clickedIndex >= 0) clickedIndex else 0)
                        }
                    )
                }
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
                    text = TimeUtils.formatTime(post.createdAt),
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
    repository: ForumRepository
) {
    var comments by remember {
        mutableStateOf<List<org.jh.forum.client.data.model.PersonalCommentListElement>>(
            emptyList()
        )
    }
    var isLoading by remember { mutableStateOf(true) }
    var hasMore by remember { mutableStateOf(true) }
    var currentPage by remember { mutableStateOf(1) }
    val listState = rememberLazyListState()

    // Load comments
    LaunchedEffect(currentPage, userId) {
        if (!hasMore && currentPage > 1) return@LaunchedEffect  // Don't load if no more data

        try {
            isLoading = true
            val result = repository.getPersonalComment(page = currentPage, pageSize = 20)
            if (result.code == 200 && result.data != null) {
                val commentList = result.data
                comments = if (currentPage == 1) {
                    commentList.list
                } else {
                    // Merge new comments with existing ones, filtering out duplicates
                    // Use combination of commentId and replyId for unique identification
                    val existingKeys = comments.map {
                        if (it.replyId != 0L) "reply_${it.replyId}" else "comment_${it.commentId}"
                    }.toSet()
                    val uniqueNewComments = commentList.list.filter { comment ->
                        val key =
                            if (comment.replyId != 0L) "reply_${comment.replyId}" else "comment_${comment.commentId}"
                        key !in existingKeys
                    }
                    comments + uniqueNewComments
                }
                hasMore = commentList.page * commentList.pageSize < commentList.total
            }
            isLoading = false
        } catch (_: Exception) {
            isLoading = false
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(Dimensions.spaceMedium),
        verticalArrangement = Arrangement.spacedBy(Dimensions.spaceMedium)
    ) {
        items(
            items = comments,
            key = { comment ->
                // Create unique key combining commentId and replyId
                // If replyId is non-zero, it's a reply, otherwise it's a comment
                if (comment.replyId != 0L) {
                    "reply_${comment.replyId}"
                } else {
                    "comment_${comment.commentId}"
                }
            }
        ) { comment ->
            PersonalCommentCard(comment = comment)
        }

        if (isLoading && comments.isNotEmpty()) {
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

        if (!isLoading && comments.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimensions.spaceExtraLarge),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "暂无评论",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // Load more when scrolled to bottom
    LaunchedEffect(listState, hasMore, isLoading) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                if (lastVisibleIndex != null &&
                    lastVisibleIndex >= comments.size - 2 &&
                    !isLoading &&
                    hasMore &&
                    comments.isNotEmpty()
                ) {
                    currentPage++
                }
            }
    }
}

@Composable
fun PersonalCommentCard(
    comment: org.jh.forum.client.data.model.PersonalCommentListElement
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
            // Comment content
            Text(
                text = comment.content,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = Dimensions.spaceSmall)
            )

            // Target content (quoted style)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Dimensions.spaceSmall)
            ) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(36.dp)
                        .padding(vertical = 4.dp)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = MaterialTheme.shapes.extraSmall
                    ) {}
                }

                Spacer(modifier = Modifier.width(Dimensions.spaceSmall))

                Text(
                    text = comment.targetContent ?: "内容不存在",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).align(Alignment.CenterVertically)
                )
            }

            // Stats row
            Spacer(modifier = Modifier.height(Dimensions.spaceSmall))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.spaceMedium)
                ) {
                    PostStatChip(icon = AppIcons.ThumbUp, count = comment.upvoteCount)
                    PostStatChip(icon = AppIcons.Comment, count = comment.replyCount)
                }

                Text(
                    text = TimeUtils.formatTime(comment.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.UserInfoCard(
    userProfile: org.jh.forum.client.data.model.GetUserProfileResponse?,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onAvatarClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.elevationSmall),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.spaceMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar on the left - clickable
            Surface(
                shape = CircleShape,
                shadowElevation = Dimensions.elevationSmall,
                color = MaterialTheme.colorScheme.surface
            ) {
                SharedElementImage(
                    imageUrl = userProfile?.avatar,
                    contentDescription = "用户头像",
                    modifier = Modifier
                        .size(72.dp)
                        .padding(Dimensions.spaceExtraSmall),
                    animatedVisibilityScope = animatedVisibilityScope,
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    shape = CircleShape,
                    onClick = {
                        userProfile?.avatar?.let { onAvatarClick(it) }
                    }
                )
            }

            Spacer(modifier = Modifier.width(Dimensions.spaceMedium))

            // Info on the right
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Username
                Text(
                    text = userProfile?.nickname ?: "",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(Dimensions.spaceExtraSmall))

                // Signature
                userProfile?.signature?.let { signature ->
                    if (signature.isNotBlank()) {
                        Text(
                            text = signature,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

