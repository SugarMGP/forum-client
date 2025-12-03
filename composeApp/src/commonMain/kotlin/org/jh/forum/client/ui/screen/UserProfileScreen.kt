package org.jh.forum.client.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.jh.forum.client.data.model.GetPersonalPostListElement
import org.jh.forum.client.data.model.GetUserProfileResponse
import org.jh.forum.client.data.model.PersonalCommentListElement
import org.jh.forum.client.data.repository.ForumRepository
import org.jh.forum.client.ui.component.ClickableImage
import org.jh.forum.client.ui.component.ImageGalleryDialog
import org.jh.forum.client.ui.theme.AppIcons
import org.jh.forum.client.ui.theme.Dimensions
import org.jh.forum.client.ui.viewmodel.AuthViewModel
import org.jh.forum.client.di.AppModule
import org.jh.forum.client.util.TimeUtils
import org.jh.forum.client.util.getAvatarOrDefault
import org.jh.forum.client.util.rememberDebouncedClick

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun UserProfileScreen(
    userId: Long,
    authViewModel: AuthViewModel,
    repository: ForumRepository,
    onPostClick: (Long) -> Unit = {},
    onNavigateBack: (() -> Unit)? = null,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToPost: (postId: Long, highlightCommentId: Long) -> Unit = { _, _ -> },
    onNavigateToComment: (commentId: Long, highlightReplyId: Long) -> Unit = { _, _ -> }
) {
    var selectedTab by remember { mutableStateOf(0) }
    val isCurrentUser = authViewModel.userProfile.collectAsState().value?.userId == userId
    var userProfile by remember {
        mutableStateOf<GetUserProfileResponse?>(
            null
        )
    }
    var isLoading by remember { mutableStateOf(true) }
    var showImageViewer by remember { mutableStateOf(false) }
    var selectedImageUrl by remember { mutableStateOf<String?>(null) }
    var showImageGallery by remember { mutableStateOf(false) }
    var galleryImages by remember { mutableStateOf<List<String>>(emptyList()) }
    var galleryInitialIndex by remember { mutableStateOf(0) }

    // Pager state for swipe navigation (only if current user)
    val tabCount = if (isCurrentUser) 2 else 1
    val pagerState = rememberPagerState(
        initialPage = selectedTab,
        pageCount = { tabCount }
    )

    // Coroutine scope for programmatic scrolling
    val scope = rememberCoroutineScope()

    // Sync pager state with selectedTab - one direction only
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            if (selectedTab != page) {
                selectedTab = page
            }
        }
    }

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

    Box(modifier = Modifier.fillMaxSize()) {
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
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(0)
                                }
                            },
                            text = { Text("帖子") }
                        )
                        // Only show Comments tab for current user
                        if (isCurrentUser) {
                            Tab(
                                selected = selectedTab == 1,
                                onClick = {
                                    scope.launch {
                                        pagerState.animateScrollToPage(1)
                                    }
                                },
                                text = { Text("评论") }
                            )
                        }
                    }

                    // Tab Content with HorizontalPager
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        when (page) {
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
                                        repository = repository,
                                        onNavigateToPost = onNavigateToPost,
                                        onNavigateToComment = onNavigateToComment
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }  // Close Scaffold content lambda

        // Image gallery dialog - placed outside Scaffold for proper z-order
        ImageGalleryDialog(
            visible = showImageGallery || showImageViewer,
            images = if (showImageViewer && selectedImageUrl != null) {
                listOfNotNull(selectedImageUrl)
            } else {
                galleryImages
            },
            initialIndex = if (showImageViewer) 0 else galleryInitialIndex,
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
    val viewModel = AppModule.userProfileViewModel
    val posts by viewModel.posts.collectAsState()
    val isLoading by viewModel.postsLoading.collectAsState()
    val hasMore by viewModel.postsHasMore.collectAsState()
    val listState = rememberLazyListState()

    // Set user ID and load posts when userId changes
    LaunchedEffect(userId) {
        viewModel.setUserId(userId)
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
    LaunchedEffect(listState, hasMore, isLoading) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                if (lastVisibleIndex != null &&
                    lastVisibleIndex >= posts.size - 2 &&
                    !isLoading &&
                    hasMore
                ) {
                    viewModel.loadPosts(userId, reset = false)
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
        onClick = rememberDebouncedClick(onClick = onClick),
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
            // Title with pin indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimensions.spaceExtraSmall)
            ) {
                // Show pin indicator for pinned posts before title
                if (post.isTopped) {
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
                    overflow = TextOverflow.Ellipsis
                )
            }

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
                        onClick = { clickedUrl: String ->
                            val clickedIndex = imageUrls.indexOf(clickedUrl)
                            onImageClick(imageUrls, if (clickedIndex >= 0) clickedIndex else 0)
                        }
                    )
                }
            }

            if (post.topics.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Dimensions.spaceSmall))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.spaceExtraSmall),
                    modifier = Modifier.fillMaxWidth()
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

            // Stats row
            Spacer(modifier = Modifier.height(Dimensions.spaceSmall))
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
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
    icon: ImageVector,
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
    onNavigateToPost: (postId: Long, highlightCommentId: Long) -> Unit = { _, _ -> },
    onNavigateToComment: (commentId: Long, highlightReplyId: Long) -> Unit = { _, _ -> }
) {
    val viewModel = AppModule.userProfileViewModel
    val comments by viewModel.comments.collectAsState()
    val isLoading by viewModel.commentsLoading.collectAsState()
    val hasMore by viewModel.commentsHasMore.collectAsState()
    val listState = rememberLazyListState()

    // Load comments when first displayed
    LaunchedEffect(Unit) {
        if (comments.isEmpty()) {
            viewModel.loadComments(reset = true)
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
                if (comment.replyId != 0L) {
                    "reply_${comment.replyId}"
                } else {
                    "comment_${comment.commentId}"
                }
            }
        ) { comment ->
            PersonalCommentCard(
                comment = comment,
                onNavigateToPost = onNavigateToPost,
                onNavigateToComment = onNavigateToComment
            )
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
                    viewModel.loadComments(reset = false)
                }
            }
    }
}

@Composable
fun PersonalCommentCard(
    comment: PersonalCommentListElement,
    onNavigateToPost: (postId: Long, highlightCommentId: Long) -> Unit = { _, _ -> },
    onNavigateToComment: (commentId: Long, highlightReplyId: Long) -> Unit = { _, _ -> }
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = rememberDebouncedClick {
            if (comment.replyId != 0L) {
                onNavigateToComment(comment.commentId, comment.replyId)
            } else {
                onNavigateToPost(comment.postId, comment.commentId)
            }
        },
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
            ) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(36.dp)
                        .padding(vertical = 4.dp)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.outline,
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


@Composable
fun UserInfoCard(
    userProfile: GetUserProfileResponse?,
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
                ClickableImage(
                    imageUrl = userProfile?.avatar.getAvatarOrDefault(),
                    contentDescription = "用户头像",
                    modifier = Modifier
                        .size(72.dp)
                        .padding(Dimensions.spaceExtraSmall),
                    contentScale = ContentScale.Crop,
                    shape = CircleShape,
                    onClick = {
                        userProfile?.avatar?.let { onAvatarClick(it.getAvatarOrDefault()) }
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

