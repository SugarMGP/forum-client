package org.jh.forum.client.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.More
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import org.jh.forum.client.data.model.CommentInfoResponse
import org.jh.forum.client.data.model.ReplyElement
import org.jh.forum.client.di.AppModule
import org.jh.forum.client.ui.component.CommentEditor
import org.jh.forum.client.ui.theme.AppIcons
import org.jh.forum.client.ui.theme.Dimensions
import org.jh.forum.client.ui.viewmodel.ReplyViewModel
import org.jh.forum.client.util.TimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentRepliesScreen(
    commentId: Long,
    viewModel: ReplyViewModel,
    onBack: () -> Unit,
    onUserClick: (Long) -> Unit = {}
) {
    val commentInfo by viewModel.commentInfo.collectAsState()
    val replies by viewModel.replies.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val hasMore by viewModel.hasMore.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var showReplyDialog by remember { mutableStateOf(false) }
    var replyTarget by remember { mutableStateOf<ReplyElement?>(null) }

    val authViewModel = AppModule.authViewModel
    val currentUserId = authViewModel.userProfile.collectAsState().value?.userId

    LaunchedEffect(commentId) {
        viewModel.loadReplies(commentId, true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("评论详情") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = Dimensions.spaceMedium),
            verticalArrangement = Arrangement.spacedBy(Dimensions.spaceSmall)
        ) {
            // Original comment
            item {
                commentInfo?.let { comment ->
                    OriginalCommentItem(
                        comment = comment,
                        onUpvote = { viewModel.upvoteComment(commentId) },
                        onUserProfileClick = {
                            comment.publisherInfo.id?.let { onUserClick(it) }
                        },
                        onReply = {
                            replyTarget = null
                            showReplyDialog = true
                        }
                    )
                }
            }

            // Replies header
            item {
                Text(
                    text = "回复（${replies.size}）",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(
                        horizontal = Dimensions.spaceMedium,
                        vertical = Dimensions.spaceSmall
                    )
                )
            }

            // Replies list
            if (replies.isEmpty() && !isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimensions.spaceMedium),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "暂无回复",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (replies.isNotEmpty()) {
                items(
                    items = replies,
                    key = { reply -> reply.replyId }
                ) { reply ->
                    ReplyItem(
                        reply = reply,
                        onUpvote = { viewModel.upvoteReply(reply.replyId) },
                        onDelete = if (currentUserId != null && currentUserId == reply.publisherInfo.id) {
                            { viewModel.deleteReply(reply.replyId) }
                        } else null,
                        onUserProfileClick = { userId ->
                            onUserClick(userId)
                        },
                        onReply = {
                            replyTarget = reply
                            showReplyDialog = true
                        }
                    )
                }
            }

            // Loading indicator
            if (isLoading) {
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

            // Error message
            if (errorMessage != null) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimensions.spaceMedium),
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            errorMessage ?: "",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(Dimensions.spaceMedium)
                        )
                    }
                }
            }
        }
    }

    // Reply dialog
    if (showReplyDialog) {
        AlertDialog(
            onDismissRequest = { showReplyDialog = false },
            title = {
                Text(
                    if (replyTarget != null) "回复 @${replyTarget?.publisherInfo?.nickname}" else "回复评论"
                )
            },
            text = {
                CommentEditor(
                    onSubmit = { content, picture ->
                        viewModel.publishReply(commentId, content, picture, replyTarget?.replyId)
                        showReplyDialog = false
                        replyTarget = null
                    }
                )
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showReplyDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
fun OriginalCommentItem(
    comment: CommentInfoResponse,
    onUpvote: () -> Unit,
    onUserProfileClick: () -> Unit,
    onReply: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimensions.spaceMedium, vertical = Dimensions.spaceSmall),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = Dimensions.elevationSmall
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.spaceMedium)
        ) {
            // User info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onUserProfileClick() }
                ) {
                    AsyncImage(
                        model = comment.publisherInfo.avatar,
                        contentDescription = "用户头像",
                        modifier = Modifier
                            .size(Dimensions.avatarLarge)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.width(Dimensions.spaceSmall))
                    Text(
                        text = comment.publisherInfo.nickname ?: "未知用户",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                    )
                    if (comment.isAuthor) {
                        Spacer(Modifier.width(Dimensions.spaceExtraSmall))
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = "楼主",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(
                                    horizontal = Dimensions.spaceSmall,
                                    vertical = Dimensions.spaceExtraSmall
                                )
                            )
                        }
                    }
                }
            }

            // Content
            Spacer(Modifier.height(Dimensions.spaceMedium))
            Text(
                text = comment.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Images
            if (comment.pictures.isNotEmpty()) {
                Spacer(Modifier.height(Dimensions.spaceSmall))
                comment.pictures.forEach { picture ->
                    AsyncImage(
                        model = picture.url,
                        contentDescription = "评论图片",
                        modifier = Modifier
                            .size(Dimensions.imagePreviewMedium)
                            .clip(MaterialTheme.shapes.medium),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(Modifier.height(Dimensions.spaceMedium))

            // Actions
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = TimeUtils.formatTime(comment.createdAt),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.spaceSmall)
                ) {
                    // Reply button
                    FilledTonalButton(
                        onClick = onReply,
                        modifier = Modifier.height(Dimensions.buttonHeightSmall),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Icon(
                            AppIcons.Comment,
                            contentDescription = "回复",
                            modifier = Modifier.size(Dimensions.iconSmall)
                        )
                        Spacer(Modifier.width(Dimensions.spaceExtraSmall))
                        Text("回复", style = MaterialTheme.typography.labelMedium)
                    }

                    // Upvote button
                    FilledTonalIconButton(
                        onClick = onUpvote,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = if (comment.isLiked) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            contentColor = if (comment.isLiked) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Dimensions.spaceExtraSmall),
                            modifier = Modifier.padding(horizontal = Dimensions.spaceSmall)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ThumbUp,
                                contentDescription = "点赞",
                                modifier = Modifier.size(Dimensions.iconSmall)
                            )
                            if (comment.upvoteCount > 0) {
                                Text(
                                    text = "${comment.upvoteCount}",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReplyItem(
    reply: ReplyElement,
    onUpvote: () -> Unit,
    onDelete: (() -> Unit)? = null,
    onUserProfileClick: (Long) -> Unit = {},
    onReply: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimensions.spaceMedium, vertical = Dimensions.spaceSmall),
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = Dimensions.elevationSmall
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.spaceMedium)
        ) {
            // User info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            reply.publisherInfo.id?.let { onUserProfileClick(it) }
                        }
                ) {
                    AsyncImage(
                        model = reply.publisherInfo.avatar,
                        contentDescription = "用户头像",
                        modifier = Modifier
                            .size(Dimensions.avatarMedium)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.width(Dimensions.spaceSmall))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = reply.publisherInfo.nickname ?: "未知用户",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                            )
                            if (reply.isAuthor) {
                                Spacer(Modifier.width(Dimensions.spaceExtraSmall))
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text(
                                        text = "楼主",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(
                                            horizontal = Dimensions.spaceSmall,
                                            vertical = Dimensions.spaceExtraSmall
                                        )
                                    )
                                }
                            }
                        }
                        if (reply.targetUser != null) {
                            Text(
                                text = "回复 @${reply.targetUser?.nickname}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // More options
                if (onDelete != null) {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.AutoMirrored.Filled.More, "更多选项")
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("删除") },
                                onClick = {
                                    onDelete()
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Delete, null)
                                }
                            )
                        }
                    }
                }
            }

            // Content
            Spacer(Modifier.height(Dimensions.spaceSmall))
            Text(
                text = reply.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Images
            if (reply.pictures.isNotEmpty()) {
                Spacer(Modifier.height(Dimensions.spaceSmall))
                reply.pictures.forEach { picture ->
                    AsyncImage(
                        model = picture.url,
                        contentDescription = "回复图片",
                        modifier = Modifier
                            .size(Dimensions.imagePreviewMedium)
                            .clip(MaterialTheme.shapes.medium),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(Modifier.height(Dimensions.spaceSmall))

            // Actions
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = TimeUtils.formatTime(reply.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.spaceSmall)
                ) {
                    // Reply button
                    TextButton(
                        onClick = onReply,
                        modifier = Modifier.height(Dimensions.buttonHeightSmall)
                    ) {
                        Icon(
                            AppIcons.Comment,
                            contentDescription = "回复",
                            modifier = Modifier.size(Dimensions.iconSmall)
                        )
                        Spacer(Modifier.width(Dimensions.spaceExtraSmall))
                        Text("回复", style = MaterialTheme.typography.labelSmall)
                    }

                    // Upvote button
                    FilledTonalIconButton(
                        onClick = onUpvote,
                        modifier = Modifier.size(Dimensions.buttonHeightSmall),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = if (reply.isLiked) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            contentColor = if (reply.isLiked) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Dimensions.spaceExtraSmall),
                            modifier = Modifier.padding(horizontal = Dimensions.spaceSmall)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ThumbUp,
                                contentDescription = "点赞",
                                modifier = Modifier.size(Dimensions.iconSmall)
                            )
                            if (reply.upvoteCount > 0) {
                                Text(
                                    text = "${reply.upvoteCount}",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
