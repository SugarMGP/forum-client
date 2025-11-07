package org.jh.forum.client.ui.screen

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
import org.jh.forum.client.data.model.GetAnnouncementListElement
import org.jh.forum.client.data.model.GetNoticeListElement
import org.jh.forum.client.data.repository.ForumRepository
import org.jh.forum.client.ui.theme.AppIcons
import org.jh.forum.client.ui.theme.Dimensions
import org.jh.forum.client.util.TimeUtils
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(
    repository: ForumRepository,
    onUserClick: (Long) -> Unit = {},
    onNavigateToPost: (postId: Long, highlightCommentId: Long) -> Unit = { _, _ -> },
    onNavigateToComment: (commentId: Long, highlightReplyId: Long) -> Unit = { _, _ -> }
) {
    val coroutineScope = rememberCoroutineScope()
    val messageViewModel = org.jh.forum.client.di.AppModule.messageViewModel

    // 消息相关状态
    var messages by remember { mutableStateOf<List<GetNoticeListElement>>(emptyList()) }
    var announcements by remember { mutableStateOf<List<GetAnnouncementListElement>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var unreadNoticeCount by remember { mutableStateOf(0) }
    var unreadAnnouncementCount by remember { mutableStateOf(0) }
    var retryTrigger by remember { mutableStateOf(0) }

    // Pagination state
    var noticeCurrentPage by remember { mutableStateOf(1) }
    var noticeHasMore by remember { mutableStateOf(true) }
    var announcementCurrentPage by remember { mutableStateOf(1) }
    var announcementHasMore by remember { mutableStateOf(true) }

    // UI状态
    var selectedNoticeType by remember { mutableStateOf(0) } // 0:全部, 1:点赞, 2:收藏, 3:评论和@
    var selectedType by remember { mutableStateOf(0) } // 0:互动, 1:公告
    var selectedAnnouncementType by remember { mutableStateOf(0) } // 0:全部, 1:学校公告, 2:系统公告

    // List states - hoist outside when branches to preserve scroll position
    val noticeListState = rememberLazyListState()
    val announcementListState = rememberLazyListState()

    // 加载互动消息
    suspend fun loadNotices(reset: Boolean = false) {
        if (!reset && !noticeHasMore) return // Don't load if no more data

        isLoading = true
        errorMessage = null
        try {
            val page = if (reset) 1 else noticeCurrentPage
            // 请求通知列表
            val noticeResponse =
                repository.getNoticeList(page = page, pageSize = 20, type = selectedNoticeType)
            if (noticeResponse.code == 200 && noticeResponse.data != null) {
                val response = noticeResponse.data
                val newNotices = response.list

                messages = if (reset) {
                    // Reset: replace with new data
                    noticeCurrentPage = 1
                    newNotices
                } else {
                    // Append: merge with deduplication across pages
                    val existingIds = messages.map { it.id }.toSet()
                    val uniqueNewNotices = newNotices.filter { it.id !in existingIds }
                    messages + uniqueNewNotices
                }

                // Update pagination state
                noticeHasMore = page * response.pageSize < response.total
                if (noticeHasMore) noticeCurrentPage++
            } else {
                errorMessage = "加载通知失败"
            }
            isLoading = false
        } catch (e: Exception) {
            errorMessage = "加载通知失败"
            e.printStackTrace()
            isLoading = false
        }
    }

    // 加载公告列表
    suspend fun loadAnnouncements(reset: Boolean = false) {
        if (!reset && !announcementHasMore) return // Don't load if no more data

        isLoading = true
        errorMessage = null
        try {
            val page = if (reset) 1 else announcementCurrentPage
            // 请求公告列表，根据选择的类型传递参数
            val type = when (selectedAnnouncementType) {
                1 -> "scholastic"
                2 -> "systematic"
                else -> ""
            }
            val announcementResponse =
                repository.getAnnouncementList(page = page, pageSize = 20, type = type)
            if (announcementResponse.code == 200 && announcementResponse.data != null) {
                val response = announcementResponse.data
                val newAnnouncements = response.list

                announcements = if (reset) {
                    // Reset: replace with new data
                    announcementCurrentPage = 1
                    newAnnouncements
                } else {
                    // Append: merge with deduplication across pages
                    val existingIds = announcements.map { it.id }.toSet()
                    val uniqueNewAnnouncements = newAnnouncements.filter { it.id !in existingIds }
                    announcements + uniqueNewAnnouncements
                }

                // Update pagination state
                announcementHasMore = page * response.pageSize < response.total
                if (announcementHasMore) announcementCurrentPage++
            } else {
                errorMessage = "加载公告失败"
            }
            isLoading = false
        } catch (e: Exception) {
            errorMessage = "加载公告失败"
            e.printStackTrace()
            isLoading = false
        }
    }

    // 检查未读消息
    suspend fun checkUnreadMessages() {
        try {
            val response = repository.checkUnread()
            if (response.code == 200 && response.data != null) {
                unreadNoticeCount = response.data.unreadNoticeCount
                unreadAnnouncementCount = response.data.unreadAnnouncementCount
                // Update the messageViewModel state directly without making another API call
                messageViewModel.updateUnreadCounts(response.data.unreadNoticeCount, response.data.unreadAnnouncementCount)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 初始加载和重试加载（在 LaunchedEffect 中启动 suspend 加载）
    LaunchedEffect(Unit, retryTrigger, selectedNoticeType, selectedType, selectedAnnouncementType) {
        checkUnreadMessages()
        // Reset pagination state when filters change
        noticeCurrentPage = 1
        noticeHasMore = true
        announcementCurrentPage = 1
        announcementHasMore = true

        if (selectedType == 0) {
            loadNotices(reset = true)
        } else {
            loadAnnouncements(reset = true)
        }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                TopAppBar(
                    title = {
                        Text("消息")
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )

                Column {
                    // 第一层：互动/公告选项卡
                    Surface(modifier = Modifier.fillMaxWidth()) {
                        SecondaryScrollableTabRow(
                            selectedTabIndex = selectedType,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("互动", "公告").forEachIndexed { index, title ->
                                Tab(
                                    selected = selectedType == index,
                                    onClick = {
                                        selectedType = index
                                        // 切换时重置选中的子类型
                                        if (index == 0) selectedNoticeType = 0
                                    },
                                    text = { Text(title) }
                                )
                            }
                        }
                    }

                    // 第二层：子类型选项卡
                    Surface(modifier = Modifier.fillMaxWidth()) {
                        // 根据选中的类型显示对应的子选项卡
                        if (selectedType == 0) {
                            // 互动子类型选项卡
                            SecondaryScrollableTabRow(
                                selectedTabIndex = selectedNoticeType,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                listOf(
                                    "全部",
                                    "点赞",
                                    "收藏",
                                    "评论和@"
                                ).forEachIndexed { index, title ->
                                    Tab(
                                        selected = selectedNoticeType == index,
                                        onClick = { selectedNoticeType = index },
                                        text = { Text(title) }
                                    )
                                }
                            }
                        } else {
                            // 公告子类型选项卡
                            SecondaryScrollableTabRow(
                                selectedTabIndex = selectedAnnouncementType,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                listOf(
                                    "全部",
                                    "学校公告",
                                    "系统公告"
                                ).forEachIndexed { index, title ->
                                    Tab(
                                        selected = selectedAnnouncementType == index,
                                        onClick = { selectedAnnouncementType = index },
                                        text = { Text(title) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // 只在最外层Column应用paddingValues
        ) {
            when {
                isLoading && messages.isEmpty() && announcements.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                errorMessage != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
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
                            Row {
                                Button(onClick = {
                                    // 触发重新加载
                                    retryTrigger++
                                }) {
                                    Text("重试")
                                }
                            }
                        }
                    }
                }

                // 显示互动消息列表
                selectedType == 0 && messages.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(Dimensions.spaceLarge),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(120.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        AppIcons.Message,
                                        contentDescription = "无消息",
                                        modifier = Modifier.size(64.dp),
                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(Dimensions.spaceLarge))
                            Text(
                                text = "暂无互动消息",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(Dimensions.spaceSmall))
                            Text(
                                text = "当有人点赞、评论或收藏您的帖子时\n您将在这里收到通知",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                // 显示公告列表
                selectedType == 1 && announcements.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(Dimensions.spaceLarge),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(120.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        AppIcons.Notifications,
                                        contentDescription = "无公告",
                                        modifier = Modifier.size(64.dp),
                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(Dimensions.spaceLarge))
                            Text(
                                text = "暂无公告",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(Dimensions.spaceSmall))
                            Text(
                                text = "系统公告和学校公告将在这里显示",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                // 显示互动消息
                selectedType == 0 -> {
                    // Monitor scroll position for pagination
                    LaunchedEffect(noticeListState) {
                        snapshotFlow { noticeListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
                            .collect { lastVisibleIndex ->
                                if (lastVisibleIndex != null &&
                                    lastVisibleIndex >= messages.size - 3 &&
                                    noticeHasMore &&
                                    !isLoading
                                ) {
                                    coroutineScope.launch {
                                        loadNotices(reset = false)
                                    }
                                }
                            }
                    }

                    LazyColumn(
                        state = noticeListState,
                        modifier = Modifier
                            .fillMaxSize(),
                        contentPadding = PaddingValues(Dimensions.spaceMedium),
                        verticalArrangement = Arrangement.spacedBy(Dimensions.spaceMedium)
                    ) {
                        items(messages) {
                            MessageItem(
                                message = it,
                                onUserClick = onUserClick,
                                onNavigateToPost = onNavigateToPost,
                                onNavigateToComment = onNavigateToComment
                            )
                        }

                        // Loading indicator - only show during initial load, not pagination
                        if (isLoading && messages.isEmpty()) {
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

                // 显示公告
                else -> {
                    // Monitor scroll position for pagination
                    LaunchedEffect(announcementListState) {
                        snapshotFlow { announcementListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
                            .collect { lastVisibleIndex ->
                                if (lastVisibleIndex != null &&
                                    lastVisibleIndex >= announcements.size - 3 &&
                                    announcementHasMore &&
                                    !isLoading
                                ) {
                                    coroutineScope.launch {
                                        loadAnnouncements(reset = false)
                                    }
                                }
                            }
                    }

                    LazyColumn(
                        state = announcementListState,
                        modifier = Modifier
                            .fillMaxSize(),
                        contentPadding = PaddingValues(Dimensions.spaceMedium),
                        verticalArrangement = Arrangement.spacedBy(Dimensions.spaceSmall)
                    ) {
                        items(announcements) {
                            AnnouncementItem(announcement = it)
                        }

                        // Loading indicator - only show during initial load, not pagination
                        if (isLoading && announcements.isEmpty()) {
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
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
fun MessageItem(
    message: GetNoticeListElement,
    onUserClick: (Long) -> Unit = {},
    onNavigateToPost: (postId: Long, highlightCommentId: Long) -> Unit = { _, _ -> },
    onNavigateToComment: (commentId: Long, highlightReplyId: Long) -> Unit = { _, _ -> }
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = message.positionContent != null) {
                when {
                    message.type == "comment" -> {
                        when {
                            message.positionType == "post" -> {
                                onNavigateToPost(message.postId, message.newCommentId)
                            }

                            else -> {
                                onNavigateToComment(message.commentId, message.newCommentId)
                            }
                        }
                    }

                    else -> {
                        when (message.positionType) {
                            "post" -> {
                                onNavigateToPost(message.postId, 0L)
                            }

                            "comment" -> {
                                onNavigateToPost(message.postId, message.commentId)
                            }

                            "reply" -> {
                                onNavigateToComment(message.commentId, message.replyId)
                            }
                        }
                    }
                }
            },
        elevation = CardDefaults.cardElevation(
            defaultElevation = Dimensions.elevationSmall,
            pressedElevation = Dimensions.elevationMedium
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.spaceMedium)
        ) {
            // Header: Avatar + Name + Action + Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Avatar and user info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .clickable {
                            message.senderInfo.id?.let { onUserClick(it) }
                        }
                ) {
                    AsyncImage(
                        model = message.senderInfo.avatar,
                        contentDescription = "用户头像",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(Dimensions.spaceSmall))

                    Column(
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Text(
                            text = message.senderInfo.nickname ?: "用户",
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = getActionText(message),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Time and unread indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.spaceSmall)
                ) {
                    Text(
                        text = TimeUtils.formatTime(message.updatedAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!message.isRead) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(6.dp)
                        ) {}
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimensions.spaceSmall))

            // Comment content (if it's a comment message)
            if (message.type == "comment" && message.newCommentContent != null) {
                Text(
                    text = message.newCommentContent,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = Dimensions.spaceSmall),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(Dimensions.spaceSmall))
            }

            // Original content (quote style with minimal design)
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(36.dp)
                        .background(
                            color = MaterialTheme.colorScheme.outline,
                            shape = MaterialTheme.shapes.extraSmall
                        )
                )

                Spacer(modifier = Modifier.width(Dimensions.spaceSmall))

                Text(
                    text = message.positionContent ?: "内容不存在",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).align(Alignment.CenterVertically)
                )
            }
        }
    }
}

// 获取操作文本
private fun getActionText(message: GetNoticeListElement): String {
    return when (message.type) {
        "like" -> "赞了你的内容"
        "comment" -> "评论了你的内容"
        "at" -> "@了你"
        "collect" -> "收藏了你的内容"
        else -> "互动"
    }
}

@OptIn(ExperimentalTime::class)
@Composable
fun AnnouncementItem(announcement: GetAnnouncementListElement) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = Dimensions.elevationSmall,
            pressedElevation = Dimensions.elevationMedium
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = MaterialTheme.shapes.large,
        onClick = { isExpanded = !isExpanded }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.spaceMedium),
            verticalAlignment = Alignment.Top
        ) {
            // Icon
            Icon(
                AppIcons.Notifications,
                contentDescription = "公告",
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(Dimensions.spaceSmall))

            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimensions.spaceSmall),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = announcement.title,
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        // Show badge for sticky announcements
                        if (announcement.sticky) {
                            Surface(
                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                shape = MaterialTheme.shapes.extraSmall
                            ) {
                                Text(
                                    text = "置顶",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(Dimensions.spaceSmall))

                    // Time and unread indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimensions.spaceSmall)
                    ) {
                        Text(
                            text = TimeUtils.formatTime(announcement.publishedAt),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (!announcement.isRead) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(6.dp)
                            ) {}
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Dimensions.spaceExtraSmall))

                // Content with animated height transition
                Text(
                    text = announcement.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .animateContentSize(
                            animationSpec = tween(
                                durationMillis = 300,
                                easing = FastOutSlowInEasing
                            )
                        )
                )

                // Signatory below content when expanded or if short enough
                if (!announcement.signatory.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(Dimensions.spaceSmall))
                    Text(
                        text = "—— ${announcement.signatory}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }

            // Expand/collapse icon indicator on the right side
            if (announcement.content.length > 50) {
                Spacer(modifier = Modifier.width(Dimensions.spaceSmall))
                Icon(
                    imageVector = if (isExpanded) AppIcons.ExpandLess else AppIcons.ExpandMore,
                    contentDescription = if (isExpanded) "收起" else "展开",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}