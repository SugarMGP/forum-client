package org.jh.forum.client.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
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
    onUserClick: (Long) -> Unit = {}
) {
    // 消息相关状态
    var messages by remember { mutableStateOf<List<GetNoticeListElement>>(emptyList()) }
    var announcements by remember { mutableStateOf<List<GetAnnouncementListElement>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var unreadNoticeCount by remember { mutableStateOf(0) }
    var unreadAnnouncementCount by remember { mutableStateOf(0) }
    var retryTrigger by remember { mutableStateOf(0) }

    // UI状态
    var selectedNoticeType by remember { mutableStateOf(0) } // 0:全部, 1:点赞, 2:收藏, 3:评论和@
    var selectedType by remember { mutableStateOf(0) } // 0:互动, 1:公告
    var selectedAnnouncementType by remember { mutableStateOf(0) } // 0:全部, 1:学校公告, 2:系统公告

    // 加载互动消息
    suspend fun loadNoticesOnce() {
        isLoading = true
        errorMessage = null
        try {
            // 请求通知列表
            val noticeResponse = repository.getNoticeList(page = 1, pageSize = 20, type = selectedNoticeType)
            if (noticeResponse.code == 200 && noticeResponse.data != null) {
                messages = noticeResponse.data.list
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
    suspend fun loadAnnouncementsOnce() {
        isLoading = true
        errorMessage = null
        try {
            // 请求公告列表，根据选择的类型传递参数
            val type = when (selectedAnnouncementType) {
                1 -> "scholastic"
                2 -> "systematic"
                else -> ""
            }
            val announcementResponse = repository.getAnnouncementList(page = 1, pageSize = 20, type = type)
            if (announcementResponse.code == 200 && announcementResponse.data != null) {
                announcements = announcementResponse.data.list
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
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 初始加载和重试加载（在 LaunchedEffect 中启动 suspend 加载）
    LaunchedEffect(Unit, retryTrigger, selectedNoticeType, selectedType, selectedAnnouncementType) {
        checkUnreadMessages()
        if (selectedType == 0) {
            loadNoticesOnce()
        } else {
            loadAnnouncementsOnce()
        }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("消息")
                            // 底栏红点逻辑：如果两个数不都为0则显示红点
                            if (unreadNoticeCount > 0 || unreadAnnouncementCount > 0) {
                                Spacer(modifier = Modifier.width(Dimensions.spaceSmall))
                                Box(
                                    modifier = Modifier
                                        .size(Dimensions.spaceSmall)
                                        .background(
                                            color = MaterialTheme.colorScheme.error,
                                            shape = CircleShape
                                        )
                                )
                            }
                        }
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
                                listOf("全部", "点赞", "收藏", "评论和@").forEachIndexed { index, title ->
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
                                listOf("全部", "学校公告", "系统公告").forEachIndexed { index, title ->
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
                isLoading -> {
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
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentPadding = PaddingValues(Dimensions.spaceMedium),
                        verticalArrangement = Arrangement.spacedBy(Dimensions.spaceMedium)
                    ) {
                        items(messages) {
                            MessageItem(
                                message = it,
                                onUserClick = onUserClick
                            )
                        }
                    }
                }

                // 显示公告
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentPadding = PaddingValues(Dimensions.spaceMedium),
                        verticalArrangement = Arrangement.spacedBy(Dimensions.spaceSmall)
                    ) {
                        items(announcements) {
                            AnnouncementItem(announcement = it)
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
    onUserClick: (Long) -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.elevationMedium),
        colors = CardDefaults.cardColors(
            containerColor = if (!message.isRead) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.spaceMedium)
        ) {
            // Header: Avatar + Name + Time + Unread badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar and user info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            message.senderInfo.id?.let { onUserClick(it) }
                        }
                ) {
                    Surface(
                        shape = CircleShape,
                        shadowElevation = Dimensions.elevationSmall,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        AsyncImage(
                            model = message.senderInfo.avatar,
                            contentDescription = "用户头像",
                            modifier = Modifier
                                .size(Dimensions.avatarLarge)
                                .padding(Dimensions.spaceExtraSmall)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.width(Dimensions.spaceSmall))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = message.senderInfo.nickname ?: "用户",
                                style = MaterialTheme.typography.titleSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        
                        Text(
                            text = getActionText(message),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
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
                            modifier = Modifier.size(8.dp)
                        ) {}
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimensions.spaceSmall))

            // Comment content (if it's a comment message)
            if (message.type == "comment" && message.newCommentContent != null) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = message.newCommentContent,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(Dimensions.spaceSmall),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(Dimensions.spaceSmall))
            }

            // Original content (quote style)
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(Dimensions.spaceSmall)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .width(3.dp)
                            .height(40.dp)
                    ) {}
                    
                    Spacer(modifier = Modifier.width(Dimensions.spaceSmall))
                    
                    Text(
                        text = message.positionContent ?: "内容不存在",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.elevationSmall),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.spaceMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 公告图标
            Box(
                modifier = Modifier.size(Dimensions.avatarLarge),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    AppIcons.Notifications,
                    contentDescription = "公告",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(Dimensions.spaceMedium))

            // 公告内容
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = announcement.title,
                        style = if (!announcement.isRead) {
                            MaterialTheme.typography.titleMedium
                        } else {
                            MaterialTheme.typography.bodyLarge
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // 未读标记（圆点）
                    if (!announcement.isRead) {
                        Box(
                            modifier = Modifier
                                .size(Dimensions.spaceSmall)
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Dimensions.spaceExtraSmall))

                Text(
                    text = announcement.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = TimeUtils.formatTime(announcement.publishedAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}