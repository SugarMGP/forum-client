package org.jh.forum.client.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.jh.forum.client.data.model.UpdateNoticeSettingsRequest
import org.jh.forum.client.data.repository.ForumRepository
import org.jh.forum.client.ui.theme.AppIcons
import org.jh.forum.client.ui.theme.Dimensions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    repository: ForumRepository,
    onNavigateBack: () -> Unit = {}
) {
    var upvoteNotice by remember { mutableStateOf(false) }
    var commentNotice by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()
    
    // Handle system back button/gesture
    BackHandler {
        onNavigateBack()
    }

    // 加载通知设置
    LaunchedEffect(Unit) {
        isLoading = true
        errorMessage = null
        try {
            val result = repository.getNoticeSettings()
            if (result.code == 200 && result.data != null) {
                upvoteNotice = result.data.upvoteNotice
                commentNotice = result.data.commentNotice
            } else {
                errorMessage = "加载通知设置失败"
            }
        } catch (e: Exception) {
            errorMessage = "加载通知设置失败: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("通知设置") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = AppIcons.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(Dimensions.spaceMedium),
            verticalArrangement = Arrangement.spacedBy(Dimensions.spaceMedium)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (errorMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = errorMessage ?: "未知错误",
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.elevationSmall),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "点赞通知",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "接收他人点赞您帖子的通知",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = upvoteNotice,
                                onCheckedChange = { checked ->
                                    // 先乐观更新 UI
                                    val previous = upvoteNotice
                                    upvoteNotice = checked

                                    // 保存设置
                                    coroutineScope.launch {
                                        try {
                                            val result = repository.updateNoticeSettings(
                                                UpdateNoticeSettingsRequest(
                                                    upvoteNotice = checked,
                                                    commentNotice = commentNotice
                                                )
                                            )
                                            if (result.code != 200) {
                                                // 恢复原状态
                                                upvoteNotice = previous
                                                errorMessage = "保存设置失败"
                                            }
                                        } catch (e: Exception) {
                                            // 恢复原状态
                                            upvoteNotice = previous
                                            errorMessage = "保存设置失败: ${e.message}"
                                        }
                                    }
                                }
                            )
                        }

                        HorizontalDivider(
                            Modifier,
                            DividerDefaults.Thickness,
                            DividerDefaults.color
                        )

                        // 评论通知设置
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "评论通知",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "接收他人评论您帖子的通知",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = commentNotice,
                                onCheckedChange = { checked ->
                                    val previous = commentNotice
                                    commentNotice = checked

                                    // 保存设置
                                    coroutineScope.launch {
                                        try {
                                            val result = repository.updateNoticeSettings(
                                                UpdateNoticeSettingsRequest(
                                                    commentNotice = checked,
                                                    upvoteNotice = upvoteNotice
                                                )
                                            )
                                            if (result.code != 200) {
                                                // 恢复原状态
                                                commentNotice = previous
                                                errorMessage = "保存设置失败"
                                            }
                                        } catch (e: Exception) {
                                            // 恢复原状态
                                            commentNotice = previous
                                            errorMessage = "保存设置失败: ${e.message}"
                                        }
                                    }
                                }
                            )
                        }
                    }
                }

                // 说明卡片
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "关于通知设置",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text = "您可以根据个人需求选择接收哪些类型的通知。关闭通知后，您将不会收到相应的推送提醒。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
