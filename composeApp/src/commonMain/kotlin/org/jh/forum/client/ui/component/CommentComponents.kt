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
import coil3.compose.AsyncImage
import org.jh.forum.client.data.model.CommentElement
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
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
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
                modifier = Modifier.clickable {
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
                        style = MaterialTheme.typography.titleSmall
                    )
                    if (comment.isAuthor) {
                        Spacer(Modifier.width(Dimensions.spaceExtraSmall))
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = MaterialTheme.shapes.extraSmall
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

@Composable
fun CommentEditor(
    onSubmit: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf("") }

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

        // 缩小间距
        Spacer(Modifier.height(Dimensions.spaceSmall))

        // 更紧凑的发布按钮
        Button(
            onClick = {
                if (text.isNotBlank()) {
                    onSubmit(text)
                    text = ""
                }
            },
            enabled = text.isNotBlank(),
            modifier = Modifier.align(Alignment.End),
            shape = MaterialTheme.shapes.small
        ) {
            Text("发布", style = MaterialTheme.typography.labelLarge)
        }
    }
}