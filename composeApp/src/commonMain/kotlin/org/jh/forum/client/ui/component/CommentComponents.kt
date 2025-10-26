package org.jh.forum.client.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.More
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.jh.forum.client.data.model.CommentElement
import org.jh.forum.client.di.AppModule
import org.jh.forum.client.ui.screen.ImagePicker
import org.jh.forum.client.ui.screen.LocalImagePickerClick
import org.jh.forum.client.ui.theme.AppIcons
import org.jh.forum.client.ui.theme.Dimensions
import org.jh.forum.client.util.TimeUtils

@Composable
fun CommentItem(
    comment: CommentElement,
    onUpvote: () -> Unit,
    onPin: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    onUserProfileClick: (Long) -> Unit = {},
    onImageClick: (String) -> Unit = {},
    onViewReplies: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

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
            // 用户信息和更多选项
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 头像和用户昵称
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            comment.publisherInfo.id?.let {
                                onUserProfileClick(it)
                            }
                        }
                ) {
                    // 用户头像
                    AsyncImage(
                        model = comment.publisherInfo.avatar,
                        contentDescription = "用户头像",
                        modifier = Modifier
                            .size(Dimensions.avatarLarge)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.width(Dimensions.spaceSmall))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (comment.isPinned) {
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = "置顶",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(Dimensions.iconSmall)
                            )
                            Spacer(Modifier.width(Dimensions.spaceExtraSmall))
                        }
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

                // 更多操作菜单
                if (onPin != null || onDelete != null) {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.AutoMirrored.Filled.More, "更多选项")
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            onPin?.let {
                                DropdownMenuItem(
                                    text = { Text(if (comment.isPinned) "取消置顶" else "置顶") },
                                    onClick = {
                                        onPin()
                                        showMenu = false
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.PushPin, null)
                                    }
                                )
                            }
                            onDelete?.let {
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
            }

            // 评论内容
            Spacer(Modifier.height(Dimensions.spaceMedium))
            Text(
                text = comment.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            // 图片展示（如果有）
            if (comment.pictures.isNotEmpty()) {
                Spacer(Modifier.height(Dimensions.spaceSmall))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.spaceSmall),
                    contentPadding = PaddingValues(vertical = Dimensions.spaceExtraSmall)
                ) {
                    items(comment.pictures) { picture ->
                        AsyncImage(
                            model = picture.url,
                            contentDescription = "评论图片",
                            modifier = Modifier
                                .size(Dimensions.imagePreviewMedium)
                                .clip(MaterialTheme.shapes.medium)
                                .clickable {
                                    picture.url?.let { onImageClick(it) }
                                },
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            // Show preview of replies if any
            if (comment.replies.isNotEmpty()) {
                Spacer(Modifier.height(Dimensions.spaceSmall))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.small
                ) {
                    Column(
                        modifier = Modifier.padding(Dimensions.spaceSmall)
                    ) {
                        // Show first 2 replies
                        comment.replies.take(2).forEach { reply ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "${reply.publisherInfo.nickname ?: "未知用户"}: ",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                                )
                                Text(
                                    text = reply.content,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                            Spacer(Modifier.height(Dimensions.spaceExtraSmall))
                        }
                        
                        // Show "view all replies" button if there are replies or if replyCount > 0
                        if (comment.replyCount > 0 && onViewReplies != null) {
                            TextButton(
                                onClick = onViewReplies,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "查看全部 ${comment.replyCount} 条回复",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Icon(
                                    AppIcons.KeyboardArrowRight,
                                    contentDescription = null,
                                    modifier = Modifier.size(Dimensions.iconSmall)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(Dimensions.spaceMedium))

            // 时间和互动区
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 格式化时间
                Text(
                    text = TimeUtils.formatTime(comment.createdAt),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.spaceSmall)
                ) {
                    // Reply button (if callback provided)
                    if (onViewReplies != null) {
                        FilledTonalButton(
                            onClick = onViewReplies,
                            modifier = Modifier.height(Dimensions.buttonHeightSmall),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Icon(
                                AppIcons.Comment,
                                contentDescription = "回复",
                                modifier = Modifier.size(Dimensions.iconSmall)
                            )
                            if (comment.replyCount > 0) {
                                Spacer(Modifier.width(Dimensions.spaceExtraSmall))
                                Text(
                                    text = "${comment.replyCount}",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }

                    // 点赞按钮
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
fun CommentEditor(
    onSubmit: (String, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf("") }
    var selectedImage by remember { mutableStateOf<String?>(null) }
    var isUploadingImage by remember { mutableStateOf(false) }
    
    val postViewModel = AppModule.postViewModel

    // 使用紧凑布局的评论编辑器
    Column(
        modifier = modifier
    ) {
        // 更紧凑的输入框
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("写下你的评论...") },
            maxLines = 4,
            shape = MaterialTheme.shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        // 图片预览（如果有）
        if (selectedImage != null) {
            Spacer(Modifier.height(Dimensions.spaceSmall))
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(MaterialTheme.shapes.medium)
            ) {
                AsyncImage(
                    model = selectedImage,
                    contentDescription = "评论图片",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f),
                    shape = CircleShape
                ) {
                    IconButton(
                        onClick = { selectedImage = null },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            AppIcons.Close,
                            contentDescription = "移除图片",
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // 缩小间距
        Spacer(Modifier.height(Dimensions.spaceSmall))

        // 底部按钮栏
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 添加图片按钮
            ImagePicker(
                onImageSelected = { bytes, filename ->
                    isUploadingImage = true
                    postViewModel.uploadImage(bytes, filename) { url ->
                        isUploadingImage = false
                        if (url != null) {
                            selectedImage = url
                        }
                    }
                },
                enabled = !isUploadingImage && selectedImage == null
            ) {
                FilledTonalIconButton(
                    onClick = LocalImagePickerClick.current,
                    enabled = !isUploadingImage && selectedImage == null
                ) {
                    if (isUploadingImage) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            AppIcons.Image,
                            contentDescription = "添加图片",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // 更紧凑的发布按钮 - Enhanced design
            FilledTonalButton(
                onClick = {
                    if (text.isNotBlank()) {
                        onSubmit(text, selectedImage)
                        text = ""
                        selectedImage = null
                    }
                },
                enabled = text.isNotBlank(),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.spaceExtraSmall)
                ) {
                    Icon(
                        AppIcons.Send,
                        contentDescription = "发布",
                        modifier = Modifier.size(Dimensions.iconSmall)
                    )
                    Text("发布", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}